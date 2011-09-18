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

package org.elasticsearch.gps.device.hibernate.lifecycle;

import java.util.ArrayList;

import java.util.Collection;
import org.elasticsearch.gps.device.hibernate.HibernateGpsDevice;
import org.elasticsearch.gps.device.hibernate.HibernateGpsDeviceException;
import org.hibernate.SessionFactory;
import org.hibernate.event.service.spi.EventListenerGroup;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostCollectionRecreateEventListener;
import org.hibernate.event.spi.PostCollectionRemoveEventListener;
import org.hibernate.event.spi.PostCollectionUpdateEventListener;
import org.hibernate.internal.SessionFactoryImpl;

/**
 * @author kimchy
 */
public class DefaultHibernateEntityCollectionLifecycleInjector extends DefaultHibernateEntityLifecycleInjector {

    private Object eventListener;

    public DefaultHibernateEntityCollectionLifecycleInjector() {
        super();
    }

    public DefaultHibernateEntityCollectionLifecycleInjector(boolean registerPostCommitListeneres) {
        super(registerPostCommitListeneres);
    }

    public void injectLifecycle(SessionFactory sessionFactory, HibernateGpsDevice device) throws HibernateGpsDeviceException {
        super.injectLifecycle(sessionFactory, device);

        SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) sessionFactory;
	// (AGR_OSEM) Hib4 ... EventListeners eventListeners = sessionFactoryImpl.getEventListeners();

	EventListenerRegistry	theRegistry = sessionFactoryImpl.getServiceRegistry().getService( EventListenerRegistry.class );

        if (registerPostCommitListeneres) {
            return;
        }

        if (eventListener instanceof PostCollectionRecreateEventListener) {
	    EventListenerGroup<PostCollectionRecreateEventListener>	theGroup = theRegistry.getEventListenerGroup( EventType.POST_COLLECTION_RECREATE );

	    Collection<PostCollectionRecreateEventListener>	theListeners = new ArrayList<PostCollectionRecreateEventListener>();
	    for ( PostCollectionRecreateEventListener eachListener : theGroup.listeners())
	    {
		    theListeners.add(eachListener);
	    }

            PostCollectionRecreateEventListener[] listeners = theListeners.toArray( new PostCollectionRecreateEventListener[ theListeners.size() ] );
            PostCollectionRecreateEventListener[] tempListeners = new PostCollectionRecreateEventListener[listeners.length + 1];
            System.arraycopy(listeners, 0, tempListeners, 0, listeners.length);
            tempListeners[listeners.length] = (PostCollectionRecreateEventListener) eventListener;
            // (AGR_OSEM) Hib4 ... eventListeners.setPostCollectionRecreateEventListeners(tempListeners);
        }

        if (eventListener instanceof PostCollectionRemoveEventListener) {
	    EventListenerGroup<PostCollectionRemoveEventListener>	theGroup = theRegistry.getEventListenerGroup( EventType.POST_COLLECTION_REMOVE );

	    Collection<PostCollectionRemoveEventListener>	theListeners = new ArrayList<PostCollectionRemoveEventListener>();
	    for ( PostCollectionRemoveEventListener eachListener : theGroup.listeners())
	    {
		    theListeners.add(eachListener);
	    }

            PostCollectionRemoveEventListener[] listeners = theListeners.toArray( new PostCollectionRemoveEventListener[ theListeners.size() ] );
            PostCollectionRemoveEventListener[] tempListeners = new PostCollectionRemoveEventListener[listeners.length + 1];
            System.arraycopy(listeners, 0, tempListeners, 0, listeners.length);
            tempListeners[listeners.length] = (PostCollectionRemoveEventListener) eventListener;
            // (AGR_OSEM) Hib4 ... eventListeners.setPostCollectionRemoveEventListeners(tempListeners);
        }

        if (eventListener instanceof PostCollectionUpdateEventListener) {
	    EventListenerGroup<PostCollectionUpdateEventListener>	theGroup = theRegistry.getEventListenerGroup( EventType.POST_COLLECTION_UPDATE );

	    Collection<PostCollectionUpdateEventListener>	theListeners = new ArrayList<PostCollectionUpdateEventListener>();
	    for ( PostCollectionUpdateEventListener eachListener : theGroup.listeners())
	    {
		    theListeners.add(eachListener);
	    }

            PostCollectionUpdateEventListener[] listeners = theListeners.toArray( new PostCollectionUpdateEventListener[ theListeners.size() ] );
            PostCollectionUpdateEventListener[] tempListeners = new PostCollectionUpdateEventListener[listeners.length + 1];
            System.arraycopy(listeners, 0, tempListeners, 0, listeners.length);
            tempListeners[listeners.length] = (PostCollectionUpdateEventListener) eventListener;
            // (AGR_OSEM) Hib4 ... eventListeners.setPostCollectionUpdateEventListeners(tempListeners);
        }
    }

    public void removeLifecycle(SessionFactory sessionFactory, HibernateGpsDevice device) throws HibernateGpsDeviceException {
        super.removeLifecycle(sessionFactory, device);

        if (registerPostCommitListeneres) {
            return;
        }

        SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) sessionFactory;
	// (AGR_OSEM) Hib4 ... EventListeners eventListeners = sessionFactoryImpl.getEventListeners();

	EventListenerRegistry	theRegistry = sessionFactoryImpl.getServiceRegistry().getService( EventListenerRegistry.class );

/* (AGR_OSEM) Hib4

        PostCollectionRecreateEventListener[] postCollectionRecreateEventListeners = eventListeners.getPostCollectionRecreateEventListeners();
        ArrayList<PostCollectionRecreateEventListener> tempPostCollectionRecreateEventListeners = new ArrayList<PostCollectionRecreateEventListener>();
        for (PostCollectionRecreateEventListener postCollectionRecreateEventListener : postCollectionRecreateEventListeners) {
            if (!(postCollectionRecreateEventListener instanceof HibernateCollectionEventListener)) {
                tempPostCollectionRecreateEventListeners.add(postCollectionRecreateEventListener);
            }
        }

        // (AGR_OSEM) Hib4 ... eventListeners.setPostCollectionRecreateEventListeners(tempPostCollectionRecreateEventListeners.toArray(new PostCollectionRecreateEventListener[tempPostCollectionRecreateEventListeners.size()]));

        PostCollectionUpdateEventListener[] postCollectionUpdateEventListeners = eventListeners.getPostCollectionUpdateEventListeners();
        ArrayList<PostCollectionUpdateEventListener> tempPostCollectionUpdateEventListeners = new ArrayList<PostCollectionUpdateEventListener>();
        for (PostCollectionUpdateEventListener postCollectionUpdateEventListener : postCollectionUpdateEventListeners) {
            if (!(postCollectionUpdateEventListener instanceof HibernateCollectionEventListener)) {
                tempPostCollectionUpdateEventListeners.add(postCollectionUpdateEventListener);
            }
        }

        // (AGR_OSEM) Hib4 ... eventListeners.setPostCollectionUpdateEventListeners(tempPostCollectionUpdateEventListeners.toArray(new PostCollectionUpdateEventListener[tempPostCollectionUpdateEventListeners.size()]));

        PostCollectionRemoveEventListener[] postCollectionRemoveEventListeners = eventListeners.getPostCollectionRemoveEventListeners();
        ArrayList<PostCollectionRemoveEventListener> tempPostCollectionRemoveEventListeners = new ArrayList<PostCollectionRemoveEventListener>();
        for (PostCollectionRemoveEventListener postCollectionRemoveEventListener : postCollectionRemoveEventListeners) {
            if (!(postCollectionRemoveEventListener instanceof HibernateCollectionEventListener)) {
                tempPostCollectionRemoveEventListeners.add(postCollectionRemoveEventListener);
            }
        }
*/
        // (AGR_OSEM) Hib4 ... eventListeners.setPostCollectionRemoveEventListeners(tempPostCollectionRemoveEventListeners.toArray(new PostCollectionRemoveEventListener[tempPostCollectionRemoveEventListeners.size()]));

        eventListener = null;
    }

    protected Object doCreateListener(HibernateGpsDevice device) {
        eventListener = new HibernateCollectionEventListener(device, marshallIds, pendingCascades, processCollection);
        return eventListener;
    }
}
