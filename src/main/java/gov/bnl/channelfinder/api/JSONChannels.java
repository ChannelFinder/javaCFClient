/**
 * Copyright (C) 2010-2012 Brookhaven National Laboratory
 * Copyright (C) 2010-2012 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * All rights reserved. Use is subject to license terms.
 */
package gov.bnl.channelfinder.api;

import java.util.ArrayList;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Tag object that can be represented as XML/JSON in payload data.
 *
 * @author Fedor Jia <fedordjia@outlook.com>
 */
@JsonRootName("channels") 
public class JSONChannels {
    private Collection<JSONChannel> channels = new ArrayList<JSONChannel>();
  
    /** Creates a new instance of JSONChannels. */
    public JSONChannels() {
    }

    /** Creates a new instance of JSONChannels with one initial channel.
     * @param c initial element
     */
    public JSONChannels(JSONChannel c) {
        channels.add(c);
    }

    /**
     * Returns a collection of JSONChannel.
     *
     * @return a collection of JSONChannel
     */
    @JsonProperty("channel")
    public Collection<JSONChannel> getChannels() {
        return channels;
    }

    /**
     * Sets the collection of channels.
     *
     * @param items new channel collection
     */
    @JsonProperty("channels")
    public void setChannels(Collection<JSONChannel> items) {
        this.channels = items;
    }

    /**
     * Adds a channel to the channel collection.
     *
     * @param item the JSONChannel to add
     */
    public void addJSONChannel(JSONChannel item) {
        this.channels.add(item);
    }

    /**
     * Creates a compact string representation for the log.
     *
     * @param data JSONChannel to create the string representation for
     * @return string representation
     */
    public static String toLog(JSONChannels data) {
        if (data.getChannels().size() == 0) {
            return "[None]";
        } else {
            StringBuilder s = new StringBuilder();
            s.append("[");
            for (JSONChannel c : data.getChannels()) {
                s.append(JSONChannel.toLog(c) + ",");
            }
            s.delete(s.length()-1, s.length());
            s.append("]");
            return s.toString();
        }
    }
}
