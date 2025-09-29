# TP4 - Modifications du 29/09 : Canal à trajets multiples et bruité

## Vue d'ensemble
Implémentation d'un canal à trajets multiples avec bruit gaussien selon le modèle :

```
r(t) = s(t) + Σ(αₖ·s(t-τₖ)) + b(t)
```

où :
- `s(t)` : signal direct
- `αₖ·s(t-τₖ)` : N trajets réfléchis avec atténuation αₖ et retard τₖ
- `b(t)` : bruit blanc gaussien centré

---

## 1. Nouveaux fichiers créés

### 1.1 `src/transmetteurs/Trajet.java`
**Rôle** : Représente un trajet réfléchi unique dans un canal à trajets multiples.

**Attributs** :
- `int tau` : retard en nombre d'échantillons
- `float alpha` : coefficient d'atténuation (0 < α < 1)

**Méthodes** :
- Constructeur avec validation (tau ≥ 0, 0 < alpha < 1)
- Getters/setters avec validation
- `toString()` pour debug

**Exemple** :
```java
Trajet t = new Trajet(5, 0.8f); // retard de 5 échantillons, atténuation de 0.8
```

---

### 1.2 `src/transmetteurs/TransmetteurMultiTrajet.java`
**Rôle** : Implémente un transmetteur avec canal à trajets multiples et bruit gaussien.

**Attributs** :
- `List<Trajet> trajets` : liste des trajets réfléchis
- `float SNRdB` : rapport signal/bruit en dB
- `int nbEch` : nombre d'échantillons par bit
- `float puissanceSignal` : puissance calculée du signal
- `float variance` : variance du bruit gaussien
- `Random rand` : générateur pour le bruit

**Constructeurs** :
```java
// Sans seed (bruit aléatoire)
TransmetteurMultiTrajet(List<Trajet> trajets, float SNRdB, int nbEch)

// Avec seed (bruit reproductible)
TransmetteurMultiTrajet(List<Trajet> trajets, float SNRdB, int nbEch, int seed)
```

**Point clé** : Le constructeur convertit automatiquement les retards `dt` (en bits) en échantillons via `dt × nbEch`. Ceci est crucial pour que les interférences inter-symboles (ISI) soient correctes.

**Méthodes principales** :

#### `calculerPuissanceSignal()`
Calcule la puissance du signal :
```
Ps = (1/N) × Σ(s²)
```

#### `calculerVariance()`
Calcule la variance du bruit à partir du SNR :
```
variance = Ps / 10^(SNR/10)
```

#### `genererSignalMultiTrajet()`
Génère le signal avec trajets multiples et bruit selon la formule :

```java
for (int i = 0; i < N; i++) {
    // 1. Signal direct s(t)
    float signalTotal = informationRecue[i];

    // 2. Trajets réfléchis : Σ(αₖ·s(t-τₖ))
    for (Trajet trajet : trajets) {
        if (i >= tau) {
            signalTotal += alpha * informationRecue[i - tau];
        }
    }

    // 3. Bruit gaussien : b(t)
    signalTotal += rand.nextGaussian() * sqrt(variance);

    informationEmise.add(signalTotal);
}
```

---

## 2. Fichiers modifiés

### 2.1 `src/simulateur/Simulateur.java`

#### Nouveaux attributs :
```java
private List<Trajet> trajetsMultiples = null;  // Liste des trajets pour -ti
```

#### Suppression :
- Attributs `bruitSeeded` et `bruitSeed` (remplacés par l'option unique `-seed`)
- Option `-seedBruit` (non conforme au PDF)

#### Modification : Changement `-ne` → `-nbEch`
Ligne 222 : pour être conforme au document de spécification.

#### Ajout : Parsing de l'option `-ti`
**Lignes 242-264** :
```java
else if (args[i].matches("-ti")) {
    trajetsMultiples = new ArrayList<>();
    i++;
    // Lire jusqu'à 5 couples (dt, ar)
    while (i < args.length && !args[i].startsWith("-") && trajetsMultiples.size() < 5) {
        int dt = Integer.valueOf(args[i]);
        i++;
        if (i >= args.length || args[i].startsWith("-")) {
            throw new ArgumentsException("Valeur ar manquante après dt pour -ti");
        }
        float ar = Float.valueOf(args[i]);
        trajetsMultiples.add(new Trajet(dt, ar));  // dt en BITS
        i++;
    }
    i--; // Compenser le i++ du for
}
```

**Format** : `-ti dt1 ar1 dt2 ar2 ... dt5 ar5` (max 5 trajets)
- `dt` : retard en nombre de **bits** (sera converti en échantillons)
- `ar` : atténuation (0 < ar < 1)

#### Modification : Création du transmetteur
**Lignes 107-130** :
```java
if (trajetsMultiples != null && !trajetsMultiples.isEmpty()) {
    // Canal à trajets multiples et bruité
    if (SNRpB == null) {
        throw new ArgumentsException("Un SNR doit être spécifié avec -snrpb pour utiliser -ti");
    }
    if (aleatoireAvecGerme && seed != null) {
        transmetteurLogique = new TransmetteurMultiTrajet<>(trajetsMultiples, SNRpB, nEch, seed);
    } else {
        transmetteurLogique = new TransmetteurMultiTrajet<>(trajetsMultiples, SNRpB, nEch);
    }
}
else if (SNRpB == null) {
    // Canal parfait
    transmetteurLogique = new TransmetteurParfait();
}
else {
    // Canal bruité simple
    if (aleatoireAvecGerme && seed != null) {
        transmetteurLogique = new TransmetteurImparfait<>(nEch, SNRpB, seed);
    } else {
        transmetteurLogique = new TransmetteurImparfait<>(nEch, SNRpB);
    }
}
```

**Point important** : `nEch` est maintenant passé au constructeur de `TransmetteurMultiTrajet` pour la conversion dt→échantillons.

#### Ajout : Debug output
**Lignes 284-300** : Messages de debug pour tracer le signal à chaque étape :
- Nombre d'éléments émis/reçus
- Premiers échantillons à chaque étape
- Permet de vérifier que le signal est bien corrompu

**Lignes 322-336** : Debug du calcul du TEB :
- Affiche les tailles comparées
- Affiche les 5 premières erreurs détectées
- Affiche le nombre total d'erreurs

---

### 2.2 `src/transmetteurs/Transmetteur.java`

#### Ajout : Méthode `connecter(Emetteur)`
**Raison** : L'interface `SourceInterface` requiert cette méthode mais elle n'était pas définie dans la classe parente.

```java
public void connecter(emmetteurs.Emetteur emetteur) {
    // Un transmetteur ne se connecte pas à un émetteur
    // Cette méthode est requise par SourceInterface mais n'est pas utilisée
}
```

**Problème résolu** : Évite que chaque classe fille (TransmetteurParfait, TransmetteurImparfait, TransmetteurMultiTrajet) doive implémenter cette méthode.

---

### 2.3 `src/emmetteurs/Emetteur.java`

#### Correction : Méthode `connecter(Transmetteur)`
**Avant (lignes 119-124)** : Logique complètement cassée
```java
// CASSÉ !
for (DestinationInterface<Float> destinationConnectee : destinationsConnectees) {
    if (destinationConnectee == transmetteur) {
        return;
    } else {
        destinationsConnectees.add(transmetteur);  // Ajouté dans le else !
    }
}
```

**Après** :
```java
public void connecter(Transmetteur transmetteurLogique) {
    if (!destinationsConnectees.contains(transmetteurLogique)) {
        destinationsConnectees.add(transmetteurLogique);
    }
}
```

**Bug résolu** : Cette erreur causait un `NullPointerException` car le transmetteur n'était jamais ajouté aux destinations, donc il ne recevait jamais le signal.

---

### 2.4 `src/sources/SourceAleatoire.java`

#### Ajout : Méthode `connecter(Emetteur)`
```java
@Override
public void connecter(Emetteur emetteur) {
    destinationsConnectees.add(emetteur);
}
```

**Raison** : Méthode requise par `SourceInterface` pour connecter la source à l'émetteur.

---

### 2.5 `src/sources/SourceFixe.java`

#### Correction : Méthode `connecter(Emetteur)`
Implémentation correcte ajoutée.

#### Correction : `connecterSonde()`
Suppression de `@Override` qui était incorrect (la méthode n'override rien).

---

### 2.6 `src/visualisations/SondeLogique.java`

#### Suppression : `implements DestinationInterface<Float>`
**Avant** :
```java
public class SondeLogique extends Sonde<Boolean> implements DestinationInterface<Float>
```

**Après** :
```java
public class SondeLogique extends Sonde<Boolean>
```

**Raison** : Conflit de types - la classe hérite déjà de `Sonde<Boolean>` qui implémente `DestinationInterface<Boolean>`. Impossible d'implémenter aussi `DestinationInterface<Float>`.

---

### 2.7 `src/transmetteurs/Recepteur.java`

#### Ajout : Debug output dans `emettre()`
**Lignes 78-81** :
```java
if (i < 3) {
    System.err.println("DEBUG Recepteur: bit " + i + " moyenne=" + moy + " seuil=" + seuil + " -> " + bits[i]);
}
```

Permet de vérifier les moyennes calculées et les décisions de seuillage.

---

## 3. Bug critique résolu : Conversion dt → échantillons

### Le problème
Dans la version initiale, `dt` (retard en bits) était directement utilisé comme retard en échantillons :

```java
// INCORRECT
float signalRetarde = informationRecue.iemeElement(i - dt);
```

Avec `dt=1` et `nbEch=5` :
- Retard effectif = **1 échantillon** (0.2 bit)
- Interférence quasi-nulle
- TEB très faible même avec conditions extrêmes

### La solution
Convertir `dt` en échantillons dans le constructeur :

```java
// CORRECT
this.trajets = new ArrayList<>();
for (Trajet t : trajets) {
    this.trajets.add(new Trajet(t.getTau() * nbEch, t.getAlpha()));
}
```

Avec `dt=1` et `nbEch=5` :
- Retard effectif = **5 échantillons** (1 bit complet)
- Vraies interférences inter-symboles (ISI)
- TEB réaliste : **0.005 → 0.27** (×54 !)

---

## 4. Exemples de commandes

### Canal parfait (pas de bruit, pas de trajets)
```bash
java simulateur.Simulateur -mess 200 -form RZ -nbEch 30
```
**Résultat attendu** : TEB = 0.0

### Canal bruité simple (bruit uniquement)
```bash
java simulateur.Simulateur -mess 200 -form RZ -nbEch 30 -snrpb 5
```
**Résultat attendu** : TEB faible (< 0.01)

### Canal à trajets multiples modéré
```bash
java simulateur.Simulateur -mess 200 -seed 12345 -form RZ -nbEch 30 -snrpb 0 -ti 1 0.5
```
**Paramètres** :
- SNR = 0 dB (bruit modéré)
- 1 trajet : dt=1 bit, α=0.5 (atténuation moyenne)

**Résultat attendu** : TEB faible à modéré

### Canal à trajets multiples sévère
```bash
java simulateur.Simulateur -mess 200 -seed 12345 -form RZ -nbEch 5 -snrpb -5 -ti 1 0.9 2 0.8
```
**Paramètres** :
- SNR = -5 dB (bruit très fort)
- nbEch = 5 (peu d'échantillons → moyenne moins stable)
- 2 trajets :
  - Trajet 1 : dt=1 bit (5 éch), α=0.9 (très fort)
  - Trajet 2 : dt=2 bits (10 éch), α=0.8 (fort)

**Résultat observé** : TEB = **0.27** (27% d'erreurs)

---

## 5. Résumé des modifications par catégorie

### Nouveaux fichiers (2)
1. `Trajet.java` - Modèle d'un trajet réfléchi
2. `TransmetteurMultiTrajet.java` - Transmetteur avec trajets multiples et bruit

### Modifications d'architecture (4)
1. `Transmetteur.java` - Ajout de `connecter(Emetteur)`
2. `Emetteur.java` - **Correction critique** de `connecter(Transmetteur)`
3. `SourceAleatoire.java` - Ajout de `connecter(Emetteur)`
4. `SourceFixe.java` - Ajout de `connecter(Emetteur)`

### Modifications du simulateur (1)
1. `Simulateur.java` - Parsing `-ti`, création TransmetteurMultiTrajet, debug

### Corrections de bugs (2)
1. `SondeLogique.java` - Suppression de conflit de types
2. **TransmetteurMultiTrajet.java** - **Conversion dt × nbEch**

### Debug (2)
1. `Simulateur.java` - Traces d'exécution
2. `Recepteur.java` - Traces de décodage

---

## 6. Points d'attention

### Option `-seed`
Une seule option `-seed` contrôle **tous** les générateurs aléatoires :
- Message aléatoire (SourceAleatoire)
- Bruit gaussien (TransmetteurImparfait, TransmetteurMultiTrajet)

### Format `-ti`
```
-ti dt1 ar1 [dt2 ar2] [dt3 ar3] [dt4 ar4] [dt5 ar5]
```
- Maximum 5 trajets
- `dt` en nombre de **bits** (pas d'échantillons)
- `ar` entre 0 et 1 (atténuation)
- Requiert `-snrpb` (le canal à trajets multiples inclut du bruit)

### Interférences inter-symboles (ISI)
Les trajets avec `dt ≥ 1` créent des interférences entre bits différents. Plus `dt` est grand, plus l'interférence affecte des bits éloignés.

### Impact de `nbEch`
- `nbEch` élevé (ex: 30) → moyennes stables → TEB faible
- `nbEch` faible (ex: 5) → moyennes instables → TEB élevé

---

## 7. Tests de validation

### Test 1 : Pas de régression (canal parfait)
```bash
java simulateur.Simulateur -mess 100 -form NRZ -nbEch 30
```
**Attendu** : TEB = 0.0

### Test 2 : Canal bruité simple fonctionne toujours
```bash
java simulateur.Simulateur -mess 200 -form RZ -nbEch 30 -snrpb 10
```
**Attendu** : TEB très faible (< 0.001)

### Test 3 : Trajets multiples génère des erreurs
```bash
java simulateur.Simulateur -mess 200 -seed 12345 -form RZ -nbEch 5 -snrpb -5 -ti 1 0.9 2 0.8
```
**Attendu** : TEB élevé (> 0.2)

### Test 4 : Reproductibilité avec seed
Exécuter 2 fois la même commande avec `-seed` doit donner le même TEB.

---

## 8. Formules mathématiques utilisées

### Signal reçu
```
r(t) = s(t) + Σ(αₖ·s(t-τₖ)) + b(t)
      k=1..N
```

### Puissance du signal
```
Ps = (1/N) × Σ s²(i)
            i=1..N
```

### Variance du bruit
```
SNR = Ps / Pb
SNR_dB = 10·log₁₀(SNR)
variance = Pb = Ps / 10^(SNR_dB/10)
```

### Bruit gaussien
```
b(t) ~ N(0, variance)
b(t) = nextGaussian() × √variance
```

### Décodage (récepteur)
```
moyenne = (1/nbEch) × Σ r(j)
                      j=début..fin

bit = {
  false  si moyenne < seuil
  true   si moyenne ≥ seuil
}
```

---

**Date** : 29/09/2025
**Auteurs** : Louis RETIF + Claude Code
**Version** : Finale après correction du bug dt×nbEch