package application;

import destinations.DestinationFinale;
import information.InformationNonConformeException;
import sources.SourceFixe;
import sources.SourceAleatoire;
import transmetteurs.TransmetteurParfait;

public class Application {
	public static void main(String[] args) throws InformationNonConformeException {
		DestinationFinale destination = new DestinationFinale();
		TransmetteurParfait transmetteur = new TransmetteurParfait();
		SourceFixe sourceF = new SourceFixe();
		SourceAleatoire sourceA = new SourceAleatoire();
		
		sourceF.connecter(transmetteur);
		sourceA.connecter(transmetteur);
		transmetteur.connecter(destination);
		
		sourceA.emettre();
		transmetteur.emettre();
		System.out.println(destination.getInformationRecue());
		
	}
}
