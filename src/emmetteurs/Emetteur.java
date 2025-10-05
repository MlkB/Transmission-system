package emmetteurs;

import destinations.DestinationInterface;

import information.Information;
import information.InformationNonConformeException;
import sources.Source;
import transmetteurs.Transmetteur;
import visualisations.Sonde;
import visualisations.SondeAnalogique;
import visualisations.SondeLogique;

/**
 * Classe décrivant l'émetteur utilisé par la chaine de transmission
 * pour coder le message booléen en message analogique
 * @param <T> type de l'information
 */
public class Emetteur<T> extends Source<Float> implements DestinationInterface <T>{

    /**
     * information reçue par l'émetteur
     */
    private Information<Boolean> informationRecue;
    /**
     * codage numérique vers analogique utilisé par l'émetteur
     */
    private String typeCodage;
    /**
     * nombre d'échantillon utilisé par l'émetteur pour le codage
     */
    private int nbEch;

    /**
     * constrycteur de la casse emetteur
     * @param typeCodage donne le codage utilisé par l'émetteur
     * @param nbEch donne le nombre d'échantillons utilisés par l'émetteur
     */
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
     * @throws InformationNonConformeException si le type de codage est inconnu
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
                    for (int i = 0; i < nbEch-1; i++) {
                        informationGeneree.add(bit ? 1.0f : -1.0f);
                    }
                    informationGeneree.add(0.0f); // Retour à zéro
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
                            // Pas de transition si le bit suivant est identique
                            value = level;
                        } else {
                            // Transition progressive
                            if (i < third) {
                                // Montée de 0 -> niveau au début du bit
                                float alpha = (float) i / third;
                                value = alpha * level;
                            } else if (i < 2 * third) {
                                // Niveau stable
                                value = level;
                            } else {
                                // Descente niveau -> 0 si bit différent
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

    /**
     * permet de connecter l'émetteur au transmetteur
     * @param transmetteurLogique donne le transmetteur logique auquel
     *                            l'émetteur doit se connecter
     */
    public void connecter(Transmetteur transmetteurLogique) {
        // Vérifier si déjà connecté
        if (!destinationsConnectees.contains(transmetteurLogique)) {
            destinationsConnectees.add(transmetteurLogique);
        }
    }


         @Override
         public void connecter(Emetteur emetteur) {

            throw new UnsupportedOperationException("Unimplemented method 'connecter'");
         }

    /**
     * permet de connecter l'émetteur à une sonde
     * @param sonde la sonde à connecter
     */
    public void connecterSonde(Sonde sonde) {
            // Vérifier si déjà connecté
            if (!destinationsConnectees.contains(sonde)) {
                destinationsConnectees.add(sonde);
            }
         }

        
       
    }

        

        
