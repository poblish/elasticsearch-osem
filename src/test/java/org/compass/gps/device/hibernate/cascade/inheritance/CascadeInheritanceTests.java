package org.compass.gps.device.hibernate.cascade.inheritance;

import java.util.HashSet;

import junit.framework.TestCase;
import org.compass.core.Compass;
import org.compass.core.CompassTemplate;
import org.compass.core.util.FileHandlerMonitor;
import org.elasticsearch.gps.CompassGpsDevice;
import org.elasticsearch.gps.device.hibernate.HibernateGpsDevice;
import org.elasticsearch.gps.device.hibernate.HibernateSyncTransactionFactory;
import org.elasticsearch.gps.impl.SingleCompassGps;
import org.elasticsearch.osem.core.ObjectContextFactory;
import org.elasticsearch.test.ElasticSearchTests;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.classic.Session;

/**
 * @author Maurice Nicholson
 */
public class CascadeInheritanceTests extends TestCase {

    private SessionFactory sessionFactory;
    private Compass compass;
    private CompassTemplate template;

    private FileHandlerMonitor fileHandlerMonitor;

    public void setUp() throws Exception {
        Configuration conf = new Configuration().configure("/org/compass/gps/device/hibernate/cascade/inheritance/hibernate.cfg.xml")
                .setProperty(Environment.HBM2DDL_AUTO, "create");
        sessionFactory = conf.buildSessionFactory();

        // set the session factory for the Hibernate transcation factory BEFORE we construct the compass instnace
        HibernateSyncTransactionFactory.setSessionFactory(sessionFactory);

//        CompassConfiguration cpConf = new CompassConfiguration()
//                .configure("/org/compass/gps/device/hibernate/cascade/inheritance/compass.cfg.xml");
//        cpConf.getSettings().setBooleanSetting(CompassEnvironment.DEBUG, true);

        compass = ElasticSearchTests.mockSimpleCompass( "10.10.10.107", ObjectContextFactory.create());	// cpConf.buildCompass();

        fileHandlerMonitor = FileHandlerMonitor.getFileHandlerMonitor(compass);
        fileHandlerMonitor.verifyNoHandlers();

        ElasticSearchTests.deleteAllIndexes(compass);
        ElasticSearchTests.verifyAllIndexes(compass);

	ElasticSearchTests.populateContextAndIndices( compass, User.class, Location.class, City.class);

        HibernateGpsDevice compassGpsDevice = new HibernateGpsDevice();
        compassGpsDevice.setName("hibernate");
        compassGpsDevice.setSessionFactory(sessionFactory);
        compassGpsDevice.setFetchCount(5000);

        SingleCompassGps compassGps = new SingleCompassGps();
        compassGps.setCompass(compass);
        compassGps.setGpsDevices(new CompassGpsDevice[] {
            compassGpsDevice
        });

        compassGps.start();

        template = new CompassTemplate(compass);
    }

    protected void tearDown() throws Exception {
        compass.close();
        fileHandlerMonitor.verifyNoHandlers();

        sessionFactory.close();
        template = null;

	ElasticSearchTests.deleteAllIndexes(compass);

/* (AGR_OSEM)
        if (compass.getSpellCheckManager() != null) {
            try {
                compass.getSpellCheckManager().deleteIndex();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
 */
    }

    public void testSaveMany() {
        Session s = sessionFactory.openSession();
        Transaction tx = s.beginTransaction();

        City london = new City();
        london.name = "London";
        City newYork = new City();
        newYork.name = "New York City";

        User user = new User();
        user.name = "Charles";
        user.favouritePlaces = new HashSet();
        user.favouritePlaces.add(london);
        user.favouritePlaces.add(newYork);

        assertEquals(0, ElasticSearchTests.countHitsForIndex( compass, "user"));
        assertEquals(0, ElasticSearchTests.countHitsForIndex( compass, "location"));
        assertEquals(0, ElasticSearchTests.countHitsForIndex( compass, "city"));

        s.save(user);

        assertEquals(1, ElasticSearchTests.countHitsForIndex( compass, "user"));
	// (AGR) FIXME. assertEquals(2, ElasticSearchTests.countHitsForIndex( compass, "location")); // same instances as city
        assertEquals(2, ElasticSearchTests.countHitsForIndex( compass, "city"));

        tx.commit();
        s.close();
    }

    public void testUpdateMany() {
	System.out.println("000");
        Session s = sessionFactory.openSession();
        Transaction tx = s.beginTransaction();

        City london = new City();
        london.name = "London";
        City newYork = new City();
        newYork.name = "New York City";

        User user = new User();
        user.name = "Charles";
        user.favouritePlaces = new HashSet();
        user.favouritePlaces.add(london);
        user.favouritePlaces.add(newYork);

        assertEquals(0, ElasticSearchTests.countHitsForIndex( compass, "user"));
        assertEquals(0, ElasticSearchTests.countHitsForIndex( compass, "location"));
        assertEquals(0, ElasticSearchTests.countHitsForIndex( compass, "city"));

        s.save(user);

        assertEquals(1, ElasticSearchTests.countHitsForIndex( compass, "user"));
	// (AGR) FIXME. assertEquals(2, ElasticSearchTests.countHitsForIndex( compass, "location")); // same instances as city
        assertEquals(2, ElasticSearchTests.countHitsForIndex( compass, "city"));

        tx.commit();
        s.close();

        s = sessionFactory.openSession();
        tx = s.beginTransaction();

        City rio = new City();
        rio.name = "Rio de Janeiro";
        Location nod = new Location();
        nod.name = "Land of Nod";

        user.favouritePlaces.add(rio);
        user.favouritePlaces.add(nod);

        s.saveOrUpdate(user);

        assertEquals(1, ElasticSearchTests.countHitsForIndex( compass, "user"));
        assertEquals(4, ElasticSearchTests.countHitsForIndex( compass, "location")); // all kinds of location
        assertEquals(3, ElasticSearchTests.countHitsForIndex( compass, "city"));

        tx.commit();
        s.close();

    }
}
