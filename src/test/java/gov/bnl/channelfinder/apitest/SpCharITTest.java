/**
 * Copyright (C) 2010-2012 Brookhaven National Laboratory
 * Copyright (C) 2010-2012 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * All rights reserved. Use is subject to license terms.
 */
package gov.bnl.channelfinder.apitest;

import static gov.bnl.channelfinder.api.Channel.Builder.channel;
import static gov.bnl.channelfinder.api.Property.Builder.*;
import static gov.bnl.channelfinder.api.Tag.Builder.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import gov.bnl.channelfinder.api.Channel;
import gov.bnl.channelfinder.api.ChannelFinderClient;
import gov.bnl.channelfinder.api.ChannelFinderClientImpl.CFCBuilder;
import gov.bnl.channelfinder.api.ChannelFinderException;
import gov.bnl.channelfinder.api.RawLoggingFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class SpCharITTest {

	private static ChannelFinderClient client;

	private final String owner = "channel";

	@BeforeClass
	public static void setup() {
		try {
			client = CFCBuilder.serviceURL("https://192.168.1.23:8181/ChannelFinder").withHTTPAuthentication(true).username("admin").password("1234").create();

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@AfterClass
	public static void teardown() {
		if (client != null) {
			client.close();
		}

	}

	@Test
	public void channelsResourceRegExTest() {
		Collection<String> validSplNames = new ArrayList<String>();
		Collection<String> invalidSplNames = new ArrayList<String>();

		validSplNames.add("abc");
		validSplNames.add("sim://noise");
		validSplNames.add("epics://with_underscore");
		validSplNames.add("epics://with-dash");
		validSplNames.add("epics://with.dot");
		validSplNames.add("epics://with{braces}");
		validSplNames.add("epics://with[box]");
		validSplNames.add("epics://with@at");
		validSplNames.add("epics://Chars:in[epics-3.14{Naming_Con}]");
		Logger.getLogger(RawLoggingFilter.class.getName()).setLevel(Level.OFF);

		for (String channelName : validSplNames) {
			Logger.getLogger(RawLoggingFilter.class.getName()).setLevel(
					Level.ALL);
			try {
				client.set(channel(channelName).owner(owner));
				Channel ch = client.getChannel(channelName);
				assertTrue("Failed to find channel " + channelName,
						ch.equals(channel(channelName).build()));
			} catch (ChannelFinderException e) {
				fail(e.getMessage());
			} finally {
				client.deleteChannel(channelName);
			}
		}
	}

	/**
	 * Checks for outcome of a multichannel put with one invalid channel
	 */
	public void checkMultiChannelPut() {
		Collection<Channel.Builder> channels = new ArrayList<Channel.Builder>();
		for (int i = 0; i < 3; i++) {
			channels.add(channel("valid://ch" + String.valueOf(i)).owner(
					"cf-channels"));
		}
		channels.add(channel("invalid://with space").owner("cf-channels"));
		client.set(channels);

	}

	/**
	 * check the validity of the propertyName acceptable name are [^\s/]
	 */
	@Test
	public void propertiesResourceRegExTest() {
		Collection<String> validPropNames = new ArrayList<String>();

		validPropNames.add("prop123");
		validPropNames.add("prop:type");
		validPropNames.add("email@addr");
		validPropNames.add("prop-type[some]");

		for (String propName : validPropNames) {
			try {
				client.set(property(propName).owner("cf-properties"));
				assertTrue("Failed to set Property " + propName, client
						.getAllProperties().contains(propName));
			} catch (ChannelFinderException e) {
				fail("Failed to add property " + propName);
			} finally {
				client.deleteProperty(propName);
			}
		}

		Collection<String> inValidPropNames = new ArrayList<String>();

		inValidPropNames.add("");
		inValidPropNames.add("propName:/notAllowed");

		for (String propName : inValidPropNames) {
			try {
				client.set(property(propName).owner("cf-properties"));
				fail("Add property with invalid name: " + propName);
			} catch (ChannelFinderException e) {
				assertTrue(true);
			}
		}
	}

	/**
	 * Check a group property set with a invalid property fails
	 */
//	@Test(expected = ChannelFinderException.class)
//	public void checkMultiPropertyPut() {
//		Collection<Property.Builder> properties = new ArrayList<Property.Builder>();
//		for (int i = 0; i < 3; i++) {
//			properties.add(property("valid:prop"+String.valueOf(i)).owner("cf-properties"));
//		}
//		properties.add(property("invalid property\tName").owner("cf-properties"));
//		//client.set
//	}
	
	/**
	 * check the validity of the propertyName acceptable name are [^\s/]
	 */
	@Test
	public void tagsResourceRegExTest() {
		Collection<String> validTagNames = new ArrayList<String>();

		validTagNames.add("tag123");
		validTagNames.add("tag:type");
		validTagNames.add("email@addr");
		validTagNames.add("tag-type[some]");

		for (String tagName : validTagNames) {
			try {
				client.set(property(tagName).owner("cf-properties"));
				assertTrue("Failed to set Property " + tagName, client
						.getAllProperties().contains(tagName));
			} catch (ChannelFinderException e) {
				fail("Failed to add property " + tagName);
			} finally {
				client.deleteProperty(tagName);
			}
		}

		Collection<String> inValidTagNames = new ArrayList<String>();

		inValidTagNames.add("");
		inValidTagNames.add("tagName:/notAllowed");

		for (String tagName : inValidTagNames) {
			try {
				client.set(tag(tagName).owner("cf-properties"));
				fail("Add property with invalid name: " + tagName);
			} catch (ChannelFinderException e) {
				assertTrue(true);
			}
		}
	}
}
