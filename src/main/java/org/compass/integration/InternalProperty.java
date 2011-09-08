/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.compass.integration;

import org.elasticsearch.osem.annotations.Searchable;

/**
 *
 * @author andrewregan
 */
@Searchable
public class InternalProperty implements Property
{
	private String	m_Key;
	private String	m_Value;

	public InternalProperty( final String inName, final String inVal)
	{
		m_Key = inName;
		m_Value = inVal;
	}

	@Override
	public String getName()
	{
		return m_Key;
	}

	@Override
	public String getValue()
	{
		return m_Value;
	}

	/****************************************************************************
	 * For elasticsearch-OSEM
	****************************************************************************/
	public String getKey()
	{
		return m_Key;
	}

	/****************************************************************************
	 * For elasticsearch-OSEM
	****************************************************************************/
	public void setKey( final String inKey)
	{
		m_Key = inKey;
	}

	/****************************************************************************
	 * For elasticsearch-OSEM
	****************************************************************************/
	public void setValue( final String inVal)
	{
		m_Value = inVal;
	}
}