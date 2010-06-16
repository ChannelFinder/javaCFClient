package gov.bnl.channelfinder.channelfinderAPI;

import gov.bnl.channelfinder.channelfinderAPI.exceptions.ChannelFinderException;
import gov.bnl.channelfinder.model.XmlChannel;
import gov.bnl.channelfinder.model.XmlChannels;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import javax.xml.ws.http.HTTPException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class ChannelFinderClient {
	private static ChannelFinderClient instance = new ChannelFinderClient();
	private WebResource service;
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
		// import the properties form the channelfinder.properties
		properties = new Properties();
		FileInputStream in;
		try {
			String propertyFile = System
					.getProperty("channelfinder.properties");
			if (propertyFile != null)
				in = new FileInputStream(propertyFile);
			else {
				System.out.println("Using default.");
				in = new FileInputStream("channelfinder.properties");
			}
			properties.load(in);
			in.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// Authentication and Authorization configuration
		TrustManager mytm[] = null;
		SSLContext ctx = null;

		try {
			mytm = new TrustManager[] { new MyX509TrustManager(properties
					.getProperty("trustStore"), properties.getProperty(
					"trustPass").toCharArray()) };
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			ctx = SSLContext.getInstance("SSL");
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
		client.addFilter(new HTTPBasicAuthFilter(properties
				.getProperty("username"), properties.getProperty("password")));

		// Logging filter - raw request and response printed to sys.o
		if (properties.getProperty("logging").equals("on")) {
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
		return UriBuilder.fromUri(properties.getProperty("url")).build();
	}

	public void changeURI(String path) {
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		service = client.resource(UriBuilder.fromUri(path).build());
	}

	/**
	 * 
	 * @param name
	 * @return the channel which matches the queried name
	 */
	public XmlChannel getChannel(String name) {
		return service.path("channel").path(name).accept(
				MediaType.APPLICATION_XML).get(XmlChannel.class);
	}

	/**
	 * Test Method
	 * 
	 * @return all the channels present in the database.
	 */
	public XmlChannels getChannels() {
		// will be replaced by the XmlChannels structure.
		return checkResponse(service.path("channels").accept(
				MediaType.APPLICATION_XML).get(ClientResponse.class),
				XmlChannels.class);
	}

	/**
	 * Add a channel
	 * 
	 * @param xmlChannel
	 */
	public void addChannel(XmlChannel xmlChannel) {
		service.path("channel").path(xmlChannel.getName()).type(
				MediaType.APPLICATION_XML).put(xmlChannel);
	}

	/**
	 * Add a group of channels
	 * 
	 * @param xmlChannels
	 */
	public void addChannels(XmlChannels xmlChannels) {
		try {
			service.path("channels").type(MediaType.APPLICATION_XML).post(
					xmlChannels);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	/**
	 * Remove Channel with specified name;
	 * 
	 * @param name
	 */
	public void removeChannel(String name) {
		service.path("channel").path(name).delete();
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
		return service.path("channels").queryParam("~name", pattern).accept(
				MediaType.APPLICATION_XML).get(XmlChannels.class);
	}

	/**
	 * 
	 * @param pattern
	 * @return
	 */
	public XmlChannels queryChannelsTag(String pattern) {
		return service.path("channels").queryParam("~tag", pattern).accept(
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
		return service.path("channels").queryParams(mMap).accept(
				MediaType.APPLICATION_XML).get(XmlChannels.class);
	}

	/**
	 * Update properties and tags of existing channel "channel"
	 * 
	 * @param channel
	 */
	public void updateChannel(XmlChannel channel) {
		try {
			service.path("channel").path(channel.getName()).type(
					MediaType.APPLICATION_XML).post(channel);
		} catch (UniformInterfaceException e) {
			checkResponse(e.getResponse(), null); // no return expected.
		}
	}

	/**
	 * Set {tag} on the set of channels {channels} PUT - delete and replace
	 * 
	 * @param channels
	 * @param tag
	 */
	public void setTag(XmlChannels channels, String tag) {
		service.path("tags").path(tag).accept(MediaType.APPLICATION_XML).put(
				channels);
	}

	/**
	 * Add {tag} on the set of channels {channels} POST -
	 * 
	 * @param channels
	 * @param tag
	 */
	public void addTag(XmlChannels channels, String tag) {
		service.path("tags").path(tag).accept(MediaType.APPLICATION_XML).post(
				channels);
	}

	/**
	 * Remove {tag} from Xmlchannel list {channels}
	 * 
	 * @param channels
	 * @param tag
	 */
	public void removeTag(XmlChannels channels, String tag) {
		service.path("tags").path(tag).accept(MediaType.APPLICATION_XML)
				.delete();
	}

	/**
	 * Add Tag {tagName} to channel {channelName}
	 * 
	 * @param channelName
	 * @param tagName
	 */
	public void setChannelTag(String channelName, String tagName) {
		XmlChannel ch = service.path("channel").path(channelName).accept(
				MediaType.APPLICATION_XML).get(XmlChannel.class);
		service.path("tags").path(tagName).path(channelName).accept(
				MediaType.APPLICATION_XML).put(ch);
	}

	/**
	 * Remove Tag {tagName} to channel {channelName}
	 * 
	 * @param channelName
	 * @param tagName
	 */
	public void removeChannelTag(String channelName, String tagName) {
		service.path("tags").path(tagName).path(channelName).accept(
				MediaType.APPLICATION_XML).delete();
	}

	// determines the existence of an error and throws ChannelFinderException.
	private <T> T checkResponse(ClientResponse clientResponse,
			Class<T> returnClass) {
		@SuppressWarnings("unused")
		Status returnStatus = clientResponse.getClientResponseStatus();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
