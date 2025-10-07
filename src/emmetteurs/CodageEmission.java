package emmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;
import sources.Source;
import transmetteurs.Transmetteur;
import visualisations.Sonde;

/**
 * Classe représentant l'ajout de redondance au message pour réduire les erreurs
 */
public class CodageEmission extends Source<Boolean> implements DestinationInterface<Boolean> {

    /**
     * constructeur non implémenté de la classe CodageEmission
     */
    public CodageEmission() {}

    /**
     * Information reçue avant codage
     */
    private Information<Boolean> informationRecue;

    /**
     * permet de récupérer l'information reçue par le codeur
     * @return l'information reçue
     */
    @Override
    public Information<Boolean> getInformationRecue() {
        return informationRecue;
    }

    /**
     * fonction permettant de recevoir l'information
     * @param information  l'information à recevoir
     * @throws InformationNonConformeException si l'information n'est pas conforme
     */
    @Override
    public void recevoir(Information<Boolean> information) throws InformationNonConformeException {
        if (information == null) {
            throw new InformationNonConformeException("Information reçue est nulle");
        }

        this.informationRecue = information;

        // On génère la nouvelle information redondante
        informationGeneree = new Information<>();

        for (Boolean bit : information) {
            if (bit) {
                // 1 -> 101 (true, false, true)
                informationGeneree.add(true);
                informationGeneree.add(false);
                informationGeneree.add(true);
            } else {
                // 0 -> 010 (false, true, false)
                informationGeneree.add(false);
                informationGeneree.add(true);
                informationGeneree.add(false);
            }
        }

        // Une fois générée, on émet vers les destinations connectées
        emettre();
    }


    @Override
    @SuppressWarnings("unchecked")
    public void connecter(Emetteur emetteur) {
        // Vérifier si déjà connecté
        if (!destinationsConnectees.contains(emetteur)) {
            destinationsConnectees.add(emetteur);
        }
    }

    /**
     * permet de connecter l'émetteur à une sonde
     * @param sonde la sonde à connecter
     */
    @SuppressWarnings("unchecked")
    public void connecterSonde(Sonde sonde) {
        // Vérifier si déjà connecté
        if (!destinationsConnectees.contains(sonde)) {
            destinationsConnectees.add(sonde);
        }
    }

}
