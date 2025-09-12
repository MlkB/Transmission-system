package sources;

import java.util.Random;
import information.Information;
import information.InformationNonConformeException;

public class SourceAleatoire extends Source {
	
		public Integer length = null;
		public Integer seed = null;

		public SourceAleatoire() {
			super();
		}
		
		public void setSeed(int seed) {
			this.seed = seed;
		}
		
		public void setLength(int length) {
			this.length = length;
		}
		
		public void generer() throws InformationNonConformeException{
			if (this.length == null) this.length = 100;
			if (this.seed == null) {
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
			else {
				Random rand = new Random(this.seed);
			    Information<Boolean> informationBinaire = new Information<Boolean>();
			    for (int j = 0; j<length; j++) {
			    	informationBinaire.add(rand.nextBoolean());
			    }
			    for (int j = 0; j<length; j++) {
			    	if (informationBinaire.iemeElement(j) != true && informationBinaire.iemeElement(j) != false) throw new InformationNonConformeException();
			    }
			    
			    this.informationGeneree = informationBinaire;
			}
		}
		
		public static void main(String[] args) throws InformationNonConformeException {
			SourceAleatoire S1 = new SourceAleatoire();
			S1.generer();
			System.out.println(S1.informationGeneree);
		}
}
