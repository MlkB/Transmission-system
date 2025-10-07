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

        // Utiliser un itérateur pour O(n) au lieu de iemeElement qui est O(n²)
        java.util.Iterator<Boolean> iter = informationRecue.iterator();

        // Parcourir les triplets de bits
        while (iter.hasNext()) {
            // Lire un triplet (3 bits) - s'assurer qu'on a bien 3 bits disponibles
            boolean b1 = iter.next();
            if (!iter.hasNext()) break;  // Triplet incomplet
            boolean b2 = iter.next();
            if (!iter.hasNext()) break;  // Triplet incomplet
            boolean b3 = iter.next();

            // Appliquer la règle de décodage avec correction d'erreur (majorité)
            int triplet = (b1 ? 4 : 0) + (b2 ? 2 : 0) + (b3 ? 1 : 0);

            boolean decoded;
            switch (triplet) {
                case 0b000: decoded = false; break;  // 010 avec 1 erreur
                case 0b001: decoded = true;  break;  // 101 avec 1 erreur
                case 0b010: decoded = false; break;  // 010 correct
                case 0b011: decoded = false; break;  // 010 avec 1 erreur
                case 0b100: decoded = true;  break;  // 101 avec 1 erreur
                case 0b101: decoded = true;  break;  // 101 correct
                case 0b110: decoded = false; break;  // 010 avec 1 erreur
                case 0b111: decoded = true;  break;  // 101 avec 1 erreur
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
