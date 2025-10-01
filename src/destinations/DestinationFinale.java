package destinations;

import information.Information;
import information.InformationNonConformeException;

/**
 * Classe d'un composant héritant de Destination
 * etant la destination finale d'une chaîne de transimission
 */

public class DestinationFinale<T> extends Destination<T> {

	/**
	 * fonction peremttant de recevoir une information de la part d'un transmetteur
	 * ou d'un recepteur
	 * @param information  l'information  à recevoir
	 * @throws InformationNonConformeException
	 */
	@Override
	public void recevoir(Information<T> information) throws InformationNonConformeException {
        if (information == null) {
            throw new InformationNonConformeException("Information reçue est nulle !");
        }
		this.informationRecue = information;
		System.out.println("DestinationFinale: reçu " + information);
	}

	/**
	 * fonction permettant de récupérer l'information reçue par la destination finale
	 * @return l'information reçue par la destination
	 */
	public Information<T> getInformation() {
		return this.informationRecue;
	}
}
