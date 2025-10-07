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
     * Analyse 4 : TEB en fonction du SNR (pour canal sans multi-trajet)
     * @throws Exception si une erreur survient lors de l'exécution du simulateur
     */
    public static void analyserSNR() throws Exception {
        System.out.println("=== Analyse TEB = f(SNR) ===");

        int nbPoints = 10;  // SNR de 0 à 9 dB
        float[] valeursTEB = new float[nbPoints];

        // Utiliser au moins 5000 bits pour une mesure fiable du TEB
        int nbBitsAnalyse = Math.max(5000, nbBitsMessage);

        // Faire varier le SNR de 0 à 9 dB
        for (int i = 0; i < nbPoints; i++) {
            float snrCourant = (float) i;

            StringBuilder args = new StringBuilder();
            args.append("-mess ").append(nbBitsAnalyse);
            args.append(" -form ").append(forme);
            args.append(" -nbEch ").append(nbEch);
            if (seed != null) {
                args.append(" -seed ").append(seed);
            }
            args.append(" -snrpb ").append(snrCourant);

            Simulateur sim = new Simulateur(args.toString().split("\\s+"));
            sim.execute();
            float teb = sim.calculTauxErreurBinaire();

            valeursTEB[i] = teb;
            System.out.println(String.format("SNR=%.1f dB => TEB=%.6f", snrCourant, teb));
        }

        VueCourbe courbe4 = new VueCourbe(valeursTEB, "TEB = f(SNR en dB)");
        try { Thread.sleep(200); } catch (Exception e) {}
        System.out.println("Graphique TEB vs SNR généré\n");
    }

    /**
     * Analyse 5 : TEB en fonction du nombre d'échantillons par bit
     * @throws Exception si une erreur survient lors de l'exécution du simulateur
     */
    public static void analyserNbEch() throws Exception {
        System.out.println("=== Analyse TEB = f(nbEch) ===");

        int nbPoints = 10;  // nbEch de 10 à 100
        float[] valeursTEB = new float[nbPoints];

        // Cible: environ 100000 échantillons totaux pour garder le temps de calcul constant
        int cibleEchantillons = 100000;

        // Référence pour garder la variance de bruit constante
        int nbEchReference = 30;  // valeur de référence

        // Faire varier nbEch de 10 à 100 par pas de 10
        for (int i = 0; i < nbPoints; i++) {
            int nbEchCourant = 10 + i * 10;

            // Ajuster le nombre de bits pour garder le nombre d'échantillons constant
            // Minimum 1000 bits pour avoir des statistiques fiables
            int nbBitsAnalyse = Math.max(1000, cibleEchantillons / nbEchCourant);

            // Ajuster le SNR pour garder la variance de bruit constante
            // variance = (puissanceSignal × nEch) / SNR_b
            // Pour variance constante: SNR_b doit être proportionnel à nEch
            float snrAjuste = snrDb + 10.0f * (float)Math.log10((double)nbEchCourant / nbEchReference);

            StringBuilder args = new StringBuilder();
            args.append("-mess ").append(nbBitsAnalyse);
            args.append(" -form ").append(forme);
            args.append(" -nbEch ").append(nbEchCourant);
            if (seed != null) {
                args.append(" -seed ").append(seed);
            }
            args.append(" -snrpb ").append(snrAjuste);

            Simulateur sim = new Simulateur(args.toString().split("\\s+"));
            sim.execute();
            float teb = sim.calculTauxErreurBinaire();

            valeursTEB[i] = teb;
            System.out.println(String.format("nbEch=%d (%d bits) => TEB=%.6f", nbEchCourant, nbBitsAnalyse, teb));
        }

        VueCourbe courbe5 = new VueCourbe(valeursTEB, "TEB = f(nbEch)");
        try { Thread.sleep(200); } catch (Exception e) {}
        System.out.println("Graphique TEB vs nbEch généré\n");
    }

    /**
     * Analyse 6 : TEB en fonction du SNR avec et sans codeur/décodeur
     * Compare l'efficacité du codage avec redondance pour la correction d'erreurs
     * @throws Exception si une erreur survient lors de l'exécution du simulateur
     */
    public static void analyserCodeur() throws Exception {
        System.out.println("=== Analyse TEB avec/sans Codeur = f(SNR) ===");

        int nbPoints = 5;  // SNR de 0 à 8 dB par pas de 2
        float[] valeursTEB_SansCodeur = new float[nbPoints];
        float[] valeursTEB_AvecCodeur = new float[nbPoints];

        // Utiliser le nombre de bits du message comme les autres analyses
        int nbBitsAnalyse = nbBitsMessage;

        // Faire varier le SNR de 0 à 8 dB par pas de 2
        for (int i = 0; i < nbPoints; i++) {
            float snrCourant = (float) (i * 2);

            // Test SANS codeur
            StringBuilder argsSans = new StringBuilder();
            argsSans.append("-mess ").append(nbBitsAnalyse);
            argsSans.append(" -form ").append(forme);
            argsSans.append(" -nbEch ").append(nbEch);
            if (seed != null) {
                argsSans.append(" -seed ").append(seed);
            }
            argsSans.append(" -snrpb ").append(snrCourant);

            Simulateur simSans = new Simulateur(argsSans.toString().split("\\s+"));
            simSans.execute();
            float tebSans = simSans.calculTauxErreurBinaire();
            valeursTEB_SansCodeur[i] = tebSans;

            // Test AVEC codeur
            StringBuilder argsAvec = new StringBuilder();
            argsAvec.append("-mess ").append(nbBitsAnalyse);
            argsAvec.append(" -form ").append(forme);
            argsAvec.append(" -nbEch ").append(nbEch);
            if (seed != null) {
                argsAvec.append(" -seed ").append(seed);
            }
            argsAvec.append(" -snrpb ").append(snrCourant);
            argsAvec.append(" -codeur");

            Simulateur simAvec = new Simulateur(argsAvec.toString().split("\\s+"));
            simAvec.execute();
            float tebAvec = simAvec.calculTauxErreurBinaire();
            valeursTEB_AvecCodeur[i] = tebAvec;

            System.out.println(String.format("SNR=%.1f dB => Sans codeur: TEB=%.6f | Avec codeur: TEB=%.6f",
                                            snrCourant, tebSans, tebAvec));
        }

        // Créer deux graphiques: un pour chaque configuration
        VueCourbe courbeSans = new VueCourbe(valeursTEB_SansCodeur, "TEB sans codeur = f(SNR en dB)");
        try { Thread.sleep(200); } catch (Exception e) {}
        VueCourbe courbeAvec = new VueCourbe(valeursTEB_AvecCodeur, "TEB avec codeur = f(SNR en dB)");
        try { Thread.sleep(200); } catch (Exception e) {}
        System.out.println("Graphiques TEB avec/sans codeur générés\n");
    }

    /**
     * Méthode appelée par Simulateur pour générer les graphiques d'analyse TEB
     * @param nbBits nombre de bits du message
     * @param nbEchantillons nombre d'échantillons par bit
     * @param snr rapport signal/bruit en dB
     * @param forme forme du signal (RZ, NRZ, NRZT)
     * @param seedValue seed pour reproductibilité (peut être null)
     * @param trajets liste des trajets définis par l'utilisateur (peut être null)
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

            // Choisir les analyses en fonction de la présence de multi-trajet
            if (trajets != null && !trajets.isEmpty()) {
                // Analyses pour canal à trajets multiples
                System.out.println("Mode: Canal à trajets multiples");
                analyserNbTrajets();
                Thread.sleep(500);
                analyserAlpha();
                Thread.sleep(500);
                analyserTau();
            } else {
                // Analyses pour canal simple (avec ou sans bruit)
                System.out.println("Mode: Canal simple");
                analyserSNR();
                Thread.sleep(500);
                analyserNbEch();
                Thread.sleep(500);
                analyserCodeur();
            }

            System.out.println("=== Graphiques TEB générés ===\n");

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