package sources;

import java.util.Random;
import information.Information;
import information.InformationNonConformeException;

/**
 * Classe d'un composant représentant la source aléatoire d'une chaîne de transmission
 */
public class SourceAleatoire extends Source<Boolean> {
	
    private Integer length = null;
    private Integer seed = null;

    public SourceAleatoire() {
        super();
    }

    /**
     * permet de fixer une graine pour la génération du message aléatoire
     * @param seed graine pseudo-aléatoire
     */
    public void setSeed(int seed) {
        this.seed = seed;
    }

    /**
     * permet de fixer la longueur du message aléatoire
     * @param length taille du message
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * permet de générer un message booléen aléatoire
     * - longueur 100 par défaut ou fixée par {@link #setLength}
     * - généré de manière pseudo-aléatoire, avec ou sans graine
     * @throws InformationNonConformeException si un élément du message n'est pas un booléen
     */
    public void generer() throws InformationNonConformeException {
        if (this.length == null) {
            this.length = 100;
        }

        Random rand = (this.seed == null) ? new Random() : new Random(this.seed);
        Information<Boolean> informationBinaire = new Information<>();

        for (int j = 0; j < length; j++) {
            informationBinaire.add(rand.nextBoolean());
        }

        // vérification de cohérence (optionnelle car rand.nextBoolean() renvoie toujours un booléen)
        for (int j = 0; j < length; j++) {
            Boolean bit = informationBinaire.iemeElement(j);
            if (bit == null) {
                throw new InformationNonConformeException("Un élément généré est nul !");
            }
        }

        this.informationGeneree = informationBinaire;
    }
}
