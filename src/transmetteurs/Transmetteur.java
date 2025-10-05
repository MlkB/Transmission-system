package transmetteurs;

import sources.*;
import visualisations.Sonde;
import visualisations.SondeAnalogique;
import destinations.*;
import information.*;

import java.util.*;

/** 
 * Classe Abstraite d'un composant transmetteur d'informations dont
 * les éléments sont de type R en entrée et de type E en sortie;
 * l'entrée du transmetteur implémente l'interface
 * DestinationInterface, la sortie du transmetteur implémente
 * l'interface SourceInterface
 * @author prou
 * @param <R> le type des informations reçues en entrée
 * @param <E> le type des informations émises en sortie
 */
public abstract  class Transmetteur <R,E> implements  DestinationInterface <R>, SourceInterface <E> {
   
    /** 
     * la liste des composants destination connectés en sortie du transmetteur 
     */
    protected LinkedList <DestinationInterface <E>> destinationsConnectees;
   
    /** 
     * l'information reçue en entrée du transmetteur 
     */
    protected Information <R>  informationRecue;
		
    /** 
     * l'information émise en sortie du transmetteur
     */		
    protected Information <E>  informationEmise;
   
    /** 
     * un constructeur factorisant les initialisations communes aux
     * réalisations de la classe abstraite Transmetteur
     */
    public Transmetteur() {
		destinationsConnectees = new LinkedList <DestinationInterface <E>> ();
		informationRecue = null;
		informationEmise = null;
    }
   	
    /**
     * retourne la dernière information reçue en entrée du
     * transmetteur
     * @return une information   
     */
    public Information <R>  getInformationRecue() {
    	return this.informationRecue;
    }

    /**
     * retourne la dernière information émise en sortie du
     * transmetteur
     * @return une information   
     */
    public Information <E>  getInformationEmise() {
    	return this.informationEmise;
    }

    public Information <Float>  getInformationAnalogEmise() {
    	return (Information<Float>) this.informationEmise;
    }
    /**
     * connecte une destination à la sortie du transmetteur
     * @param destination  la destination à connecter
     */
    public void connecter (DestinationInterface<E> destination) {
    	destinationsConnectees.add((DestinationInterface<E>) destination);
    }

    /**
     * connecte un émetteur (requis par SourceInterface, non utilisé pour un transmetteur)
     * @param emetteur  l'émetteur à connecter
     */
    public void connecter (emmetteurs.Emetteur emetteur) {
    	// Un transmetteur ne se connecte pas à un émetteur
    	// Cette méthode est requise par SourceInterface mais n'est pas utilisée
    }

    /**
     * déconnecte une destination de la la sortie du transmetteur
     * @param destination  la destination à déconnecter
     */
    public void deconnecter (DestinationInterface <E> destination) {
    	destinationsConnectees.remove(destination);
    }
   	    
    /**
     * reçoit une information.  Cette méthode, en fin d'exécution,
     * appelle la méthode émettre.
     * @param information  l'information  reçue
     * @throws InformationNonConformeException si l'Information comporte une anomalie
     */
    public  abstract void recevoir(Information <R> information) throws InformationNonConformeException;
   
    /**
     * émet l'information construite par le transmetteur
     * @throws InformationNonConformeException si l'Information comporte une anomalie
     */
    public  abstract void emettre() throws InformationNonConformeException;   


    
}