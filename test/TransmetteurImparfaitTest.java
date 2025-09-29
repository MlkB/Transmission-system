package test;

import static org.junit.jupiter.api.Assertions.*;

import information.Information;
import transmetteurs.TransmetteurParfait;
import java.util.Arrays;
import org.junit.Test;




public class TransmetteurImparfaitTest {
    @Test
    public void testCalculPuissanceSignal() throws Exception {
        TransmetteurImparfait<Float> ti = new TransmetteurImparfait<>(3, 10.0f);
        Information<Float> info = new Information<>(new Float[] {1.0f, -1.0f, 1.0f});
        ti.recevoir(info);
        ti.calculPuissanceSignal();
        assertEquals(1.0f, ti.puissanceSignal);

    }

    @Test
    public void testCalculVariance() throws Exception {
        TransmetteurImparfait<Float> ti = new TransmetteurImparfait<>(3, 10.0f);
        Information<Float> info = new Information<>(new Float[]{1.0f, -1.0f, 1.0f});
        ti.recevoir(info);

        // méthode publique pour tester la variance
        ti.calculPuissanceSignal();
        ti.calculVariance();
        float bitsignal = ti.puissanceSignal / ti.nEch;
        float expectedVariance = bitsignal / (float) Math.pow(10, ti.SNRdB / 10.0);

        assertEquals(expectedVariance, ti.variance, 1e-6);

    }
    @Test
    public void testGenererBBAG() throws Exception {
        TransmetteurImparfait<Float> ti = new TransmetteurImparfait<>(3, 10.0f, 42); // seed pour reproductibilité
        Information<Float> info = new Information<>(new Float[]{1.0f, -1.0f, 1.0f});
        ti.recevoir(info);
        ti.genererBBAG();
        assertEquals(info.nbElements(), ti.informationEmise.nbElements());
        assertEquals(info.nbElements(), ti.bruit.size());


    }

    @Test
    public void testEmettre() throws Exception {
        TransmetteurImparfait<Float> ti = new TransmetteurImparfait<>(3, 10.0f, 42);
        Information<Float> info = new Information<>(new Float[]{1.0f, -1.0f, 1.0f});
        ti.recevoir(info);

        // Destination de test
        List<Information<Float>> recues = new ArrayList<>();
        DestinationInterface<Float> dest = new DestinationInterface<Float>() {
            @Override
            public void recevoir(Information<Float> information) {
                recues.add(information);
            }
        };

        ti.connecter(dest);
        ti.emettre();
        assertEquals(1, recues.size());
        assertEquals(info.nbElements(), recues.get(0).nbElements());



    }




}
