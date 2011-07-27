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

package org.ooloo.insta4j;

import javax.ws.rs.core.MultivaluedMap;

/**
 * @author dzontak@gmail.com
 */
public interface SimpleClient {
	void updateAuthenticationCredentials(String username, String password);

	boolean authenticate();

	String authenticate(String jsonp);

	MultivaluedMap<String, String> add(String url, String title, String selection);

	String add(String url, String title, String selection, String redirect, String jsonp);
}
