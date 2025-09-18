package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import information.Information;
import transmetteurs.Recepteur;

public class RecepteurTest {

    @Test
    void testConversionAnalogToLogic() throws Exception {
        Float[] signal = {1.0f, 1.0f, 0.0f, 0.0f, 0.6f, 0.6f};
        Information<Float> analogInfo = new Information<>(signal);

        Recepteur recepteur = new Recepteur(2, 0.5f);
        recepteur.recevoir(analogInfo);

        Information<Boolean> logicInfo = recepteur.getInformationEmise();

        assertEquals(3, logicInfo.nbElements());
        assertTrue(logicInfo.iemeElement(0));
        assertFalse(logicInfo.iemeElement(1));
        assertTrue(logicInfo.iemeElement(2));
    }

    @Test
    void testIllegalNbEch() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Recepteur(0);
        });

        assertEquals("nbEch doit être > 0", exception.getMessage());
    }
}
