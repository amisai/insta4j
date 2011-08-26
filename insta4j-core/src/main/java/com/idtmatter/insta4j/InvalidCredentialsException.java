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

package com.idtmatter.insta4j;


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
