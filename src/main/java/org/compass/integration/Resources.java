/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.compass.integration;

import java.util.Map;
import org.elasticsearch.search.SearchHit;

/**
 *
 * @author andrewregan
 */
public class Resources
{
	/****************************************************************************
	****************************************************************************/
	public static Resource fromHit(final SearchHit inHit)
	{
		return new InternalResource( inHit.getIndex(), inHit.getId()).setSourceProperties( inHit.getSource() );
	}

	/****************************************************************************
	****************************************************************************/
	public static Resource fromSourceMap( final Map<String, Object> inMap)
	{
		return InternalResource.fromSourceMap(inMap);
	}

	/****************************************************************************
	****************************************************************************/
	public static Resource fromJsonMap( final String inIdx, final String inId, final Map<String, Object> inMap)
	{
		return InternalResource.fromJsonMap( inIdx, inId, inMap);
	}
}