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
@JsonRootName("property") 
public class JSONProperty {
    private String name = null;
    private String value = null;
    private String owner = null;
    private JSONChannels channels = null;

    /**
     * Creates a new instance of JSONProperty.
     *
     */
    public JSONProperty() {
    }

    /**
     * Creates a new instance of JSONProperty.
     *
     * @param name
     * @param owner
     */
    public JSONProperty(String name, String owner) {
        this.owner = owner;
        this.name = name;
    }

    /**
     * Creates a new instance of JSONProperty.
     *
     * @param name
     * @param owner
     * @param value
     */
    public JSONProperty(String name, String owner, String value) {
        this.value = value;
        this.owner = owner;
        this.name = name;
    }

    /**
     * Getter for property name.
     *
     * @return property name
     */
    @JsonProperty("@name")
    public String getName() {
        return name;
    }

    /**
     * Setter for property name.
     *
     * @param name property name
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
     * Getter for property value.
     *
     * @return property value
     */
    @JsonProperty("@value")
    public String getValue() {
        return value;
    }

    /**
     * Setter for property value.
     *
     * @param value property value
     */
    @JsonProperty("value")
    public void setValue(String value) {
        this.value = value;
    }
    @JsonProperty("@value")
    public void setValue1(String value) {
        this.value = value;
    }

    /**
     * Getter for property owner.
     *
     * @return property owner
     */
    @JsonProperty("@owner")
    public String getOwner() {
        return owner;
    }

    /**
     * Setter for property owner.
     *
     * @param owner property owner
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
     * Getter for property's JSONChannels.
     *
     * @return JSONChannels object
     */
    @JsonProperty("channels")
    public JSONChannels getJSONChannels() {
        return channels;
    }

    /**
     * Setter for property's JSONChannels.
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
     * @param data the JSONProperty to log
     * @return string representation for log
     */
    public static String toLog(JSONProperty data) {
         if (data.channels == null) {
            return data.getName() + "(" + data.getOwner() + ")";
        } else {
            return data.getName() + "(" + data.getOwner() + ")"
                    + JSONChannels.toLog(data.channels);
        }
    }
}
