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
@JsonRootName("properties") 
public class JSONProperties {
    private Collection<JSONProperty> properties = new ArrayList<JSONProperty>();
  
    /** Creates a new instance of JSONProperties. */
    public JSONProperties() {
    }

    /** Creates a new instance of JSONProperties with one initial property.
     * @param p initial element
     */
    public JSONProperties(JSONProperty p) {
        properties.add(p);
    }

    /**
     * Returns a collection of JSONProperty.
     *
     * @return a collection of JSONProperty
     */
    @JsonProperty("property")
    public Collection<JSONProperty> getProperties() {
        return properties;
    }

    /**
     * Sets the collection of properties.
     *
     * @param items new property collection
     */
    @JsonProperty("properties")
    public void setProperties(Collection<JSONProperty> items) {
        this.properties = items;
    }

    /**
     * Adds a property to the property collection.
     *
     * @param item the JSONProperty to add
     */
    public void addJSONProperty(JSONProperty item) {
        this.properties.add(item);
    }

    /**
     * Creates a compact string representation for the log.
     *
     * @param data JSONChannel to create the string representation for
     * @return string representation
     */
    public static String toLog(JSONProperties data) {
        if (data.getProperties().size() == 0) {
            return "[None]";
        } else {
            StringBuilder s = new StringBuilder();
            s.append("[");
            for (JSONProperty p : data.getProperties()) {
                s.append(JSONProperty.toLog(p) + ",");
            }
            s.delete(s.length()-1, s.length());
            s.append("]");
            return s.toString();
        }
    }
}
