package org.ooloo.insta4j.client;

/**
 * An enumeration for {@link org.ooloo.insta4j.jaxb.InstaRecordBean#type}
 *
 * @author dzontak@gmail.com
 */
public enum RecordType {
	BOOKMARK("bookmark"),
	FOLDER("folder"),
	META("meta"),
	USER("user"),
	ERROR("error");
	private String _type;

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
