package gov.bnl.channelfinder.channelfinderAPI;

import static org.junit.Assert.assertTrue;
import gov.bnl.channelfinder.channelfinderAPI.exceptions.ChannelFinderException;
import gov.bnl.channelfinder.model.XmlChannel;
import gov.bnl.channelfinder.model.XmlChannels;
import gov.bnl.channelfinder.model.XmlProperty;
import gov.bnl.channelfinder.model.XmlTag;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class QueryTest {

	private static XmlChannels chs;
	private static int channelcount;

	/**
	 * insert test data - for performing the queries described below.
	 */
	@BeforeClass
	public static void populateChannels() {
		chs = new XmlChannels();
		XmlChannel ch1 = new XmlChannel("pvk:01<first>", "shroffk");
		ch1.addProperty(new XmlProperty("prop", "1", "shroffk"));
		ch1.addProperty(new XmlProperty("prop2", "2", "shroffk"));
		ch1.addTag(new XmlTag("a", "shroffk"));
		XmlChannel ch2 = new XmlChannel("pvk:02<second>", "shroffk");
		ch2.addProperty(new XmlProperty("prop", "1", "shroffk"));
		ch2.addTag(new XmlTag("b", "shroffk"));
		XmlChannel ch3 = new XmlChannel("pvk:03<second>", "shroffk");
		ch3.addProperty(new XmlProperty("prop", "1", "shroffk"));
		ch3.addTag(new XmlTag("b", "shroffk"));
		chs.addChannel(ch1);
		chs.addChannel(ch2);
		chs.addChannel(ch3);
		try {
			channelcount = ChannelFinderClient.getInstance().getChannels()
					.getChannels().size();
			ChannelFinderClient.getInstance().addChannels(chs);
		} catch (ChannelFinderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Test
	public void querychannels() {

	}

	@AfterClass
	public static void cleanup() {
		ChannelFinderClient.getInstance().removeChannels(chs);
		assertTrue(ChannelFinderClient.getInstance().getChannels()
				.getChannels().size() == channelcount);
	}
}
