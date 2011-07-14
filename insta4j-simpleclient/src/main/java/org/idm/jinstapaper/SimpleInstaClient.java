package org.idm.jinstapaper;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import javax.security.auth.login.FailedLoginException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * A java client for Simple Instapaper api @see http://www.instapaper.com/api/simple
 *
 */
public class SimpleInstaClient {
	private static final String INSTAPAPER_BASE_API_URL = "https://www.instapaper.com/api";
	private String _username;
	private String _password;
	private Client client;
	private final ClientConfig config = new DefaultClientConfig();

	public SimpleInstaClient(final String username, final String password) {
		this._username = username;
		this._password = password;
		client = Client.create(config);
		// client basic authentication
		client.addFilter(new HTTPBasicAuthFilter(_username, _password));
		client.addFilter(new LoggingFilter());
	}


	private ClientResponse _authenticate(final String jsonp) throws FailedLoginException {

		final WebResource resource = client.resource(INSTAPAPER_BASE_API_URL).path("/authenticate");
		final MultivaluedMap postData = new MultivaluedMapImpl();
		if (jsonp != null) {
			postData.add("jsonp", jsonp);
		}
		final ClientResponse response = resource.type(MediaType.APPLICATION_FORM_URLENCODED)
				.post(ClientResponse.class, postData);
		return response;
	}

	public boolean authenticate() throws FailedLoginException {
		final ClientResponse response = processResponse(this._authenticate(null));
		return (response.getStatus() == Response.Status.OK.getStatusCode());
	}

	public String authenticate(final String jsonp) throws FailedLoginException {
		final ClientResponse response = this._authenticate(jsonp);
		return response.getEntity(String.class);

	}


	/**
	 * Adds URLs to an Instapaper account.
	 *
	 * @param url	   A valid url to cache to the instapaper account.
	 * @param title	 Optional, plain text, no HTML, UTF-8. If omitted or empty, Instapaper will crawl the URL to
	 *                  detect a title.
	 * @param selection optional, plain text, no HTML, UTF-8. Will show up as the description under an item in the interface.
	 *                  Some clients use this to describe where it came from, such as the text of the source Twitter post
	 *                  when sending a link from a Twitter client.
	 * @return A Map containing all the response headers, when response 201 is returned 2 additional headers will be
	 *         added.
	 *         <p>
	 *         Content-Location: The saved URL for the newly created item, after any normalization and redirect-following.
	 *         X-Instapaper-Title: The saved title for the page, after any auto-detection.
	 *         if jasonp value was passed in the map will contain a javascript callback function under key 'jasonp'
	 *         </p>
	 * @throws javax.security.auth.login.FailedLoginException Is thrown if username or password are invalid.
	 * @throws RuntimeException	 Is thrown if a code other than 201 is returned with an appropriate error description.
	 */
	public MultivaluedMap<String, String> add(final String url, final String title, final String selection)
			throws FailedLoginException {
		return processResponse(this._add(url, title, selection, null, null)).getHeaders();

	}


	/**
	 * Adds URLs to an Instapaper account.
	 *
	 * @param url	   A valid url to cache to the instapaper account.
	 * @param title	 Optional, plain text, no HTML, UTF-8. If omitted or empty, Instapaper will crawl the URL to
	 *                  detect a title.
	 * @param selection optional, plain text, no HTML, UTF-8. Will show up as the description under an item in the interface.
	 *                  Some clients use this to describe where it came from, such as the text of the source Twitter post
	 *                  when sending a link from a Twitter client.
	 * @param redirect  optional. (value supported 'close')  Specifies that, instead of returning the status code,
	 *                  the resulting page should show an HTML “Saved!” notification that attempts to close its own window with Javascript after a short delay.
	 * @param jsonp	 To receive results as JSON to a specified callback function, pass a valid Javascript function
	 *                  name as the jsonp parameter to Add or Authenticate, e.g. jsonp=callbackName.
	 * @return A json response
	 * @throws javax.security.auth.login.FailedLoginException Is thrown if username or password are invalid.
	 * @throws RuntimeException	 Is thrown if a code other than 201 is returned with an appropriate error description.
	 */
	public String add(final String url, final String title, final String selection, final String redirect,
			final String jsonp) throws FailedLoginException {
		return _add(url, title, selection, redirect, jsonp).getEntity(String.class);
	}


	private ClientResponse _add(final String url, final String title, final String selection, final String redirect,
			final String jsonp) throws FailedLoginException {

		final WebResource resource = client.resource(INSTAPAPER_BASE_API_URL).path("/add");
		final MultivaluedMap postData = new MultivaluedMapImpl();
		postData.add("url", url);
		if (title != null) {
			postData.add("title", title);
		}
		if (selection != null) {
			postData.add("selection", selection);
		}
		if (redirect != null) {
			postData.add("redirect", redirect);
		}
		if (jsonp != null) {
			postData.add("jsonp", jsonp);
		}

		final ClientResponse response = resource.type(MediaType.APPLICATION_FORM_URLENCODED)
				.post(ClientResponse.class, postData);
		return response;

	}


	private ClientResponse processResponse(final ClientResponse response) throws FailedLoginException {
		switch (response.getStatus()) {
			case 200:
				return response;
			case 201:
				return response;
			case 400: // Bad request or exceeded the rate limit. Probably missing a required parameter, such as url.
				throw new IllegalArgumentException(
						"Bad request or exceeded the rate limit. Probably missing a required " +
								"parameter, such as url.");
			case 403: // Invalid username or password.
				throw new FailedLoginException("Invalid credentials provided");
			case 500: // The service encountered an error. Please try again later.
				throw new RuntimeException("The service encountered an error. Please try again later.");
			default:
				throw new RuntimeException(
						String.format("Instapaper api returned an unknown code '%s'", response.getStatus()));
		}
	}
}
