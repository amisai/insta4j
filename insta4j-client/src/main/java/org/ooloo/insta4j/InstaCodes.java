package org.ooloo.insta4j;

import org.ooloo.insta4j.client.InstaClientException;
import org.ooloo.insta4j.client.ResourceExistsException;

public class InstaCodes {

	/**
	 * Base interface for statuses used in responses.
	 */
	public interface CodeType {
		/**
		 * Get the associated status code
		 *
		 * @return the status code
		 */
		public int getCode();

		/**
		 * Get the class of status code
		 *
		 * @return the class of status code
		 */
		public Code.Family getFamily();

		/**
		 * Get the reason phrase
		 *
		 * @return the reason phrase
		 */
		public String getReasonPhrase();
	}

	public enum Code implements CodeType {
		/**
		 * http codes
		 */
		_200(200, "OK", null),
		_201(201, "Created", null),
		_400(400, "Bad request or exceeded the rate limit. Probably missing a required parameter, such as url",
				InstaClientException.class),
		_403(403, "Invalid username or password", InvalidCredentialsException.class),
		_401(401, "Invalid xAuth credentials", InvalidCredentialsException.class),
		_500(500, "The service encountered an error. Please try again later", InstaClientException.class),
		/**
		 * GeneralError
		 */
		_1040(1040, "Rate-limit exceeded", InstaClientException.class),
		_1041(1041, "Subscription account required", InstaClientException.class),
		_1042(1042, "Application is suspended", InstaClientException.class),

		/**
		 * BookmarkError
		 */

		// 1220: Domain requires full content to be supplied
		_1220(1220, "Domain requires full content to be supplied", InstaClientException.class),
		// 1221: Domain has opted out of Instapaper compatibility
		_1221(1221, "Domain has opted out of Instapaper compatibility", InstaClientException.class),
		// 1240: Invalid URL specified
		_1240(1240, "Invalid URL specified", InstaClientException.class),
		// 1241: Invalid or missing bookmark_id
		_1241(1241, "Invalid or missing bookmark_id", InstaClientException.class),
		// 1242: Invalid or missing folder_id
		_1242(1242, "Invalid or missing folder_id", IllegalArgumentException.class),
		// 1243: Invalid or missing progress
		_1243(1243, "Invalid or missing progress", InstaClientException.class),
		// 1244: Invalid or missing progress_timestamp
		_1244(1244, "Invalid or missing progress_timestamp", InstaClientException.class),
		// 1245: Private bookmarks require supplied content
		_1245(1245, "Private bookmarks require supplied content", InstaClientException.class),
		// 1246: Unexpected error when saving bookmark
		_1246(1246, "Unexpected error when saving bookmark", InstaClientException.class),

		/**
		 * FolderError
		 */
		//1250: Invalid or missing title
		_1250(1250, "Invalid or missing title", InstaClientException.class),
		//1251: User already has a folder with this title
		_1251(1251, "User already has a folder with this title", ResourceExistsException.class),
		//1252: Cannot add bookmarks to this folder
		_1252(1252, "Cannot add bookmarks to this folder", InstaClientException.class);

		private final int code;
		private final String reason;
		private final Family family;
		private final Class<? extends RuntimeException> exceptionClass;

		/**
		 * An enumeration representing the class of status code. Family is used
		 * here since class is overloaded in Java.
		 */
		public enum Family {
			HTTP_ERROR,
			SUCCESSFUL,
			GENERAL_ERROR,
			BOOKMARK_ERROR,
			FOLDER_ERROR,
			OTHER
		}

		Code(final int code, final String reasonPhrase, final Class<? extends RuntimeException> exceptionClass) {
			this.code = code;
			this.reason = reasonPhrase;
			this.exceptionClass = exceptionClass;

			if (code >= 200 || code <= 201) {
				this.family = Family.SUCCESSFUL;
			} else if (code >= 400 || code <= 500) {
				this.family = Family.HTTP_ERROR;
			} else if (code >= 1040 || code <= 1045) {
				this.family = Family.GENERAL_ERROR;
			} else if (code >= 1220 || code <= 1246) {
				this.family = Family.BOOKMARK_ERROR;
			} else if (code >= 1250 || code <= 1252) {
				this.family = Family.FOLDER_ERROR;
			} else {
				this.family = Family.OTHER;
			}
		}

		/**
		 * Get the class of status code
		 *
		 * @return the class of status code
		 */
		public Family getFamily() {
			return family;
		}

		/**
		 * Get the associated status code
		 *
		 * @return the Code code
		 */
		public int getCode() {
			return code;
		}

		/**
		 * Get the reason phrase
		 *
		 * @return the reason phrase
		 */
		public String getReasonPhrase() {
			return toString();
		}

		/**
		 * Get the associated Exception class
		 * if null no exception needs to be raised.
		 *
		 * @return the Code code
		 */
		public Class<? extends RuntimeException> getExceptionClass() {
			return exceptionClass;
		}

		/**
		 * Get the reason phrase
		 *
		 * @return the reason phrase
		 */
		@Override
		public String toString() {
			return reason;
		}

		/**
		 * Convert a numerical error code into the corresponding Code
		 *
		 * @param code the numerical error code
		 * @return the matching Code or null is no matching Code is defined
		 */
		public static Code fromCode(final int code) {
			for (final Code error : Code.values()) {
				if (error.code == code) {
					return error;
				}
			}
			return null;
		}
	}
}
