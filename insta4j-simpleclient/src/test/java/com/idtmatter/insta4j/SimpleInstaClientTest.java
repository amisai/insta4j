/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.idtmatter.insta4j;

import junit.framework.Assert;
import org.junit.Test;
import javax.security.auth.login.FailedLoginException;
import javax.ws.rs.core.MultivaluedMap;


public class SimpleInstaClientTest {

	@Test
	public void authenticateValidAccountSecuredWithPasswordTest() throws Exception {
		final SimpleInstaClient simpleClient = SimpleInstaClient.create("jinstapaper@gmail.com", "open");
		Assert.assertTrue(simpleClient.authenticate());
	}

	@Test
	public void shouldUpdateAuthenticationCredentials() throws Exception {
		final SimpleInstaClient simpleClient = SimpleInstaClient.create("jinstapaper@gmail.com", "closed");
		try {
			Assert.assertTrue(simpleClient.authenticate());

		} catch (InvalidCredentialsException ice) {
			simpleClient.updateAuthenticationCredentials("jinstapaper@gmail.com", "open");
		}
		Assert.assertTrue(simpleClient.authenticate());

	}

	@Test
	public void authenticateValidAccountSecuredWithPasswordWithJsonTest() throws Exception {
		final SimpleInstaClient simpleClient = SimpleInstaClient.create("jinstapaper@gmail.com", "open");
		final String instapaperCallback = simpleClient.authenticate("instapaperCallback");
		Assert.assertEquals("instapaperCallback({\"status\":200});", instapaperCallback);
	}

	@Test(expected = InvalidCredentialsException.class)
	public void authenticateWithInvalidPasswordTest() throws Exception {
		final SimpleInstaClient simpleClient = SimpleInstaClient.create("jinstapaper@gmail.com", "ops");
		Assert.assertTrue(simpleClient.authenticate());
	}

	@Test
	public void authenticateWithInvalidPasswordWithJsonpTest() throws Exception {
		final SimpleInstaClient simpleClient = SimpleInstaClient.create("jinstapaper@gmail.com", "ops");
		final String instapaperCallback = simpleClient.authenticate("instapaperCallback");
		Assert.assertEquals("instapaperCallback({\"status\":403});", instapaperCallback);
	}

	@Test
	public void authenticateWithInvalidUsernameWithJsonpTest() throws Exception {
		final SimpleInstaClient simpleClient = SimpleInstaClient.create(null, null);
		final String instapaperCallback = simpleClient.authenticate("instapaperCallback");
		Assert.assertEquals("instapaperCallback({\"status\":403});", instapaperCallback);
	}

	@Test
	public void addUrlWithCorrectCredentialsTest() throws Exception {
		final SimpleInstaClient simpleClient = SimpleInstaClient.create("jinstapaper@gmail.com", "open");
		final MultivaluedMap<String, String> addResults = simpleClient
				.add("http://toilettwit.info/", "ToileTTwiT", "Adding Toilettwit");
		Assert.assertNotNull(addResults);
		Assert.assertEquals("http://toilettwit.info/", addResults.getFirst("Content-Location"));
		Assert.assertEquals("\"ToileTTwiT\"", addResults.getFirst("X-Instapaper-Title"));
	}

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void addBadUrlTest() throws Exception {
		final SimpleInstaClient simpleClient = SimpleInstaClient.create("jinstapaper@gmail.com", "open");
		simpleClient.add(null, null, null);
	}

	@Test
	public void addUrlWithCorrectCredentialsForJsonTest() throws Exception {
		final SimpleInstaClient simpleClient = new SimpleInstaClient("jinstapaper@gmail.com", "open");
		final String responce = simpleClient
				.add("http://toilettwit.info/", "ToileTTwiT", "Adding Toilettwit", null, "instapaperCallback");
		Assert.assertEquals("instapaperCallback({\"status\":201,\"url\":\"http:\\/\\/toilettwit.info\\/\"});",
				responce);
	}

	@Test(expected = InvalidCredentialsException.class)
	public void addUrlWithIncorrectCredentialsTest() throws FailedLoginException {
		final SimpleInstaClient simpleClient = SimpleInstaClient.create("jinstapaper@gmail.com", "ope");
		final MultivaluedMap<String, String> response = simpleClient
				.add("http://toilettwit.info/", "ToileTTwiT", "Adding Toilettwit");
		Assert.assertNotNull(response);
	}
}
