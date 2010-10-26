package gov.bnl.channelfinder.api;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author shroffk
 *
 */
public class Channel {

	private final String name;
	private final String owner;
	private final Set<Tag> tags;
	private final Set<Property> properties;

	public static class Builder {
		// required
		private String name;
		// optional
		private String owner;
		private Set<Tag.Builder> tags = new HashSet<Tag.Builder>();
		private Set<Property.Builder> properties = new HashSet<Property.Builder>();

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
			tags.add(tag);
			return this;
		}

		public Builder with(Property.Builder property) {
			properties.add(property);
			return this;
		}

		XmlChannel toXml() {
			XmlChannel xmlChannel = new XmlChannel(name, owner);
			for (Tag.Builder tag : tags) {
				xmlChannel.addTag(tag.toXml());
			}
			for (Property.Builder property : properties) {
				xmlChannel.addProperty(property.toXml());
			}
			return xmlChannel;

		}
	}

	Channel(XmlChannel channel) {
		this.name = channel.getName();
		this.owner = channel.getOwner();
		Set<Tag> newTags = new HashSet<Tag>();
		for (XmlTag tag : channel.getXmlTags()) {
			newTags.add(new Tag(tag));
		}
		this.tags = Collections.unmodifiableSet(newTags);
		Set<Property> newProperties = new HashSet<Property>();
		for (XmlProperty property : channel.getXmlProperties()) {
			newProperties.add(new Property(property));
		}
		this.properties = Collections.unmodifiableSet(newProperties);

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
		return tags;
	}

	public Collection<Property> getProperties() {
		return properties;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/* (non-Javadoc)
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
		
}
