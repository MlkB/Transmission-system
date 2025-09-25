package transmetteurs;

import java.util.Iterator;
import destinations.DestinationInterface;
import emmetteurs.Emetteur;
import information.Information;
import information.InformationNonConformeException;
import sources.SourceInterface;
import visualisations.Sonde;
import visualisations.SondeAnalogique;
import visualisations.SondeLogique;

/**
 * classe représentant un transmetteur parfait dans la chaîne de transmission
 * le transmetteur n'a aucune perte
 * @param <E>
 */
public class TransmetteurParfait<E> extends Transmetteur<E,E> {

  
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
        this.informationEmise = information;
        try {
            emettre();
        } catch (InformationNonConformeException e) {
           e.printStackTrace();
    }

    }

    /**
     * permet d'envoyer l'information reçue à une destination finale
     * ou à un récepteur
     * @throws InformationNonConformeException
     */
    @Override
    public void emettre() throws InformationNonConformeException {
        this.informationEmise = this.informationRecue;
        Iterator<DestinationInterface<E>> it = destinationsConnectees.iterator();
        while (it.hasNext()) {
            DestinationInterface<E> destinationConnectee = it.next();
            destinationConnectee.recevoir(this.informationEmise);
        }
    }
    /**
     * permet de se connecter à une destination ou à un récepteur
     * @param destination  la destination à connecter
     */

     public void connecter(DestinationInterface<E> destination) {
		destinationsConnectees.add((DestinationInterface<E>) destination);
	}
        @Override
        public SourceInterface connecter(SondeLogique sondeLogique) {
            this.destinationsConnectees.add((DestinationInterface<E>) sondeLogique);
            return null;
        }
        @Override
        public SourceInterface<Integer> connecter(SondeAnalogique sondeAnalogique) {
            this.destinationsConnectees.add((DestinationInterface<E>) sondeAnalogique);
            return null;
        }
        @Override
        public void connecter(Emetteur emetteur) {
            emetteur.connecter((Transmetteur<Float, Boolean>) this);
        }

   

}
      
   
   
