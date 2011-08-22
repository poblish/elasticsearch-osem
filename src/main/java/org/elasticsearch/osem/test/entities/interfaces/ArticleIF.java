/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.elasticsearch.osem.test.entities.interfaces;

/**
 *
 * @author andrewregan
 */
public interface ArticleIF
{
	Long getId();
	void setContent( final String inContent);
	void setTitle( final String inContent);
	void setResource(ActorResourceIF inRes);
}