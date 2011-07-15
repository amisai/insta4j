package org.ooloo.insta4j.client;

/**
 * A runtime exception that is thrown if a resource being created already exists,
 * like a folder already exists with the same name.
 */
public class ResourceExistsException extends RuntimeException {

	public ResourceExistsException() {
		super();	//To change body of overridden methods use File | Settings | File Templates.
	}

	public ResourceExistsException(final String message) {
		super(message);	//To change body of overridden methods use File | Settings | File Templates.
	}

	public ResourceExistsException(final String message, final Throwable cause) {
		super(message, cause);	//To change body of overridden methods use File | Settings | File Templates.
	}

	public ResourceExistsException(final Throwable cause) {
		super(cause);	//To change body of overridden methods use File | Settings | File Templates.
	}
}
