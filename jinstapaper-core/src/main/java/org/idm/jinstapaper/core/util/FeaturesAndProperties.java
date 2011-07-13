package org.idm.jinstapaper.core.util;

import java.util.Map;

/**
 * @author Denis Zontak
 */
public interface FeaturesAndProperties {

	 /**
     * Get the map of features associated with the client.
     *
     * @return the features.
     *         The returned value shall never be null.
     */
    Map<String, Boolean> getFeatures();

    /**
     * Get the value of a feature.
     *
     * @param featureName the feature name.
     * @return true if the feature is present and set to true, otherwise false
     *         if the feature is present and set to false or the feature is not
     *         present.
     */
    boolean getFeature(String featureName);

    /**
     * Get the map of properties associated with the client.
     *
     * @return the properties.
     *         The returned value shall never be null.
     */
    Map<String, Object> getProperties();

    /**
     * Get the value of a property.
     *
     * @param propertyName the property name.
     * @return the property, or null if there is no property present for the
     *         given property name.
     */
    Object getProperty(String propertyName);
}
