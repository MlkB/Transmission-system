package simulateur;

/**
 * classe ArgumentsException qui renvoie
 * une exception si les arguments du simulateur
 * ne sont pas corrects
 */
public class ArgumentsException extends Exception {
    
    private static final long serialVersionUID = 1789L;

    /**
     * constructeur de l'ecception
     * @param s la phrase à afficher
     */
    public ArgumentsException(String s) {
	super(s);
    }
}
