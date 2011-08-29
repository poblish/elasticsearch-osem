/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.compass.core;

import org.elasticsearch.search.SearchHits;

/**
 *
 * @author andrewregan
 */
public interface CompassHits
{
	long length();

	SearchHits getSearchHits();
}