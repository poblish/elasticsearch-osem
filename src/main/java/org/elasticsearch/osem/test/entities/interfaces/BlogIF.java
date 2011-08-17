/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.elasticsearch.osem.test.entities.interfaces;

/**
 *
 * @author andrewregan
 */
public interface BlogIF extends ActorResourceIF
{
	void addFeed( FeedIF x);
	String getURL();
}