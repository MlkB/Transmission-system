package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;


public class TransmetteurImparfait<E> extends Transmetteur {

    public float puissanceSignal;
    public float variance;
    public float SNRdB;
    protected int nEch;
    protected LinkedList<Float> bruit = new LinkedList<Float>();

    public TransmetteurImparfait(int nEch, float SNRdB) {
        super();
        this.SNRdB = SNRdB;
        this.nEch = nEch;
    }

    @Override
    public void connecter(DestinationInterface destination) {
        // TODO Auto-generated method stub
		destinationsConnectees.add(destination);
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

    private void calculerVariance() {
        calculPuissanceSignal();
        this.variance = (this.puissanceSignal * nEch) / (2 * (float) Math.pow(10, SNRdB / 10));
    }

    private void genererBBAG() {
        this.informationEmise = new Information<Float>();
        calculerVariance();
        Random rand = new Random(); // create an instance
        for (int i = 0; i < informationRecue.nbElements(); i++) {
            float echantillon = (Float) informationRecue.iemeElement(i); // cast en Float
            double bruitEchantillon = rand.nextGaussian() * Math.sqrt(variance);
            this.bruit.add((float) (bruitEchantillon));
            this.informationEmise.add(echantillon + (float) (bruitEchantillon));
        }

    }

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
