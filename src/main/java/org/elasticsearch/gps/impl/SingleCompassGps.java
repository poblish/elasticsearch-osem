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

package org.elasticsearch.gps.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.compass.core.Compass;
import org.compass.core.CompassCallback;
import org.compass.core.CompassException;
import org.compass.core.CompassTemplate;
import org.compass.core.config.CompassSettings;
import org.compass.core.events.RebuildEventListener;
import org.compass.core.mapping.Cascade;
import org.compass.core.spi.InternalCompass;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.gps.CompassGpsException;
import org.elasticsearch.gps.IndexPlan;

/**
 * <p>A {@link org.compass.gps.CompassGps} implementation that holds a
 * single <code>Compass</code> instance. The <code>Compass</code> instance
 * is used for both the index operation and the mirror operation.
 *
 * @author kimchy
 */
public class SingleCompassGps extends AbstractCompassGps {

    protected ESLogger log = Loggers.getLogger( getClass() );

    private Compass compass;

    private CompassTemplate compassTemplate;

    private volatile Compass indexCompass;

//  private volatile CompassTemplate indexCompassTemplate;

    private Map<String, Object> indexSettings;

    private CompassSettings indexCompassSettings;

    public SingleCompassGps() {

    }

    public SingleCompassGps(Compass compass) {
        this.compass = compass;
    }

    protected void doStart() throws CompassGpsException {
        if (compass == null) {
            throw new IllegalArgumentException("Must set the compass property");
        }
        indexCompassSettings = new CompassSettings();
        if (indexSettings != null) {
            indexCompassSettings.addSettings(indexSettings);
        }

/* (AGR_OSEM)
        if (indexCompassSettings.getSetting(CompassEnvironment.CONNECTION_SUB_CONTEXT) == null) {
            indexCompassSettings.setSetting(CompassEnvironment.CONNECTION_SUB_CONTEXT, "gpsindex");
        }
        if (indexCompassSettings.getSetting(LuceneEnvironment.LocalCache.DISABLE_LOCAL_CACHE) == null) {
            indexCompassSettings.setBooleanSetting(LuceneEnvironment.LocalCache.DISABLE_LOCAL_CACHE, true);
        }
        // indexing relies on thread local binding of local transactions
        if (indexCompassSettings.getSetting(CompassEnvironment.Transaction.DISABLE_THREAD_BOUND_LOCAL_TRANSATION) == null) {
            indexCompassSettings.setBooleanSetting(CompassEnvironment.Transaction.DISABLE_THREAD_BOUND_LOCAL_TRANSATION, false);
        }
        if (indexCompassSettings.getSetting(CompassEnvironment.Cascade.DISABLE) == null) {
            indexCompassSettings.setBooleanSetting(CompassEnvironment.Cascade.DISABLE, true);
        }
        if (indexCompassSettings.getSetting(LuceneEnvironment.Transaction.Processor.TYPE) == null) {
            indexCompassSettings.setSetting(LuceneEnvironment.Transaction.Processor.TYPE, LuceneEnvironment.Transaction.Processor.Lucene.NAME);
        }
        // no need to async load the cache
        indexCompassSettings.setBooleanSetting(LuceneEnvironment.SearchEngineIndex.CACHE_ASYNC_INVALIDATION, false);
        indexCompassSettings.setBooleanSetting(CompassEnvironment.Transaction.DISABLE_AUTO_JOIN_SESSION, true);
*/
        this.compassTemplate = new CompassTemplate(compass);

        // add a rebuild listener
        ((InternalCompass) compass).addRebuildEventListener(new RebuildEventListener() {
            public void onCompassRebuild(Compass compass) {
                if (log.isDebugEnabled()) {
                    log.debug("Rebuild detected, restarting");
                }
                refresh();
            }
        });
    }

    protected void doStop() throws CompassGpsException {
    }

    protected void doIndex( final IndexPlan inIndexPlan) throws CompassGpsException
    {
	final IndicesAdminClient		theClient = compass.getClient().admin().indices();

	log.info("doIndex(): IndexPlan = " + inIndexPlan);

	if ( inIndexPlan.getTypes() == null)
	{
		// Just delete all... actually *don't*, cos we need to recreate existing ones, and we don't have a way of doing that right now...

		// theClient.prepareDelete("_all").execute().actionGet();
	}
	else
	{
		// Get index names...

		final Collection<String>		theTypes = new ArrayList<String>();

		for ( Class eachClazz : inIndexPlan.getTypes())
		{
			theTypes.add( compass.getObjectContext().getType(eachClazz) );
		}

		log.info("Index types: " + theTypes);

		// Delete all...

		final String[]	theIndexesArray = theTypes.toArray( new String[ theTypes.size()]);

		log.info("Deleting indices: " + Arrays.toString(theIndexesArray));

		compass.getClient().admin().indices().prepareDelete(theIndexesArray).execute().actionGet();

		// (Re-)create all...

		log.info("CREATING indices: " + Arrays.toString(theIndexesArray));

		for ( String eachIndex : theIndexesArray)    // (AGR) Should we batch?
		{
			if ( inIndexPlan.getSettings() != null)
			{
				theClient.create( new CreateIndexRequest( eachIndex, inIndexPlan.getSettings())).actionGet();
			}
			else
			{
				theClient.create( new CreateIndexRequest(eachIndex)).actionGet();
			}
		}
	}

	new DefaultReplaceIndexCallback(devices.values(), inIndexPlan).buildIndexIfNeeded();

/* (AGR_OSEM)
        ((InternalCompass) compass).stop();

        // create the temp compass index, and clean it
        indexCompass = compass.clone(indexCompassSettings);
        indexCompass.getSearchEngineIndexManager().cleanIndex();
        indexCompassTemplate = new CompassTemplate(indexCompass);

        indexCompass.getSearchEngineIndexManager().clearCache();
        compass.getSearchEngineIndexManager().replaceIndex(indexCompass.getSearchEngineIndexManager(),
                new DefaultReplaceIndexCallback(devices.values(), indexPlan));
        indexCompass.getSearchEngineIndexManager().clearCache();
        try {
            indexCompass.getSearchEngineIndexManager().deleteIndex();
        } catch (CompassException e) {
            log.debug("Failed to delete gps index after indexing, ignoring", e);
        }
        indexCompass.close();
        indexCompass = null;
        indexCompassTemplate = null;

        ((InternalCompass) compass).start();

        if (compass.getSpellCheckManager() != null) {
            log.info("Rebulding spell check index ...");
            try {
                compass.getSpellCheckManager().concurrentRebuild();
                log.info("Spell check index rebuilt");
            } catch (Exception e) {
                log.info("Spell check index failed, will rebuilt it next time", e);
            }
        }
*/
    }

    public void executeForIndex( final CompassCallback callback) throws CompassException
    {
/* (AGR_OSEM)
        if (indexCompassTemplate == null) {
            throw new IllegalStateException("executeForIndex is called outside of an index operation");
        }
        indexCompassTemplate.execute(callback);
*/
	new CompassTemplate(compass).execute(callback);
    }

    public void executeForMirror( CompassCallback callback) throws CompassException
    {
	compassTemplate.execute(callback);
    }

    public boolean hasMappingForEntityForIndex(Class clazz) throws CompassException
    {
	final String	theMapping = compass.getObjectContext().getType(clazz);

	return ( theMapping != null);
    }

    public boolean hasMappingForEntityForIndex(String inClassName) throws CompassException {
	try {
		return hasMappingForEntityForIndex( Class.forName(inClassName) );
	}
	catch (ClassNotFoundException ex) {
		return !inClassName.contains(".");	// If no . assume it's already an index name (!), else it really is a class we know nothing about
	}
    }

    public boolean hasMappingForEntityForMirror(Class clazz, Cascade cascade) throws CompassException {
        return hasMappingForEntity(clazz, compass, cascade);
    }

    public boolean hasMappingForEntityForMirror(String name, Cascade cascade) throws CompassException {
        return hasMappingForEntity(name, compass, cascade);
    }

/* (AGR_OSEM)
    public ResourceMapping getMappingForEntityForIndex(String name) throws CompassException {
        return getRootMappingForEntity(name, getIndexCompass());
    }

    public ResourceMapping getMappingForEntityForIndex(Class clazz) throws CompassException {
        return getRootMappingForEntity(clazz, getIndexCompass());
    }
*/
    public Compass getIndexCompass() {
        if (indexCompass == null) {
            return compass;
        }
        return indexCompass;
    }

    public Compass getMirrorCompass() {
        return compass;
    }

    /**
     * Sets the compass instance that will be used with this Gps implementation.
     * It will be used directly for mirror operations, and will be cloned
     * (optionally adding the {@link #setIndexSettings(java.util.Properties)}
     * for index operations.
     */
    public void setCompass(Compass compass) {
        this.compass = compass;
    }

    /**
     * Sets the additional cloned compass index settings. The settings can
     * override existing settings used to create the Compass instance. Can be
     * used to define different connection string for example.
     */
    public void setIndexSettings(Properties indexSettings) {
        if (this.indexSettings == null) {
            this.indexSettings = new HashMap<String, Object>();
        }
        for (Map.Entry entry : indexSettings.entrySet()) {
            this.indexSettings.put((String) entry.getKey(), entry.getValue());
        }
    }

    /**
     * Sets the additional cloned compass index settings. The settings can
     * override existing settings used to create the Compass instance. Can be
     * used to define different connection string for example.
     */
    public void setIndexSettings(Map<String, Object> indexSettings) {
        if (this.indexSettings == null) {
            this.indexSettings = new HashMap<String, Object>();
        }
        this.indexSettings.putAll(indexSettings);
    }

    /**
     * Sets the additional cloned compass index settings. The settings can
     * override existing settings used to create the Compass instance. Can be
     * used to define different connection string for example.
     */
    public void setIndexProperties(Properties indexSettings) {
        setIndexSettings(indexSettings);
    }

    /**
     * Sets the additional cloned compass index settings. The settings can
     * override existing settings used to create the Compass instance. Can be
     * used to define different connection string for example.
     */
    public void setIndexSettings(CompassSettings indexSettings) {
        if (this.indexSettings == null) {
            this.indexSettings = new HashMap<String, Object>();
        }
        this.indexSettings.putAll(indexSettings.getUnderlyingMap());
    }
}
