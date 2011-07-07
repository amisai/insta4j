package org.idm.jinstapaper;

import com.sun.jersey.test.framework.JerseyTest;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class AuthenticationServiceTest extends JerseyTest {

	private static final Logger LOG = Logger.getLogger(AuthenticationServiceTest.class);

	private final static String contextPath = "https://www.instapaper.com/api/authenticate";
	private final static String servletPath = "https://www.instapaper.com";
	private final static String resourcePackageName = "api.authenticate";

	public AuthenticationServiceTest() throws Exception {
		super(contextPath, servletPath, resourcePackageName);
	}

	/**
	 * Authentication
	 * <p/>
	 * This method validates an Instapaper username and password. Calling this before adding pages is not necessary. Use this if you want to only check credentials without adding a URL to Instapaper, such as when you first prompt the user for Instapaper credentials in a settings screen or on the first Instapaper request.
	 * <p/>
	 * URL: https://www.instapaper.com/api/authenticate
	 * <p/>
	 * Parameters:
	 * <p/>
	 * username
	 * password
	 * jsonp — optional. See JSONP.
	 * Or you can pass the username and password via HTTP Basic Auth.
	 * <p/>
	 * Resulting status codes:
	 * <p/>
	 * 200: OK
	 * 403: Invalid username or password.
	 * 500: The service encountered an error. Please try again later.
	 *
	 * @throws Exception
	 */
	@Test
	public void basicAuthenticationTest() throws Exception {
		final HttpClient client = new HttpClient();

		// pass our credentials to HttpClient, they will only be used for
		// authenticating to servers with realm "realm" on the host
		// "www.instapaper.com", to authenticate against
		// an arbitrary realm or host change the appropriate argument to null.
		client.getState().setCredentials(new AuthScope("www.instapaper.com", 443, "realm"),
				new UsernamePasswordCredentials("www.instapaper.com", "open"));

		// create a GET method that reads a file over HTTPS, we're assuming
		// that this file requires basic authentication using the realm above.
		final GetMethod get = new GetMethod(contextPath);

		// Tell the GET method to automatically handle authentication. The
		// method will use any appropriate credentials to handle basic
		// authentication requests.  Setting this value to false will cause
		// any request for authentication to return with a status of 401.
		// It will then be up to the client to handle the authentication.
		get.setDoAuthentication(true);

		try {
			// execute the GET
			final int status = client.executeMethod(get);

			// print the status and response
			Assert.assertEquals(403, status);

			LOG.debug(status + "\n" + get.getResponseBodyAsString());

		} finally {
			// release any connection resources used by the method
			get.releaseConnection();
		}
	}


	@Test
	public void atenticateInstapaperUser() throws Exception {
		final HttpClient client = new HttpClient();

		// create a GET method that reads a file over HTTPS, we're assuming
		// that this file requires basic authentication using the realm above.
		final GetMethod get = new GetMethod(
				String.format(contextPath + "?%s&%s", "username=jinstapaper@gmail.com", "password=open"));


		try {
			// execute the GET
			final int status = client.executeMethod(get);
			Assert.assertEquals(200, status);

		} finally {
			// release any connection resources used by the method
			get.releaseConnection();
		}
	}
}

