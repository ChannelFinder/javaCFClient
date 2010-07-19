package gov.bnl.channelfinder.api;

import gov.bnl.channelfinder.model.XmlChannel;
import gov.bnl.channelfinder.model.XmlChannels;
import gov.bnl.channelfinder.model.XmlProperty;
import gov.bnl.channelfinder.model.XmlTag;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.swing.text.html.parser.ParserDelegator;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.Arrays;

import java.util.prefs.*;

/**
 * TODO: make this not a singleton. Add a constructor to programmatically pass
 * the configuration.
 * 
 * @author shroffk
 * 
 */
public class ChannelFinderClient {
	private static ChannelFinderClient instance = new ChannelFinderClient();
	private WebResource service;
	private static Preferences preferences;
	private static Properties properties;

	/*
	 * 
	 * Input checking parse the inputs - check for existance of a owner for a
	 * tag before performing the tag operations.
	 * 
	 * property addition without owners predefined
	 * 
	 * refactor function names - query to find - (getchannel and getchannels ?)
	 */

	/**
	 * Create an instance of ChannelFinderClient
	 */
	private ChannelFinderClient() {

		preferences = Preferences.userNodeForPackage(ChannelFinderClient.class);
		// Use the properties file for defaults
		properties = new Properties();
		// FileInputStream in;
		InputStream is;
		try {
			String propertyFile = System
					.getProperty("channelfinder.properties");
			if (propertyFile != null) {
				is = new FileInputStream(propertyFile);
			} else {
				is = this.getClass().getResourceAsStream(
						"/channelfinder.properties");
			}
			properties.load(is);
			is.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// Authentication and Authorization configuration
		TrustManager mytm[] = null;
		SSLContext ctx = null;

		// trail
		try {
			mytm = new TrustManager[] { new MyX509TrustManager(
					preferences.get(
							"trustStore", properties.getProperty("trustStore")), //$NON-NLS-1$
					preferences
							.get(
									"trustPass", properties.getProperty("trustPass")).toCharArray()) }; //$NON-NLS-1$
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			ctx = SSLContext.getInstance(preferences.get(
					"protocol", properties.getProperty("protocol"))); //$NON-NLS-1$
			ctx.init(null, mytm, null);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ClientConfig config = new DefaultClientConfig();
		config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
				new HTTPSProperties(null, ctx));
		Client client = Client.create(config);
		client.addFilter(new HTTPBasicAuthFilter(preferences.get("username",
				properties.getProperty("username")), preferences.get(
				"password", properties.getProperty("password")))); //$NON-NLS-1$ //$NON-NLS-2$

		// Logging filter - raw request and response printed to sys.o
		if (preferences
				.get(
						"raw_html_logging", properties.getProperty("raw_html_logging")).equals("on")) { //$NON-NLS-1$ //$NON-NLS-2$
			client.addFilter(new LoggingFilter());
		}
		service = client.resource(getBaseURI());
	}

	/**
	 * Returns the (singleton) instance of ChannelFinderClient
	 * 
	 * @return the instance of ChannelFinderClient
	 */
	public static ChannelFinderClient getInstance() {
		return instance;
	}

	private static URI getBaseURI() {
		return UriBuilder
				.fromUri(
						preferences
								.get(
										"channel_finder_url", properties.getProperty("channel_finder_url"))).build(); //$NON-NLS-1$
	}

	@Deprecated
	public void resetPreferences() {
		try {
			Preferences.userNodeForPackage(this.getClass()).clear();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param name
	 * @return the channel which matches the queried name
	 */
	public XmlChannel retreiveChannel(String name)
			throws ChannelFinderException {
		try {
			return service.path("channel").path(name).accept( //$NON-NLS-1$
					MediaType.APPLICATION_XML).get(XmlChannel.class);
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	/**
	 * Test Method
	 * 
	 * @return all the channels present in the database.
	 */
	public XmlChannels retrieveChannels() throws ChannelFinderException {
		// will be replaced by the XmlChannels structure.
		try {
			return service.path("channels").accept( //$NON-NLS-1$
					MediaType.APPLICATION_XML).get(XmlChannels.class);
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	/**
	 * Add a channel
	 * 
	 * @param xmlChannel
	 */
	public void addChannel(XmlChannel xmlChannel) throws ChannelFinderException {
		try {
			service.path("channel").path(xmlChannel.getName()).type( //$NON-NLS-1$
					MediaType.APPLICATION_XML).put(xmlChannel);
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	/**
	 * Add a group of channels
	 * 
	 * @param xmlChannels
	 */
	public void addChannels(XmlChannels xmlChannels)
			throws ChannelFinderException {
		try {
			service.path("channels").type(MediaType.APPLICATION_XML).post( //$NON-NLS-1$
					xmlChannels);
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	/**
	 * Remove Channel with specified name;
	 * 
	 * @param name
	 */
	public void removeChannel(String name) throws ChannelFinderException {
		try {
			service.path("channel").path(name).delete(); //$NON-NLS-1$
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	/**
	 * Remove a group of channels
	 * 
	 * @param channels
	 */
	public void removeChannels(Collection<String> channels) {
		for (String channelName : channels) {
			removeChannel(channelName);
		}
	}

	/**
	 * 
	 * @param pattern
	 * @return
	 */
	public XmlChannels queryChannelsByName(String pattern)
			throws ChannelFinderException {
		try {
			return service
					.path("channels").queryParam("~name", pattern).accept( //$NON-NLS-1$ //$NON-NLS-2$
							MediaType.APPLICATION_XML).accept(
							MediaType.APPLICATION_JSON).get(XmlChannels.class);
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	/**
	 * 
	 * @param pattern
	 * @return
	 */
	public XmlChannels queryChannelsByTag(String pattern)
			throws ChannelFinderException {
		try {
			return service.path("channels").queryParam("~tag", pattern).accept( //$NON-NLS-1$ //$NON-NLS-2$
					MediaType.APPLICATION_XML).accept(
					MediaType.APPLICATION_JSON).get(XmlChannels.class);
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	/**
	 * This function is a subset of queryChannels - should it be removed??
	 * <p>
	 * TODO: add the usage of patterns and implement on top of the general query
	 * using the map
	 * 
	 * @param property
	 * @return
	 * @throws ChannelFinderException
	 */
	public XmlChannels queryChannelsByProp(String property, String... patterns)
			throws ChannelFinderException {
		try {
			return service.path("channels").queryParam(property, "*").accept( //$NON-NLS-1$ //$NON-NLS-2$
					MediaType.APPLICATION_XML).accept(
					MediaType.APPLICATION_JSON).get(XmlChannels.class);
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	/**
	 * Query for channels based on the criteria specified in the map
	 * 
	 * @param map
	 * @return
	 */
	public XmlChannels queryChannels(Map<String, String> map) {
		MultivaluedMapImpl mMap = new MultivaluedMapImpl();
		Iterator<Map.Entry<String, String>> itr = map.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<String, String> entry = itr.next();
			mMap
					.put(entry.getKey(), Arrays.asList(entry.getValue().split(
							",")));
		}
		return queryChannels(mMap);
	}

	/**
	 * Multivalued map used to search for a key with multiple values. e.g.
	 * property a=1 or property a=2
	 * 
	 * @param map
	 *            Multivalue map for searching a key with multiple values
	 * @return
	 */
	public XmlChannels queryChannels(MultivaluedMapImpl map) {
		return service.path("channels").queryParams(map).accept(
				MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_JSON)
				.get(XmlChannels.class);
	}

	/**
	 * Update properties and tags of existing channel "channel"
	 * 
	 * @param channel
	 * @throws ChannelFinderException
	 */
	public void updateChannel(XmlChannel channel) throws ChannelFinderException {
		try {
			service.path("channel").path(channel.getName()).type( //$NON-NLS-1$
					MediaType.APPLICATION_XML).post(channel);
		} catch (UniformInterfaceException e) {
			// check for errors while trying an update
			throw new ChannelFinderException(e);
		}
	}

	/**
	 * 
	 * @param channelName
	 * @param tag
	 */
	public void addTag(String channelName, XmlTag tag) {
		Collection<String> list = new ArrayList<String>();
		list.add(channelName);
		addTag(list, tag);
	}

	/**
	 * Add {tag} on the set of channels {channels}
	 * 
	 * @param channelNames
	 * @param tag
	 */
	public void addTag(Collection<String> channelNames, XmlTag tag) {
		try {
			XmlChannels channels = new XmlChannels();
			XmlChannel channel;
			for (String channelName : channelNames) {
				channel = new XmlChannel(channelName, "");
				channel.addTag(tag);
				channels.addChannel(channel);
			}
			service
					.path("tags").path(tag.getName()).type(MediaType.APPLICATION_XML).post( //$NON-NLS-1$
							channels);
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	/**
	 * Remove Tag {tagName} from channel {channelName}
	 * 
	 * @param channelName
	 * @param tagName
	 */
	public void removeTag(String channelName, String tagName) {
		service.path("tags").path(tagName).path(channelName).accept( //$NON-NLS-1$
				MediaType.APPLICATION_XML).delete();
	}

	/**
	 * Remove Tag {tagName} from a list of channels {channelNames}
	 * 
	 * @param channelName
	 * @param tagName
	 */
	public void removeTag(Collection<String> channelNames, String tagName) {
		for (String channelName : channelNames) {
			removeTag(channelName, tagName);
		}
	}

	/**
	 * Add Tag {tagName} to channel {channelName}
	 * 
	 * @param channelName
	 * @param tagName
	 */
	public void resetTag(String channelName, XmlTag tag)
			throws ChannelFinderException {
		try {
			service.path("tags").path(tag.getName()).path(channelName).type(
					MediaType.APPLICATION_XML).put(tag);
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	/**
	 * Set {tag} on the set of channels {channels} and remove it from all others
	 * 
	 * @param channels
	 * @param tag
	 */
	public void resetTag(Collection<String> channelNames, XmlTag tag) {
		try {
			XmlChannels channels = new XmlChannels();
			XmlChannel channel;
			for (String channelName : channelNames) {
				channel = new XmlChannel();
				channel.setName(channelName);
				channel.addTag(tag);
				channels.addChannel(channel);
			}
			service
					.path("tags").path(tag.getName()).accept(MediaType.APPLICATION_XML).put( //$NON-NLS-1$
							channels);
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	/**
	 * Remove {tag} from all channels
	 * 
	 * @param tag
	 */
	public void deleteTag(String tag) {
		try {
			service.path("tags").path(tag).accept(MediaType.APPLICATION_XML) //$NON-NLS-1$
					.delete();
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	/**
	 * 
	 * @param channelName
	 * @param property
	 * @throws ChannelFinderException
	 */
	public void addProperty(String channelName, XmlProperty property)
			throws ChannelFinderException {
		XmlChannel channel = retreiveChannel(channelName);
		if (channel != null) {
			channel.addProperty(property);
			updateChannel(channel);
		}
	}

	/**
	 * 
	 * @param channels
	 * @param property
	 * @throws ChannelFinderException
	 */
	public void addProperty(Collection<String> channelNames,
			XmlProperty property) throws ChannelFinderException {
		for (String channelName : channelNames) {
			addProperty(channelName, property);
		}
	}

	/**
	 * 
	 * @param channelName
	 * @param propertyName
	 * @throws ChannelFinderException
	 */
	public void removeProperty(String channelName, String propertyName)
			throws ChannelFinderException {
		try {
			service.path("properties").path(propertyName).path(channelName)
					.accept(MediaType.APPLICATION_XML).accept(
							MediaType.APPLICATION_JSON).delete();
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	/**
	 * 
	 * @param channels
	 * @param propertyName
	 * @throws ChannelFinderException
	 */
	public void removeProperty(Collection<String> channelNames,
			String propertyName) throws ChannelFinderException {
		for (String channelName : channelNames) {
			removeProperty(channelName, propertyName);
		}
	}

	/**
	 * 
	 * @param property
	 * @throws ChannelFinderException
	 */
	public void deleteProperty(String property) throws ChannelFinderException {
		try {
			service.path("properties").path(property).accept(
					MediaType.APPLICATION_XML).accept(
					MediaType.APPLICATION_JSON).delete();
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}
}
