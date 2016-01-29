/**
 * Copyright (C) 2010-2012 Brookhaven National Laboratory
 * Copyright (C) 2010-2012 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * All rights reserved. Use is subject to license terms.
 */
package gov.bnl.channelfinder.api;

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
@JsonRootName("tag") 
public class JSONTag {
	private String name = null;
    private String owner = null;
    private JSONChannels channels = null;

    /**
     * Creates a new instance of JSONTag.
     *
     */
    public JSONTag() {
    }

    /**
     * Creates a new instance of JSONTag.
     *
     * @param name
     */
    public JSONTag(String name) {
        this.name = name;
    }

    /**
     * Creates a new instance of JSONTag.
     *
     * @param name
     * @param owner
     */
    public JSONTag(String name, String owner) {
        this.name = name;
        this.owner = owner;
    }

    /**
     * Getter for tag name.
     *
     * @return tag name
     */
    @JsonProperty("@name")
    public String getName() {
        return name;
    }

    /**
     * Setter for tag name.
     *
     * @param name tag name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }
    @JsonProperty("@name")
    public void setName1(String name) {
        this.name = name;
    }

    /**
     * Getter for tag owner.
     *
     * @return tag owner
     */
    @JsonProperty("@owner")
    public String getOwner() {
        return owner;
    }

    /**
     * Setter for tag owner.
     *
     * @param owner tag owner
     */
    @JsonProperty("owner")
    public void setOwner(String owner) {
        this.owner = owner;
    }
    @JsonProperty("@owner")
    public void setOwner1(String owner) {
        this.owner = owner;
    }

    /**
     * Getter for tag's JSONChannels.
     *
     * @return JSONChannels object
     */
    @JsonProperty("channels")
    public JSONChannels getJSONChannels() {
        return channels;
    }

    /**
     * Setter for tag's JSONChannels.
     *
     * @param channels JSONChannels object
     */
    @JsonProperty("xmlChannels")
    public void setJSONChannels(JSONChannels channels) {
        this.channels = channels;
    }
    @JsonProperty("channels")
    public void setJSONChannels1(JSONChannels channels) {
        this.channels = channels;
    }

    /**
     * Creates a compact string representation for the log.
     *
     * @param data the JSONTag to log
     * @return string representation for log
     */
    public static String toLog(JSONTag data) {
        if (data.channels == null) {
            return data.getName() + "(" + data.getOwner() + ")";
        } else {
            return data.getName() + "(" + data.getOwner() + ")"
                    + JSONChannels.toLog(data.channels);
        }
    }
}
