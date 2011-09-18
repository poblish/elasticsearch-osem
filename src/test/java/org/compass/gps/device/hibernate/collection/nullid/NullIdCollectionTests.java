package org.compass.gps.device.hibernate.collection.nullid;

import junit.framework.TestCase;
import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.core.util.FileHandlerMonitor;
import org.elasticsearch.gps.CompassGpsDevice;
import org.elasticsearch.gps.device.hibernate.HibernateGpsDevice;
import org.elasticsearch.gps.device.hibernate.HibernateSyncTransactionFactory;
import org.elasticsearch.gps.impl.SingleCompassGps;
import org.elasticsearch.osem.core.ObjectContext;
import org.elasticsearch.osem.core.ObjectContextFactory;
import org.elasticsearch.test.ElasticSearchTests;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

public class NullIdCollectionTests extends TestCase {

    private Compass compass;

    private FileHandlerMonitor fileHandlerMonitor;

    private SessionFactory sessionFactory;

    public void setUp() throws Exception {
        Configuration conf = new Configuration().configure("/org/compass/gps/device/hibernate/collection/nullid/hibernate.cfg.xml")
                .setProperty(Environment.HBM2DDL_AUTO, "create");
        sessionFactory = conf.buildSessionFactory();

        // set the session factory for the Hibernate transcation factory BEFORE we construct the compass instnace
        HibernateSyncTransactionFactory.setSessionFactory(sessionFactory);

	final ObjectContext	theObjectContext = ObjectContextFactory.create();

	theObjectContext.add( Album.class );
	theObjectContext.add( User.class );

//        CompassConfiguration cpConf = new CompassConfiguration()
//                .configure("/org/compass/gps/device/hibernate/collection/nullid/compass.cfg.xml");
//        cpConf.getSettings().setBooleanSetting(CompassEnvironment.DEBUG, true);
//        compass = cpConf.buildCompass();

	compass = ElasticSearchTests.mockSimpleCompass( "10.10.10.103", theObjectContext);	// cpConf.buildCompass();

        fileHandlerMonitor = FileHandlerMonitor.getFileHandlerMonitor(compass);
        fileHandlerMonitor.verifyNoHandlers();

        ElasticSearchTests.deleteAllIndexes(compass);
        ElasticSearchTests.verifyAllIndexes(compass);

        HibernateGpsDevice compassGpsDevice = new HibernateGpsDevice();
        compassGpsDevice.setName("hibernate");
        compassGpsDevice.setSessionFactory(sessionFactory);
        compassGpsDevice.setFetchCount(5000);
        compassGpsDevice.setMirrorDataChanges(true);

        SingleCompassGps compassGps = new SingleCompassGps();
        compassGps.setCompass(compass);
        compassGps.setGpsDevices(new CompassGpsDevice[]{compassGpsDevice});

        compassGps.start();

    }

    protected void tearDown() throws Exception {
        compass.close();

        fileHandlerMonitor.verifyNoHandlers();
        
        sessionFactory.close();
        
	ElasticSearchTests.deleteAllIndexes(compass);
    }

    public void testMarshall() {
        // Bad Hibernate, it does not set the ids on the Album objects if
        // not using hibSession.save(album).
        CompassSession session = compass.openSession();
        // (AGR_OSEM) ... CompassTransaction tr = session.beginTransaction();

        Session hibSession = sessionFactory.openSession();
        Transaction hibTr = hibSession.beginTransaction();

        User u1 = new User();
        u1.setName("barcho");

        Album a1 = new Album();
        a1.setTitle("the first album");
        hibSession.save(a1);

        Album a2 = new Album();
        a2.setTitle("the second album");
        hibSession.save(a2);

        u1.getAlbums().add(a1);
        u1.getAlbums().add(a2);

        hibSession.save(u1);

        // (AGR_OSEM) ... tr.commit();
        session.close();

        hibTr.commit();
        hibSession.close();
    }
}
