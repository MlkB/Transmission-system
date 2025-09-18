package application;

import information.Information;
import sources.SourceFixe;            // <- si tu veux l’affichage de comparaison
import emmetteurs.Emetteur;
import transmetteurs.TransmetteurParfait;
import transmetteurs.Recepteur;
import destinations.DestinationFinale;

public class TestEmetteur {
    public static void main(String[] args) throws Exception {

        // 1) Données de test
        Boolean[] bits = { true, false, true, true, false, false, true };
        Information<Boolean> infoBits = new Information<>(bits);

        // 2) Chaîne : Emetteur(Boolean->Float) -> Lien parfait -> Recepteur(Float->Boolean) -> Destination
        Emetteur<Boolean> emetteur = new Emetteur<>("NRZ");
        TransmetteurParfait<Float> lien = new TransmetteurParfait<>();

        Recepteur recepteur = new Recepteur(1, 0f);       // 1 échantillon/bit, seuil 0f
        DestinationFinale dest = new DestinationFinale();

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
