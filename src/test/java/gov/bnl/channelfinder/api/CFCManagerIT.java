/**
 * Copyright (C) 2010-2012 Brookhaven National Laboratory
 * Copyright (C) 2010-2012 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * All rights reserved. Use is subject to license terms.
 */
/**
 * 
 */
package gov.bnl.channelfinder.api;

import gov.bnl.channelfinder.api.ChannelFinderClientImpl.CFCBuilder;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author shroffk
 * 
 */
public class CFCManagerIT {
	
	private static Logger logger = Logger.getLogger(CFCManagerIT.class.getName()); 

	@BeforeClass
	public static void beforeTests() {
	}

	@AfterClass
	public static void afterTests() {
	}

	@Test
	public void simpleReadConnection() {
		ChannelFinderClient cfc;
		cfc = CFCBuilder.serviceURL("http://localhost:8080/ChannelFinder")
				.create();
		cfc.getAllTags();
		Logger.getLogger(RawLoggingFilter.class.getName()).setLevel(Level.ALL);
		cfc.findByName("_*");
		Logger.getLogger(RawLoggingFilter.class.getName()).setLevel(Level.OFF);
	}

}
