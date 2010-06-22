package gov.bnl.channelfinder.channelfinderAPI;

import static org.junit.Assert.assertTrue;

import java.util.Hashtable;
import java.util.Map;

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
		ch1.addProperty(new XmlProperty("prop", "shroffk", "1"));
		ch1.addProperty(new XmlProperty("prop2", "shroffk", "2"));
		ch1.addTag(new XmlTag("Taga", "shroffk"));
		XmlChannel ch2 = new XmlChannel("pvk:02<second>", "shroffk");
		ch2.addProperty(new XmlProperty("prop", "shroffk", "1"));
		ch2.addTag(new XmlTag("Taga", "shroffk"));
		ch2.addTag(new XmlTag("Tagb", "shroffk"));
		XmlChannel ch3 = new XmlChannel("pvk:03<second>", "shroffk");
		ch3.addProperty(new XmlProperty("prop", "shroffk", "2"));
		ch3.addTag(new XmlTag("Tagb", "shroffk"));
		ch3.addTag(new XmlTag("Tagc", "shroffk"));
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
	public void queryAllChannels() {
		Map<String, String> map = new Hashtable<String, String>();
		map.put("~name", "*");
		XmlChannels channels = ChannelFinderClient.getInstance().queryChannels(
				map);
		assertTrue(ChannelFinderClient.getInstance().getChannels()
				.getChannels().size() == channels.getChannels().size());
	}
	
	@Test
	public void queryChannels() {
		Map<String, String> map = new Hashtable<String, String>();
		map.put("~name", "pvk:*");
		XmlChannels channels = ChannelFinderClient.getInstance().queryChannels(
				map);
		assertTrue(channels.getChannels().size() == 3);
	}
	
	@Test 
	public void queryChannelsbyProperty(){
		Map<String, String> map = new Hashtable<String, String>();
		map.put("prop", "1");
		XmlChannels channels = ChannelFinderClient.getInstance().queryChannels(
				map);
		assertTrue(channels.getChannels().size() == 2);
		
		map.put("prop", "1");
		map.put("prop", "2");
		channels = ChannelFinderClient.getInstance().queryChannels(
				map);
		assertTrue(channels.getChannels().size() == 1);
		
		map.clear();
		map.put("cell", "14");
		channels = ChannelFinderClient.getInstance().queryChannels(
				map);		
	}
	

	@AfterClass
	public static void cleanup() {
		ChannelFinderClient.getInstance().removeChannels(chs);
		assertTrue(ChannelFinderClient.getInstance().getChannels()
				.getChannels().size() == channelcount);
	}
}
