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
import org.elasticsearch.osem.core.ObjectContext;
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

	final ObjectContext	theObjectContext = ObjectContextFactory.create();

        // set the session factory for the Hibernate transcation factory BEFORE we construct the compass instnace
        HibernateSyncTransactionFactory.setSessionFactory(sessionFactory);

//        CompassConfiguration cpConf = new CompassConfiguration()
//                .configure("/org/compass/gps/device/hibernate/cascade/inheritance/compass.cfg.xml");
//        cpConf.getSettings().setBooleanSetting(CompassEnvironment.DEBUG, true);
        compass = ElasticSearchTests.mockSimpleCompass( "10.10.10.107", theObjectContext);	// cpConf.buildCompass();

        fileHandlerMonitor = FileHandlerMonitor.getFileHandlerMonitor(compass);
        fileHandlerMonitor.verifyNoHandlers();

        // (AGR_OSEM) ... compass.getSearchEngineIndexManager().deleteIndex();
        // (AGR_OSEM) ... compass.getSearchEngineIndexManager().verifyIndex();

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

/* (AGR_OSEM)

        try {
            compass.getSearchEngineIndexManager().deleteIndex();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        assertEquals(0, template.find("alias:user").length());
        assertEquals(0, template.find("alias:location").length());
        assertEquals(0, template.find("alias:city").length());

        s.save(user);

        assertEquals(1, template.find("alias:user").length());
        assertEquals(2, template.find("alias:location").length()); // same instances as city
        assertEquals(2, template.find("alias:city").length());

        tx.commit();
        s.close();
    }

    public void testUpdateMany() {
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

        assertEquals(0, template.find("alias:user").length());
        assertEquals(0, template.find("alias:location").length());
        assertEquals(0, template.find("alias:city").length());

        s.save(user);

        assertEquals(1, template.find("alias:user").length());
        assertEquals(2, template.find("alias:location").length()); // same instances as city
        assertEquals(2, template.find("alias:city").length());

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

        assertEquals(1, template.find("alias:user").length());
        assertEquals(4, template.find("alias:location").length()); // all kinds of location
        assertEquals(3, template.find("alias:city").length());

        tx.commit();
        s.close();

    }
}
