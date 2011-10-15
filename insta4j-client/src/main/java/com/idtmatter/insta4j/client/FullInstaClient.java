/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.idtmatter.insta4j.client;

import com.idtmatter.insta4j.InstaCodes;
import com.idtmatter.insta4j.client.config.DefaultInstaClientConfig;
import com.idtmatter.insta4j.client.config.InstaClientConfig;
import com.idtmatter.insta4j.jaxb.InstaRecordBean;
import com.idtmatter.insta4j.jsonp.JAXBContextResolver;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.client.impl.CopyOnWriteHashMap;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.oauth.client.OAuthClientFilter;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthSecrets;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static java.util.Arrays.asList;

/**
 * An Java client for Full API  based on documentation at http://www.instapaper.com/api/full
 *
 * @author dzontak@gmail.com
 * @author Sajit Kunnumkal
 */
public class FullInstaClient implements InstaClient {

	private static final Logger log = Logger.getLogger(FullInstaClient.class);
	private static final String INSTAPAPER_BASE_API_URL = "https://www.instapaper.com";
	private final Client client;
	private final InstaClientConfig instaConfig;
	private String _token = null;
	private String _tokenSecret = null;
	private final Stack<ClientFilter> filterStack = new Stack<ClientFilter>();
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

	public FullInstaClient(@Nullable final String username, @Nullable final String password) {
		this(username, password, new DefaultInstaClientConfig());
	}


	/**
	 * Sets up the Jersy  {@link Client} with {@link com.idtmatter.insta4j.jsonp.JAXBContextResolver} and  {@link OAuthClientFilter}
	 * Gets an OAuth access token for a user via {@link #authorize(String, String)}
	 *
	 * @param username		  Instapaper username
	 * @param password		  Optional Instapaper password
	 * @param instaClientConfig The client configuration.
	 * @throws com.idtmatter.insta4j.InvalidCredentialsException
	 *          If username and password are not valid.
	 */
	public FullInstaClient(@Nullable final String username, @Nullable final String password,
			final InstaClientConfig instaClientConfig) {
		this.instaConfig = instaClientConfig;
		// maps json to Jaxb bean InstaRecordBean
		final ClientConfig config = new DefaultClientConfig();
		config.getClasses().add(JAXBContextResolver.class);
		client = Client.create(config);
		if (log.isDebugEnabled()) {
			client.addFilter(new LoggingFilter());
		}

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
		filterStack.push(oAuthClientFilter);

		/**
		 * TODO: the initial authorization request should be made by the users of this client, and
		 * probably not  during constructions of this object.
		 */
		// get the token and tokenSecret for the user.
		authorize(username, password);

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


	private void authorized(@NotNull final String token, @NotNull final String tokenSecret) {
		token(token);
		tokenSecret(tokenSecret);
		// remove the auth filter already registered either in the constructor or by this method.
		client.removeFilter(filterStack.pop());
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
		filterStack.push(oAuthClientFilter);

		if (log.isDebugEnabled()) {
			log.debug(String.format("oAuth authorized token=%s, tokenSecret=%s", token, tokenSecret));
		}
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
		final ClientResponse response = processResponse(
				resource.type(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON)
						.post(ClientResponse.class));
		final List<InstaRecordBean> instaRecordBeans = response.getEntity(new GenericType<List<InstaRecordBean>>() {
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
	 * @throws com.idtmatter.insta4j.InvalidCredentialsException
	 *                          A RuntimeException is thrown if the user authentication failed
	 * @throws RuntimeException Is thrown authorization with oAuth failed, the message will be what Instapaper Full
	 *                          api returns in case of an error.
	 */
	public Map<String, String> authorize(@NotNull final String username, @NotNull final String password) {
		final WebResource resource = client
				.resource(UriBuilder.fromUri(INSTAPAPER_BASE_API_URL + "/api/1/oauth/access_token").build());
		final MultivaluedMap<String, String> postData = new MultivaluedMapImpl();
		postData.add("x_auth_username", username);
		postData.add("x_auth_password", password);
		postData.add("x_auth_mode", "client_auth");
		final ClientResponse response = processResponse(
				resource.type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class, postData));

		final String entity = response.getEntity(String.class);
		final String[] tokens = entity.split("&");
		final Map<String, String> aouthTokenMap = new HashMap<String, String>(2);
		final String[] oauth_token = tokens[0].split("=");
		final String[] oauth_token_secret = tokens[1].split("=");
		aouthTokenMap.put(oauth_token[0], oauth_token[1]);
		aouthTokenMap.put(oauth_token_secret[0], oauth_token_secret[1]);

		// signal the client that oAuth token and secret had been recived.
		authorized(aouthTokenMap.get("oauth_token"), aouthTokenMap.get("oauth_token_secret"));

		return aouthTokenMap;
	}

	/**
	 * Lists the user's unread bookmarks, and can also synchronize reading positions.
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
	public List<InstaRecordBean> listBookmarks(final String limit, final String folderId, final String... bookmarkId) {
		final WebResource resource = client.resource(INSTAPAPER_BASE_API_URL).path("/api/1/bookmarks/list");
		final MultivaluedMap<String, String> postData = new MultivaluedMapImpl();
		if (limit != null) {
			postData.add("limit", limit);
		}
		if (folderId != null) {
			postData.add("folder_id", folderId);
		}
		if (bookmarkId != null) {
			postData.add("have", StringUtils.collectionToDelimitedString(asList(bookmarkId), ","));
		}

		final List<InstaRecordBean> instaRecordBeans = processJsonResponse(
				resource.type(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON)
						.post(ClientResponse.class, postData));

		return instaRecordBeans;
	}


	/**
	 * Updates the user's reading progress on a single article.
	 * This functionality is included in the have parameter of the /api/1/bookmarks/list operation above,
	 * but this method exists in case you want to call it separately.
	 *
	 * @param bookmarkId:        The bookmark to update.
	 * @param progress:          The user's progress, as a floating-point number between 0.0 and 1.0,
	 *                           defined as the top edge of the user's current viewport, expressed as a percentage of the article’s total length.
	 * @param progressTimestamp: The Unix timestamp value of the time that the progress was recorded.
	 * @return The modified bookmark on success.
	 */

	public InstaRecordBean updateReadProgress(@NotNull final String bookmarkId, @NotNull final Double progress,
			@NotNull final Long progressTimestamp) {
		final WebResource resource = client.resource(INSTAPAPER_BASE_API_URL)
				.path("/api/1/bookmarks/update_read_progress");
		final MultivaluedMap<String, String> postData = new MultivaluedMapImpl();
		postData.add("bookmark_id", bookmarkId);
		postData.add("progress", Double.toString(progress));
		postData.add("progress_timestamp", Long.toString(progressTimestamp));

		final List<InstaRecordBean> instaRecordBeans = processJsonResponse(
				resource.type(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON)
						.post(ClientResponse.class, postData));
		return (instaRecordBeans.iterator().hasNext() ? instaRecordBeans.iterator().next() : null);
	}

	/**
	 * /api/1/bookmarks/add
	 * Adds a new unread bookmark to the user's account.
	 *
	 * @param url:               Required, except when using private sources (see below).
	 * @param title:             Optional. If omitted or empty, the title will be looked up by Instapaper synchronously. This will delay the action, so please specify the title if you have it.
	 *                           description: Optional. A brief, plaintext description or summary of the article. Twitter clients often put the source tweet’s text here, and Instapaper’s bookmarklet puts the selected text here if the user has selected any.
	 * @param folder_id:         Optional. The integer folder ID as returned by the folder/list method described below.
	 * @param resolve_final_url: Optional, default 1. Specify 1 if the url might not be the final URL that a browser would resolve when fetching it, such as if it’s a shortened URL, it’s a URL from an RSS feed that might be proxied, or it’s likely to go through any other redirection when viewed in a browser. This will cause Instapaper to attempt to resolve all redirects itself, synchronously. This will delay the action, so please specify 0 for this parameter if you’re reasonably confident that this URL won’t be redirected, such as if it’s already being viewed in a web browser.
	 * @return The bookmark on success, which may not be new: if this user already saved this URL,
	 *         it will be moved to the top of the Unread list (or the specified folder) and assigned the new values for title, description, and content (see below).
	 *         In addition to standard invalid-parameter errors, you may encounter these special errors:
	 *         <p/>
	 *         1220: Domain requires full content to be supplied — Pages from this domain require the content parameter below, usually because a login or subscription is required to get their content, and you haven’t supplied it. If you already have the page loaded in a browser environment, simply re-submit with the body contents (e.g. Javascript’s document.body.innerHTML). If not, you may need to open it in a browser first, or give the user a helpful message explaining that content from this site needs to be viewed in a browser before saving to Instapaper.
	 *         1221: Domain has opted out of Instapaper compatibility — The publisher of this domain has requested that Instapaper not save any of its content. Do not attempt to work around this, which is prohibited in the API Terms of Use. Please give the user a helpful message, where applicable, that the publishers of this site do not permit Instapaper usage.
	 *         Supplying HTML content for a bookmarkYou can optionally supply the full HTML content of pages that Instapaper wouldn’t otherwise be able to crawl from its servers, such as pages that require logins or subscriptions.
	 *         <p/>
	 *         content: The full HTML content of the page, or just the <body> node’s content if possible, such as the value of document.body.innerHTML in Javascript. Must be UTF-8.
	 *         A bookmark’s content is not shared to other users through any sharing functionality (such as Starred-folder subscriptions). It is only used to generate the text version of the bookmark for the user that created it.
	 *         This is not for material that doesn’t have a dedicated, permanent URL or should be excluded from sharing and tracking functionality completely, such as a private message on a social network, driving directions, or the results of a session. For that, see “Private sources” below.
	 *         Private sourcesBookmarks can be private, such as the bookmarks that result from Instapaper’s email-in text feature. Private bookmarks are not shared to other users through any sharing functionality (such as Starred-folder subscriptions), and they do not have URLs.
	 *         Set this parameter to a non-empty string to set a bookmark to private:
	 *         <p/>
	 *         is_private_from_source: A short description label of the source of the private bookmark, such as “email” or “MyNotebook Pro”.
	 *         When using this, values passed to url will be ignored, and the content parameter (above) is required.
	 *         In any bookmark objects output by this API, the private_source field will be an empty string for public bookmarks, and this value for private bookmarks. They will have an automatically generated, non-functioning value in the url field of the form instapaper://private-content/.... Do not use these URLs in your application.
	 */

	public InstaRecordBean addBookmark(final String url, final String title, final String folder_id,
			final Boolean resolve_final_url) {
		final WebResource resource = client.resource(INSTAPAPER_BASE_API_URL).path("/api/1/bookmarks/add");
		final MultivaluedMap<String, String> postData = new MultivaluedMapImpl();
		postData.add("url", url);

		if (title != null) {
			postData.add("title", title);
		}
		if (folder_id != null) {
			postData.add("folder_id", folder_id);
		}
		if (resolve_final_url != null) {
			postData.add("resolve_final_url", resolve_final_url ? "1" : "0");
		}

		final List<InstaRecordBean> instaRecordBeans = processJsonResponse(
				resource.type(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON)
						.post(ClientResponse.class, postData));
		return (instaRecordBeans.iterator().hasNext() ? instaRecordBeans.iterator().next() : null);
	}

	/**
	 * Permanently deletes the specified bookmark. This is NOT the same as Archive. Please be clear to users if you’re going to do this.
	 *
	 * @param bookmark_id An empty array on success.
	 * @return True on sucess
	 */
	public Boolean deleteBookmark(final String bookmark_id) {
		final WebResource resource = client.resource(INSTAPAPER_BASE_API_URL).path("/api/1/bookmarks/delete");
		final MultivaluedMap<String, String> postData = new MultivaluedMapImpl();
		postData.add("bookmark_id", bookmark_id);

		final List<InstaRecordBean> instaRecordBeans = processJsonResponse(
				resource.type(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON)
						.post(ClientResponse.class, postData));
		return (instaRecordBeans.iterator().hasNext() ? true : false);
	}

	/**
	 * Stars the specified bookmark.
	 *
	 * @param bookmark_id The identifier for a bookark
	 * @return The modified bookmark on success.
	 */
	public InstaRecordBean starBookmark(final String bookmark_id) {
		final WebResource resource = client.resource(INSTAPAPER_BASE_API_URL).path("/api/1/bookmarks/star");
		final MultivaluedMap<String, String> postData = new MultivaluedMapImpl();
		postData.add("bookmark_id", bookmark_id);

		final List<InstaRecordBean> instaRecordBeans = processJsonResponse(
				resource.type(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON)
						.post(ClientResponse.class, postData));
		return instaRecordBeans.iterator().hasNext() ? instaRecordBeans.iterator().next() : null;
	}

	/**
	 * Un-stars the specified bookmark.
	 *
	 * @param bookmark_id The identifier for a bookark
	 * @return The modified bookmark on success.
	 */
	public InstaRecordBean unstarBookmark(final String bookmark_id) {
		final WebResource resource = client.resource(INSTAPAPER_BASE_API_URL).path("/api/1/bookmarks/unstar");
		final MultivaluedMap<String, String> postData = new MultivaluedMapImpl();
		postData.add("bookmark_id", bookmark_id);

		final List<InstaRecordBean> instaRecordBeans = processJsonResponse(
				resource.type(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON)
						.post(ClientResponse.class, postData));
		return instaRecordBeans.iterator().hasNext() ? instaRecordBeans.iterator().next() : null;
	}

	/**
	 * Moves the specified bookmark to the Archive.
	 *
	 * @param bookmark_id The identifier for a bookark
	 * @return The modified bookmark on success.
	 */
	public InstaRecordBean archiveBookmark(final String bookmark_id) {
		final WebResource resource = client.resource(INSTAPAPER_BASE_API_URL).path("/api/1/bookmarks/archive");
		final MultivaluedMap<String, String> postData = new MultivaluedMapImpl();
		postData.add("bookmark_id", bookmark_id);

		final List<InstaRecordBean> instaRecordBeans = processJsonResponse(
				resource.type(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON)
						.post(ClientResponse.class, postData));
		return instaRecordBeans.iterator().hasNext() ? instaRecordBeans.iterator().next() : null;
	}

	/**
	 * Moves the specified bookmark to the top of the Unread folder.
	 *
	 * @param bookmark_id The identifier for a bookark
	 * @return The modified bookmark on success.
	 */
	public InstaRecordBean unarchiveBookmark(final String bookmark_id) {
		final WebResource resource = client.resource(INSTAPAPER_BASE_API_URL).path("/api/1/bookmarks/unarchive");
		final MultivaluedMap<String, String> postData = new MultivaluedMapImpl();
		postData.add("bookmark_id", bookmark_id);

		final List<InstaRecordBean> instaRecordBeans = processJsonResponse(
				resource.type(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON)
						.post(ClientResponse.class, postData));

		return instaRecordBeans.iterator().hasNext() ? instaRecordBeans.iterator().next() : null;
	}


	/**
	 * Moves the specified bookmark to the top of the Unread folder.
	 *
	 * @param bookmark_id The identifier for a bookark
	 * @param folder_id   The identifier for a folder
	 * @return The modified bookmark on success.
	 */
	public InstaRecordBean moveBookmark(@NotNull final String bookmark_id, @NotNull final String folder_id) {
		final WebResource resource = client.resource(INSTAPAPER_BASE_API_URL).path("/api/1/bookmarks/move");
		final MultivaluedMap<String, String> postData = new MultivaluedMapImpl();
		postData.add("bookmark_id", bookmark_id);
		postData.add("folder_id", folder_id);

		final List<InstaRecordBean> instaRecordBeans = processJsonResponse(
				resource.type(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON)
						.post(ClientResponse.class, postData));
		return instaRecordBeans.iterator().hasNext() ? instaRecordBeans.iterator().next() : null;
	}

	/**
	 * Returns the specified bookmark’s processed text-view HTML, which is always text/html encoded as UTF-8.
	 *
	 * @param bookmark_id The identifier for a bookark
	 * @param folder_id   The identifier for a folder
	 * @return HTML with an HTTP 200 OK status, not the standard API output structures,
	 *         or an HTTP 400 status code and a standard error structure if anything goes wrong.
	 */
	public String getBookmark(@NotNull final String bookmark_id, @NotNull final String folder_id) {
		final WebResource resource = client.resource(INSTAPAPER_BASE_API_URL).path("/api/1/bookmarks/get_text");
		final MultivaluedMap<String, String> postData = new MultivaluedMapImpl();
		postData.add("bookmark_id", bookmark_id);
		postData.add("folder_id", folder_id);

		final ClientResponse response = processResponse(
				resource.type(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON)
						.post(ClientResponse.class, postData));

		return response.getEntity(String.class);
	}

	/**
	 * A list of the account’s user-created folders.
	 * <p/>
	 *
	 * @return A list of the account’s user-created folders. This only includes organizational folders and does
	 *         not include RSS-feed folders or starred-subscription folders, as the implementation of those is changing in the near future
	 */
	public List<InstaRecordBean> listFolders() {
		final WebResource resource = client.resource(INSTAPAPER_BASE_API_URL).path("/api/1/folders/list");
		//TODO: instapaper documentation suggests to always do a post, is get ok here.
		return processJsonResponse(
				resource.type(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON)
						.get(ClientResponse.class));
	}

	/**
	 * Creates an organizational folder.
	 *
	 * @param title The name of the folder.
	 * @return The newly created folder, or error 1251: “User already has a folder with this title”
	 *         if the title isn’t unique among this user's folders.
	 * @throws ResourceExistsException Is thrown if user already has a folder with this title
	 */
	public InstaRecordBean createFolder(@NotNull final String title) {
		final WebResource resource = client.resource(INSTAPAPER_BASE_API_URL).path("/api/1/folders/add");
		final MultivaluedMap<String, String> postData = new MultivaluedMapImpl();
		postData.add("title", title);

		final List<InstaRecordBean> instaRecordBeans = processJsonResponse(
				resource.type(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON)
						.post(ClientResponse.class, postData));

		return instaRecordBeans.iterator().hasNext() ? instaRecordBeans.iterator().next() : null;
	}

	/**
	 * Deletes the folder and moves any articles in it to the Archive.
	 *
	 * @param folder_id A unique identifier of the folder.
	 * @return True if the folder was deleted
	 * @throws ResourceExistsException Is thrown If the folder could not be created with a reason in the message.
	 */
	public Boolean deleteFolder(@NotNull final String folder_id) {
		final WebResource resource = client.resource(INSTAPAPER_BASE_API_URL).path("/api/1/folders/delete");
		final MultivaluedMap<String, String> postData = new MultivaluedMapImpl();
		postData.add("folder_id", folder_id);

		final List<InstaRecordBean> instaRecordBeans = processJsonResponse(
				resource.type(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON)
						.post(ClientResponse.class, postData));
		return (instaRecordBeans != null ? true : false);
	}

	/**
	 * ReOrders the position of the folders
	 *
	 * @param folderPositionMap A <K,V> map where K = Folder Id and V = position
	 * @return A list of the account’s user-created folders. This only includes organizational folders and does
	 *         not include RSS-feed folders or starred-subscription folders, as the implementation of those is changing in the near future
	 */
	public List<InstaRecordBean> setFolderOrder(final Map<Integer, Long> folderPositionMap) {
		final WebResource resource = client.resource(INSTAPAPER_BASE_API_URL).path("/api/1/folders/set_order");
		final MultivaluedMap<String, String> postData = new MultivaluedMapImpl();
		final StringBuilder stringBuilder = new StringBuilder();
		final Set<Integer> folderIds = folderPositionMap.keySet();
		for (final Integer folderId : folderIds) {
			stringBuilder
					.append(Integer.toString(folderId) + ":" + Long.toString(folderPositionMap.get(folderId)) + ",");
		}
		final String ordermap = StringUtils.trimTrailingCharacter(stringBuilder.toString(), ',');
		if (log.isDebugEnabled()) {
			log.debug(ordermap);
		}
		postData.add("order", ordermap);
		return processJsonResponse(resource.type(MediaType.APPLICATION_FORM_URLENCODED).
				accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, postData));

	}


	/**
	 * Handle http error codes and throw and exception assinged in the enumeration
	 * {@link com.idtmatter.insta4j.InstaCodes.Code#exceptionClass}
	 *
	 * @param response The jersey client response
	 * @return Back the response if If the the status Belongs to {@link com.idtmatter.insta4j.InstaCodes.Code.Family#SUCCESSFUL}
	 *         else throw and exception type assegned to {@link com.idtmatter.insta4j.InstaCodes.Code#exceptionClass} associated to the
	 *         {@link ClientResponse#status}
	 */
	private ClientResponse processResponse(final ClientResponse response) {
		final InstaCodes.Code code = InstaCodes.Code.fromCode(response.getStatus());
		if (code != null) {
			final Class<? extends RuntimeException> exceptionClass = code.getExceptionClass();
			if (exceptionClass == null) {
				return response;
			} else {
				// raise an exception
				try {
					throw exceptionClass.getConstructor(String.class).newInstance(code.getReasonPhrase());
				} catch (InstantiationException e) {
					//ignore
				} catch (IllegalAccessException e) {
					//ignore
				} catch (InvocationTargetException e) {
					//ignore
				} catch (NoSuchMethodException e) {
					//ignore
				}

				throw new RuntimeException(response.getEntity(String.class));
			}
		} else {
			// no code returned in the response everything must be ok.
			return response;
		}
	}

	/**
	 * Handle Instapaper error codes and throw and exception assinged in the enumeration
	 * {@link com.idtmatter.insta4j.InstaCodes.Code#exceptionClass}
	 *
	 * @param response jersey client response
	 * @return A collection of {@link InstaRecordBean}s if there is no error type retruned in the json response
	 *         else throw and exception type assigned to {@link com.idtmatter.insta4j.InstaCodes.Code#exceptionClass} associated
	 *         to the Instapaper error code returned in the response if any
	 */

	private List<InstaRecordBean> processJsonResponse(final ClientResponse response) {
		final List<InstaRecordBean> recordBeans = response.getEntity(new GenericType<List<InstaRecordBean>>() {
		});
		final List<InstaRecordBean> errorRecords = selectRecordsByType(recordBeans, RecordType.ERROR);
		// Should only contain zero or 1 codeEnum record.
		final InstaRecordBean errorRecord = (errorRecords.isEmpty() ? null : errorRecords.iterator().next());
		if (errorRecord != null) {
			final int code = Integer.parseInt(errorRecord.error_code);
			final InstaCodes.Code codeEnum = InstaCodes.Code.fromCode(code);
			final Class<? extends RuntimeException> exceptionClass =
					codeEnum != null ? codeEnum.getExceptionClass() : null;
			if (exceptionClass == null) {
				// {@link InstaCodes.Code#exceptionClass} is null the code is a success code return the records
				return recordBeans;
			} else {
				throw raiseRuntimeException(errorRecord, codeEnum, exceptionClass);
			}
		} else {
			return recordBeans;
		}
	}

	private RuntimeException raiseRuntimeException(final InstaRecordBean errorRecord, final InstaCodes.Code codeEnum,
			final Class<? extends RuntimeException> exceptionClass) {
		/**
		 * raise an exception based on {@link com.idtmatter.insta4j.InstaCodes.Code#exceptionClass}
		 */
		try {
			final String message = (errorRecord != null ?
					String.format("[%s] %s", errorRecord.error_code, errorRecord.message) : codeEnum.getReasonPhrase());
			final RuntimeException exception;
			if (exceptionClass.equals(InstaClientException.class)) {
				exception = exceptionClass.getConstructor(String.class, InstaCodes.Code.class)
						.newInstance(message, codeEnum);
			} else {
				exception = exceptionClass.getConstructor(String.class).newInstance(message);
			}
			return exception;
		} catch (Exception e) {
			throw new RuntimeException("The exception does not hava an appropriate constructor", e);
		}
	}

	/**
	 * Selects {@link InstaRecordBean}s where <param>recordType</param> equals {@link InstaRecordBean#type}
	 *
	 * @param recordBeanList A collection of {@link InstaRecordBean}s
	 * @param recordType	 A record type
	 * @return A collection of {@link InstaRecordBean}s that match to recordType
	 */
	public static List<InstaRecordBean> selectRecordsByType(final List<InstaRecordBean> recordBeanList,
			final RecordType recordType) {
		final List<InstaRecordBean> recordBeans = new ArrayList<InstaRecordBean>();
		for (final InstaRecordBean recordBean : recordBeanList) {
			if (recordType.type().equals(recordBean.type)) {
				recordBeans.add(recordBean);
			}
		}
		return recordBeans;
	}
}
