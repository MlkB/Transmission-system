package emmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;
import sources.Source;

public class Emetteur<T> extends Source<Float> implements  DestinationInterface <T>{        
    
    private Information<Boolean> informationRecue;
    private String TypeCodage; 

    public Emetteur(String Typecodage) {
        super();
        this.TypeCodage=Typecodage;
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
                boolean previousLevel = false; // niveau précédent : false = -1, true = 1
                for (Boolean bit : informationRecue) {
                    if (bit) {
                        // Inverse le niveau par rapport au précédent
                        previousLevel = !previousLevel;
                    }
                    informationGeneree.add(previousLevel ? 1.0f : -1.0f);
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