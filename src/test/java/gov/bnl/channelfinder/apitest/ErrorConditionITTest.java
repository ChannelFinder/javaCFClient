/**
 * Copyright (C) 2010-2012 Brookhaven National Laboratory
 * Copyright (C) 2010-2012 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * All rights reserved. Use is subject to license terms.
 */
package gov.bnl.channelfinder.apitest;

import static gov.bnl.channelfinder.api.Channel.Builder.channel;
import static gov.bnl.channelfinder.api.Property.Builder.property;
import static gov.bnl.channelfinder.api.Tag.Builder.tag;
import static org.junit.Assert.assertTrue;
import gov.bnl.channelfinder.api.Channel;
import gov.bnl.channelfinder.api.ChannelFinderClient;
import gov.bnl.channelfinder.api.ChannelFinderClientImpl.CFCBuilder;
import gov.bnl.channelfinder.api.ChannelFinderException;
import gov.bnl.channelfinder.api.Property;
import gov.bnl.channelfinder.api.XmlChannel;

import java.util.Collection;
import java.util.HashSet;

import org.junit.BeforeClass;
import org.junit.Test;

public class ErrorConditionITTest {

	// TODO
	// test check if error with the correct messages are thrown.

	// @Test
	// public void addEmptyChannel() {
	// try {
	// ChannelFinderClient.getInstance().addChannel(channel(new XmlChannel()));
	// fail("Added an empty channel.");
	// } catch (ChannelFinderException e) {
	// assertTrue(true);
	// }
	//
	// }

	private static ChannelFinderClient client;

	@BeforeClass
	public static void setup() {
		client = CFCBuilder.serviceURL("https://192.168.1.23:8181/ChannelFinder").withHTTPAuthentication(true).username("admin").password("1234").create();
	}

	@Test
	public void addOrphanChannel() {
		try {
			client.set(channel("JustName"));
			assertTrue(false);
		} catch (ChannelFinderException e) {
			assertTrue(true);
		}	
	}

	@Test
	public void addOrphanTag() {
		client.set(tag("JustName"));
	}

	@Test
	public void addOrphanProperty() {
		client.set(property("JustName"));
	}

	/**
	 * Add a set of channels with one incorrect channel
	 */
	@Test
	public void addSetwithBadChannel() {
		Collection<Channel.Builder> channels = new HashSet<Channel.Builder>();
		channels.add(channel("name1").owner("owner"));
		channels.add(channel(""));
		// channel ch2 has empty name
		try {
			client.set(channels);
			assertTrue(false);
		} catch (ChannelFinderException e) {
			assertTrue(true);
		}

		// channel ch2 has name but no owner
		channels.clear();
		channels.add(channel("name1").owner("owner"));
		channels.add(channel("name2"));
		try {
			client.set(channels);
			assertTrue(false);
		} catch (ChannelFinderException e) {
			assertTrue(true);
		}
	}

	/**
	 * Try to delete a non-existent channel
	 */
	@Test
	public void removeNonExistentChannel() {
		try {
			client.deleteChannel("NonExistantChannel");
			assertTrue(false);
		} catch (ChannelFinderException e) {
			assertTrue(true);
		}
	}

	public void removeSetWithNonExistentChannel() {

	}

	/**
	 * Test detection of error condition - attempting to update a channel that
	 * does not exist
	 */
	@Test
	public void updateNonExistentChannel() {
		try {
			client.update(channel("NonExistantChannel"));
			assertTrue(true);
		} catch (ChannelFinderException e) {
			assertTrue(false);
		}
	}

	@Test
	public void addTag2NonExistentChannel() {
		try {
			client.set(tag("sometag", "boss"), "NonExistantChannel");
			assertTrue(false);
		} catch (ChannelFinderException e) {
			assertTrue(true);
		}
	}

	public void addProperty2NonExistentChannel() {
		try {
			client.set(property("someproperty", "boss"), "NonExistantChannel");
			assertTrue(false);
		} catch (ChannelFinderException e) {
			assertTrue(true);
		}
	}

	/**
	 * Cannot create a channel with property value null
	 */
	@Test
	public void addPropertyWithNullValue() {
		Property.Builder property = property("testProperty").owner("owner");
		Channel.Builder channel = channel("testChannel").owner("owner").with(property);
		try {
			client.set(property);
			client.set(channel);
		} catch (ChannelFinderException e) {
			assertTrue(true);
		}
		finally {
			client.deleteProperty(property.toXml().getName());
		}
	}

	/**
	 * Cannot create a channel with property value ""
	 */
	@Test
	public void addPropertyWithEmptyValue() {
		Property.Builder property = property("testProperty").owner("owner").value("");
		Channel.Builder channel = channel("testChannel").owner("owner").with(property);
		try {
			client.set(property);
			client.set(channel);
		}  catch (ChannelFinderException e) {
			assertTrue(true);
		}finally {
			client.deleteProperty(property.toXml().getName());
		}
	}

	@Test
	public void updateChannelWithEmptyProperty() {
		Property.Builder property = property("testProperty").owner("owner").value("");
		Channel.Builder channel = channel("testChannel").owner("owner").with(property);
		try {
			client.set(channel);
		}  catch (ChannelFinderException e) {
			assertTrue(true);
		}
	}
}
