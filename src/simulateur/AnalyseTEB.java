package simulateur;

import transmetteurs.Trajet;
import visualisations.VueCourbe;
import java.util.List;

/**
 * Classe pour analyser le Taux d'Erreur Binaire (TEB) en fonction de différents paramètres
 * du canal à trajets multiples.
 * Génère 3 graphiques avec VueCourbe :
 * 1. TEB = f(NbTrajets)
 * 2. TEB = f(alpha_i)
 * 3. TEB = f(tau_i)
 */
public class AnalyseTEB {

    // Ces paramètres sont passés par le Simulateur via genererGraphiques()
    private static int nbBitsMessage;
    private static int nbEch;
    private static float snrDb;
    private static String forme;
    private static Integer seed;
    private static List<Trajet> trajetsUtilisateur;

    /**
     * Construit un analyseur de TEB avec les paramètres par défaut.
     */
    public AnalyseTEB() {
        // rien à initialiser pour l'instant
    }

    /**
     * Analyse 1 : TEB en fonction du nombre de trajets
     * On ajoute progressivement les trajets définis par l'utilisateur
     * @throws Exception si une erreur survient lors de l'exécution du simulateur
     *                   ou lors de l'analyse des résultats.
     */
    public static void analyserNbTrajets() throws Exception {
        System.out.println("=== Analyse TEB = f(NbTrajets) ===");

        // Si pas de trajets définis, on ne fait rien
        if (trajetsUtilisateur == null || trajetsUtilisateur.isEmpty()) {
            System.out.println("Pas de trajets définis, analyse ignorée\n");
            return;
        }

        int nbTrajetsMax = trajetsUtilisateur.size();
        int nbPoints = nbTrajetsMax + 1;  // 0 à nbTrajetsMax
        float[] valeursTEB = new float[nbPoints];

        // Tester de 0 à nbTrajetsMax trajets
        for (int nbTrajets = 0; nbTrajets < nbPoints; nbTrajets++) {
            StringBuilder args = new StringBuilder();
            args.append("-mess ").append(nbBitsMessage);
            args.append(" -form ").append(forme);
            args.append(" -nbEch ").append(nbEch);
            if (seed != null) {
                args.append(" -seed ").append(seed);
            }
            args.append(" -snrpb ").append(snrDb);

            if (nbTrajets > 0) {
                args.append(" -ti");
                for (int i = 0; i < nbTrajets; i++) {
                    Trajet t = trajetsUtilisateur.get(i);
                    args.append(" ").append(t.getTau()).append(" ").append(t.getAlpha());
                }
            }

            Simulateur sim = new Simulateur(args.toString().split("\\s+"));
            sim.execute();
            float teb = sim.calculTauxErreurBinaire();

            valeursTEB[nbTrajets] = teb;
            System.out.println("NbTrajets=" + nbTrajets + " => TEB=" + teb);
        }

        VueCourbe courbe1 = new VueCourbe(valeursTEB, "TEB = f(Nombre de trajets)");
        try { Thread.sleep(200); } catch (Exception e) {}  // Laisser le temps à la fenêtre de se créer
        System.out.println("Graphique TEB vs NbTrajets généré\n");
    }

    /**
     * Analyse 2 : TEB en fonction du coefficient d'atténuation alpha
     * On prend les trajets utilisateur et on fait varier alpha du premier trajet
     * @throws Exception si une erreur survient lors de l'exécution du simulateur
     * ou lors de l'analyse des résultats.
     */
    public static void analyserAlpha() throws Exception {
        System.out.println("=== Analyse TEB = f(alpha_i) ===");

        // Si pas de trajets définis, on ne fait rien
        if (trajetsUtilisateur == null || trajetsUtilisateur.isEmpty()) {
            System.out.println("Pas de trajets définis, analyse ignorée\n");
            return;
        }

        int nbPoints = 9;  // alpha de 0.1 à 0.9
        float[] valeursTEB = new float[nbPoints];

        // Récupérer le tau du premier trajet
        int tauPremier = trajetsUtilisateur.get(0).getTau();

        // Faire varier alpha de 0.1 à 0.9
        for (int i = 0; i < nbPoints; i++) {
            float alpha = 0.1f + i * 0.1f;

            StringBuilder args = new StringBuilder();
            args.append("-mess ").append(nbBitsMessage);
            args.append(" -form ").append(forme);
            args.append(" -nbEch ").append(nbEch);
            if (seed != null) {
                args.append(" -seed ").append(seed);
            }
            args.append(" -snrpb ").append(snrDb);
            args.append(" -ti");
            args.append(" ").append(tauPremier).append(" ").append(alpha);  // trajet 1 : alpha variable
            // Ajouter les autres trajets tels quels
            for (int j = 1; j < trajetsUtilisateur.size(); j++) {
                Trajet t = trajetsUtilisateur.get(j);
                args.append(" ").append(t.getTau()).append(" ").append(t.getAlpha());
            }

            Simulateur sim = new Simulateur(args.toString().split("\\s+"));
            sim.execute();
            float teb = sim.calculTauxErreurBinaire();

            valeursTEB[i] = teb;
            System.out.println(String.format("Alpha=%.1f => TEB=%.6f", alpha, teb));
        }

        VueCourbe courbe2 = new VueCourbe(valeursTEB, "TEB = f(alpha_i)");
        try { Thread.sleep(200); } catch (Exception e) {}  // Laisser le temps à la fenêtre de se créer
        System.out.println("Graphique TEB vs Alpha généré\n");
    }

    /**
     * Analyse 3 : TEB en fonction du délai tau
     * On prend les trajets utilisateur et on fait varier tau du premier trajet
     * @throws Exception si une erreur survient lors de l'exécution du simulateur
     * ou lors de l'analyse des résultats.
     */
    public static void analyserTau() throws Exception {
        System.out.println("=== Analyse TEB = f(tau_i) ===");

        // Si pas de trajets définis, on ne fait rien
        if (trajetsUtilisateur == null || trajetsUtilisateur.isEmpty()) {
            System.out.println("Pas de trajets définis, analyse ignorée\n");
            return;
        }

        int nbPoints = 20;  // tau de 1 à 20
        float[] valeursTEB = new float[nbPoints];

        // Récupérer l'alpha du premier trajet
        float alphaPremier = trajetsUtilisateur.get(0).getAlpha();

        // Faire varier tau de 1 à 20 échantillons
        for (int tau = 1; tau <= nbPoints; tau++) {
            StringBuilder args = new StringBuilder();
            args.append("-mess ").append(nbBitsMessage);
            args.append(" -form ").append(forme);
            args.append(" -nbEch ").append(nbEch);
            if (seed != null) {
                args.append(" -seed ").append(seed);
            }
            args.append(" -snrpb ").append(snrDb);
            args.append(" -ti");
            args.append(" ").append(tau).append(" ").append(alphaPremier);  // trajet 1 : tau variable
            // Ajouter les autres trajets tels quels
            for (int j = 1; j < trajetsUtilisateur.size(); j++) {
                Trajet t = trajetsUtilisateur.get(j);
                args.append(" ").append(t.getTau()).append(" ").append(t.getAlpha());
            }

            Simulateur sim = new Simulateur(args.toString().split("\\s+"));
            sim.execute();
            float teb = sim.calculTauxErreurBinaire();

            valeursTEB[tau - 1] = teb;
            System.out.println("Tau=" + tau + " => TEB=" + teb);
        }

        VueCourbe courbe3 = new VueCourbe(valeursTEB, "TEB = f(tau_i)");
        try { Thread.sleep(200); } catch (Exception e) {}  // Laisser le temps à la fenêtre de se créer
        System.out.println("Graphique TEB vs Tau généré\n");
    }

    /**
     * Méthode appelée par Simulateur pour générer les 3 graphiques d'analyse TEB
     * @param nbBits nombre de bits du message
     * @param nbEchantillons nombre d'échantillons par bit
     * @param snr rapport signal/bruit en dB
     * @param forme forme du signal (RZ, NRZ, NRZT)
     * @param seedValue seed pour reproductibilité (peut être null)
     * @param trajets liste des trajets définis par l'utilisateur
     */
    public static void genererGraphiques(int nbBits, int nbEchantillons, float snr, String forme, Integer seedValue, List<Trajet> trajets) {
        try {
            // Configurer les paramètres
            nbBitsMessage = nbBits;
            nbEch = nbEchantillons;
            snrDb = snr;
            AnalyseTEB.forme = forme;
            seed = seedValue;
            trajetsUtilisateur = trajets;

            System.out.println("\n=== Génération des graphiques d'analyse TEB ===");
            System.out.println("Paramètres: mess=" + nbBits + ", nbEch=" + nbEchantillons +
                             ", snr=" + snr + "dB, forme=" + forme);

            // Lancer les 3 analyses avec un délai pour laisser l'interface se rafraîchir
            analyserNbTrajets();
            Thread.sleep(500);  // Attendre 500ms
            analyserAlpha();
            Thread.sleep(500);
            analyserTau();

            System.out.println("=== 3 graphiques TEB générés ===\n");

        } catch (Exception e) {
            System.err.println("Erreur lors de l'analyse TEB : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Point d'entrée principal permettant de tester l'analyse du TEB
     * en mode autonome (standalone).
     * <p>
     * Utilise des paramètres par défaut (nombre de bits, nombre d'échantillons,
     * forme du signal, seed, et trajets prédéfinis) afin de générer
     * automatiquement les graphiques de TEB.
     * </p>
     *
     * @param args arguments de la ligne de commande (non utilisés ici)
     */
    public static void main(String[] args) {
        // Utiliser des paramètres par défaut pour le test standalone
        List<Trajet> trajetsTest = new java.util.ArrayList<>();
        trajetsTest.add(new Trajet(6, 0.4f));
        trajetsTest.add(new Trajet(4, 0.1f));
        trajetsTest.add(new Trajet(3, 0.8f));
        genererGraphiques(1000, 30, 10.0f, "RZ", 42, trajetsTest);
    }
}