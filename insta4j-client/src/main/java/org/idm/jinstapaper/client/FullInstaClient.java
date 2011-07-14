package org.idm.jinstapaper.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.client.impl.CopyOnWriteHashMap;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.oauth.client.OAuthClientFilter;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthSecrets;
import org.apache.log4j.Logger;
import org.idm.jinstapaper.client.config.DefaultInstaClientConfig;
import org.idm.jinstapaper.client.config.InstaClientConfig;
import org.idm.jinstapaper.jaxb.InstaRecordBean;
import org.idm.jinstapaper.jsonp.JAXBContextResolver;
import org.springframework.util.StringUtils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.util.*;

import static com.sun.jersey.api.client.ClientResponse.Status.fromStatusCode;
import static java.util.Arrays.asList;

/**
 * An Java client for Full API  http://www.instapaper.com/api/full
 *
 * @author Denis Zontak
 */
public class FullInstaClient {

	private static final Logger log = Logger.getLogger(FullInstaClient.class);
	private static final String INSTAPAPER_BASE_API_URL = "https://www.instapaper.com";
	private Client client;
	private final ClientConfig config = new DefaultClientConfig();
	private final InstaClientConfig instaConfig;

	private String _username;
	private String _password;
	private static final Properties PROPERTIES = new Properties();
	private String _token = null;
	private String _tokenSecret = null;
	private Stack<OAuthClientFilter> oAuthClientFilterStack = new Stack<OAuthClientFilter>();
	private CopyOnWriteHashMap<String, Object> properties;


	public Map<String, Object> getProperties() {
		if (properties == null) {
			properties = new CopyOnWriteHashMap<String, Object>();
		}
		return properties;
	}

	public FullInstaClient() {
		this(null, null);
	}

	public FullInstaClient(final InstaClientConfig instaClientConfig) {
		this(null, null, instaClientConfig);
	}

	public FullInstaClient(final String username, final String password) {
		this(username, password, new DefaultInstaClientConfig());
	}


	/**
	 * Sets up the Jersy  {@link Client} with {@JAXBContextResolver} and  {@link OAuthClientFilter}
	 * Gets an OAuth access token for a user via {@link #authorize(String, String)}
	 *
	 * @param username		  Instapaper username
	 * @param password		  Optional Instapaper password
	 * @param instaClientConfig The client configuration.
	 * @throws InvalidCredentialsException If username and password are not valid.
	 */
	public FullInstaClient(final String username, final String password, final InstaClientConfig instaClientConfig) {
		this.username(username);
		this.password(password);
		this.instaConfig = instaClientConfig;
		// maps json to Jaxb bean InstaRecordBean
		config.getClasses().add(JAXBContextResolver.class);
		client = Client.create(config);
		//TODO: make this configurable.
		client.addFilter(new LoggingFilter());

		// create a new OAuth client oAuthClientFilter passing the needed info
		final OAuthClientFilter oAuthClientFilter = new OAuthClientFilter(client.getProviders(), new OAuthParameters()
				.consumerKey((String) instaConfig.getProperty(InstaClientConfig.PROPERTY_CONSUMER_KEY)),
				new OAuthSecrets()
						.consumerSecret((String) instaConfig.getProperty(InstaClientConfig.PROPERTY_CONSUMER_SECRET)));
		// Add oAuthClientFilter to the client
		client.addFilter(oAuthClientFilter);
		/**
		 * 		store a reference to the oAuth filter on the stack so that we can easily remove it when
		 * 	    we are registering a filter with token and token secret after authorization is successfull.
		 */
		oAuthClientFilterStack.push(oAuthClientFilter);

		/**
		 * TODO: the initial authorization request should be made by the users of this client, and probably not  during constructions of this object.
		 */
		// get the token and tokenSecret for the user.
		final Map<String, String> aouthTokenMap = this.authorize(username, password);

	}

	public FullInstaClient username(final String username) {
		this._username = username;
		return this;
	}

	public FullInstaClient password(final String password) {
		this._password = password;
		return this;
	}

	private FullInstaClient token(final String token) {
		this._token = token;
		return this;
	}

	private FullInstaClient tokenSecret(final String tokenSecret) {
		this._tokenSecret = tokenSecret;
		return this;
	}

	public static FullInstaClient create(final String username, final String password) {
		return new FullInstaClient(username, password);
	}

	/**
	 * A Factory method to create the instace of the client.
	 * You need to set username and password on the client via the builder methods.
	 *
	 * @return An new client instace with username and password set to null.
	 */
	public static FullInstaClient create() {
		return new FullInstaClient();
	}


	protected void authorized(final String token, final String tokenSecret) {
		token(token);
		tokenSecret(tokenSecret);
		// remove the auth filter already registered either in the constructor or by this method.
		client.removeFilter(oAuthClientFilterStack.pop());
		// now add a oAuth filter with the token and secret
		final OAuthClientFilter oAuthClientFilter = new OAuthClientFilter(client.getProviders(), new OAuthParameters()
				.consumerKey((String) instaConfig.getProperty(InstaClientConfig.PROPERTY_CONSUMER_KEY)).token(_token),
				new OAuthSecrets()
						.consumerSecret((String) instaConfig.getProperty(InstaClientConfig.PROPERTY_CONSUMER_SECRET))
						.tokenSecret(_tokenSecret));
		client.addFilter(oAuthClientFilter);
		/**
		 *  push the newly registered filter on the stack so we can track it and unregisterd it before authorizing
		 *  next time.
		 */
		oAuthClientFilterStack.push(oAuthClientFilter);

		if (log.isDebugEnabled()) {
			log.debug(String.format("oAuth authorized token=%s, tokenSecret=%s", token, tokenSecret));
		}
	}

	/**
	 * Lists the user’s unread bookmarks, and can also synchronize reading positions.
	 *
	 * @param limit	  Optional. A number between 1 and 500, default 25.
	 * @param folderId   Optional. Possible values are unread (default), starred, archive,
	 *                   or a folder_id value from /api/1/folders/list.
	 * @param bookmarkId Optional. A concatenation of bookmark_id values that the client already has from the specified folder. See below.
	 *                   <p>
	 *                   The “have” parameter: This is a comma-separated list of bookmark_id values that the client
	 *                   already has in its local bookmark data, and shouldn’t be re-sent. Any IDs sent in the have parameter
	 *                   that would not have appeared in the list within the given limit are returned in a delete_ids parameter on the meta object.
	 *                   </p>
	 *                   <p>
	 *                   The have parameter can just be a list of the bookmark_id values, e.g.:
	 *                   12345,12346,12347
	 *                   …in which case Instapaper won’t include those bookmarks in the output.
	 *                   </p>
	 *                   <p>
	 *                   But it can also do more. Each bookmark returned by the API has a hash value, which is computed from its URL, title, description, and reading progress. If you join the hash value you have for an article with its article ID using a colon, e.g.:
	 *                   12345:OjMuzFp6,12346:0n4ONgYs,12347:YXo82wTR
	 *                   …then Instapaper will omit those bookmarks from the output, but only if the hashes haven’t changed. So you can use this method to selectively be informed of updates to article metadata without otherwise re-downloading the entire list on each update.
	 *                   </p>
	 *                   <p>
	 *                   Finally, you can optionally append two more fields to each ID with colons to indicate how far the user has read each article, as a floating-point value between 0.0 and 1.0 to indicate progress and the Unix timestamp value of the time that the progress was recorded, e.g.:
	 *                   12345:OjMuzFp6:0.5:1288584076
	 *                   This would indicate that the bookmark with bookmark_id=12345 and hash=OjMuzFp6 was read to 0.5 progress, or 50% of its length, at timestamp 1288584076 (2010-11-01 12:01:16am EST). If the server’s information is less recent than this, it will update the bookmark and return it in the output with a new hash value.
	 *                   </p>
	 * @return One meta object, the current user, and between 0 and limit bookmarks.
	 */
	public String listBookmarksJson(final String limit, final String folderId, final String... bookmarkId) {
		final WebResource resource = client.resource(INSTAPAPER_BASE_API_URL).path("/api/1/bookmarks/list");
		final MultivaluedMap postData = new MultivaluedMapImpl();
		if (limit != null) {
			postData.add("limit", limit);
		}
		if (folderId != null) {
			postData.add("folder_id", folderId);
		}
		if (bookmarkId != null) {
			postData.add("have", StringUtils.collectionToDelimitedString(asList(bookmarkId), ","));
		}
		final ClientResponse response = resource.type(MediaType.APPLICATION_FORM_URLENCODED)
				.accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, postData);
		return response.getEntity(String.class);
	}


	public List<InstaRecordBean> listBookmarks(final String limit, final String folderId, final String... bookmarkId) {
		final WebResource resource = client.resource(INSTAPAPER_BASE_API_URL).path("/api/1/bookmarks/list");
		final MultivaluedMap postData = new MultivaluedMapImpl();
		if (limit != null) {
			postData.add("limit", limit);
		}
		if (folderId != null) {
			postData.add("folder_id", folderId);
		}
		if (bookmarkId != null) {
			postData.add("have", StringUtils.collectionToDelimitedString(asList(bookmarkId), ","));
		}
		final List<InstaRecordBean> instaRecordBeans = resource.type(MediaType.APPLICATION_FORM_URLENCODED)
				.accept(MediaType.APPLICATION_JSON).post(new GenericType<List<InstaRecordBean>>() {
				}, postData);

		return instaRecordBeans;

	}

	/**
	 * Returns the currently logged in user.
	 * Output on success: A user object, e.g.
	 * [{"type":"user","user_id":54321,"username":"TestUserOMGLOL"}]
	 *
	 * @return A record representing the currently logged in user or an error record.
	 */
	public InstaRecordBean verifyCredentials() {
		final WebResource resource = client.resource(INSTAPAPER_BASE_API_URL).path("/api/1/account/verify_credentials");
		final List<InstaRecordBean> instaRecordBeans = resource.accept(MediaType.APPLICATION_JSON)
				.post(new GenericType<List<InstaRecordBean>>() {
				});

		return instaRecordBeans.iterator().hasNext() ? instaRecordBeans.iterator().next() : null;
	}


	/**
	 * Gets an OAuth access token for a user.
	 * This method also stored the token and secret internally so that it can be passed on every
	 * subsiquent request in Authentication header.
	 *
	 * @param username An instapaper user name
	 * @param password An optional password.
	 * @return A Map containing key 'oauth_token' with oauth user token
	 *         and a value of token secret under the key 'oauth_token_secret'
	 * @throws InvalidCredentialsException A RuntimeException is thrown if the user authentication failed
	 * @throws RuntimeException			Is thrown authorization with oAuth failed, the message will be what Instapaper Full
	 *                                     api returns in case of an error.
	 */
	public Map<String, String> authorize(final String username, final String password) {
		final WebResource resource = client
				.resource(UriBuilder.fromUri(INSTAPAPER_BASE_API_URL + "/api/1/oauth/access_token").build());
		final MultivaluedMap postData = new MultivaluedMapImpl();
		postData.add("x_auth_username", username);
		postData.add("x_auth_password", password);
		postData.add("x_auth_mode", "client_auth");
		final ClientResponse response = processResponse(
				resource.type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class, postData));

		final String entity = response.getEntity(String.class);
		final String[] tokens = entity.split("&");
		final Map<String, String> aouthTokenMap = new HashMap<String, String>(2);
		final String[] oauth_token = StringUtils.split(tokens[0], "=");
		final String[] oauth_token_secret = StringUtils.split(tokens[1], "=");
		aouthTokenMap.put(oauth_token[0], oauth_token[1]);
		aouthTokenMap.put(oauth_token_secret[0], oauth_token_secret[1]);

		// signale the client that oAuth token and secret had been recived.
		authorized(aouthTokenMap.get("oauth_token"), aouthTokenMap.get("oauth_token_secret"));

		return aouthTokenMap;
	}


	private ClientResponse processResponse(final ClientResponse response) {
		switch (fromStatusCode(response.getStatus())) {
			case OK:
				return response;
			case CREATED:
				return response;
			case BAD_REQUEST: // Bad request or exceeded the rate limit. Probably missing a required parameter,
				// such as url.
				throw new IllegalArgumentException(response.getEntity(String.class));
			case FORBIDDEN: // Invalid username or password.
				throw new InvalidCredentialsException(response.getEntity(String.class));
			case UNAUTHORIZED: // Invalid xAuth credentials..
				throw new InvalidCredentialsException(response.getEntity(String.class));
			case INTERNAL_SERVER_ERROR: // The service encountered an error. Please try again later.
				throw new RuntimeException(response.getEntity(String.class));
			default:
				throw new RuntimeException(
						String.format("Instapaper api returned an unknown code '%s'", response.getStatus()));
		}
	}
}
