package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import sources.SourceFixe;
import information.Information;

import java.util.Arrays;

/**
 * classe de test permettant de tester le fonctionnement de la source fixe
 */
public class SourceFixeTest {

    /**
     * teste la bonne génération du message
     * @throws Exception
     */
    @Test
    void testMessageGeneration() throws Exception {
    	Information<Boolean> message = new Information<>(new Boolean[] {
    		    true, false, true, false, true
    		});
        SourceFixe source = new SourceFixe(message);
        source.emettre();

        Information<Boolean> info = source.getInformationEmise();
        assertEquals(5, info.nbElements());
        assertTrue(info.iemeElement(0));  // true
        assertFalse(info.iemeElement(1)); // false
        assertTrue(info.iemeElement(2));  // true
        assertFalse(info.iemeElement(3)); // false
        assertTrue(info.iemeElement(4));  // true
    }

    /**
     * teste la génération d'une erreur si le message est invalide
     */
    @Test
    void testInvalidMessage() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new SourceFixe(null);
        });

        assertEquals("Message non binaire (0 ou 1 uniquement)", exception.getMessage());
    }
}
