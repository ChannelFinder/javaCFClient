package gov.bnl.channelfinder.channelfinderAPI;

import gov.bnl.channelfinder.channelfinderAPI.exceptions.ChannelFinderException;
import gov.bnl.channelfinder.model.XmlChannel;
import gov.bnl.channelfinder.model.XmlChannels;
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

import java.util.prefs.*;

public class ChannelFinderClient {
	private static ChannelFinderClient instance = new ChannelFinderClient();
	private WebResource service;
	private static Preferences preferences;
	private static Properties properties;

	/*
	 * TODO Error handling a. simply throw the http error returned by
	 * channelFinder webservice ? b. catch/handle the http errors and then throw
	 * our own ?
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
	public XmlChannel getChannel(String name) throws ChannelFinderException {
		return service.path("channel").path(name).accept( //$NON-NLS-1$
				MediaType.APPLICATION_XML).get(XmlChannel.class);
	}

	/**
	 * Test Method
	 * 
	 * @return all the channels present in the database.
	 */
	public XmlChannels getChannels() {
		// will be replaced by the XmlChannels structure.
		return checkResponse(service.path("channels").accept( //$NON-NLS-1$
				MediaType.APPLICATION_XML).get(ClientResponse.class),
				XmlChannels.class);
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
			checkResponse(e.getResponse(), null);
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
			checkResponse(e.getResponse(), null);
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
			checkResponse(e.getResponse(), null);
		}
	}

	/**
	 * Remove a group of channels
	 * 
	 * @param channels
	 */
	public void removeChannels(XmlChannels channels) {
		Iterator<XmlChannel> itr = channels.getChannels().iterator();
		while (itr.hasNext()) {
			removeChannel(itr.next().getName());
		}
	}

	/**
	 * 
	 * @param pattern
	 * @return
	 */
	public XmlChannels queryChannelsName(String pattern) {
		return service.path("channels").queryParam("~name", pattern).accept( //$NON-NLS-1$ //$NON-NLS-2$
				MediaType.APPLICATION_XML).get(XmlChannels.class);
	}

	/**
	 * 
	 * @param pattern
	 * @return
	 */
	public XmlChannels queryChannelsTag(String pattern) {
		return service.path("channels").queryParam("~tag", pattern).accept( //$NON-NLS-1$ //$NON-NLS-2$
				MediaType.APPLICATION_XML).get(XmlChannels.class);
	}

	/**
	 * 
	 * @param map
	 * @return
	 */
	public XmlChannels queryChannels(Map<String, String> map) {
		MultivaluedMapImpl mMap = new MultivaluedMapImpl();
		Iterator<Map.Entry<String, String>> itr = map.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<String, String> entry = itr.next();
			mMap.add(entry.getKey(), entry.getValue());
		}
		return service.path("channels").queryParams(mMap).accept( //$NON-NLS-1$
				MediaType.APPLICATION_XML).get(XmlChannels.class);
	}

	/**
	 * Update properties and tags of existing channel "channel"
	 * 
	 * @param channel
	 */
	public void updateChannel(XmlChannel channel) throws ChannelFinderException {
		try {
			service.path("channel").path(channel.getName()).type( //$NON-NLS-1$
					MediaType.APPLICATION_XML).post(channel);
		} catch (UniformInterfaceException e) {
			// check for errors while trying an update
			checkResponse(e.getResponse(), null);
		}
	}

	/**
	 * Set {tag} on the set of channels {channels} PUT - delete and replace
	 * 
	 * @param channels
	 * @param tag
	 */
	public void setTag(XmlChannels channels, String tag) {
		service.path("tags").path(tag).accept(MediaType.APPLICATION_XML).put( //$NON-NLS-1$
				channels);
	}

	/**
	 * Add {tag} on the set of channels {channels} POST -
	 * 
	 * @param channels
	 * @param tag
	 */
	public void addTag(XmlChannels channels, String tag) {
		service.path("tags").path(tag).type(MediaType.APPLICATION_XML).post( //$NON-NLS-1$
				channels);
	}

	/**
	 * Remove {tag} from all channels
	 * 
	 * @param tag
	 */
	public void removeTag(String tag) {
		service.path("tags").path(tag).accept(MediaType.APPLICATION_XML) //$NON-NLS-1$
				.delete();
	}

	/**
	 * Add Tag {tagName} to channel {channelName}
	 * 
	 * @param channelName
	 * @param tagName
	 */
	public void setChannelTag(String channelName, XmlTag tag)
			throws ChannelFinderException {
		try {
			service.path("tags").path(tag.getName()).path(channelName).type(
					MediaType.APPLICATION_XML).put(tag);
		} catch (UniformInterfaceException e) {
			checkResponse(e.getResponse(), null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Remove Tag {tagName} to channel {channelName}
	 * 
	 * @param channelName
	 * @param tagName
	 */
	public void removeChannelTag(String channelName, String tagName) {
		service.path("tags").path(tagName).path(channelName).accept( //$NON-NLS-1$
				MediaType.APPLICATION_XML).delete();
	}

	// determines the existence of an error and throws ChannelFinderException.
	private <T> T checkResponse(ClientResponse clientResponse,
			Class<T> returnClass) {
		int statusCode = clientResponse.getStatus();
		if (statusCode >= 200 && statusCode < 300) {
			// OK
			return clientResponse.getEntity(returnClass);
		} else if (statusCode >= 300 && statusCode < 400) {
			// Redirect
			throw new ChannelFinderException(clientResponse
					.getClientResponseStatus(), new UniformInterfaceException(
					clientResponse), parseErrorMsg(clientResponse
					.getEntity(String.class)));
		} else if (statusCode >= 400 && statusCode < 500) {
			// Client Error
			throw new ChannelFinderException(clientResponse
					.getClientResponseStatus(), new UniformInterfaceException(
					clientResponse), parseErrorMsg(clientResponse
					.getEntity(String.class)));
		} else if (statusCode >= 500) {
			// Server Error
			throw new ChannelFinderException(clientResponse
					.getClientResponseStatus(), new UniformInterfaceException(
					clientResponse), parseErrorMsg(clientResponse
					.getEntity(String.class)));
		} else {
			return null;
		}
	}

	private String parseErrorMsg(String entity) {
		try {
			ClientResponseParser callback = new ClientResponseParser();
			Reader reader = new StringReader(entity);
			new ParserDelegator().parse(reader, callback, false);
			return callback.getMessage();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
