package org.ooloo.insta4j.client.config;

import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DefaultInstaClientConfig implements InstaClientConfig {

	private final Map<String, Boolean> features = new HashMap<String, Boolean>();

	private final Map<String, Object> properties = new HashMap<String, Object>();

	public DefaultInstaClientConfig() {
		init();
	}

	private void init() {
		final Properties PROPERTIES = new Properties();
		FileInputStream st = null;
		try {
			//TODO: Avoid using ResourceUtils.
			st = new FileInputStream(ResourceUtils.getFile("classpath:" + PROPERTY_JINSTAPAPER_PROPERTY_NAME));
			PROPERTIES.load(st);
		} catch (IOException e) {
			// ignore
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (IOException ex) {
					// ignore
				}
			}
		}

		for (final String name : new String[]{PROPERTY_CONSUMER_KEY, PROPERTY_CONSUMER_SECRET}) {
			final String value = System.getProperty(name);
			if (value != null) {
				PROPERTIES.setProperty(name, value);
			}
		}

		if (PROPERTIES.getProperty(PROPERTY_CONSUMER_KEY) == null ||
				PROPERTIES.getProperty(PROPERTY_CONSUMER_SECRET) == null) {
			throw new IllegalArgumentException(String.format("No consumerKey and/or consumerSecret found in %s file. " +
					"You have to provide these as system properties.", PROPERTY_JINSTAPAPER_PROPERTY_NAME));
		}

		for (final Map.Entry<Object, Object> entry : PROPERTIES.entrySet()) {
			properties.put((String) entry.getKey(), entry.getValue());

		}

	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, Boolean> getFeatures() {
		return features;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getFeature(final String featureName) {
		final Boolean v = features.get(featureName);
		return (v != null) ? v : false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, Object> getProperties() {
		return properties;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getProperty(final String propertyName) {
		return properties.get(propertyName);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getPropertyAsFeature(final String name) {
		final Boolean v = (Boolean) getProperties().get(name);
		return (v != null) ? v : false;
	}
}
