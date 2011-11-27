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

package org.compass.gps.device.jpa;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import junit.framework.TestCase;
import org.compass.core.Compass;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.util.FileHandlerMonitor;
import org.compass.gps.device.jpa.model.Simple;
import org.compass.gps.device.jpa.model.SimpleBase;
import org.compass.gps.device.jpa.model.SimpleExtend;
import org.elasticsearch.gps.CompassGps;
import org.elasticsearch.gps.device.jpa.JpaGpsDevice;
import org.elasticsearch.gps.impl.SingleCompassGps;
import org.elasticsearch.osem.core.ObjectContextFactory;
import org.elasticsearch.test.ElasticSearchTests;

/**
 * @author kimchy
 */
public abstract class AbstractJpaGpsDeviceTests extends TestCase {

    protected EntityManagerFactory entityManagerFactory;

    protected Compass compass;

    protected CompassGps compassGps;

    private FileHandlerMonitor fileHandlerMonitor;

    @Override
    protected void setUp() throws Exception {
        entityManagerFactory = doSetUpEntityManagerFactory();
        setUpCompass();

        fileHandlerMonitor = FileHandlerMonitor.getFileHandlerMonitor(compass);
        fileHandlerMonitor.verifyNoHandlers();

        ElasticSearchTests.deleteAllIndexes(compass);
        ElasticSearchTests.verifyAllIndexes(compass);

        ElasticSearchTests.populateContextAndIndices( compass, SimpleBase.class, Simple.class, SimpleExtend.class);
        
        setUpGps();
        compassGps.start();
        setUpDB();
    }

    @Override
    protected void tearDown() throws Exception {
        tearDownDB();
        compassGps.stop();
        compass.close();
        entityManagerFactory.close();

        fileHandlerMonitor.verifyNoHandlers();
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

    protected abstract EntityManagerFactory doSetUpEntityManagerFactory();

    protected abstract void setUpCoreCompass(CompassConfiguration conf);

    protected void setUpCompass() throws Exception {

        // (AGR_OSEM) ... CompassConfiguration cpConf = new CompassAnnotationsConfiguration().setConnection("target/test-index");
        // (AGR_OSEM) ... cpConf.getSettings().setBooleanSetting(CompassEnvironment.DEBUG, true);
        // (AGR_OSEM) ... cpConf.getSettings().setBooleanSetting(LuceneEnvironment.Optimizer.SCHEDULE, false);
        File testPropsFile = new File("compass.test.properties");
        if (testPropsFile.exists()) {
            Properties testProps = new Properties();
            testProps.load(new FileInputStream(testPropsFile));
            // (AGR_OSEM) ... cpConf.getSettings().addSettings(testProps);
        }

	// (AGR_OSEM) ...  setUpCoreCompass(cpConf);

	compass = ElasticSearchTests.mockSimpleCompass( "10.10.10.101", ObjectContextFactory.create());
    }

    protected void setUpGps() {
        compassGps = new SingleCompassGps(compass);
        setUpGpsDevice();
    }

    protected void setUpGpsDevice() {
        JpaGpsDevice jpaGpsDevice = new JpaGpsDevice();
        jpaGpsDevice.setName("jdoDevice");
        jpaGpsDevice.setEntityManagerFactory(entityManagerFactory);
        addDeviceSettings(jpaGpsDevice);
        compassGps.addGpsDevice(jpaGpsDevice);
    }

    protected void addDeviceSettings(JpaGpsDevice device) {

    }

    protected void setUpDB() throws Exception {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();
        setUpDB(entityManager);
        entityTransaction.commit();
        entityManager.close();
    }

    protected void setUpDB(EntityManager entityManager) throws Exception {
    }

    protected void tearDownDB() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();
        tearDownDB(entityManager);
        entityTransaction.commit();
        entityManager.close();
    }

    protected void tearDownDB(EntityManager entityManager) {
    }
}
