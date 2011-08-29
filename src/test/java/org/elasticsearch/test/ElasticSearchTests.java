/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.elasticsearch.test;

import org.elasticsearch.gps.IndexPlan;
import org.compass.core.events.RebuildEventListener;
import org.compass.core.spi.InternalCompass;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.osem.core.ElasticSearchSession;
import org.compass.core.CompassSession;
import org.easymock.IAnswer;
import org.compass.core.Compass;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.osem.core.ObjectContext;
import org.powermock.api.easymock.PowerMock;
import static org.easymock.EasyMock.*;
import static org.elasticsearch.index.query.QueryBuilders.*;

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

	    expect( theCompassSettings.getClassLoader() ).andReturn( inCtxt.getClass().getClassLoader() ).anyTimes();
	    expect( theCompassSettings.getSetting( CompassEnvironment.CONNECTION ) ).andReturn("/tmp").anyTimes();

	    replay(theCompassSettings);

	    return mockCompass( inClient, inCtxt, theCompassSettings);
	}

	/****************************************************************************
	****************************************************************************/
	public static Compass mockCompass( final Client inClient, final ObjectContext inCtxt, final CompassSettings inSettings)
	{
		final InternalCompass theCompass = createMock( InternalCompass.class );

		expect( theCompass.getSettings() ).andReturn(inSettings).atLeastOnce();
		expect( theCompass.getObjectContext() ).andReturn(inCtxt).anyTimes();
		expect( theCompass.getClient() ).andReturn(inClient).anyTimes();
		expect( theCompass.openSession() ).andAnswer( new IAnswer<CompassSession>() {

			public CompassSession answer() throws Throwable
			{
				return new ElasticSearchSession( inCtxt, inClient);
			}
		} ).anyTimes();

		theCompass.close();
		expectLastCall().anyTimes();

		theCompass.addRebuildEventListener( anyObject( RebuildEventListener.class ) );
		expectLastCall().anyTimes();

		///////////////////////////////////////////////////////////////////////////////////////////////////

		replay(theCompass);

		return theCompass;
	}

	/****************************************************************************
	****************************************************************************/
	public static void deleteAllIndexes( final Compass inCompass)
	{
		deleteAllIndexes( inCompass.getClient() );
	}

	/****************************************************************************
	****************************************************************************/
	public static void deleteAllIndexes( final Client inClient)
	{
		inClient.admin().indices().prepareDelete("_all").execute().actionGet();
	}

	/****************************************************************************
	****************************************************************************/
	public static void verifyAllIndexes( final Compass inCompass)
	{
		// NOOP
	}

	/****************************************************************************
	****************************************************************************/
	public static long countHitsForIndex( final Compass inCompass, final String inIdx)
	{
		inCompass.getClient().admin().indices().prepareRefresh(inIdx).execute().actionGet();	// (AGR) Be careful!!

		return inCompass.getClient().prepareSearch(inIdx).setQuery( matchAllQuery() ).execute().actionGet().getHits().getTotalHits();
	}

	/****************************************************************************
	****************************************************************************/
	public static void populateContextAndIndices( final Compass inCompass, final Class... inClasses)
	{
		final IndicesAdminClient		theClient = inCompass.getClient().admin().indices();

		for ( final Class eachClazz : inClasses)
		{
			inCompass.getObjectContext().add(eachClazz);

			final String	theIndexName = inCompass.getObjectContext().getType(eachClazz);

			/* final CreateIndexResponse x = */ theClient.create( new CreateIndexRequest(theIndexName)).actionGet();    // (AGR) Should we batch?
		}
	}
}