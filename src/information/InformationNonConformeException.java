package information;

/**
 * exception levée quand l'information n'est pas du type ou de la forme
 * attendue
 */
public class InformationNonConformeException extends Exception {

    /**
     * Identifiant de version de la classe (utilisé pour la sérialisation).
     */
    private static final long serialVersionUID = 1917L;

    /**
     * constructeur de la classe sans description donnée
     */
    public InformationNonConformeException() {
	super();
    }

    /**
     * constructeur de la classe avec une description donnée
     * @param motif description à afficher avec l'exception
     */
    public InformationNonConformeException(String motif) {
	super(motif);
    }
}
