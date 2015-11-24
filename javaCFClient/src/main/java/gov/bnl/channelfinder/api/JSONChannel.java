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
@JsonRootName("channel") 
public class JSONChannel {
    private String name;
    private String owner;
    private JSONProperties properties = new JSONProperties();
    private JSONTags tags = new JSONTags();
  
    /** Creates a new instance of JSONChannel */
    public JSONChannel() {
    }

    /**
     * Creates a new instance of JSONChannel.
     *
     * @param name channel name
     */
    public JSONChannel(String name) {
        this.name = name;
    }

    /**
     * Creates a new instance of JSONChannel.
     *
     * @param name channel name
     * @param owner owner name
     */
    public JSONChannel(String name, String owner) {
        this.name = name;
        this.owner = owner;
    }

    /**
     * Getter for channel name.
     *
     * @return name
     */
    @JsonProperty("@name")
    public String getName() {
        return name;
    }

    /**
     * Setter for channel name.
     *
     * @param name the value to set
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
     * Getter for channel owner.
     *
     * @return owner
     */
    @JsonProperty("@owner")
    public String getOwner() {
        return owner;
    }

    /**
     * Setter for channel owner.
     *
     * @param owner
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
     * Getter for channel's JSONProperties.
     *
     * @return JSONProperties
     */
    @JsonProperty("properties")
    public JSONProperties getJSONProperties() {
        return properties;
    }

    /**
     * Setter for channel's JSONProperties.
     *
     * @param properties JSONProperties
     */
    @JsonProperty("xmlProperties")
    public void setJSONProperties(JSONProperties properties) {
        this.properties = properties;
    }
    
    @JsonProperty("properties")
    public void setJSONProperties1(JSONProperties properties) {
        this.properties = properties;
    }

    /**
     * Adds an JSONProperty to the channel.
     *
     * @param property single JSONProperty
     */
    public void addJSONProperty(JSONProperty property) {
        this.properties.addJSONProperty(property);
    }

    /**
     * Getter for the channel's JSONTags.
     *
     * @return JSONTags for this channel
     */
    @JsonProperty("tags")
    public JSONTags getJSONTags() {
        return tags;
    }

    /**
     * Setter for the channel's JSONTags.
     *
     * @param tags JSONTags
     */
    @JsonProperty("xmlTags")
    public void setJSONTags(JSONTags tags) {
        this.tags = tags;
    }
    
    @JsonProperty("tags")
    public void setJSONTags1(JSONTags tags) {
        this.tags = tags;
    }


    /**
     * Adds an JSONTag to the collection.
     *
     * @param tag
     */
    public void addJSONTag(JSONTag tag) {
        this.tags.addJSONTag(tag);
    }

    /**
     * Creates a compact string representation for the log.
     *
     * @param data JSONChannel to create the string representation for
     * @return string representation
     */
    public static String toLog(JSONChannel data) {
        return data.getName() + "(" + data.getOwner() + "):["
                + JSONProperties.toLog(data.properties)
                + JSONTags.toLog(data.tags)
                + "]";
    }
}

