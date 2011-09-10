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

import org.elasticsearch.gps.device.hibernate.HibernateGpsDevice;
import org.elasticsearch.gps.device.hibernate.HibernateGpsDeviceException;
import org.hibernate.SessionFactory;
import org.hibernate.event.service.spi.EventListenerGroup;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.internal.SessionFactoryImpl;

/**
 * Injects lifecycle listeners directly into Hibernate for mirroring operations.
 *
 * <p>By default, registers with plain insert/update/delete listeners, which will be triggered
 * by Hibernate before committing (and up to Hibernate flushing logic). Also allows to be created
 * with setting the <code>registerPostCommitListeneres</code> to <code>true</code> which will cause
 * the insert/update/delete listeneres to be registered as post commit events.
 *
 * @author kimchy
 */
public class DefaultHibernateEntityLifecycleInjector implements HibernateEntityLifecycleInjector {

    protected boolean registerPostCommitListeneres = false;

    protected boolean marshallIds = false;

    protected boolean pendingCascades = true;

    protected boolean processCollection = true;

    public DefaultHibernateEntityLifecycleInjector() {
        this(false);
    }

    /**
     * Creates a new lifecycle injector. Allows to control if the insert/update/delete
     * even listeners will be registered with post commit listeres (flag it <code>true</code>)
     * or with plain post events (triggered based on Hibrenate flushing logic).
     *
     * @param registerPostCommitListeneres <code>true</code> if post commit listeners will be
     *                                     registered. <code>false</code> for plain listeners.
     */
    public DefaultHibernateEntityLifecycleInjector(boolean registerPostCommitListeneres) {
        this.registerPostCommitListeneres = registerPostCommitListeneres;
    }

    /**
     * Should the listener try and marshall ids for the event listener of post insert. Some
     * Hibernate versions won't put the generated ids in the object that is inserted. Defaults
     * to <code>false</code>.
     */
    public void setMarshallIds(boolean marshallIds) {
        this.marshallIds = marshallIds;
    }

    /**
     * Should the listener try and handle pending cascades avoiding trying to save/update relationships in Compass
     * before they were processed by Hibernate. Default to <code>true<code>.
     *
     * <p>Note, if set, might cause Compass event processing to be a *tad* slower.
     */
    public void setPendingCascades(boolean pendingCascades) {
        this.pendingCascades = pendingCascades;
    }

    /**
     * Should the event listener automatically set the processed flag on collections that are created as a result
     * of the marshalling process of Compass. Defaults to <code>true</code>.
     */
    public void setProcessCollection(boolean processCollection) {
        this.processCollection = processCollection;
    }

    public void injectLifecycle(SessionFactory sessionFactory, HibernateGpsDevice device) throws HibernateGpsDeviceException {

        SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) sessionFactory;
	// (AGR_OSEM) Hib4 ... EventListeners eventListeners = sessionFactoryImpl.getEventListeners();

	EventListenerRegistry	theRegistry = null;	// (AGR_OSEM) Hib4

        Object hibernateEventListener = doCreateListener(device);

        if (hibernateEventListener instanceof PostInsertEventListener) {
            EventListenerGroup<PostInsertEventListener> postInsertEventListeners;
            if (registerPostCommitListeneres) {
                postInsertEventListeners = theRegistry.getEventListenerGroup( EventType.POST_COMMIT_INSERT );
            } else {
                postInsertEventListeners = theRegistry.getEventListenerGroup( EventType.POST_INSERT );
            }
            PostInsertEventListener[] tempPostInsertEventListeners = new PostInsertEventListener[postInsertEventListeners.count() + 1];
            System.arraycopy(postInsertEventListeners, 0, tempPostInsertEventListeners, 0, postInsertEventListeners.count());
            tempPostInsertEventListeners[postInsertEventListeners.count()] = (PostInsertEventListener) hibernateEventListener;
            if (registerPostCommitListeneres) {
                // (AGR_OSEM) Hib4 ... eventListeners.setPostCommitInsertEventListeners(tempPostInsertEventListeners);
            } else {
                // (AGR_OSEM) Hib4 ... eventListeners.setPostInsertEventListeners(tempPostInsertEventListeners);
            }
        }

        if (hibernateEventListener instanceof PostUpdateEventListener) {
	    EventListenerGroup<PostUpdateEventListener> postUpdateEventListeners;
            if (registerPostCommitListeneres) {
                postUpdateEventListeners = theRegistry.getEventListenerGroup( EventType.POST_COMMIT_UPDATE );
            } else {
                postUpdateEventListeners = theRegistry.getEventListenerGroup( EventType.POST_UPDATE );
            }
            PostUpdateEventListener[] tempPostUpdateEventListeners = new PostUpdateEventListener[postUpdateEventListeners.count() + 1];
            System.arraycopy(postUpdateEventListeners, 0, tempPostUpdateEventListeners, 0, postUpdateEventListeners.count());
            tempPostUpdateEventListeners[postUpdateEventListeners.count()] = (PostUpdateEventListener) hibernateEventListener;
            if (registerPostCommitListeneres) {
                // (AGR_OSEM) Hib4 ... eventListeners.setPostCommitUpdateEventListeners(tempPostUpdateEventListeners);
            } else {
                // (AGR_OSEM) Hib4 ... eventListeners.setPostUpdateEventListeners(tempPostUpdateEventListeners);
            }
        }

        if (hibernateEventListener instanceof PostDeleteEventListener) {
            EventListenerGroup<PostDeleteEventListener> postDeleteEventListeners;
            if (registerPostCommitListeneres) {
                postDeleteEventListeners = theRegistry.getEventListenerGroup( EventType.POST_COMMIT_DELETE );
            } else {
                postDeleteEventListeners = theRegistry.getEventListenerGroup( EventType.POST_DELETE );
            }
            PostDeleteEventListener[] tempPostDeleteEventListeners = new PostDeleteEventListener[postDeleteEventListeners.count() + 1];
            System.arraycopy(postDeleteEventListeners, 0, tempPostDeleteEventListeners, 0, postDeleteEventListeners.count());
            tempPostDeleteEventListeners[postDeleteEventListeners.count()] = (PostDeleteEventListener) hibernateEventListener;
            if (registerPostCommitListeneres) {
                // (AGR_OSEM) Hib4 ... eventListeners.setPostCommitDeleteEventListeners(tempPostDeleteEventListeners);
            } else {
                // (AGR_OSEM) Hib4 ... eventListeners.setPostDeleteEventListeners(tempPostDeleteEventListeners);
            }
        }
    }

    public void removeLifecycle(SessionFactory sessionFactory, HibernateGpsDevice device) throws HibernateGpsDeviceException {

        // (AGR_OSEM) Hib4 ... SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) sessionFactory;
        // (AGR_OSEM) Hib4 ... EventListeners eventListeners = sessionFactoryImpl.getEventListeners();

	EventListenerRegistry	theRegistry = null;	// (AGR_OSEM) Hib4

        EventListenerGroup<PostInsertEventListener> postInsertEventListeners;

        if (registerPostCommitListeneres) {
            postInsertEventListeners = theRegistry.getEventListenerGroup( EventType.POST_COMMIT_INSERT );
        } else {
            postInsertEventListeners = theRegistry.getEventListenerGroup( EventType.POST_INSERT );
        }

	ArrayList<PostInsertEventListener> tempPostInsertEventListeners = new ArrayList<PostInsertEventListener>();

        for ( PostInsertEventListener postInsertEventListener : postInsertEventListeners.listeners()) {
            if (!(postInsertEventListener instanceof HibernateEventListener)) {
                tempPostInsertEventListeners.add(postInsertEventListener);
            }
        }
        if (registerPostCommitListeneres) {
            // (AGR_OSEM) Hib4 ... eventListeners.setPostCommitInsertEventListeners((PostInsertEventListener[]) tempPostInsertEventListeners.toArray(new PostInsertEventListener[tempPostInsertEventListeners.size()]));
        } else {
            // (AGR_OSEM) Hib4 ... eventListeners.setPostInsertEventListeners((PostInsertEventListener[]) tempPostInsertEventListeners.toArray(new PostInsertEventListener[tempPostInsertEventListeners.size()]));
        }

        EventListenerGroup<PostUpdateEventListener> postUpdateEventListeners;

        if (registerPostCommitListeneres) {
            postUpdateEventListeners = theRegistry.getEventListenerGroup( EventType.POST_COMMIT_UPDATE );
        } else {
            postUpdateEventListeners = theRegistry.getEventListenerGroup( EventType.POST_UPDATE );
        }

        ArrayList<PostUpdateEventListener> tempPostUpdateEventListeners = new ArrayList<PostUpdateEventListener>();

        for ( PostUpdateEventListener postUpdateEventListener : postUpdateEventListeners.listeners()) {
            if (!(postUpdateEventListener instanceof HibernateEventListener)) {
                tempPostUpdateEventListeners.add(postUpdateEventListener);
            }
        }
        if (registerPostCommitListeneres) {
            // (AGR_OSEM) Hib4 ... eventListeners.setPostCommitUpdateEventListeners((PostUpdateEventListener[]) tempPostUpdateEventListeners.toArray(new PostUpdateEventListener[tempPostUpdateEventListeners.size()]));
        } else {
            // (AGR_OSEM) Hib4 ... eventListeners.setPostUpdateEventListeners((PostUpdateEventListener[]) tempPostUpdateEventListeners.toArray(new PostUpdateEventListener[tempPostUpdateEventListeners.size()]));
        }

        EventListenerGroup<PostDeleteEventListener> postDeleteEventListeners;

        if (registerPostCommitListeneres) {
            postDeleteEventListeners = theRegistry.getEventListenerGroup( EventType.POST_COMMIT_DELETE );
        } else {
            postDeleteEventListeners = theRegistry.getEventListenerGroup( EventType.POST_DELETE );
        }

        ArrayList<PostDeleteEventListener> tempPostDeleteEventListeners = new ArrayList<PostDeleteEventListener>();

        for ( PostDeleteEventListener postDeleteEventListener : postDeleteEventListeners.listeners()) {
            if (!(postDeleteEventListener instanceof HibernateEventListener)) {
                tempPostDeleteEventListeners.add(postDeleteEventListener);
            }
        }
        if (registerPostCommitListeneres) {
            // (AGR_OSEM) Hib4 ... eventListeners.setPostCommitDeleteEventListeners((PostDeleteEventListener[]) tempPostDeleteEventListeners.toArray(new PostDeleteEventListener[tempPostDeleteEventListeners.size()]));
        } else {
            // (AGR_OSEM) Hib4 ... eventListeners.setPostDeleteEventListeners((PostDeleteEventListener[]) tempPostDeleteEventListeners.toArray(new PostDeleteEventListener[tempPostDeleteEventListeners.size()]));
        }
    }

    protected Object doCreateListener(HibernateGpsDevice device) {
        return new HibernateEventListener(device, marshallIds, pendingCascades, processCollection);
    }
}