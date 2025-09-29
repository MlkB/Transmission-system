package sources;

import java.util.Random;

import destinations.DestinationInterface;
import emmetteurs.Emetteur;
import information.Information;
import information.InformationNonConformeException;


/**
 * Classe d'un composant représentant la source aléatoire d'une chaîne de transmission
 */
public class SourceAleatoire extends Source {
	
		public Integer length = null;
		public Integer seed = null;

		public SourceAleatoire() {
			super();
		}

	/**
	 * permet de fixer une graine pour la génération du message aléatoire
	 * @param seed
	 */
	public void setSeed(int seed) {
			this.seed = seed;
		}

	/**
	 * permet de fixer la longueur du message aléatoire
	 * @param length
	 */
		public void setLength(int length) {
			this.length = length;
		}

	/**
	 * permet de générer un message booléen aléatoire
	 * le message aléatoire est soit de longueur 100, soit de longueur fixée par le paramètre length
	 * le message aléatoire est soit pseudo-aléatoire, soit généré selon la graine fixée par le paramètre seed
	 * @throws InformationNonConformeException si un élément du message n'est pas un booléen
	 */
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
	public void connecter(DestinationInterface destination) {
		destinationsConnectees.add(destination);
	}

	@Override
	public void connecter(Emetteur emetteur) {
		destinationsConnectees.add(emetteur);
	}
}