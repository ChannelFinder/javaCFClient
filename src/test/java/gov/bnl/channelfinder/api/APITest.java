package gov.bnl.channelfinder.api;

import static gov.bnl.channelfinder.api.Channel.Builder.*;
import static gov.bnl.channelfinder.api.ChannelUtil.*;
import static gov.bnl.channelfinder.api.Property.Builder.*;
import static gov.bnl.channelfinder.api.Tag.Builder.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import gov.bnl.channelfinder.api.Tag.Builder;

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
		ChannelFinderClient.resetPreferences();
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
			Channel channel = client.getChannel(channelName);
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
	@SuppressWarnings("deprecation")
	@Test
	public void addRemoveChannels() {
		Collection<Channel.Builder> channels = new HashSet<Channel.Builder>();
		channels.add(channel("first").owner("TestOwner"));
		channels.add(channel("second").owner("TestOwner"));
		try {
			client.add(channels);
			assertTrue(client.getAllChannels()
					.containsAll(toChannels(channels)));

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
		Tag.Builder testTag1 = tag("TestTag1").owner("shroffk");
		Tag.Builder testTag2 = tag("TestTag2").owner("shroffk");
		try {
			// ensure that the tag exist.
			client.add(testTag1);
			client.add(testTag2);
			client.add(testChannel.with(testTag1));
			Channel retChannel = client.getChannel(testChannel.build()
					.getName());
			// check for no initial properties or tags
			assertTrue(retChannel.getTags().contains(testTag1.build()));
			// uses the POST method
			testChannel = channel("TestChannel").owner("shroffk");
			client.updateChannel(testChannel.with(testTag2));
			assertTrue(client.getChannel(testChannel.build().getName())
					.getTags().contains(testTag1.build()));
			assertTrue(client.getChannel(testChannel.build().getName())
					.getTags().contains(testTag2.build()));
		} finally {
			client.remove(testChannel);
			assertTrue("CleanUp failed",
					client.getAllChannels().size() == channelCount);
		}

	}

	/**
	 * Test destructive update - existing channel is completely replaced
	 */
	@Test
	public void setChannel() {
		Channel.Builder oldChannel = channel("old").owner("shroffk").with(
				tag("oldTag", "shroffk"));
		Channel.Builder newChannel = channel("old").owner("shroffk").with(
				tag("newTag", "shroffk"));
		client.add(tag("oldTag", "shroffk"));
		client.add(tag("newTag", "shroffk"));
		try {
			client.add(oldChannel);
			assertTrue(client.findChannelsByTag("oldTag").contains(
					oldChannel.build()));
			client.add(newChannel);
			assertTrue(!client.findChannelsByTag("oldTag").contains(
					oldChannel.build()));
			assertTrue(client.findChannelsByTag("newTag").contains(
					newChannel.build()));
		} finally {
			client.deleteTag("oldTag");
			client.deleteTag("newTag");
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
		client.add(tag(tagName, "tagOwner"));
		client.add(channel(channelName).owner("TestOwner"));
		assertTrue(!getTagNames(client.getChannel(channelName)).contains(
				tagName));
		client.add(tag(tagName, "tagOwner"), channelName);
		assertTrue(getTagNames(client.getChannel(channelName))
				.contains(tagName));
		client.remove(tag(tagName), channelName);
		assertTrue(!getTagNames(client.getChannel(channelName)).contains(
				tagName));
		client.remove(channel(channelName));
		assertTrue("CleanUp failed", !client.getAllChannels().contains(
				channel(channelName).build()));
	}

	/**
	 * Add a Remove a tag from multiple channels
	 */
	@SuppressWarnings("deprecation")
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
			assertTrue(client.findChannelsByTag(tag.build().getName())
					.containsAll(toChannels(channelSet)));
			client.remove(tag, getChannelNames(toChannels(channelSubSet)));
			Collection<Channel.Builder> diffSet = new HashSet<Channel.Builder>(
					channelSet);
			diffSet.removeAll(channelSubSet);
			assertTrue(client.findChannelsByTag(tag.build().getName())
					.containsAll(toChannels(diffSet)));
			// this method is not atomic
			client.remove(channelSet);
		} catch (ChannelFinderException e) {
		}

	}

	/**
	 * Remove Tag from all channels
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void deleteTag() {
		Collection<Channel.Builder> channels = new HashSet<Channel.Builder>();
		channels.add(channel("first").owner("TestOwner"));
		channels.add(channel("second").owner("TestOwner"));
		try {
			client.add(channels);
			Tag.Builder tag = tag("TestTag", "shroffk");
			client.add(tag);
			client.add(tag, getChannelNames(toChannels(channels)));
			assertTrue(client.findChannelsByTag("TestTag").size() > 0);
			client.deleteTag("TestTag");
			assertTrue(client.findChannelsByTag("TestTag").size() == 0);
		} finally {
			client.remove(channels);
		}
	}

	/**
	 * test the destructive setting of tags on channel/s
	 */
	@SuppressWarnings("deprecation")
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
			client.add(tag);
			// add tag to set1
			client.add(tag, getChannelNames(toChannels(channelSet1)));
			assertTrue(client.findChannelsByTag(tag.toXml().getName())
					.containsAll(toChannels(channelSet1)));
			// set the tag on channel first and remove it from every other
			// channel
			client.set(tag, "first");
			assertTrue(client.findChannelsByTag(tag.build().getName()).size() == 1);
			assertTrue(client.findChannelsByTag(tag.build().getName())
					.contains(channel("first").build()));
			// add the tag to set2 and remove it from every other channel
			client.set(tag, getChannelNames(toChannels(channelSet2)));
			assertTrue(client.findChannelsByTag(tag.build().getName()).size() == 2);
			assertTrue(client.findChannelsByTag(tag.toXml().getName())
					.containsAll(toChannels(channelSet2)));
		} finally {
			client.remove(channelSet1);
			client.remove(channelSet2);
		}

	}

	/**
	 * Add and Remove a property to a single channel
	 */
	@Test
	public void addRemoveProperty() {
		Channel.Builder testChannel = channel("TestChannel").owner("shroffk");
		Property.Builder property = property("TestProperty", "TestValue")
				.owner("TestOwner");

		try {
			client.add(testChannel);
			client.add(property);
			client.add(property, testChannel.toXml().getName());
			Collection<Channel> result = client.findChannelsByProp(property
					.build().getName());
			assertTrue(result.contains(testChannel.build()));
			client.remove(property, testChannel.toXml().getName());
			assertTrue(client.findChannelsByProp(property.toXml().getName())
					.size() == 0);
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
			client.add(property);
			int initialCount = client.findChannelsByProp(
					property.toXml().getName(), "*").size();
			client.add(property, getChannelNames(toChannels(channels)));
			assertTrue(client.findChannelsByProp(property.toXml().getName(),
					"*").containsAll(toChannels(channels)));
			client.remove(property, getChannelNames(toChannels(channels)));
			assertTrue(client.findChannelsByProp(property.toXml().getName(),
					"*").size() == initialCount);
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
		Property.Builder property = property("DeleteProp", "TestValue").owner(
				"shroffk");
		try {
			client.add(channels);
			client.add(property);
			client.add(property, getChannelNames(toChannels(channels)));
			assertTrue(client.findChannelsByProp(property.toXml().getName(),
					"*").size() == 2);
			client.deleteProperty(property.toXml().getName());
			assertTrue(client.findChannelsByProp(property.toXml().getName(),
					"*").size() == 0);
		} finally {
			client.remove(channels);
		}
	}

	/**
	 * check non-destructive addition of tags and channels
	 */
	@Test
	public void addTagsProperty() {
		client.add(tag("existingTag", "owner"));
		client.add(property("existingProperty", "propValue").owner("owner"));
		Channel.Builder testChannel = channel("testChannel").owner("owner")
				.with(tag("existingTag", "owner")).with(
						property("existingProperty", "propValue")
								.owner("owner"));
		client.add(testChannel);
		Channel result = (client.getChannel(testChannel.build().getName()));
		assertTrue(result.getTags().contains(
				tag("existingTag", "owner").build()));
		assertTrue(result.getProperties().contains(
				property("existingProperty", "propValue").build()));

		client.add(tag("newTag", "owner"));
		client.add(tag("newTag", "owner"), testChannel.build().getName());

		client.add(property("newProperty", "newPropValue").owner("owner"));
		client.add(property("newProperty", "newPropValue").owner("owner"),
				testChannel.build().getName());

		result = (client.getChannel(testChannel.build().getName()));
		assertTrue(result.getTags().contains(
				tag("existingTag", "owner").build()));
		assertTrue(result.getTags().contains(tag("newTag", "owner").build()));
		assertTrue(result.getProperties().contains(
				property("existingProperty", "propValue").build()));
		assertTrue(result.getProperties().contains(
				property("newProperty", "newPropValue").build()));
		client.remove(testChannel);
	}

	@Test
	public void getAllTags() {
		client.add(tag("TestTag", "testOwner"));
		assertTrue(client.getAllTags().contains("TestTag"));
		client.deleteTag("TestTag");
		assertTrue("TestTag clean up failed", !client.getAllTags().contains(
				"TestTag"));
	}

	@Test
	public void getAllProperties() {
		String propertyName = "TestProperty";
		client.add(property(propertyName, "testValue").owner("testOwner"));
		assertTrue(client.getAllProperties().contains(propertyName));
		client.deleteProperty(propertyName);
		assertTrue("TestProperty clean up failed", !client.getAllProperties()
				.contains(propertyName));
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
