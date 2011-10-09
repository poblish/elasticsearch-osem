/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.compass.integration;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.natpryce.maybe.Maybe;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.common.base.Objects;
import org.elasticsearch.osem.annotations.Searchable;
import org.elasticsearch.osem.core.ObjectContext;

/**
 *
 * @author andrewregan
 */
@Searchable
public class InternalResource implements Resource
{
	private String			m_Id;
	private String			m_Index;
	private Map<String,Object>	m_Properties = new HashMap<String,Object>();

	private final static String[]	EMPTY_VALS = {};

	/****************************************************************************
	****************************************************************************/
	@SuppressWarnings("unchecked")
	public InternalResource( final ObjectContext inCtxt, final Map<String,Object> inMap)
	{
		m_Id = (String) inMap.get("id");
		m_Index = (String) inMap.get("index");

		setSourceProperties((Map<String,Object>) inMap.get("properties"));
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
	public final InternalResource setSourceProperties( final Map<String,Object> inMap)
	{
		for ( Entry<String,Object> eachEntry : inMap.entrySet())
		{
			if (eachEntry.getKey().equals("_class"))
			{
				continue;
			}

			m_Properties.put( eachEntry.getKey(), eachEntry.getValue());
		}

	/*	@SuppressWarnings("unchecked")
		final List<HashMap<String,String>>	theOsemProps = ((List<HashMap<String,String>>) inMap.get("properties"));

		if ( theOsemProps != null)
		{
			for ( final HashMap<String,String> eachMap : theOsemProps)
			{
				// m_Properties.add( new InternalProperty( eachMap.get("key"), eachMap.get("value")) );
			}
		}

		for ( Entry<String,Object> eachEntry : inMap.entrySet())
		{
			if (eachEntry.getKey().equals("properties"))	continue;

			// m_Properties.add( new InternalProperty( eachEntry.getKey(), String.valueOf( eachEntry.getValue() )) );
		}
*/
		return this;
	}

	/****************************************************************************
	***************************************************************************
	private Collection<Property> _getMatches( final String inName)
	{
		return Collections2.filter( m_Properties, new Predicate<Property>() {

			@Override
			public boolean apply( final Property inProp)
			{
				return inProp.getName().equals(inName);
			}
		});
	}*/

	/****************************************************************************
	****************************************************************************/
	@Override
	public void removeProperties( final String inName)
	{
		m_Properties.remove(inName);	// m_Properties.removeAll( _getMatches(inName) );
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public void removeProperty( final String inName)
	{
		m_Properties.remove(inName);	// m_Properties.removeAll( _getMatches(inName) );	// FIXME. Check only 1 match
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public boolean hasProperty( final String inName)
	{
		return _getMatchFromMap( m_Properties, inName, false).isKnown();
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
	public Map<String,Object> getProperties()
	{
		return m_Properties;
	}

	/****************************************************************************
	 * For elasticsearch-OSEM
	****************************************************************************/
	public void setProperties( final Map<String,Object> inProps)
	{
		m_Properties = inProps;
	}
	
	/****************************************************************************
	****************************************************************************/
	@Override
	public String getValue( final String inName)
	{
		return _getStringForObject( _getMatchFromMap( m_Properties, inName, false) ).otherwise((String) null);
	}

	/****************************************************************************
	    (AGR) FIXME. A bit dumb - can't we have default values, etc.?
	****************************************************************************/
	@Override
	public int getIntValue( final String inName)
	{
		return Integer.parseInt( getValue(inName) );
	}

	/****************************************************************************
	    (AGR) FIXME. A bit dumb - can't we have default values, etc.?
	****************************************************************************/
	@Override
	public long getLongValue( final String inName)
	{
		return Long.parseLong( getValue(inName) );
	}

	/****************************************************************************
	    (AGR) FIXME. A bit dumb - can't we have default values, etc.?
	****************************************************************************/
	@Override
	public float getFloatValue( final String inName)
	{
		return Float.parseFloat( getValue(inName) );
	}

	/****************************************************************************
	    (AGR) FIXME. A bit dumb - can't we have default values, etc.?
	****************************************************************************/
	@Override
	public double getDoubleValue( final String inName)
	{
		return Double.parseDouble( getValue(inName) );
	}

	/****************************************************************************
	    (AGR) FIXME. A bit dumb - can't we have default values, etc.?
	****************************************************************************/
	@Override
	public boolean getBooleanValue( final String inName)
	{
		return Boolean.parseBoolean( getValue(inName) );
	}

	/****************************************************************************
	****************************************************************************/
	private static Maybe<String> _getStringForObject( final Maybe<Object> inObj)
	{
		if (!inObj.isKnown())
		{
			return Maybe.unknown();
		}

		final Object	theActualObj = inObj.iterator().next();

		if ( theActualObj != null)
		{
			if ( theActualObj instanceof List)
			{
				@SuppressWarnings("unchecked")
				final List<String>	theList = (List) theActualObj;

				return Maybe.definitely( theList.isEmpty() ? null : theList.iterator().next());
			}
			else
			{
				return Maybe.definitely( String.valueOf(theActualObj) );
			}
		}

		return Maybe.definitely(null);
	}

	/****************************************************************************
	****************************************************************************/
	@SuppressWarnings("unchecked")
	private static Maybe<Object> _getMatchFromMap( final Map<String,Object> inMap, final String inName, final boolean inWantMultipleValues)
	{
		if (inMap.containsKey(inName))
		{
			return Maybe.definitely( inMap.get(inName) );
		}

		for ( Entry<String,Object> eachEntry : inMap.entrySet())
		{
			if ( eachEntry.getValue() instanceof Map)
			{
				final Maybe<Object>	theResult = _getMatchFromMap((Map<String,Object>) eachEntry.getValue(), inName, inWantMultipleValues);

				if (theResult.isKnown())
				{
					return theResult;
				}
			}
			else if ( eachEntry.getValue() instanceof Collection)
			{
				final Collection		theColl = (Collection) eachEntry.getValue();

				if (!theColl.isEmpty())
				{
					if (inWantMultipleValues)
					{
						final Collection<Object>	theMultipleMatches = new ArrayList<Object>();

						for ( Object eachItem : theColl)
						{
							if ( eachItem instanceof Map)
							{
								final Maybe<Object>	theResult = _getMatchFromMap((Map<String,Object>) eachItem, inName, inWantMultipleValues);

								if (theResult.isKnown())
								{
									final Object	theResultObj = theResult.iterator().next();

									if ( theResultObj instanceof Collection)
									{
										return Maybe.definitely(theResultObj);
									}

									theMultipleMatches.add(theResultObj);
								}
							}
						}

						if (!theMultipleMatches.isEmpty())
						{
							return Maybe.definitely((Object) theMultipleMatches);
						}
					}
					else
					{
						final Object	theFirstItem = theColl.iterator().next();    // Assume that each item in collection has the *same* property keys set!

						if ( theFirstItem instanceof Map)
						{
							final Maybe<Object>	theResult = _getMatchFromMap((Map<String,Object>) theFirstItem, inName, inWantMultipleValues);

							if (theResult.isKnown())
							{
								return theResult;
							}
						}
					}

				}
			}
		}

		return Maybe.unknown();
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public String[] getValues( final String inName)
	{
		final Maybe<Object>	theResult = _getMatchFromMap( m_Properties, inName, true);

		if (!theResult.isKnown())
		{
			return EMPTY_VALS;
		}

		///////////////////////////////////////////////////////////////////////////

		final Object	theVal = theResult.iterator().next();

		if ( theVal instanceof String)
		{
			return new String[]{ (String) theVal};
		}
		else if ( theVal instanceof List)
		{
			@SuppressWarnings("unchecked")
			final List<String>	theList = (List<String>) theVal;

			return Lists.transform( theList, new Function<Object,String>() {

				@Override
				public String apply( final Object inObj)
				{
					return String.valueOf(inObj);
				}
			}).toArray( new String[ theList.size() ] );
		}

		return EMPTY_VALS;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Resource addProperty( final String inName, Object inValue)
	{
		if (m_Properties.containsKey(inName))
		{
			final Object	theVal = m_Properties.get(inName);

			if ( theVal instanceof String)
			{
				final List<String>	theList = new ArrayList<String>();

				theList.add((String) theVal);
				theList.add((String) inValue);

				m_Properties.put( inName, theList);
			}
			else if ( theVal instanceof List)
			{
				((List<String>) theVal).add((String) inValue);
			}
		}
		else
		{
			m_Properties.put( inName, inValue);
		}

		return this;
	}

	@Override
	public Resource setProperty( final String inName, Object inValue)
	{
		removeProperties(inName);

		return addProperty( inName, inValue);
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper("Resource")
				.add( "id", m_Id)
				.add( "idx", m_Index)
				.add( "props", m_Properties)
				.toString();
	}
}