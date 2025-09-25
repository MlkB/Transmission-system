package transmetteurs;

import destinations.DestinationInterface;
import emmetteurs.Emetteur;
import information.Information;
import information.InformationNonConformeException;
import sources.SourceInterface;
import visualisations.SondeAnalogique;
import visualisations.SondeLogique;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;



public class TransmetteurImparfait extends Transmetteur<Float,Float> {

    public float puissanceSignal;
    public float variance;
    public float SNRdB;
    protected int nEch;
    private int seed;
    private Random rand;
    protected LinkedList<Float> bruit = new LinkedList<Float>();

// Constructeur sans graine (bruit totalement aléatoire)
    public TransmetteurImparfait(int nEch, float SNRdB) {
        super();
        this.SNRdB = SNRdB;
        this.nEch = nEch;
        this.rand = new Random(); // aléatoire total
    }

    // Constructeur avec graine
    public TransmetteurImparfait(int nEch, float SNRdB, int seed) {
        super();
        this.SNRdB = SNRdB;
        this.nEch = nEch;
        this.seed = seed;
        this.rand = new Random(seed); // reproductible
    }



   /**
     * permet de recevoir l'information depuis une source ou un émetteur
     * @param information  l'information  à recevoir
     * @throws InformationNonConformeException
     */
    @Override
    public void recevoir(Information information) throws InformationNonConformeException {
        if (information == null) {
            throw new InformationNonConformeException("L'information est nulle");
        }
        this.informationRecue = information;
    }
    
    public void calculPuissanceSignal() {
        float somme = 0;

        for (int i = 0; i < informationRecue.nbElements(); i++) {
            float echantillon = (Float) informationRecue.iemeElement(i); // cast en Float
            somme += echantillon * echantillon;
        }
        this.puissanceSignal = somme/informationRecue.nbElements();

    }

    public void calculerVariance() {
        calculPuissanceSignal();
        // estimate signal power per bit instead of per sample
        float bitsignal = puissanceSignal / nEch; 
        this.variance = bitsignal / (float) Math.pow(10, SNRdB / 10.0);
    }

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

    @Override
    public void emettre() throws InformationNonConformeException {
        Iterator<DestinationInterface<Float>> it = destinationsConnectees.iterator();
        genererBBAG();
        while (it.hasNext()) {
            DestinationInterface<Float> destinationConnectee = it.next();
            destinationConnectee.recevoir(this.informationEmise);
        }
    }

    
        
    
}
