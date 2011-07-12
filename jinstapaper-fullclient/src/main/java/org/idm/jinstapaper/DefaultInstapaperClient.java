package org.idm.jinstapaper;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.oauth.client.OAuthClientFilter;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthSecrets;
import org.apache.log4j.Logger;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * An Java client for Full API  http://www.instapaper.com/api/full
 *
 * @author Denis Zontak
 */
public class DefaultInstapaperClient {


	private static final Logger log = Logger.getLogger(DefaultInstapaperClient.class);

	private static final String INSTAPAPER_BASE_API_URL = "https://www.instapaper.com";
	private Client client;
	private final ClientConfig config = new DefaultClientConfig();
	private String _username;
	private String _password;
	private static final Properties PROPERTIES = new Properties();
	private static final String PROPERTIES_FILE_NAME = "jinstapaper.properties";
	private static final String PROPERTY_CONSUMER_KEY = "oauth.consumer.key";
	private static final String PROPERTY_CONSUMER_SECRET = "oauth.consumer.secret";
	private String _token = null;
	private String _tokenSecret = null;

	public DefaultInstapaperClient(final String username, final String password) {
		loadSettings();
		this._username = username;
		this._password = password;
		client = Client.create(config);
		client.addFilter(new LoggingFilter());

		// create a new auth handler
		final OAuthClientFilter.AuthHandler authHandler = new OAuthClientFilter.AuthHandler() {
			public void authorized(final String token, final String tokenSecret) {
				// we received an authorized access token - store it for future runs
				_token = token;
				_tokenSecret = tokenSecret;

				client.addFilter(new OAuthClientFilter(client.getProviders(),
				new OAuthParameters().consumerKey(PROPERTIES.getProperty(PROPERTY_CONSUMER_KEY)).token(_token),
				new OAuthSecrets().consumerSecret(PROPERTIES.getProperty(PROPERTY_CONSUMER_SECRET))
						.tokenSecret(_tokenSecret)));

				if (log.isDebugEnabled()) {
					log.debug(String.format("oAuth authorized token=%s, tokenSecret=%s", token, tokenSecret));
				}
			}

			public String authorize(final URI authorizationUri) {

				if (log.isDebugEnabled()) {
					log.debug(String.format("oAuth authorizing url %s", authorizationUri));
				}

				final WebResource resource = client.resource(authorizationUri);
				final MultivaluedMap postData = new MultivaluedMapImpl();
				postData.add("x_auth_username", username);
				postData.add("x_auth_password", password);
				postData.add("x_auth_mode", "client_auth");
				final ClientResponse response = resource.type(MediaType.APPLICATION_FORM_URLENCODED)
						.post(ClientResponse.class, postData);
				final String entity = response.getEntity(String.class);
				if (log.isDebugEnabled()) {
					log.debug(String.format("oAuth authorization response %s", entity));
				}
				return entity;
			}
		};

		// create a new OAuth client oAuthClientFilter passing the needed info as well as the AuthHandler
		final OAuthClientFilter oAuthClientFilter = new OAuthClientFilter(client.getProviders(),
				new OAuthParameters().consumerKey(PROPERTIES.getProperty(PROPERTY_CONSUMER_KEY)),
				new OAuthSecrets().consumerSecret(PROPERTIES.getProperty(PROPERTY_CONSUMER_SECRET)));

		// Add oAuthClientFilter to the client
		client.addFilter(oAuthClientFilter);

		//oauth_token=KjSy3vu4pefj5KaMOrsP8gBUjtUki8OuDf7sQERsL9TJ1vQSQY&oauth_token_secret=1CeJdzYXVQYKlOFISRvfAMhg4u4k96x6F5JvgAvNvgNmKcm2TD

		final String authorizationTocketAndSecret = authHandler
				.authorize(UriBuilder.fromUri(INSTAPAPER_BASE_API_URL + "/api/1/oauth/access_token").build());
		final String[] tokens = StringUtils.split(authorizationTocketAndSecret, "&");
		final Map<String,String> aouthTokenMap = new HashMap<String, String>();
		final String[] oauth_token = StringUtils.split(tokens[0], "=");
		final String[] oauth_token_secret = StringUtils.split(tokens[1], "=");
		aouthTokenMap.put(oauth_token[0], oauth_token[1]);
		aouthTokenMap.put(oauth_token_secret[0], oauth_token_secret[1]);
		authHandler.authorized(aouthTokenMap.get("oauth_token"), aouthTokenMap.get("oauth_token_secret"));


	}


	// Account methods

	/**
	 * Gets an OAuth access token for a user. HTTPS is required: https://www.instapaper.com/api/1/oauth/access_token
	 */


	private static void loadSettings() {

		FileInputStream st = null;
		try {
			st = new FileInputStream(ResourceUtils.getFile("classpath:" + PROPERTIES_FILE_NAME));
			PROPERTIES.load(st);
		} catch (IOException e) {
			// ignore
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (IOException ex) {
					// ignore
				}
			}
		}

		for (final String name : new String[]{PROPERTY_CONSUMER_KEY, PROPERTY_CONSUMER_SECRET}) {
			final String value = System.getProperty(name);
			if (value != null) {
				PROPERTIES.setProperty(name, value);
			}
		}

		if (PROPERTIES.getProperty(PROPERTY_CONSUMER_KEY) == null ||
				PROPERTIES.getProperty(PROPERTY_CONSUMER_SECRET) == null) {
			throw new IllegalArgumentException(String.format("No consumerKey and/or consumerSecret found in %s file. " +
					"You have to provide these as system properties.", PROPERTIES_FILE_NAME));
		}
	}

	private static void storeSettings() {
		FileOutputStream st = null;
		try {
			st = new FileOutputStream(PROPERTIES_FILE_NAME);
			PROPERTIES.store(st, null);
		} catch (IOException e) {
			// ignore
		} finally {
			try {
				st.close();
			} catch (IOException ex) {
				// ignore
			}
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
	public String listBookmarks(final String limit, final String folderId, final String... bookmarkId) {
		final WebResource resource = client.resource(INSTAPAPER_BASE_API_URL).path("/api/1/bookmarks/list");
		final MultivaluedMap postData = new MultivaluedMapImpl();
		if (limit != null) {
			postData.add("limit", limit);
		}
		if (folderId != null) {
			postData.add("folder_id", folderId);
		}
		if (bookmarkId != null) {
			postData.add("have", "client_auth");
		}
		final ClientResponse response = resource.type(MediaType.APPLICATION_FORM_URLENCODED)
				.post(ClientResponse.class, postData);
		return response.getEntity(String.class);


	}
}
