package gov.bnl.channelfinder.api;

import gov.bnl.channelfinder.api.Channel.Builder;

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
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.google.common.base.Joiner;
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
public class ChannelFinderClient {
	private final WebResource service;

	private final ExecutorService executor;

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

		private ExecutorService executor = Executors.newSingleThreadExecutor();

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

		public CFCBuilder withExecutor(ExecutorService executor) {
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
					this.httpBasicAuthFilter, this.logginFilter, this.executor);
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
			HTTPBasicAuthFilter httpBasicAuthFilter,
			LoggingFilter loggingFilter, ExecutorService executor) {
		Client client = Client.create(config);
		if (httpBasicAuthFilter != null) {
			client.addFilter(httpBasicAuthFilter);
		}
		if (loggingFilter != null) {
			client.addFilter(loggingFilter);
		}
		service = client.resource(UriBuilder.fromUri(uri).build());
		this.executor = executor;
	}

	/**
	 * Get a list of all the properties currently existing
	 * 
	 * @return
	 */
	public Collection<String> getAllProperties() {

		return wrappedSubmit(new Callable<Collection<String>>() {

			@Override
			public Collection<String> call() throws Exception {

				Collection<String> allProperties = new HashSet<String>();
				XmlProperties allXmlProperties = service.path("properties")
						.accept(MediaType.APPLICATION_XML)
						.get(XmlProperties.class);
				for (XmlProperty xmlProperty : allXmlProperties.getProperties()) {
					allProperties.add(xmlProperty.getName());
				}
				return allProperties;
			}

		});
	}

	/**
	 * Get a list of names of all the tags currently existing
	 * 
	 * @return a list of the existing TagNames
	 */
	public Collection<String> getAllTags() {
		return wrappedSubmit(new Callable<Collection<String>>() {

			@Override
			public Collection<String> call() throws Exception {
				Collection<String> allTags = new HashSet<String>();
				XmlTags allXmlTags = service.path("tags")
						.accept(MediaType.APPLICATION_XML).get(XmlTags.class);
				for (XmlTag xmlTag : allXmlTags.getTags()) {
					allTags.add(xmlTag.getName());
				}
				return allTags;
			}
		});
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
		return wrappedSubmit(new FindByChannelName(channelName));
	}

	private class FindByChannelName implements Callable<Channel> {

		private final String channelName;

		FindByChannelName(String channelName) {
			super();
			this.channelName = channelName;
		}

		@Override
		public Channel call() throws UniformInterfaceException {
			return new Channel(service
					.path("channels").path(channelName).accept( //$NON-NLS-1$
							MediaType.APPLICATION_XML).get(XmlChannel.class));
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
		wrappedSubmit(new SetChannels(new XmlChannels(channel.toXml())));
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
			wrappedSubmit(new SetChannels(xmlChannels));
		} catch (UniformInterfaceException e) {
			throw new ChannelFinderException(e);
		}
	}

	private class SetChannels implements Runnable {

		private final XmlChannels xmlChannels;

		SetChannels(XmlChannels xmlChannels) {
			super();
			this.xmlChannels = xmlChannels;
		}

		@Override
		public void run() {
			service.path("channels").type(MediaType.APPLICATION_XML).post( //$NON-NLS-1$
					this.xmlChannels);
		}

	}

	/**
	 * Add a Tag <tt>tag</tt> with no associated channels to the database.
	 * 
	 * @param tag
	 */
	public void set(Tag.Builder tag) {
		wrappedSubmit(new SetTag(tag.toXml()));
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
		Collection<String> channelNames = new ArrayList<String>();
		channelNames.add(channelName);
		wrappedSubmit(new SetTag(tag.toXml(), channelNames));

	}

	/**
	 * Set tag <tt>tag</tt> on the set of channels {channels} and remove it from
	 * all others
	 * 
	 * @param channels
	 * @param tag
	 */
	public void set(Tag.Builder tag, Collection<String> channelNames) {
		wrappedSubmit(new SetTag(tag.toXml(), channelNames));
	}

	private class SetTag implements Runnable {
		private final XmlTag xmlTag;

		SetTag(XmlTag xmlTag) {
			super();
			this.xmlTag = xmlTag;
		}

		SetTag(XmlTag xmlTag, Collection<String> channelNames) {
			super();
			this.xmlTag = xmlTag;
			try {
				XmlChannels channels = new XmlChannels();
				XmlChannel channel;
				for (String channelName : channelNames) {
					channel = new XmlChannel(channelName);
					channels.addXmlChannel(channel);
				}
				xmlTag.setXmlChannels(channels);
				service.path("tags").path(this.xmlTag.getName())
						.accept(MediaType.APPLICATION_XML).put(this.xmlTag);
			} catch (UniformInterfaceException e) {
				throw new ChannelFinderException(e);
			}
		}

		@Override
		public void run() {
			service.path("tags").path(xmlTag.getName())
					.accept(MediaType.APPLICATION_XML).put(xmlTag);
		}

	}

	/**
	 * Add a new property <tt>property</tt>
	 * 
	 * @param prop
	 */
	public void set(Property.Builder prop) {
		wrappedSubmit(new SetProperty(prop.toXml()));
	}

	/**
	 * set it on this channel and remove it from all others
	 * 
	 * @param prop
	 * @param channelName
	 */
	public void set(Property.Builder prop, String channelName) {
		Collection<String> ch = new ArrayList<String>();
		ch.add(channelName);
		wrappedSubmit(new SetProperty(prop.toXml(), ch));
	}

	/**
	 * all with the same value
	 * 
	 * @param prop
	 * @param channelNames
	 */
	public void set(Property.Builder prop, Collection<String> channelNames) {
		wrappedSubmit(new SetProperty(prop.toXml(), channelNames));
	}

	/**
	 * 
	 * @param prop
	 * @param channelPropertyMap
	 */
	public void set(Property.Builder prop,
			Map<String, String> channelPropertyMap) {
		wrappedSubmit(new SetProperty(prop.toXml(), channelPropertyMap));
	}

	private class SetProperty implements Runnable {
		private final XmlProperty xmlProperty;

		SetProperty(XmlProperty prop) {
			this.xmlProperty = prop;
		}

		SetProperty(XmlProperty prop,
				Map<String, String> channelPropertyMap) {
			super();
			this.xmlProperty = prop;
			XmlChannels channels = new XmlChannels();
			for (Entry<String, String> e : channelPropertyMap.entrySet()) {
				XmlChannel xmlChannel = new XmlChannel(e.getKey());
				// need a copy to avoid a cycle
				xmlChannel.addXmlProperty(new XmlProperty(this.xmlProperty
						.getName(), this.xmlProperty.getOwner(), e.getValue()));
				channels.addXmlChannel(xmlChannel);
			}
			this.xmlProperty.setXmlChannels(channels);
		}

		SetProperty(XmlProperty prop, Collection<String> channelNames) {
			super();
			this.xmlProperty = prop;
			XmlChannels channels = new XmlChannels();
			for (String channelName : channelNames) {
				XmlChannel xmlChannel = new XmlChannel(channelName);
				// need a copy to avoid a linking cycle
				xmlChannel.addXmlProperty(new XmlProperty(this.xmlProperty
						.getName(), this.xmlProperty.getOwner(),
						this.xmlProperty.getValue()));
				channels.addXmlChannel(xmlChannel);
			}
			this.xmlProperty.setXmlChannels(channels);
		}

		@Override
		public void run() {
			service.path("properties").path(xmlProperty.getName())
					.accept(MediaType.APPLICATION_XML)
					.accept(MediaType.APPLICATION_JSON).put(xmlProperty);
		}
	}

	/**
	 * Update properties and tags of existing channel <tt>channel</tt>
	 * 
	 * @param channel
	 * @throws ChannelFinderException
	 */
	public void update(Channel.Builder channel) throws ChannelFinderException {
		wrappedSubmit(new UpdateChannel(channel.toXml()));
	}

	private class UpdateChannel implements Runnable {
		private final XmlChannel channel;

		UpdateChannel(XmlChannel channel) {
			super();
			this.channel = channel;
		}

		@Override
		public void run() {
			service.path("channels").path(channel.getName()).type( //$NON-NLS-1$
					MediaType.APPLICATION_XML).post(channel);
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
		wrappedSubmit(new UpdateTag(tag.toXml(), channelName));
	}

	/**
	 * Add the Tag <tt>tag</tt> to the set of the channels with names
	 * <tt>channelNames</tt>
	 * 
	 * @param channelNames
	 * @param tag
	 */
	public void update(Tag.Builder tag, Collection<String> channelNames) {
		wrappedSubmit(new UpdateTag(tag.toXml(), channelNames));
	}

	private class UpdateTag implements Runnable {
		private final XmlTag xmlTag;

		UpdateTag(XmlTag xmlTag) {
			super();
			this.xmlTag = xmlTag;
		}
		
		UpdateTag(XmlTag xmlTag, String ChannelName){
			super();
			this.xmlTag = xmlTag;
			this.xmlTag.setXmlChannels(new XmlChannels(new XmlChannel(ChannelName)));
		}
		
		UpdateTag(XmlTag xmlTag, Collection<String> channelNames){
			super();
			this.xmlTag = xmlTag;
			XmlChannels channels = new XmlChannels();
			for (String channelName : channelNames) {
				channels.addXmlChannel(new XmlChannel(channelName, ""));
			}
			xmlTag.setXmlChannels(channels);
		}
		
		@Override
		public void run() {
			service.path("tags").path(xmlTag.getName())
					.type(MediaType.APPLICATION_XML).post(xmlTag);
		}

	}

	/**
	 * Add Property <tt>property</tt> to the channel <tt>channelName</tt>
	 * 
	 * @param string
	 * @param owner
	 */
	public void update(Property.Builder property, String channelName) {
		XmlProperty xmlProperty = property.toXml();
		XmlChannel xmlChannel = new XmlChannel(channelName);
		// need a defensive copy to avoid A cycle
		xmlChannel.addXmlProperty(new XmlProperty(xmlProperty.getName(),
				xmlProperty.getOwner(), xmlProperty.getValue()));
		xmlProperty.setXmlChannels(new XmlChannels(xmlChannel));
		wrappedSubmit(new UpdateChannelProperty(xmlProperty, channelName));
	}

	private class UpdateChannelProperty implements Runnable {
		private final String channelName;
		private final XmlProperty xmlProperty;

		UpdateChannelProperty(XmlProperty xmlProperty, String channelName) {
			super();
			this.xmlProperty = xmlProperty;
			this.channelName = channelName;
		}

		@Override
		public void run() {
			service.path("properties").path(xmlProperty.getName())
					.path(channelName).accept(MediaType.APPLICATION_XML)
					.accept(MediaType.APPLICATION_JSON).put(xmlProperty);
		}

	}

	/**
	 * @param channelNames
	 * @param property
	 */
	public void update(Property.Builder property,
			Collection<String> channelNames) {
		XmlProperty xmlProperty = property.toXml();
		XmlChannels channels = new XmlChannels();
		for (String channelName : channelNames) {
			XmlChannel xmlChannel = new XmlChannel(channelName);
			// need a defensive copy to avoid A cycle
			xmlChannel.addXmlProperty(new XmlProperty(xmlProperty.getName(),
					xmlProperty.getOwner(), xmlProperty.getValue()));
			channels.addXmlChannel(xmlChannel);
		}
		xmlProperty.setXmlChannels(channels);
		wrappedSubmit(new UpdateProperty(xmlProperty));
	}

	public void update(Property.Builder property,
			Map<String, String> channelPropertyMap) {
		XmlProperty xmlProperty = property.toXml();
		XmlChannels channels = new XmlChannels();
		for (Entry<String, String> e : channelPropertyMap.entrySet()) {
			XmlChannel xmlChannel = new XmlChannel(e.getKey());
			// need a defensive copy to avoid A cycle
			xmlChannel.addXmlProperty(new XmlProperty(xmlProperty.getName(),
					xmlProperty.getOwner(), e.getValue()));
			channels.addXmlChannel(xmlChannel);
		}
		xmlProperty.setXmlChannels(channels);
		wrappedSubmit(new UpdateProperty(xmlProperty));
	}

	private class UpdateProperty implements Runnable {
		private final XmlProperty xmlProperty;

		UpdateProperty(XmlProperty xmlProperty) {
			super();
			this.xmlProperty = xmlProperty;
		}

		@Override
		public void run() {
			service.path("properties").path(xmlProperty.getName())
					.accept(MediaType.APPLICATION_XML)
					.accept(MediaType.APPLICATION_JSON).post(xmlProperty);
		}

	}

	/**
	 * 
	 * @param pattern
	 * @return
	 */
	public Collection<Channel> findByName(String pattern)
			throws ChannelFinderException {
		return wrappedSubmit(new FindByParam("~name", pattern));
	}

	/**
	 * 
	 * @param pattern
	 * @return
	 */
	public Collection<Channel> findByTag(String pattern)
			throws ChannelFinderException {
		return wrappedSubmit(new FindByParam("~tag", pattern));
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
			String... pattern) throws ChannelFinderException {
		Map<String, String> propertyPatterns = new HashMap<String, String>();
		if (pattern.length > 0) {
			propertyPatterns.put(property, Joiner.on(",").join(pattern));
		} else {
			propertyPatterns.put(property, "*");
		}
		return wrappedSubmit(new FindByMap(propertyPatterns));

	}

	private class FindByParam implements Callable<Collection<Channel>> {

		private String parameter;
		private String pattern;

		FindByParam(String parameter, String pattern) {
			this.parameter = parameter;
			this.pattern = pattern;
		}

		@Override
		public Collection<Channel> call() throws Exception {
			Collection<Channel> channels = new HashSet<Channel>();
			XmlChannels xmlChannels = service
					.path("channels").queryParam(this.parameter, this.pattern).accept( //$NON-NLS-1$ //$NON-NLS-2$
							MediaType.APPLICATION_XML)
					.accept(MediaType.APPLICATION_JSON).get(XmlChannels.class);
			for (XmlChannel xmlchannel : xmlChannels.getChannels()) {
				channels.add(new Channel(xmlchannel));
			}
			return Collections.unmodifiableCollection(channels);
		}

	}

	/**
	 * Query for channels based on the criteria specified in the map
	 * 
	 * @param map
	 * @return
	 */
	public Collection<Channel> find(Map<String, String> map)
			throws ChannelFinderException {
		return wrappedSubmit(new FindByMap(map));
	}

	/**
	 * Multivalued map used to search for a key with multiple values. e.g.
	 * property a=1 or property a=2
	 * 
	 * @param map
	 *            Multivalue map for searching a key with multiple values
	 * @return
	 */
	public Collection<Channel> find(MultivaluedMapImpl map)
			throws ChannelFinderException {
		return wrappedSubmit(new FindByMap(map));
	}

	private class FindByMap implements Callable<Collection<Channel>> {

		private MultivaluedMapImpl map;

		FindByMap(Map<String, String> map) {
			MultivaluedMapImpl mMap = new MultivaluedMapImpl();
			Iterator<Map.Entry<String, String>> itr = map.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry<String, String> entry = itr.next();
				mMap.put(entry.getKey(),
						Arrays.asList(entry.getValue().split(",")));
			}
			this.map = mMap;
		}

		FindByMap(MultivaluedMapImpl map) {
			this.map = new MultivaluedMapImpl();
			this.map.putAll(map);
		}

		@Override
		public Collection<Channel> call() throws Exception {
			Collection<Channel> channels = new HashSet<Channel>();
			XmlChannels xmlChannels = service.path("channels")
					.queryParams(this.map).accept(MediaType.APPLICATION_XML)
					.accept(MediaType.APPLICATION_JSON).get(XmlChannels.class);
			for (XmlChannel xmlchannel : xmlChannels.getChannels()) {
				channels.add(new Channel(xmlchannel));
			}
			return Collections.unmodifiableCollection(channels);
		}

	}

	/**
	 * Completely Delete {tag} with name = tagName from all channels
	 * 
	 * @param tagName
	 */
	public void deleteTag(String tagName) throws ChannelFinderException {
		wrappedSubmit(new DeleteElement("tags", tagName));
	}

	/**
	 * Completely Delete property with name = propertyName from all channels
	 * 
	 * @param propertyName
	 * @throws ChannelFinderException
	 */
	public void deleteProperty(String propertyName)
			throws ChannelFinderException {
		wrappedSubmit(new DeleteElement("properties", propertyName));
	}

	/**
	 * Remove the channel identified by <tt>channel</tt>
	 * 
	 * @param channel
	 *            channel to be removed
	 * @throws ChannelFinderException
	 */
	public void deleteChannel(String channelName) throws ChannelFinderException {
		wrappedSubmit(new DeleteElement("channels", channelName));
	}

	private class DeleteElement implements Runnable {
		private final String elementType;
		private final String elementName;

		DeleteElement(String elementType, String elementName) {
			super();
			this.elementType = elementType;
			this.elementName = elementName;
		}

		@Override
		public void run() {
			service.path(elementType).path(elementName).delete();
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
			deleteChannel(channel.build().getName());
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
		wrappedSubmit(new DeleteElementfromChannel("tags", tag.toXml()
				.getName(), channelName));
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
		wrappedSubmit(new DeleteElementfromChannel("properties", property
				.build().getName(), channelName));
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

	private class DeleteElementfromChannel implements Runnable {
		private final String elementType;
		private final String elementName;
		private final String channelName;

		DeleteElementfromChannel(String elementType, String elementName,
				String channelName) {
			super();
			this.elementType = elementType;
			this.elementName = elementName;
			this.channelName = channelName;
		}

		@Override
		public void run() {
			service.path(this.elementType).path(this.elementName)
					.path(this.channelName).accept(MediaType.APPLICATION_XML)
					.accept(MediaType.APPLICATION_JSON).delete();
		}

	}

	public void close() {
		this.executor.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!this.executor.awaitTermination(60, TimeUnit.SECONDS)) {
				this.executor.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!this.executor.awaitTermination(60, TimeUnit.SECONDS))
					System.err.println("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			this.executor.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	private <T> T wrappedSubmit(Callable<T> callable) {
		try {
			return this.executor.submit(callable).get();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			if (e.getCause() != null
					&& e.getCause() instanceof UniformInterfaceException) {
				throw new ChannelFinderException(
						(UniformInterfaceException) e.getCause());
			}
			throw new RuntimeException(e);
		}
	}

	private void wrappedSubmit(Runnable runnable) {
		try {
			this.executor.submit(runnable).get();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			if (e.getCause() != null
					&& e.getCause() instanceof UniformInterfaceException) {
				throw new ChannelFinderException(
						(UniformInterfaceException) e.getCause());
			}
			throw new RuntimeException(e);
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

}
