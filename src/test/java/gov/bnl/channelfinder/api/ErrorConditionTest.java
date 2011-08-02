package gov.bnl.channelfinder.api;

import static gov.bnl.channelfinder.api.Channel.Builder.channel;
import static gov.bnl.channelfinder.api.Property.Builder.property;
import static gov.bnl.channelfinder.api.Tag.Builder.tag;
import static org.junit.Assert.assertTrue;
import gov.bnl.channelfinder.api.ChannelFinderClient.CFCBuilder;

import java.util.Collection;
import java.util.HashSet;

import org.junit.BeforeClass;
import org.junit.Test;

public class ErrorConditionTest {

	// TODO
	// test check if error with the correct messages are thrown.

	// @Test
	// public void addEmptyChannel() {
	// try {
	// ChannelFinderClient.getInstance().addChannel(channel(new XmlChannel()));
	// fail("Added an empty channel.");
	// } catch (ChannelFinderException e) {
	// assertTrue(true);
	// }
	//
	// }

	private static ChannelFinderClient client;

	@BeforeClass
	public static void setup() {
		ChannelFinderClient.resetPreferences();
		client = CFCBuilder.toDefault().withHTTPAuthentication(true).create();
	}

	@Test(expected = ChannelFinderException.class)
	public void addOrphanChannel() {
		XmlChannel xmlChannel = new XmlChannel();
		xmlChannel.setName("onlyName");
		client.set(channel("JustName"));
	}

	@Test(expected = ChannelFinderException.class)
	public void addOrphanTag() {
		client.set(tag("JustName"));
	}

	@Test(expected = ChannelFinderException.class)
	public void addOrphanProperty() {
		client.set(property("JustName"));
	}

	/**
	 * Add a set of channels with one incorrect channel
	 */
	@Test
	public void addSetwithBadChannel() {
		Collection<Channel.Builder> channels = new HashSet<Channel.Builder>();
		channels.add(channel("name1").owner("owner"));
		channels.add(channel(""));
		// channel ch2 has empty name
		try {
			client.set(channels);
			assertTrue(false);
		} catch (ChannelFinderException e) {
			assertTrue(true);
		}

		// channel ch2 has name but no owner
		channels.clear();
		channels.add(channel("name1").owner("owner"));
		channels.add(channel("name2"));
		try {
			client.set(channels);
			assertTrue(false);
		} catch (ChannelFinderException e) {
			assertTrue(true);
		}
	}

	/**
	 * Try to delete a non-existent channel
	 */
	@Test
	public void removeNonExistentChannel() {
		try {
			client.delete(channel("NonExistantChannel"));
			assertTrue(false);
		} catch (ChannelFinderException e) {
			assertTrue(true);
		}
	}

	public void removeSetWithNonExistentChannel() {

	}

	/**
	 * Test detection of error condition - attempting to update a channel that
	 * does not exist
	 */
	@Test
	public void updateNonExistentChannel() {
		try {
			client.update(channel("NonExistantChannel"));
			assertTrue(false);
		} catch (ChannelFinderException e) {
			assertTrue(true);
		}
	}

	@Test
	public void addTag2NonExistentChannel() {
		try {
			client.set(tag("sometag", "boss"), "NonExistantChannel");
			assertTrue(false);
		} catch (ChannelFinderException e) {
			assertTrue(true);
		}
	}

	public void addProperty2NonExistentChannel() {

	}

}
