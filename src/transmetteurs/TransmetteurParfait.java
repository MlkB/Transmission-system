package transmetteurs;

import java.util.Iterator;
import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;
import visualisations.SondeAnalogique;

/**
 * classe représentant un transmetteur parfait dans la chaîne de transmission
 * le transmetteur n'a aucune perte
 * @param <E>
 */
public class TransmetteurParfait<E> extends Transmetteur<E,E> {

    public TransmetteurParfait() {
        super();
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
		destinationsConnectees.add(destination);
	}
    @Override
      public void connecter(SondeAnalogique sondeTransmetteur) {
        
              for (DestinationInterface<E> destinationConnectee : destinationsConnectees) {
                if (destinationConnectee == sondeTransmetteur) {
                     return; // Déjà connecté
      }
                else { destinationsConnectees.add((DestinationInterface<E>) sondeTransmetteur);   }       
         
           }
      }

}
      
   
   
