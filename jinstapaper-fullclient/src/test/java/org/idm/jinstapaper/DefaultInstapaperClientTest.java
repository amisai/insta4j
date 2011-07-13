package org.idm.jinstapaper;

import junit.framework.Assert;
import org.idm.jinstapaper.jaxb.InstapaperRecordBean;
import org.junit.Test;

import javax.security.auth.login.FailedLoginException;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: deniszontak
 * Date: 7/11/11
 * Time: 11:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultInstapaperClientTest {

	@Test
	public void listBookmarksJsonTest() {
		final DefaultInstapaperClient client = new DefaultInstapaperClient("jinstapaper@gmail.com", "open");
		final String bookmarkList = client.listBookmarksJson(null, null, null);

		// [{"type":"meta"},{"type":"user","user_id":1615568,"username":"jinstapaper@gmail.com",
		// "subscription_is_active":"1"},
		// {"type":"bookmark","bookmark_id":184117327,"url":"http:\/\/toilettwit.info\/","title":"ToileTTwiT",
		// "description":"Adding Toilettwit","time":1310438674,"starred":"0","private_source":"","hash":"nHlrTfNc","progress":0,"progress_timestamp":0}
		// ,{"type":"bookmark","bookmark_id":182746673,"url":"http:\/\/blogs.oracle.com\/PavelBucek\/entry\/replacing_client_used_in_jersey","title":"Replacing client used in Jersey Test Framework (Pavel Bucek's weblog)","description":"","time":1310080684,"starred":"0","private_source":"","hash":"3FOA5RyF","progress":0,"progress_timestamp":0}]

		Assert.assertNotNull(bookmarkList);
	}

	@Test
	public void listBookmarksTest() {
		final DefaultInstapaperClient client = new DefaultInstapaperClient("jinstapaper@gmail.com", "open");
		final List<InstapaperRecordBean> instapaperRecordBeans = client.listBookmarks(null, null, null);

		// [{"type":"meta"},{"type":"user","user_id":1615568,"username":"jinstapaper@gmail.com",
		// "subscription_is_active":"1"},
		// {"type":"bookmark","bookmark_id":184117327,"url":"http:\/\/toilettwit.info\/","title":"ToileTTwiT",
		// "description":"Adding Toilettwit","time":1310438674,"starred":"0","private_source":"","hash":"nHlrTfNc","progress":0,"progress_timestamp":0}
		// ,{"type":"bookmark","bookmark_id":182746673,"url":"http:\/\/blogs.oracle.com\/PavelBucek\/entry\/replacing_client_used_in_jersey","title":"Replacing client used in Jersey Test Framework (Pavel Bucek's weblog)","description":"","time":1310080684,"starred":"0","private_source":"","hash":"3FOA5RyF","progress":0,"progress_timestamp":0}]

		Assert.assertNotNull(instapaperRecordBeans);
	}

	@Test
	public void verifyCredentialsTest() {
		final DefaultInstapaperClient client = new DefaultInstapaperClient("jinstapaper@gmail.com", "open");
		final InstapaperRecordBean instapaperRecordBean = client.verifyCredentials();
		//[{"type":"user","user_id":1615568,"username":"jinstapaper@gmail.com","subscription_is_active":"1"}]
		Assert.assertNotNull(instapaperRecordBean);
		Assert.assertEquals("user", instapaperRecordBean.type);
		Assert.assertEquals(true, instapaperRecordBean.subscription_is_active);
		Assert.assertEquals("1615568", instapaperRecordBean.user_id);
		Assert.assertEquals("jinstapaper@gmail.com", instapaperRecordBean.username);
	}

	@Test
	public void authorizeTest() throws FailedLoginException {
		final DefaultInstapaperClient client = new DefaultInstapaperClient("jinstapaper@gmail.com", "open");
		final Map<String, String> authorize = client.authorize("jinstapaper@gmail.com", "open");
		Assert.assertNotNull(authorize);
		Assert.assertNotNull(authorize.get("oauth_token"));
		Assert.assertNotNull(authorize.get("oauth_token_secret"));
	}

	@Test(expected = FailedLoginException.class)
	public void authorizeWithWrongPasswordTest() throws FailedLoginException {
		final DefaultInstapaperClient client = new DefaultInstapaperClient("jinstapaper@gmail.com", "open");
		final Map<String, String> authorize = client.authorize("jinstapaper@gmail.com", "ooloo");
		Assert.assertNotNull(authorize);
		Assert.assertNotNull(authorize.get("oauth_token"));
		Assert.assertNotNull(authorize.get("oauth_token_secret"));
	}
}
