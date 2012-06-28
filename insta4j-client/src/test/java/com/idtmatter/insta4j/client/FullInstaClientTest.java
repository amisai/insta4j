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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.FailedLoginException;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.idtmatter.insta4j.InvalidCredentialsException;
import com.idtmatter.insta4j.jaxb.InstaRecordBean;

public class FullInstaClientTest {

	private static final Logger log = LoggerFactory.getLogger(FullInstaClientTest.class);

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
		Assert.assertEquals(Boolean.TRUE, instaRecordBean.subscription_is_active);
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
//		Assert.assertEquals("Hacker News", instaRecordBean.title);
		Assert.assertEquals("http://news.ycombinator.com/", instaRecordBean.url);
		Assert.assertEquals("", instaRecordBean.description);
		Assert.assertNotNull(instaRecordBean.hash);
		Assert.assertNotNull(instaRecordBean.progress_timestamp);
		Assert.assertNotNull(instaRecordBean.time);
		Assert.assertNotNull(instaRecordBean.starred);
		Assert.assertNotNull(instaRecordBean.progress);
		Assert.assertNotNull(instaRecordBean.private_source);
		instaRecordBean.toString();
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
		Assert.assertNotNull(instaRecordBean);
	}

	@Test
	public void shouldCreateFolder() throws Exception {
		final FullInstaClient client = FullInstaClient.create("jinstapaper@gmail.com", "open");
		final InstaRecordBean instaRecordBean = client.createFolder(String.valueOf(System.currentTimeMillis()));
		Assert.assertNotNull(instaRecordBean);
		Assert.assertEquals("folder", instaRecordBean.type);
		Assert.assertNotNull(instaRecordBean.folder_id);
		Assert.assertNotNull(instaRecordBean.position);
		Assert.assertEquals(Boolean.TRUE, instaRecordBean.sync_to_mobile);
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
		final List<InstaRecordBean> bookmarks = FullInstaClient
				.selectRecordsByType(client.listBookmarks(null, null, null), RecordType.BOOKMARK);
		Assert.assertNotNull(bookmarks);
		final InstaRecordBean bookmarkRecord = bookmarks.iterator().next();
		Assert.assertNotNull(bookmarkRecord);
		final Long progressTimestamp = System.currentTimeMillis();
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
		final Map<Integer, Long> folderPositionMap = new HashMap<Integer, Long>();
		int count = 0;
		for (final InstaRecordBean folder : folders) {
			folderPositionMap.put(Integer.valueOf(folder.folder_id), Long.valueOf(folders.size() - count));
			count++;
		}
		final List<InstaRecordBean> reOrderedList = client.setFolderOrder(folderPositionMap);
		count = 0;
		for (final InstaRecordBean reOrderFolder : reOrderedList) {
			log.debug(reOrderFolder.toString());
			final Long position = folderPositionMap.get(Integer.valueOf(reOrderFolder.folder_id));
			log.debug("Position in Map of folder " + reOrderFolder.folder_id + " = " + position);
			Assert.assertEquals(reOrderFolder.position, position);
			count++;
		}
	}


	@Test
	public void testDeleteBookmark() {
		final FullInstaClient client = FullInstaClient.create("jinstapaper@gmail.com", "open");
		final List<InstaRecordBean> folders = client.listFolders();
		Assert.assertNotNull(folders);
		Assert.assertFalse(folders.isEmpty());
		final InstaRecordBean firstFolder = folders.get(0);
		final InstaRecordBean initBookmark = client
				.addBookmark("http://www.nytimes.com/pages/todayspaper/index.html?src=hp1-0-P#nytfrontpage",
						"DeleteBookmarkTest", firstFolder.folder_id, false);
		Assert.assertTrue(client.deleteBookmark(initBookmark.bookmark_id));
	}

	@Test
	public void shouldStarBookmark() {
		final FullInstaClient client = FullInstaClient.create("jinstapaper@gmail.com", "open");
		final List<InstaRecordBean> folders = client.listFolders();
		final InstaRecordBean firstFolder = folders.get(0);
		final InstaRecordBean initBookmark = client
				.addBookmark("http://news.ycombinator.com/", "ProgressTest", firstFolder.folder_id, false);
		final InstaRecordBean staredBookmark = client.starBookmark(initBookmark.bookmark_id);
		Assert.assertTrue(staredBookmark.starred);
		Assert.assertEquals(initBookmark.bookmark_id, staredBookmark.bookmark_id);
	}

	@Test
	public void shouldMoveBookmark() {
		final FullInstaClient client = FullInstaClient.create("jinstapaper@gmail.com", "open");
		final List<InstaRecordBean> folders = client.listFolders();
		final InstaRecordBean firstFolder = folders.iterator().next();
		final InstaRecordBean initBookmark = client
				.addBookmark("http://news.ycombinator.com/", "MoveBookmarkTest", firstFolder.folder_id, false);
		final InstaRecordBean movedBookmark = client.moveBookmark(initBookmark.bookmark_id, firstFolder.folder_id);
		Assert.assertNotNull(movedBookmark);
		//TODO: how to test that the bookmark was moved to the top of the Unread folder
	}

	@Test
	public void shouldUnStarBookmark() {
		final FullInstaClient client = FullInstaClient.create("jinstapaper@gmail.com", "open");
		final List<InstaRecordBean> folders = client.listFolders();
		final InstaRecordBean firstFolder = folders.get(0);
		final InstaRecordBean initBookmark = client
				.addBookmark("http://news.ycombinator.com/", "ProgressTest", firstFolder.folder_id, false);
		final InstaRecordBean staredBookmark = client.starBookmark(initBookmark.bookmark_id);
		Assert.assertTrue(staredBookmark.starred);
		Assert.assertFalse(client.unstarBookmark(initBookmark.bookmark_id).starred);
	}

	/**
	 * // FIXME shouldn't the Bean have a property to indicate archived state?
	 * How are we going to test if its archived?
	 * Current jason response does not contain  archived state.
	 * [{"type":"bookmark","bookmark_id":187033032,"url":"http:\/\/news.ycombinator.com\/","title":"ProgressTest","description":"","time":1311166421,"starred":"0","private_source":"","hash":"cfuJ4m8D","progress":"0","progress_timestamp":40}]
	 */

	@Test
	public void shouldArchiveAndUnarchiveBookmark() {
		final FullInstaClient client = FullInstaClient.create("jinstapaper@gmail.com", "open");
		final List<InstaRecordBean> folders = client.listFolders();
		Assert.assertTrue(folders.iterator().hasNext());
		final InstaRecordBean firstFolder = folders.iterator().next();
		final InstaRecordBean initBookmark = client
				.addBookmark("http://news.ycombinator.com/", "ArchiveTest", firstFolder.folder_id, false);
		final InstaRecordBean archivedBookmark = client.archiveBookmark(initBookmark.bookmark_id);
		Assert.assertNotNull(archivedBookmark);
		final InstaRecordBean unarchivedBookmark = client.unarchiveBookmark(initBookmark.bookmark_id);
		Assert.assertNotNull(unarchivedBookmark);
	}
}
