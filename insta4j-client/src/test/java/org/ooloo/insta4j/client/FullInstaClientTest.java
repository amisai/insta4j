package org.ooloo.insta4j.client;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ietf.jgss.Oid;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ooloo.insta4j.jaxb.InstaRecordBean;

import javax.security.auth.login.FailedLoginException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FullInstaClientTest {

	private static final Log log = LogFactory.getLog(FullInstaClientTest.class);

	@Test
	public void listBookmarksTest() {

		final FullInstaClient client = FullInstaClient.create("jinstapaper@gmail.com", "open");
		final List<InstaRecordBean> instaRecordBeans = client.listBookmarks(null, null, null);

		// [{"type":"meta"},{"type":"user","user_id":1615568,"username":"jinstapaper@gmail.com",
		// "subscription_is_active":"1"},
		// {"type":"bookmark","bookmark_id":184117327,"url":"http:\/\/toilettwit.info\/","title":"ToileTTwiT",
		// "description":"Adding Toilettwit","time":1310438674,"starred":"0","private_source":"","hash":"nHlrTfNc","progress":0,"progress_timestamp":0}
		// ,{"type":"bookmark","bookmark_id":182746673,"url":"http:\/\/blogs.oracle.com\/PavelBucek\/entry\/replacing_client_used_in_jersey","title":"Replacing client used in Jersey Test Framework (Pavel Bucek's weblog)","description":"","time":1310080684,"starred":"0","private_source":"","hash":"3FOA5RyF","progress":0,"progress_timestamp":0}]

		Assert.assertNotNull(instaRecordBeans);
	}

	@Test
	public void verifyCredentialsTest() {
		final FullInstaClient client = FullInstaClient.create("jinstapaper@gmail.com", "open");
		final InstaRecordBean instaRecordBean = client.verifyCredentials();
		//[{"type":"user","user_id":1615568,"username":"jinstapaper@gmail.com","subscription_is_active":"1"}]
		Assert.assertNotNull(instaRecordBean);
		Assert.assertEquals("user", instaRecordBean.type);
		Assert.assertEquals(true, instaRecordBean.subscription_is_active);
		Assert.assertEquals("1615568", instaRecordBean.user_id);
		Assert.assertEquals("jinstapaper@gmail.com", instaRecordBean.username);
	}

	@Test
	public void authorizeTest() throws FailedLoginException {
		final FullInstaClient client = FullInstaClient.create("jinstapaper@gmail.com", "open");
		final Map<String, String> authorize = client.authorize("jinstapaper@gmail.com", "open");
		Assert.assertNotNull(authorize);
		Assert.assertNotNull(authorize.get("oauth_token"));
		Assert.assertNotNull(authorize.get("oauth_token_secret"));
	}

	@Test(expected = InvalidCredentialsException.class)
	public void authorizeWithWrongPasswordTest() throws FailedLoginException {
		final FullInstaClient client = FullInstaClient.create("jinstapaper@gmail.com", "open");
		final Map<String, String> authorize = client.authorize("jinstapaper@gmail.com", "ooloo");
		Assert.assertNotNull(authorize);
		Assert.assertNotNull(authorize.get("oauth_token"));
		Assert.assertNotNull(authorize.get("oauth_token_secret"));
	}

	@Test
	public void shouldAddBookmark() throws Exception {
		final FullInstaClient client = FullInstaClient.create("jinstapaper@gmail.com", "open");
		final InstaRecordBean instaRecordBean = client.addBookmark("http://news.ycombinator.com/", null, null, null);
		Assert.assertNotNull(instaRecordBean);
		Assert.assertEquals("bookmark", instaRecordBean.type);
		Assert.assertEquals("Hacker News", instaRecordBean.title);
		Assert.assertEquals("http://news.ycombinator.com/", instaRecordBean.url);
		Assert.assertEquals("", instaRecordBean.description);
		Assert.assertNotNull(instaRecordBean.hash);
		Assert.assertNotNull(instaRecordBean.progress_timestamp);
		Assert.assertNotNull(instaRecordBean.time);
		Assert.assertNotNull(instaRecordBean.starred);
		Assert.assertNotNull(instaRecordBean.progress);
		Assert.assertNotNull(instaRecordBean.private_source);
	}

	@Test
	public void shouldAddBookmarkWithCustomTitle() throws Exception {
		final FullInstaClient client = FullInstaClient.create("jinstapaper@gmail.com", "open");
		final InstaRecordBean instaRecordBean = client
				.addBookmark("http://news.ycombinator.com/", "My Hacker News", null, null);
		Assert.assertNotNull(instaRecordBean);
		Assert.assertEquals("bookmark", instaRecordBean.type);
		Assert.assertEquals("My Hacker News", instaRecordBean.title);
		Assert.assertEquals("http://news.ycombinator.com/", instaRecordBean.url);
		Assert.assertEquals("", instaRecordBean.description);
		Assert.assertNotNull(instaRecordBean.hash);
		Assert.assertNotNull(instaRecordBean.progress_timestamp);
		Assert.assertNotNull(instaRecordBean.time);
		Assert.assertNotNull(instaRecordBean.starred);
		Assert.assertNotNull(instaRecordBean.progress);
		Assert.assertNotNull(instaRecordBean.private_source);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailToAddBookmarkToFolderThatDoesNotExist() throws Exception {
		final FullInstaClient client = FullInstaClient.create("jinstapaper@gmail.com", "open");
		final InstaRecordBean instaRecordBean = client
				.addBookmark("http://news.ycombinator.com/", null, "IDONTEXISTYET", null);
	}

	@Test
	public void shouldCreateFolder() throws Exception {
		final FullInstaClient client = FullInstaClient.create("jinstapaper@gmail.com", "open");
		final InstaRecordBean instaRecordBean = client.createFolder(String.valueOf(System.currentTimeMillis()));
		Assert.assertNotNull(instaRecordBean);
		Assert.assertEquals("folder", instaRecordBean.type);
		Assert.assertNotNull(instaRecordBean.folder_id);
		Assert.assertNotNull(instaRecordBean.position);
		Assert.assertEquals(true, instaRecordBean.sync_to_mobile);
		// delete the folder
		Assert.assertTrue(client.deleteFolder(instaRecordBean.folder_id));

	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldDeleteFolderThatDoesNotExist() throws Exception {
		final FullInstaClient client = FullInstaClient.create("jinstapaper@gmail.com", "open");
		Assert.assertFalse(client.deleteFolder("000"));
	}

	@Test
	public void shouldDeleteAnExistingFolderId() throws Exception {
		final FullInstaClient client = FullInstaClient.create("jinstapaper@gmail.com", "open");
		final String folderId = String.valueOf(System.currentTimeMillis());
		final InstaRecordBean instaRecordBean = client.createFolder(folderId);
		Assert.assertNotNull(instaRecordBean);
		Assert.assertTrue(client.deleteFolder(instaRecordBean.folder_id));
	}

	@Test
	public void shouldListFolders() throws Exception {
		final FullInstaClient client = FullInstaClient.create("jinstapaper@gmail.com", "open");
		final List<InstaRecordBean> recordBeans = client.listFolders();
		Assert.assertNotNull(recordBeans);
	}

	@Test(expected = RuntimeException.class)
	public void shouldFailToGetBookmarks() throws Exception {
		final FullInstaClient client = FullInstaClient.create("jinstapaper@gmail.com", "open");
		final String bookarmks = client.getBookmark(null, null);
		Assert.assertNotNull(bookarmks);
	}

	@Test()
	public void shouldGetBookmarks() throws Exception {
		final FullInstaClient client = FullInstaClient.create("jinstapaper@gmail.com", "open");
		final List<InstaRecordBean> folders = FullInstaClient.getRecordByType(client.listFolders(), "folder");
		Assert.assertNotNull(folders);
		final InstaRecordBean folder = folders.iterator().next();
		final List<InstaRecordBean> bookmarks = FullInstaClient
				.getRecordByType(client.listBookmarks(null, null, null), "bookmark");
		Assert.assertNotNull(bookmarks);
		final InstaRecordBean recordBean = bookmarks.iterator().next();
		final String thebookmark = client.getBookmark(recordBean.bookmark_id, folder.folder_id);
		Assert.assertNotNull(thebookmark);
	}

	@Test
	public void shouldReOrderFolders() {
		final FullInstaClient client = FullInstaClient.create("jinstapaper@gmail.com", "open");
		final List<InstaRecordBean> folders = FullInstaClient.getRecordByType(client.listFolders(), "folder");
		Assert.assertNotNull(folders);
		final Map<Integer, Integer> folderPositionMap = new HashMap<Integer, Integer>();
		int count = 0;
		for (final InstaRecordBean folder : folders) {
			folderPositionMap.put(Integer.valueOf(folder.folder_id), folders.size() - count);
			count++;
		}
		final List<InstaRecordBean> reOrderedList = client.setFolderOrder(folderPositionMap);
		count = 0;
		for (final InstaRecordBean reOrderFolder : reOrderedList) {
			log.debug(reOrderFolder);
			final long position = folderPositionMap.get(Integer.valueOf(reOrderFolder.folder_id));
			log.debug("Position in Map of folder " + reOrderFolder.folder_id + " = " + position);
			Assert.assertEquals(reOrderFolder.position, position);
			count++;
		}
	}
	
	@Test
	public void testUpdateReadProgress(){
		final FullInstaClient client = FullInstaClient.create("jinstapaper@gmail.com", "open");
		List<InstaRecordBean> folders = client.listFolders();
		Assert.assertNotNull(folders);
		Assert.assertFalse(folders.isEmpty());
		InstaRecordBean firstFolder = folders.get(0);
		InstaRecordBean initBookmark = client.addBookmark("http://news.ycombinator.com/", "ProgressTest", firstFolder.folder_id, false);
		Assert.assertNotNull(initBookmark);
		Assert.assertNotNull(initBookmark.bookmark_id);
		double initProgress = initBookmark.progress;
		long initialTimestamp = initBookmark.progress_timestamp;
	
		InstaRecordBean updatedRecord = client.updateReadProgress(initBookmark.bookmark_id, Double.valueOf(1.0-initBookmark.progress), Long.toString(initialTimestamp+5L));
		Assert.assertEquals(1.0, updatedRecord.progress+initBookmark.progress,0.01);
	}
	
	@Test
	@Ignore
	public void testDeleteBookmark(){
		final FullInstaClient client = FullInstaClient.create("jinstapaper@gmail.com", "open");
		List<InstaRecordBean> folders = client.listFolders();
		Assert.assertNotNull(folders);
		Assert.assertFalse(folders.isEmpty());
		InstaRecordBean firstFolder = folders.get(0);
		InstaRecordBean initBookmark = client.addBookmark("http://news.ycombinator.com/", "ProgressTest", firstFolder.folder_id, false);
		Assert.assertTrue(client.deleteBookmark(initBookmark.bookmark_id));
	}
	
	@Test
	public void shouldStarBookmark(){
		final FullInstaClient client = FullInstaClient.create("jinstapaper@gmail.com", "open");
		List<InstaRecordBean> folders = client.listFolders();
		InstaRecordBean firstFolder = folders.get(0);
		InstaRecordBean initBookmark = client.addBookmark("http://news.ycombinator.com/", "ProgressTest", firstFolder.folder_id, false);
		InstaRecordBean staredBookmark = client.starBookmark(initBookmark.bookmark_id);
		Assert.assertTrue(staredBookmark.starred);
		Assert.assertEquals(initBookmark.bookmark_id, staredBookmark.bookmark_id);
	}
}
