package sources;

import information.Information;

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

}


