/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.compass.integration;

import java.util.Map;
import org.elasticsearch.osem.core.ObjectContext;
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
	public static Resource fromMap( final ObjectContext inCtxt, final Map<String, Object> inMap)
	{
		return new InternalResource( inCtxt, inMap);
	}
}