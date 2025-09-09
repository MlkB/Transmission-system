package transmetteurs;

import java.util.LinkedList;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;
import sources.Source;
import sources.SourceAleatoire;

@SuppressWarnings("unchecked")
public class transmetteurParfait<E> extends Transmetteur{
    
    public <E> transmetteurParfait (){
        super();
    }
    @Override
    public void recevoir(Information information) throws InformationNonConformeException {
       
        throw new UnsupportedOperationException("Unimplemented method 'recevoir'") ;
    }
    
    {
        this.informationRecue = this.informationEmise;
        String info = SourceAleatoire.generateRandom(8);
        info =  this.informationEmise.toString();
        System.out.println("The generated information is: " + info);

    }

    @Override
    public void emettre() throws InformationNonConformeException {
        
        throw new UnsupportedOperationException("Unimplemented method 'emettre'");
    }
     {
      try{
        for (Object obj : destinationsConnectees) {
            @SuppressWarnings("unchecked")
            DestinationInterface<E> destinationConnectee = (DestinationInterface<E>) obj;
            destinationConnectee.recevoir(this.informationRecue);
        }
        this.informationEmise = informationRecue;   
       } catch (InformationNonConformeException e) {
              e.printStackTrace();

     }
    
}

}
