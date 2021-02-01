/**
 * Copyright (C) 2010-2012 Brookhaven National Laboratory
 * Copyright (C) 2010-2012 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * All rights reserved. Use is subject to license terms.
 */
package gov.bnl.channelfinder.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static gov.bnl.channelfinder.api.Tag.Builder.*;
import static gov.bnl.channelfinder.api.Property.Builder.*;
import static gov.bnl.channelfinder.api.Channel.Builder.*;
import static org.junit.Assert.assertTrue;

public class ChannelTest {

	private static Logger logger = Logger.getLogger(CFCManagerIT.class.getName());

	@BeforeClass
	public static void beforeTests() {
	}

	@AfterClass
	public static void afterTests() {
	}

	@Test
	public void getElementTest() {
		Collection<Tag.Builder> testTags;
		testTags = new ArrayList<Tag.Builder>();
		for (int i = 0; i < 10; i++) {
			testTags.add(tag("tag" + String.valueOf(i),
					"owner" + String.valueOf(i)));
		}
		Collection<Property.Builder> testProperties;
		testProperties = new ArrayList<Property.Builder>();
		for (int i = 0; i < 10; i++) {
			testProperties.add(property("property" + String.valueOf(i),
					String.valueOf(i)).owner("owner" + String.valueOf(i)));
		}
		Channel channel = channel("testChannel").withTags(testTags)
				.withProperties(testProperties).build();

		Random generator = new Random();
		int i = generator.nextInt(11);
		String propName = "property" + String.valueOf(i);
		Property prop = channel.getProperty(propName);
		Property expectedProp = property(propName, String.valueOf(i)).owner(
				"ignoreOwner").build();
		assertTrue(
				"Failed to find the expected property: "
						+ expectedProp.toString(), prop.equals(expectedProp));
		i = generator.nextInt(11);
		String tagName = "tag" + String.valueOf(i);
		Tag tag = channel.getTag(tagName);
		Tag expextedTag = tag(tagName).owner("ignoreOwner").build();
		assertTrue(
				"Failed to find the expected tag: " + expextedTag.toString(),
				tag.equals(expextedTag));
	}

}
