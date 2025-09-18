package test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import sources.SourceAleatoire;
import information.Information;
import information.InformationNonConformeException;

public class SourceAleatoireTest {

    @Test
    void testDefaultLength() throws InformationNonConformeException {
        SourceAleatoire source = new SourceAleatoire();
        source.generer();
        source.emettre();
        Information<Boolean> info = source.getInformationEmise();

        assertNotNull(info);
        assertEquals(100, info.nbElements()); 
    }

    @Test
    void testCustomLength() throws InformationNonConformeException {
        SourceAleatoire source = new SourceAleatoire();
        source.setLength(42);
        source.generer();
        source.emettre();
        Information<Boolean> info = source.getInformationEmise();

        assertNotNull(info);
        assertEquals(42, info.nbElements());
    }

    @Test
    void testWithSeed() throws InformationNonConformeException {
        SourceAleatoire source1 = new SourceAleatoire();
        source1.setSeed(1234);
        source1.setLength(10);
        source1.generer();
        source1.emettre();

        SourceAleatoire source2 = new SourceAleatoire();
        source2.setSeed(1234);
        source2.setLength(10);
        source2.generer();
        source2.emettre();

        assertEquals(source1.getInformationEmise(), source2.getInformationEmise());
    }

    @Test
    void testOnlyBooleansGenerated() throws InformationNonConformeException {
        SourceAleatoire source = new SourceAleatoire();
        source.setLength(50);
        source.generer();
        source.emettre();
        Information<Boolean> info = source.getInformationEmise();

        assertNotNull(info);
        for (int i = 0; i < info.nbElements(); i++) {
            Boolean bit = info.iemeElement(i);
            assertTrue(bit == Boolean.TRUE || bit == Boolean.FALSE);
        }
    }
}
