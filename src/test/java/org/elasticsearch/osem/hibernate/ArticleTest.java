/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.elasticsearch.osem.hibernate;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.osem.test.entities.interfaces.ArticleIF;
import org.elasticsearch.osem.test.entities.impl.Actor;
import org.elasticsearch.osem.test.entities.impl.Blog;
import org.elasticsearch.osem.test.entities.impl.Feed;
import org.elasticsearch.osem.test.entities.impl.TestArticle;
import org.elasticsearch.osem.test.entities.interfaces.ActorIF;
import org.elasticsearch.osem.test.entities.interfaces.ActorResourceIF;
import org.elasticsearch.osem.test.entities.interfaces.BlogIF;
import org.testng.annotations.Test;
import org.elasticsearch.ElasticSearchException;
import java.util.Locale;
import org.elasticsearch.osem.core.ObjectContext;
import org.elasticsearch.osem.core.ObjectContextFactory;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 *
 * @author andrewregan
 */
public class ArticleTest
{
	private final static String	INDEX = "AbstractArticle".toLowerCase();

	private static final ESLogger	m_Logger = Loggers.getLogger( ArticleTest.class );

	/****************************************************************************
	****************************************************************************/
	@Test
	public void testArticle() throws IOException
	{
		Client client = new TransportClient().addTransportAddress( new InetSocketTransportAddress("10.10.10.107", 9300));

		//////////////////////////////////////////////////////////////////////////////////////

		final ObjectContext	theCtxt = ObjectContextFactory.create();

		theCtxt.add( Actor.class );
		theCtxt.add( Blog.class );
		theCtxt.add( Feed.class );
		theCtxt.add( TestArticle.class );

		//////////////////////////////////////////////////////////////////////////////////////

		final QueryBuilder	theIdQB = idsQuery("xxx").addIds("1979");

		try
		{
		//	final SearchRequest	sr = Requests.searchRequest(INDEX);
			final SearchResponse	theResp = client.prepareSearch(INDEX).setQuery(theIdQB).execute().actionGet( 5, TimeUnit.SECONDS);

			m_Logger.debug("Found = " + theResp);
		}
		catch (ElasticSearchException e)    // Index doesn't exist
		{
			m_Logger.error("", e);
		}

		//////////////////////////////////////////////////////////////////////////////////////

		final ArticleIF		theArticle = new TestArticle();
		theArticle.setContent("As long as British governments back wars and occupations in the Middle East and Muslim world, there will continue to be a risk of violence in Britain. But attempts to drive British Muslims out of normal political activity, and the refusal to confront anti-Muslim hatred, can only ratchet up the danger and threaten us all.");
		theArticle.setTitle("Seumas Milne");

		final ActorIF		theActor = new Actor( "aregan", "Andrew", "Regan", "", null);
		final BlogIF		theBlog = new Blog( theActor, "http://www.poblish.org/blog/", Locale.UK);
		final ActorResourceIF	theRes = new Feed( theBlog, "http://www.poblish.org/blog/", "desc", false, Locale.UK);

		theArticle.setResource(theRes);

		//////////////////////////////////////////////////////////////////////////////////////

		final IndexResponse	theResponse = client.prepareIndex( INDEX, "xxx", "1979")
							    .setSource( theCtxt.write(theArticle) )
							    .execute()
							    .actionGet();

		m_Logger.debug("theResp = " + theResponse + " / " + theResponse.id());

		//////////////////////////////////////////////////////////////////////////////////////

		client.close();
	}
}
