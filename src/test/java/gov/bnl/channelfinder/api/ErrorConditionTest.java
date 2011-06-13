package gov.bnl.channelfinder.api;

import static gov.bnl.channelfinder.api.Channel.Builder.channel;
import static gov.bnl.channelfinder.api.Tag.Builder.tag;
import static gov.bnl.channelfinder.api.Property.Builder.property;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Before;
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
	public static void setup(){
		ChannelFinderClient.resetPreferences();
		client = ChannelFinderClient.getInstance();
	}
	
	
	@Test(expected = ChannelFinderException.class)
	public void addOrphanChannel() {
		XmlChannel xmlChannel = new XmlChannel();
		xmlChannel.setName("onlyName");
		client.add(channel("JustName"));
	}

	@Test(expected = ChannelFinderException.class)
	public void addOrphanTag() {
		client.add(tag("JustName"));
	}

	@Test(expected = ChannelFinderException.class)
	public void addOrphanProperty() {
		client.add(property("JustName"));
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
			ChannelFinderClient.getInstance().add(channels);
			assertTrue(false);
		} catch (ChannelFinderException e) {
			assertTrue(true);
		}

		// channel ch2 has name but no owner
		channels.clear();
		channels.add(channel("name1").owner("owner"));
		channels.add(channel("name2"));
		try {
			ChannelFinderClient.getInstance().add(channels);
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
			ChannelFinderClient.getInstance().remove(
					channel("NonExistantChannel"));
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
			ChannelFinderClient.getInstance().updateChannel(
					channel("NonExistantChannel"));
			assertTrue(false);
		} catch (ChannelFinderException e) {
			assertTrue(true);
		}
	}

	@Test
	public void addTag2NonExistentChannel() {
		try {
			ChannelFinderClient.getInstance().set(tag("sometag", "boss"),
					"NonExistantChannel");
			assertTrue(false);
		} catch (ChannelFinderException e) {
			assertTrue(true);
		}
	}

	public void addProperty2NonExistentChannel() {

	}

}
