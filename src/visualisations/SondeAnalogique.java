package visualisations;
	
import java.util.Iterator;

import destinations.DestinationInterface;
import information.Information;

/** 
 * Classe réalisant l'affichage d'information composée d'éléments
 * réels (float)
 * @author prou
 */
public class SondeAnalogique extends Sonde <Float> {

    /** la fenêtre d'affichage de la courbe */
    private VueCourbe vueCourbe;

    /**
     * pour construire une sonde analogique
     * @param nom  le nom de la fenêtre d'affichage
     */
    public SondeAnalogique(String nom) {
        super(nom);
        this.vueCourbe = null;
    }



    public void recevoir (Information <Float> information) {
        informationRecue = information;
        int nbElements = information.nbElements();
        float [] table = new float[nbElements];
        int i = 0;
        for (float f : information) {
            table[i] = f;
            i++;
        }
        // Créer la fenêtre seulement la première fois
        if (vueCourbe == null) {
            vueCourbe = new VueCourbe (table, nom);
        }
    }


   
    
}
