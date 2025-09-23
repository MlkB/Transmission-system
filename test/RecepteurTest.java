package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import information.Information;
import transmetteurs.Recepteur;

/**
 * classe de test permettant de tester le fonctionnement du récepteur
 */
public class RecepteurTest {

    /**
     * teste la bonne conversion du numérique vers l'analogique
     * @throws Exception
     */
    @Test
    void testConversionAnalogToLogic() throws Exception {
        Float[] signal = {1.0f, 1.0f, 0.0f, 0.0f, 0.6f, 0.6f};
        Information<Float> analogInfo = new Information<>(signal);

        Recepteur recepteur = new Recepteur(2, 0.5f;"NRZ");
        recepteur.recevoir(analogInfo);

        Information<Boolean> logicInfo = recepteur.getInformationEmise();

        assertEquals(3, logicInfo.nbElements());
        assertTrue(logicInfo.iemeElement(0));
        assertFalse(logicInfo.iemeElement(1));
        assertTrue(logicInfo.iemeElement(2));
    }

    /**
     * teste la bonne génération d'erreur si le bombre d'écho est en dessous de zéro
     */
    @Test
    void testIllegalNbEch() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Recepteur(0);
        });

        assertEquals("nbEch doit être > 0", exception.getMessage());
    }
}
