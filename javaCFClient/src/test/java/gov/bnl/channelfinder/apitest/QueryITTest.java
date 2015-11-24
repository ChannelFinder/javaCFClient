/**
 * Copyright (C) 2010-2012 Brookhaven National Laboratory
 * Copyright (C) 2010-2012 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * All rights reserved. Use is subject to license terms.
 */
package gov.bnl.channelfinder.apitest;

import static gov.bnl.channelfinder.api.ChannelFinderClientImpl.*;
import static gov.bnl.channelfinder.api.Channel.Builder.channel;
import static gov.bnl.channelfinder.api.Property.Builder.property;
import static gov.bnl.channelfinder.api.Tag.Builder.tag;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import gov.bnl.channelfinder.api.Channel;
import gov.bnl.channelfinder.api.ChannelFinderClient;
import gov.bnl.channelfinder.api.ChannelFinderClientImpl;
import gov.bnl.channelfinder.api.ChannelFinderException;
import gov.bnl.channelfinder.api.Property;
import gov.bnl.channelfinder.api.Tag;
import gov.bnl.channelfinder.api.ChannelFinderClientImpl.CFCBuilder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.core.util.MultivaluedMapImpl;

//multivalue map

public class QueryITTest {

	private static Collection<Channel.Builder> channels = new HashSet<Channel.Builder>();
	private static int initialChannelCount;

	private static ChannelFinderClient client;

	// Tags
	static Tag.Builder tagA = tag("Taga").owner("channel");
	static Tag.Builder tagB = tag("Tagb").owner("channel");
	static Tag.Builder tagC = tag("Tagc").owner("channel");
	static Tag.Builder tagStar = tag("Tag*").owner("channel");
	// Properties
	static Property.Builder prop = property("prop").owner("channel");
	static Property.Builder prop2 = property("prop2").owner("channel");

	/**
	 * insert test data - for performing the queries described below.
	 */
	@BeforeClass
	public static void populateChannels() {

		ChannelFinderClientImpl.resetPreferences();
		try {
			client = CFCBuilder.serviceURL("https://192.168.122.242:8181/ChannelFinder").withHTTPAuthentication(true).username("admin").password("1234").create();
			initialChannelCount = client.findByName("*").size();
			// Add the tags and properties.
			client.set(prop);
			client.set(prop2);
			client.set(tagA);
			client.set(tagB);
			client.set(tagC);
			client.set(tagStar);

			// Add the channels
			client.set(channel("pvk:01<first>").owner("channel")
					.with(prop.value("1")).with(prop2.value("2")).with(tagA));
			client.set(channel("pvk:02<second>").owner("channel")
					.with(prop.value("1")).with(tagA).with(tagB));
			client.set(channel("pvk:03<second>").owner("channel")
					.with(prop.value("2")).with(tagB).with(tagC));
			client.set(channel("distinctName").owner("channel")
					.with(prop.value("*")).with(tagStar));
		} catch (ChannelFinderException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * search by name
	 */
	@Test
	public void findbyName() {
		Collection<Channel> channels = client.findByName("pvk:0?<*");
		assertTrue("failed to find channels based on name expect 3 found "
				+ channels.size(), channels.size() == 3);

		channels = client.findByName("pvk:01<first>|pvk:02<second>");
		assertTrue(
				"failed to find channels on ',' seperated name pattern, expected 2 found "
						+ channels.size(), channels.size() == 2);
	}

	/**
	 * search by tag
	 */
	@Test
	public void findbyTag() {
		Collection<Channel> channels = client.findByTag("Taga");
		assertTrue("failed to find channels based on name expect 2 found "
				+ channels.size(), channels.size() == 2);

		channels = client.findByTag("Taga, Tagb");
		assertTrue(
				"failed to find channels on ',' seperated name pattern, expected 1 found "
						+ channels.size(), channels.size() == 1);
	}

	/**
	 * search by property
	 */
	@Test
	public void findbyProperty() {
		Collection<Channel> channels = client.findByProperty("prop", "2");
		assertTrue("failed to find channels based on name expect 1 found "
				+ channels.size(), channels.size() == 1);

		channels = client.findByProperty("prop", "1", "2");
		assertTrue("failed to find channels based on name expect 3 found "
				+ channels.size(), channels.size() == 3);

		channels = client.findByProperty("prop", "1, 2");
		assertTrue("failed to find channels based on name expect 3 found "
				+ channels.size(), channels.size() == 3);
	}

	/**
	 * check if all channels are returned
	 */
	@Test
	public void queryAllChannels() {
		Map<String, String> map = new Hashtable<String, String>();
		map.put("~name", "*");
		Collection<Channel> channels = client.find(map);
		assertTrue(client.findByName("*").size() == channels.size());
	}

	/**
	 * 
	 */
	@Test
	public void queryChannels() {
		Map<String, String> map = new Hashtable<String, String>();
		map.put("~name", "pvk:*");
		Collection<Channel> channels = client.find(map);
		assertTrue(channels.size() == 3);
	}

	/**
	 * When multiple properties are queried, the result is a logical AND of all
	 * the query conditions
	 */
	@Test
	public void queryChannelsbyProperty() {
		Map<String, String> map = new Hashtable<String, String>();
		map.put("prop", "1");
		Collection<Channel> channels = client.find(map);
		assertTrue(channels.size() == 2);

		map.put("prop", "1");
		map.put("prop2", "2");
		channels = client.find(map);
		assertTrue(channels.size() == 1);

		map.clear();
		map.put("cell", "14");
		channels = client.find(map);
	}

	/**
	 * When you have multiple value for same property results in the values
	 * being OR'ed
	 */
	@Test
	public void testMultipleParameters() {
		MultivaluedMapImpl map = new MultivaluedMapImpl();
		map.add("prop", "1");
		map.add("prop", "2");
		Collection<Channel> channels = client.find(map);
		assertTrue(channels.size() == 3);
	}

	@Test
	public void testQueryString() {
		String query = "pvk:*";
		assertTrue("Failed to query for pvk:* - expect 3 found " + client.find(query).size(), client.find(query).size() == 3);
		query = "* prop=1,2";
		assertTrue(
				"Failed to query using name and property, expected: 3 found: "
						+ client.find(query).size(),
				client.find(query).size() == 3);
		query = "pvk* prop=1,2 prop2=*";
		assertTrue(
				"Failed to query using name and multiple properties, expected: 1 found: "
						+ client.find(query).size(),
				client.find(query).size() == 1);
		query = "pvk* prop=1,2 Tags=Taga,Tagb";
		assertTrue(
				"Failed to query using name and property and tag, expected: 1 found: "
						+ client.find(query).size(),
				client.find(query).size() == 1);
		query = "pvk* prop=1, 2 Tags=Taga, Tagb";
		 assertTrue(
				"Failed to query using name and property and tag with spaces, expected: 1 found: "
						+ client.find(query).size(),
				client.find(query).size() == 1);
		 
		 query = "*first*|*second*";
		 Collection<Channel> test = client.find(query);
		 int i = test.size();
		 assertTrue(
				"Failed to query using name, expected: 3 found: "
						+ client.find(query).size(),
				client.find(query).size() == 3);

	}

	/**
	 * Testing for the use of special chars.
	 */
	@Test
	public void testQueryForSpecialChar() {
		MultivaluedMapImpl map = new MultivaluedMapImpl();
		// property values are special chars
		map.add("prop", "*");
		assertTrue(client.find(map).size() == 4);
		map.clear();
		map.add("prop", "\\*");
		assertTrue(client.find(map).size() == 1);
		// tag names are special chars
		map.clear();
		map.add("~tag", "Tag*");
		Collection<Channel> result = client.find(map);
		assertTrue("Expect 4 but found " + result.size(), result.size() == 4);
		map.clear();
		map.add("~tag", "Tag\\*");
		assertTrue(client.find(map).size() == 1);
	}

	@AfterClass
	public static void cleanup() {

		channels.add(channel("pvk:01<first>"));
		channels.add(channel("pvk:02<second>"));
		channels.add(channel("pvk:03<second>"));
		channels.add(channel("distinctName"));

		client.delete(channels);
		// clean up all the tags and properties
		client.deleteProperty(prop.build().getName());
		client.deleteProperty(prop2.toJSON().getName());
		client.deleteTag(tagA.toJSON().getName());
		client.deleteTag(tagB.toJSON().getName());
		client.deleteTag(tagC.toJSON().getName());
		client.deleteTag(tagStar.toJSON().getName());
		int finalChannelCount = client.findByName("*").size();
		assertTrue("Failed clean up expected " + initialChannelCount
				+ " channels found " + finalChannelCount,
				finalChannelCount == initialChannelCount);
	}
}
