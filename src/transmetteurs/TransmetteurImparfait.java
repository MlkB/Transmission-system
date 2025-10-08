package transmetteurs;

import destinations.DestinationInterface;
import emmetteurs.Emetteur;
import information.Information;
import information.InformationNonConformeException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;


/**
 * Transmetteur imparfait simulant un canal avec bruit gaussien.
 * <p>
 * Ce transmetteur prend des informations de type {@code Float} en entrée, ajoute un
 * bruit gaussien selon le SNR/bit demandé et émet les informations modifiées aux
 * destinations connectées.
 * </p>
 *
 * @param <E> le type d'information émise (ici {@code Float})
 */
public class TransmetteurImparfait<E> extends Transmetteur {

    /** Puissance du signal reçu. */
    public float puissanceSignal;

    /** Variance du bruit gaussien à ajouter. */
    public float variance;

    /** Rapport signal/bruit en dB. */
    public float SNRdB;

    /** Nombre d'échantillons par bit. */
    protected int nEch;

    /** Graine utilisée pour la génération aléatoire. */
    private int seed;

    /** Générateur de nombres aléatoires pour le bruit. */
    private Random rand;

    /** Liste des échantillons de bruit générés. */
    protected LinkedList<Float> bruit = new LinkedList<Float>();

    /**
     * Constructeur sans graine (bruit totalement aléatoire)
     * @param nEch le nombre d'échantillon à utiliser
     * @param SNRdB le signal sur bruit en décibel à utiliser
     */
    public TransmetteurImparfait(int nEch, float SNRdB) {
        super();
        this.SNRdB = SNRdB;
        this.nEch = nEch;
        this.rand = new Random(); // aléatoire total
    }

    /**
     * Constructeur avec graine
     * @param nEch le nombre d'échantillon à utiliser
     * @param SNRdB le signal sur bruit en décibel à utiliser
     * @param seed la graine à utiliser pour générer le bruit gaussien
     */
    public TransmetteurImparfait(int nEch, float SNRdB, int seed) {
        super();
        this.SNRdB = SNRdB;
        this.nEch = nEch;
        this.seed = seed;
        this.rand = new Random(seed); // reproductible
    }

    /**
     * permet de connecter le transmetteur à une destination
     * @param destination  la destination à connecter
     */
    @Override
    @SuppressWarnings("unchecked")
    public void connecter(DestinationInterface destination) {
		destinationsConnectees.add(destination);
    }

   /**
     * permet de recevoir l'information depuis une source ou un émetteur
     * @param information  l'information  à recevoir
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
     * permet de calculer la puissance du signa reçu
     */
    public void calculPuissanceSignal() {
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
     * Calcule la variance du bruit gaussien à ajouter pour atteindre le SNR/bit demandé.
     *
     * Pour SNR par bit avec moyennage au récepteur :
     * - La puissance du signal par échantillon est puissanceSignal
     * - Au récepteur, on moyenne nEch échantillons, ce qui améliore le SNR d'un facteur nEch
     * - Pour avoir SNR_b au niveau bit après moyennage :
     *   σ²_échantillon = puissanceSignal / (SNR_b / nEch) = (puissanceSignal × nEch) / SNR_b
     *
     * Met à jour {@code variance}.
     */

    public void calculerVariance() {
        calculPuissanceSignal();
        // Variance par échantillon pour obtenir SNR_b après moyennage de nEch échantillons
        float snrLineaire = (float) Math.pow(10, SNRdB / 10.0);
        this.variance = (puissanceSignal * nEch) / snrLineaire;
    }

    /**
     * Génère un échantillon de bruit gaussien centré réduit selon la formule de Box-Muller.
     * @return un échantillon suivant une loi normale N(0,1)
     */
    public double genererBruitGaussienFormule() {
        double u1 = Math.random(); // tirage uniforme [0,1]
        double u2 = Math.random();

        // Box-Muller : transforme les uniformes en loi gaussienne centrée réduite
        double x = Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);

        // Ici, x suit une loi normale centrée réduite : moyenne μ = 0, variance σ² = 1
        // Sa densité suit bien : f(x) = (1/sqrt(2π)) * exp(-x²/2)

        return x;
    }

    /**
     * génère le bruit blanc additif gaussien et
     * l'ajoute au signal reçu pour créer le signal émis
     */
    @SuppressWarnings("unchecked")
    public void genererBBAG() {
        this.informationEmise = new Information<Float>();
        calculerVariance();
        double sqrtVariance = Math.sqrt(variance);

        for (Object obj : informationRecue) {
            float echantillon = (Float) obj;
            double bruitEchantillon = genererBruitGaussienFormule() * sqrtVariance;
            this.bruit.add((float) bruitEchantillon);
            this.informationEmise.add(echantillon + (float) bruitEchantillon);
        }

    }

    /**
     * envoie le signal émis à toutes les destinations
     * @throws InformationNonConformeException si l'information n'est pas conforme
     */
    @Override
    @SuppressWarnings("unchecked")
    public void emettre() throws InformationNonConformeException {
        Iterator<DestinationInterface<E>> it = destinationsConnectees.iterator();
        genererBBAG();
        while (it.hasNext()) {
            DestinationInterface<E> destinationConnectee = it.next();
            destinationConnectee.recevoir(this.informationEmise);
        }
    }

    
    
}
