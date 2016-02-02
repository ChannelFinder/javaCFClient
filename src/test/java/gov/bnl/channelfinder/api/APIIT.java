/**
 * Copyright (C) 2010-2012 Brookhaven National Laboratory
 * Copyright (C) 2010-2012 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * All rights reserved. Use is subject to license terms.
 */
package gov.bnl.channelfinder.api;

import static gov.bnl.channelfinder.api.Channel.Builder.channel;
import static gov.bnl.channelfinder.api.ChannelUtil.getChannelNames;
import static gov.bnl.channelfinder.api.ChannelUtil.getProperty;
import static gov.bnl.channelfinder.api.ChannelUtil.getTagNames;
import static gov.bnl.channelfinder.api.ChannelUtil.toChannels;
import static gov.bnl.channelfinder.api.Property.Builder.property;
import static gov.bnl.channelfinder.api.Tag.Builder.tag;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sun.jersey.api.client.ClientResponse.Status;

import gov.bnl.channelfinder.api.Channel.Builder;
import gov.bnl.channelfinder.api.ChannelFinderClientImpl.CFCBuilder;

public class APIIT {
	private static ChannelFinderClient client;
	private static int channelCount;
	private static Property.Builder propertyapi;
	private static Tag.Builder tagapi;
	private static Channel.Builder channelapi;

	@Mock
	private static ChannelFinderClient reader;
	@Mock
	private static ChannelFinderClient writer;

	@Rule
	public ExpectedException exception = ExpectedException.none();
	private Builder channelSet;

	@BeforeClass
	public static void beforeTests() {
		// ChannelFinderClient.resetPreferences();
		// client = ChannelFinderClient.getInstance();
		//	client = CFCBuilder.serviceURL().withHTTPAuthentication(true).create();
		propertyapi = property("propertyapiname", "propertyapivalue");
		tagapi = tag("tagapiname", "tagapiowner");
		client = CFCBuilder.serviceURL("https://localhost:9191/ChannelFinder").withHTTPAuthentication(true).username("admin").password("1234").create();
		client.set(tagapi);
		client.set(propertyapi);
		channelCount = client.findByName("*").size();
	}

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void compositeClientTest() {		
		ChannelFinderClientComp composite = ChannelFinderClientComp.getInstance();
		composite.setReader(reader);
		composite.setWriter(writer);

		String ch = "channelName";

		composite.set(channel(ch).owner("APIIT").with(tagapi).with(propertyapi));
		verify(reader, times(0)).set(any(Channel.Builder.class));
		verify(writer, times(1)).set(any(Channel.Builder.class));

		composite.getChannel(ch);
		verify(reader, times(1)).getChannel(ch);
		verify(writer, times(0)).getChannel(ch);
		
		composite.update(channel(ch).owner("APIIT").with(tagapi).with(propertyapi));
		verify(reader, times(0)).update(any(Channel.Builder.class));
		verify(writer, times(1)).update(any(Channel.Builder.class));

		composite.deleteChannel(ch);
		verify(reader, times(0)).deleteChannel(ch);
		verify(writer, times(1)).deleteChannel(ch);
	}

	/**
	 * set and delete a single channel
	 */
	@Test
	public void setDeleteChannelTest() {
		String channelName = "TestChannelName";
		try {
			// Add a channel
			client.set(channel(channelName).with(propertyapi).with(tagapi).owner("channel"));
			client.getChannel(channelName);
			// Remove a channel
			client.deleteChannel(channelName);
			Collection<Channel> result = client.findByName("*");
			assertTrue(result == null || !result.contains(channel(channelName)));
			assertTrue("CleanUp failed", client.findByName("*").size() == channelCount);
		} catch (ChannelFinderException e) {
			if (e.getStatus().equals(Status.NOT_FOUND))
				fail("Channel not added. " + e.getMessage());
		} finally {

		}
	}

	@Test
	public void setDeleteTagTest() {
		client.set(tag("setTag").owner("channel"));
		assertTrue("failed to create Tag: setTag ", client.getAllTags()
				.contains("setTag"));
		client.deleteTag("setTag");
		assertTrue("failed to delete Tag: setTag ", !client.getAllTags()
				.contains("setTag"));
	}

	@Test
	public void setDeletePropertTesty() {
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
	public void addRemoveChannelsTest() {
		Collection<Channel.Builder> channels = new HashSet<Channel.Builder>();
		channels.add(channel("first").with(propertyapi).with(tagapi).owner("channel"));
		channels.add(channel("second").with(propertyapi).with(tagapi).owner("channel"));
		try {
			client.set(channels);
			assertTrue(client.findByName("*").containsAll(toChannels(channels)));
		} catch (ChannelFinderException e) {
			fail("Failed to add channels first and/or second \n Cause:"
					+ e.getMessage());
		} finally {
			client.delete(channels);
			assertTrue("CleanUp failed",
					client.findByName("*").size() == channelCount);
		}
	}

	/**
	 * update an existing channel with a new property and new tag
	 * 
	 * Add/update test
	 */
	@Test
	public void updateChannel() {
		Channel.Builder testChannel = channel("TestChannel").with(propertyapi).with(tagapi).owner("channel");
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
			testChannel = channel("TestChannel").with(propertyapi).owner("channel");
			client.update(testChannel.with(testTag2));
			assertTrue(client.getChannel(testChannel.build().getName())
					.getTags().contains(testTag1.build()));
			assertTrue(client.getChannel(testChannel.build().getName())
					.getTags().contains(testTag2.build()));
		} finally {
			client.deleteChannel(testChannel.build().getName());
			client.deleteTag("TestTag1");
			client.deleteTag("TestTag2");
			assertTrue("CleanUp failed",
					client.findByName("*").size() == channelCount);
		}

	}

	/**
	 * Test destructive _set()_ : existing channel is completely replaced
	 */
	@Test
	public void setChannel() {
		Channel.Builder oldChannel = channel("old").with(propertyapi).owner("channel").with(
				tag("oldTag").owner("channel"));
		Channel.Builder newChannel = channel("old").with(propertyapi).owner("channel").with(
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
			client.deleteChannel(newChannel.build().getName());
			client.deleteTag("oldTag");
			client.deleteTag("newTag");
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
		client.set(channel(channelName).with(propertyapi).with(tagapi).owner("channel"));
		assertTrue(!getTagNames(client.getChannel(channelName)).contains(
				tagName));
		client.update(tag(tagName, "channel"), channelName);
		assertTrue(getTagNames(client.getChannel(channelName))
				.contains(tagName));
		client.delete(tag(tagName), channelName);
		assertTrue(!getTagNames(client.getChannel(channelName)).contains(
				tagName));
		client.deleteChannel(channelName);
		assertTrue("CleanUp failed",
				!client.findByName("*").contains(channel(channelName).build()));
		client.deleteTag(tagName);
	}

	/**
	 * Update multiple channels with a _tag_ Delete a tag from multiple channels
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void updateDeleteTag2Channels() {
		Tag.Builder tag = tag("tag").owner("channel");
		Collection<Channel.Builder> channelSet = new HashSet<Channel.Builder>();
		Collection<Channel.Builder> channelSubSet = new HashSet<Channel.Builder>();
		channelSubSet.add(channel("first").with(propertyapi).with(tagapi).owner("channel"));
		channelSubSet.add(channel("second").with(propertyapi).with(tagapi).owner("channel"));
		channelSet.addAll(channelSubSet);
		channelSet.add(channel("third").with(propertyapi).with(tagapi).owner("channel"));

		try {
			client.set(channelSet);
			client.set(tag);
			client.update(tag, getChannelNames(toChannels(channelSet)));
			assertTrue(client.findByTag(tag.build().getName()).containsAll(
					toChannels(channelSet)));
			client.delete(tag, getChannelNames(toChannels(channelSubSet)));
			client.delete(tag, "first");
			Collection<Channel.Builder> diffSet = new HashSet<Channel.Builder>(
					channelSet);
			diffSet.removeAll(channelSubSet);
			assertTrue(client.findByTag(tag.build().getName()).containsAll(
					toChannels(diffSet)));
			// this method is not atomic
		} catch (ChannelFinderException e) {
			fail(e.getMessage());
		} finally {
			client.deleteTag(tag.build().getName());
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
		channels.add(channel("first").with(propertyapi).with(tagapi).owner("channel"));
		channels.add(channel("second").with(propertyapi).with(tagapi).owner("channel"));
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
		channelSet1.add(channel("first").with(propertyapi).with(tagapi).owner("channel"));
		channelSet1.add(channel("second").with(propertyapi).with(tagapi).owner("channel"));
		Collection<Channel.Builder> channelSet2 = new HashSet<Channel.Builder>();
		channelSet2.add(channel("third").with(propertyapi).with(tagapi).owner("channel"));
		channelSet2.add(channel("forth").with(propertyapi).with(tagapi).owner("channel"));
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
		Channel.Builder testChannel = channel("TestChannel").with(propertyapi).with(tagapi).owner("channel");
		Property.Builder property = property("TestProperty", "TestValue").owner("channel");
		try {
			client.set(testChannel);
			client.set(property);
			client.update(property, testChannel.toXml().getName());
			Collection<Channel> result = client.findByProperty(property.build().getName());
			assertTrue(result.contains(testChannel.build()));
			client.delete(property, testChannel.toXml().getName());
			assertTrue(client.findByProperty(property.toXml().getName()).size() == 0);
		} finally {
			client.deleteChannel(testChannel.build().getName());
			client.deleteProperty("TestProperty");
		}

	}

	/**
	 * Update and Delete a property from multiple channels
	 */
	@Test
	public void updateDeleteProperty2Channels() {
		Collection<Channel.Builder> channels = new HashSet<Channel.Builder>();
		channels.add(channel("first").with(propertyapi).with(tagapi).owner("channel"));
		channels.add(channel("second").with(propertyapi).with(tagapi).owner("channel"));
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
	public void updatePropertyValue() {
		String propertyName = "TestProperty";
		String initialPropertyValue = "TestValue";
		Property.Builder property = property(propertyName, initialPropertyValue)
				.owner("channel");
		Collection<Channel.Builder> channels = new HashSet<Channel.Builder>();
		channels.add(channel("first").with(propertyapi).with(tagapi).owner("channel"));
		channels.add(channel("second").with(propertyapi).with(tagapi).owner("channel"));
		try {
			client.set(property);
			client.set(channels);
			// add the property to all channels, all the properties will have
			// the same value
			client.update(property, getChannelNames(toChannels(channels)));
			Collection<Channel> result = client.findByProperty(propertyName,
					"*");
			for (Channel channel : result) {
				assertTrue("Unexpected state of property: TestProperty",
						getProperty(channel, propertyName).getValue()
								.equalsIgnoreCase(initialPropertyValue));
			}
			// update with a property object without any value would add the
			// property to
			// the channel if it does noe exist, if the property does exist it
			// is unaffected.
			Builder ch3 = channel("third").owner("channel").with(propertyapi).with(tagapi);
			channels.add(ch3);
			client.set(ch3);

			// update with a property object with a new value, this should add
			// the
			// property to the channel if it does not exist, in all cases the
			// new value of the property will be used
			client.update(property(propertyName, "newValue"),
					getChannelNames(toChannels(channels)));
			result = client.findByProperty(propertyName, "*");
			for (Channel channel : result) {
				assertTrue("Unexpected state of property: TestProperty",
						getProperty(channel, propertyName).getValue()
								.equalsIgnoreCase("newValue"));
			}
			Map<String, String> channelValueMap = new HashMap<String, String>();
			for (Channel channel : result) {
				channelValueMap.put(channel.getName(), channel.getName()
						+ "-uniqueValue");
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
			client.deleteProperty(propertyName);
		}

	}

	/**
	 * Test set a common property on a set of channels all channels should have
	 * the same property with the same value
	 */
	@Test
	public void setCommonProperty() {
		Collection<Channel.Builder> channelSet = new HashSet<Channel.Builder>();
		channelSet.add(channel("first").with(propertyapi).with(tagapi).owner("channel"));
		channelSet.add(channel("second").with(propertyapi).with(tagapi).owner("channel"));
		Property.Builder property = property("CommonProperty", "CommonValue")
				.owner("channel");
		try {
			client.set(channelSet);
			client.set(property, getChannelNames(toChannels(channelSet)));
			assertTrue(
					"failed to set the common property",
					getChannelNames(
							client.findByProperty("CommonProperty",
									"CommonValue")).containsAll(
							getChannelNames(toChannels(channelSet))));
		} finally {
			client.deleteProperty("CommonProperty");
			client.delete(channelSet);
		}
	}

	/**
	 * test the destructive setting of properties on a single channel and a set
	 * of channels
	 */
	@Test
	public void setProperty() {
		Collection<Channel.Builder> channelSet1 = new HashSet<Channel.Builder>();
		channelSet1.add(channel("first").with(propertyapi).with(tagapi).owner("channel"));
		channelSet1.add(channel("second").with(propertyapi).with(tagapi).owner("channel"));
		Collection<Channel.Builder> channelSet2 = new HashSet<Channel.Builder>();
		channelSet2.add(channel("third").with(propertyapi).with(tagapi).owner("channel"));
		channelSet2.add(channel("forth").with(propertyapi).with(tagapi).owner("channel"));
		String propertyName = "TestProperty";
		Property.Builder property = property(propertyName, "TestValue").owner(
				"channel");
		try {
			client.set(property);
			client.set(channelSet1);
			client.set(channelSet2);
			client.set(property, "first");
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
			fail(e.getMessage());
		} finally {
			client.deleteProperty(propertyName);
			client.delete(channelSet1);
			client.delete(channelSet2);
		}
	}

	/**
	 * Set a Property on a single Channel, other channels with the same property
	 * must remain unaffected
	 * 
	 * TODO incorporate test into previous test, should be removed
	 */
	@Test
	public void setChannelProperty() {
		Collection<Channel.Builder> channelSet = new HashSet<Channel.Builder>();
		channelSet.add(channel("first").with(propertyapi).with(tagapi).owner("channel"));
		channelSet.add(channel("second").with(propertyapi).with(tagapi).owner("channel"));
		channelSet.add(channel("third").with(propertyapi).with(tagapi).owner("channel"));
		channelSet.add(channel("forth").with(propertyapi).with(tagapi).owner("channel"));
		String propertyName = "TestProperty";
		Property.Builder property = property(propertyName, "TestValue").owner(
				"channel");
		try {
			client.set(channelSet);
			client.set(property, "first");
			assertTrue("failed added a property to a single channel: first",
					getChannelNames(client.findByProperty(propertyName, "*"))
							.contains("first"));
			client.set(property, "third");
			assertTrue(
					"Failed to add a property in a non destructive manner to channel: third",
					getChannelNames(client.findByProperty(propertyName, "*"))
							.contains("third"));

		} finally {
			client.deleteProperty(propertyName);
			client.delete(channelSet);
		}
	}

	/**
	 * Delete a Property
	 */
	@Test
	public void deleteProperty() {
		Collection<Channel.Builder> channels = new HashSet<Channel.Builder>();
		channels.add(channel("first").with(propertyapi).with(tagapi).owner("channel"));
		channels.add(channel("second").with(propertyapi).with(tagapi).owner("channel"));
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
			client.update(tag("newTag").owner("channel"), 
					testChannel.build().getName());

			client.set(property("newProperty").owner("channel"));
			client.update(property("newProperty", "newPropValue").owner("channel"),	testChannel.build().getName());

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
			client.deleteChannel(testChannel.build().getName());
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
