package visualisations;
	
import information.Information;

/** 
 * Classe réalisant l'affichage (textuel) d'information composée
 * d'éléments de type T
 * @author prou
 * @param <T> : type des informations transportées par le canal de transmission
 */
public class SondeTextuelle <T> extends Sonde <T> {
   
    /**
     * pour construire une sonde textuelle
     * @param nom  le nom de la fenêtre d'affichage
     */
    public SondeTextuelle(String nom) {
	super(nom);
    }
   
    public void recevoir (Information <T> information) { 		 		 	
	informationRecue = information;
	System.out.println(nom + " : " + information);
    }
}
