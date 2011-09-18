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

package org.compass.gps.device.hibernate.simple;

import java.io.IOException;

import junit.framework.TestCase;
import org.compass.core.Compass;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.util.FileHandlerMonitor;
import org.elasticsearch.gps.CompassGps;
import org.elasticsearch.gps.device.hibernate.HibernateGpsDevice;
import org.elasticsearch.gps.impl.SingleCompassGps;
import org.elasticsearch.osem.core.ObjectContextFactory;
import org.elasticsearch.test.ElasticSearchTests;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

/**
 * @author kimchy
 */
public abstract class AbstractHibernateGpsDeviceTests extends TestCase {

    protected SessionFactory sessionFactory;

    protected Compass compass;

    private FileHandlerMonitor fileHandlerMonitor;

    protected HibernateGpsDevice hibernateGpsDevice;

    protected CompassGps compassGps;

    protected void setUp() throws Exception
    {
        sessionFactory = doSetUpSessionFactory();
        setUpCompass();

        fileHandlerMonitor = FileHandlerMonitor.getFileHandlerMonitor(compass);
        fileHandlerMonitor.verifyNoHandlers();

        setUpGps();
        compassGps.start();
        setUpDB();
    }

    protected void tearDown() throws Exception {
        tearDownDB();
        compassGps.stop();
        compass.close();
        
        fileHandlerMonitor.verifyNoHandlers();

        sessionFactory.close();

	ElasticSearchTests.deleteAllIndexes(compass);

/* (AGR_OSEM)
        if (compass.getSpellCheckManager() != null) {
            try {
                compass.getSpellCheckManager().deleteIndex();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } */
    }

    protected void doTearDown() throws Exception {
        
    }

    protected abstract SessionFactory doSetUpSessionFactory();

    protected abstract void setUpCoreCompass(CompassConfiguration conf);

    protected void setUpCompass() throws IOException
    {
//        CompassConfiguration cpConf = null;	// (AGR_OSEM) ... new CompassConfiguration().setConnection("target/test-index");
//
//        File testPropsFile = new File("compass.test.properties");
//        if (testPropsFile.exists()) {
//            Properties testProps = new Properties();
//            testProps.load(new FileInputStream(testPropsFile));
//            cpConf.getSettings().addSettings(testProps);
//        }
//        setUpCoreCompass(cpConf);
//        cpConf.getSettings().setBooleanSetting(CompassEnvironment.DEBUG, true);
//        compass = cpConf.buildCompass();

	compass = ElasticSearchTests.mockSimpleCompass( "10.10.10.103", ObjectContextFactory.create());

        ElasticSearchTests.deleteAllIndexes(compass);
        ElasticSearchTests.verifyAllIndexes(compass);

	ElasticSearchTests.populateContextAndIndices( compass, Simple.class, SimpleBase.class, SimpleExtend.class);
    }

    protected void setUpGps() {
        compassGps = new SingleCompassGps(compass);
        setUpGpsDevice();
    }

    protected void setUpGpsDevice() {
        hibernateGpsDevice = new HibernateGpsDevice();
        hibernateGpsDevice.setName("jdoDevice");
        hibernateGpsDevice.setSessionFactory(sessionFactory);
        addDeviceSettings(hibernateGpsDevice);
        compassGps.addGpsDevice(hibernateGpsDevice);
    }

    protected void addDeviceSettings(HibernateGpsDevice device) {

    }

    protected void setUpDB() {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        setUpDB(session);
        transaction.commit();
        session.close();
    }

    protected void setUpDB(Session session) {
    }

    protected void tearDownDB() {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        tearDownDB(session);
        transaction.commit();
        session.close();
    }

    protected void tearDownDB(Session session) {
    }
}