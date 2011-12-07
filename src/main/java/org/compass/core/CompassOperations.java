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

package org.compass.core;

/**
 * A interface describing all the available operations allowed by compass.
 *
 * @author kimchy
 */
public interface CompassOperations {

    /**
     * Deletes an object from Compass. The object must have been either loaded
     * by Compass or it's ids must be set if already known.
     *
     * @param obj The object to delete
     * @throws CompassException
     */
    void delete(Object obj) throws CompassException;

    /**
     * Deletes an object from Compass with multiple alias's. The object can
     * either be the id (or an array of ids), or the actual data object with
     * it's property ids set.
     *
     * @param alias The alias that the objects maps under
     * @param obj   The object to delete
     * @throws CompassException
     */
    void delete(String alias, Object obj) throws CompassException;

    /**
     * Deletes an object from Compass with multiple alias's based on
     * its ids.
     *
     * @param alias The alias that the objects maps under
     * @param ids   The ids of the object to delete
     * @throws CompassException
     */
    void delete(String alias, Object... ids) throws CompassException;

    /**
     * Deletes an object from Compass that match the mapping specified for the defined class.
     * The object can either be the id (or an array of ids), or the actual data object with
     * it's property ids set.
     *
     * @param clazz The class that represtents the required mapping
     * @param obj   The object to delete
     * @throws CompassException
     */
    void delete(Class clazz, Object obj) throws CompassException;

    /**
     * Deletes an object from Compass that match the mapping specified for the defined class
     * based on its ids.
     *
     * @param clazz The class that represtents the required mapping
     * @param ids   The object ids to delete
     * @throws CompassException
     */
    void delete(Class clazz, Object... ids) throws CompassException;

    /**
     * Returns an object that match the mapping specified for the defined class,
     * and matches the specified id. The id can be an object of the class (with
     * the id attributes set), an array of id objects, or the actual id object.
     * Returns <code>null</code> if the object is not found.
     *
     * @param clazz The class that represents the required mapping
     * @param id    The id that identifies the resource
     * @return The object, returns <code>null</code> if not found
     * @throws CompassException
     */
    <T> T get(Class<T> clazz, Object id) throws CompassException;

    /**
     * Returns an object that match the mapping specified for the defined class,
     * and matches the specified ids. Returns <code>null</code> if the object
     * is not found.
     *
     * @param clazz The class that represents the required mapping
     * @param ids   The ids that identifies the resource
     * @return The object, returns <code>null</code> if not found
     * @throws CompassException
     */
    <T> T get(Class<T> clazz, Object... ids) throws CompassException;

    /**
     * Returns an object that match the mapping specified for the defined alias,
     * and matches the specified id. The id can be an object of the class (with
     * the id attributes set), an array of id objects, or the actual id object.
     * Returns <code>null</code> if the object is not found.
     *
     * @param alias The alias that represents the required mapping
     * @param id    The id that identifies the resource
     * @return The object, returns <code>null</code> if not found
     * @throws CompassException
     */
    Object get(String alias, Object id) throws CompassException;

    /**
     * Returns an object that match the mapping specified for the defined alias,
     * and matches the specified ids. Returns <code>null</code> if the object is
     * not found.
     *
     * @param alias The alias that represents the required mapping
     * @param ids   The ids that identifies the resource
     * @return The object, returns <code>null</code> if not found
     * @throws CompassException
     */
    Object get(String alias, Object... ids) throws CompassException;

    /**
     * Loads and returns an object that match the mapping specified for the
     * defined class, and matches the specified id. The id can be an object of
     * the class (with the id attributes set), an array of id objects, or the
     * actual id object. Throws an exception if the resource is not found.
     *
     * @param clazz The class that represents the required mapping
     * @param id    The id that identifies the resource
     * @return The object
     * @throws CompassException
     */
    <T> T load(Class<T> clazz, Object id) throws CompassException;

    /**
     * Loads and returns an object that match the mapping specified for the
     * defined class, and matches the specified ids.
     *
     * @param clazz The class that represents the required mapping
     * @param ids   The ids that identifies the resource
     * @return The object
     * @throws CompassException
     */
    <T> T load(Class<T> clazz, Object... ids) throws CompassException;

    /**
     * Loads and returns an object that match the mapping specified for the
     * defined class, and matches the specified id. The id can be an object of
     * the class (with the id attributes set), an array of id objects, or the
     * actual id object. Throws an exception if the resource is not found.
     *
     * @param alias The alias that represents the required mapping
     * @param id    The id that identifies the resource
     * @return The object
     * @throws CompassException
     */
    Object load(String alias, Object id) throws CompassException;

    /**
     * Loads and returns an object that match the mapping specified for the
     * defined class, and matches the specified ids.
     *
     * @param alias The alias that represents the required mapping
     * @param ids   The ids that identifies the resource
     * @return The object
     * @throws CompassException
     */
    Object load(String alias, Object... ids) throws CompassException;

    /**
     * Creates a NEW object in Compass. All the meta data defined in the Compass
     * mapping files will be indexed and saved for later searching. Note that if
     * the same object (same alias and same id's) already exists in the index, it
     * won't be deleted.
     *
     * @param obj The object to save.
     * @throws CompassException
     */
    void create(Object obj) throws CompassException;

    /**
     * Creates a NEW object in Compass that shares mapping alais with multiple
     * objects. All the meta data defined in Compass mapping files will be
     * indexed and saved for later searching. Note that if
     * the same object (same alias and same id's) already exists in the index, it
     * won't be deleted.
     *
     * @param alias The alias that match the object mappings
     * @param obj   The object to save
     * @throws CompassException
     */
    void create(String alias, Object obj) throws CompassException;

    /**
     * Saves an object in Compass. All the meta data defined in the Compass
     * mapping files will be indexed and saved for later searching.
     *
     * @param obj The object to save.
     * @throws CompassException
     */
    void save(Object obj) throws CompassException;

    /**
     * Saves an object in Compass that shares mapping alais with multiple
     * objects. All the meta data defined in Compass mapping files will be
     * indexed and saved for later searching.
     *
     * @param alias The alias that match the object mappings
     * @param obj   The object to save
     * @throws CompassException
     */
    void save(String alias, Object obj) throws CompassException;

    /**
     * Evicts the given object from the first level cache (transaction scoped
     * cache).
     *
     * @param obj The objects to evict.
     */
    void evict(Object obj);

    /**
     * Evicts the given object from the first level cache (transaction scoped
     * cache). The object can either be the id (or an array of ids), or the
     * actual data object with it's property ids set.
     *
     * @param alias The alias of the object/entry to evict.
     * @param id    The id of the object/entry to evict.
     */
    void evict(String alias, Object id);

    /**
     * Evicts the given resource from the first level cache (transaction scoped
     * cache).
     *
     * @param resource The resource to evict.
    void evict(Resource resource);
     */

    /**
     * Evicts all the objects and the resources from the first level cache.
     */
    void evictAll();
}
