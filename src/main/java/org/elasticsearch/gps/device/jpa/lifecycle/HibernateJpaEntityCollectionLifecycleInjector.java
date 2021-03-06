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

package org.elasticsearch.gps.device.jpa.lifecycle;

import java.io.Serializable;
import javax.persistence.EntityManagerFactory;

import org.elasticsearch.gps.device.jpa.JpaGpsDevice;
import org.elasticsearch.gps.device.jpa.JpaGpsDeviceException;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.event.spi.AbstractCollectionEvent;
import org.hibernate.event.spi.PostCollectionRecreateEvent;
import org.hibernate.event.spi.PostCollectionRecreateEventListener;
import org.hibernate.event.spi.PostCollectionRemoveEvent;
import org.hibernate.event.spi.PostCollectionRemoveEventListener;
import org.hibernate.event.spi.PostCollectionUpdateEvent;
import org.hibernate.event.spi.PostCollectionUpdateEventListener;
import org.hibernate.internal.SessionFactoryImpl;

/**
 * @author kimchy
 */
public class HibernateJpaEntityCollectionLifecycleInjector extends HibernateJpaEntityLifecycleInjector {

    public static class HibernateCollectionEventListener extends HibernateEventListener
            implements PostCollectionRecreateEventListener, PostCollectionRemoveEventListener, PostCollectionUpdateEventListener {
		private static final long serialVersionUID = 1L;

        public HibernateCollectionEventListener(JpaGpsDevice device) {
            super(device);
        }

        public void onPostRecreateCollection(PostCollectionRecreateEvent postCollectionRecreateEvent) {
            processCollectionEvent(postCollectionRecreateEvent);
        }

        public void onPostRemoveCollection(PostCollectionRemoveEvent postCollectionRemoveEvent) {
            processCollectionEvent(postCollectionRemoveEvent);
        }

        public void onPostUpdateCollection(PostCollectionUpdateEvent postCollectionUpdateEvent) {
            processCollectionEvent(postCollectionUpdateEvent);
        }

        private void processCollectionEvent(AbstractCollectionEvent event) {
            final Object entity = event.getAffectedOwnerOrNull();
            if (entity == null) {
                //Hibernate cannot determine every single time the owner especially incase detached objects are involved
                // or property-ref is used
                //Should log really but we don't know if we're interested in this collection for indexing
                return;
            }
            Serializable id = getId(entity, event);
            if (id == null) {
                log.warn("Unable to reindex entity on collection change, id cannot be extracted: " + event.getAffectedOwnerEntityName());
                return;
            }

            postUpdate(entity);
        }

        private Serializable getId(Object entity, AbstractCollectionEvent event) {
            Serializable id = event.getAffectedOwnerIdOrNull();
            if (id == null) {
                //most likely this recovery is unnecessary since Hibernate Core probably try that
                EntityEntry entityEntry = event.getSession().getPersistenceContext().getEntry(entity);
                id = entityEntry == null ? null : entityEntry.getId();
            }
            return id;
        }
    }

    private Object eventListener;

    public HibernateJpaEntityCollectionLifecycleInjector() {
        super();
    }

    public HibernateJpaEntityCollectionLifecycleInjector(boolean registerPostCommitListeneres) {
        super(registerPostCommitListeneres);
    }

    public void injectLifecycle(EntityManagerFactory entityManagerFactory, JpaGpsDevice device) throws JpaGpsDeviceException {
        super.injectLifecycle(entityManagerFactory, device);

        if (registerPostCommitListeneres) {
            return;
        }

        HibernateEntityManagerFactory hibernateEntityManagerFactory =
                (HibernateEntityManagerFactory) entityManagerFactory;
        SessionFactoryImpl sessionFactory = (SessionFactoryImpl) hibernateEntityManagerFactory.getSessionFactory();

/* (AGR_OSEM) Hib4

        EventListeners eventListeners = sessionFactory.getEventListeners();

        if (eventListener instanceof PostCollectionRecreateEventListener) {
            PostCollectionRecreateEventListener[] listeners = eventListeners.getPostCollectionRecreateEventListeners();
            PostCollectionRecreateEventListener[] tempListeners = new PostCollectionRecreateEventListener[listeners.length + 1];
            System.arraycopy(listeners, 0, tempListeners, 0, listeners.length);
            tempListeners[listeners.length] = (PostCollectionRecreateEventListener) eventListener;
            eventListeners.setPostCollectionRecreateEventListeners(tempListeners);
        }

        if (eventListener instanceof PostCollectionRemoveEventListener) {
            PostCollectionRemoveEventListener[] listeners = eventListeners.getPostCollectionRemoveEventListeners();
            PostCollectionRemoveEventListener[] tempListeners = new PostCollectionRemoveEventListener[listeners.length + 1];
            System.arraycopy(listeners, 0, tempListeners, 0, listeners.length);
            tempListeners[listeners.length] = (PostCollectionRemoveEventListener) eventListener;
            eventListeners.setPostCollectionRemoveEventListeners(tempListeners);
        }

        if (eventListener instanceof PostCollectionUpdateEventListener) {
            PostCollectionUpdateEventListener[] listeners = eventListeners.getPostCollectionUpdateEventListeners();
            PostCollectionUpdateEventListener[] tempListeners = new PostCollectionUpdateEventListener[listeners.length + 1];
            System.arraycopy(listeners, 0, tempListeners, 0, listeners.length);
            tempListeners[listeners.length] = (PostCollectionUpdateEventListener) eventListener;
            eventListeners.setPostCollectionUpdateEventListeners(tempListeners);
        }
 */
    }

    public void removeLifecycle(EntityManagerFactory entityManagerFactory, JpaGpsDevice device) throws JpaGpsDeviceException {
        super.removeLifecycle(entityManagerFactory, device);

        if (registerPostCommitListeneres) {
            return;
        }

        HibernateEntityManagerFactory hibernateEntityManagerFactory =
                (HibernateEntityManagerFactory) entityManagerFactory;
        SessionFactoryImpl sessionFactory = (SessionFactoryImpl) hibernateEntityManagerFactory.getSessionFactory();

/* (AGR_OSEM) Hib4

	EventListeners eventListeners = sessionFactory.getEventListeners();

        PostCollectionRecreateEventListener[] postCollectionRecreateEventListeners = eventListeners.getPostCollectionRecreateEventListeners();
        ArrayList<PostCollectionRecreateEventListener> tempPostCollectionRecreateEventListeners = new ArrayList<PostCollectionRecreateEventListener>();
        for (PostCollectionRecreateEventListener postCollectionRecreateEventListener : postCollectionRecreateEventListeners) {
            if (!(postCollectionRecreateEventListener instanceof HibernateCollectionEventListener)) {
                tempPostCollectionRecreateEventListeners.add(postCollectionRecreateEventListener);
            }
        }
        eventListeners.setPostCollectionRecreateEventListeners(tempPostCollectionRecreateEventListeners.toArray(new PostCollectionRecreateEventListener[tempPostCollectionRecreateEventListeners.size()]));

        PostCollectionUpdateEventListener[] postCollectionUpdateEventListeners = eventListeners.getPostCollectionUpdateEventListeners();
        ArrayList<PostCollectionUpdateEventListener> tempPostCollectionUpdateEventListeners = new ArrayList<PostCollectionUpdateEventListener>();
        for (PostCollectionUpdateEventListener postCollectionUpdateEventListener : postCollectionUpdateEventListeners) {
            if (!(postCollectionUpdateEventListener instanceof HibernateCollectionEventListener)) {
                tempPostCollectionUpdateEventListeners.add(postCollectionUpdateEventListener);
            }
        }
        eventListeners.setPostCollectionUpdateEventListeners(tempPostCollectionUpdateEventListeners.toArray(new PostCollectionUpdateEventListener[tempPostCollectionUpdateEventListeners.size()]));

        PostCollectionRemoveEventListener[] postCollectionRemoveEventListeners = eventListeners.getPostCollectionRemoveEventListeners();
        ArrayList<PostCollectionRemoveEventListener> tempPostCollectionRemoveEventListeners = new ArrayList<PostCollectionRemoveEventListener>();
        for (PostCollectionRemoveEventListener postCollectionRemoveEventListener : postCollectionRemoveEventListeners) {
            if (!(postCollectionRemoveEventListener instanceof HibernateCollectionEventListener)) {
                tempPostCollectionRemoveEventListeners.add(postCollectionRemoveEventListener);
            }
        }
        eventListeners.setPostCollectionRemoveEventListeners(tempPostCollectionRemoveEventListeners.toArray(new PostCollectionRemoveEventListener[tempPostCollectionRemoveEventListeners.size()]));
*/
        eventListener = null;
    }

    protected Object doCreateListener(JpaGpsDevice device) {
        eventListener = new HibernateCollectionEventListener(device);
        return eventListener;
    }
}
