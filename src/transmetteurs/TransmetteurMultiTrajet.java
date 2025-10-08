package transmetteurs;

import destinations.DestinationInterface;
import emmetteurs.Emetteur;
import information.Information;
import information.InformationNonConformeException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Classe représentant un transmetteur avec canal à trajets multiples et bruité.
 * Le signal reçu est modélisé par : r(t) = s(t) + Somme(α_i*s(t-tau_i)) + b(t)
 * où :
 * - s(t) est le signal direct
 * - α_k·s(t-tau_k) représente les N trajets réfléchis avec atténuation et retard
 * - b(t) est un bruit blanc gaussien centré
 * @param <E> : type des données d'entrée
 */
public class TransmetteurMultiTrajet<E> extends Transmetteur {

    private List<Trajet> trajets;            // liste des trajets réfléchis
    private float SNRdB;                     // rapport signal/bruit en dB
    private float puissanceSignal;           // puissance du signal
    private float variance;                  // variance du bruit gaussien
    private Random rand;                     // générateur aléatoire
    private int nbEch;                       // nombre d'échantillons par bit

    /**
     * Constructeur avec liste de trajets (bruit totalement aléatoire)
     * @param trajets la liste des trajets réfléchis (dt en nombre de bits)
     * @param SNRdB le rapport signal/bruit en dB
     * @param nbEch le nombre d'échantillons par bit
     */
    public TransmetteurMultiTrajet(List<Trajet> trajets, float SNRdB, int nbEch) {
        super();
        if (trajets == null || trajets.isEmpty()) {
            throw new IllegalArgumentException("La liste de trajets ne peut pas être vide");
        }
        this.nbEch = nbEch;
        // Convertir les retards dt (en bits) en échantillons (dt × nbEch)
        this.trajets = new ArrayList<>();
        for (Trajet t : trajets) {
            this.trajets.add(new Trajet(t.getTau() * nbEch, t.getAlpha()));
        }
        this.SNRdB = SNRdB;
        this.rand = new Random(); // aléatoire total
    }

    /**
     * Constructeur avec liste de trajets et seed (bruit reproductible)
     * @param trajets la liste des trajets réfléchis (dt en nombre de bits)
     * @param SNRdB le rapport signal/bruit en dB
     * @param nbEch le nombre d'échantillons par bit
     * @param seed la graine pour le générateur aléatoire
     */
    public TransmetteurMultiTrajet(List<Trajet> trajets, float SNRdB, int nbEch, int seed) {
        super();
        if (trajets == null || trajets.isEmpty()) {
            throw new IllegalArgumentException("La liste de trajets ne peut pas être vide");
        }
        this.nbEch = nbEch;
        // Convertir les retards dt (en bits) en échantillons (dt × nbEch)
        this.trajets = new ArrayList<>();
        for (Trajet t : trajets) {
            this.trajets.add(new Trajet(t.getTau() * nbEch, t.getAlpha()));
        }
        this.SNRdB = SNRdB;
        this.rand = new Random(seed); // reproductible
    }

    @Override
    @SuppressWarnings("unchecked")
    public void connecter(DestinationInterface destination) {
        destinationsConnectees.add(destination);
    }

    /**
     * Permet de recevoir l'information depuis une source ou un émetteur
     * @param information l'information à recevoir
     * @throws InformationNonConformeException si l'information est nulle
     */
    @Override
    @SuppressWarnings("unchecked")
    public void recevoir(Information information) throws InformationNonConformeException {
        if (information == null) {
            throw new InformationNonConformeException("L'information est nulle");
        }
        this.informationRecue = information;
    }

    /**
     * Calcule la puissance du signal reçu : Ps = (1/N) * Somme(s^2)
     */
    private void calculerPuissanceSignal() {
        float somme = 0;
        int count = 0;
        for (Object obj : informationRecue) {
            float echantillon = (Float) obj;
            somme += echantillon * echantillon;
            count++;
        }
        this.puissanceSignal = somme / count;
    }

    /**
     * Calcule la variance du bruit gaussien à partir du SNR
     *
     * Pour SNR par bit avec moyennage au récepteur :
     * - Utilise la puissance du signal AVANT multipath (signal direct uniquement)
     * - Applique le facteur nbEch pour compenser le moyennage au récepteur
     * - variance = (puissanceSignal × nEch) / SNR_b
     */
    private void calculerVariance() {
        calculerPuissanceSignal();
        float snrLineaire = (float) Math.pow(10, SNRdB / 10.0);
        this.variance = (puissanceSignal * nbEch) / snrLineaire;
    }

    /**
     * Génère le signal avec trajets multiples et bruit gaussien
     * Formule : r(t) = s(t) + Somme(α_k*s(t-tau_k)) + b(t)
     */
    @SuppressWarnings("unchecked")
    private void genererSignalMultiTrajet() {
        this.informationEmise = new Information<Float>();
        calculerVariance();
        double sqrtVariance = Math.sqrt(variance);

        int N = informationRecue.nbElements(); //nb ech analogiques

        for (int i = 0; i < N; i++) {
            // 1. Signal direct s(t)
            float echantillon = (Float) informationRecue.iemeElement(i);
            float signalTotal = echantillon; // soit le premier terme s(t) (cf. l99)

            // 2. Somme de tous les trajets réfléchis : Somme(α_k*s(t-tau_k))
            for (Trajet trajet : trajets) {
                int tau = trajet.getTau();
                float alpha = trajet.getAlpha();

                // Vérifier qu'on a un signal à réfléchir (i >= tau) : i-tau
                if (i >= tau) {
                    float signalRetarde = (Float) informationRecue.iemeElement(i - tau);
                    signalTotal += alpha * signalRetarde;
                }
                // Sinon, pas encore de signal à réfléchir pour ce trajet
            }

            // 3. Ajout du bruit blanc gaussien centré : b(t)
            double bruitEchantillon = rand.nextGaussian() * sqrtVariance;
            signalTotal += (float) bruitEchantillon;

            // Ajout du signal total à l'information émise
            this.informationEmise.add(signalTotal);
        }
    }

    /**
     * Émet l'information avec trajets multiples et bruit vers toutes les destinations connectées
     * @throws InformationNonConformeException si un problème survient lors de l'émission
     */
    @Override
    @SuppressWarnings("unchecked")
    public void emettre() throws InformationNonConformeException {
        genererSignalMultiTrajet();

        Iterator<DestinationInterface<E>> it = destinationsConnectees.iterator();
        while (it.hasNext()) {
            DestinationInterface<E> destinationConnectee = it.next();
            destinationConnectee.recevoir(this.informationEmise);
        }
    }

    /**
     * Retourne la liste des trajets réfléchis
     * @return la liste des trajets
     */
    public List<Trajet> getTrajets() {
        return new ArrayList<>(trajets);
    }

    /**
     * Ajoute un nouveau trajet réfléchi
     * @param trajet le trajet à ajouter
     */
    public void ajouterTrajet(Trajet trajet) {
        if (trajet != null) {
            this.trajets.add(trajet);
        }
    }

    /**
     * Supprime tous les trajets
     */
    public void clearTrajets() {
        this.trajets.clear();
    }

    /**
     * Retourne le rapport signal/bruit en dB
     * @return le SNR en dB
     */
    public float getSNRdB() {
        return SNRdB;
    }

    /**
     * Modifie le rapport signal/bruit
     * @param SNRdB le nouveau SNR en dB
     */
    public void setSNRdB(float SNRdB) {
        this.SNRdB = SNRdB;
    }

    /**
     * Retourne la puissance du signal calculée
     * @return la puissance du signal
     */
    public float getPuissanceSignal() {
        return puissanceSignal;
    }

    /**
     * Retourne la variance du bruit calculée
     * @return la variance du bruit
     */
    public float getVariance() {
        return variance;
    }
}