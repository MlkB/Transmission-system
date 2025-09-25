package sources;

import emmetteurs.Emetteur;
import information.Information;
import visualisations.SondeLogique;

public class SourceFixe extends Source {

	public SourceFixe() {
		super();
	}
	
	public void generer(String message) {
		Information<Boolean> informationBinaire = new Information<Boolean>();
        for (int j = 0; j < message.length(); j++) {
        	if (message.charAt(j) =='1') informationBinaire.add(true);
        	else if (message.charAt(j) =='0') informationBinaire.add(false);
        	else {
        		break;
        	}
        }
        
        this.informationGeneree = informationBinaire;
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


