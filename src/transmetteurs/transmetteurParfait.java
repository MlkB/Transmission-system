package transmetteurs;

import java.util.LinkedList;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;
import sources.Source;
import sources.SourceAleatoire;

@SuppressWarnings("unchecked")
public class transmetteurParfait<T> extends Transmetteur <T,T> {
    
    public <E> transmetteurParfait (){
        super();
    }
@Override
    public void recevoir(Information information) throws InformationNonConformeException {
        this.informationRecue = information;
        this.informationEmise = this.informationRecue;
        try {
            emettre();
        } catch (InformationNonConformeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void emettre() throws InformationNonConformeException {
        for (DestinationInterface<T> dest : destinationsConnectees) {
            try {
                dest.recevoir(informationEmise);
            } catch (InformationNonConformeException e) {
                e.printStackTrace();
            }
        }
    }
        public static void main(String[] args) {
            String info = SourceAleatoire.generateRandom(8);
            System.out.println("The generated information is: " + info);
        }

}
