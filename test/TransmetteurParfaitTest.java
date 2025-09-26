package test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import information.Information;
import transmetteurs.TransmetteurParfait;

/**
 * classe de test permettant de tester le fonctionnement dutransmetteur parfait
 */
public class TransmetteurParfaitTest {

    /**
     * vérifie que le transmetteur parfait transmet correctement les informations
     * @throws Exception
     */
    @Test
    public void testTransmissionIdentique() throws Exception {
        TransmetteurParfait<Float> tp = new TransmetteurParfait<>();
        Information<Float> info = new Information<>(new Float[] {0.1f, 0.5f, 0.9f});
        tp.recevoir(info);
        tp.emettre();
        assertEquals(info, tp.getInformationAnalogEmise());
    }
}
