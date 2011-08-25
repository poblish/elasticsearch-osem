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

package org.compass.gps.impl;

import java.util.Properties;

import junit.framework.TestCase;
import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.gps.device.MockIndexGpsDevice;
import org.compass.gps.device.MockIndexGpsDeviceObject;
import org.elasticsearch.gps.impl.SingleCompassGps;

/**
 * @author kimchy
 */
public class SingleCompassGpsIndexTests extends TestCase {

    private Compass compass;

    private SingleCompassGps compassGps;

    private MockIndexGpsDevice device;

    public void testSimpleIndex()
    {
        CompassConfiguration conf = new CompassConfiguration();
        // (AGR_OSEM) ... conf.setSetting(CompassEnvironment.CONNECTION, "target/test-index");
        // (AGR_OSEM) ... conf.addClass(MockIndexGpsDeviceObject.class);
        conf.getSettings().setBooleanSetting(CompassEnvironment.DEBUG, true);
        compass = conf.buildCompass();

        device = new MockIndexGpsDevice();
        device.setName("test");
        compassGps = new SingleCompassGps(compass);
        compassGps.addGpsDevice(device);
        compassGps.start();

        // (AGR_OSEM) ... compass.getSearchEngineIndexManager().deleteIndex();
        // (AGR_OSEM) ... compass.getSearchEngineIndexManager().createIndex();

        assertNoObjects();

        device.add(new Long(1), "testvalue");
        compassGps.index();

        assertExists(new Long(1));
        
        compassGps.stop();
        compass.close();
    }

    public void testWithPropertiesForSingleCompassGps()
    {
        CompassConfiguration conf = new CompassConfiguration();
        // (AGR_OSEM) ... conf.setSetting(CompassEnvironment.CONNECTION, "target/test-index");
        // (AGR_OSEM) ... conf.addClass(MockIndexGpsDeviceObject.class);
        conf.getSettings().setBooleanSetting(CompassEnvironment.DEBUG, true);

        compass = conf.buildCompass();
        // (AGR_OSEM) ... compass.getSearchEngineIndexManager().deleteIndex();
        // (AGR_OSEM) ... compass.getSearchEngineIndexManager().createIndex();

        device = new MockIndexGpsDevice();
        device.setName("test");
        compassGps = new SingleCompassGps(compass);
        compassGps.addGpsDevice(device);
        Properties props = new Properties();
        props.setProperty(LuceneEnvironment.SearchEngineIndex.MAX_BUFFERED_DOCS, "100");
        compassGps.setIndexSettings(props);
        compassGps.start();

        assertNoObjects();

        device.add(new Long(1), "testvalue");
        compassGps.index();

        assertExists(new Long(1));
        
        compassGps.stop();
        compass.close();
    }

    private void assertExists(Long id) {
        CompassSession session = compass.openSession();
        // (AGR_OSEM) ... CompassTransaction tr = session.beginTransaction();
        session.load(MockIndexGpsDeviceObject.class, id);
        // (AGR_OSEM) ... tr.commit();
        session.close();
    }

    private void assertNoObjects() {
        CompassSession session = compass.openSession();
        // (AGR_OSEM) ... CompassTransaction tr = session.beginTransaction();
        // (AGR_OSEM) ... assertEquals(0, session.queryBuilder().matchAll().hits().length());
        // (AGR_OSEM) ... tr.commit();
        session.close();
    }
}
