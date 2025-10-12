package application;

import information.Information;
import sources.SourceFixe;            // <- si tu veux l’affichage de comparaison
import emmetteurs.Emetteur;
import transmetteurs.TransmetteurParfait;
import transmetteurs.Recepteur;
import destinations.DestinationFinale;

/**
 * Classe de test pour vérifier le fonctionnement d'un {@link Emetteur}
 * et sa propagation d'informations à travers une chaîne de transmission
 * simple : Emetteur -> TransmetteurParfait -> Recepteur -> DestinationFinale.
 * <p>
 * Le test crée des données binaires, les envoie via l'émetteur, et
 * compare les informations émises et reçues pour valider la transmission.
 * </p>
 */
public class TestEmetteur {

    /**
     * Constructeur par défaut de la classe de test.
     * <p>
     * Rien à initialiser car toutes les méthodes sont statiques.
     * </p>
     */
    public TestEmetteur() {
        // pas d'initialisation nécessaire
    }

    /**
     * Point d'entrée principal du test.
     * <p>
     * Crée une chaîne de transmission simple et vérifie la propagation des informations.
     * </p>
     *
     * @param args arguments de la ligne de commande (non utilisés)
     * @throws Exception si une erreur survient lors de la réception ou de l'émission des informations
     */
    public static void main(String[] args) throws Exception {

        // 1) Données de test
        Boolean[] bits = { true, false, true, true, false, false, true };
        Information<Boolean> infoBits = new Information<>(bits);

        // 2) Chaîne : Emetteur(Boolean->Float) -> Lien parfait -> Recepteur(Float->Boolean) -> Destination
        Emetteur<Boolean> emetteur = new Emetteur<>("NRZ", 1, -1.0f, 1.0f);  // NRZ avec amplitude -1 à +1
        TransmetteurParfait<Float> lien = new TransmetteurParfait<>();

        Recepteur recepteur = new Recepteur(1, 0f, "NRZ");    // 1 échantillon/bit, seuil 0f, codage NRZ
        DestinationFinale<Boolean> dest = new DestinationFinale<>();

        // Connexions
        
        emetteur.connecter(lien);
        lien.connecter(recepteur);
        recepteur.connecter(dest);


        emetteur.recevoir(infoBits);
        emetteur.emettre();  // propage à lien -> récepteur -> destination

        // 4) Vérif
        System.out.println("Emis : " + infoBits);
        System.out.println("Recu : " + dest.getInformationRecue());
        System.out.println("OK ?  " + infoBits.equals(dest.getInformationRecue()));
    }
}
