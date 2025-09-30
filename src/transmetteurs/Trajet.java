package transmetteurs;

/**
 * Classe représentant un trajet réfléchi dans un canal à trajets multiples.
 * Chaque trajet est caractérisé par un retard (tau) et une atténuation (alpha).
 */
public class Trajet {

    private int tau;        // retard en nombre d'échantillons
    private float alpha;    // coefficient d'atténuation (0 < alpha < 1)

    /**
     * Constructeur d'un trajet réfléchi
     * @param tau le retard en nombre d'échantillons (doit être >= 0)
     * @param alpha le coefficient d'atténuation (doit être entre 0 et 1)
     * @throws IllegalArgumentException si les paramètres sont invalides
     */
    public Trajet(int tau, float alpha) {
        if (tau < 0) {
            throw new IllegalArgumentException("Le retard tau doit être >= 0");
        }
        if (alpha < 0 || alpha > 1) {
            throw new IllegalArgumentException("L'atténuation alpha doit être entre 0 et 1");
        }
        this.tau = tau;
        this.alpha = alpha;
    }

    /**
     * Retourne le retard du trajet
     * @return le retard en nombre d'échantillons
     */
    public int getTau() {
        return tau;
    }

    /**
     * Retourne le coefficient d'atténuation du trajet
     * @return le coefficient d'atténuation
     */
    public float getAlpha() {
        return alpha;
    }

    /**
     * Modifie le retard du trajet
     * @param tau le nouveau retard (doit être >= 0)
     * @throws IllegalArgumentException si tau &lt; 0
     */
    public void setTau(int tau) {
        if (tau < 0) {
            throw new IllegalArgumentException("Le retard tau doit être >= 0");
        }
        this.tau = tau;
    }

    /**
     * Modifie le coefficient d'atténuation du trajet
     * @param alpha le nouveau coefficient (doit être entre 0 et 1)
     * @throws IllegalArgumentException si alpha n'est pas entre 0 et 1
     */
    public void setAlpha(float alpha) {
        if (alpha < 0 || alpha > 1) {
            throw new IllegalArgumentException("L'atténuation alpha doit être entre 0 et 1");
        }
        this.alpha = alpha;
    }

    @Override
    public String toString() {
        return "Trajet{tau=" + tau + ", alpha=" + alpha + "}";
    }
}