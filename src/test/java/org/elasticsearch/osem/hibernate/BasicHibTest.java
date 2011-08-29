/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.elasticsearch.osem.hibernate;

import org.elasticsearch.osem.core.ObjectContextFactory;
import java.util.Collection;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import java.util.Locale;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.compass.core.Compass;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.gps.CompassGps;
import org.elasticsearch.gps.device.hibernate.HibernateGpsDevice;
import org.elasticsearch.gps.impl.SingleCompassGps;
import org.elasticsearch.osem.test.entities.impl.Actor;
import org.elasticsearch.osem.test.entities.impl.Blog;
import org.elasticsearch.osem.test.entities.impl.Feed;
import org.elasticsearch.osem.test.entities.impl.TestArticle;
import org.elasticsearch.osem.test.entities.interfaces.ActorIF;
import org.elasticsearch.osem.test.entities.interfaces.ArticleIF;
import org.elasticsearch.osem.test.entities.interfaces.BlogIF;
import org.elasticsearch.osem.test.entities.interfaces.FeedIF;
import org.elasticsearch.test.ElasticSearchTests;
import org.testng.Assert;
import org.testng.annotations.Test;
import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 *
 * @author andrewregan
 */
public class BasicHibTest
{
	private static Client		s_Client;

	private final static String	IDX_ACTOR	= "actor";
	private final static String	IDX_ARTICLE	= "testarticle";
	private final static String	IDX_BLOG	= "blog";
	private final static String	IDX_FEED	= "feed";

	private final static String	TEST_TYPE	= "xxx";

	private static final ESLogger	m_Logger = Loggers.getLogger( BasicHibTest.class );

	/****************************************************************************
	****************************************************************************/
	@BeforeMethod
	public void setUp()
	{
		s_Client = new TransportClient().addTransportAddress( new InetSocketTransportAddress("10.10.10.107", 9300));

		// s_Client.prepareDeleteByQuery( IDX_ACTOR, IDX_ARTICLE, IDX_BLOG, IDX_FEED).setQuery( matchAllQuery() ).execute().actionGet();

		/* Clear down... */ ElasticSearchTests.deleteAllIndexes(s_Client);
	}
    
	/****************************************************************************
	****************************************************************************/
	@Test
	public void basicTest()
	{
		m_Logger.debug( "*** Using: " + s_Client + " / " + ((TransportClient) s_Client).connectedNodes());

		//////////////////////////////////////////////////////////////////////////////////////

		final Compass	theCompass = ElasticSearchTests.mockCompass( s_Client, ObjectContextFactory.create());

		ElasticSearchTests.populateContextAndIndices( theCompass, Actor.class, Blog.class, Feed.class, TestArticle.class);

		//////////////////////////////////////////////////////////////////////////////////////

		EntityManagerFactory	theEMF = Persistence.createEntityManagerFactory("OsemTestPU");
		final EntityManager	theEM = theEMF.createEntityManager();
		HibernateGpsDevice	theDevice = new HibernateGpsDevice("Hib", theEM);

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

		theEM.getTransaction().begin();

		m_Logger.debug( "*** Persisting...");

		theEM.persist(theFeed);

		theArticle.setResource(theFeed);

		theEM.persist(theArticle);
		theEM.flush();
		theEM.getTransaction().commit();
		theEM.clear();

		testSynchronisation(theEM);

		//////////////////////////////////////////////////////////////////////////////////////

		final ActorIF	theLoadedActor = theEM.createQuery("FROM Actor WHERE userName='aregan'", Actor.class).getSingleResult();

		theEM.getTransaction().begin();
		theEM.remove(theLoadedActor);
		theEM.flush();
		theEM.getTransaction().commit();

		Assert.assertTrue( theEM.createQuery("FROM Actor WHERE userName='aregan'", Actor.class).getResultList().isEmpty() );

		s_Client.admin().indices().prepareRefresh("actor").execute().actionGet();

		final SearchResponse	theActorResp2 = s_Client.prepareSearch("actor").setQuery( matchAllQuery() ).execute().actionGet();

		Assert.assertEquals( theActorResp2.getHits().totalHits(), 0, "Wrong number of Actors #2");

		testSynchronisation(theEM);

		//////////////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////////////////////////////

		final FeedIF	theLoadedFeed = theEM.createQuery("FROM Feed WHERE url='http://www.poblish.org/blog/'", Feed.class).getSingleResult();

		theEM.getTransaction().begin();
		theEM.remove(theLoadedFeed);
		theEM.flush();
		theEM.getTransaction().commit();

		Assert.assertTrue( theEM.createQuery("FROM Feed WHERE url='http://www.poblish.org/blog/'", Feed.class).getResultList().isEmpty() );

		s_Client.admin().indices().prepareRefresh("feed").execute().actionGet();

		final SearchResponse	theFeedResp2 = s_Client.prepareSearch("feed").setQuery( matchAllQuery() ).execute().actionGet();

		Assert.assertEquals( theFeedResp2.getHits().totalHits(), 0, "Wrong number of Feeds #2");

		testSynchronisation(theEM);
	}

	/****************************************************************************
	****************************************************************************/
	private void testSynchronisation( final EntityManager inEM)
	{
		final Collection<TestArticle>	theArticles = inEM.createQuery("FROM TestArticle WHERE title='Seumas Milne'", TestArticle.class).getResultList();
		final Collection<Actor>		theActors = inEM.createQuery("FROM Actor WHERE userName='aregan'", Actor.class).getResultList();
		final Collection<Blog>		theBlogs = inEM.createQuery("FROM Blog WHERE url='http://www.poblish.org/blog/'", Blog.class).getResultList();
		final Collection<Feed>		theFeeds = inEM.createQuery("FROM Feed WHERE url='http://www.poblish.org/blog/'", Feed.class).getResultList();

		System.out.println("> Articles = " + theArticles);
		System.out.println("> Actors   = " + theActors);
		System.out.println("> Blogs    = " + theBlogs);
		System.out.println("> Feeds    = " + theFeeds);

		Assert.assertTrue( theArticles.size() <= 1 );
		Assert.assertTrue( theActors.size() <= 1 );
		Assert.assertTrue( theBlogs.size() <= 1 );
		Assert.assertTrue( theFeeds.size() <= 1 );

		s_Client.admin().indices().prepareRefresh( IDX_ACTOR, IDX_ARTICLE, IDX_BLOG, IDX_FEED).execute().actionGet();

		//////////////////////////////////////////////////////////////////////////////////////////////////

		if (theArticles.isEmpty())
		{
			final SearchResponse	theZeroArticlesResp = s_Client.prepareSearch(IDX_ARTICLE).setQuery( matchAllQuery() ).execute().actionGet();

			Assert.assertEquals( theZeroArticlesResp.getHits().totalHits(), 0, "Wrong number of Articles #2");
		}
		else
		{
			final ArticleIF		the1Article = theArticles.iterator().next();

			Assert.assertNotNull( the1Article.getId() );

			final SearchResponse	theActorResp = s_Client.prepareSearch(IDX_ARTICLE).setQuery( idsQuery(TEST_TYPE).addIds( String.valueOf( the1Article.getId() ) ) ).execute().actionGet();

			Assert.assertEquals( theActorResp.getHits().totalHits(), 1, "Wrong number of Articles (#1) for Id #" + the1Article.getId());
		}

		//////////////////////////////////////////////////////////////////////////////////////////////////

		if (theActors.isEmpty())
		{
			final SearchResponse	theZeroActorsResp = s_Client.prepareSearch(IDX_ACTOR).setQuery( matchAllQuery() ).execute().actionGet();

			Assert.assertEquals( theZeroActorsResp.getHits().totalHits(), 0, "Wrong number of Actors #2");
		}
		else
		{
			final ActorIF		the1Actor = theActors.iterator().next();

			Assert.assertNotNull( the1Actor.getId() );

			final SearchResponse	theActorResp = s_Client.prepareSearch(IDX_ACTOR).setQuery( idsQuery(TEST_TYPE).addIds( String.valueOf( the1Actor.getId() ) ) ).execute().actionGet();

			Assert.assertEquals( theActorResp.getHits().totalHits(), 1, "Wrong number of Actors (#1) for Id #" + the1Actor.getId());
		}

		//////////////////////////////////////////////////////////////////////////////////////////////////

		if (theBlogs.isEmpty())
		{
			final SearchResponse	theZeroBlogsResp = s_Client.prepareSearch(IDX_BLOG).setQuery( matchAllQuery() ).execute().actionGet();

			Assert.assertEquals( theZeroBlogsResp.getHits().totalHits(), 0, "Wrong number of Blogs #2");
		}
		else
		{
			final BlogIF		the1Blog = theBlogs.iterator().next();

			Assert.assertNotNull( the1Blog.getId() );

			final SearchResponse	theBlogResp = s_Client.prepareSearch(IDX_BLOG).setQuery( idsQuery(TEST_TYPE).addIds( String.valueOf( the1Blog.getId() ) ) ).execute().actionGet();

			Assert.assertEquals( theBlogResp.getHits().totalHits(), 1, "Wrong number of Blogs (#1) for Id #" + the1Blog.getId());
		}

		//////////////////////////////////////////////////////////////////////////////////////////////////

		if (theFeeds.isEmpty())
		{
			final SearchResponse	theZeroFeedsResp = s_Client.prepareSearch(IDX_FEED).setQuery( matchAllQuery() ).execute().actionGet();

			Assert.assertEquals( theZeroFeedsResp.getHits().totalHits(), 0, "Wrong number of Feeds #2");
		}
		else
		{
			final FeedIF		the1Feed = theFeeds.iterator().next();

			Assert.assertNotNull( the1Feed.getId() );

			final SearchResponse	theFeedResp = s_Client.prepareSearch(IDX_FEED).setQuery( idsQuery(TEST_TYPE).addIds( String.valueOf( the1Feed.getId() ) ) ).execute().actionGet();

			Assert.assertEquals( theFeedResp.getHits().totalHits(), 1, "Wrong number of Feeds (#1) for Id #" + the1Feed.getId());
		}
	}

	/****************************************************************************
	****************************************************************************/
	@AfterMethod
	public void tearDown()
	{
		s_Client.close();
	}
}