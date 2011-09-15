/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.compass.integration;

import java.util.Map;

/**
 *
 * @author andrewregan
 */
public interface Resource
{
	int getIntValue(String name);
	long getLongValue(String name);
	float getFloatValue(String name);
	double getDoubleValue(String name);
	boolean getBooleanValue(String name);

	void removeProperties( final String inName);
	void removeProperty( final String inName);

	boolean hasProperty( final String inName);

	Map<String, Object> getMap();

	/**
	 * Returns the mapping alias of the associated Resource
	 *
	 * @return The alias
	 */
	String getAlias();

	/**
	 * Returns the id of the resource. Used when there is only one id
	 * for the resource.
	 *
	 * @return The id of the resource.
	 */
	String getId();
	void setId( String inId);

	/**
	 * Returns the string value of the property with the given name if any exist
	 * in this resource, or null. If multiple properties exist with this name,
	 * this method returns the first value added.
	 *
	 * @param name The name of the property
	 * @return The first value that match the name
	 */
	String getValue(String name);

	/**
	 * Returns an array of values of the property specified as the method
	 * parameter. This method can return <code>null</code>.
	 *
	 * @param name the name of the property
	 * @return a <code>String[]</code> of property values
	 */
	String[] getValues(String name);

	/**
	 * Adds a property to the resource based on resource mapping definitions. If
	 * the property already exists in the resource (the name exists), it will be
	 * added on top of it (won't replace it). ONLY use this method with resource
	 * mapping.
	 *
	 * @param name  the name of the property
	 * @param value the value to be set (will be converted to a string).
	 * @throws SearchEngineException
	 */
	Resource addProperty(String name, Object value);

	/**
	 * Sets a property to the resource (removes then adds) based on resource mapping definitions.
	 * ONLY use this method with resource mapping.
	 *
	 * @param name  the name of the property
	 * @param value the value to be set (will be converted to a string).
	 * @throws SearchEngineException
	 */
	Resource setProperty(String name, Object value);
}