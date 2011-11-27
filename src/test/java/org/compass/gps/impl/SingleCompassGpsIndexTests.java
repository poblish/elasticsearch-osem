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

import org.elasticsearch.osem.test.entities.impl.TestArticle;
import org.elasticsearch.osem.test.entities.impl.Actor;
import java.util.Properties;

import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.gps.device.MockIndexGpsDevice;
import org.compass.gps.device.MockIndexGpsDeviceObject;
import org.elasticsearch.client.Client;
import org.elasticsearch.gps.impl.SingleCompassGps;
import org.elasticsearch.osem.core.ObjectContext;
import org.elasticsearch.osem.core.ObjectContextFactory;
import org.elasticsearch.test.ElasticSearchTests;
import org.testng.Assert;
import org.testng.annotations.Test;
import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * @author kimchy
 */
public class SingleCompassGpsIndexTests {

    private Compass compass;

    private SingleCompassGps compassGps;

    private MockIndexGpsDevice device;

    @Test
    public void doTestSimpleIndex()
    {
	final ObjectContext	theObjectContext = ObjectContextFactory.create();

//        CompassConfiguration conf = new CompassConfiguration();
//        conf.setSetting(CompassEnvironment.CONNECTION, "target/test-index");
//        conf.addClass(MockIndexGpsDeviceObject.class);
//        conf.getSettings().setBooleanSetting(CompassEnvironment.DEBUG, true);
//        compass = conf.buildCompass();

	compass = ElasticSearchTests.mockSimpleCompass( "10.10.10.101", theObjectContext);

        ElasticSearchTests.deleteAllIndexes(compass);
        ElasticSearchTests.verifyAllIndexes(compass);

	ElasticSearchTests.populateContextAndIndices( compass, MockIndexGpsDeviceObject.class);

        device = new MockIndexGpsDevice();
        device.setName("test");
        compassGps = new SingleCompassGps(compass);
        compassGps.addGpsDevice(device);
        compassGps.start();

        // (AGR_OSEM) ... compass.getSearchEngineIndexManager().deleteIndex();
        // (AGR_OSEM) ... compass.getSearchEngineIndexManager().createIndex();

        assertNoObjects( compass.getClient() );

        device.add(new Long(1), "testvalue");
        compassGps.index();

        assertExists(new Long(1));
        
        compassGps.stop();
        compass.close();
    }

    @Test
    public void doTestWithPropertiesForSingleCompassGps()
    {
//        CompassConfiguration conf = new CompassConfiguration();
//        // (AGR_OSEM) ... conf.setSetting(CompassEnvironment.CONNECTION, "target/test-index");
//        // (AGR_OSEM) ... conf.addClass(MockIndexGpsDeviceObject.class);
//        conf.getSettings().setBooleanSetting(CompassEnvironment.DEBUG, true);

        compass = ElasticSearchTests.mockSimpleCompass( "10.10.10.101", ObjectContextFactory.create());

        ElasticSearchTests.deleteAllIndexes(compass);
        ElasticSearchTests.verifyAllIndexes(compass);

	ElasticSearchTests.populateContextAndIndices( compass, Actor.class, TestArticle.class, MockIndexGpsDeviceObject.class);

        device = new MockIndexGpsDevice();
        device.setName("test");
        compassGps = new SingleCompassGps(compass);
        compassGps.addGpsDevice(device);
        Properties props = new Properties();
        props.setProperty(LuceneEnvironment.SearchEngineIndex.MAX_BUFFERED_DOCS, "100");
        compassGps.setIndexSettings(props);
        compassGps.start();

        assertNoObjects( compass.getClient() );

        device.add(new Long(1), "testvalue");
        compassGps.index();

        assertExists(new Long(1));
        
        compassGps.stop();
        compass.close();
    }

    private void assertExists( Long id) {
        CompassSession session = compass.openSession();
        // (AGR_OSEM) ... CompassTransaction tr = session.beginTransaction();
        session.load(MockIndexGpsDeviceObject.class, id);
        // (AGR_OSEM) ... tr.commit();
        session.close();
    }

    private void assertNoObjects( Client inClient) {
        CompassSession session = compass.openSession();
        // (AGR_OSEM) ... CompassTransaction tr = session.beginTransaction();
	
        Assert.assertEquals( inClient.prepareSearch("_all").setQuery( matchAllQuery() ).execute().actionGet().getHits().getTotalHits(), 0);

        // (AGR_OSEM) ... tr.commit();
        session.close();
    }
}