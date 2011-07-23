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

package org.ooloo.insta4j.jsonp;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import org.ooloo.insta4j.jaxb.InstaRecordBean;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Resloves mapping from json <-> JaxB from Instapaper json responses to {@link InstaRecordBean}
 *
 * @author dzontak@gmail.com
 */
@Provider
public final class JAXBContextResolver implements ContextResolver<JAXBContext> {

	private final JAXBContext context;
	private final Set<Class> types;

	public JAXBContextResolver() throws Exception {
		final Class[] cTypes = {InstaRecordBean.class};
		this.types = new HashSet(Arrays.asList(cTypes));
		this.context = new JSONJAXBContext(JSONConfiguration.natural().build(), cTypes);
	}

	public JAXBContext getContext(final Class<?> objectType) {
		return (types.contains(objectType)) ? context : null;
	}
}
