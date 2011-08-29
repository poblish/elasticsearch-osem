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

package org.compass.gps.device.hibernate.eg;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;
import org.compass.core.Compass;
import org.compass.core.util.FileHandlerMonitor;
import org.elasticsearch.osem.core.ObjectContextFactory;
import org.elasticsearch.test.ElasticSearchTests;
import org.objectweb.jotm.Jotm;

public abstract class AbstractHibernateGpsDeviceTests extends TestCase {

    private Jotm jotm;

    protected Compass mirrorCompass;

    protected Compass indexCompass;

    private FileHandlerMonitor fileHandlerMonitorIndex;
    private FileHandlerMonitor fileHandlerMonitorMirror;

    // (AGR_OSEM) ... protected DualCompassGps compassGps;

    protected void setUp() throws Exception {
        super.setUp();

        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
        System.setProperty(Context.PROVIDER_URL, "rmi://localhost:1099");

        try {
            java.rmi.registry.LocateRegistry.createRegistry(1099);
        } catch (Exception e) {

        }

        jotm = new Jotm(true, true);
        Context ctx = new InitialContext();
        ctx.rebind("java:comp/UserTransaction", jotm.getUserTransaction());

//        CompassConfiguration cpConf = new CompassConfiguration()
//                .configure("/org/compass/gps/device/hibernate/eg/compass-mirror.cfg.xml");
//        cpConf.getSettings().setBooleanSetting(CompassEnvironment.DEBUG, true);

	mirrorCompass = ElasticSearchTests.mockSimpleCompass( "10.10.10.107", ObjectContextFactory.create());

	fileHandlerMonitorMirror = FileHandlerMonitor.getFileHandlerMonitor(mirrorCompass);
        fileHandlerMonitorMirror.verifyNoHandlers();

        ElasticSearchTests.deleteAllIndexes(mirrorCompass);
        ElasticSearchTests.verifyAllIndexes(mirrorCompass);

	ElasticSearchTests.populateContextAndIndices( mirrorCompass, User.class);

//        CompassConfiguration cpBatchConf = new CompassConfiguration()
//                .configure("/org/compass/gps/device/hibernate/eg/compass-index.cfg.xml");
//        cpBatchConf.getSettings().setBooleanSetting(CompassEnvironment.DEBUG, true);
//        indexCompass = cpBatchConf.buildCompass();

	indexCompass = ElasticSearchTests.mockCompass( mirrorCompass.getClient(), ObjectContextFactory.create());

        fileHandlerMonitorIndex = FileHandlerMonitor.getFileHandlerMonitor(indexCompass);
        fileHandlerMonitorIndex.verifyNoHandlers();

        // ElasticSearchTests.deleteAllIndexes(indexCompass);
        // ElasticSearchTests.verifyAllIndexes(indexCompass);

        // (AGR_OSEM) ... compassGps = new DualCompassGps(indexCompass, mirrorCompass);
    }

    protected void tearDown() throws Exception {
        jotm.stop();
        mirrorCompass.close();
        indexCompass.close();

        fileHandlerMonitorMirror.verifyNoHandlers();
        fileHandlerMonitorIndex.verifyNoHandlers();
        super.tearDown();
    }

}
