package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;


/**
 * Transmetteur imparfait modélisant un canal AWGN (Bruit Blanc Additif Gaussien).
 * Le bruit est calibré à partir d'un SNR par bit (dB) demandé.
 * <p>Étapes : estimation de la puissance moyenne des échantillons reçus,
 * estimation de l'énergie par bit {@code E_b ≈ P_signal / nEch}, calcul de la variance
 * {@code σ² = E_b / 10^(SNRdB/10)}, puis ajout d'un bruit gaussien centré à chaque échantillon.</p>
 * Une graine optionnelle permet de reproduire le bruit.
 */

public class TransmetteurImparfait<E> extends Transmetteur {

    public float puissanceSignal;
    public float variance;
    public float SNRdB;
    protected int nEch;
    private int seed;
    private Random rand;
    protected LinkedList<Float> bruit = new LinkedList<Float>();
    
    /**
     * Canal AWGN sans graine (bruit non reproductible).
     * @param nEch nombre d'échantillons par symbole
     * @param SNRdB SNR par bit (dB)
     */

// Constructeur sans graine (bruit totalement aléatoire)
    public TransmetteurImparfait(int nEch, float SNRdB) {
        super();
        this.SNRdB = SNRdB;
        this.nEch = nEch;
        this.rand = new Random(); // aléatoire total
    }
    
    /**
     * Canal AWGN avec graine (bruit reproductible).
     * @param nEch nombre d'échantillons par symbole
     * @param SNRdB SNR par bit (dB)
     * @param seed graine du générateur pseudo-aléatoire du bruit
     */

    // Constructeur avec graine
    public TransmetteurImparfait(int nEch, float SNRdB, int seed) {
        super();
        this.SNRdB = SNRdB;
        this.nEch = nEch;
        this.seed = seed;
        this.rand = new Random(seed); // reproductible
    }

    /**
     * Connecte une destination (récepteur/sonde) au canal.
     * @param destination destination à connecter
     */
    @Override
    public void connecter(DestinationInterface destination) {
        // TODO Auto-generated method stub
		destinationsConnectees.add(destination);
    }

    /**
     * Reçoit la trame analogique en provenance de l'émetteur.
     * @param information trame analogique (échantillons {@code Float})
     * @throws InformationNonConformeException si {@code information} est {@code null}
     */
    @Override
    public void recevoir(Information information) throws InformationNonConformeException {
        if (information == null) {
            throw new InformationNonConformeException("L'information est nulle");
        }
        this.informationRecue = information;
        emettre();
    }
    
    /**
     * Estime la puissance moyenne du signal reçu (moyenne des carrés des échantillons).
     * Met à jour {@code puissanceSignal}.
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

    private void calculerVariance() {
        calculPuissanceSignal();
        // estimate signal power per bit instead of per sample
        float bitsignal = puissanceSignal / nEch; 
        this.variance = bitsignal / (float) Math.pow(10, SNRdB / 10.0);
    }
    
    /**
     * Génère le bruit blanc gaussien additif (centré) et l'ajoute échantillon par échantillon
     * à la trame reçue. Met à jour {@code informationEmise} et la liste {@code bruit}.
     */

    private void genererBBAG() {
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
     * Émet la trame bruitée vers les destinations connectées.
     * @throws InformationNonConformeException propagation d'exception éventuelle des destinations
     */
    @Override
    public void emettre() throws InformationNonConformeException {
        Iterator<DestinationInterface<E>> it = destinationsConnectees.iterator();
        genererBBAG();
        while (it.hasNext()) {
            DestinationInterface<E> destinationConnectee = it.next();
            destinationConnectee.recevoir(this.informationEmise);
        }
    }
    
}
