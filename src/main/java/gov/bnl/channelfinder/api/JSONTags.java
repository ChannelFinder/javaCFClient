/**
 * Copyright (C) 2010-2012 Brookhaven National Laboratory
 * Copyright (C) 2010-2012 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * All rights reserved. Use is subject to license terms.
 */
package gov.bnl.channelfinder.api;

import java.util.ArrayList;
import java.util.Collection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonParseException;

/**
 * Tag object that can be represented as XML/JSON in payload data.
 *
 * @author Fedor Jia <fedordjia@outlook.com>
 */
@JsonRootName("tags") 
public class JSONTags {
    private Collection<JSONTag> tags = new ArrayList<JSONTag>();
  
    /** Creates a new instance of JSONTags. */
    public JSONTags() {
    }

    /** Creates a new instance of JSONTags with one initial tag.
     * @param t initial element
     */
    public JSONTags(JSONTag t) {
        tags.add(t);
    }

    /**
     * Returns a collection of JSONTag.
     *
     * @return a collection of JSONTag
     */
    @JsonProperty("tag")
    public Collection<JSONTag> getTags() {
        return tags;
    }

    /**
     * Sets the collection of tags.
     *
     * @param items new tag collection
     */
    @JsonProperty("tags")
    public void setTags(Collection<JSONTag> items) {
        this.tags = items;
    }

    /**
     * Adds a tag to the tag collection.
     *
     * @param item the JSONTag to add
     */
    public void addJSONTag(JSONTag item) {
        this.tags.add(item);
    }

    /**
     * Creates a compact string representation for the log.
     *
     * @param data JSONTags to create the string representation for
     * @return string representation
     */
    public static String toLog(JSONTags data) {
        if (data.getTags().size() == 0) {
            return "[None]";
        } else {
            StringBuilder s = new StringBuilder();
            s.append("[");
            for (JSONTag t : data.getTags()) {
                s.append(JSONTag.toLog(t) + ",");
            }
            s.delete(s.length()-1, s.length());
            s.append("]");
            return s.toString();
        }
    }
}
