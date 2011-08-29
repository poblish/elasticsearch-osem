/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.compass.gps.device.hibernate.collection;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.compass.core.Compass;
import org.compass.core.CompassTemplate;
import org.compass.core.util.FileHandlerMonitor;
import org.elasticsearch.gps.device.hibernate.HibernateGpsDevice;
import org.elasticsearch.gps.impl.SingleCompassGps;
import org.elasticsearch.osem.core.ObjectContext;
import org.elasticsearch.osem.core.ObjectContextFactory;
import org.elasticsearch.test.ElasticSearchTests;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.objectweb.jotm.Jotm;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author kimchy
 */
public class CollectionTests {

    private Jotm jotm;

    private Compass compass;

    private FileHandlerMonitor fileHandlerMonitor;

    private CompassTemplate compassTemplate;

    private SingleCompassGps compassGps;

    private SessionFactory sessionFactory;

    @BeforeMethod
    public void setUp() throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
        System.setProperty(Context.PROVIDER_URL, "rmi://localhost:1099");

        try {
            java.rmi.registry.LocateRegistry.createRegistry(1099);
        } catch (Exception e) {

        }

        jotm = new Jotm(true, true);
        Context ctx = new InitialContext();
        ctx.rebind("java:comp/UserTransaction", jotm.getUserTransaction());

	final ObjectContext	theObjectContext = ObjectContextFactory.create();

	theObjectContext.add( Child.class );
	theObjectContext.add( Parent.class );

//        CompassConfiguration cpConf = new CompassConfiguration()
//                .configure("/org/compass/gps/device/hibernate/collection/compass.cfg.xml");
//        cpConf.getSettings().setBooleanSetting(CompassEnvironment.DEBUG, true);
//        compass = cpConf.buildCompass();

	compass = ElasticSearchTests.mockSimpleCompass( "10.10.10.107", theObjectContext);	// cpConf.buildCompass();

        fileHandlerMonitor = FileHandlerMonitor.getFileHandlerMonitor(compass);
        fileHandlerMonitor.verifyNoHandlers();

        ElasticSearchTests.deleteAllIndexes(compass);
        ElasticSearchTests.verifyAllIndexes(compass);

        compassTemplate = new CompassTemplate(compass);

        compassGps = new SingleCompassGps(compass);

        Configuration conf = new Configuration().configure("/org/compass/gps/device/hibernate/collection/hibernate.cfg.xml")
                .setProperty(Environment.HBM2DDL_AUTO, "create");
        sessionFactory = conf.buildSessionFactory();

        HibernateGpsDevice device = new HibernateGpsDevice();
        device.setSessionFactory(sessionFactory);
        device.setName("hibernateDevice");
        compassGps.addGpsDevice(device);
        compassGps.start();
    }

    @Test
    public void doTestCollection() {

        Session s = sessionFactory.openSession();
        Transaction tx = s.beginTransaction();
        Parent parent = new Parent();
        parent.setValue("value");
        Child child1 = new Child("child1");
        parent.addChild(child1);
        Child child2 = new Child("child2");
        parent.addChild(child2);
        s.save(parent);
        tx.commit();
        s.close();

        parent = compassTemplate.load(Parent.class, parent.getId());
        Assert.assertEquals( parent.getValue(), "value");

        Assert.assertEquals( compassTemplate.find("child1").length(), 1);

        s = sessionFactory.openSession();
        tx = s.beginTransaction();
        parent = (Parent) s.load(Parent.class, parent.getId());
        parent.setValue("newvalue");
        s.flush();
        tx.commit();
        s.close();

        s = sessionFactory.openSession();
        tx = s.beginTransaction();
        parent.setValue("newvalue1");
        s.saveOrUpdate(parent);
        tx.commit();
        s.close();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        sessionFactory.close();
        compassGps.stop();
        compass.close();

        fileHandlerMonitor.verifyNoHandlers();

        jotm.stop();
    }
}