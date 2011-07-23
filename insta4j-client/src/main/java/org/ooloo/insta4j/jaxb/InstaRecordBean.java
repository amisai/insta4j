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

package org.ooloo.insta4j.jaxb;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A jaxb annotated bean which can bind any json record retured by Full Instapaper Api.
 * @href http://www.instapaper.com/api/full
 *
 * @author dzontak@gmail.com
 *
 */
@XmlRootElement(name = "instarecord")
public class InstaRecordBean {
	// Defines the type of record, user, bookmarks, error, etc.
	@XmlElement
	public String type;

	// User data
	// {"type":"user","user_id":1615568,"username":"jinstapaper@gmail.com","subscription_is_active":"1"}
	@XmlElement
	public String user_id;
	@XmlElement
	public String username;
	@XmlElement
	public boolean subscription_is_active;

	// Bookmarks
	// {"type":"bookmark","bookmark_id":184117327,"url":"http:\/\/toilettwit.info\/","title":"ToileTTwiT",
	// "description":"Adding Toilettwit","time":1310438674,"starred":"0","private_source":"","hash":"nHlrTfNc",
	// "progress":0,"progress_timestamp":0}
	@XmlElement
	public String bookmark_id;
	@XmlElement
	public String url;
	@XmlElement
	public String title;
	@XmlElement
	public String description;
	@XmlElement
	public long time;
	@XmlElement
	public boolean starred;
	@XmlElement
	public String private_source;
	@XmlElement
	public String hash;
	@XmlElement
	public double progress;
	@XmlElement
	public long progress_timestamp;
	//[{"type":"error", "error_code":1240, "message":"Invalid URL specified"}]
	@XmlElement
	public String error_code;
	@XmlElement
	public String message;

	/**
	 * Folder
	 */
	//[{"type":"folder","folder_id":1190085,"title":"news","sync_to_mobile":"1","position":1310749195}]
	@XmlElement
	public String folder_id;
	@XmlElement
	public boolean sync_to_mobile;
	@XmlElement
	public long position;

	@Override
	public String toString() {
		return "{folder_id:" + folder_id + ",position:" + position + "}";
	}

}
