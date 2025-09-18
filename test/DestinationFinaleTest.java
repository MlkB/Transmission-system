package test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Arrays;

import destinations.DestinationFinale;
import information.Information;

public class DestinationFinaleTest {

    @Test
    public void testReception() throws Exception {
        DestinationFinale dest = new DestinationFinale();  // PAS de <Boolean>
        Information<Boolean> info = new Information<>(new Boolean[] {true, false, true});
        dest.recevoir(info);
        assertEquals(info, dest.getInformation());  // PAS getInformationRecue()
    }
}
