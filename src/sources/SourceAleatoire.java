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

	/**
	 * longueur du message aléatoire
	 */
	public Integer length = null;
	/**
	 * graine du générateur aléatoire
	 */
	public Integer seed = null;

	/**
	 * constructeur de la source aléatoire
	 */
	public SourceAleatoire() {
			super();
		}

	/**
	 * permet de fixer une graine pour la génération du message aléatoire
	 * @param seed la graine à utiliser pour la génération aléatoire
	 */
	public void setSeed(int seed) {
			this.seed = seed;
		}

	/**
	 * permet de fixer la longueur du message aléatoire
	 * @param length la longueur du message à générer
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
	@SuppressWarnings("unchecked")
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
	@SuppressWarnings("unchecked")
	public void connecter(DestinationInterface destination) {
		destinationsConnectees.add(destination);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void connecter(Emetteur emetteur) {
		destinationsConnectees.add(emetteur);
	}
}