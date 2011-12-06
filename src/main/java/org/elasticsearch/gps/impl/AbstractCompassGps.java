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

import java.util.Arrays;
import java.util.HashMap;

import org.compass.core.Compass;
import org.compass.core.mapping.Cascade;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.gps.CompassGpsDevice;
import org.elasticsearch.gps.CompassGpsException;
import org.elasticsearch.gps.DefaultIndexPlan;
import org.elasticsearch.gps.IndexPlan;
import org.elasticsearch.gps.spi.CompassGpsInterfaceDevice;

/**
 * A simple base class for {@link org.compass.gps.CompassGps}
 * implementations.
 *
 * @author kimchy
 */
public abstract class AbstractCompassGps implements CompassGpsInterfaceDevice {

    protected ESLogger log = Loggers.getLogger( getClass() );

    protected HashMap<String, CompassGpsDevice> devices = new HashMap<String, CompassGpsDevice>();

    private volatile boolean started = false;

    private volatile boolean performingIndexOperation = false;

    public void addGpsDevice(CompassGpsDevice gpsDevice) {
        checkDeviceValidity(gpsDevice);
        gpsDevice.injectGps(this);
        devices.put(gpsDevice.getName(), gpsDevice);
    }

    public void setGpsDevices(CompassGpsDevice[] devices) {
        this.devices.clear();
        for (CompassGpsDevice device : devices) {
            checkDeviceValidity(device);
            device.injectGps(this);
            this.devices.put(device.getName(), device);
        }
    }

    protected CompassGpsDevice getGpsDevice(String name) {
        return devices.get(name);
    }

    private void checkDeviceValidity(CompassGpsDevice device) {
        if (device.getName() == null) {
            throw new IllegalArgumentException("Must specify a name for a gps device");
        }
        if (devices.get(device.getName()) != null) {
            throw new IllegalArgumentException("A gps device with the name [" + device.getName()
                    + "] is defined twice. It is not allowed.");
        }
    }

    protected boolean hasMappingForEntity(Class clazz, Compass checkedCompass, Cascade cascade)
    {
	final String	theTypeStr = checkedCompass.getObjectContext().getType(clazz);

	// log.info("*** " + s + " for class: " + clazz + " / " + Arrays.toString( checkedCompass.getObjectContext().getTypes() ));

	// return ((InternalCompass) checkedCompass).getMapping().hasMappingForClass(clazz, cascade);

	return ( theTypeStr != null);
    }

    protected boolean hasMappingForEntity(String name, Compass checkedCompass, Cascade cascade)
    {
	final String	s = "???";	// checkedCompass.getObjectContext().getType(name);

	log.info("*** " + s + " for '" + name + "' / " + Arrays.toString( checkedCompass.getObjectContext().getTypes() ));;

        // (AGR_OSEM) ... return ((InternalCompass) checkedCompass).getMapping().hasMappingForAlias(name, cascade);
	    return false;
    }

    public synchronized void index() throws CompassGpsException, IllegalStateException {
        index(new DefaultIndexPlan());
    }

    public void index(Class... types) throws CompassGpsException, IllegalStateException {
        index(new DefaultIndexPlan().setTypes(types));
    }

    public void index(String... aliases) throws CompassGpsException, IllegalStateException {
        index(new DefaultIndexPlan().setAliases(aliases));
    }

    public synchronized void index( final Settings inEsSettings) throws CompassGpsException, IllegalStateException {
        index(new DefaultIndexPlan(inEsSettings));
    }

    public void index( final Settings inEsSettings, Class... types) throws CompassGpsException, IllegalStateException {
        index(new DefaultIndexPlan(inEsSettings).setTypes(types));
    }

    public void index( final Settings inEsSettings, String... aliases) throws CompassGpsException, IllegalStateException {
        index(new DefaultIndexPlan(inEsSettings).setAliases(aliases));
    }

    public synchronized void index(IndexPlan indexPlan) throws CompassGpsException, IllegalStateException {
        if (!isRunning()) {
            throw new IllegalStateException("CompassGps must be running in order to perform the index operation");
        }

/* (AGR_OSEM)
        if (((InternalCompass) getMirrorCompass()).getTransactionFactory().getTransactionBoundSession() != null) {
            throw new CompassGpsException("index() operation is not allowed to be called within a transaction (mirror)");
        }
        if (((InternalCompass) getIndexCompass()).getTransactionFactory().getTransactionBoundSession() != null) {
            throw new CompassGpsException("index() operation is not allowed to be called within a transaction (index)");
        }
 */
        if (isPerformingIndexOperation()) {
            throw new IllegalArgumentException("Indexing alredy in process, not allowed to call index()");
        }
        try {
            performingIndexOperation = true;
            doIndex(indexPlan);
        }
	catch (Throwable t) {
		throw new CompassGpsException( "Error", t);
	}
	finally {
            performingIndexOperation = false;
        }
    }

    protected abstract void doIndex(IndexPlan indexPlan) throws CompassGpsException;

    public synchronized void start() throws CompassGpsException {
        doStart();
        if (!started) {
            for (CompassGpsDevice device : devices.values()) {
                device.start();
            }
            started = true;
        }
    }

    protected abstract void doStart() throws CompassGpsException;

    protected abstract void doStop() throws CompassGpsException;

    public synchronized void stop() throws CompassGpsException {
        if (started) {
            for (CompassGpsDevice device : devices.values()) {
                device.stop();
            }
            started = false;
        }
        doStop();
    }

    public synchronized void refresh() throws CompassGpsException {
        for (CompassGpsDevice device : devices.values()) {
            device.refresh();
        }
    }

    public boolean isRunning() {
        return started;
    }

    public boolean isPerformingIndexOperation() {
        return performingIndexOperation;
    }

    protected void finalize() throws Throwable {
        super.finalize();
        stop();
    }
}
