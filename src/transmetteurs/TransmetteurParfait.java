package transmetteurs;

import java.util.Iterator;
import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;
import emmetteurs.Emetteur;

/**
 * Transmetteur parfait : recopie l'information reçue telle quelle
 * vers toutes les destinations connectées (aucune altération).
 * @param <E> type des éléments transportés
 */

public class TransmetteurParfait<E> extends Transmetteur<E,E> {

    public TransmetteurParfait() {
        super();
    }

    /**
     * Reçoit l'information depuis la source/émetteur en amont.
     * @param information information à recevoir
     * @throws InformationNonConformeException si {@code information} est {@code null}
     */
    @Override
    public void recevoir(Information<E> information) throws InformationNonConformeException {
        if (information == null) {
            throw new InformationNonConformeException("L'information est nulle");
        }
        this.informationRecue = information;
        emettre();
    }

    @Override
    public void connecter(Emetteur emetteur) {

    }

    /**
     * Émet l'information reçue vers toutes les destinations connectées, sans modification.
     * @throws InformationNonConformeException propagation d'exception éventuelle des destinations
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
     * Connecte une destination au transmetteur parfait.
     * @param destination destination à connecter
     */
    @Override
	public void connecter(DestinationInterface<E> destination) {
		destinationsConnectees.add(destination);
	}


}
