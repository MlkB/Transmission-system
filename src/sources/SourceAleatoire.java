package sources;

import java.util.Random;
import information.Information;
import information.InformationNonConformeException;

public class SourceAleatoire extends Source {

		public SourceAleatoire() {
			super();
		}
		
		public void generer() throws InformationNonConformeException{
			int length = 100;
			Random rand = new Random();
		    Information<Boolean> informationBinaire = new Information<Boolean>();
		    for (int j = 0; j<length; j++) {
		    	informationBinaire.add(rand.nextBoolean());
		    }
		    for (int j = 0; j<length; j++) {
		    	if (informationBinaire.iemeElement(j) != true && informationBinaire.iemeElement(j) != false) throw new InformationNonConformeException();
		    }
		    
		    this.informationGeneree = informationBinaire;
		}
		
		public static void main(String[] args) throws InformationNonConformeException {
			SourceAleatoire S1 = new SourceAleatoire();
			S1.generer();
			System.out.println(S1.informationGeneree);
		}
}
