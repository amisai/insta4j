package org.idm.jinstapaper;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Test;

import javax.security.auth.login.FailedLoginException;
import javax.ws.rs.core.MultivaluedMap;


public class SimpleInstapaperClientTest {

	@Test
	public void authenticateValidAccountSecuredWithPasswordTest() throws Exception {
		final SimpleInstapaperClient simpleClient = new SimpleInstapaperClient("jinstapaper@gmail.com", "open");
		Assert.assertTrue(simpleClient.authenticate());
	}

	@Test
	public void authenticateValidAccountSecuredWithPasswordWithJsonTest() throws Exception {
		final SimpleInstapaperClient simpleClient = new SimpleInstapaperClient("jinstapaper@gmail.com", "open");
		final String instapaperCallback = simpleClient.authenticate("instapaperCallback");
		Assert.assertEquals("instapaperCallback({\"status\":200});", instapaperCallback);
	}

	@Test(expected = FailedLoginException.class)
	public void authenticateWithInvalidPasswordTest() throws Exception {
		final SimpleInstapaperClient simpleClient = new SimpleInstapaperClient("jinstapaper@gmail.com", "ops");
		Assert.assertTrue(simpleClient.authenticate());
	}

	@Test
	public void authenticateWithInvalidPasswordWithJsonpTest() throws Exception {
		final SimpleInstapaperClient simpleClient = new SimpleInstapaperClient("jinstapaper@gmail.com", "ops");
		final String instapaperCallback = simpleClient.authenticate("instapaperCallback");
		Assert.assertEquals("instapaperCallback({\"status\":403});", instapaperCallback);
	}

	@Test
	public void authenticateWithInvalidUsernameWithJsonpTest() throws Exception {
		final SimpleInstapaperClient simpleClient = new SimpleInstapaperClient(null, null);
		final String instapaperCallback = simpleClient.authenticate("instapaperCallback");
		Assert.assertEquals("instapaperCallback({\"status\":403});", instapaperCallback);
	}


	@Test
	public void addUrlWithCorrectCredentialsTest() throws Exception {
		final SimpleInstapaperClient simpleClient = new SimpleInstapaperClient("jinstapaper@gmail.com", "open");
		final MultivaluedMap<String, String> addResults = simpleClient
				.add("http://toilettwit.info/", "ToileTTwiT", "Adding Toilettwit");
		Assert.assertNotNull(addResults);
		Assert.assertEquals("http://toilettwit.info/",addResults.getFirst("Content-Location"));
		Assert.assertEquals("\"ToileTTwiT\"", addResults.getFirst("X-Instapaper-Title"));
	}
}
