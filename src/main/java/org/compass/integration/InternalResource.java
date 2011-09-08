/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.compass.integration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.elasticsearch.osem.annotations.Searchable;
import org.elasticsearch.osem.core.ObjectContext;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import java.util.HashMap;

/**
 *
 * @author andrewregan
 */
@Searchable
public class InternalResource implements Resource
{
	private String			m_Id;
	private String			m_Index;
	private List<Property>		m_Properties = new ArrayList<Property>();

	private final static String[]	EMPTY_VALS = {};

	/****************************************************************************
	****************************************************************************/
	@SuppressWarnings("unchecked")
	public InternalResource( final ObjectContext inCtxt, final Map<String,Object> inMap)
	{
	//	System.out.println(" = " + inMap);
	//	System.out.println(" => " + inMap.get("properties"));
	//	System.out.println(" => " + inMap.get("properties").getClass());
	//	System.out.println("k = " + inMap.keySet());

		m_Id = (String) inMap.get("id");
		m_Index = (String) inMap.get("index");
	//	m_Properties = (List<Property>) inMap.get("properties");

		for ( HashMap<String,String> eachMap : ((List<HashMap<String,String>>) inMap.get("properties")))
		{
			m_Properties.add( new InternalProperty( eachMap.get("key"), eachMap.get("value")) );
		}
/*		try
		{
			m_Index = inCtxt.getType( Class.forName((String) inMap.get("_class")) );
		}
		catch (ClassNotFoundException ex)
		{
			throw new RuntimeException(ex);	// (AGR) Yuk!
		}
*/
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	/****************************************************************************
	****************************************************************************/
	public InternalResource( final String inIdx)
	{
		m_Index = inIdx;
	}

	/****************************************************************************
	****************************************************************************/
	public InternalResource( final String inIdx, final String inId)
	{
		m_Index = inIdx;
		m_Id = inId;
	}

	/****************************************************************************
	****************************************************************************/
	private Collection<Property> _getMatches( final String inName)
	{
		return Collections2.filter( m_Properties, new Predicate<Property>() {

			@Override
			public boolean apply( final Property inProp)
			{
				return inProp.getName().equals(inName);
			}
		});
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public void removeProperties( final String inName)
	{
		m_Properties.removeAll( _getMatches(inName) );
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public void removeProperty( final String inName)
	{
		m_Properties.removeAll( _getMatches(inName) );	// FIXME. Check only 1 match
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public boolean hasProperty( final String inName)
	{
		return !_getMatches(inName).isEmpty();
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public Map<String,Object> getMap()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public String getId()
	{
		return m_Id;
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public void setId( final String inId)
	{
		m_Id = inId;
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public String getAlias()
	{
		return m_Index;
	}

	/****************************************************************************
	 * For elasticsearch-OSEM
	****************************************************************************/
	public String getIndex()
	{
		return m_Index;
	}

	/****************************************************************************
	 * For elasticsearch-OSEM
	****************************************************************************/
	public void setIndex( final String inIndex)
	{
		m_Index = inIndex;
	}

	/****************************************************************************
	 * For elasticsearch-OSEM
	****************************************************************************/
	public List<Property> getProperties()
	{
		return m_Properties;
	}

	/****************************************************************************
	 * For elasticsearch-OSEM
	****************************************************************************/
	public void setProperties( final List<Property> inProps)
	{
		m_Properties = inProps;
	}
	
	@Override
	public String getValue( final String inName)
	{
		for ( Property each : m_Properties)
		{
			if (each.getName().equals(inName))
			{
				return each.getValue();
			}
		}

		return null;
	}

	@Override
	public String[] getValues( final String inName)
	{
		Collection<String>	theVals = null;

		for ( Property each : m_Properties)
		{
			if (each.getName().equals(inName))
			{
				if ( theVals == null)
				{
					theVals = new ArrayList<String>();
				}

				theVals.add( each.getValue() );
			}
		}

		return ( theVals != null) ? theVals.toArray( new String[ theVals.size() ]) : EMPTY_VALS;
	}

	@Override
	public Resource addProperty( final String inName, Object inValue)
	{
		m_Properties.add( new InternalProperty( inName, String.valueOf(inValue) ) );
		return this;
	}

	@Override
	public Resource setProperty( final String inName, Object inValue)
	{
		removeProperties(inName);

		return addProperty( inName, inValue);
	}
}