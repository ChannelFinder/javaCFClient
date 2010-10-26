package gov.bnl.channelfinder.api;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashSet;

import static gov.bnl.channelfinder.api.Channel.Builder.*;
import static gov.bnl.channelfinder.api.Tag.Builder.*;
import static gov.bnl.channelfinder.api.Property.Builder.*;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.sun.jersey.api.client.ClientResponse.Status;

public class APITest {
	private static ChannelFinderClient client;
	private static int channelCount;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void beforeTests() {
		client = client.getInstance();
//		channelCount = client.getAllChannels().size();
	}

	@Test
	public void builderTest() {
		exception.expect(is(ChannelFinderException.class));
		exception.expect(new StatusMatcher(Status.NOT_FOUND));
		client.getChannel("ChannelName");
		// client.add(Channel.channel("myChannel").with(tag("myTag",
		// "myTagOwner")));
		// client.add("channelName", tag("tagName", "tagOwner"));
		// client.add("channelName", property("propertyName", "propertyValue")
		// .owner("propertyOwner"));

	}

	/**
	 * Add a single channel
	 */
	@Test
	public void addRemoveChannel() {
		String channelName = "TestChannelName";
		try {
			client.add(channel(channelName).owner("TestOwner"));
			client.getChannel(channelName);
			client.remove(channel(channelName));
//			assertTrue(!client.getAllChannels().contains(channel(channelName)));
		} catch (ChannelFinderException e) {
			if (e.getStatus().equals(Status.NOT_FOUND))
				fail("Channel not added. " + e.getMessage());
		}

	}

	/**
	 * Add a set of channels
	 */
	@Test
	public void addRemoveChannels() {
		Collection<Channel.Builder> channels = new HashSet<Channel.Builder>();
		channels.add(channel("first").owner("TestOwner"));
		channels.add(channel("second").owner("TestOwner"));
		try {
			client.add(channels);
			assertTrue(client.getAllChannels().size() == channelCount + 2);
			assertTrue(client.getAllChannels().containsAll(channels));
			client.remove(channels);
		} catch (ChannelFinderException e) {
			fail(e.getMessage());
		}

	}

	/**
	 * Add a Tag to a single channel
	 */
	@Test
	public void addTag() {
		String channelName = "TestChannel";
		String tagName = "TestTag";
		try {
			client.add(channel(channelName).owner("TestOwner").with(
					tag(tagName, "TestOwner")));
			// assertTrue(null, client.)
		} catch (ChannelFinderException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void addTag2Channels() {

	}

	@Test
	public void addProperty() {

	}

	@Test
	public void addProperty2Channels() {

	}

	class StatusMatcher extends BaseMatcher<ChannelFinderException> {

		private Status status;

		StatusMatcher(Status status) {
			this.status = status;
		}

		@Override
		public void describeTo(Description description) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean matches(Object item) {
			if (((ChannelFinderException) item).getStatus().equals(this.status))
				return true;
			else
				return false;
		}
	}
}
