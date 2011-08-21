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

package com.idtmatter.insta4j.client;

/**
 * An enumeration for {@link com.idtmatter.insta4j.jaxb.InstaRecordBean#type} field
 *
 * @author dzontak@gmail.com
 */
public enum RecordType {
	BOOKMARK("bookmark"),
	FOLDER("folder"),
	META("meta"),
	USER("user"),
	ERROR("error");
	private final String _type;

	RecordType(final String type) {
		_type = type;
	}

	public String type() {
		return _type;
	}

	/**
	 * Convert a String type code into the corresponding {@link RecordType}
	 *
	 * @param recordType the string type
	 * @return the matching {@link RecordType} or null is no matching type is defined
	 */
	public static RecordType fromType(final int recordType) {
		for (final RecordType type : RecordType.values()) {
			if (type._type.equals(recordType)) {
				return type;
			}
		}
		return null;
	}
}
