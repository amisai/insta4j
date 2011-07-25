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

package org.ooloo.insta4j.client;

import com.sun.istack.NotNull;
import org.ooloo.insta4j.jaxb.InstaRecordBean;

import java.util.List;
import java.util.Map;

/**
 * @author dzontak@gmail.com
 */
public interface InstaClient {
	Map<String, Object> getProperties();

	InstaRecordBean verifyCredentials();

	Map<String, String> authorize(@NotNull String username, @NotNull String password);

	List<InstaRecordBean> listBookmarks(String limit, String folderId, String... bookmarkId);

	InstaRecordBean updateReadProgress(@NotNull String bookmarkId, @NotNull Double progress,
			@NotNull Long progressTimestamp);

	InstaRecordBean addBookmark(String url, String title, String folder_id, Boolean resolve_final_url);

	Boolean deleteBookmark(String bookmark_id);

	InstaRecordBean starBookmark(String bookmark_id);

	InstaRecordBean unstarBookmark(String bookmark_id);

	InstaRecordBean archiveBookmark(String bookmark_id);

	InstaRecordBean unarchiveBookmark(String bookmark_id);

	InstaRecordBean moveBookmark(@NotNull String bookmark_id, @NotNull String folder_id);

	String getBookmark(@NotNull String bookmark_id, @NotNull String folder_id);

	List<InstaRecordBean> listFolders();

	InstaRecordBean createFolder(@NotNull String title);

	Boolean deleteFolder(@NotNull String folder_id);

	List<InstaRecordBean> setFolderOrder(Map<Integer, Long> folderPositionMap);
}
