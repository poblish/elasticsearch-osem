/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.elasticsearch.test;

import org.compass.core.Compass;
import org.compass.core.config.CompassSettings;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.osem.core.ObjectContext;
import static org.easymock.EasyMock.*;

/**
 *
 * @author andrewregan
 */
public class ElasticSearchTests
{
	/****************************************************************************
	****************************************************************************/
	public static Client simpleClient( final String inIP)
	{
	    return simpleClient( inIP, 9300);
	}

	/****************************************************************************
	****************************************************************************/
	public static Client simpleClient( final String inIP, final int inPort)
	{
	    return new TransportClient().addTransportAddress( new InetSocketTransportAddress( inIP, inPort));
	}

	/****************************************************************************
	****************************************************************************/
	public static Compass mockSimpleCompass( final String inSingleNodeIP, final ObjectContext inCtxt)
	{
	    return mockCompass( simpleClient(inSingleNodeIP), inCtxt);
	}

	/****************************************************************************
	****************************************************************************/
	public static Compass mockCompass( final Client inClient, final ObjectContext inCtxt)
	{
	    final CompassSettings theCompassSettings = createMock( CompassSettings.class );

	    expect( theCompassSettings.getClassLoader() ).andReturn( inCtxt.getClass().getClassLoader() ).atLeastOnce();

	    replay(theCompassSettings);

	    return mockCompass( inClient, inCtxt, theCompassSettings);
	}

	/****************************************************************************
	****************************************************************************/
	public static Compass mockCompass( final Client inClient, final ObjectContext inCtxt, final CompassSettings inSettings)
	{
	    final Compass theCompass = createMock( Compass.class );

	    expect( theCompass.getSettings() ).andReturn(inSettings).atLeastOnce();
	    expect( theCompass.getObjectContext() ).andReturn(inCtxt).atLeastOnce();
	    expect( theCompass.getClient() ).andReturn(inClient).atLeastOnce();

	    replay(theCompass);

	    return theCompass;
	}
}
