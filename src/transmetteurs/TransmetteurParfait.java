package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;

public class TransmetteurParfait extends Transmetteur {

	public TransmetteurParfait() {
		super();
	}
	
	@Override
	public void recevoir(Information information) throws InformationNonConformeException {
		this.informationRecue = information;
		
	}

	@Override
	public void emettre() throws InformationNonConformeException {
		for (DestinationInterface<E> destinationConnectee : destinationsConnectees) {
            destinationConnectee.recevoir(informationRecue);
		}
		this.informationEmise = informationRecue;	
	}

}
