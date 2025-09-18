package emmetteurs;

import static org.junit.Assert.*;



import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import information.Information;
import information.InformationNonConformeException;

/**
 * classe de test de l'élément émetteur
 */
public class EmetteurTest {
	
	// Générer une information de bits avec True , False pour simuler l'inforamtion logique reçue

	/**
	 * génère une information booléenne pour simuler l'information reçue
	 * @param bits
	 * @return
	 */
	   public static Information<Boolean> info(Boolean... bits) {
	        Information<Boolean> i = new Information<>();
	        for (Boolean b : bits) i.add(b);
	        return i;
	    }
	   // Générer une information de valuers min et max de type float pour simuler l'inforamtion analogique après conversion 

	/**
	 * Générer une information de valuers min et max de type float pour simuler l'inforamtion analogique après conversion
	 * @param info
	 * @return
	 */
	    public static float[] toArray(Information<Float> info) {
	        List<Float> L = new ArrayList<>();
	        for (Float f : info) L.add(f);
	        float[] out = new float[L.size()];
	        for (int k = 0; k < L.size(); k++) out[k] = L.get(k);
	        return out;
	    }
	    
	    //Tester la création des émetteurs

	/**
	 *	test si le codage est sensible à la casse ou non
	 * @throws Exception
	 */
	@Test
	public void typeCodageInsensibleALaCasse() throws Exception {
	        Emetteur<Boolean> e1 = new Emetteur<>("nRz", 2);
	        e1.recevoir(info(true, false));
	        assertArrayEquals(new float[]{1f, -1f}, toArray(e1.getInformationEmise()), 1e-6f);

	        Emetteur<Boolean> e2 = new Emetteur<>("RZ", 2);
	        e2.recevoir(info(true, false));
	        assertArrayEquals(new float[]{1f, 0f, -1f, 0f}, toArray(e2.getInformationEmise()), 1e-6f);

	        Emetteur<Boolean> e3 = new Emetteur<>("nrzt", 3);
	        e3.recevoir(info(true, false));
	        assertArrayEquals(new float[]{1f,1f,1f, -1f,-1f,-1f}, toArray(e3.getInformationEmise()), 1e-6f);
	    }

	/**
	 *	teste un codage invalide (une suite de lettres)
	 * @throws InformationNonConformeException
	 */
	@Test(expected = InformationNonConformeException.class)
	public void typeCodageInvalide1() throws InformationNonConformeException  {
	    	
	        Emetteur<Boolean> e1 = new Emetteur<>("XYZ", 2);
	        Information<Boolean> data1 = info(true, false);
	        e1.recevoir(data1);
	        }

	/**
	 * teste un codage invalide (une suite de nombre non binaire)
	 * @throws InformationNonConformeException
	 */
	@Test(expected = InformationNonConformeException.class)
	public void typeCodageInvalide2() throws InformationNonConformeException{
	        Emetteur<Boolean> e2 = new Emetteur<>("123", 2);
	        Information<Boolean> data2 = info(true);
	        e2.recevoir(data2);
	       
	    }

	   

	// Test des types de conversion de l'émetteur

	/**
	 * test la conversion NRZ
	 * @throws InformationNonConformeException
	 */
	@Test 
	public void conversionNRZ()  throws InformationNonConformeException {
	      Emetteur<Boolean> e = new Emetteur<>("NRZ", 9);
	        e.recevoir(info(true, false, true));
	        assertArrayEquals(new float[]{1f, -1f, 1f}, toArray(e.getInformationEmise()), 1e-6f);}

	/**
	 * test la conversion RZ
	 * @throws InformationNonConformeException
	 */
	    @Test
	public void conversionRZ()throws InformationNonConformeException{
	        Emetteur<Boolean> e = new Emetteur<>("RZ", 30); 
	        e.recevoir(info(true, false, true));
	        assertArrayEquals(new float[]{1f,0f, -1f,0f, 1f,0f}, toArray(e.getInformationEmise()), 1e-6f);
	    }

	/**
	 * test la conversion NRZT
	 * @throws InformationNonConformeException
	 */
	    @Test
	public void conversionNRZT() throws InformationNonConformeException {
	        Emetteur<Boolean> e = new Emetteur<>("NRZT", 3);
	        e.recevoir(info(true, false, true));
	        assertArrayEquals(
	            new float[]{1f,1f,1f, -1f,-1f,-1f, 1f,1f,1f},
	            toArray(e.getInformationEmise()),1e-6f);
		
	}
	    //Test pour un nombre d'échantillons invalide

	/**
	 * test pour un nombre d'échantillon invalide
	 * @throws InformationNonConformeException
	 */
	  @Test   
	public void nrztNbEchInvalide() throws InformationNonConformeException {
	        Emetteur<Boolean> e1 = new Emetteur<>("NRZT", -2);
	        e1.recevoir(info(true, false));
	        // Avec le code actuel : 0 échantillons ajoutés ⇒ sortie vide
	        assertEquals("L'information émise par le transmetteur doit etre vide",
	        		0, e1.getInformationEmise().nbElements());
	        
	        Emetteur<Boolean> e2 = new Emetteur<>("NRZT", 0);
	        e2.recevoir(info(true, false));
	        // Avec le code actuel : 0 échantillons ajoutés ⇒ sortie vide
	        assertEquals("L'information émise par le transmetteur doit etre vide",
	        		0, e2.getInformationEmise().nbElements());
	    }
	    
	    //Test en cas de réception d'une info NULL

	/**
	 * test en cas de réception nulle
	 * @throws InformationNonConformeException
	 */
	@Test(expected = InformationNonConformeException.class)
	public void testRecevoir() throws InformationNonConformeException {
		
		  Emetteur<Boolean> e = new Emetteur<>("NRZ", 2);
		  e.recevoir(null);

	}

	

}
