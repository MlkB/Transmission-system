package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;

/**
 * classe permettant de décoder l'information binaire
 * en retirant la redondance ajoutée par le codeur
 */
public class DecodageReception extends Transmetteur<Boolean, Boolean> implements DestinationInterface<Boolean> {
    private Information<Boolean> informationRecue;
    private Information<Boolean> informationGeneree;

    /**
     * constructeur non implémenté de la classe DecodageReception
     */
    public DecodageReception() {}

    @Override
    public void recevoir(Information<Boolean> information) throws InformationNonConformeException {
        if (information == null) {
            throw new InformationNonConformeException("Information reçue est nulle");
        }
        this.informationRecue = information;

        // Décodage puis émission
        emettre();
    }

    @Override
    public void emettre() throws InformationNonConformeException {
        informationGeneree = new Information<>();

        // Ensuite : parcourir les triplets de bits
        for (int i = 0; i + 2 < informationRecue.nbElements(); i += 3) {
            boolean b1 = informationRecue.iemeElement(i);
            boolean b2 = informationRecue.iemeElement(i + 1);
            boolean b3 = informationRecue.iemeElement(i + 2);

            // Appliquer la règle de décodage donnée
            boolean decoded;
            int triplet = (b1 ? 4 : 0) + (b2 ? 2 : 0) + (b3 ? 1 : 0);

            switch (triplet) {
                case 0b000: decoded = false; break;
                case 0b001: decoded = true;  break;
                case 0b010: decoded = false; break;
                case 0b011: decoded = false; break;
                case 0b100: decoded = true;  break;
                case 0b101: decoded = true;  break;
                case 0b110: decoded = false; break;
                case 0b111: decoded = true;  break;
                default: decoded = false; // sécurité
            }

            informationGeneree.add(decoded);
        }

        // Enfin, transmettre aux destinations connectées
        for (DestinationInterface<Boolean> dest : destinationsConnectees) {
            dest.recevoir(informationGeneree);
        }
    }
}
