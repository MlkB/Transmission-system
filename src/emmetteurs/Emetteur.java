package emmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;
import sources.Source;
import sources.SourceInterface;
import transmetteurs.Transmetteur;
import visualisations.Sonde;
import visualisations.SondeAnalogique;
import visualisations.SondeLogique;

public class Emetteur extends Source<Float> implements DestinationInterface<Boolean> {

    private Information<Boolean> informationRecue;
    private String typeCodage;
    private int nbEch;

    public Emetteur(String typeCodage, int nbEch) {
        super();
        this.typeCodage = (typeCodage == null) ? "RZ" : typeCodage;
        this.nbEch = nbEch;
    }

    /** Convertit l'information binaire en signal analogique */
    private void convertirSignal() throws InformationNonConformeException {
        informationGeneree = new Information<>();

        switch (typeCodage.toUpperCase()) {
            case "NRZ":
                for (Boolean bit : informationRecue) {
                    for (int i = 0; i < nbEch; i++) {
                        informationGeneree.add(bit ? 1.0f : -1.0f);
                    }
                }
                break;

            case "RZ":
                for (Boolean bit : informationRecue) {
                    for (int i = 0; i < nbEch - 1; i++) {
                        informationGeneree.add(bit ? 1.0f : -1.0f);
                    }
                    informationGeneree.add(0.0f);
                }
                break;

            case "NRZT":
                for (int b = 0; b < informationRecue.nbElements(); b++) {
                    float level = informationRecue.iemeElement(b) ? 1.0f : -1.0f;
                    float nextLevel = (b < informationRecue.nbElements() - 1)
                            ? (informationRecue.iemeElement(b + 1) ? 1.0f : -1.0f)
                            : 0.0f;

                    int third = nbEch / 3;
                    for (int i = 0; i < nbEch; i++) {
                        float value;
                        if (nextLevel == level) {
                            value = level;
                        } else if (i < third) {
                            value = level * (float) i / third;
                        } else if (i < 2 * third) {
                            value = level;
                        } else {
                            value = level * (1 - (float) (i - 2 * third) / third);
                        }
                        informationGeneree.add(value);
                    }
                }
                break;

            default:
                throw new InformationNonConformeException("Type de codage inconnu : " + typeCodage);
        }
    }

    @Override
    public void recevoir(Information<Boolean> information) throws InformationNonConformeException {
        if (information == null) {
            throw new InformationNonConformeException("L'information est vide");
        }
        this.informationRecue = information;
        convertirSignal();
        emettre(); // send to all connected destinations
    }

    @Override
    public Information<Boolean> getInformationRecue() {
        return this.informationRecue;
    }

    /** Connecte une destination qui reçoit des informations Float 
     * @param transmetteurLogique2 */
   public void connecter(Transmetteur<Float,Boolean> transmetteurLogique) {
    if (!destinationsConnectees.contains(transmetteurLogique)) {
        destinationsConnectees.add((DestinationInterface<Float>) transmetteurLogique);
    }
   }


    /** Connecte une sonde, qui doit aussi être DestinationInterface<Float> */
    public void connecterSonde(DestinationInterface<Float> sonde) {
        if (!destinationsConnectees.contains(sonde)) {
            destinationsConnectees.add((DestinationInterface<Float>) sonde);
        }
    }



}


