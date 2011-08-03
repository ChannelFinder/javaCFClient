package gov.bnl.channelfinder.api;

import static gov.bnl.channelfinder.api.Channel.Builder.channel;
import static gov.bnl.channelfinder.api.ChannelUtil.getProperty;
import static gov.bnl.channelfinder.api.ChannelUtil.getTag;
import static gov.bnl.channelfinder.api.Property.Builder.property;
import static gov.bnl.channelfinder.api.Tag.Builder.tag;
import static org.junit.Assert.assertTrue;
import gov.bnl.channelfinder.api.Channel.Builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

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

	@Test
	public void testFindTag() {
		int i = generator.nextInt(11);
		assertTrue("Failed to Find Tag",
				getTag(channel.build(), "tag" + String.valueOf(i)).getName()
						.equalsIgnoreCase("tag" + String.valueOf(i)));
	}

	@Test
	public void testFindProperty() {
		int i = generator.nextInt(11);
		assertTrue(
				"Failed to Find property",
				getProperty(channel.build(), "property" + String.valueOf(i))
						.getName().equalsIgnoreCase(
								"property" + String.valueOf(i))
						&& getProperty(channel.build(),
								"property" + String.valueOf(i)).getValue()
								.equalsIgnoreCase(String.valueOf(i)));
	}
}
