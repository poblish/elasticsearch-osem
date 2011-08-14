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

package org.compass.core.spi;

import org.compass.core.CompassSession;

/**
 * 
 * @author kimchy
 * 
 */
public interface InternalCompassSession extends CompassSession {

    InternalCompass getCompass();

    // (AGR_OSEM) ... SearchEngine getSearchEngine();

    // (AGR_OSEM) ... MarshallingStrategy getMarshallingStrategy();

    // (AGR_OSEM) ... FirstLevelCache getFirstLevelCache();

    // (AGR_OSEM) ... Object get(String alias, Object id, MarshallingContext context) throws CompassException;

    // (AGR_OSEM) ... Object getByResource(Resource resource) throws CompassException;

    // (AGR_OSEM) ... Resource getResourceByIdResource(Resource idResource) throws CompassException;

    // (AGR_OSEM) ... Resource getResourceByIdResourceNoCache(Resource idResource) throws CompassException;

    // (AGR_OSEM) ... CompassMapping getMapping();

    // (AGR_OSEM) ... CompassMetaData getMetaData();

    void startTransactionIfNeeded();
    
    // (AGR_OSEM) ... void addDelegateClose(InternalSessionDelegateClose delegateClose);

    void unbindTransaction();

    // context operations

    // (AGR_OSEM) ... void create(String alias, Object object, DirtyOperationContext context) throws CompassException;

    // (AGR_OSEM) ... void create(Object object, DirtyOperationContext context) throws CompassException;

    // (AGR_OSEM) ... void save(String alias, Object object, DirtyOperationContext context) throws CompassException;

    // (AGR_OSEM) ... void save(Object object, DirtyOperationContext context) throws CompassException;

    // (AGR_OSEM) ... void delete(String alias, Object obj, DirtyOperationContext context) throws CompassException;

    // (AGR_OSEM) ... void delete(Class clazz, Object obj, DirtyOperationContext context) throws CompassException;

    // (AGR_OSEM) ... void delete(Object obj, DirtyOperationContext context) throws CompassException;
}
