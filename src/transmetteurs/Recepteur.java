package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;

/**
 * Classe représentant un objet de type récepteur dans une chaîne de transmission
 * recevant l'information d'un transmetteur et la transmettant à une destination finale.
 * L'objet reçoit une information analogique et la reconvertit en binaire
 */
public class Recepteur extends Transmetteur<Float, Boolean> implements DestinationInterface<Float> {
    
    private final int nbEch;
    private final float seuil;

    public Recepteur(int nbEch, Float seuil){
        super();
        if(nbEch <= 0){
            throw new IllegalArgumentException("nbEch doit être > 0");
        }
        this.nbEch = nbEch;
        this.seuil = seuil;
    }
    /*Par défaut signaux de 0 à 1*/
    public Recepteur(int nbEch){
        this(nbEch,0.5f);
    }

    /**
     * permet de recevoir l'information de la part d'un transmetteur
     * @param information  l'information  à recevoir
     * @throws InformationNonConformeException
     */
    @Override
    public void recevoir(Information<Float> information) throws InformationNonConformeException {
        this.informationRecue = information;
        emettre();
    }

    /*Conversion analogique(float) en logique(Boolean)*/

    /**
     * Convertit un message analogique en message booléen
     * @throws InformationNonConformeException
     */
    @Override
    public void emettre() throws InformationNonConformeException {
        int n = this.informationRecue.nbElements();
        int nbSymbols = n/nbEch; // -> Combien de bits reçu

        Boolean[] bits = new Boolean[nbSymbols];

        for(int i = 0; i < nbSymbols; i++){
            float moy = 0f;

            /*On fixe le début et la fin de l'intervalle en cours*/
            int debut = i * nbEch;
            int fin = (i+1) * nbEch;

            /*On calcule la moyenne de la valeur des échantillons du symbole*/
            for(int j = debut; j < fin; j++){
                moy += this.informationRecue.iemeElement(j);
            }

            moy /= nbEch;

            /*Décision si true ou false en fonction du seuil*/
            if(moy < seuil){
                bits[i] = false;
            } else if (moy >= seuil) {
                bits[i] = true;
            }

        }
        this.informationEmise = new Information<>(bits);

        /*On émet vers la ou les destinations connectée(s) */
        for(DestinationInterface<Boolean> destination : destinationsConnectees){
            destination.recevoir(informationEmise);
        }
    }

    @Override
    public void connecter(DestinationInterface<Boolean> destination) {
      
    }
}
