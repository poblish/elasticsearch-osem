/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.elasticsearch.osem.hibernate;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import java.util.Locale;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.compass.core.Compass;
import org.compass.core.config.CompassSettings;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.gps.CompassGps;
import org.elasticsearch.gps.device.hibernate.HibernateGpsDevice;
import org.elasticsearch.gps.impl.SingleCompassGps;
import org.elasticsearch.osem.core.ObjectContext;
import org.elasticsearch.osem.core.ObjectContextFactory;
import org.elasticsearch.osem.test.entities.impl.Actor;
import org.elasticsearch.osem.test.entities.impl.Blog;
import org.elasticsearch.osem.test.entities.impl.Feed;
import org.elasticsearch.osem.test.entities.impl.TestArticle;
import org.elasticsearch.osem.test.entities.interfaces.ActorIF;
import org.elasticsearch.osem.test.entities.interfaces.ArticleIF;
import org.elasticsearch.osem.test.entities.interfaces.BlogIF;
import org.elasticsearch.osem.test.entities.interfaces.FeedIF;
import org.hibernate.Session;
import org.testng.Assert;
import org.testng.annotations.Test;
import static org.easymock.EasyMock.*;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 *
 * @author andrewregan
 */
public class BasicHibTest
{
	private static Client		s_Client;

	private static final ESLogger	m_Logger = Loggers.getLogger( BasicHibTest.class );

	/****************************************************************************
	****************************************************************************/
	@BeforeMethod
	public void setUp()
	{
		s_Client = new TransportClient().addTransportAddress( new InetSocketTransportAddress("10.10.10.107", 9300));

		// Clear down...

		s_Client.prepareDeleteByQuery("actor", "blog", "feed", "testarticle").setQuery( matchAllQuery() ).execute().actionGet();
	}
    
	/****************************************************************************
	****************************************************************************/
	@Test
	public void basicTest()
	{
		m_Logger.debug( "*** Using: " + s_Client + " / " + ((TransportClient) s_Client).connectedNodes());

		//////////////////////////////////////////////////////////////////////////////////////

		final ObjectContext	theCtxt = ObjectContextFactory.create();

		theCtxt.add( Actor.class );
		theCtxt.add( Blog.class );
		theCtxt.add( Feed.class );
		theCtxt.add( TestArticle.class );

		//////////////////////////////////////////////////////////////////////////////////////

		CompassSettings		theCompassSettings = createMock( CompassSettings.class );

		expect( theCompassSettings.getClassLoader() ).andReturn( this.getClass().getClassLoader() ).atLeastOnce();

		replay(theCompassSettings);

		//////////////////////////////////////////////////////////////////////////////////////

		Compass			theCompass = createMock( Compass.class );

		expect( theCompass.getSettings() ).andReturn(theCompassSettings).atLeastOnce();
		expect( theCompass.getObjectContext() ).andReturn(theCtxt).atLeastOnce();
		expect( theCompass.getClient() ).andReturn(s_Client).atLeastOnce();

		replay(theCompass);

		//////////////////////////////////////////////////////////////////////////////////////

		EntityManagerFactory	theEMF = Persistence.createEntityManagerFactory("OsemTestPU");
		final EntityManager	theEM = theEMF.createEntityManager();
		final Session		theHibSession = (Session) theEM.getDelegate();
		HibernateGpsDevice	theDevice = new HibernateGpsDevice("Hib", theHibSession.getSessionFactory());

	//	theDevice.setIgnoreMirrorExceptions(true);
		theDevice.setFetchCount(500);

		final CompassGps		theCompassGps = new SingleCompassGps(theCompass);
		theCompassGps.addGpsDevice(theDevice);

	//	log.debug("Starting Compass... " + theCompassGps);

		theCompassGps.start();

		//////////////////////////////////////////////////////////////////////////////////////

		final ArticleIF	theArticle = new TestArticle();
		theArticle.setContent("As long as British governments back wars and occupations in the Middle East and Muslim world, there will continue to be a risk of violence in Britain. But attempts to drive British Muslims out of normal political activity, and the refusal to confront anti-Muslim hatred, can only ratchet up the danger and threaten us all.");
		theArticle.setTitle("Seumas Milne");

		final ActorIF		theActor = new Actor( "aregan", "Andrew", "Regan", "", null);
		final BlogIF		theBlog = new Blog( theActor, "http://www.poblish.org/blog/", Locale.UK);
		final FeedIF		theFeed = new Feed( theBlog, "http://www.poblish.org/blog/", "desc", false, Locale.UK);

		if (!theEM.getTransaction().isActive())
		{
			theEM.getTransaction().begin();
		}

		m_Logger.debug( "*** Persisting...");

		theEM.persist(theFeed);

		theArticle.setResource(theFeed);

		theEM.persist(theArticle);
		theEM.flush();
		theEM.getTransaction().commit();
		theEM.clear();

		//////////////////////////////////////////////////////////////////////////////////////

		final ActorIF		theLoadedActor = theEM.createQuery("FROM Actor WHERE userName='aregan'", Actor.class).getSingleResult();
		final BlogIF		theLoadedBlog = theEM.createQuery("FROM Blog WHERE url='http://www.poblish.org/blog/'", Blog.class).getSingleResult();
		final FeedIF		theLoadedFeed = theEM.createQuery("FROM Feed WHERE url='http://www.poblish.org/blog/'", Feed.class).getSingleResult();

		Assert.assertNotNull( theLoadedActor.getId() );
		Assert.assertNotNull( theLoadedBlog.getId() );
		Assert.assertNotNull( theLoadedFeed.getId() );

		//////////////////////////////////////////////////////////////////////////////////////

		m_Logger.debug( "*** Refreshing...");

		s_Client.admin().indices().prepareRefresh("actor").execute().actionGet();
		s_Client.admin().indices().prepareRefresh("blog").execute().actionGet();
		s_Client.admin().indices().prepareRefresh("feed").execute().actionGet();

		m_Logger.debug( "*** Searching...");

		final SearchResponse	theActorResp = s_Client.prepareSearch("actor").setQuery( idsQuery("xxx").addIds( String.valueOf( theLoadedActor.getId() ) ) ).execute().actionGet();

		Assert.assertEquals( theActorResp.getHits().totalHits(), 1, "Wrong number of Actors (#1) for Id #" + theLoadedActor.getId());

		final SearchResponse	theBlogResp = s_Client.prepareSearch("blog").setQuery( idsQuery("xxx").addIds( String.valueOf( theLoadedBlog.getId() ) ) ).execute().actionGet();

		Assert.assertEquals( theBlogResp.getHits().totalHits(), 1, "Wrong number of Blogs (#1) for Id #" + theLoadedBlog.getId());

		final SearchResponse	theFeedResp = s_Client.prepareSearch("feed").setQuery( idsQuery("xxx").addIds( String.valueOf( theLoadedFeed.getId() ) ) ).execute().actionGet();

		Assert.assertEquals( theFeedResp.getHits().totalHits(), 1, "Wrong number of Feeds (#1) for Id #" + theLoadedFeed.getId());

		//////////////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////////////////////////////

		theEM.getTransaction().begin();

		m_Logger.debug( "*** Removing...");

		theEM.remove(theLoadedActor);
		theEM.flush();
		theEM.getTransaction().commit();

		Assert.assertTrue( theEM.createQuery("FROM Actor WHERE userName='aregan'", Actor.class).getResultList().isEmpty() );

		s_Client.admin().indices().prepareRefresh("actor").execute().actionGet();

		final SearchResponse	theActorResp2 = s_Client.prepareSearch("actor").setQuery( matchAllQuery() ).execute().actionGet();

		Assert.assertEquals( theActorResp2.getHits().totalHits(), 0, "Wrong number of Actors #2");

		//////////////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////////////////////////////

		theEM.getTransaction().begin();
		theEM.remove(theLoadedFeed);
		theEM.flush();
		theEM.getTransaction().commit();

		Assert.assertTrue( theEM.createQuery("FROM Feed WHERE url='http://www.poblish.org/blog/'", Feed.class).getResultList().isEmpty() );

		s_Client.admin().indices().prepareRefresh("feed").execute().actionGet();

		final SearchResponse	theFeedResp2 = s_Client.prepareSearch("feed").setQuery( matchAllQuery() ).execute().actionGet();

		Assert.assertEquals( theFeedResp2.getHits().totalHits(), 0, "Wrong number of Feeds #2");
	}

	/****************************************************************************
	****************************************************************************/
	@AfterMethod
	public void tearDown()
	{
		s_Client.close();
	}
}