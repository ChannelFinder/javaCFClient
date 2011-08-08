package gov.bnl.channelfinder.api;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author shroffk
 * 
 */
public class Channel {

	private final String name;
	private final String owner;
	
	private final Map<String, Tag> tags;
	private final Map<String, Property> properties;

	public static class Builder {
		// required
		private String name;
		// optional
		private String owner;
		private Set<Tag.Builder> tags = new HashSet<Tag.Builder>();
		private Set<Property.Builder> properties = new HashSet<Property.Builder>();

		public static Builder channel(Channel channel) {
			Builder channelBuilder = new Builder();
			channelBuilder.name = channel.getName();
			channelBuilder.owner = channel.getOwner();
			for (Tag tag : channel.getTags()) {
				channelBuilder.tags.add(Tag.Builder.tag(tag));
			}
			for (Property property : channel.getProperties()) {
				channelBuilder.properties.add(Property.Builder
						.property(property));
			}
			return channelBuilder;
		}

		public static Builder channel(String name) {
			Builder channelBuilder = new Builder();
			channelBuilder.name = name;
			return channelBuilder;
		}

		public Builder owner(String owner) {
			this.owner = owner;
			return this;
		}

		public Builder with(Tag.Builder tag) {
			this.tags.add(tag);
			return this;
		}

		public Builder withTags(Collection<Tag.Builder> tags) {
			for (Tag.Builder tag : tags) {
				this.tags.add(tag);
			}
			return this;
		}

		public Builder with(Property.Builder property) {
			this.properties.add(property);
			return this;
		}

		public Builder withProperties(Collection<Property.Builder> properties) {
			for (Property.Builder property : properties) {
				this.properties.add(property);
			}
			return this;
		}

		XmlChannel toXml() {
			XmlChannel xmlChannel = new XmlChannel(name, owner);
			for (Tag.Builder tag : tags) {
				xmlChannel.addXmlTag(tag.toXml());
			}
			for (Property.Builder property : properties) {
				xmlChannel.addXmlProperty(property.toXml());
			}
			return xmlChannel;

		}

		Channel build() {
			return new Channel(this);
		}
	}

	Channel(XmlChannel channel) {
		this.name = channel.getName();
		this.owner = channel.getOwner();
		Map<String, Tag> newTags = new HashMap<String, Tag>();
		for (XmlTag tag : channel.getXmlTags().getTags()) {
			newTags.put(tag.getName(), new Tag(tag));
		}
		this.tags = Collections.unmodifiableMap(newTags);
		Map<String, Property> newProperties = new HashMap<String, Property>();
		for (XmlProperty property : channel.getXmlProperties().getProperties()) {
			newProperties.put(property.getName(), new Property(property));
		}
		this.properties = Collections.unmodifiableMap(newProperties);

	}

	private Channel(Builder builder) {
		this.name = builder.name;
		this.owner = builder.owner;
		Map<String, Tag> newTags = new HashMap<String, Tag>();
		for (Tag.Builder tag : builder.tags) {
			newTags.put(tag.build().getName(), tag.build());
		}
		this.tags = Collections.unmodifiableMap(newTags);
		Map<String, Property> newProperties = new HashMap<String, Property>();
		for (Property.Builder property : builder.properties) {
			newProperties.put(property.build().getName(), property.build());
		}
		this.properties = Collections.unmodifiableMap(newProperties);
	}

	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public String getOwner() {
		return owner;
	}

	public Collection<Tag> getTags() {
		return tags.values();
	}

	public Tag getTag(String tagName){
		return tags.get(tagName);
	}
	
	public Collection<Property> getProperties() {
		return properties.values();
	}

	public Property getProperty(String propertyName) {
		return properties.get(propertyName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Channel))
			return false;
		Channel other = (Channel) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Channel [name=" + name + ", owner=" + owner + "]";
	}

}
