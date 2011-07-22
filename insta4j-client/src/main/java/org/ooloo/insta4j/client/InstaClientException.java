package org.ooloo.insta4j.client;

import org.ooloo.insta4j.InstaCodes;

/**
 * A RuntimeException that wraps {@link InstaCodes.Code} and should be thrown by Insta clients methods if an error
 * is found in the response.
 *
 * @author dzontak@gmail.com
 */
public class InstaClientException extends RuntimeException {

	private final InstaCodes.Code _code;

	public InstaClientException(final InstaCodes.Code code) {
		super();
		this._code = code;
	}

	public InstaClientException(final InstaCodes.Code code, final Throwable cause) {
		super(cause);
		this._code = code;
	}

	public InstaClientException(final String message, final InstaCodes.Code code, final Throwable cause) {
		super(message, cause);
		this._code = code;
	}

	public InstaClientException(final String message, final InstaCodes.Code code) {
		super(message);
		this._code = code;
	}
}
