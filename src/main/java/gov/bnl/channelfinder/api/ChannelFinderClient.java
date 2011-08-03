package gov.bnl.channelfinder.api;

import static gov.bnl.channelfinder.api.Channel.Builder.*;
import static gov.bnl.channelfinder.api.Tag.Builder.*;

import gov.bnl.channelfinder.api.Channel.Builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * TODO: make this not a singleton. Add a constructor to programmatically pass
 * the configuration.
 * 
 * TODO: replace the usage of Xml* types with channel,tag,properties
 * 
 * @author shroffk
 * 
 */
/**
 * @author shroffk
 * 
 */
public class ChannelFinderClient {
	private WebResource service;

	public static class CFCBuilder {

		// required
		private URI uri = null;

		// optional
		private boolean withHTTPAuthentication = false;
		private HTTPBasicAuthFilter httpBasicAuthFilter = null;
		private LoggingFilter logginFilter = null;

		private ClientConfig clientConfig = null;
		private TrustManager[] trustManager = new TrustManager[] { new DummyX509TrustManager() };;
		@SuppressWarnings("unused")
		private SSLContext sslContext = null;

		private String protocol = null;
		private String username = null;
		private String password = null;

		private Executor executor = Executors.newSingleThreadExecutor();

		private CFProperties preferences = new CFProperties();

		private static final String default_service_url = "http://localhost:8080/ChannelFinder/resources";

		private CFCBuilder() {
			this.uri = URI.create(this.preferences.getPreferenceValue(
					"channel_finder_url", default_service_url));
			this.protocol = this.uri.getScheme();
		}

		private CFCBuilder(URI uri) {
			this.uri = uri;
			this.protocol = this.uri.getScheme();
		}

		public static CFCBuilder toDefault() {
			return new CFCBuilder();
		}

		public static CFCBuilder to(String uri) {
			return new CFCBuilder(URI.create(uri));
		}

		public static CFCBuilder to(URI uri) {
			return new CFCBuilder(uri);
		}

		public CFCBuilder withHTTPAuthentication(boolean withHTTPAuthentication) {
			this.withHTTPAuthentication = withHTTPAuthentication;
			return this;
		}

		public CFCBuilder username(String username) {
			this.username = username;
			return this;
		}

		public CFCBuilder password(String password) {
			this.password = password;
			return this;
		}

		public CFCBuilder withLogging(Logger logger) {
			this.logginFilter = new LoggingFilter(logger);
			return this;
		}

		public CFCBuilder withClientConfig(ClientConfig clientConfig) {
			this.clientConfig = clientConfig;
			return this;
		}

		@SuppressWarnings("unused")
		private CFCBuilder withSSLContext(SSLContext sslContext) {
			this.sslContext = sslContext;
			return this;
		}

		public CFCBuilder withTrustManager(TrustManager[] trustManager) {
			this.trustManager = trustManager;
			return this;
		}

		public CFCBuilder withExecutor(Executor executor) {
			this.executor = executor;
			return this;
		}

		public ChannelFinderClient create() {
			if (this.protocol.equalsIgnoreCase("http")) {
				this.clientConfig = new DefaultClientConfig();
			} else if (this.protocol.equalsIgnoreCase("https")) {
				if (this.clientConfig == null) {
					SSLContext sslContext = null;
					try {
						sslContext = SSLContext.getInstance("SSL");
						sslContext.init(null, this.trustManager, null);
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (KeyManagementException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					this.clientConfig = new DefaultClientConfig();
					this.clientConfig.getProperties().put(
							HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
							new HTTPSProperties(new HostnameVerifier() {

								@Override
								public boolean verify(String hostname,
										SSLSession session) {
									return true;
								}
							}, sslContext));
				}
			}
			if (this.withHTTPAuthentication) {
				this.httpBasicAuthFilter = new HTTPBasicAuthFilter(
						ifNullReturnPreferenceValue(this.username, "username",
								"username"), ifNullReturnPreferenceValue(
								this.password, "password", "password"));
			}
			return new ChannelFinderClient(this.uri, this.clientConfig,
					this.httpBasicAuthFilter, this.logginFilter);
		}

		private String ifNullReturnPreferenceValue(String value, String key,
				String Default) {
			if (value == null) {
				return this.preferences.getPreferenceValue(key, Default);
			} else {
				return value;
			}
		}
	}

	ChannelFinderClient(URI uri, ClientConfig config,
			HTTPBasicAuthFilter httpBasicAuthFilter, LoggingFilter loggingFilter) {
		Client client = Client.create(config);
		if (httpBasicAuthFilter != null) {
			client.addFilter(httpBasicAuthFilter);
		}
		if (loggingFilter != null) {
			client.addFilter(loggingFilter);
		}
		service = client.resource(UriBuilder.fromUri(uri).build());
	}

	/**
	 * Get a list of all the properties currently existing
	 * 
	 * @return
	 */
	public Collection<String> getAllProperties() {
		Collection<String> allProperties = new HashSet<String>();
		try {
			XmlProperties allXmlProperties = service.path("properties")
					.accept(MediaType.APPLICATION_XML).get(XmlProperties.class);
			for (XmlProperty xmlProperty : allXmlProperties.getProperties()) {
				allProperties.add(xmlProperty.getName());
			}
			return allProperties;
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	/**
	 * Get a list of all the tags currently existing
	 * 
	 * @return
	 */
	public Collection<String> getAllTags() {
		Collection<String> allTags = new HashSet<String>();
		try {
			XmlTags allXmlTags = service.path("tags")
					.accept(MediaType.APPLICATION_XML).get(XmlTags.class);
			for (XmlTag xmlTag : allXmlTags.getTags()) {
				allTags.add(xmlTag.getName());
			}
			return allTags;
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	@Deprecated
	public static void resetPreferences() {
		try {
			Preferences.userNodeForPackage(ChannelFinderClient.class).clear();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns a channel that exactly matches the channelName
	 * <tt>channelName</tt>
	 * 
	 * @param channelName
	 * @return
	 * @throws ChannelFinderException
	 */
	public Channel getChannel(String channelName) throws ChannelFinderException {
		try {
			return new Channel(service
					.path("channels").path(channelName).accept( //$NON-NLS-1$
							MediaType.APPLICATION_XML).get(XmlChannel.class));
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	/**
	 * Set a single channel <tt>channel</tt>, if the channel already exists it
	 * will be replaced with the given channel
	 * 
	 * @param channel
	 *            the channel to be added
	 * @throws ChannelFinderException
	 */
	public void set(Channel.Builder channel) throws ChannelFinderException {
		try {
			service.path("channels").path(channel.toXml().getName()).type( //$NON-NLS-1$
					MediaType.APPLICATION_XML).put(channel.toXml());
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	/**
	 * Set a set of channels, if any channels already exists it is replaced.
	 * 
	 * @param channels
	 *            set of channels to be added
	 * @throws ChannelFinderException
	 */
	public void set(Collection<Builder> channels) throws ChannelFinderException {
		try {
			XmlChannels xmlChannels = new XmlChannels();
			for (Channel.Builder channel : channels) {
				xmlChannels.addXmlChannel(channel.toXml());
			}
			service.path("channels").type(MediaType.APPLICATION_XML).post( //$NON-NLS-1$
					xmlChannels);
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	/**
	 * Add a Tag <tt>tag</tt> with no associated channels to the database.
	 * 
	 * @param tag
	 */
	public void set(Tag.Builder tag) {
		try {
			XmlTag xmlTag = tag.toXml();
			service.path("tags").path(xmlTag.getName())
					.accept(MediaType.APPLICATION_XML)
					.accept(MediaType.APPLICATION_JSON).put(xmlTag);
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}


	/**
	 * Add tag <tt>tag</tt> to channel <tt>channelName</tt> and remove the tag
	 * from all other channels
	 * 
	 * @param tag
	 * @param channelName
	 * @throws ChannelFinderException
	 */
	public void set(Tag.Builder tag, String channelName)
			throws ChannelFinderException {
		try {
			// service.path("tags").path(tag.toXml().getName()).path(channel)
			// .type(MediaType.APPLICATION_XML).put(tag.toXml());
			Collection<String> channels = new ArrayList<String>();
			channels.add(channelName);
			set(tag, channels);
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}

	}
	

	/**
	 * Set tag <tt>tag</tt> on the set of channels {channels} and remove it from
	 * all others
	 * 
	 * @param channels
	 * @param tag
	 */
	public void set(Tag.Builder tag, Collection<String> channelNames) {
		// Better than recursively calling set(tag, channel) for each channel
		try {
			XmlTag xmlTag = tag.toXml();
			XmlChannels channels = new XmlChannels();
			XmlChannel channel;
			for (String channelName : channelNames) {
				channel = new XmlChannel(channelName);
				channels.addXmlChannel(channel);
			}
			xmlTag.setXmlChannels(channels);
			service.path("tags").path(tag.toXml().getName())
					.accept(MediaType.APPLICATION_XML).put(xmlTag);
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}
	
	/**
	 * Add a new property <tt>property</tt>
	 * 
	 * @param prop
	 */
	public void set(Property.Builder prop) {
		try {
			XmlProperty property = prop.toXml();
			service.path("properties").path(property.getName())
					.accept(MediaType.APPLICATION_XML)
					.accept(MediaType.APPLICATION_JSON).put(property);
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}
	
	public void set(Property.Builder prop, String channelName){
		Map<String, String> map = new HashMap<String, String>();
		map.put(channelName, prop.toXml().getValue());
		set(prop, map);
	}
	
	public void set(Property.Builder prop, Collection<String> channelNames){
		Map<String, String> map = new HashMap<String, String>();
		String propertyValue = prop.toXml().getValue();
		for (String channelName : channelNames) {
			map.put(channelName, propertyValue);			
		}
		set(prop, map);
	}
	
	public void set(Property.Builder prop, Map<String, String> channelPropertyMap) {
		XmlProperty xmlProperty = prop.toXml();
		XmlChannels channels = new XmlChannels();
		for(Entry<String, String> e : channelPropertyMap.entrySet()){
			XmlChannel xmlChannel = new XmlChannel(e.getKey());
			// need a defensive copy to avoid a cycle
			xmlChannel.addXmlProperty(new XmlProperty(xmlProperty.getName(),
					xmlProperty.getOwner(), e.getValue()));
			channels.addXmlChannel(xmlChannel);
		}
		xmlProperty.setXmlChannels(channels);
		try {
			service.path("properties").path(xmlProperty.getName())
					.accept(MediaType.APPLICATION_XML)
					.accept(MediaType.APPLICATION_JSON).put(xmlProperty);
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	/**
	 * Update properties and tags of existing channel <tt>channel</tt>
	 * 
	 * @param channel
	 * @throws ChannelFinderException
	 */
	public void update(Channel.Builder channel)
			throws ChannelFinderException {
		try {
			service.path("channels").path(channel.toXml().getName()).type( //$NON-NLS-1$
					MediaType.APPLICATION_XML).post(channel.toXml());
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}
	
	/**
	 * Add Tag <tt>tag </tt> to Channel with name <tt>channelName</tt>
	 * 
	 * @param string
	 *            Name of the channel to which the tag is to be added
	 * @param tag
	 *            the tag to be added
	 */
	public void update(Tag.Builder tag, String channelName) {
		Set<String> channelNames = new HashSet<String>();
		channelNames.add(channelName);
		update(tag, channelNames);
	}

	/**
	 * Add the Tag <tt>tag</tt> to the set of the channels with names
	 * <tt>channelNames</tt>
	 * 
	 * @param channelNames
	 * @param tag
	 */
	public void update(Tag.Builder tag, Collection<String> channelNames) {
		try {
			XmlTag xmlTag = tag.toXml();
			XmlChannels channels = new XmlChannels();
			XmlChannel channel;
			for (String channelName : channelNames) {
				channel = new XmlChannel(channelName, "");
				channels.addXmlChannel(channel);
			}
			xmlTag.setXmlChannels(channels);
			service.path("tags").path(tag.toXml().getName())
					.type(MediaType.APPLICATION_XML).post(xmlTag);
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	/**
	 * Add Property <tt>property</tt> to the channel <tt>channelName</tt>
	 * 
	 * @param string
	 * @param owner
	 */
	public void update(Property.Builder property, String channelName) {
		Set<String> channelNames = new HashSet<String>();
		channelNames.add(channelName);
		update(property, channelNames);
	}

	/**
	 * @param channelNames
	 * @param property
	 */
	public void update(Property.Builder property, Collection<String> channelNames) {
		Map<String, String> map = new HashMap<String, String>();
		String propertyValue = property.toXml().getValue();
		for (String channelName : channelNames) {
			map.put(channelName, propertyValue);			
		}
		update(property, map);
	}
	
	public void update(Property.Builder property, Map<String, String> channelPropertyMap) {
		XmlProperty xmlProperty = property.toXml();
		XmlChannels channels = new XmlChannels();
		for ( Entry<String, String> e: channelPropertyMap.entrySet()) {
			XmlChannel xmlChannel = new XmlChannel(e.getKey());
			// need a defensive copy to avoid A cycle
			xmlChannel.addXmlProperty(new XmlProperty(xmlProperty.getName(),
					xmlProperty.getOwner(), e.getValue()));
			channels.addXmlChannel(xmlChannel);
		}
		xmlProperty.setXmlChannels(channels);
		try {
			service.path("properties").path(xmlProperty.getName())
					.accept(MediaType.APPLICATION_XML)
					.accept(MediaType.APPLICATION_JSON).post(xmlProperty);
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	/**
	 * 
	 * @param pattern
	 * @return
	 */
	public Collection<Channel> findByName(String pattern)
			throws ChannelFinderException {
		try {
			Collection<Channel> channels = new HashSet<Channel>();
			XmlChannels xmlChannels = service
					.path("channels").queryParam("~name", pattern).accept( //$NON-NLS-1$ //$NON-NLS-2$
							MediaType.APPLICATION_XML)
					.accept(MediaType.APPLICATION_JSON).get(XmlChannels.class);
			for (XmlChannel xmlchannel : xmlChannels.getChannels()) {
				channels.add(new Channel(xmlchannel));
			}
			return Collections.unmodifiableCollection(channels);
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	/**
	 * 
	 * @param pattern
	 * @return
	 */
	public Collection<Channel> findByTag(String pattern)
			throws ChannelFinderException {
		try {
			Collection<Channel> channels = new HashSet<Channel>();
			XmlChannels xmlChannels = service
					.path("channels").queryParam("~tag", pattern).accept( //$NON-NLS-1$ //$NON-NLS-2$
							MediaType.APPLICATION_XML)
					.accept(MediaType.APPLICATION_JSON).get(XmlChannels.class);
			for (XmlChannel xmlchannel : xmlChannels.getChannels()) {
				channels.add(new Channel(xmlchannel));
			}
			return Collections.unmodifiableCollection(channels);

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
	public Collection<Channel> findByProperty(String property,
			String... patterns) throws ChannelFinderException {
		try {
			Collection<Channel> channels = new HashSet<Channel>();
			XmlChannels xmlChannels = service
					.path("channels").queryParam(property, "*").accept( //$NON-NLS-1$ //$NON-NLS-2$
							MediaType.APPLICATION_XML)
					.accept(MediaType.APPLICATION_JSON).get(XmlChannels.class);
			for (XmlChannel xmlchannel : xmlChannels.getChannels()) {
				channels.add(new Channel(xmlchannel));
			}
			return Collections.unmodifiableCollection(channels);
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
	public Collection<Channel> find(Map<String, String> map) {
		MultivaluedMapImpl mMap = new MultivaluedMapImpl();
		Iterator<Map.Entry<String, String>> itr = map.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<String, String> entry = itr.next();
			mMap.put(entry.getKey(), Arrays.asList(entry.getValue().split(",")));
		}
		return find(mMap);
	}

	/**
	 * Multivalued map used to search for a key with multiple values. e.g.
	 * property a=1 or property a=2
	 * 
	 * @param map
	 *            Multivalue map for searching a key with multiple values
	 * @return
	 */
	public Collection<Channel> find(MultivaluedMapImpl map) {
		Collection<Channel> channels = new HashSet<Channel>();
		XmlChannels xmlChannels = service.path("channels").queryParams(map)
				.accept(MediaType.APPLICATION_XML)
				.accept(MediaType.APPLICATION_JSON).get(XmlChannels.class);
		for (XmlChannel xmlchannel : xmlChannels.getChannels()) {
			channels.add(new Channel(xmlchannel));
		}
		return Collections.unmodifiableCollection(channels);
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
	 * @param property
	 * @throws ChannelFinderException
	 */
	public void deleteProperty(String property) throws ChannelFinderException {
		try {
			service.path("properties").path(property)
					.accept(MediaType.APPLICATION_XML)
					.accept(MediaType.APPLICATION_JSON).delete();
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	Collection<Channel> getAllChannels() {
		try {
			XmlChannels channels = service.path("channels").accept( //$NON-NLS-1$
					MediaType.APPLICATION_XML).get(XmlChannels.class);
			Collection<Channel> set = new HashSet<Channel>();
			for (XmlChannel channel : channels.getChannels()) {
				set.add(new Channel(channel));
			}
			return set;
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	/**
	 * Remove the channel identified by <tt>channel</tt>
	 * 
	 * @param channel
	 *            channel to be removed
	 * @throws ChannelFinderException
	 */
	public void delete(Channel.Builder channel) throws ChannelFinderException {
		try {
			service.path("channels").path(channel.toXml().getName()).delete(); //$NON-NLS-1$
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	/**
	 * Remove the set of channels identified by <tt>channels</tt>
	 * 
	 * @param channels
	 * @throws ChannelFinderException
	 */
	@Deprecated
	public void delete(Collection<Channel.Builder> channels)
			throws ChannelFinderException {
		for (Channel.Builder channel : channels) {
			delete(channel);
		}
	}

	/**
	 * Remove tag <tt>tag</tt> from the channel with the name
	 * <tt>channelName</tt>
	 * 
	 * @param tag
	 * @param channelName
	 */
	public void delete(Tag.Builder tag, String channelName)
			throws ChannelFinderException {
		try {
			service.path("tags").path(tag.toXml().getName()).path(channelName).accept( //$NON-NLS-1$
							MediaType.APPLICATION_XML).delete();
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	/**
	 * Remove the tag <tt>tag </tt> from all the channels <tt>channelNames</tt>
	 * 
	 * @param tag
	 * @param channelNames
	 * @throws ChannelFinderException
	 */
	public void delete(Tag.Builder tag, Collection<String> channelNames)
			throws ChannelFinderException {
		// TODO optimize using the /tags/<name> payload with list of channels
		for (String channelName : channelNames) {
			delete(tag, channelName);
		}
	}

	/**
	 * Remove property <tt>property</tt> from the channel with name
	 * <tt>channelName</tt>
	 * 
	 * @param property
	 * @param name
	 * @throws ChannelFinderException
	 */
	public void delete(Property.Builder property, String channelName)
			throws ChannelFinderException {
		try {
			service.path("properties").path(property.toXml().getName())
					.path(channelName).accept(MediaType.APPLICATION_XML)
					.accept(MediaType.APPLICATION_JSON).delete();
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	/**
	 * Remove the property <tt>property</tt> from the set of channels
	 * <tt>channelNames</tt>
	 * 
	 * @param property
	 * @param channelNames
	 * @throws ChannelFinderException
	 */
	public void delete(Property.Builder property,
			Collection<String> channelNames) throws ChannelFinderException {
		for (String channel : channelNames) {
			delete(property, channel);
		}
	}

}
