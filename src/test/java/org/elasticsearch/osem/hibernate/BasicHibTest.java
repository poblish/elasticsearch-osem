/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.elasticsearch.osem.hibernate;

import org.elasticsearch.osem.test.entities.interfaces.ActorIF;
import java.util.Locale;
import org.elasticsearch.osem.test.entities.interfaces.ActorResourceIF;
import org.elasticsearch.osem.test.entities.interfaces.BlogIF;
import javax.persistence.EntityManager;
import org.elasticsearch.osem.test.entities.interfaces.ArticleIF;
import org.compass.core.config.CompassSettings;
import org.elasticsearch.osem.test.entities.impl.Actor;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.osem.core.ObjectContext;
import org.elasticsearch.osem.core.ObjectContextFactory;
import org.elasticsearch.osem.test.entities.impl.Blog;
import org.elasticsearch.osem.test.entities.impl.Feed;
import org.elasticsearch.osem.test.entities.impl.TestArticle;
import org.testng.annotations.Test;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.compass.core.Compass;
import org.elasticsearch.gps.CompassGps;
import org.elasticsearch.gps.device.hibernate.HibernateGpsDevice;
import org.elasticsearch.gps.impl.SingleCompassGps;
import org.hibernate.Session;
import static org.easymock.EasyMock.*;
import static org.powermock.api.easymock.PowerMock.createMock;

/**
 *
 * @author andrewregan
 */
// @RunWith(PowerMockRunner.class)
public class BasicHibTest
{
	private final static String	INDEX = "AbstractArticle".toLowerCase();

	@Test
	public void basicTest()
	{
		Client client = new TransportClient().addTransportAddress( new InetSocketTransportAddress("10.10.10.107", 9300));

		//////////////////////////////////////////////////////////////////////////////////////

	//	client.prepareDeleteByQuery(INDEX).execute().actionGet();

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

		System.out.println("Starting Compass... " + theCompassGps);

		theCompassGps.start();

		//////////////////////////////////////////////////////////////////////////////////////

		final ArticleIF	theArticle = new TestArticle();
		theArticle.setContent("As long as British governments back wars and occupations in the Middle East and Muslim world, there will continue to be a risk of violence in Britain. But attempts to drive British Muslims out of normal political activity, and the refusal to confront anti-Muslim hatred, can only ratchet up the danger and threaten us all.");
		theArticle.setTitle("Seumas Milne");

		final ActorIF		theActor = new Actor( "aregan", "Andrew", "Regan", "", null);
		final BlogIF		theBlog = new Blog( theActor, "http://www.poblish.org/blog/", Locale.UK);
		final ActorResourceIF	theFeed = new Feed( theBlog, "http://www.poblish.org/blog/", "desc", false, Locale.UK);

		if (!theEM.getTransaction().isActive())
		{
			theEM.getTransaction().begin();
		}

		theEM.persist(theFeed);

		theArticle.setResource(theFeed);

		theEM.persist(theArticle);
		theEM.flush();
		theEM.getTransaction().commit();

		//////////////////////////////////////////////////////////////////////////////////////

//		final IndexResponse	theResponse = client.prepareIndex( INDEX, "xxx", "1980")
//							    .setSource( theCtxt.write(theArticle) )
//							    .execute()
//							    .actionGet();

//		System.out.println("theResp = " + theResponse + " / " + theResponse.id());

		//////////////////////////////////////////////////////////////////////////////////////

		client.close();
	}
}