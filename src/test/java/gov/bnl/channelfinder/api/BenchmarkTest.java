package gov.bnl.channelfinder.api;

import static gov.bnl.channelfinder.api.Channel.Builder.channel;
import static gov.bnl.channelfinder.api.Property.Builder.property;
import static gov.bnl.channelfinder.api.Tag.Builder.tag;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BenchmarkTest {

	private static Collection<Channel.Builder> channels = new HashSet<Channel.Builder>();
	private static long originalChannelCount;
	private long time;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// create a table of 2000 channels
		originalChannelCount = ChannelFinderClient.getInstance().getAllChannels()
				.size();
		
		for (int i = 0; i < 2000; i++) {
			String channelName = "2000";
			channelName += getName(i);
			Channel.Builder channel = channel(channelName).owner("boss").with(
					property("prop", Integer.toString(i)).owner("boss"));
			if (i < 1000)
				channel.with(tag("tagA", "boss"));
			if ((i >= 500) || (i < 1500))
				channel.with(tag("tagB", "boss"));
			channels.add(channel);
		}
		// Add all the channels;
		try {
			ChannelFinderClient.getInstance().add(channels);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ChannelFinderClient.getInstance().remove(channels);
		assertTrue(ChannelFinderClient.getInstance().getAllChannels()
				.size() == originalChannelCount);
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
			Channel ch = ChannelFinderClient.getInstance().getChannel(
					"2000first:a<000>:0:0");
			assertTrue(ch.getName().equals("2000first:a<000>:0:0"));
			System.out.println("query1Channel duration : "
					+ (System.currentTimeMillis() - time));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void query10Channels() {
		time = System.currentTimeMillis();
		Collection<Channel> chs = ChannelFinderClient.getInstance()
				.findChannelsByName("2000first:a<400>:0*");
		assertTrue(chs.size() == 10);
		System.out.println("query10Channels duration : "
				+ (System.currentTimeMillis() - time));
	}

	@Test
	public void query100Channels() {
		time = System.currentTimeMillis();
		Collection<Channel> chs = ChannelFinderClient.getInstance()
				.findChannelsByName("2000first:a<400>:*");
		assertTrue(chs.size() == 100);
		System.out.println("query100Channels duration : "
				+ (System.currentTimeMillis() - time));
	}

	@Test
	public void query500Channels() {
		time = System.currentTimeMillis();
		Collection<Channel> chs = ChannelFinderClient.getInstance()
				.findChannelsByName("2000first:b*");
		assertTrue(chs.size() == 500);
		System.out.println("query500Channels duration : "
				+ (System.currentTimeMillis() - time));
	}

	@Test
	public void query1000Channels() {
		time = System.currentTimeMillis();
		Collection<Channel> chs = ChannelFinderClient.getInstance()
				.findChannelsByName("2000second:*");
		assertTrue(chs.size() == 1000);
		System.out.println("query1000Channels duration : "
				+ (System.currentTimeMillis() - time));
	}

	@Test
	public synchronized void query2000Channels() {
		time = System.currentTimeMillis();
		Collection<Channel> chs = ChannelFinderClient.getInstance()
				.findChannelsByName("2000*");
		assertTrue(chs.size() == 2000);
		System.out.println("query2000Channels duration : "
				+ (System.currentTimeMillis() - time));
	}

}
