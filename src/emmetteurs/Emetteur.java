package emmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;
import sources.Source;

/**
 * Classe Emetteur représentant un objet de la chaîne de transmission
 * qui reçoit l'information booléenne de la source et la transforme
 * en information analogique
 * <ul>
 *   <li><b>NRZ</b> : niveau ±1 constant pendant tout le symbole.</li>
 *   <li><b>RZ</b>  : niveau ±1 sur {@code nbEch-1} échantillons, puis retour à 0.</li>
 *   <li><b>NRZT</b>: forme trapézoïdale (rampe–palier–rampe) ; retour à 0 si transition.</li>
 * </ul>
 * Les échantillons générés sont de type {@code Float}.
 * @param <T>
 */


/**
 * Construit un émetteur.
 * @param typeCodage forme d'onde ("NRZ", "RZ", "NRZT") ; si {@code null}, "RZ"
 * @param nbEch nombre d'échantillons par symbole (≥ 1)
 */
public class Emetteur extends Source<Float> implements  DestinationInterface<Boolean>{        
    
    private Information<Boolean> informationRecue;
    private String typeCodage;
    private int nbEch;  

    public Emetteur(String typeCodage, int nbEch) {
        super();
        this.typeCodage = (typeCodage == null) ? "RZ" : typeCodage;
        this.nbEch=nbEch;
    }

    //Iterator<DestinationInterface<E>> it = destinationsConnectees.iterator();

    /**
     * Fonction permettant de convertir l'information booléenne reçue
     * en information analogique selon trois types de codages :
     * NRZ, RZ et NRZT. Le type de codage est déterminé selon la valeur
     * de la fonction typeCodage
     * @throws InformationNonConformeException
     */
    private void convertirSignal() throws InformationNonConformeException {
        informationGeneree = new Information<>();

        if ("NRZ".equalsIgnoreCase(typeCodage)) {
            for (Boolean bit : informationRecue) {
                for (int i = 0; i < nbEch; i++) {
                    informationGeneree.add(bit ? 1.0f : -1.0f);
                }
            }
        } else if ("RZ".equalsIgnoreCase(typeCodage)) {
            for (Boolean bit : informationRecue) {
                for (int i = 0; i < nbEch - 1; i++) {
                    informationGeneree.add(bit ? 1.0f : -1.0f);
                }
                informationGeneree.add(0.0f); // retour à zéro
            }
        } else if ("NRZT".equalsIgnoreCase(typeCodage)) {
            for (int b = 0; b < informationRecue.nbElements(); b++) {
                float level = informationRecue.iemeElement(b) ? 1.0f : -1.0f;
                float nextLevel = (b < informationRecue.nbElements() - 1)
                        ? (informationRecue.iemeElement(b + 1) ? 1.0f : -1.0f)
                        : 0.0f; // retour à 0 après le dernier bit

                int third = nbEch / 3;

                for (int i = 0; i < nbEch; i++) {
                    float value;
                    if (nextLevel == level) {
                        value = level;
                    } else {
                        if (i < third) {
                            float alpha = (float) i / third;
                            value = alpha * level;
                        } else if (i < 2 * third) {
                            value = level;
                        } else {
                            float alpha = (float) (i - 2 * third) / third;
                            value = level * (1 - alpha);
                        }
                    }
                    informationGeneree.add(value);
                }
            }
        } else {
            throw new InformationNonConformeException("Type de codage inconnu");
        }
    }

    /**
     * Reçoit l'information binaire de la source, la convertit en analogique
     * selon la forme d'onde configurée puis l'émet vers les destinations connectées.
     * @param information information binaire à recevoir
     * @throws InformationNonConformeException si l'information est {@code null} ou si le type de codage est inconnu
     */
    @Override
    public void recevoir(Information<Boolean> information) throws InformationNonConformeException {
        if (information == null) {
            throw new InformationNonConformeException("L'information est vide");
        }
        this.informationRecue = information;
        convertirSignal();
        emettre();
    }

    /**
     * Fonction permettant de récupérer l'information reçue par l'émetteur
     * @return informationRecue l'information binaire reçue par l'émetteur
 
     */
    @Override
    public Information<Boolean> getInformationRecue() {
            return this.informationRecue;
      }



    }
    
    /**
     * analyse les arguments passés en ligne de commande
     * et initialise les attributs du Simulateur en conséquence
     * 
     * @param args  les arguments passés en ligne de commande
     * 
     * @throws Exception si un problème survient lors de l'analyse
     * des arguments
     *
     * 
    
        * @throws InformationNonConformeException si l'Information comporte une anomalie
        */