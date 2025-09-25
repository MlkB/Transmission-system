package sources;

import java.util.Random;

import emmetteurs.Emetteur;
import information.Information;
import information.InformationNonConformeException;
import visualisations.SondeLogique; 

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

		@Override
		public void connecter(Emetteur emetteur) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'connecter'");
		}

		@Override
		public void connecterSonde(SondeLogique sondeSource) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'connecterSonde'");
		}
		
}