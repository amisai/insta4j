package org.idm.jinstapaper.jaxb;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Every jaxb model should extend from this model.
 */
@XmlRootElement(name = "record")
public class InstapaperRecordBean {
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
	public int progress;
	@XmlElement
	public long progress_timestamp;

	//[{"type":"error", "error_code":1240, "message":"Invalid URL specified"}]
	@XmlElement
	public String error_code;
	@XmlElement
	public String message;

}
