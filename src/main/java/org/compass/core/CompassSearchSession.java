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

import java.io.Serializable;
import org.compass.integration.InternalResource;
import org.compass.integration.Resource;
import org.compass.integration.Resources;
import org.compass.integration.SearchHelperIF;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.base.Preconditions;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.osem.core.ObjectContext;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.facet.AbstractFacetBuilder;
import org.elasticsearch.search.facet.Facets;
import org.elasticsearch.search.sort.SortOrder;

/**
 * A specialized interface that provides only search and read capabilities.
 *
 * <p>Using the session depends on how transaction management should be done (also see
 * {@link Compass#openSearchSession()}. The simplest form looks like this:
 *
 * <pre>
 * CompassSearchSession session = compass.openSearchSession();
 * try {
 *      // do search operations
 * } finally {
 *      session.close();
 * }
 * </pre>
 *
 * @author kimchy
 */
public interface CompassSearchSession {

	static class InternalSearchHelper implements SearchHelperIF
	{
		private final Client		m_Client;
		private final ObjectContext	m_Ctxt;

		private final static String	DEFAULT_TYPE = "default";
		private final static int		DEFAULT_MATCHES_COUNT = 99999;	// (AGR) 3 October 2011. A bit iffy!!!

		private final static ESLogger	logger = Loggers.getLogger( InternalSearchHelper.class );

		public InternalSearchHelper( final CompassSession inSsn)
		{
			m_Client = inSsn.getClient();
			m_Ctxt = inSsn.getObjectContext();
		}

		@Override
		public SearchHits getHits( final QueryBuilder inQuery, final String... inIndices)
		{
			Preconditions.checkArgument( inIndices.length >= 1);

			final SearchResponse	theResp = m_Client.prepareSearch(inIndices).setQuery(inQuery).setSize(DEFAULT_MATCHES_COUNT).execute().actionGet();
			return _getHits(theResp);
		}

		@Override
		public Facets getFacets( final QueryBuilder inQuery, final AbstractFacetBuilder inFacetB, final FilterBuilder inFilter, final String... inIndices)
		{
			Preconditions.checkArgument( inIndices.length >= 1);

			return m_Client.prepareSearch(inIndices).addFacet(inFacetB).setQuery(inQuery).setFilter(inFilter).setSize(DEFAULT_MATCHES_COUNT).execute().actionGet().getFacets();
		}

		@Override
		public SearchHits getHits( final QueryBuilder inQuery, final FilterBuilder inFilter, final String... inIndices)
		{
			Preconditions.checkArgument( inIndices.length >= 1);

			final SearchResponse	theResp = m_Client.prepareSearch(inIndices).setQuery(inQuery).setFilter(inFilter).setSize(DEFAULT_MATCHES_COUNT).execute().actionGet();
			return _getHits(theResp);
		}

		@Override
		public SearchHits getHits( final QueryBuilder inQuery, final int inMaxNum, final String... inIndices)
		{
			Preconditions.checkArgument( inIndices.length >= 1);

			final SearchResponse	theResp = m_Client.prepareSearch(inIndices).setQuery(inQuery).setSize(inMaxNum).execute().actionGet();
			return _getHits(theResp);
		}

		@Override
		public SearchHits getHits( final QueryBuilder inQuery, final FilterBuilder inFilter, final int inMaxNum, final String... inIndices)
		{
			Preconditions.checkArgument( inIndices.length >= 1);

			final SearchResponse	theResp = m_Client.prepareSearch(inIndices).setQuery(inQuery).setFilter(inFilter).setSize(inMaxNum).execute().actionGet();
			return _getHits(theResp);
		}

		// @Override
		public GetResponse getResource( final String inIdx, final String inId)
		{
			return m_Client.prepareGet( inIdx, DEFAULT_TYPE, inId).execute().actionGet();
		}

		@Override
		public Resource getResourceOrNull( final String inIdx, final Object inId)
		{
			return ( inId != null) ? getResourceOrNull( inIdx, String.valueOf(inId)) : null;
		}

		@Override
		public Resource getResourceOrNull( final String inIdx, final Serializable inId)
		{
			return ( inId != null) ? getResourceOrNull( inIdx, String.valueOf(inId)) : null;
		}

		@Override
		public Resource getResourceOrNull( final String inIdx, final String inId)
		{
			if ( inId == null)
			{
				return null;
			}

			final GetResponse	theResp = getResource( inIdx, inId);

			return theResp.exists() ? Resources.fromSourceMap( theResp.getSource() ) : null;
		}

		@Override
		public Resource createResource( final String inIdx)
		{
			return new InternalResource(inIdx);
		}

		@Override
		public void saveResource( final Resource inResource)
		{
			final XContentBuilder	theResBuilder = m_Ctxt.write(inResource);

			m_Client.prepareIndex( inResource.getAlias(), DEFAULT_TYPE, inResource.getId())
				.setSource(theResBuilder)
				.execute().actionGet();
		}

		@Override
		public SearchHits sortedHits( final QueryBuilder inQuery, final String inSortField, final String... inIndices)
		{
			final SearchResponse	theResp = m_Client.prepareSearch(inIndices).setQuery(inQuery).setSize(DEFAULT_MATCHES_COUNT).addSort( inSortField, SortOrder.ASC).execute().actionGet();
			return _getHits(theResp);
		}

		@Override
		public SearchHits sortedHits( final QueryBuilder inQuery, final int inMaxNum, final String inSortField, final String... inIndices)
		{
			final SearchResponse	theResp = m_Client.prepareSearch(inIndices).setQuery(inQuery).setSize(inMaxNum).addSort( inSortField, SortOrder.ASC).execute().actionGet();
			return _getHits(theResp);
		}

		@Override
		public SearchHits sortedHits( final QueryBuilder inQuery, final FilterBuilder inFilter, final int inMaxNum, final String inSortField, final String... inIndices)
		{
			final SearchResponse	theResp = m_Client.prepareSearch(inIndices).setQuery(inQuery).setFilter(inFilter).setSize(inMaxNum).addSort( inSortField, SortOrder.ASC).execute().actionGet();
			return _getHits(theResp);
		}

		@Override
		public SearchHits reversedHits( final QueryBuilder inQuery, final String inSortField, final String... inIndices)
		{
			final SearchResponse	theResp = m_Client.prepareSearch(inIndices).setQuery(inQuery).setSize(DEFAULT_MATCHES_COUNT).addSort( inSortField, SortOrder.DESC).execute().actionGet();
			return _getHits(theResp);
		}

		@Override
		public SearchHits reversedHits( final QueryBuilder inQuery, final int inMaxNum, final String inSortField, final String... inIndices)
		{
			final SearchResponse	theResp = m_Client.prepareSearch(inIndices).setQuery(inQuery).setSize(inMaxNum).addSort( inSortField, SortOrder.DESC).execute().actionGet();
			return _getHits(theResp);
		}

		@Override
		public SearchHits reversedHits( final QueryBuilder inQuery, final FilterBuilder inFilter, final int inMaxNum, final String inSortField, final String... inIndices)
		{
			final SearchResponse	theResp = m_Client.prepareSearch(inIndices).setQuery(inQuery).setFilter(inFilter).setSize(inMaxNum).addSort( inSortField, SortOrder.DESC).execute().actionGet();
			return _getHits(theResp);
		}

		@Override
		public Resource getResourceOrNull( final Class<?> inClass, final Serializable inId)
		{
			return getResourceOrNull( m_Ctxt.getType(inClass), inId);
		}

		@Override
		public Resource getResourceOrNull( final Class<?> inClass, final long inId)
		{
			return getResourceOrNull( m_Ctxt.getType(inClass), Long.valueOf(inId));
		}

		@Override
		public void index( final String inIndex, final String inId, final Object inInitialObject)
		{
			index( inIndex, DEFAULT_TYPE, inId, inInitialObject);
		}

		@Override
		public void index( final String inIndex, final String inType, final String inId, final Object inInitialObject)
		{
			index( inIndex, inType, inId, m_Ctxt.write(inInitialObject));
		}

		@Override
		public void index( final String inIndex, final String inId, final XContentBuilder inInitialBuilder)
		{
			index( inIndex, DEFAULT_TYPE, inId, inInitialBuilder);
		}

		@Override
		public void index( final String inIndex, final String inType, final String inId, final XContentBuilder inInitialBuilder)
		{
			/* final IndexResponse	theResponse = */ m_Client.prepareIndex( inIndex, inType, inId)
									.setSource(inInitialBuilder)
									.execute()
									.actionGet();
		}

		private SearchHits _getHits( final SearchResponse inResponse)
		{
			return inResponse.getHits();
		}

		@Override
		public void refreshIndices( final String... inIndices)
		{
			m_Client.admin().indices().prepareRefresh(inIndices).execute().actionGet();
		}
	}

	SearchHelperIF elasticSearch();

	Client getClient();

	ObjectContext getObjectContext();

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
     * Closes the search session.
     */
    void close();
}
