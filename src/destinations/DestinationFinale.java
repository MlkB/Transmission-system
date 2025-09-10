package destinations;

import information.Information;
import information.InformationNonConformeException;

public class DestinationFinale extends Destination {

	@Override
	public void recevoir(Information information) throws InformationNonConformeException {
		// TODO Auto-generated method stub
		this.informationRecue = information;
	}
	
}
