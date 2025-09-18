package sources;

import information.Information;

/**
 * Classe d'un composant représentant la source fixe d'une chaîne de transmission
 */
public class SourceFixe extends Source {

	public SourceFixe() {
		super();
	}

	/**
	 * permet de générer un message booléen
	 * @param message
	 * le paramètre message doit être sous la forme d'une chaîne de caractères représentant un message
	 * binaire
	 */
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


