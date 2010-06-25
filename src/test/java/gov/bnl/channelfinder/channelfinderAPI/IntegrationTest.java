package gov.bnl.channelfinder.channelfinderAPI;

import static org.junit.Assert.assertTrue;

import java.util.prefs.Preferences;

import gov.bnl.channelfinder.channelfinderAPI.ChannelFinderClient;
import gov.bnl.channelfinder.channelfinderAPI.exceptions.ChannelFinderException;
import gov.bnl.channelfinder.model.XmlChannel;
import gov.bnl.channelfinder.model.XmlChannels;
import gov.bnl.channelfinder.model.XmlProperty;
import gov.bnl.channelfinder.model.XmlTag;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class IntegrationTest {

	/**
	 * Rigourous Test :-)
	 */

	@BeforeClass
	public static void setUp() throws Exception {
		// set up the preferences
		Preferences preferences = Preferences
				.userNodeForPackage(ChannelFinderClient.class);
		preferences
				.put("channel_finder_url",
						"https://channelfinder.nsls2.bnl.gov:8181/ChannelFinder/resources");
		preferences.put("username", "boss");
		preferences.put("password", "1234");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDown() throws Exception {
	}

	/**
	 * use GET to acquire a list of channels
	 */
	@Test
	public void getChannels() {
		try {
			XmlChannels chs = ChannelFinderClient.getInstance().getChannels();
			System.out.println(chs.getChannels().size());
			assertTrue(chs.getChannels().size() >= 0);
		} catch (ChannelFinderException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getStatus().getStatusCode() + " "
					+ e.getStatus() + "\n" + e.getMessage());
			// e.printStackTrace();
		}
	}

	/**
	 * Add single channel pvk01
	 */

	@Test
	public void addremoveChannel() {
		try {
			XmlChannel channel = new XmlChannel("pvk01:<first>", "boss");
			ChannelFinderClient.getInstance().addChannel(channel);
			XmlChannels chs = ChannelFinderClient.getInstance().getChannels();
			assertTrue(chs.containsKey("pvk01:<first>"));
			ChannelFinderClient.getInstance().removeChannel("pvk01:<first>");
			chs = ChannelFinderClient.getInstance().getChannels();
			assertTrue(!chs.getChannels().contains(channel));
		} catch (ChannelFinderException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getStatus().getStatusCode() + e.getMessage());
		}
	}

	/**
	 * Add and remove channels
	 */
	@Test
	public void addremoveChannels() {
		XmlChannels chs = new XmlChannels();
		for (int i = 1; i <= 3; i++) {
			chs.addChannel(new XmlChannel("pvk0" + i, "shroffk"));
		}
		// add
		ChannelFinderClient.getInstance().addChannels(chs);
		XmlChannels rchs = ChannelFinderClient.getInstance().getChannels();
		int count = rchs.getChannels().size();
		for (int i = 1; i <= 3; i++) {
			assertTrue(rchs.containsKey("pvk0" + i));
		}
		// remove
		ChannelFinderClient.getInstance().removeChannels(chs);
		rchs = ChannelFinderClient.getInstance().getChannels();
		for (int i = 1; i <= 3; i++) {
			assertTrue(!rchs.containsKey("pvk0" + i));
		}
		// check 3 channels were removed
		assertTrue((count - rchs.getChannels().size()) == 3);
		// assertTrue(rchs.getChannels().size() == 0);
	}

	/**
	 * 
	 */

	/**
	 * update an existing channel with a new property and new tag
	 * 
	 * Add/update test
	 */
	@Test
	public void updateChannel() {
		XmlChannel channel = new XmlChannel("pvk03", "shroffk");
		ChannelFinderClient.getInstance().addChannel(channel);
		XmlChannel retChannel = ChannelFinderClient.getInstance().getChannel(
				channel.getName());
		// check for no initial properties or tags
		assertTrue(retChannel.getXmlProperties().size() == 0);
		assertTrue(retChannel.getXmlTags().size() == 0);
		channel.addProperty(new XmlProperty("prop1", "shroffk", "val1"));
		channel.addTag(new XmlTag("tag1", "shroffk"));
		// uses the POST method
		ChannelFinderClient.getInstance().updateChannel(channel);
		assertTrue(ChannelFinderClient.getInstance().getChannel(
				channel.getName()).getXmlProperties().size() == 1);
		assertTrue(ChannelFinderClient.getInstance().getChannel(
				channel.getName()).getXmlTags().size() == 1);
		ChannelFinderClient.getInstance().removeChannel(channel.getName());
	}

	/**
	 * Test destructive update - existing channel is completely replaced
	 * 
	 */
	@Test
	public void addChannel() {
		XmlChannel oldChannel = new XmlChannel("old", "shroffk");
		oldChannel.addTag(new XmlTag("old", "shroffk"));
		ChannelFinderClient.getInstance().addChannel(oldChannel);
		assertTrue(ChannelFinderClient.getInstance().queryChannelsTag("old")
				.getChannels().size() == 1);
		XmlChannel newChannel = new XmlChannel("old", "shroffk");
		newChannel.addTag(new XmlTag("new", "shroffk"));
		ChannelFinderClient.getInstance().addChannel(newChannel);
		assertTrue(ChannelFinderClient.getInstance().queryChannelsTag("old")
				.getChannels().size() == 0);
		assertTrue(ChannelFinderClient.getInstance().queryChannelsTag("new")
				.getChannels().size() == 1);
		ChannelFinderClient.getInstance().removeChannel(newChannel.getName());
	}

	/**
	 * Test set Tag to all the channels specified. The tag already exists in the
	 * database
	 */
	@Test
	public void setTag() {
		XmlChannels chs = new XmlChannels();
		XmlChannel ch = new XmlChannel("pvk01", "boss");
		XmlTag tag = new XmlTag("tagName", "shroffk");
		ch.addTag(tag);
		chs.addChannel(ch);
		chs.addChannel(new XmlChannel("pvk02", "boss"));
		chs.addChannel(new XmlChannel("pvk03", "boss"));
		ChannelFinderClient.getInstance().addChannels(chs);
		assertTrue(ChannelFinderClient.getInstance().queryChannelsTag(
				tag.getName()).getChannels().size() == 1);
		ChannelFinderClient.getInstance().setTag(chs, tag.getName());
		assertTrue(ChannelFinderClient.getInstance()
				.queryChannelsTag("tagName").getChannels().size() == 3);
		ChannelFinderClient.getInstance().removeChannels(chs);
	}

	/**
	 * Test set Tag to all the channels specified. The tag ownership details are
	 * present in the payload
	 */
	@Test
	public void setTag2() {
		XmlChannels chs = new XmlChannels();
		for (int i = 1; i <= 3; i++) {
			chs.addChannel(new XmlChannel("pvk0" + i, "boss"));
		}
		ChannelFinderClient.getInstance().addChannels(chs);

		XmlTag tag = new XmlTag("tagName", "shroffk");
		// adding owner to the payload.
		chs.getChannels().toArray(new XmlChannel[0])[0].addTag(tag);
		assertTrue(ChannelFinderClient.getInstance().queryChannelsTag(
				tag.getName()).getChannels().size() == 0);
		ChannelFinderClient.getInstance().setTag(chs, tag.getName());
		assertTrue(ChannelFinderClient.getInstance().queryChannelsTag(
				tag.getName()).getChannels().size() == 3);
		ChannelFinderClient.getInstance().removeChannels(chs);

	}

	/**
	 * test the correct operation of both methods to add tags. addTag() - non
	 * destructive - POST. setTag() - destructive - PUT. The tag ownership
	 * details are already present in the database
	 */
	@Test
	public void addTag() {
		XmlChannels chs = new XmlChannels();
		XmlChannels results;
		XmlChannel ch = new XmlChannel("pvk01", "boss");
		XmlTag tag = new XmlTag("tagName", "boss");
		ch.addTag(tag);
		chs.addChannel(ch);
		for (int i = 2; i <= 4; i++) {
			chs.addChannel(new XmlChannel("pvk0" + i, "boss"));
		}
		ChannelFinderClient.getInstance().addChannels(chs);

		// Add a tag to channels using post - this should leave other tags
		// intact
		// tagged channels include pvk01
		results = ChannelFinderClient.getInstance().queryChannelsTag(
				tag.getName());
		assertTrue(results.getChannels().size() == 1);

		chs = new XmlChannels();
		chs.addChannel(new XmlChannel("pvk02", "boss"));
		ChannelFinderClient.getInstance().addTag(chs, tag.getName());

		// tagged channels include pvk01, pvk02
		results = ChannelFinderClient.getInstance().queryChannelsTag(
				tag.getName());
		assertTrue(results.getChannels().size() == 2);
		for (XmlChannel channel : results.getChannels()) {
			assertTrue(channel.getName().equals("pvk01")
					|| channel.getName().equals("pvk02"));
		}

		chs = new XmlChannels();
		chs.addChannel(new XmlChannel("pvk03", "boss"));
		chs.addChannel(new XmlChannel("pvk04", "boss"));
		ChannelFinderClient.getInstance().setTag(chs, tag.getName());

		// tagged channels include pvk03 and 04 - tag removed from pvk01 and 02
		results = ChannelFinderClient.getInstance().queryChannelsTag(
				tag.getName());
		assertTrue(results.getChannels().size() == 2);
		for (XmlChannel channel : results.getChannels()) {
			assertTrue(channel.getName().equals("pvk03")
					|| channel.getName().equals("pvk04"));
		}
		for (int i = 1; i <= 4; i++) {
			ChannelFinderClient.getInstance().removeChannel("pvk0" + i);
		}
	}

	/**
	 * Remove tag from all channels
	 */
	@Test
	public void removeTag(){
		ChannelFinderClient client = ChannelFinderClient.getInstance();
		XmlChannel ch;
		XmlTag tag = new XmlTag("tagName", "boss");
		for (int i = 1; i <= 3; i++) {
			ch = new XmlChannel("pvk0"+i, "boss");
			ch.addTag(tag);
			client.addChannel(ch);
		}
		assertTrue(client.queryChannelsTag(tag.getName()).getChannels().size() == 3);
		client.removeTag(tag.getName());
		assertTrue(client.queryChannelsTag(tag.getName()).getChannels().size() == 0);
		//cleanup
		for (int i = 1; i <= 3; i++) {
			ChannelFinderClient.getInstance().removeChannel("pvk0" + i);
		}
		
	}
	
	/**
	 * Test adding and removing a tag on a single channel
	 */
	@Test
	public void addRemoveChannelTag(){
		ChannelFinderClient client = ChannelFinderClient.getInstance();
		XmlChannel channel = new XmlChannel("pvk01", "boss");
		client.addChannel(channel);
		
		// Add tag
		XmlTag tag = new XmlTag("tagName", "boss");
		client.setChannelTag(channel.getName(), tag);
		assertTrue(client.queryChannelsTag(tag.getName()).getChannels().size() == 1);
		// Remove tag
		client.removeChannelTag(channel.getName(), tag.getName());
		assertTrue(client.queryChannelsTag(tag.getName()).getChannels().size() == 0);
		//cleanup 
		client.removeChannel(channel.getName());
	}	
}
