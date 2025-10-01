package transmetteurs;
import java.util.Arrays;
import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;

/**
 * Récepteur : conversion analogique → binaire par moyennage et décision par seuil.
 * Pour chaque symbole (bloc de {@code nbEch} échantillons), on calcule la moyenne
 * puis on décide {@code bit = (moy >= seuil)}.
 */
public class Recepteur extends Transmetteur<Float, Boolean> implements DestinationInterface<Float> {
    private final int nbEch;
    private final float seuil;
    private String typeCodage;
    
    /**
     * Construit un récepteur paramétré.
     * @param nbEch nombre d'échantillons par symbole (strictement > 0)
     * @param seuil seuil de décision (typiquement 0.0 pour niveaux ±1)
     * @param typeCodage indicatif de forme d'onde (non utilisé ici pour la décision)
     */

    public Recepteur(int nbEch, Float seuil, String typeCodage){
        super();
        if(nbEch <= 0){
            throw new IllegalArgumentException("nbEch doit être > 0");
        }
        this.typeCodage = (typeCodage == null) ? "RZ" : typeCodage; // fallback
        this.nbEch = nbEch;
        this.seuil = seuil;
    }
    
    /**
     * Construit un récepteur avec seuil par défaut (0.5) et forme "RZ".
     * @param nbEch nombre d'échantillons par symbole (> 0)
     */
    /* Constructeur par défaut : signaux entre 0 et 1 */
    public Recepteur(int nbEch){
        this(nbEch,0.5f,"RZ");
    }

    /**
     * Reçoit l'information analogique en entrée du récepteur.
     * @param information trame analogique (échantillons {@code Float})
     * @throws InformationNonConformeException si {@code information} est {@code null}
     */
    @Override
    public void recevoir(Information<Float> information) throws InformationNonConformeException {
        if (information == null) {
            throw new InformationNonConformeException("L'information reçue est nulle");
        }
        this.informationRecue = information;
        emettre();
    }

    /**
     * Convertit l'information analogique reçue en bits :
     * découpe en blocs de {@code nbEch} échantillons, calcule la moyenne par bloc
     * puis applique la décision {@code moy >= seuil}. Émet ensuite vers les destinations.
     * @throws InformationNonConformeException si l'information reçue est invalide
     */
    @Override
    public void emettre() throws InformationNonConformeException {
        int n = this.informationRecue.nbElements();
        int nbSymbols = n / nbEch; // -> Combien de bits reçus

        Boolean[] bits = new Boolean[nbSymbols];

        for(int i = 0; i < nbSymbols; i++) {
            float moy = 0f;

            int debut = i * nbEch;
            int fin = (i + 1) * nbEch;

            // calcul de la moyenne sur le symbole
            for (int j = debut; j < fin; j++) {
                moy += this.informationRecue.iemeElement(j);
            }
            moy /= nbEch;

            // décision en fonction du seuil
            bits[i] = moy >= seuil;
        }

        this.informationEmise = new Information<>(bits);
        System.out.println("Recepteur: bits décodés = " + Arrays.toString(bits));

        /* On émet vers la ou les destinations connectée(s) */
        for(DestinationInterface<Boolean> destination : destinationsConnectees){
            destination.recevoir(informationEmise);
        }
    }
    
    /**
     * Connecte une destination binaire au récepteur.
     * @param destination destination à connecter (ignorée si {@code null})
     */
    @Override
    public void connecter(DestinationInterface<Boolean> destination) {
    	if(destination != null) {
        destinationsConnectees.add(destination);
    	}
    }
}