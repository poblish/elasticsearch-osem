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

package org.elasticsearch.gps.device.hibernate.embedded;

import java.util.Properties;

import org.compass.core.Compass;
import org.compass.core.CompassTemplate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.event.service.spi.EventListenerGroup;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.internal.SessionFactoryImpl;

/**
 * A helper class used when working with embedded Compass within Hibernate. Allows for access to the
 * {@link org.compass.core.Compass} and {@link org.compass.gps.CompassGps} instances.
 *
 * @author kimchy
 */
public abstract class HibernateHelper {

    private HibernateHelper() {
    }

    /**
     * Returns the Compass instance associated with the given Hibernate from the Hibernate session.
     */
    public static Compass getCompass(Session session) {
        return findEventListener(session).getCompass();
    }

    /**
     * Returns a CompassTemplate instance associated with the given Hibernate from the Hibernate session.
     */
    public static CompassTemplate getCompassTempalte(Session session) {
        return new CompassTemplate(findEventListener(session).getCompass());
    }

    /**
     * Returns the Compass instance associated with the given Hibernate from the Hiberante session factory.
     */
    public static Compass getCompass(SessionFactory sessionFactory) {
        return findEventListener(sessionFactory).getCompass();
    }

    /**
     * Returns the CompassTemplate instance associated with the given Hibernate from the Hiberante session factory.
     */
    public static CompassTemplate getCompassTempalte(SessionFactory sessionFactory) {
        return new CompassTemplate(findEventListener(sessionFactory).getCompass());
    }

    /**
     * Returns the settings of the indexing Compass instance (from the CompassGps) associated with Hibernate
     * based on the provided Hibernate session.
     */
    public static Properties getIndexSettings(Session session) {
        return findEventListener(session).getIndexSettings();
    }

    /**
     * Returns the settings of the indexing Compass instance (from the CompassGps) associated with Hibernate
     * based on the provided Hibernate session factory.
     */
    public static Properties getIndexSettings(SessionFactory sessionFactory) {
        return findEventListener(sessionFactory).getIndexSettings();
    }

    /**
     * Retruns a {@link org.compass.gps.CompassGps} based on the Compass instance associated with the
     * Hibernate session factory.
     */
/* (AGR_OSEM)
    public static CompassGps getCompassGps(SessionFactory sessionFactory) {
        HibernateGpsDevice device = new HibernateGpsDevice("hibernate", sessionFactory);
        return getCompassGps(device);
    }
*/
    /**
     * Returns a {@link org.compass.gps.CompassGps} to wrap the provided Hibernat Gps device.
     */
/* (AGR_OSEM)
    public static CompassGps getCompassGps(HibernateGpsDevice device) {
        SingleCompassGps gps = new SingleCompassGps(getCompass(device.getSessionFactory()));
        device.setMirrorDataChanges(false);
        gps.setIndexProperties(getIndexSettings(device.getSessionFactory()));
        gps.addGpsDevice(device);
        gps.start();
        return gps;
    }
*/
    private static CompassEventListener findEventListener(SessionFactory sessionFactory) {
        if (sessionFactory instanceof SessionFactoryImpl) {
	    EventListenerRegistry	theRegistry = null;	// (AGR_OSEM) Hib4
            return findEventListener( theRegistry.getEventListenerGroup( EventType.POST_INSERT ) );
        } else {
            Session session = sessionFactory.openSession();
            try {
                return findEventListener(session);
            } finally {
                session.close();
            }
        }
    }

    private static CompassEventListener findEventListener(Session session) {
	EventListenerRegistry	theRegistry = null;	// (AGR_OSEM) Hib4
        return findEventListener( theRegistry.getEventListenerGroup( EventType.POST_INSERT ) );
    }

    private static CompassEventListener findEventListener( EventListenerGroup<PostInsertEventListener> listeners) {
        for (PostInsertEventListener candidate : listeners.listeners()) {
            if (candidate instanceof CompassEventListener) {
                return (CompassEventListener) candidate;
            }
        }
        throw new HibernateException(
                "Compass Event listeners not configured, please check the reference documentation and the " +
                        "application's hibernate.cfg.xml");
    }
}
