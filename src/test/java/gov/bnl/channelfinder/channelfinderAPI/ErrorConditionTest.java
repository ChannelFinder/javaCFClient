package gov.bnl.channelfinder.channelfinderAPI;

import static org.junit.Assert.*;
import gov.bnl.channelfinder.channelfinderAPI.exceptions.*;
import gov.bnl.channelfinder.model.XmlChannel;
import gov.bnl.channelfinder.model.XmlChannels;
import gov.bnl.channelfinder.model.XmlTag;

import org.junit.Test;

public class ErrorConditionTest {

	// TODO
	// test check if error with the correct messages are thrown.
	
//	@Test
	public void addEmptyChannel() {
		try {
			ChannelFinderClient.getInstance().addChannel(new XmlChannel());
			assertTrue(false);
		} catch (ChannelFinderException e) {
			assertTrue(true);
		}

	}
	
	@Test (expected=ChannelFinderException.class)
	public void addOrphanChannel(){
		XmlChannel xmlChannel = new XmlChannel();
		xmlChannel.setName("onlyName");
		ChannelFinderClient.getInstance().addChannel(xmlChannel );
	}
	
	/**
	 * Add a set of channels with one incorrect channel
	 */
	@Test
	public void addSetwithBadChannel() {
		XmlChannel ch1 = new XmlChannel("name1","owner");
		XmlChannel ch2 = new XmlChannel();
		XmlChannels chs = new XmlChannels();
		chs.addChannel(ch1);
		chs.addChannel(ch2);
		
		// channel ch2 has no name
		try {
			ChannelFinderClient.getInstance().addChannels(chs);
			assertTrue(false);
		} catch (ChannelFinderException e) {
//			e.printStackTrace();
			assertTrue(true);
		}
		
		// channel ch2 has name but no owner
		chs.getChannels().remove(ch2);
		ch2.setName("name2");
		chs.addChannel(ch2);
		try {
			ChannelFinderClient.getInstance().addChannels(chs);
			assertTrue(false);
		} catch (ChannelFinderException e) {
//			e.printStackTrace();
			assertTrue(true);
		}
	}

	/**
	 * Try to delete a non-existent channel
	 */
	@Test
	public void removeNonExistentChannel() {
		try {
			XmlChannel channel = new XmlChannel("someNewChannel", "owner");
			ChannelFinderClient.getInstance().removeChannel(channel.getName());
			assertTrue(false);
		} catch (ChannelFinderException e) {
			assertTrue(true);
//			e.printStackTrace();
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
			XmlChannel channel = new XmlChannel("NewChannel", "owner");
			ChannelFinderClient.getInstance().updateChannel(channel);
			assertTrue(false);
		} catch (ChannelFinderException e) {
			assertTrue(true);
		}
	}

	@Test
	public void addTag2NonExistentChannel() {
		try {
			XmlTag tag = new XmlTag("sometag", "boss");
			ChannelFinderClient.getInstance().setTag("someChannel", tag);
			assertTrue(false);
		} catch (ChannelFinderException e) {
//			e.printStackTrace();
			assertTrue(true);
		}
	}

	public void addProperty2NonExistentChannel() {

	}

}
