package gov.bnl.channelfinder.api;

import static gov.bnl.channelfinder.api.ChannelFinderClient.*;
import static gov.bnl.channelfinder.api.Channel.Builder.channel;
import static gov.bnl.channelfinder.api.Property.Builder.property;
import static gov.bnl.channelfinder.api.Tag.Builder.tag;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.core.util.MultivaluedMapImpl;

//multivalue map

public class QueryTest {

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

		ChannelFinderClient.resetPreferences();
		client = CFCBuilder.serviceURL().withHTTPAuthentication(true).create();
		try {
			initialChannelCount = client.getAllChannels().size();
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
	 * check if all channels are returned
	 */
	@Test
	public void queryAllChannels() {
		Map<String, String> map = new Hashtable<String, String>();
		map.put("~name", "*");
		Collection<Channel> channels = client.find(map);
		assertTrue(client.getAllChannels().size() == channels.size());
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
		assertTrue(client.find(map).size() == 4);
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
		client.deleteProperty(prop2.toXml().getName());
		client.deleteTag(tagA.toXml().getName());
		client.deleteTag(tagB.toXml().getName());
		client.deleteTag(tagC.toXml().getName());
		client.deleteTag(tagStar.toXml().getName());
		assertTrue(client.getAllChannels().size() == initialChannelCount);
	}
}