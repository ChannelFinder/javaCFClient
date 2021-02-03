/**
 * Copyright (C) 2010-2012 Brookhaven National Laboratory
 * Copyright (C) 2010-2012 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * All rights reserved. Use is subject to license terms.
 */
package gov.bnl.channelfinder.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A Scroll object represents channel finder scroll.
 * Scroll has its id for requesting the next scroll and channels.
 *
 * @author Genie Jhang (changj@frib.msu.edu)
 * @author shroffk (Original author)
 */
public class Scroll {

	private final String id;

	private final Set<Channel> channels = new HashSet<>();

	/**
	 * Builder class to aid in a construction of a channel.
	 *
     * @author Genie Jhang (changj@frib.msu.edu)
	 * @author shroffk (Original author)
	 *
	 */
	public static class Builder {
		private String id;
		private Collection<Channel> channels;

		/**
		 * Create a scroll builder initialized to a copy of the scroll
		 *
		 * @param scroll
		 *            - the scroll to be copied
		 * @return scroll {@link Builder} with all the attributes copied from
		 *         the scroll
		 */
		public static Builder scroll(Scroll scroll) {
			Builder scrollBuilder = new Builder();
			scrollBuilder.id = scroll.getId();
			scrollBuilder.channels = scroll.getChannels();

			return scrollBuilder;
		}

		/**
		 * Create a scroll builder for a scroll with the given id
		 *
		 * @param id
		 *            - name of the channel you are creating
		 * @return scroll {@link Builder} with the scroll id set to id
		 */
		public static Builder scroll(String id) {
			Builder scrollBuilder = new Builder();
			scrollBuilder.id = id;

			return scrollBuilder;
		}

		/**
		 * Set id for the scroll to be created
		 *
		 * @param id
		 *            - string scroll id
		 * @return scroll {@link Builder} with id set to id
		 */
		public Builder id(String id) {
			this.id = id;

			return this;
		}

		/**
		 * Set a list of channels for the scroll to be created
		 *
		 * @param channels
		 *            - channel list in the scroll
		 * @return scroll {@link Builder} with the channels set to channels
		 */
		public Builder channels(Collection<Channel> channels) {
			this.channels = channels;

			return this;
		}

		/**
		 * build a {@link XmlScroll} object using this builder.
		 *
		 * @return a {@link XmlScroll}
		 */
		public XmlScroll toXml() {
			List<XmlChannel> xmlChannels = new ArrayList<>();
		    for (Channel channel : channels) {
				xmlChannels.add(Channel.Builder.channel(channel).toXml());
			}
			XmlScroll xmlScroll;
			xmlScroll = new XmlScroll(id, xmlChannels);

			return xmlScroll;
		}

		/**
		 * build a {@link Scroll} object using this builder.
		 *
		 * @return a {@link Scroll}
		 */
		public Scroll build() {
			return new Scroll(this);
		}
	}

	Scroll(XmlScroll scroll) {
		this.id = scroll.getId();
		for (XmlChannel xmlChannel : scroll.getXmlChannels()) {
			this.channels.add(new Channel(xmlChannel));
		}

	}

	private Scroll(Builder builder) {
		this.id = builder.id;
		this.channels.addAll(builder.channels);
	}

	/**
	 * Returns the id of the scroll.
	 * 
	 * @return scroll id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the channel list in the scroll
	 * 
	 * @return channel list in the scroll
	 */
	public Collection<Channel> getChannels() {
		return Collections.unmodifiableCollection(channels);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (!(obj instanceof Scroll)) { return false; }
		Scroll other = (Scroll) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		}
		else if (!id.equals(other.id)) { return false; }
		else if (channels.hashCode() != other.hashCode()) { return false; }

		return true;
	}

	@Override
	public String toString() {
		return "Scroll [id=" + id + ", channels=" + channels + "]";
	}

}
