package emmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;
import sources.Source;

/**
 * Classe Emetteur représentant un objet de la chaîne de transmission
 * qui reçoit l'information booléenne de la source et la transforme
 * en information analogique
 * @param <T>
 */
public class Emetteur<T> extends Source<Float> implements  DestinationInterface <T>{        
    
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
    public void convertir_signal() throws InformationNonConformeException {

            informationGeneree = new Information<>();
            // codage en ligne

            if ("NRZ".equalsIgnoreCase(typeCodage)) {
                 for (Boolean bit : informationRecue) {
                     for (int i = 0; i < nbEch; i++) {
                         informationGeneree.add(bit ? 1.0f : -1.0f);
                     }
                }


            } else if ("RZ".equalsIgnoreCase(typeCodage)) {
                for (Boolean bit : informationRecue) {
                    informationGeneree.add(bit ? 1.0f : -1.0f);
                    informationGeneree.add(0.0f); // Retour à zéro
                }
            } else if ("NRZT".equalsIgnoreCase(typeCodage)) {
                for (Boolean bit : informationRecue) {
                    float level = bit ? 1.0f : -1.0f;
                    for (int i = 0; i < nbEch; i++) {
                        informationGeneree.add(level);
                    }
                }


            } else {
                throw new InformationNonConformeException("Type de codage inconnu");
            }
        }

    /**
     * Fonction permettant à l'émetteur de récupérer l'information envoyée par la source.
     * @param information  l'information  à recevoir
     * @throws InformationNonConformeException si l'information est nulle
     */
    @Override
    public void recevoir(Information<T> information) throws InformationNonConformeException {
            if (information == null) {
                throw new InformationNonConformeException("L'information est vide");
            }
            this.informationRecue = (Information<Boolean>) information;
            convertir_signal();
            emettre();

        }

    /**
     * Fonction permettant de récupérer l'information reçue par l'émetteur
     * @return informationRecue
     */
    @Override
    public Information getInformationRecue() {
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