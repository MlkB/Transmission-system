package transmetteurs;

import java.util.Iterator;
import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;

public class TransmetteurParfait<E> extends Transmetteur<E,E> {

    public TransmetteurParfait() {
        super();
    }
    @Override
    public void recevoir(Information information) throws InformationNonConformeException {
        if (information == null) {
            throw new InformationNonConformeException("L'information est nulle");
        }
        this.informationRecue = information;

    }

    @Override
    public void emettre() throws InformationNonConformeException {
        this.informationEmise = this.informationRecue;
        Iterator<DestinationInterface<E>> it = destinationsConnectees.iterator();
        while (it.hasNext()) {
            DestinationInterface<E> destinationConnectee = it.next();
            destinationConnectee.recevoir(this.informationEmise);
        }
    }

}
