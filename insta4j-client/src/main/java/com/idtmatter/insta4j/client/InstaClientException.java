/*
 *   Licensed under the Apache License, Version 2.0 (the "License");
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

import com.idtmatter.insta4j.InstaCodes;

/**
 * A RuntimeException that wraps {@link com.idtmatter.insta4j.InstaCodes.Code} and should be thrown by Insta clients methods if an error
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
