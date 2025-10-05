package sources;

import destinations.DestinationInterface;
import emmetteurs.Emetteur;
import information.Information;
import visualisations.SondeLogique;

/**
 * Classe d'un composant représentant la source fixe d'une chaîne de transmission
 */
public class SourceFixe extends Source {

	/**
	 * constructeur de la source fixe
	 */
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

	@Override
	public void connecter(DestinationInterface destination) {
		destinationsConnectees.add(destination);
	}

	@Override
	public void connecter(Emetteur emetteur) {
		destinationsConnectees.add(emetteur);
	}

}


