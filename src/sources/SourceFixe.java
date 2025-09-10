package sources;

import information.Information;

public class SourceFixe extends Source {

	public SourceFixe() {
		super();
	}
	
	public void genererInformation(String message) {
		Information<Boolean> informationBinaire = new Information<Boolean>();
        for (int j = 0; j < message.length(); j++) {
        	if (message.charAt(j) =='1') informationBinaire.add(true);
        	else if (message.charAt(j) =='0') informationBinaire.add(false);
        	else {
        		System.out.println("aie aie aie");
        		break;
        	}
        }
        
        this.informationGeneree = informationBinaire;
	}
	
	public static void main(String[] args) {
		SourceFixe S1 = new SourceFixe();
		S1.genererInformation("ojfozj");
		System.out.println(S1.informationGeneree);
	}
	
}
