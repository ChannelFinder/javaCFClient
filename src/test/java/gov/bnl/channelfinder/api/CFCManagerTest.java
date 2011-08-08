/**
 * 
 */
package gov.bnl.channelfinder.api;

import gov.bnl.channelfinder.api.ChannelFinderClient.CFCBuilder;

import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author shroffk
 * 
 */
public class CFCManagerTest {
	
	private static Logger logger = Logger.getLogger(CFCManagerTest.class.getName()); 

	@BeforeClass
	public static void beforeTests() {
	}

	@AfterClass
	public static void afterTests() {
	}

	@Test
	public void simpleReadConnection() {
		ChannelFinderClient cfc;
		cfc = CFCBuilder.to("http://localhost:8080/ChannelFinder")
				.create();
		cfc.getAllTags();
		cfc.getAllChannels();
		cfc.findByName("_*");
	}

}
