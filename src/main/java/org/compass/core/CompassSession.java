/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.compass.core;

/**
 *
 * @author andrewregan
 */
public interface CompassSession extends CompassSearchSession, CompassIndexSession, CompassOperations
{
    /**
     * Indicates that the session will be used for read only operations. Allowing to optimize
     * search and read.
     */
    void setReadOnly();

    /**
     * Returns <code>true</code> if the session is read only.
     *
     * @see #setReadOnly()
     */
    boolean isReadOnly();

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
     * Evicts all the objects and the resources from the first level cache.
     */
    void evictAll();

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
     * When not using explicit {@link org.compass.core.CompassTransaction} in order to manage transactions, can be called
     * to rollback the current running transaction. Effectively also closes the session.
     */
    void rollback() throws CompassException;

    /**
     * Same as {@link CompassSession#close()}.
     */
    void commit() throws CompassException;

    /**
     * Closes the CompassSession. Note, if this session is "contained" within another session,
     * it won't actually be closed, and defer closing the session to the other session.
     *
     * <p>If there is an on going transaction associated with the session that has not been committed
     * / rolledback yet, will commit the transaction (and in case of failure, will roll it back). Failed
     * commits will throw an exception from the close method.
     *
     * @throws CompassException
     * @see org.compass.core.Compass#openSession()
     */
    void close() throws CompassException;

    /**
     * Returns <code>true</code> if the session is closed. Note, if this session
     * "joined" another session, it won't actually be closed, and defer closing
     * the session to the outer session.
     */
    boolean isClosed();
}