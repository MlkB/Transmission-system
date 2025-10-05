package visualisations;
	
/** 
 * @author B. Prou
 * Updated by E. Cousin - 2021
 *
 */	

import java.util.*;
import javax.swing.*;

/**
 * Classe de base pour une fenêtre de visualisation.
 * <p>
 * Gère la position automatique des fenêtres créées pour éviter le chevauchement.
 * Chaque instance est ajoutée à une liste globale {@code lesVues} pour permettre
 * la fermeture collective.
 * </p>
 */
 public class Vue extends JFrame{

    /** Liste de toutes les instances de Vue créées. */
    private static LinkedList<Vue> lesVues=new LinkedList<Vue>();
    /** Identifiant de version pour la sérialisation. */
    private static final long serialVersionUID = 1917L;

    /** Position horizontale pour la prochaine fenêtre. */
    protected  static int xPosition = 0;
    /** Position verticale pour la prochaine fenêtre. */
    protected  static int yPosition = 0;
    /** Décalage vertical entre deux fenêtres. */
    private static int yDecalage = 200;

    /**
     * Retourne la position X pour placer une nouvelle fenêtre.
     *
     * @return position X
     */
    public static int getXPosition() {
	xPosition += 0;
	return xPosition - 0;
    }

    /**
     * Retourne la position Y pour placer une nouvelle fenêtre.
     *
     * @return position Y
     */
    public static int getYPosition() {
	yPosition += yDecalage;
	return yPosition - yDecalage;
    }

    /**
     * Constructeur d'une fenêtre de visualisation.
     * <p>
     * Ajoute automatiquement la fenêtre à la liste globale {@code lesVues}.
     * </p>
     *
     * @param nom le titre de la fenêtre
     */
    public  Vue (String nom) {          
        super(nom);
        lesVues.add(this);
    }

    /**
     * Réinitialise la position verticale pour la prochaine fenêtre.
     */
    public static void resetPosition(){
	yPosition = 0;
    }

      /** Définit la position horizontale pour la prochaine fenêtre.
            *
            * @param x nouvelle position X
     */
    public static void setXPosition(int x){
	xPosition = x;
    }

    /**
     * Ferme toutes les fenêtres créées et réinitialise les positions.
     */
    public static void kill(){
        for(Vue v:lesVues)
            v.dispose();
        lesVues.clear();
        resetPosition();
    }
   
}
