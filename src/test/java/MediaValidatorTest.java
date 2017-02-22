/**
 * 
 */
package test.java;

import static org.junit.Assert.*;

import java.util.List;
import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import com.comscore.dataaccess.MediaQueries;
import com.comscore.fileutils.ImportFiles;
import com.comscore.main.MediaAccess;
import com.comscore.main.Constants.StringConstants;
import com.comscore.media.Duration;
import com.comscore.media.Media;

/**
 * @author gregb
 * 
 * Note - Incomplete test suite, but provides some useful utilities
 *
 */
public class MediaValidatorTest {

	/**
	 * Test method for Media Object
	 */
	@Test
	public void testValidateMedia() {
	Media m = new Media.Builder("NX-01", "Vengeance",
			"2063-04-05").provider("Edith Keeler")
			.revenue((float)953.70).duration(new Duration(552,48,0))
			.build();
		// could also use assertEquals on the 'True' ones but there doesn't appear to be an 'assertNotEquals'
		assertTrue(m.getStb().equals("NX-01"));
		assertFalse(m.getStb().equals("Millenium Falcon"));
		assertTrue(m.getTitle().equals("Vengeance"));
		assertFalse(m.getTitle().equals("Corellian Cruiser"));
		assertTrue(m.getProvider().equals("Edith Keeler"));
		assertFalse(m.getProvider().equals("Momaw Nadon"));
		assertTrue(m.getLeaseDate().equals("2063-04-05"));
		assertFalse(m.getLeaseDate().equals("19 BBY"));;
		assertTrue(m.getRevenue() == (float)953.70);
		assertFalse(m.getRevenue() == (float)14368089.43);
		assertTrue(m.getViewTime().toTimeString().equals("552:48:00"));
		assertFalse(m.getViewTime().toTimeString().equals("00:15:00"));

	}
	
	public void importFiles() {
		ImportFiles imp = new ImportFiles("data/");
		try {
			// get list of map between filenames and media objects
			Map<String,List<Media>> importMedia = imp.importFile("data-input/stb_data.txt");

			for (String filename : importMedia.keySet()) {
				imp.writeMedia(filename, importMedia.get(filename));
			} 
			importMedia = imp.importFile("data-input/stb_data2.txt");

			for (String filename : importMedia.keySet()) {
				imp.writeMedia(filename, importMedia.get(filename));
			} 
		} catch (IOException ioe) {
			System.out.println("JUnit: Unable to load file");
			return;
		}
	}
	
	/*
	 * NOTE - assumes you have these files in 
	 */
	@Test
	public void testFileUtils() {
		ImportFiles imp = new ImportFiles("data/");
		try {
			Map<String,String> stbFiles = imp.getAllFilenames();

			for (String s : stbFiles.keySet()) {
				//System.out.println(s + " :: " + stbFiles.get(s));
				assertTrue(stbFiles.get(s).endsWith(StringConstants.DATASTORE_EXTENSION.value()));
			}
			
			Map<String,List<Media>> newMedia = imp.importFile("data-input/stb_data.txt");
			assertEquals(1,newMedia.get("data/stb2" + StringConstants.DATASTORE_EXTENSION.value()).size());
			assertEquals("stb3", newMedia.get("data/stb3" + StringConstants.DATASTORE_EXTENSION.value()).get(0).getStb());
			
			newMedia = imp.importFile("data-input/stb_data2.txt");
			assertEquals(3,newMedia.get("data/stb4" + StringConstants.DATASTORE_EXTENSION.value()).size());
			assertEquals("stb5", newMedia.get("data/stb5" + StringConstants.DATASTORE_EXTENSION.value()).get(0).getStb());
		} catch (IOException ioe) {
			ioe.printStackTrace();
			assertTrue(false);
		}	
		
	}
	
	@Test
	public void testFilters() {
		importFiles();  //make sure everything is loaded
		ImportFiles imp = new ImportFiles("data/");
		List<Media> importMedia = imp.importJson("stb4-STB.json");
		
		MediaQueries mq = new MediaQueries();
		List<Media> filteredMedia = mq.filter(importMedia, "DATE=2014-04-03");
		assertEquals(9.0d, filteredMedia.get(0).getRevenue(),0.001d);
		assertEquals(7500, filteredMedia.get(0).getViewTime().toSeconds());
		
		filteredMedia = mq.filter(importMedia, "TITLE=the hobbit");
		assertEquals(99.0d, filteredMedia.get(0).getRevenue(),0.001d);
		assertEquals("2014-04-02", filteredMedia.get(0).getLeaseDate());
	}
	
	@Test
	public void testOrdering() {
		ImportFiles imp = new ImportFiles("data/");
		List<Media> importMedia = imp.importJson("stb4-STB.json");
		
		MediaQueries mq = new MediaQueries();
		List<Media> orderedMedia = mq.order(importMedia, "DATE");
		assertEquals("the matrix", orderedMedia.get(0).getTitle());
		assertEquals("the hobbit", orderedMedia.get(1).getTitle());
		assertEquals("unbreakable", orderedMedia.get(2).getTitle());
		
	}
	
	/*
	 * This is just for visual inspection of STDOUT.  -s is simply a print function
	 * applied to STDOUT
	 */
	@Test
	public void testWholeProgram() {
		String[] test1 = {"-d", "data/", "-l", "data-input/stb_data.txt"};
		String[] test2 = {"-d", "data/", "-l", "data-input/stb_data2.txt"};
		String[] test3 = {"-d", "data/", "-f", "STB=stb1"};
		String[] test4 = {"-d", "data/", "-f", "DATE=2014-04-01"};
		String[] test5 = {"-d", "data/", "-f", "STB=stb4,DATE=2014-04-01"};
		String[] test6 = {"-d", "data/", "-f", "STB=stb1", "-o", "DATE"};
		String[] test7 = {"-d", "data/", "-f", "STB=stb4", "-o", "REV,DATE"};
		String[] test8 = {"-d", "data/", "-s", "DATE,STB,REV"};
		String[] test9 = {"-d", "data/", "-f", "STB=stb4", "-s", "DATE,STB,REV", "-o", "REV,DATE"};
		//String[] test1 = {"-d", "data/", "-l", "stb_data.txt"};
		//String[] test1 = {"-d", "data/", "-l", "stb_data.txt"};
		//String[] test1 = {"-d", "data/", "-l", "stb_data.txt"};
		MediaAccess.main(test1);
		System.out.println("------------------------");
		MediaAccess.main(test2);
		System.out.println("------------------------");
		MediaAccess.main(test3);
		System.out.println("------------------------");
		MediaAccess.main(test4);
		System.out.println("------------------------");
		MediaAccess.main(test5);
		System.out.println("------------------------");
		MediaAccess.main(test6);
		System.out.println("------------------------");
		MediaAccess.main(test7);
		System.out.println("------------------------");
		MediaAccess.main(test8);
		System.out.println("------------------------");
		MediaAccess.main(test9);
		System.out.println("------------------------");
	}

}
