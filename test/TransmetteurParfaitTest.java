package test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import information.Information;
import transmetteurs.TransmetteurParfait;

public class TransmetteurParfaitTest {

    @Test
    public void testTransmissionIdentique() throws Exception {
        TransmetteurParfait<Float> tp = new TransmetteurParfait<>();
        Information<Float> info = new Information<>(new Float[] {0.1f, 0.5f, 0.9f});
        tp.recevoir(info);
        tp.emettre();
        assertEquals(info, tp.getInformationAnalogEmise());
    }
}
