package gov.bnl.channelfinder.api;

import static org.junit.Assert.*;

import org.junit.Test;

public class QueryParserTest {


	@Test(expected = IllegalArgumentException.class)
	public void testInvalidQueryString()
	{
		String query = "pvk* prop= ";
		ChannelFinderClientImpl.buildSearchMap(query);
	}
}
