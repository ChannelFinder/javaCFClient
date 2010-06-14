package gov.bnl.channelfinder.channelfinderAPI;

import static org.junit.Assert.assertTrue;
import gov.bnl.channelfinder.model.XmlChannel;
import gov.bnl.channelfinder.model.XmlChannels;
import gov.bnl.channelfinder.model.XmlProperty;
import gov.bnl.channelfinder.model.XmlTag;

import java.util.Iterator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class QueryTest {

	/**
	 * insert test data - for performing the queries described below.
	 */
	@BeforeClass
	public static void populateChannels(){
		
	}
	
	/**
	 * 
	 */
	@Test
	public void querychannels() {
		XmlChannels chs = new XmlChannels();
		XmlChannel ch1 = new XmlChannel("pvk:01<first>", "shroffk");
		ch1.addProperty(new XmlProperty("prop", "1", "shroffk"));
		ch1.addTag(new XmlTag("a", "shroffk"));
		XmlChannel ch2 = new XmlChannel("pvk:02<second>", "shroffk");
		ch2.addProperty(new XmlProperty("prop", "1", "shroffk"));
		ch2.addTag(new XmlTag("b", "shroffk"));
		chs.addChannel(ch1);
		chs.addChannel(ch2);
		ChannelFinderClient.getInstance().addChannels(chs);
		// find a single channel
		assertTrue(ChannelFinderClient.getInstance().getChannel("pvk:01*").getName().equals(
				"pvk:01<first>"));
		// find by name
		assertTrue(ChannelFinderClient.getInstance().queryChannelsName("pvk:0*")
				.getChannels().size() == 2);
		// find by tag
		assertTrue(ChannelFinderClient.getInstance().queryChannelsTag("a").getChannels()
				.size() == 1);
		// check for special char in return type
		@SuppressWarnings("unused")
		Iterator<XmlChannel> itr = ChannelFinderClient.getInstance().getChannels()
				.getChannels().iterator();
		ChannelFinderClient.getInstance().removeChannels(chs);
	}

	@AfterClass
	public static void cleanup(){
		
	}
}
