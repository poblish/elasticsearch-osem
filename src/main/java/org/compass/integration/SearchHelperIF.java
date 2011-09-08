/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.compass.integration;

import java.io.Serializable;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHits;

/**
 *
 * @author andrewregan
 */
public interface SearchHelperIF
{
	SearchHits getHits( final QueryBuilder inQuery);
	SearchHits getHits( final QueryBuilder inQuery, final FilterBuilder inFilter, final String... inIndices);
	SearchHits getHits( final QueryBuilder inQuery, final String... inIndices);
	SearchHits getHits( final QueryBuilder inQuery, final int inMax);
	SearchHits getHits( final QueryBuilder inQuery, final int inMax, final String... inIndices);
	SearchHits getHits( final QueryBuilder inQuery, final FilterBuilder inFilter, final int inMax, final String... inIndices);

	SearchHits sortedHits( final QueryBuilder inQuery, final String inSortField, final String... inIndices);
	SearchHits sortedHits( final QueryBuilder inQuery, final int inMax, final String inSortField, final String... inIndices);

	SearchHits reversedHits( final QueryBuilder inQuery, final String inSortField, final String... inIndices);
	SearchHits reversedHits( final QueryBuilder inQuery, final int inMax, final String inSortField, final String... inIndices);

	Resource createResource( final String inIdx);

	Resource getResourceOrNull( final Class<?> inIdx, final Serializable inId);	// in place of Session.load / .get
	Resource getResourceOrNull( final Class<?> inIdx, final long inId);		// in place of Session.load / .get

	Resource getResourceOrNull( final String inIdx, final String inId);		// in place of Session.load / .get
	Resource getResourceOrNull( final String inIdx, final Serializable inId);		// in place of Session.load / .get
	Resource getResourceOrNull( final String inIdx, final Object inId);		// in place of Session.load / .get
}