package gov.bnl.channelfinder.api;

import static gov.bnl.channelfinder.api.ChannelFinderClient.*;
import static gov.bnl.channelfinder.api.Channel.Builder.channel;
import static gov.bnl.channelfinder.api.ChannelUtil.getChannelNames;
import static gov.bnl.channelfinder.api.ChannelUtil.getTagNames;
import static gov.bnl.channelfinder.api.ChannelUtil.toChannels;
import static gov.bnl.channelfinder.api.ChannelUtil.getProperty;
import static gov.bnl.channelfinder.api.Property.Builder.property;
import static gov.bnl.channelfinder.api.Tag.Builder.tag;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.acl.Owner;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import junit.framework.TestCase;

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
		// client = ChannelFinderClient.getInstance();
		client = CFCBuilder.toDefault().withHTTPAuthentication(true).create();
		channelCount = client.getAllChannels().size();
	}

	@Test
	public void builderTest() {
		exception.expect(is(ChannelFinderException.class));
		exception.expect(new StatusMatcher(Status.NOT_FOUND));
		client.getChannel("ChannelName");
	}

	/**
	 * set and delete a single channel
	 */
	@Test
	public void setDeleteChannel() {
		String channelName = "TestChannelName";
		try {
			// Add a channel
			client.set(channel(channelName).owner("channel"));
			client.getChannel(channelName);
			// Remove a channel
			client.delete(channel(channelName));
			assertTrue(!client.getAllChannels().contains(channel(channelName)));
			assertTrue("CleanUp failed",
					client.getAllChannels().size() == channelCount);
		} catch (ChannelFinderException e) {
			if (e.getStatus().equals(Status.NOT_FOUND))
				fail("Channel not added. " + e.getMessage());
		} finally {

		}
	}

	@Test
	public void setDeleteTag() {
		client.set(tag("setTag").owner("channel"));
		assertTrue("failed to create Tag: setTag ", client.getAllTags()
				.contains("setTag"));
		client.deleteTag("setTag");
		assertTrue("failed to delete Tag: setTag ", !client.getAllTags()
				.contains("setTag"));
	}

	@Test
	public void setDeleteProperty() {
		client.set(property("setProperty").owner("channel"));
		assertTrue("failed to create Property: setPtoperty", client
				.getAllProperties().contains("setProperty"));
		client.deleteProperty("setProperty");
		assertTrue("Failed to Delete the property: setProperty", !client
				.getAllProperties().contains("setProperties"));
	}

	/**
	 * Add a set of channels
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void addRemoveChannels() {
		Collection<Channel.Builder> channels = new HashSet<Channel.Builder>();
		channels.add(channel("first").owner("channel"));
		channels.add(channel("second").owner("channel"));
		try {
			client.set(channels);
			assertTrue(client.getAllChannels()
					.containsAll(toChannels(channels)));
		} catch (ChannelFinderException e) {
			fail("Failed to add channels first and/or second \n Cause:"
					+ e.getMessage());
		} finally {
			client.delete(channels);
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
		Channel.Builder testChannel = channel("TestChannel").owner("channel");
		Tag.Builder testTag1 = tag("TestTag1").owner("channel");
		Tag.Builder testTag2 = tag("TestTag2").owner("channel");
		try {
			// ensure that the tag exist.
			client.set(testTag1);
			client.set(testTag2);
			client.set(testChannel.with(testTag1));
			Channel retChannel = client.getChannel(testChannel.build()
					.getName());
			// check for no initial properties or tags
			assertTrue(retChannel.getTags().contains(testTag1.build()));
			// uses the POST method
			testChannel = channel("TestChannel").owner("channel");
			client.update(testChannel.with(testTag2));
			assertTrue(client.getChannel(testChannel.build().getName())
					.getTags().contains(testTag1.build()));
			assertTrue(client.getChannel(testChannel.build().getName())
					.getTags().contains(testTag2.build()));
		} finally {
			client.delete(testChannel);
			client.deleteTag("TestTag1");
			client.deleteTag("TestTag2");
			assertTrue("CleanUp failed",
					client.getAllChannels().size() == channelCount);
		}

	}

	/**
	 * Test destructive _set()_ : existing channel is completely replaced
	 */
	@Test
	public void setChannel() {
		Channel.Builder oldChannel = channel("old").owner("channel").with(
				tag("oldTag").owner("channel"));
		Channel.Builder newChannel = channel("old").owner("channel").with(
				tag("newTag").owner("channel"));
		client.set(tag("oldTag").owner("channel"));
		client.set(tag("newTag").owner("channel"));
		try {
			client.set(oldChannel);
			assertTrue(client.findByTag("oldTag").contains(oldChannel.build()));
			client.set(newChannel);
			assertTrue(!client.findByTag("oldTag").contains(oldChannel.build()));
			assertTrue(client.findByTag("newTag").contains(newChannel.build()));
		} finally {
			client.deleteTag("oldTag");
			client.deleteTag("newTag");
			client.delete(newChannel);
		}
	}

	/**
	 * update and delete a Tag from a single channel
	 */
	@Test
	public void updateDeleteTag2Channel() {
		String channelName = "TestChannel";
		String tagName = "TestTag";
		client.set(tag(tagName).owner("channel"));
		client.set(channel(channelName).owner("channel"));
		assertTrue(!getTagNames(client.getChannel(channelName)).contains(
				tagName));
		client.update(tag(tagName, "channel"), channelName);
		assertTrue(getTagNames(client.getChannel(channelName))
				.contains(tagName));
		client.delete(tag(tagName), channelName);
		assertTrue(!getTagNames(client.getChannel(channelName)).contains(
				tagName));
		client.delete(channel(channelName));
		assertTrue("CleanUp failed",
				!client.getAllChannels().contains(channel(channelName).build()));
		client.deleteTag(tagName);
	}

	/**
	 * Update multiple channels with a _tag_
	 * Delete a tag from multiple channels
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void updateDeleteTag2Channels() {
		Tag.Builder tag = tag("tag").owner("channel");
		Collection<Channel.Builder> channelSet = new HashSet<Channel.Builder>();
		Collection<Channel.Builder> channelSubSet = new HashSet<Channel.Builder>();
		channelSubSet.add(channel("first").owner("channel"));
		channelSubSet.add(channel("second").owner("channel"));
		channelSet.addAll(channelSubSet);
		channelSet.add(channel("third").owner("channel"));

		try {
			client.set(channelSet);
			client.set(tag);
			client.update(tag, getChannelNames(toChannels(channelSet)));
			assertTrue(client.findByTag(tag.build().getName()).containsAll(
					toChannels(channelSet)));
			client.delete(tag, getChannelNames(toChannels(channelSubSet)));
			Collection<Channel.Builder> diffSet = new HashSet<Channel.Builder>(
					channelSet);
			diffSet.removeAll(channelSubSet);
			assertTrue(client.findByTag(tag.build().getName()).containsAll(
					toChannels(diffSet)));
			// this method is not atomic
		} catch (ChannelFinderException e) {
			fail(e.getMessage());
		} finally {
			client.delete(channelSet);
		}

	}

	/**
	 * Remove Tag from all channels
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void deleteTag() {
		Collection<Channel.Builder> channels = new HashSet<Channel.Builder>();
		channels.add(channel("first").owner("channel"));
		channels.add(channel("second").owner("channel"));
		try {
			client.set(channels);
			Tag.Builder tag = tag("TestTag").owner("channel");
			client.set(tag);
			client.update(tag, getChannelNames(toChannels(channels)));
			assertTrue(client.findByTag("TestTag").size() > 0);
			client.deleteTag("TestTag");
			assertTrue(client.findByTag("TestTag").size() == 0);
		} finally {
			client.delete(channels);
		}
	}

	/**
	 * test the destructive setting of tags on a single channel and a set of
	 * channels
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void setTag() {
		Collection<Channel.Builder> channelSet1 = new HashSet<Channel.Builder>();
		channelSet1.add(channel("first").owner("channel"));
		channelSet1.add(channel("second").owner("channel"));
		Collection<Channel.Builder> channelSet2 = new HashSet<Channel.Builder>();
		channelSet2.add(channel("third").owner("channel"));
		channelSet2.add(channel("forth").owner("channel"));
		Tag.Builder tag = tag("TestTag").owner("channel");

		try {
			client.set(channelSet2);
			client.set(channelSet1);
			client.set(tag);
			// add tag to set1
			client.update(tag, getChannelNames(toChannels(channelSet1)));
			assertTrue(client.findByTag(tag.toXml().getName()).containsAll(
					toChannels(channelSet1)));
			// set the tag on channel first and remove it from every other
			// channel
			client.set(tag, "first");
			assertTrue(client.findByTag(tag.build().getName()).size() == 1
					&& client.findByTag(tag.build().getName()).contains(
							channel("first").build()));
			// add the tag to channelSet2 and remove it from every other channel
			client.set(tag, getChannelNames(toChannels(channelSet2)));
			assertTrue(client.findByTag(tag.build().getName()).size() == 2
					&& client.findByTag(tag.toXml().getName()).containsAll(
							toChannels(channelSet2)));
		} finally {
			client.delete(channelSet1);
			client.delete(channelSet2);
		}

	}

	/**
	 * Update and Delete a property from a single channel
	 */
	@Test
	public void updateDeleteProperty() {
		Channel.Builder testChannel = channel("TestChannel").owner("channel");
		Property.Builder property = property("TestProperty", "TestValue")
				.owner("channel");

		try {
			client.set(testChannel);
			client.set(property);
			client.update(property, testChannel.toXml().getName());
			Collection<Channel> result = client.findByProperty(property.build()
					.getName());
			assertTrue(result.contains(testChannel.build()));
			client.delete(property, testChannel.toXml().getName());
			assertTrue(client.findByProperty(property.toXml().getName()).size() == 0);
		} finally {
			client.delete(testChannel);
			client.deleteProperty("TestProperty");
		}

	}

	/**
	 * Update and Delete a property from multiple channels
	 */
	@Test
	public void updateDeleteProperty2Channels() {
		Collection<Channel.Builder> channels = new HashSet<Channel.Builder>();
		channels.add(channel("first").owner("channel"));
		channels.add(channel("second").owner("channel"));
		Property.Builder property = property("TestProperty", "TestValue")
				.owner("channel");
		try {
			client.set(channels);
			client.set(property);
			int initialCount = client.findByProperty(
					property.toXml().getName(), "*").size();
			client.update(property, getChannelNames(toChannels(channels)));
			assertTrue(client.findByProperty(property.toXml().getName(), "*")
					.containsAll(toChannels(channels)));
			client.delete(property, getChannelNames(toChannels(channels)));
			assertTrue(client.findByProperty(property.toXml().getName(), "*")
					.size() == initialCount);
		} finally {
			client.delete(channels);
		}
	}
	
	/**
	 * Update Property Values on a set of channels
	 */
	@Test
	public void updatePropertyValue(){
		String propertyName = "TestProperty";
		String initialPropertyValue = "TestValue";				
		Property.Builder property = property(propertyName, initialPropertyValue)
				.owner("channel");
		Collection<Channel.Builder> channels = new HashSet<Channel.Builder>();
		channels.add(channel("first").owner("channel"));
		channels.add(channel("second").owner("channel"));
		try{
			client.set(property);
			client.set(channels);
			// add the property to all channels, all the properties will have the same value
			client.update(property, getChannelNames(toChannels(channels)));
			Collection<Channel> result = client.findByProperty(propertyName, "*");
			for (Channel channel : result) {
				assertTrue(
						"Unexpected state of property: TestProperty",
						getProperty(channel, propertyName)
								.getValue()
								.equalsIgnoreCase(initialPropertyValue));
			}
			Map<String, String> channelValueMap = new HashMap<String, String>();
			for (Channel channel : result) {
				channelValueMap.put(channel.getName(), channel.getName()+"-uniqueValue");
			}
			// update the property to a set of channels, each channels specifies
			// it associated property calue in a channelValueMap
			client.update(property, channelValueMap);
			result = client.findByProperty(propertyName, "*");
			for (Channel channel : result) {
				assertTrue(
						"Failed to correctly update the property Value for channel "
								+ channel.getName(),
						getProperty(channel, propertyName).getValue()
								.equalsIgnoreCase(
										channelValueMap.get(channel.getName())));
			}

		} finally {
			client.delete(channels);
		}
		
	}
	
	/**
	 * test the destructive setting of properties on a single channel and a set
	 * of channels
	 */
	@Test
	public void setProperty() {
		Collection<Channel.Builder> channelSet1 = new HashSet<Channel.Builder>();
		channelSet1.add(channel("first").owner("channel"));
		channelSet1.add(channel("second").owner("channel"));
		Collection<Channel.Builder> channelSet2 = new HashSet<Channel.Builder>();
		channelSet2.add(channel("third").owner("channel"));
		channelSet2.add(channel("forth").owner("channel"));
		String propertyName = "TestProperty";
		Property.Builder property = property(propertyName, "TestValue").owner(
				"channel");
		try {
			client.set(channelSet1);
			client.set(channelSet2);
			client.update(property, getChannelNames(toChannels(channelSet1)));
			assertTrue(
					"Failed to added property to channelSet1",
					client.findByProperty(propertyName, "*").containsAll(
							toChannels(channelSet1)));
			client.set(property, "first");
			Collection<Channel> result = client.findByProperty(propertyName,
					"*");
			assertTrue("Failed to set the property", result.size() == 1
					&& getChannelNames(result).contains("first"));
			// set property with unique values for each channel
			Map<String, String> channelPropertyMap = new HashMap<String, String>();
			channelPropertyMap.put("third", "thirdValue");
			channelPropertyMap.put("forth", "forthValue");
			client.set(property, channelPropertyMap);
			result = client.findByProperty(propertyName, "*");
			assertTrue(
					"Failed to set property on multiple channels",
					result.size() == 2
							&& result.containsAll(toChannels(channelSet2)));
			// check the property values are correctly set
			for (Channel channel : result) {
				assertTrue(ChannelUtil
						.getProperty(channel, propertyName)
						.getValue()
						.equalsIgnoreCase(
								channelPropertyMap.get(channel.getName())));
			}
		} catch (Exception e) {
			
		} finally {
			client.delete(channelSet1);
			client.delete(channelSet2);
		}
	}
	
	/**
	 * Delete a Property
	 */
	@Test
	public void deleteProperty() {
		Collection<Channel.Builder> channels = new HashSet<Channel.Builder>();
		channels.add(channel("first").owner("channel"));
		channels.add(channel("second").owner("channel"));
		Property.Builder property = property("DeleteProp", "TestValue").owner(
				"channel");
		try {
			client.set(channels);
			client.set(property);
			client.update(property, getChannelNames(toChannels(channels)));
			assertTrue(client.findByProperty(property.toXml().getName(), "*")
					.size() == 2);
			client.deleteProperty(property.toXml().getName());
			assertTrue(client.findByProperty(property.toXml().getName(), "*")
					.size() == 0);
		} finally {
			client.delete(channels);
		}
	}

	/**
	 * check non-destructive addition of tags and channels
	 */
	@Test
	public void updateTagsProperty() {
		Channel.Builder testChannel = channel("testChannel")
				.owner("channel")
				.with(tag("existingTag").owner("channel"))
				.with(property("existingProperty", "propValue")
						.owner("channel"));
		try {
			client.set(tag("existingTag").owner("channel"));
			client.set(property("existingProperty", "propValue").owner(
					"channel"));

			client.set(testChannel);
			Channel result = (client.getChannel(testChannel.build().getName()));
			assertTrue(result.getTags().contains(
					tag("existingTag").owner("channel").build()));
			assertTrue(result.getProperties().contains(
					property("existingProperty", "propValue").build()));

			client.set(tag("newTag").owner("channel"));
			client.update(tag("newTag").owner("channel"), testChannel.build()
					.getName());

			client.set(property("newProperty", "newPropValue").owner("channel"));
			client.update(
					property("newProperty", "newPropValue").owner("channel"),
					testChannel.build().getName());

			result = (client.getChannel(testChannel.build().getName()));
			assertTrue(result.getTags().contains(
					tag("existingTag").owner("channel").build()));
			assertTrue(result.getTags().contains(
					tag("newTag").owner("channel").build()));
			assertTrue(result.getProperties().contains(
					property("existingProperty", "propValue").build()));
			assertTrue(result.getProperties().contains(
					property("newProperty", "newPropValue").build()));
		} finally {
			client.delete(testChannel);
			client.deleteTag("existingTag");
			client.deleteTag("newTag");
			client.deleteProperty("existingProperty");
			client.deleteProperty("newProperty");
		}
	}

	@Test
	public void getAllTags() {
		client.set(tag("TestTag", "channel"));
		assertTrue(client.getAllTags().contains("TestTag"));
		client.deleteTag("TestTag");
		assertTrue("TestTag clean up failed",
				!client.getAllTags().contains("TestTag"));
	}

	@Test
	public void getAllProperties() {
		String propertyName = "TestProperty";
		client.set(property(propertyName, "testValue").owner("channel"));
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
