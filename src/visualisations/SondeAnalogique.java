package visualisations;

import java.util.Iterator;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

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
        // Ne plus créer de fenêtre Java, tout est affiché en Python maintenant
        // if (vueCourbe == null) {
        //     vueCourbe = new VueCourbe (table, nom);
        // }

        // Exporter les données vers CSV pour affichage Python
        exportToCSV(table);
    }

    /**
     * Exporte les données vers un fichier CSV
     * @param table tableau de valeurs à exporter
     */
    private void exportToCSV(float[] table) {
        String fileName = "sonde_" + nom.replaceAll("[^a-zA-Z0-9]", "_") + ".csv";
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("index,valeur");
            for (int i = 0; i < table.length; i++) {
                writer.println(i + "," + table[i]);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'export CSV de " + nom + ": " + e.getMessage());
        }
    }
}
