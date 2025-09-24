package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;

public class TransmetteurImparfait extends Transmetteur {

    @Override
    public void connecter(DestinationInterface destination) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'connecter'");
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

    @Override
    public void emettre() throws InformationNonConformeException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'emettre'");
    }
    
}
