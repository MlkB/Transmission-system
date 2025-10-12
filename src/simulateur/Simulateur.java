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
import visualisations.VueCourbe;
import emmetteurs.CodageEmission;
import transmetteurs.DecodageReception;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
 *   <li>-comparaison : affiche la comparaison des TEB pour NRZ, NRZT et RZ sur le même graphique</li>
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

    /** indique si le Simulateur doit générer les graphiques d'analyse TEB */
    private boolean modeAnalyse = false;

    /** indique si le Simulateur doit comparer RZ/NRZ/NRZT dans les analyses TEB */
    private boolean modeComparaison = false;

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

	/** le nombre d'échantilllons utilisés */
	private int nEch = 30;

    /** le rapport signal sur bruit SNR utilisé en décibel */
	private Float SNRpB;

    /** Paramètres du canal à trajets multiples */
    private List<Trajet> trajetsMultiples = null;

	/** booléen disant si on utilise un codeur ou non */
	private Boolean codeur = false;

	/** amplitude minimale du signal analogique */
	private float amplMin = 0.0f;

	/** amplitude maximale du signal analogique */
	private float amplMax = 1.0f;



    private Emetteur emetteur = null;
    private Recepteur recepteur = null;
   	
   
    /** Le constructeur de Simulateur construit une chaîne de
     * transmission composée d'une Source {@code <Boolean>}, d'une Destination
     * {@code <Boolean>}et de Transmetteur(s) [voir la méthode
     * analyseArguments]...  <br> Les différents composants de la
     * chaîne de transmission (Source, Transmetteur(s), Destination,
     * Sonde(s) de visualisation) sont créés et connectés.
     * @param args le tableau des différents arguments.
     *
     * @throws ArgumentsException si un des arguments est incorrect
     * @throws InformationNonConformeException si l'information générée ou transmise ne correspond pas
	 *         au type ou à la forme attendue
     *
     */
	@SuppressWarnings("unchecked")
    public  Simulateur(String [] args) throws ArgumentsException, InformationNonConformeException {
    	// analyser et récupérer les arguments   	
    	analyseArguments(args);
    	
    	// 1. Create the correct source based on the arguments
        if (messageAleatoire) {
            // Random message
            SourceAleatoire SA = new SourceAleatoire();
            if (aleatoireAvecGerme && seed != null) {
                SA.setSeed(seed);  // assuming you add a setSeed method
            }
            SA.setLength(nbBitsMess);  // assuming you add a setNbBits method
            SA.generer(); 
            source = SA;
        } else {
            // Fixed message
            SourceFixe SF = new SourceFixe();
            SF.generer(messageString);
            source = SF;
        }
    	
        // Création du transmetteur selon les options
        if (trajetsMultiples != null && !trajetsMultiples.isEmpty()) {
            // Canal à trajets multiples (avec ou sans bruit)
            // Si SNR non spécifié, utiliser SNR très élevé (1000 dB) = canal parfait
            float snrEffectif = (SNRpB != null) ? SNRpB : 1000.0f;

            if (aleatoireAvecGerme && seed != null) {
                transmetteurLogique = new TransmetteurMultiTrajet<>(trajetsMultiples, snrEffectif, nEch, seed);
            } else {
                transmetteurLogique = new TransmetteurMultiTrajet<>(trajetsMultiples, snrEffectif, nEch);
            }
        }
        else if (SNRpB == null) {
            // Canal parfait (pas de bruit)
            transmetteurLogique = new TransmetteurParfait();
        }
        else {
            // Canal simple avec bruit uniquement
            if (aleatoireAvecGerme && seed != null) {
                transmetteurLogique = new TransmetteurImparfait<>(nEch, SNRpB, seed);
            }
            else {
                transmetteurLogique = new TransmetteurImparfait<>(nEch, SNRpB);
            }
        }

		// Toujours créer les maillons de base
		emetteur = new Emetteur(form, nEch, amplMin, amplMax);

		// Seuil de décision dépend de l'amplitude et du codage:
		// - RZ: bit=1 donne amplMin-amplMax-amplMin (1/3 chacun), moyenne = amplMin + (amplMax-amplMin)/3
		//       seuil optimal = amplMin + (amplMax-amplMin)/6
		// - NRZ/NRZT: bit=0→amplMin, bit=1→amplMax, seuil = (amplMin+amplMax)/2
		float seuil = "RZ".equalsIgnoreCase(form)
		              ? (amplMin + (amplMax - amplMin) / 6.0f)
		              : ((amplMin + amplMax) / 2.0f);
		recepteur = new Recepteur(nEch, seuil, form);
		destination = new DestinationFinale();


		if (codeur) {
			CodageEmission codeur = new CodageEmission();
			DecodageReception decodeur = new DecodageReception();

			// Chaîne avec codage
			source.connecter(codeur);
			codeur.connecter(emetteur);
			emetteur.connecter(transmetteurLogique);
			transmetteurLogique.connecter(recepteur);
			recepteur.connecter(decodeur);
			decodeur.connecter(destination);

			if (affichage) {
				source.connecter(new SondeLogique("source", 100));
				emetteur.connecterSonde(new SondeAnalogique("émetteur"));
				transmetteurLogique.connecter(new SondeAnalogique("transmetteur"));
				decodeur.connecter(new SondeLogique("récepteur2", 100));
			}
		} else {
			// Chaîne sans codage (ancienne version)
			source.connecter(emetteur);
			emetteur.connecter(transmetteurLogique);
			transmetteurLogique.connecter(recepteur);
			recepteur.connecter(destination);

			if (affichage) {
				source.connecter(new SondeLogique("source", 100));
				emetteur.connecterSonde(new SondeAnalogique("émetteur"));
				transmetteurLogique.connecter(new SondeAnalogique("transmetteur"));
				recepteur.connecter(new SondeLogique("récepteur", 100));
			}
		}
    }
   
   
   
    /** La méthode analyseArguments extrait d'un tableau de chaînes de
     * caractères les différentes options de la simulation.  <br>Elle met
     * à jour les attributs correspondants du Simulateur.
     *
     * @param args le tableau des différents arguments.
     * <br>
     * <br>Les arguments autorisés sont : 
     * <br> 
     * <dl>
     * <dt> -mess m  </dt><dd> m (String) constitué de 7 ou plus digits à 0 | 1, le message à transmettre</dd>
     * <dt> -mess m  </dt><dd> m (int) constitué de 1 à 6 digits, le nombre de bits du message "aléatoire" à transmettre</dd> 
     * <dt> -s </dt><dd> pour demander l'utilisation des sondes d'affichage</dd>
     * <dt> -seed v </dt><dd> v (int) d'initialisation pour les générateurs aléatoires</dd> 
     * </dl>
     *
     * @throws ArgumentsException si un des arguments est incorrect.
     *
     */   
    private  void analyseArguments(String[] args)  throws  ArgumentsException {

    	for (int i=0;i<args.length;i++){ // traiter les arguments 1 par 1

    		if (args[i].matches("-s")){
    			affichage = true;
    		}

    		else if (args[i].matches("-seed")) {
    			aleatoireAvecGerme = true;
    			i++; 
    			try { 
    				seed = Integer.valueOf(args[i]);
    			}
    			catch (Exception e) {
    				throw new ArgumentsException("Valeur du parametre -seed  invalide :" + args[i]);
    			}           		
    		}

    		else if (args[i].matches("-mess")){
    			i++; 
    			// traiter la valeur associee
    			messageString = args[i];
    			if (args[i].matches("[0,1]{7,}")) { // au moins 7 digits
    				messageAleatoire = false;
    				nbBitsMess = args[i].length();
    			} 
    			else if (args[i].matches("[0-9]{1,6}")) { // de 1 à 6 chiffres
    				messageAleatoire = true;
    				nbBitsMess = Integer.valueOf(args[i]);
    				if (nbBitsMess < 1) 
    					throw new ArgumentsException ("Valeur du parametre -mess invalide : " + nbBitsMess);
    			}
    			else 
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

			else if (args[i].matches("-nbEch")) {
				i++;
				try {
					nEch = Integer.valueOf(args[i]);
				}
				catch (Exception e) {
					throw new ArgumentsException("Valeur du parametre -nbEch invalide :" + args[i]);
				}
			}

            else if (args[i].matches("-snrpb")) {
				i++;
				try {
					SNRpB = Float.valueOf(args[i]);
				}
				catch (Exception e) {
					throw new ArgumentsException("Valeur du parametre -snrpb invalide :" + args[i]);
				}
			}

			else if (args[i].matches("-ti")) {
				// Trajets multiples : lire les couples (dt, ar) jusqu'à 5 max
				trajetsMultiples = new ArrayList<>();
				i++;
				while (i < args.length && !args[i].startsWith("-") && trajetsMultiples.size() < 5) {
					try {
						int dt = Integer.valueOf(args[i]);
						i++;
						if (i >= args.length || args[i].startsWith("-")) {
							throw new ArgumentsException("Valeur ar manquante après dt pour -ti");
						}
						float ar = Float.valueOf(args[i]);
						trajetsMultiples.add(new Trajet(dt, ar));
						i++;
					} catch (NumberFormatException e) {
						throw new ArgumentsException("Valeurs invalides pour -ti : " + args[i-1]);
					}
				}
				i--; // Compenser le i++ du for
				if (trajetsMultiples.isEmpty()) {
					throw new ArgumentsException("Au moins un couple (dt, ar) requis pour -ti");
				}
			}

			else if (args[i].matches("-codeur")) {
				codeur = true;
			}

			else if (args[i].matches("-comparaison")) {
				modeComparaison = true;
			}

			else if (args[i].matches("-ampl")) {
				i++;
				try {
					amplMin = Float.valueOf(args[i]);
					i++;
					if (i >= args.length) {
						throw new ArgumentsException("Valeur max manquante pour -ampl");
					}
					amplMax = Float.valueOf(args[i]);
					if (amplMin >= amplMax) {
						throw new ArgumentsException("Pour -ampl : min doit être inférieur à max");
					}
				}
				catch (NumberFormatException e) {
					throw new ArgumentsException("Valeurs invalides pour -ampl : " + args[i]);
				}
			}


    		else throw new ArgumentsException("Option invalide :"+ args[i]);
    	}
      
    }
     
    
   	
    /** La méthode execute effectue un envoi de message par la source
     * de la chaîne de transmission du Simulateur.
     *
     * @throws Exception si un problème survient lors de l'exécution
     *
     */ 
    public void execute() throws Exception {

    	source.emettre();
		//System.err.println("DEBUG: Source a émis " + source.getInformationEmise().nbElements() + " bits");
		//System.err.println("DEBUG: Premiers bits source: " + source.getInformationEmise().iemeElement(0) + " " + source.getInformationEmise().iemeElement(1) + " " + source.getInformationEmise().iemeElement(2));
		//System.err.println("DEBUG: Emetteur a généré " + emetteur.getInformationEmise().nbElements() + " échantillons");

        // emetteur émet automatiquement vers transmetteur dans recevoir()
        transmetteurLogique.emettre();
		//System.err.println("DEBUG: Transmetteur a émis " + transmetteurLogique.getInformationEmise().nbElements() + " échantillons");

        // transmetteur émet vers recepteur
		//System.err.println("DEBUG: Signal transmetteur premiers échantillons: " + transmetteurLogique.getInformationAnalogEmise().iemeElement(0) + " " + transmetteurLogique.getInformationAnalogEmise().iemeElement(1) + " " + transmetteurLogique.getInformationAnalogEmise().iemeElement(2));
		//System.err.println("DEBUG: Signal emetteur premiers échantillons: " + emetteur.getInformationEmise().iemeElement(0) + " " + emetteur.getInformationEmise().iemeElement(1) + " " + emetteur.getInformationEmise().iemeElement(2));
        recepteur.recevoir(transmetteurLogique.getInformationAnalogEmise());
        //recepteur.emettre();
		//System.err.println("DEBUG: Recepteur a émis " + recepteur.getInformationEmise().nbElements() + " bits");
		//System.err.println("DEBUG: Premiers bits recepteur: " + recepteur.getInformationEmise().iemeElement(0) + " " + recepteur.getInformationEmise().iemeElement(1) + " " + recepteur.getInformationEmise().iemeElement(2));
        // recepteur émet vers destination

        //System.out.println(destination.getInformationRecue());

    }
   
   	   	
   	
    /**
     * Retourne si le mode affichage est activé
     * @return true si l'affichage est activé
     */
    public boolean isAffichageActive() {
        return affichage;
    }

    /** La méthode qui calcule le taux d'erreur binaire en comparant
     * les bits du message émis avec ceux du message reçu.
     *
     * @return  La valeur du Taux dErreur Binaire.
     */
    public float  calculTauxErreurBinaire() {
    	Information<Boolean> infoEmise = source.getInformationEmise();
    	Information<Boolean> infoRecue = destination.getInformationRecue();

		int size = Math.min(infoEmise.nbElements(), infoRecue.nbElements());
		int error = 0;

		// Debug
		//System.err.println("DEBUG: Taille émise = " + infoEmise.nbElements());
		//System.err.println("DEBUG: Taille reçue = " + infoRecue.nbElements());
		//System.err.println("DEBUG: Comparaison sur " + size + " bits");

		// Use iterators for O(n) performance instead of iemeElement which is O(n²) with LinkedList
		Iterator<Boolean> iterEmis = infoEmise.iterator();
		Iterator<Boolean> iterRecu = infoRecue.iterator();
		for (int i = 0; i < size; i++) {
			Boolean emis = iterEmis.next();
			Boolean recu = iterRecu.next();
			if (!emis.equals(recu)) {
				error++;
				if (error <= 5) { // Afficher les 5 premières erreurs
					//System.err.println("DEBUG: Erreur bit " + i + " : émis=" + emis + " reçu=" + recu);
				}
			}
		}
		//System.err.println("DEBUG: Nombre d'erreurs = " + error);
		return (float) error / size;
	}


	/**
	 * Supprime tous les anciens fichiers CSV (sondes et TEB) pour éviter d'afficher des données obsolètes
	 */
	private static void nettoyerTousLesCSV() {
		try {
			java.io.File currentDir = new java.io.File(".");
			java.io.File[] csvFiles = currentDir.listFiles((dir, name) ->
				(name.startsWith("sonde_") || name.startsWith("TEB_")) && name.endsWith(".csv")
			);

			if (csvFiles != null) {
				for (java.io.File file : csvFiles) {
					file.delete();
				}
			}
		} catch (Exception e) {
			System.err.println("Erreur lors du nettoyage des CSV: " + e.getMessage());
		}
	}

	/**
	 * Appelle le script Python pour générer les graphiques avec graduations et légendes
	 * Cette méthode exécute plot_curves.py pour afficher les courbes à partir des CSV
	 * @param comparaison si true, affiche la comparaison NRZ/NRZT/RZ
	 */
	private static void appelScriptPython(boolean comparaison) {
		try {
			System.out.println("\n=== Génération des graphiques Python ===");

			// Déterminer la commande Python (python3 ou python)
			String pythonCmd = "python3";
			try {
				Process testPython = Runtime.getRuntime().exec("python3 --version");
				testPython.waitFor();
				if (testPython.exitValue() != 0) {
					pythonCmd = "python";
				}
			} catch (Exception e) {
				pythonCmd = "python";
			}

			// Exécuter le script Python avec ou sans -comparaison
			ProcessBuilder pb;
			if (comparaison) {
				pb = new ProcessBuilder(pythonCmd, "plot_curves.py", "-comparaison");
			} else {
				pb = new ProcessBuilder(pythonCmd, "plot_curves.py");
			}
			pb.inheritIO(); // Pour afficher la sortie du script Python
			Process process = pb.start();

			// Attendre que le script se termine
			int exitCode = process.waitFor();

			if (exitCode == 0) {
				System.out.println("Graphiques Python générés avec succès !");
			} else {
				System.err.println("Erreur lors de l'exécution du script Python (code: " + exitCode + ")");
			}

		} catch (Exception e) {
			System.err.println("Impossible d'exécuter le script Python: " + e.getMessage());
			System.err.println("Vous pouvez exécuter manuellement: python3 plot_curves.py");
		}
	}


    /** La fonction main instancie un Simulateur à l'aide des
     *  arguments paramètres et affiche le résultat de l'exécution
     *  d'une transmission.
     *  @param args les différents arguments qui serviront à l'instanciation du Simulateur.
     */
    public static void main(String [] args) { 

    	Simulateur simulateur = null;

    	try {
    		simulateur = new Simulateur(args);
    	}
    	catch (Exception e) {
    		System.out.println(e);
    		System.exit(-1);
    	}

    	// Nettoyer les anciens fichiers CSV si affichage activé
    	if (simulateur.isAffichageActive()) {
    		nettoyerTousLesCSV();
    	}

    	try {
    		simulateur.execute();
    		String s = "java  Simulateur  ";
    		for (int i = 0; i < args.length; i++) { //copier tous les paramètres de simulation
    			s += args[i] + "  ";
    		}
    		System.out.println(s + "  =>   TEB : " + simulateur.calculTauxErreurBinaire());

    		// Générer les graphiques d'analyse TEB si affichage activé ET si bruit ou multi-trajets présent
    		if (simulateur.isAffichageActive()) {
    			// Vérifier si on a du bruit (-snrpb) ou des multi-trajets (-ti)
    			boolean avecBruitOuMultiTrajets = (simulateur.SNRpB != null) ||
    			                                   (simulateur.trajetsMultiples != null && !simulateur.trajetsMultiples.isEmpty());

    			if (avecBruitOuMultiTrajets) {
    				// Utiliser SNR = 10dB par défaut si pas de bruit dans la simulation
    				float snr = (simulateur.SNRpB != null) ? simulateur.SNRpB : 10.0f;

    				if (simulateur.modeComparaison) {
    					// Mode comparaison : générer les TEB pour NRZ, NRZT et RZ
    					try {
    						if (simulateur.trajetsMultiples != null && !simulateur.trajetsMultiples.isEmpty()) {
    							// Mode comparaison avec multi-trajets
    							AnalyseTEB.analyserComparaisonMultiTrajets(simulateur.nbBitsMess, simulateur.nEch, snr, simulateur.seed, simulateur.trajetsMultiples);
    						} else {
    							// Mode comparaison simple (SNR et nbEch)
    							AnalyseTEB.analyserComparaison(simulateur.nbBitsMess, simulateur.nEch, snr, simulateur.seed);
    						}
    					} catch (Exception e) {
    						System.err.println("Erreur lors de l'analyse de comparaison: " + e.getMessage());
    						e.printStackTrace();
    					}
    				} else {
    					// Mode normal
    					AnalyseTEB.genererGraphiques(simulateur.nbBitsMess, simulateur.nEch,
    					                             snr, simulateur.form, simulateur.seed, simulateur.trajetsMultiples, simulateur.codeur);
    				}

    			} else {
    				// Pas de bruit ni de multi-trajets : ne rien faire
    				System.out.println("\nAucune analyse TEB à générer (pas de -snrpb ni de -ti)");
    			}

    			// Appeler le script Python pour afficher les sondes (et les TEB si disponibles)
    			appelScriptPython(simulateur.modeComparaison);
    		}
    	}
    	catch (Exception e) {
    		System.out.println(e);
    		e.printStackTrace();
    		System.exit(-2);
    	}
    }
}

