/**
 * 
 */
package gov.bnl.channelfinder.api;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * @author shroffk
 * 
 */
public class ChannelUtil {

	/**
	 * Return a list of tag names associated with this channel
	 * 
	 * @param channel
	 * @return
	 */
	public static Collection<String> getTagNames(Channel channel) {
		Collection<String> tagNames = new HashSet<String>();
		for (Tag tag : channel.getTags()) {
			tagNames.add(tag.getName());
		}
		return tagNames;
	}

	/**
	 * Return a union of tag names associated with channels
	 * @param channels
	 * @return
	 */
	public static Collection<String> getAllTagNames(Collection<Channel> channels) {
		Collection<String> tagNames = new HashSet<String>();
		for (Channel channel : channels) {
			tagNames.addAll(getTagNames(channel));
		}
		return tagNames;
	}

	/**
	 * Return a list of property names associated with this channel
	 * 
	 * @param channel
	 * @return
	 */
	public static Collection<String> getPropertyNames(Channel channel) {
		Collection<String> propertyNames = new HashSet<String>();
		for (Property property : channel.getProperties()) {
			propertyNames.add(property.getName());
		}
		return propertyNames;
	}

	/**
	 * Return a union of property names associated with channels
	 * @param channels
	 * @return
	 */
	public static Collection<String> getPropertyNames(
			Collection<Channel> channels) {
		Collection<String> propertyNames = new HashSet<String>();
		for (Channel channel : channels) {
			propertyNames.addAll(getPropertyNames(channel));
		}
		return propertyNames;
	}
	
	
}