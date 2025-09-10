package destinations;

import information.Information;
import information.InformationNonConformeException;

public class DestinationFinale extends Destination {

	@Override
	public void recevoir(Information information) throws InformationNonConformeException {
		this.informationRecue = information;
	}
	
	public Information getInformation() {
		return this.informationRecue;
	}
}
