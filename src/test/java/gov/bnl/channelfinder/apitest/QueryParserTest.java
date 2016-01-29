/**
 * Copyright (C) 2010-2012 Brookhaven National Laboratory
 * Copyright (C) 2010-2012 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * All rights reserved. Use is subject to license terms.
 */
package gov.bnl.channelfinder.apitest;

import static org.junit.Assert.*;
import gov.bnl.channelfinder.api.ChannelFinderClientImpl;

import org.junit.Test;

public class QueryParserTest {


	@Test(expected = IllegalArgumentException.class)
	public void testInvalidQueryString()
	{
		String query = "pvk* prop= ";
		ChannelFinderClientImpl.buildSearchMap(query);
	}
}
