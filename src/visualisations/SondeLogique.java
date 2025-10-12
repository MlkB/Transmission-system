package visualisations;

import destinations.DestinationInterface;
import information.Information;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Classe réalisant l'affichage d'information composée d'éléments
 * booléens
 * @author prou
 */
public class SondeLogique extends Sonde <Boolean> {

    /** le nombre de pixels en largeur pour un élément d'information
     * Boolean à afficher dans la fenêtre */
    private int nbPixels;

    /** la fenêtre d'affichage de la courbe */
    private VueCourbe vueCourbe;

    /**
     * pour construire une sonde logique
     * @param nom  le nom de la fenêtre d'affichage
     * @param nbPixels  le nombre pixels en largeur pour un élément d'information Boolean à afficher dans la fenêtre
     */
    public SondeLogique(String nom, int nbPixels) {
        super(nom);
        this.nbPixels = nbPixels;
        this.vueCourbe = null;
        System.err.println("DEBUG: Creating SondeLogique for " + nom + " @" + this);
    }

    public void recevoir (Information <Boolean> information) {
	informationRecue = information;
	int nbElements = information.nbElements();
	boolean [] table = new boolean[nbElements];
	int i = 0;
	for (boolean b : information) {
            table[i] = b;
            i++;
	}
	// Ne plus créer de fenêtre Java, tout est affiché en Python maintenant
	// if (vueCourbe == null) {
      	//     vueCourbe = new VueCourbe (table,  nbPixels, nom);
	// }

	// Exporter les données vers CSV pour affichage Python
	exportToCSV(table);
    }

    /**
     * Exporte les données vers un fichier CSV
     * @param table tableau de valeurs booléennes à exporter
     */
    private void exportToCSV(boolean[] table) {
        String fileName = "sonde_" + nom.replaceAll("[^a-zA-Z0-9]", "_") + ".csv";
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("index,valeur");
            for (int i = 0; i < table.length; i++) {
                writer.println(i + "," + (table[i] ? 1 : 0));
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'export CSV de " + nom + ": " + e.getMessage());
        }
    }
}
