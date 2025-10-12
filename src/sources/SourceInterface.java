package sources;

import information.*;
import visualisations.SondeAnalogique;
import visualisations.SondeLogique;
import destinations.DestinationInterface;
import emmetteurs.Emetteur;

/** 
 * Interface d'un composant ayant le comportement d'une source
 * d'informations dont les éléments sont de type T
 * @author prou
 * @param <T> type des valeurs traitées
 */
public interface SourceInterface <T>  {
   
    /**
     * pour obtenir la dernière information émise par une source.
     * @return une information   
     */
    public Information <T>  getInformationEmise();
   
    /**
     * pour connecter une destination à la source
     * @param emetteur  la destination à connecter
     */
    public void connecter (Emetteur emetteur);
   
    /**
     * pour émettre l'information contenue dans une source
     * @throws InformationNonConformeException si l'Information comporte une anomalie
     */
    public void emettre() throws InformationNonConformeException; 
}
