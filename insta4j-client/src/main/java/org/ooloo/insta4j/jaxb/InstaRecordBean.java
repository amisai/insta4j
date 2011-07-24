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

import org.apache.log4j.Logger;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * A jaxb annotated bean which can bind any json record retured by Full Instapaper Api http://www.instapaper.com/api/full
 *
 * @author dzontak@gmail.com
 */
@XmlRootElement(name = "instarecord")
public class InstaRecordBean implements Serializable {

	private transient static final Logger log = Logger.getLogger(InstaRecordBean.class);

	/**
	 * Defines the type of record, user, bookmarks, error, etc. see enum {@link org.ooloo.insta4j.client.RecordType}
	 */
	@XmlElement
	public String type;

	// User data
	// {"type":"user","user_id":1615568,"username":"jinstapaper@gmail.com","subscription_is_active":"1"}
	@XmlElement
	public String user_id;
	@XmlElement
	public String username;
	@XmlElement
	public Boolean subscription_is_active;

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
	public Long time;
	@XmlElement
	public Boolean starred;
	@XmlElement
	public String private_source;
	@XmlElement
	public String hash;
	@XmlElement
	public Double progress;
	@XmlElement
	public Long progress_timestamp;
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
	public Boolean sync_to_mobile;
	@XmlElement
	public Long position;

	/**
	 * Gives the object representation in form of json
	 * e.g. [{"type":"folder","folder_id":1190085,"title":"news","sync_to_mobile":"1","position":1310749195}]
	 *
	 * @return A json representation of the bean
	 */
	@Override
	public String toString() {

		final StringBuilder builder = new StringBuilder("[");
		for (final Field field : this.getClass().getFields()) {
			try {
				final Object fieldValue = field.get(this);
				if (fieldValue != null) {
					builder.append("\"").append(field.getName()).append("\"").append(":").append("\"")
							.append(fieldValue).append("\"").append(",");
				}
			} catch (IllegalAccessException e) {
				log.warn(String.format("Failed to access field %s due to error %s", field.getName(), e.getMessage()));
			}
		}
		builder.append("]");
		// surround json by {}
		if (!builder.toString().equals("[]")) {
			builder.insert(1, "{");
			//replace the last , with }
			builder.replace(builder.length() - 2, builder.length() - 1, "}");
		}
		return builder.toString();

	}


	public Boolean isNull() throws IllegalAccessException {
		return this.toString().equals("[]");
	}

}
