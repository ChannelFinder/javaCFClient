package gov.bnl.channelfinder.api;

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
	private static int channelcount;

	/**
	 * insert test data - for performing the queries described below.
	 */
	@BeforeClass
	public static void populateChannels() {
		channels.add(channel("pvk:01<first>").owner("shroffk").with(
				property("prop", "1").owner("shroffk")).with(
				property("prop2", "2").owner("shroffk")).with(
				tag("Taga", "shroffk")));
		channels.add(channel("pvk:02<second>").owner("shroffk").with(
				property("prop", "1").owner("shroffk")).with(
				tag("Taga", "shroffk")).with(tag("Tagb", "shroffk")));
		channels.add(channel("pvk:03<second>").owner("shroffk").with(
				property("prop", "2").owner("shroffk")).with(tag("Tagb", "shroffk")).with(
				tag("Tagc", "shroffk")));
		channels.add(channel("distinctName").owner("shroffk").with(
				property("prop", "*").owner("shroffk")).with(
				tag("Tag*", "shroffk")));
		try {
			channelcount = ChannelFinderClient.getInstance().getAllChannels()
					.size();
			ChannelFinderClient.getInstance().add(channels);		
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
		Collection<Channel> channels = ChannelFinderClient.getInstance().findChannels(
				map);
		assertTrue(ChannelFinderClient.getInstance().getAllChannels().size() == channels
				.size());
	}

	/**
	 * 
	 */
	@Test
	public void queryChannels() {
		Map<String, String> map = new Hashtable<String, String>();
		map.put("~name", "pvk:*");
		Collection<Channel> channels = ChannelFinderClient.getInstance().findChannels(
				map);
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
		Collection<Channel> channels = ChannelFinderClient.getInstance().findChannels(
				map);
		assertTrue(channels.size() == 2);

		map.put("prop", "1");
		map.put("prop2", "2");
		channels = ChannelFinderClient.getInstance().findChannels(map);
		assertTrue(channels.size() == 1);

		map.clear();
		map.put("cell", "14");
		channels = ChannelFinderClient.getInstance().findChannels(map);
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
		Collection<Channel> channels = ChannelFinderClient.getInstance().findChannels(
				map);
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
		assertTrue(ChannelFinderClient.getInstance().findChannels(map)
				.size() == 4);
		map.clear();
		map.add("prop", "\\*");
		assertTrue(ChannelFinderClient.getInstance().findChannels(map)
				.size() == 1);
		// tag names are special chars
		map.clear();
		map.add("~tag", "Tag*");
		assertTrue(ChannelFinderClient.getInstance().findChannels(map)
				.size() == 4);
		map.clear();
		map.add("~tag", "Tag\\*");
		assertTrue(ChannelFinderClient.getInstance().findChannels(map)
				.size() == 1);
	}

	@AfterClass
	public static void cleanup() {
		ChannelFinderClient.getInstance().remove(channels);
		assertTrue(ChannelFinderClient.getInstance().getAllChannels().size() == channelcount);
	}
}