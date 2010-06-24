package gov.bnl.channelfinder.channelfinderAPI;

import static org.junit.Assert.assertTrue;

import gov.bnl.channelfinder.model.XmlChannel;
import gov.bnl.channelfinder.model.XmlChannels;
import gov.bnl.channelfinder.model.XmlProperty;
import gov.bnl.channelfinder.model.XmlTag;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BenchmarkTest {

	private static XmlChannels channels = new XmlChannels();
	private long time;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// create a table of 2000 channels
		XmlChannels chs = ChannelFinderClient.getInstance().getChannels();
		assertTrue(chs.getChannels().size() >= 0);

		XmlChannel channel;
		XmlProperty property;
		XmlTag tagA = new XmlTag("tagA", "boss");
		XmlTag tagB = new XmlTag("tagB", "boss");
		for (int i = 0; i < 2000; i++) {
			String channelName = "2000";
			channelName += getName(i);
			channel = new XmlChannel(channelName, "boss");
			property = new XmlProperty("prop", "boss", Integer.toString(i));
			channel.addProperty(property);
			if (i < 1000)
				channel.addTag(tagA);
			if ((i >= 500) || (i < 1500))
				channel.addTag(tagB);
			channels.addChannel(channel);
		}
		// Add all the channels;
		try {
			ChannelFinderClient.getInstance().addChannels(channels);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ChannelFinderClient.getInstance().removeChannels(channels);
		System.out.println(ChannelFinderClient.getInstance().getChannels().getChannels()
				.size());
	}
	

	private static String getName(int i) {
		if (i < 1000)
			return "first:" + getName500(i);
		else
			return "second:" + getName500(i - 1000);
	}

	private static String getName500(int i) {
		if (i < 500)
			return "a" + getName100(i);
		else
			return "b" + getName100(i - 500);
	}

	private static String getName100(int i) {
		return "<" + Integer.toString(i / 100) + "00>" + getNameID(i % 100);
	}

	private static String getNameID(int i) {
		return ":" + Integer.toString(i / 10) + ":" + Integer.toString(i);
	}
	
	@Test
	public synchronized void query1Channel() {
		time = System.currentTimeMillis();
		try {
		XmlChannel ch = ChannelFinderClient.getInstance().getChannel("2000first:a<000>:0:0");
		assertTrue(ch.getName().equals("2000first:a<000>:0:0"));
		System.out.println("query1Channel duration : " + (System.currentTimeMillis() - time));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void query10Channels() {
		time = System.currentTimeMillis();
		XmlChannels chs = ChannelFinderClient.getInstance().queryChannelsName(
				"2000first:a<400>:0*");
		assertTrue(chs.getChannels().size() == 10);
		System.out.println("query10Channels duration : " + (System.currentTimeMillis() - time));
	}

	@Test
	public void query100Channels() {
		time = System.currentTimeMillis();
		XmlChannels chs = ChannelFinderClient.getInstance().queryChannelsName(
				"2000first:a<400>:*");
		assertTrue(chs.getChannels().size() == 100);
		System.out.println("query100Channels duration : " + (System.currentTimeMillis() - time));
	}

	@Test
	public void query500Channels() {
		time = System.currentTimeMillis();
		XmlChannels chs = ChannelFinderClient.getInstance()
				.queryChannelsName("2000first:b*");
		assertTrue(chs.getChannels().size() == 500);
		System.out.println("query500Channels duration : " + (System.currentTimeMillis() - time));
	}

	@Test
	public void query1000Channels() {
		time = System.currentTimeMillis();
		XmlChannels chs = ChannelFinderClient.getInstance().queryChannelsName("2000second:*");
		assertTrue(chs.getChannels().size() == 1000);
		System.out.println("query1000Channels duration : " + (System.currentTimeMillis() - time));
	}

	@Test
	public synchronized void query2000Channels() {
		time = System.currentTimeMillis();
		XmlChannels chs = ChannelFinderClient.getInstance()
				.queryChannelsName("2000*");
		assertTrue(chs.getChannels().size() == 2000);
		System.out.println("query2000Channels duration : " + (System.currentTimeMillis() - time));
	}

}
