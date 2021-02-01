/**
 * Copyright (C) 2010-2012 Brookhaven National Laboratory
 * Copyright (C) 2010-2012 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * All rights reserved. Use is subject to license terms.
 */
package gov.bnl.channelfinder.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("scroll")
public class XmlScroll {
    private String id;
    private List<XmlChannel> xmlChannels = new ArrayList<XmlChannel>();
    
    /**
     * Creates a new instance of XmlScroll.
     *
     */
    public XmlScroll() {
    }
    
    /**
     * Creates a new instance of XmlScroll.
     *
     * @param id - scroll name
     * @param xmlChannels - list of channels
     */
    public XmlScroll(String id, List<XmlChannel> xmlChannels) {
        super();
        this.id = id;
        this.xmlChannels = xmlChannels;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("channels")
    public List<XmlChannel> getXmlChannels() {
        return xmlChannels;
    }

    @JsonProperty("channels")
    public void setXmlChannels(List<XmlChannel> xmlChannels) {
        this.xmlChannels = xmlChannels;
    }
}