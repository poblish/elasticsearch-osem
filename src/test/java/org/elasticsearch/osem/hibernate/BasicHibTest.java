/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.elasticsearch.osem.hibernate;

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

//		CompassConfiguration	theCompassConfig = new CompassConfiguration();
		Compass			theCompass = createMock( Compass.class );

		expect( theCompass.getSettings() ).andReturn(theCompassSettings).atLeastOnce();

		replay(theCompass);

		//////////////////////////////////////////////////////////////////////////////////////

		EntityManagerFactory	theEMF = Persistence.createEntityManagerFactory("OsemTestPU");
		Session			theHibSession = (Session) theEMF.createEntityManager().getDelegate();
		HibernateGpsDevice	theDevice = new HibernateGpsDevice("Hib", theHibSession.getSessionFactory());

	//	theDevice.setIgnoreMirrorExceptions(true);
		theDevice.setFetchCount(500);

		final CompassGps		theCompassGps = new SingleCompassGps(theCompass);
		theCompassGps.addGpsDevice(theDevice);

		// Runtime.getRuntime().addShutdownHook(new CompassShutdownHook());

		System.out.println("Starting Compass... " + theCompassGps);

		theCompassGps.start();
	}
}