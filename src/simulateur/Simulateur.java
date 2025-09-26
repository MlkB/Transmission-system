package simulateur;

import destinations.Destination;
import sources.Source;
import sources.SourceAleatoire;
import sources.SourceFixe;
import transmetteurs.*;
import destinations.DestinationFinale;
import information.Information;
import information.InformationNonConformeException;
import visualisations.SondeLogique;
import visualisations.SondeAnalogique;
import emmetteurs.Emetteur;

/**
 * La classe Simulateur permet de construire et simuler une chaîne de
 * transmission composée d'une Source, d'un nombre variable de
 * Transmetteur(s) et d'une Destination.
 * Simulateur d'une chaîne de transmission numérique :
 * Source (fixe/aléatoire) → Émetteur (NRZ/RZ/NRZT) → Canal (parfait ou AWGN) → Récepteur → Destination.
 *
 * <p>Options de ligne de commande :
 * <ul>
 *   <li>-s : active les sondes d'affichage (logique/analogique)</li>
 *   <li>-mess m : si m est binaire (ex. "10101"), message fixe ; si m est un entier N, génère N bits aléatoires</li>
 *   <li>-seed v : graine pour la génération du message (reproductibilité)</li>
 *   <li>-form F : forme d'onde de l'émetteur (NRZ | RZ | NRZT). Défaut : RZ</li>
 *   <li>-ne k : nombre d'échantillons par symbole (nbEch). Défaut : 30</li>
 *   <li>-snrpb d : SNR par bit en dB ; si absent → transmetteur parfait (pas de bruit)</li>
 *   <li>-seedBruit v : graine du bruit AWGN (reproductibilité du canal)</li>
 * </ul>
 * Exemples :
 * <pre>
 *   java simulateur.Simulateur -mess 101001 -form NRZ -ne 20
 *   java simulateur.Simulateur -mess 1000 -seed 42 -snrpb 8 -seedBruit 7 -form NRZT -ne 30
 * </pre>
 
 */
public class Simulateur {

    /** indique si le Simulateur utilise des sondes d'affichage */
    private boolean affichage = false;

    /** indique si le Simulateur utilise un message généré de manière aléatoire (message imposé sinon) */
    private boolean messageAleatoire = true;

    /** indique si le Simulateur utilise un germe pour initialiser les générateurs aléatoires */
    private boolean aleatoireAvecGerme = false;

    /** la valeur de la semence utilisée pour les générateurs aléatoires */
    private Integer seed = null; // pas de semence par défaut

    /** la longueur du message aléatoire à transmettre si un message n'est pas imposé */
    private int nbBitsMess = 100;

    /** la chaîne de caractères correspondant à m dans l'argument -mess m */
    private String messageString = "100";

    /** le composant Source de la chaîne de transmission */
    private Source<Boolean> source = null;

    /** le composant Transmetteur logique de la chaîne de transmission */
    private Transmetteur<Float, Float> transmetteurLogique = null;

    /** le composant Destination de la chaîne de transmission */
    private Destination<Boolean> destination = null;

    /** la conversion numérique à analogique utilisée */
    private String form = "RZ";

    /** le nombre d'échantillons utilisés */
    private int nEch = 30;

    /** le rapport signal sur bruit SNR utilisé en décibel */
    private Float SNRpB;

    /** signal bruité par graine */
    private Boolean bruitSeeded = false;

    /** graine du bruit */
    private int bruitSeed;

    private Emetteur emetteur = null;
    private Recepteur recepteur = null;

    /**
     * Constructeur de Simulateur : construit la chaîne complète
     */
    public Simulateur(String[] args) throws ArgumentsException, InformationNonConformeException {
        // analyser et récupérer les arguments
        analyseArguments(args);

        // 1. Créer la source
        if (messageAleatoire) {
            SourceAleatoire SA = new SourceAleatoire();
            if (aleatoireAvecGerme && seed != null) {
                SA.setSeed(seed);
            }
            SA.setLength(nbBitsMess);
            SA.generer();
            source = SA;
        } else {
            SourceFixe SF = new SourceFixe();
            SF.generer(messageString);
            source = SF;
        }

        // 2. Choisir le transmetteur (parfait ou bruité)
        if (SNRpB == null) {
            transmetteurLogique = new TransmetteurParfait<>();
        } else {
            if (bruitSeeded) {
                transmetteurLogique = new TransmetteurImparfait(nEch, SNRpB, bruitSeed);
            } else {
                transmetteurLogique = new TransmetteurImparfait(nEch, SNRpB);
            }
        }

        // 3. Créer émetteur / récepteur / destination
        emetteur = new Emetteur(form, nEch);
        recepteur = new Recepteur(nEch, 0f, form);
        destination = new DestinationFinale<Boolean>();

        // 4. Chaîne principale
        source.connecter(emetteur);
        emetteur.connecter(transmetteurLogique);
        transmetteurLogique.connecter(recepteur);
        recepteur.connecter(destination);

        // 5. Connexion des sondes en parallèle
        if (affichage) {
            source.connecter(new SondeLogique("Source", 100));
            emetteur.connecter(new SondeAnalogique("Émetteur"));
            transmetteurLogique.connecter(new SondeAnalogique("Canal"));
            recepteur.connecter(new SondeLogique("Récepteur", 100));
        }
    }

    /**
     * Analyse des arguments de la simulation et initialise les attributs.
     * @param args les arguments de ligne de commande
     * @throws ArgumentsException en cas d'option inconnue ou de valeur invalide
     */
    private void analyseArguments(String[] args) throws ArgumentsException {
        for (int i = 0; i < args.length; i++) {
            if (args[i].matches("-s")) {
                affichage = true;
            }

            else if (args[i].matches("-seed")) {
                aleatoireAvecGerme = true;
                i++;
                try {
                    seed = Integer.valueOf(args[i]);
                } catch (Exception e) {
                    throw new ArgumentsException("Valeur du parametre -seed invalide :" + args[i]);
                }
            }

            else if (args[i].matches("-mess")) {
                i++;
                messageString = args[i];
                if (args[i].matches("[0,1]{7,}")) {
                    messageAleatoire = false;
                    nbBitsMess = args[i].length();
                } else if (args[i].matches("[0-9]{1,6}")) {
                    messageAleatoire = true;
                    nbBitsMess = Integer.valueOf(args[i]);
                    if (nbBitsMess < 1)
                        throw new ArgumentsException("Valeur du parametre -mess invalide : " + nbBitsMess);
                } else
                    throw new ArgumentsException("Valeur du parametre -mess invalide : " + args[i]);
            }

            else if (args[i].matches("-form")) {
                i++;
                if (i < args.length) {
                    form = args[i];
                } else {
                    throw new ArgumentsException("Valeur du parametre -form manquante");
                }
            }

            else if (args[i].matches("-ne")) {
                i++;
                try {
                    nEch = Integer.valueOf(args[i]);
                } catch (Exception e) {
                    throw new ArgumentsException("Valeur du parametre -ne invalide :" + args[i]);
                }
            }

            else if (args[i].matches("-snrpb")) {
                i++;
                try {
                    SNRpB = Float.valueOf(args[i]);
                } catch (Exception e) {
                    throw new ArgumentsException("Valeur du parametre -snrpb invalide :" + args[i]);
                }
            }

            else if (args[i].matches("-seedBruit")) {
                bruitSeeded = true;
                i++;
                try {
                    bruitSeed = Integer.valueOf(args[i]);
                } catch (Exception e) {
                    throw new ArgumentsException("Valeur du parametre -seedBruit invalide :" + args[i]);
                }
            }

            else
                throw new ArgumentsException("Option invalide :" + args[i]);
        }
    }

    /**
     * Exécute la simulation : déclenche l'émission depuis la source.
     * @throws Exception si un composant de la chaîne lève une exception d'exécution
     */
    public void execute() throws Exception {
        source.emettre(); // toute la chaîne est déclenchée automatiquement
        System.out.println("Message reçu : " + destination.getInformationRecue());
    }

    /**
     * Calcule le taux d'erreur binaire (TEB) en comparant l'information émise et reçue.
     * <p>Formule : TEB = (#bits erronés) / (#bits comparés). La comparaison se fait
     * sur la plus petite des deux longueurs par sécurité.</p>
     * @return le TEB dans [0,1]
     */
    public float calculTauxErreurBinaire() {
        Information<Boolean> infoEmise = source.getInformationEmise();
        Information<Boolean> infoRecue = destination.getInformationRecue();

        int size = Math.min(infoEmise.nbElements(), infoRecue.nbElements());
        int error = 0;
        for (int i = 0; i < size; i++) {
            if (!infoEmise.iemeElement(i).equals(infoRecue.iemeElement(i))) {
                error++;
            }
        }
        return (float) error / size;
    }

    /**
     * Point d'entrée du simulateur.
     * Construit la chaîne, exécute la simulation et affiche le TEB.
     * @param args arguments de ligne de commande (voir Javadoc de la classe)
     */
    public static void main(String[] args) {
        Simulateur simulateur = null;

        try {
            simulateur = new Simulateur(args);
        } catch (Exception e) {
            System.out.println(e);
            System.exit(-1);
        }

        try {
            simulateur.execute();
            String s = "java Simulateur ";
            for (String arg : args) {
                s += arg + " ";
            }
            System.out.println(s + " => TEB : " + simulateur.calculTauxErreurBinaire());
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
            System.exit(-2);
        }
    }
}