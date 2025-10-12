package simulateur;

import transmetteurs.Trajet;
import visualisations.VueCourbe;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

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
     * Exporte les données TEB vers un fichier CSV
     * @param fileName nom du fichier (sans extension)
     * @param xLabel label de l'axe X
     * @param xValues valeurs de l'axe X
     * @param yValues valeurs de l'axe Y (TEB)
     */
    private static void exportToCSV(String fileName, String xLabel, float[] xValues, float[] yValues) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("TEB_" + fileName + ".csv"))) {
            writer.println(xLabel + ",TEB");
            for (int i = 0; i < xValues.length && i < yValues.length; i++) {
                writer.println(xValues[i] + "," + yValues[i]);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'export CSV de " + fileName + ": " + e.getMessage());
        }
    }

    /**
     * Exporte les données TEB vers un fichier CSV (version simplifiée pour index)
     * @param fileName nom du fichier (sans extension)
     * @param xLabel label de l'axe X
     * @param yValues valeurs de l'axe Y (TEB), l'index sert de valeur X
     */
    private static void exportToCSV(String fileName, String xLabel, float[] yValues) {
        float[] xValues = new float[yValues.length];
        for (int i = 0; i < yValues.length; i++) {
            xValues[i] = i;
        }
        exportToCSV(fileName, xLabel, xValues, yValues);
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
        exportToCSV("NbTrajets", "NbTrajets", valeursTEB);
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
        float[] valeursAlpha = new float[nbPoints];

        // Récupérer le tau du premier trajet
        int tauPremier = trajetsUtilisateur.get(0).getTau();

        // Faire varier alpha de 0.1 à 0.9
        for (int i = 0; i < nbPoints; i++) {
            float alpha = 0.1f + i * 0.1f;
            valeursAlpha[i] = alpha;

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
        exportToCSV("Alpha", "Alpha", valeursAlpha, valeursTEB);
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
        float[] valeursTau = new float[nbPoints];

        // Récupérer l'alpha du premier trajet
        float alphaPremier = trajetsUtilisateur.get(0).getAlpha();

        // Faire varier tau de 1 à 20 échantillons
        for (int tau = 1; tau <= nbPoints; tau++) {
            valeursTau[tau - 1] = tau;

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
        exportToCSV("Tau", "Tau", valeursTau, valeursTEB);
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
        float[] valeursSNR = new float[nbPoints];

        // Utiliser le nombre de bits du message
        int nbBitsAnalyse = nbBitsMessage;

        // Faire varier le SNR de 0 à 9 dB
        for (int i = 0; i < nbPoints; i++) {
            float snrCourant = (float) i;
            valeursSNR[i] = snrCourant;

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
        exportToCSV("SNR", "SNR_dB", valeursSNR, valeursTEB);
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
        float[] valeursNbEch = new float[nbPoints];

        // Référence pour garder la variance de bruit constante
        int nbEchReference = 30;  // valeur de référence

        // Faire varier nbEch de 10 à 100 par pas de 10
        for (int i = 0; i < nbPoints; i++) {
            int nbEchCourant = 10 + i * 10;
            valeursNbEch[i] = nbEchCourant;

            // Utiliser le nombre de bits du message
            int nbBitsAnalyse = nbBitsMessage;

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
            System.out.println(String.format("nbEch=%d => TEB=%.6f", nbEchCourant, teb));
        }

        VueCourbe courbe5 = new VueCourbe(valeursTEB, "TEB = f(nbEch)");
        try { Thread.sleep(200); } catch (Exception e) {}
        exportToCSV("NbEch", "NbEch", valeursNbEch, valeursTEB);
        System.out.println("Graphique TEB vs nbEch généré\n");
    }

    /**
     * Analyse 6 : TEB en fonction du SNR avec et sans codeur/décodeur
     * Compare l'efficacité du codage avec redondance pour la correction d'erreurs
     * @throws Exception si une erreur survient lors de l'exécution du simulateur
     */
    public static void analyserCodeur() throws Exception {
        System.out.println("=== Analyse TEB avec/sans Codeur = f(SNR) ===");

        int nbPoints = 10;  // SNR de 0 à 9 dB
        float[] valeursTEB_SansCodeur = new float[nbPoints];
        float[] valeursTEB_AvecCodeur = new float[nbPoints];
        float[] valeursSNR = new float[nbPoints];

        // Utiliser le nombre de bits du message comme les autres analyses
        int nbBitsAnalyse = nbBitsMessage;

        // Faire varier le SNR de 0 à 9 dB
        for (int i = 0; i < nbPoints; i++) {
            float snrCourant = (float) i;
            valeursSNR[i] = snrCourant;

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
        exportToCSV("Codeur_Sans", "SNR_dB", valeursSNR, valeursTEB_SansCodeur);
        exportToCSV("Codeur_Avec", "SNR_dB", valeursSNR, valeursTEB_AvecCodeur);
        System.out.println("Graphiques TEB avec/sans codeur générés\n");
    }

    /**
     * Analyse de comparaison : TEB en fonction du SNR pour NRZ, NRZT et RZ
     * Génère un fichier CSV pour chaque type de codage
     * @param nbBits nombre de bits du message
     * @param nbEchantillons nombre d'échantillons par bit
     * @param snr rapport signal/bruit en dB de référence
     * @param seedValue seed pour reproductibilité (peut être null)
     * @throws Exception si une erreur survient lors de l'exécution du simulateur
     */
    public static void analyserComparaison(int nbBits, int nbEchantillons, float snr, Integer seedValue) throws Exception {
        // Configurer les paramètres
        nbBitsMessage = nbBits;
        nbEch = nbEchantillons;
        snrDb = snr;
        seed = seedValue;

        System.out.println("=== Analyse Comparaison TEB : NRZ vs NRZT vs RZ ===");

        String[] formes = {"NRZ", "NRZT", "RZ"};
        int nbPoints = 10;  // SNR de 0 à 9 dB

        for (String formeCourante : formes) {
            float[] valeursTEB = new float[nbPoints];
            float[] valeursSNR = new float[nbPoints];

            System.out.println("Analyse pour " + formeCourante + "...");

            // Faire varier le SNR de 0 à 9 dB
            for (int i = 0; i < nbPoints; i++) {
                float snrCourant = (float) i;
                valeursSNR[i] = snrCourant;

                StringBuilder args = new StringBuilder();
                args.append("-mess ").append(nbBitsMessage);
                args.append(" -form ").append(formeCourante);
                args.append(" -nbEch ").append(nbEch);
                if (seed != null) {
                    args.append(" -seed ").append(seed);
                }
                args.append(" -snrpb ").append(snrCourant);

                Simulateur sim = new Simulateur(args.toString().split("\\s+"));
                sim.execute();
                float teb = sim.calculTauxErreurBinaire();

                valeursTEB[i] = teb;
                System.out.println(String.format("  %s - SNR=%.1f dB => TEB=%.6f", formeCourante, snrCourant, teb));
            }

            // Exporter vers CSV avec le nom du type de codage
            exportToCSV(formeCourante, "SNR_dB", valeursSNR, valeursTEB);
        }

        System.out.println("Fichiers de comparaison SNR générés\n");

        // Générer aussi l'analyse nbEch
        System.out.println("=== Analyse Comparaison TEB : NRZ vs NRZT vs RZ (nbEch) ===");

        for (String formeCourante : formes) {
            float[] valeursTEB = new float[nbPoints];
            float[] valeursNbEch = new float[nbPoints];

            System.out.println("Analyse nbEch pour " + formeCourante + "...");

            // Référence pour garder la variance de bruit constante
            int nbEchReference = 30;

            // Faire varier nbEch de 10 à 100 par pas de 10
            for (int i = 0; i < nbPoints; i++) {
                int nbEchCourant = 10 + i * 10;
                valeursNbEch[i] = nbEchCourant;

                // Ajuster le SNR pour garder la variance de bruit constante
                float snrAjuste = snrDb + 10.0f * (float)Math.log10((double)nbEchCourant / nbEchReference);

                StringBuilder args = new StringBuilder();
                args.append("-mess ").append(nbBitsMessage);
                args.append(" -form ").append(formeCourante);
                args.append(" -nbEch ").append(nbEchCourant);
                if (seed != null) {
                    args.append(" -seed ").append(seed);
                }
                args.append(" -snrpb ").append(snrAjuste);

                Simulateur sim = new Simulateur(args.toString().split("\\s+"));
                sim.execute();
                float teb = sim.calculTauxErreurBinaire();

                valeursTEB[i] = teb;
                System.out.println(String.format("  %s - nbEch=%d => TEB=%.6f", formeCourante, nbEchCourant, teb));
            }

            // Exporter vers CSV avec "_NbEch" pour différencier
            exportToCSV(formeCourante + "_NbEch", "NbEch", valeursNbEch, valeursTEB);
        }

        System.out.println("Fichiers de comparaison nbEch générés\n");
    }

    /**
     * Analyse de comparaison pour multi-trajets : TEB pour NRZ, NRZT et RZ
     * Génère les analyses NbTrajets, Alpha et Tau pour chaque type de codage
     * @param nbBits nombre de bits du message
     * @param nbEchantillons nombre d'échantillons par bit
     * @param snr rapport signal/bruit en dB
     * @param seedValue seed pour reproductibilité (peut être null)
     * @param trajets liste des trajets définis par l'utilisateur
     * @throws Exception si une erreur survient lors de l'exécution du simulateur
     */
    public static void analyserComparaisonMultiTrajets(int nbBits, int nbEchantillons, float snr, Integer seedValue, List<Trajet> trajets) throws Exception {
        // Configurer les paramètres
        nbBitsMessage = nbBits;
        nbEch = nbEchantillons;
        snrDb = snr;
        seed = seedValue;
        trajetsUtilisateur = trajets;

        if (trajets == null || trajets.isEmpty()) {
            System.out.println("Pas de trajets définis pour la comparaison multi-trajets\n");
            return;
        }

        String[] formes = {"NRZ", "NRZT", "RZ"};

        // ===== Analyse 1 : TEB = f(NbTrajets) =====
        System.out.println("=== Analyse Comparaison TEB : NRZ vs NRZT vs RZ (NbTrajets) ===");

        int nbTrajetsMax = trajets.size();
        int nbPoints = nbTrajetsMax + 1;

        for (String formeCourante : formes) {
            float[] valeursTEB = new float[nbPoints];
            float[] valeursNbTrajets = new float[nbPoints];

            System.out.println("Analyse NbTrajets pour " + formeCourante + "...");

            for (int nbTrajets = 0; nbTrajets < nbPoints; nbTrajets++) {
                valeursNbTrajets[nbTrajets] = nbTrajets;

                StringBuilder args = new StringBuilder();
                args.append("-mess ").append(nbBitsMessage);
                args.append(" -form ").append(formeCourante);
                args.append(" -nbEch ").append(nbEch);
                if (seed != null) {
                    args.append(" -seed ").append(seed);
                }
                args.append(" -snrpb ").append(snrDb);

                if (nbTrajets > 0) {
                    args.append(" -ti");
                    for (int i = 0; i < nbTrajets; i++) {
                        Trajet t = trajets.get(i);
                        args.append(" ").append(t.getTau()).append(" ").append(t.getAlpha());
                    }
                }

                Simulateur sim = new Simulateur(args.toString().split("\\s+"));
                sim.execute();
                float teb = sim.calculTauxErreurBinaire();

                valeursTEB[nbTrajets] = teb;
                System.out.println(String.format("  %s - NbTrajets=%d => TEB=%.6f", formeCourante, nbTrajets, teb));
            }

            exportToCSV(formeCourante + "_NbTrajets", "NbTrajets", valeursNbTrajets, valeursTEB);
        }

        // ===== Analyse 2 : TEB = f(Alpha) =====
        System.out.println("\n=== Analyse Comparaison TEB : NRZ vs NRZT vs RZ (Alpha) ===");

        nbPoints = 9;
        int tauPremier = trajets.get(0).getTau();

        for (String formeCourante : formes) {
            float[] valeursTEB = new float[nbPoints];
            float[] valeursAlpha = new float[nbPoints];

            System.out.println("Analyse Alpha pour " + formeCourante + "...");

            for (int i = 0; i < nbPoints; i++) {
                float alpha = 0.1f + i * 0.1f;
                valeursAlpha[i] = alpha;

                StringBuilder args = new StringBuilder();
                args.append("-mess ").append(nbBitsMessage);
                args.append(" -form ").append(formeCourante);
                args.append(" -nbEch ").append(nbEch);
                if (seed != null) {
                    args.append(" -seed ").append(seed);
                }
                args.append(" -snrpb ").append(snrDb);
                args.append(" -ti");
                args.append(" ").append(tauPremier).append(" ").append(alpha);

                for (int j = 1; j < trajets.size(); j++) {
                    Trajet t = trajets.get(j);
                    args.append(" ").append(t.getTau()).append(" ").append(t.getAlpha());
                }

                Simulateur sim = new Simulateur(args.toString().split("\\s+"));
                sim.execute();
                float teb = sim.calculTauxErreurBinaire();

                valeursTEB[i] = teb;
                System.out.println(String.format("  %s - Alpha=%.1f => TEB=%.6f", formeCourante, alpha, teb));
            }

            exportToCSV(formeCourante + "_Alpha", "Alpha", valeursAlpha, valeursTEB);
        }

        // ===== Analyse 3 : TEB = f(Tau) =====
        System.out.println("\n=== Analyse Comparaison TEB : NRZ vs NRZT vs RZ (Tau) ===");

        nbPoints = 20;
        float alphaPremier = trajets.get(0).getAlpha();

        for (String formeCourante : formes) {
            float[] valeursTEB = new float[nbPoints];
            float[] valeursTau = new float[nbPoints];

            System.out.println("Analyse Tau pour " + formeCourante + "...");

            for (int tau = 1; tau <= nbPoints; tau++) {
                valeursTau[tau - 1] = tau;

                StringBuilder args = new StringBuilder();
                args.append("-mess ").append(nbBitsMessage);
                args.append(" -form ").append(formeCourante);
                args.append(" -nbEch ").append(nbEch);
                if (seed != null) {
                    args.append(" -seed ").append(seed);
                }
                args.append(" -snrpb ").append(snrDb);
                args.append(" -ti");
                args.append(" ").append(tau).append(" ").append(alphaPremier);

                for (int j = 1; j < trajets.size(); j++) {
                    Trajet t = trajets.get(j);
                    args.append(" ").append(t.getTau()).append(" ").append(t.getAlpha());
                }

                Simulateur sim = new Simulateur(args.toString().split("\\s+"));
                sim.execute();
                float teb = sim.calculTauxErreurBinaire();

                valeursTEB[tau - 1] = teb;
                System.out.println(String.format("  %s - Tau=%d => TEB=%.6f", formeCourante, tau, teb));
            }

            exportToCSV(formeCourante + "_Tau", "Tau", valeursTau, valeursTEB);
        }

        System.out.println("Fichiers de comparaison multi-trajets générés\n");
    }

    /**
     * Méthode appelée par Simulateur pour générer les graphiques d'analyse TEB
     * @param nbBits nombre de bits du message
     * @param nbEchantillons nombre d'échantillons par bit
     * @param snr rapport signal/bruit en dB
     * @param forme forme du signal (RZ, NRZ, NRZT)
     * @param seedValue seed pour reproductibilité (peut être null)
     * @param trajets liste des trajets définis par l'utilisateur (peut être null)
     * @param avecCodeur true si le codeur est utilisé
     */
    public static void genererGraphiques(int nbBits, int nbEchantillons, float snr, String forme, Integer seedValue, List<Trajet> trajets, boolean avecCodeur) {
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
                // Analyser le codeur seulement si utilisé
                if (avecCodeur) {
                    Thread.sleep(500);
                    analyserCodeur();
                }
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
        genererGraphiques(1000, 30, 10.0f, "RZ", 42, trajetsTest, false);
    }
}