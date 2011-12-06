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
import org.elasticsearch.action.index.IndexResponse;
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

//			logger.info( "getHits( final QueryBuilder inQuery, final String... inIndices)", (Object) inIndices);
//			logger.info( "@ Finding: " + inQuery + " among " + Arrays.toString(inIndices));

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

//			logger.info( "getHits( final QueryBuilder inQuery, final FilterBuilder inFilter, final String... inIndices)");
//			logger.info( "@ Finding: " + inQuery + " among " + Arrays.toString(inIndices) + ", applying " + inFilter);

			final SearchResponse	theResp = m_Client.prepareSearch(inIndices).setQuery(inQuery).setFilter(inFilter).setSize(DEFAULT_MATCHES_COUNT).execute().actionGet();
			return _getHits(theResp);
		}

		@Override
		public SearchHits getHits( final QueryBuilder inQuery, final int inMaxNum, final String... inIndices)
		{
			Preconditions.checkArgument( inIndices.length >= 1);

//			logger.info( "getHits( final QueryBuilder inQuery, final int inMaxNum, final String... inIndices)", (Object) inIndices);
//			logger.info( "@ Finding " + inMaxNum + " recs for " + inQuery + " among " + Arrays.toString(inIndices));

			final SearchResponse	theResp = m_Client.prepareSearch(inIndices).setQuery(inQuery).setSize(inMaxNum).execute().actionGet();
			return _getHits(theResp);
		}

		@Override
		public SearchHits getHits( final QueryBuilder inQuery, final FilterBuilder inFilter, final int inMaxNum, final String... inIndices)
		{
			Preconditions.checkArgument( inIndices.length >= 1);

//			logger.info( "getHits( final QueryBuilder inQuery, final FilterBuilder inFilter, final int inMaxNum, final String... inIndices)", (Object) inIndices);
//			logger.info( "@ Finding " + inMaxNum + " recs for " + inQuery + " among " + Arrays.toString(inIndices) + ", applying " + inFilter);

			final SearchResponse	theResp = m_Client.prepareSearch(inIndices).setQuery(inQuery).setFilter(inFilter).setSize(inMaxNum).execute().actionGet();
			return _getHits(theResp);
		}

		// @Override
		public GetResponse getResource( final String inIdx, final String inId)
		{
//			logger.info( "getResource('" + inIdx + "'," + inId + ")");

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

//			try {
//				System.out.println("Saving '" + inResource.getAlias() + "' Resource: " + theResBuilder.string());
//			}
//			catch (IOException ex) {
//				// NOOP
//			}

			m_Client.prepareIndex( inResource.getAlias(), DEFAULT_TYPE, inResource.getId())
				.setSource(theResBuilder)
				.execute().actionGet();
		}

		@Override
		public SearchHits sortedHits( final QueryBuilder inQuery, final String inSortField, final String... inIndices)
		{
//			logger.info( "sortedHits( final QueryBuilder inQuery, final String inSortField, final String... inIndices)", (Object) inIndices);
//			logger.info( "@ Finding: " + inQuery + " among " + Arrays.toString(inIndices));

			final SearchResponse	theResp = m_Client.prepareSearch(inIndices).setQuery(inQuery).setSize(DEFAULT_MATCHES_COUNT).addSort( inSortField, SortOrder.ASC).execute().actionGet();
			return _getHits(theResp);
		}

		@Override
		public SearchHits sortedHits( final QueryBuilder inQuery, final int inMaxNum, final String inSortField, final String... inIndices)
		{
//			logger.info( "sortedHits( final QueryBuilder inQuery, final int inMaxNum, final String inSortField, final String... inIndices)", (Object) inIndices);
//			logger.info( "@ Finding " + inMaxNum + " recs for " + inQuery + " among " + Arrays.toString(inIndices));

			final SearchResponse	theResp = m_Client.prepareSearch(inIndices).setQuery(inQuery).setSize(inMaxNum).addSort( inSortField, SortOrder.ASC).execute().actionGet();
			return _getHits(theResp);
		}

		@Override
		public SearchHits sortedHits( final QueryBuilder inQuery, final FilterBuilder inFilter, final int inMaxNum, final String inSortField, final String... inIndices)
		{
//			logger.info( "sortedHits( final QueryBuilder inQuery, final FilterBuilder inFilter, final int inMaxNum, final String inSortField, final String... inIndices)", (Object) inIndices);
//			logger.info( "@ Finding " + inMaxNum + " recs for " + inQuery + " among " + Arrays.toString(inIndices));

			final SearchResponse	theResp = m_Client.prepareSearch(inIndices).setQuery(inQuery).setFilter(inFilter).setSize(inMaxNum).addSort( inSortField, SortOrder.ASC).execute().actionGet();
			return _getHits(theResp);
		}

		@Override
		public SearchHits reversedHits( final QueryBuilder inQuery, final String inSortField, final String... inIndices)
		{
//			logger.info( "reversedHits( final QueryBuilder inQuery, final String inSortField, final String... inIndices)", (Object) inIndices);
//			logger.info( "@ Finding: " + inQuery + " among " + Arrays.toString(inIndices));

			final SearchResponse	theResp = m_Client.prepareSearch(inIndices).setQuery(inQuery).setSize(DEFAULT_MATCHES_COUNT).addSort( inSortField, SortOrder.DESC).execute().actionGet();
			return _getHits(theResp);
		}

		@Override
		public SearchHits reversedHits( final QueryBuilder inQuery, final int inMaxNum, final String inSortField, final String... inIndices)
		{
//			logger.info( "reversedHits( final QueryBuilder inQuery, final int inMaxNum, final String inSortField, final String... inIndices)", (Object) inIndices);
//			logger.info( "@ Finding " + inMaxNum + " recs for " + inQuery + " among " + Arrays.toString(inIndices));

			final SearchResponse	theResp = m_Client.prepareSearch(inIndices).setQuery(inQuery).setSize(inMaxNum).addSort( inSortField, SortOrder.DESC).execute().actionGet();
			return _getHits(theResp);
		}

		@Override
		public SearchHits reversedHits( final QueryBuilder inQuery, final FilterBuilder inFilter, final int inMaxNum, final String inSortField, final String... inIndices)
		{
//			logger.info( "reversedHits( final QueryBuilder inQuery, final FilterBuilder inFilter, final int inMaxNum, final String inSortField, final String... inIndices)", (Object) inIndices);
//			logger.info( "@ Finding " + inMaxNum + " recs for " + inQuery + " among " + Arrays.toString(inIndices));

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
//			try {
//				System.out.println("\n#### Indexing: '" + inIndex + "' #" + inId + " ... " + inInitialBuilder.string() + "####\n");
//			}
//			catch (IOException ex) {}
//
//			final boolean		mappingAcked = m_Client.admin().indices().preparePutMapping(inIndex)
//											  .setSource( m_Ctxt.getMapping(inClass) )
//											  .setType(DEFAULT_TYPE)
//											  .setIgnoreConflicts(false)
//											  .execute()
//											  .actionGet().getAcknowledged();
//
//			if (!mappingAcked)	// FIXME!
//			{
//				throw new RuntimeException("Mapping for '" + inIndex + "' not acknowledged!");
//			}

			final IndexResponse	theResponse = m_Client.prepareIndex( inIndex, inType, inId)
									.setSource(inInitialBuilder)
									.execute()
									.actionGet();
		}

		private SearchHits _getHits( final SearchResponse inResponse)
		{
			final SearchHits		theHits = inResponse.getHits();

		//	logger.info( "@ Response = " + ( theHits.getTotalHits() > 100 ? "(> 100 hits)" : inResponse));

			return theHits;
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
     * Runtimes settings that apply on the session level.
     *
     * @return Runtime settings applies on the session level
     */
    // (AGR_OSEM) ... CompassSettings getSettings();

    /**
     * When not using the {@link org.compass.core.CompassTransaction} interface, will begin a local transaction
     * instead of the configured transaction.
     */
    // (AGR_OSEM) ... CompassSearchSession useLocalTransaction();

    /**
     * Returns a Resource that match the mapping specified for the defined class
     * type, and specified id. The id can be an object of the class (with the id
     * attributes set), an array of id objects, or the actual id object. Returns
     * <code>null</code> if the object is not found.
     *
     * @param clazz The class that represents the required mapping
     * @param id    The id that identifies the resource
     * @return The resource, returns <code>null</code> if not found
     * @throws CompassException
     */
   // (AGR_OSEM) ...  Resource getResource(Class clazz, Object id) throws CompassException;

    /**
     * Returns a Resource that match the mapping specified for the defined class
     * type, and specified ids.
     *
     * @param clazz The class that represents the required mapping
     * @param ids   The ids that identifies the resource
     * @return The resource, returns <code>null</code> if not found
     * @throws CompassException
     */
    // (AGR_OSEM) ... Resource getResource(Class clazz, Object... ids) throws CompassException;

    /**
     * Returns a Resource that match the mapping specified for the defined alias
     * (possibley different object types), and matches the specified id. The id
     * can be an object of the class (with the id attributes set), an array of
     * id objects, or the actual id object. Returns <code>null</code> if the
     * object is not found.
     *
     * @param alias The alias that represents the required mapping
     * @param id    The id that identifies the resource
     * @return The resource
     * @throws CompassException
     */
    // (AGR_OSEM) ... Resource getResource(String alias, Object id) throws CompassException;

    /**
     * Returns a Resource that match the mapping specified for the defined alias
     * (possibley different object types), and matches the specified ids. Returns
     * <code>null</code> if the object is not found.
     *
     * @param alias The alias that represents the required mapping
     * @param ids   The ids that identifies the resource
     * @return The resource
     * @throws CompassException
     */
    // (AGR_OSEM) ... Resource getResource(String alias, Object... ids) throws CompassException;

    /**
     * Loads and returns a Resource that match the mapping specified for the
     * defined class, and matches the specified id. The id can be an object of
     * the class (with the id attributes set), an array of id objects, or the
     * actual id object. Throws an exception if the resource is not found.
     *
     * @param clazz The class that represents the required mapping
     * @param id    The id that identifies the resource
     * @return The resource
     * @throws CompassException
     */
    // (AGR_OSEM) ... Resource loadResource(Class clazz, Object id) throws CompassException;

    /**
     * Loads and returns a Resource that match the mapping specified for the
     * defined class, and matches the specified ids. Throws an exception if
     * the resource is not found.
     *
     * @param clazz The class that represents the required mapping
     * @param ids   The ids that identifies the resource
     * @return The resource
     * @throws CompassException
     */
    // (AGR_OSEM) ... Resource loadResource(Class clazz, Object... ids) throws CompassException;

    /**
     * Loads and returns a Resource that match the mapping specified for the
     * defined alias, and matches the specified id. The id can be an object of
     * the class (with the id attributes set), an array of id objects, or the
     * actual id object. Throws an exception if the resource is not found.
     *
     * @param alias The alias that represents the required mapping
     * @param id    The id that identifies the resource
     * @return The resource
     * @throws CompassException
     */
    // (AGR_OSEM) ... Resource loadResource(String alias, Object id) throws CompassException;

    /**
     * Loads and returns a Resource that match the mapping specified for the
     * defined alias, and matches the specified ids. Throws an exception if
     * the resource is not found.
     *
     * @param alias The alias that represents the required mapping
     * @param ids   The ids that identifies the resource
     * @return The resource
     * @throws CompassException
     */
    // (AGR_OSEM) ... Resource loadResource(String alias, Object... ids) throws CompassException;

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
     * Finds a list of objects that match the specified query. The query syntax
     * is a search engine format query. For detailed description of the query
     * syntax please visit the site.
     * <p>
     * Several examples are:
     * <ul>
     * <li>A set of words - i.e. "Jack London". Compass will search the default
     * property (usually ALL properties, specified in CompassEnvironment).</li>
     * <li>A set of words prefixed by meta data name - i.e. author:"Jack
     * London". Compass will search only meta data name author matching keywords
     * Jack London.
     * <li>Multiple meta data names - i.e. author:"Jack London" AND book:Fang*.
     * Compass will search both meta data name author matching keywords Jack
     * London and meta data name book matching wildcard Fang*</li>
     * </ul>
     * </p>
     * <p>
     * Note that the list may contains several object types (classes) with no
     * relation between them (except for the semantic relation).
     * </p>
     *
     * @param query The query string to search by
     * @return A hits of objects that matches the query string
     * @throws CompassException
     */
    // (AGR_OSEM) ... CompassHits find(String query) throws CompassException;

    /**
     * Creats a new query builder, used to build queries programmatically.
     *
     * @return The query builder.
     */
    // (AGR_OSEM) ... CompassQueryBuilder queryBuilder() throws CompassException;

    /**
     * Creats a new query filter builder, used to build filters of queries
     * programmatically.
     *
     * @return The query filter builder.
     */
    // (AGR_OSEM) ... CompassQueryFilterBuilder queryFilterBuilder() throws CompassException;

    /**
     * Creates a new terms frequencies builder used to get terms names and
     * freqs for a list of property names.
     *
     * <p>Note, term frequencies are updated to reflect latest changes to the index
     * only after an optimization has taken place (note, calling optimize might not
     * cause optimization).
     *
     * @param names The property names
     * @return A term freqs builder
     * @throws CompassException
     */
    // (AGR_OSEM) ... CompassTermFreqsBuilder termFreqsBuilder(String... names) throws CompassException;

    /**
     * Returns an Analyzer helper. Can be used to help analyze given texts.
     *
     * @return the analyzer helper
     * @throws CompassException
     */
    // (AGR_OSEM) ... CompassAnalyzerHelper analyzerHelper() throws CompassException;

    /**
     * Closes the search session.
     */
    void close();
}
