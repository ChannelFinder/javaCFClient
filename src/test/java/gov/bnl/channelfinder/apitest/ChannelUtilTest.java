/**
 * Copyright (C) 2010-2012 Brookhaven National Laboratory
 * Copyright (C) 2010-2012 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * All rights reserved. Use is subject to license terms.
 */
package gov.bnl.channelfinder.apitest;

import static gov.bnl.channelfinder.api.Channel.Builder.channel;
import static gov.bnl.channelfinder.api.ChannelUtil.getProperty;
import static gov.bnl.channelfinder.api.ChannelUtil.getTag;
import static gov.bnl.channelfinder.api.Property.Builder.property;
import static gov.bnl.channelfinder.api.Tag.Builder.tag;
import static org.junit.Assert.assertTrue;
import gov.bnl.channelfinder.api.Channel;
import gov.bnl.channelfinder.api.Channel.Builder;
import gov.bnl.channelfinder.api.ChannelUtil;
import gov.bnl.channelfinder.api.Property;
import gov.bnl.channelfinder.api.Tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Sets;

public class ChannelUtilTest {

	private static Builder channel;
	private static Collection<Tag.Builder> tags = new ArrayList<Tag.Builder>();
	private static Collection<Property.Builder> properties = new ArrayList<Property.Builder>();

	private Random generator = new Random();

	@BeforeClass
	public static void beforeClass() {
		channel = channel("ChannelTest").owner("cf-Channel");
		// create 10 tags, properties
		for (int i = 0; i < 10; i++) {
			Tag.Builder tag = tag("tag" + String.valueOf(i), "cf-tag");
			tags.add(tag);
			Property.Builder property = property(
					"property" + String.valueOf(i), String.valueOf(i)).owner(
					"cf-property");
			properties.add(property);
			channel.with(tag).with(property);
		}

	}

	private Collection<Channel> getTestChannels() {
		Collection<Channel> channels = new ArrayList<Channel>();
		for (int i = 1; i <= 10; i++) {
			Builder channel = channel("testChannel" + i);
			channel.with(property("all", String.valueOf(i)));
			channel.with(tag("all"));
			if (i % 2 == 0) {
				channel.with(property("even", String.valueOf(i)));
				channel.with(tag("even"));
			}
			if (i % 5 == 0) {
				channel.with(property("five", String.valueOf(i)));
				channel.with(tag("five"));
			}
			channels.add(channel.build());
		}
		return channels;
	}

	// TODO more vigorous tests conditions.
	@Test
	public void testfilterChannelbyProperty() {
		Collection<Channel> channels = getTestChannels();
		Collection<Channel> results;

		// Filter by properties
		Collection<String> requiredProps = new ArrayList<String>();
		requiredProps.add("all");
		results = ChannelUtil.filterbyProperties(channels, requiredProps);
		assertTrue(
				"Query all channels with prop 'all' failed, expected 10 found "
						+ results.size(), results.size() == 10);
		requiredProps.add("even");
		results = ChannelUtil.filterbyProperties(channels, requiredProps);
		assertTrue(
				"Query all channels with prop 'even' failed, expected 5 found "
						+ results.size(), results.size() == 5);
		requiredProps.add("five");
		results = ChannelUtil.filterbyProperties(channels, requiredProps);
		assertTrue(
				"Query all channels with prop 'five' failed, expected 1 found "
						+ results.size(), results.size() == 1);
		requiredProps.add("noProp");
		results = ChannelUtil.filterbyProperties(channels, requiredProps);
		assertTrue(
				"Query all channels with prop 'noProp' failed, expected 0 found "
						+ results.size(), results.size() == 0);

		// Filter by Tags
		Collection<String> requiredTags = new ArrayList<String>();
		requiredTags.add("all");
		results = ChannelUtil.filterbyTags(channels, requiredTags);
		assertTrue(
				"Query all channels with tag 'all' failed, expected 10 found "
						+ results.size(), results.size() == 10);
		requiredTags.add("even");
		results = ChannelUtil.filterbyTags(channels, requiredTags);
		assertTrue(
				"Query all channels with tag 'even' failed, expected 5 found "
						+ results.size(), results.size() == 5);
		requiredTags.add("five");
		results = ChannelUtil.filterbyTags(channels, requiredTags);
		assertTrue(
				"Query all channels with tag 'five' failed, expected 1 found "
						+ results.size(), results.size() == 1);
		requiredTags.add("notag");
		results = ChannelUtil.filterbyTags(channels, requiredTags);
		assertTrue(
				"Query all channels with tag 'notag' failed, expected 0 found "
						+ results.size(), results.size() == 0);

		// Filter by properties and tags
		requiredProps = new ArrayList<String>();
		requiredTags = new ArrayList<String>();
		requiredProps.add("all");
		requiredTags.add("all");
		results = ChannelUtil.filterbyElements(channels, requiredProps,
				requiredTags);
		assertTrue(
				"Query all channels with tag 'all' && prop 'all' failed, expected 10 found "
						+ results.size(), results.size() == 10);
		requiredProps.add("even");
		requiredTags.add("even");
		results = ChannelUtil.filterbyElements(channels, requiredProps,
				requiredTags);
		assertTrue(
				"Query all channels with tag 'even' && prop 'even' failed, expected 5 found "
						+ results.size(), results.size() == 5);
		requiredProps.add("five");
		requiredTags.add("five");
		results = ChannelUtil.filterbyElements(channels, requiredProps,
				requiredTags);
		assertTrue(
				"Query all channels with tag 'five' && prop 'five' failed, expected 1 found "
						+ results.size(), results.size() == 1);
		requiredProps.add("noProp");
		requiredTags.add("notag");
		results = ChannelUtil.filterbyElements(channels, requiredProps,
				requiredTags);
		assertTrue(
				"Query all channels with tag 'noTag' && prop 'noProp'failed, expected 0 found "
						+ results.size(), results.size() == 0);

	}

	@Test
	public void testGetPropValues() {
		Collection<Channel> channels = getTestChannels();
		// property all has values 1-10
		assertTrue(
				"Failed to get all property values for property 'all' ",
				ChannelUtil.getPropValues(channels, "all").containsAll(
						new ArrayList<String>(Arrays.asList("1", "2", "3", "4",
								"5", "6", "7", "8", "9", "10"))));
		// property even has values 2,4,6,8,10
		assertTrue(
				"Failed to get all the property Values for property 'even' ",
				ChannelUtil.getPropValues(channels, "even").containsAll(
						new ArrayList<String>(Arrays.asList("2", "4", "6", "8",
								"10"))));
		// property five had values 5,10
		assertTrue(
				"Failed to get all the property Values for property 'five'",
				ChannelUtil.getPropValues(channels, "five").containsAll(
						new HashSet<String>(Sets.newHashSet("5", "10"))));
	}
}
