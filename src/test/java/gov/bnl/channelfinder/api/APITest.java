package gov.bnl.channelfinder.api;

import static gov.bnl.channelfinder.api.Channel.Builder.*;
import static gov.bnl.channelfinder.api.ChannelUtil.*;
import static gov.bnl.channelfinder.api.Property.Builder.*;
import static gov.bnl.channelfinder.api.Tag.Builder.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashSet;

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
		client = ChannelFinderClient.getInstance();
		channelCount = client.getAllChannels().size();
	}

	@Test
	public void test() {

	}

	@Test
	public void builderTest() {
		exception.expect(is(ChannelFinderException.class));
		exception.expect(new StatusMatcher(Status.NOT_FOUND));
		client.getChannel("ChannelName");
	}

	/**
	 * Add a single channel
	 */
	@Test
	public void addRemoveChannel() {
		String channelName = "TestChannelName";
		try {
			// Add a channel
			client.add(channel(channelName).owner("TestOwner"));
			client.getChannel(channelName);
			// Remove a channel
			client.remove(channel(channelName));
			assertTrue(!client.getAllChannels().contains(channel(channelName)));
			assertTrue("CleanUp failed",
					client.getAllChannels().size() == channelCount);
		} catch (ChannelFinderException e) {
			if (e.getStatus().equals(Status.NOT_FOUND))
				fail("Channel not added. " + e.getMessage());
		} finally {

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
			assertTrue(client.getAllChannels().containsAll(toChannels(channels)));

		} catch (ChannelFinderException e) {
			fail(e.getMessage());
		} finally {
			client.remove(channels);
			assertTrue("CleanUp failed",
					client.getAllChannels().size() == channelCount);
		}
	}

	/**
	 * update an existing channel with a new property and new tag
	 * 
	 * Add/update test
	 */
	@Test
	public void updateChannel() {
		Channel.Builder testChannel = channel("TestChannel").owner("shroffk");
		Tag.Builder testTag = tag("TestTag").owner("shroffk");
		try {
			client.add(testChannel);
			Channel retChannel = client.getChannel(testChannel.build()
					.getName());
			// check for no initial properties or tags
			assertTrue(retChannel.getTags().size() == 0);
			// uses the POST method
			client.updateChannel(testChannel.with(testTag));
			assertTrue(client.getChannel(testChannel.build().getName())
					.getTags().contains(testTag.build()));
		} catch (ChannelFinderException e) {
			e.printStackTrace();
		} finally {
			client.remove(testChannel);
			assertTrue("CleanUp failed",
					client.getAllChannels().size() == channelCount);
		}

	}

	/**
	 * TODO fix the assert to be smarter than just size checks
	 * Test destructive update - existing channel is completely replaced
	 */
	@Test
	public void setChannel() {
		Channel.Builder oldChannel = channel("old").owner("shroffk").with(
				tag("oldTag", "shroffk"));
		Channel.Builder newChannel = channel("old").owner("shroffk").with(
				tag("newTag", "shroffk"));
		try {
			client.add(oldChannel);
			assertTrue(client.findChannelsByTag("oldTag").size() == 1);
			client.add(newChannel);
			assertTrue(client.findChannelsByTag("oldTag").size() == 0);
			assertTrue(client.findChannelsByTag("newTag").size() == 1);
		} catch (ChannelFinderException e) {
			e.printStackTrace();
		} finally {
			client.remove(newChannel);
		}
	}

	/**
	 * Add a Tag to a single channel
	 */
	@Test
	public void addRemoveTag() {
		String channelName = "TestChannel";
		String tagName = "TestTag";
		try {
			client.add(channel(channelName).owner("TestOwner"));
			assertTrue(!getTagNames(client.getChannel(channelName)).contains(
					tagName));
			client.add(tag(tagName, "tagOwner"), channelName);
			assertTrue(getTagNames(client.getChannel(channelName)).contains(
					tagName));
			client.remove(tag(tagName), channelName);
			assertTrue(!getTagNames(client.getChannel(channelName)).contains(
					tagName));
			client.remove(channel(channelName));
			assertTrue("CleanUp failed",
					client.getAllChannels().size() == channelCount);
		} catch (ChannelFinderException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * TODO redo assert with new find methods and checks .contains
	 */
	@Test
	public void addRemoveTag2Channels() {
		Tag.Builder tag = tag("tag", "tagOwner");
		Collection<Channel.Builder> channelSet = new HashSet<Channel.Builder>();
		Collection<Channel.Builder> channelSubSet = new HashSet<Channel.Builder>();
		channelSubSet.add(channel("first").owner("TestOwner"));
		channelSubSet.add(channel("second").owner("TestOwner"));
		channelSet.addAll(channelSubSet);
		channelSet.add(channel("third").owner("TestOwner"));

		try {
			client.add(channelSet);
			client.add(tag, getChannelNames(toChannels(channelSet)));
			client.remove(tag, getChannelNames(toChannels(channelSubSet)));
			client.remove(channelSet);
		} catch (ChannelFinderException e) {
		}

	}

	/**
	 * TODO redo assert with new find methods and checks .contains Remove Tag
	 * from all channels
	 */
	@Test
	public void deleteTag() {
		Collection<Channel.Builder> channels = new HashSet<Channel.Builder>();
		channels.add(channel("first").owner("TestOwner"));
		channels.add(channel("second").owner("TestOwner"));
		try {
			client.add(channels);
			client
					.add(tag("Tag", "shroffk"),
							getChannelNames(toChannels(channels)));
			assertTrue(client.findChannelsByTag("Tag").size() > 0);
			client.deleteTag("Tag");
			assertTrue(client.findChannelsByTag("Tag").size() == 0);
		} catch (ChannelFinderException e) {
			e.printStackTrace();
		} finally {
			client.remove(channels);
		}
	}

	/**
	 * TODO redo assert with new find methods and checks .contains test the
	 * destructive setting of tags on channel/s
	 */
	@Test
	public void testSetTag() {
		Collection<Channel.Builder> channelSet1 = new HashSet<Channel.Builder>();
		channelSet1.add(channel("first").owner("TestOwner"));
		channelSet1.add(channel("second").owner("TestOwner"));
		Collection<Channel.Builder> channelSet2 = new HashSet<Channel.Builder>();
		channelSet2.add(channel("third").owner("TestOwner"));
		channelSet2.add(channel("forth").owner("TestOwner"));
		Tag.Builder tag = tag("TestTag", "TestOwner");

		try {
			client.add(channelSet2);
			client.add(channelSet1);
			// add tag to set1
			client.add(tag, getChannelNames(toChannels(channelSet1)));
			assertTrue(client.findChannelsByTag(tag.toXml().getName())
					.size() == 2);
			// set the tag on channel first and remove it from every other
			// channel
			client.set(tag, "first");
			assertTrue(client.findChannelsByTag(tag.toXml().getName())
					.size() == 1);
			// add the tag to set2 and remove it from every other channel
			client.set(tag, getChannelNames(toChannels(channelSet2)));
			assertTrue(client.findChannelsByTag(tag.toXml().getName())
					.size() == 2);
			// TODO check if the rest of the channel remains unchanges
		} catch (ChannelFinderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			client.remove(channelSet1);
			client.remove(channelSet2);
		}

	}

	/**
	 * Add And Remove a property to a single channel
	 */
	@Test
	public void addRemoveProperty() {
		Channel.Builder testChannel = channel("TestChannel").owner("shroffk");
		Property.Builder property = property("TestProperty", "TestValue")
				.owner("TestOwner");

		try {
			client.add(testChannel);
			client.add(property, testChannel.toXml().getName());
			assertTrue(client.findChannelsByProp(property.toXml().getName())
					.size() == 1);
			client.remove(property, testChannel.toXml().getName());
			assertTrue(client.findChannelsByProp(property.toXml().getName())
					.size() == 0);
		} catch (ChannelFinderException e) {
			e.printStackTrace();
		} finally {
			client.remove(testChannel);
		}

	}

	/**
	 * TODO fix assert stmt Add and Remove a property from multiple channels
	 */
	@Test
	public void addRemoveProperty2Channels() {
		Collection<Channel.Builder> channels = new HashSet<Channel.Builder>();
		channels.add(channel("first").owner("shroffk"));
		channels.add(channel("second").owner("shroffk"));
		Property.Builder property = property("TestProperty", "TestValue")
				.owner("shroffk");
		try {
			client.add(channels);
			client.add(property, getChannelNames(toChannels(channels)));
			assertTrue(client.findChannelsByProp(property.toXml().getName(),
					"*").size() == 2);
			client.remove(property, getChannelNames(toChannels(channels)));
			assertTrue(client.findChannelsByProp(property.toXml().getName(),
					"*").size() == 0);
		} catch (ChannelFinderException e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		} finally {
			client.remove(channels);
		}
	}

	/**
	 * TODO fix the asserts 
	 */
	@Test
	public void deleteProperty() {
		Collection<Channel.Builder> channels = new HashSet<Channel.Builder>();
		channels.add(channel("first").owner("shroffk"));
		channels.add(channel("second").owner("shroffk"));
		Property.Builder property = property("TestProperty", "TestValue")
				.owner("shroffk");
		try {
			client.add(channels);
			client.add(property, getChannelNames(toChannels(channels)));
			assertTrue(client.findChannelsByProp(property.toXml().getName(),
					"*").size() == 2);
			client.deleteProperty(property.toXml().getName());
			assertTrue(client.findChannelsByProp(property.toXml().getName(),
					"*").size() == 0);
		} catch (ChannelFinderException e) {
			e.printStackTrace();
		} finally {
			client.remove(channels);
		}
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
