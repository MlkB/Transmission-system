package sources;

import information.Information;
import information.InformationNonConformeException;

/**
 * Classe d'un composant représentant la source fixe d'une chaîne de transmission
 */
public class SourceFixe extends Source<Boolean> {

    public SourceFixe() {
        super();
    }

    /**
     * permet de générer un message booléen
     * @param message
     * chaîne binaire composée uniquement de '0' et '1'
     * @throws InformationNonConformeException si un caractère est invalide
     */
    public void generer(String message) throws InformationNonConformeException {
        Information<Boolean> informationBinaire = new Information<>();
        for (int j = 0; j < message.length(); j++) {
            char c = message.charAt(j);
            if (c == '1') {
                informationBinaire.add(true);
            } else if (c == '0') {
                informationBinaire.add(false);
            } else {
                throw new InformationNonConformeException(
                    "Caractère invalide dans le message : " + c);
            }
        }
        this.informationGeneree = informationBinaire;
    }
}
