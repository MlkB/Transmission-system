package emmetteurs;

import destinations.DestinationInterface;

import information.Information;
import information.InformationNonConformeException;
import sources.Source;
import visualisations.SondeAnalogique;

public class Emetteur<T> extends Source<Float> implements  DestinationInterface <T>{        
    
    private Information<Boolean> informationRecue;
    private String TypeCodage;
    private int nbEch;  

    public Emetteur(String Typecodage, int nbEch) {
        super();
        this.TypeCodage=Typecodage;
        this.nbEch=nbEch;
    }

    //Iterator<DestinationInterface<E>> it = destinationsConnectees.iterator();


        public void convertir_signal() throws InformationNonConformeException {
                    
            informationGeneree = new Information<>();
            // codage en ligne 
        
            if ("NRZ".equalsIgnoreCase(TypeCodage)) {
                 for (Boolean bit : informationRecue) {
                    informationGeneree.add(bit ? 1.0f : -1.0f);
                }
            
            
            } else if ("RZ".equalsIgnoreCase(TypeCodage)) {
                for (Boolean bit : informationRecue) {
                    informationGeneree.add(bit ? 1.0f : -1.0f);
                    informationGeneree.add(0.0f); // Retour à zéro
                }
            } else if ("NRZT".equalsIgnoreCase(TypeCodage)) {
               // boolean previousLevel = false; // niveau précédent : false = -1, true = 1
                for (Boolean bit : informationRecue) {
                    if (bit) {
                // bit 1 → on envoie uniquement +1
                        for (int i = 0; i < nbEch; i++) {
                            informationGeneree.add(1.0f);
                        }
                    } else {
                        // bit 0 → on envoie uniquement -1
                        for (int i = 0; i < nbEch; i++) {
                            informationGeneree.add(-1.0f);
                        }
                    }
                //previousLevel = !previousLevel; // Inversion du niveau précédent
             }
            } else {
                throw new InformationNonConformeException("Type de codage inconnu");
            }
        }
          @Override
        public void recevoir(Information<T> information) throws InformationNonConformeException {
            if (information == null) {
                throw new InformationNonConformeException("L'information est vide");
            }
            this.informationRecue = (Information<Boolean>) information;
            convertir_signal();
            emettre();
        
        }

          @Override
          public Information getInformationRecue() {
                return this.informationRecue;
          }

          @Override
          public void connecter(SondeAnalogique sondeTransmetteur) {
               for (DestinationInterface<Float> destinationConnectee : destinationsConnectees) {
                     if (destinationConnectee == sondeTransmetteur) {
                          return; // Déjà connecté
                      
                     } else { destinationsConnectees.add(sondeTransmetteur);   }       
              
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



     

 

    }