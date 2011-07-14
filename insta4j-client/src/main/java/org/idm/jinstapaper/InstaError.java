package org.idm.jinstapaper;

public class InstaError {


	/**
	 * Base interface for statuses used in responses.
	 */
	public interface ErrorType {
		/**
		 * Get the associated status code
		 *
		 * @return the status code
		 */
		public int getErrorCode();

		/**
		 * Get the class of status code
		 *
		 * @return the class of status code
		 */
		public Error.Family getFamily();

		/**
		 * Get the reason phrase
		 *
		 * @return the reason phrase
		 */
		public String getReasonPhrase();
	}


	public enum Error implements ErrorType {
		/**
		 * http errors
		 */
		_400(400, "Bad request or exceeded the rate limit. Probably missing a required parameter, such as url"),
		_403(403, "Invalid username or password"),
		_401(401, "Invalid xAuth credentials"),
		_500(500, "The service encountered an error. Please try again later"),

		/**
		 * GeneralError
		 */

		_1040(1040, "Rate-limit exceeded"),
		_1041(1041, "Subscription account required"),
		_1042(1042, "Application is suspended"),

		/**
		 * BookmarkError
		 */
		// 1220: Domain requires full content to be supplied
		_1220(1220, "Domain requires full content to be supplied"),
		// 1221: Domain has opted out of Instapaper compatibility
		_1221(1221, "Domain has opted out of Instapaper compatibility"),
		// 1240: Invalid URL specified
		_1240(1240, "Invalid URL specified"),
		// 1241: Invalid or missing bookmark_id
		_1241(1241, "Invalid or missing bookmark_id"),
		// 1242: Invalid or missing folder_id
		_1242(1242, "Invalid or missing folder_id"),
		// 1243: Invalid or missing progress
		_1243(1243, "Invalid or missing progress"),
		// 1244: Invalid or missing progress_timestamp
		_1244(1244, "Invalid or missing progress_timestamp"),
		// 1245: Private bookmarks require supplied content
		_1245(1245, "Private bookmarks require supplied content"),
		// 1246: Unexpected error when saving bookmark
		_1246(1246, "Unexpected error when saving bookmark"),

		/**
		 * FolderError
		 */
		//1250: Invalid or missing title
		_1250(1250, "Invalid or missing title"),
		//1251: User already has a folder with this title
		_1251(1251, "User already has a folder with this title"),
		//1252: Cannot add bookmarks to this folder
		_1252(1252, "Cannot add bookmarks to this folder");


		private final int code;
		private final String reason;
		private Family family;

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

		Error(final int statusCode, final String reasonPhrase) {
			this.code = statusCode;
			this.reason = reasonPhrase;
			switch ((int) Math.ceil(code / 100)) {
				case 5:
					this.family = Family.HTTP_ERROR;
					break;
				case 10:
					this.family = Family.GENERAL_ERROR;
					break;
				case 12:
					this.family = Family.BOOKMARK_ERROR;
					break;
				default:
					this.family = Family.OTHER;
					break;
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
		 * @return the Error code
		 */
		public int getErrorCode() {
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
		 * Get the reason phrase
		 *
		 * @return the reason phrase
		 */
		@Override
		public String toString() {
			return reason;
		}

		/**
		 * Convert a numerical error code into the corresponding Error
		 *
		 * @param errorCode the numerical error code
		 * @return the matching Error or null is no matching Error is defined
		 */
		public static Error fromStatusCode(final int errorCode) {
			for (final Error error : Error.values()) {
				if (error.code == errorCode) {
					return error;
				}
			}
			return null;
		}
	}


}
