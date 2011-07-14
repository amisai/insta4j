package org.ooloo.insta4j.client;


/**
 * A runtime exception thrown by a InstapaperClient that signals a
 * failure to authenticate a user.
 *
 * @author dzontak@gmail.com
 */
public class InvalidCredentialsException extends RuntimeException {
	public InvalidCredentialsException() {
		super();
	}

	public InvalidCredentialsException(final String message) {
		super(message);
	}

	public InvalidCredentialsException(final Throwable cause) {
		super(cause);
	}

	public InvalidCredentialsException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
