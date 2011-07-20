package org.ooloo.insta4j.client;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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


	@Test
	public void shouldAddBookmarkToFolder() throws Exception {
		final FullInstaClient client = FullInstaClient.create("jinstapaper@gmail.com", "open");
		final InstaRecordBean folderRecord = client.createFolder(String.valueOf(System.currentTimeMillis()));
		Assert.assertNotNull(folderRecord);
		Assert.assertNotNull(folderRecord.folder_id);

		final InstaRecordBean instaRecordBean = client
				.addBookmark("http://news.ycombinator.com/", "My Hacker News", folderRecord.folder_id, Boolean.TRUE);
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
		final List<InstaRecordBean> folders = FullInstaClient
				.selectRecordsByType(client.listFolders(), RecordType.FOLDER);
		Assert.assertNotNull(folders);
		final InstaRecordBean folder = folders.iterator().next();
		final List<InstaRecordBean> bookmarks = FullInstaClient
				.selectRecordsByType(client.listBookmarks(null, null, null), RecordType.BOOKMARK);
		Assert.assertNotNull(bookmarks);
		final InstaRecordBean recordBean = bookmarks.iterator().next();
		final String thebookmark = client.getBookmark(recordBean.bookmark_id, folder.folder_id);
		Assert.assertNotNull(thebookmark);


	}

	@Test
	public void shouldUpdateReadProgress() throws Exception {
		final FullInstaClient client = FullInstaClient.create("jinstapaper@gmail.com", "open");
		final List<InstaRecordBean> folders = FullInstaClient
				.selectRecordsByType(client.listFolders(), RecordType.FOLDER);
		Assert.assertNotNull(folders);
		final InstaRecordBean folder = folders.iterator().next();
		final List<InstaRecordBean> bookmarks = FullInstaClient
				.selectRecordsByType(client.listBookmarks(null, null, null), RecordType.BOOKMARK);
		Assert.assertNotNull(bookmarks);
		final InstaRecordBean bookmarkRecord = bookmarks.iterator().next();
		Assert.assertNotNull(bookmarkRecord);
		final long progressTimestamp = System.currentTimeMillis();
		final InstaRecordBean bookararkRecord = client
				.updateReadProgress(bookmarkRecord.bookmark_id, 0.5, progressTimestamp);
		Assert.assertNotNull(bookararkRecord);
		Assert.assertEquals(0.5D, bookararkRecord.progress);
		Assert.assertEquals(progressTimestamp, bookararkRecord.progress_timestamp);
	}

	@Test
	public void shouldReOrderFolders() {
		final FullInstaClient client = FullInstaClient.create("jinstapaper@gmail.com", "open");
		final List<InstaRecordBean> folders = FullInstaClient
				.selectRecordsByType(client.listFolders(), RecordType.FOLDER);
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
}
