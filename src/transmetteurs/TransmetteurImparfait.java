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

        for (int i = 0; i < informationRecue.nbElements(); i++) {
            float echantillon = (Float) informationRecue.iemeElement(i); // cast en Float
            somme += echantillon * echantillon;
        }
        this.puissanceSignal = somme/informationRecue.nbElements();

    }

    /**
     * Calcule la variance du bruit gaussien à ajouter pour atteindre le SNR/bit demandé :
     * {@code E_b ≈ puissanceSignal / nEch}, puis {@code σ² = E_b / 10^(SNRdB/10)}.
     * Met à jour {@code variance}.
     */

    public void calculerVariance() {
        calculPuissanceSignal();
        // estimate signal power per bit instead of per sample
        float bitsignal = puissanceSignal / nEch; 
        this.variance = bitsignal / (float) Math.pow(10, SNRdB / 10.0);
    }

    /**
     * génère le bruit blanc additif gaussien et
     * l'ajoute au signal reçu pour créer le signal émis
     */
    @SuppressWarnings("unchecked")
    public void genererBBAG() {
        this.informationEmise = new Information<Float>();
        calculerVariance();
        for (int i = 0; i < informationRecue.nbElements(); i++) {
            float echantillon = (Float) informationRecue.iemeElement(i); // cast en Float
            double bruitEchantillon = rand.nextGaussian() * Math.sqrt(variance);
            this.bruit.add((float) (bruitEchantillon));
            this.informationEmise.add(echantillon + (float) (bruitEchantillon));
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
