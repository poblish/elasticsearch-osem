/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.elasticsearch.osem.test.entities.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import org.elasticsearch.osem.annotations.Index;
import org.elasticsearch.osem.annotations.Indexable;
import org.elasticsearch.osem.annotations.Searchable;
import org.elasticsearch.osem.test.entities.interfaces.ActorIF;
import org.elasticsearch.osem.test.entities.interfaces.BlogIF;
import org.elasticsearch.osem.test.entities.interfaces.FeedIF;

/**
 *
 * @author andrewregan
 */
@Searchable
@Entity(name="Test_Feed")
public class Feed implements FeedIF, Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Indexable
	private Long					id;

	@Column(name="url")
	@Indexable
	private String					m_URL;

	@Column(name="description")
	@Indexable
	private String					m_Description;

	@Column(name="twitterAccount")
	@Indexable(index=Index.NOT_ANALYZED)
	private String					m_TwitterAccountName;

	@ManyToOne(cascade=CascadeType.ALL, /* (AGR) 22 Jan 2010. Breaks indexing of FeedArticles if LAZY is on!!! ... fetch=FetchType.LAZY, */ targetEntity=Blog.class)
	@Indexable
	private BlogIF					m_RelatedBlog;

	@ManyToMany(mappedBy="m_NonBlogFeeds", fetch=FetchType.LAZY, targetEntity=Actor.class)
	@Indexable
	private final Set<ActorIF>			m_Actors = new HashSet<ActorIF>();

	@Column(name="locale",columnDefinition="VARCHAR(20)",nullable=true)
	@Indexable
	private Locale					m_Locale;

	@Column(name="isGroupFeed",columnDefinition="TINYINT(1) DEFAULT 0",nullable=false)
	@Indexable(indexName="feedIsGroup")
	private boolean					m_IsGroupFeed;

	private final static Collection<String>		s_URLTermsWeDontWant = Collections.emptyList();

	@Column(name="disabled",columnDefinition="TINYINT(1) DEFAULT 0",nullable=false)
	private boolean					m_Disabled = false;

	/****************************************************************************
	****************************************************************************/
	@SuppressWarnings("PMD")
	public Feed()
	{
	}

	/****************************************************************************
	****************************************************************************/
	public Feed( final BlogIF inRelatedBlog, final String inURL, final String inDesc, final boolean inIsGroupFeed, final Locale inLocale)
	{
		if ( inRelatedBlog != null)
		{
			inRelatedBlog.addFeed(this);

			m_RelatedBlog = inRelatedBlog;
		}

		m_URL = inURL.trim();
		m_Description = inDesc;
		m_IsGroupFeed = inIsGroupFeed;
		m_Locale = inLocale;
	}

	/****************************************************************************
	****************************************************************************/
	public Long getId()
	{
		return id;
	}

	/****************************************************************************
	****************************************************************************/
	public BlogIF getRelatedBlog()
	{
		return m_RelatedBlog;
	}

	/****************************************************************************
	****************************************************************************/
	public String getURL()
	{
		return m_URL;
	}

	/****************************************************************************
	****************************************************************************/
	public void setURL( final String inURL)
	{
		m_URL = inURL;
	}

	/****************************************************************************
	****************************************************************************/
	public void addActor( final ActorIF inActor)
	{
		m_Actors.add(inActor);
	}

	/****************************************************************************
	****************************************************************************/
	public Set<ActorIF> getActors()
	{
		return m_Actors;
	}

	/****************************************************************************
	****************************************************************************/
	public boolean isGroupFeed()
	{
		return m_IsGroupFeed;
	}

	/****************************************************************************
	****************************************************************************/
	public Collection<String> getActorUsernames()
	{
		Collection<String>	theColl = new ArrayList<String>();

		for ( ActorIF each : m_Actors)
		{
			theColl.add( each.getUserName() );
		}

		return theColl;
	}

	/****************************************************************************
	****************************************************************************/
	public Locale getPreferredLocale()
	{
		return ( m_Locale != null) ? m_Locale : Locale.ENGLISH;
	}

	/****************************************************************************
	****************************************************************************/
	public String getDisplayName()
	{
		return m_Description;
	}

	/****************************************************************************
	****************************************************************************/
	public String getNameForChannel()
	{
		return getDisplayName();
	}

	/****************************************************************************
	****************************************************************************/
	public boolean getIsGroupResource()
	{
		return m_IsGroupFeed;
	}

	/****************************************************************************
	****************************************************************************/
	public void setIsGroupResource( final boolean inValue)
	{
		m_IsGroupFeed = inValue;
	}

	/****************************************************************************
	****************************************************************************/
	public boolean isMultiAuthored()
	{
		return false;
	}

	/****************************************************************************
	****************************************************************************/
	public String getTwitterAccountName()
	{
		return m_TwitterAccountName;
	}

	/****************************************************************************
	****************************************************************************/
	public void setTwitterAccountName( final String inName)
	{
		m_TwitterAccountName = inName;
	}

	/****************************************************************************
	****************************************************************************/
	public boolean isDisabled()
	{
		return m_Disabled;
	}

	/****************************************************************************
	****************************************************************************/
	public void setDisabled( final boolean inVal)
	{
		m_Disabled = inVal;
	}

	/****************************************************************************
	****************************************************************************/
	public String getIdColumnName()
	{
		return "feedId";
	}

	/****************************************************************************
	****************************************************************************/
	@Override public int hashCode()
	{
		int hash = 0;
		hash += ( m_URL != null ? m_URL.hashCode() : 0);
		return hash;
	}

	/****************************************************************************
	****************************************************************************/
	@Override public boolean equals( final Object object)
	{
		if ( object == null)
		{
			return false;
		}
		if ( this == object)
		{
			return true;
		}

		if (!(object instanceof FeedIF))
		{
			return false;
		}

		Feed other = (Feed) object;

		if (( m_URL == null) ? (other.getURL() != null) : !m_URL.equals( other.getURL() ))
		{
			return false;
		}
		return true;
	}

	/****************************************************************************
	****************************************************************************/
	@Override public String toString()
	{
		if ( id == null)
		{
			return "NEW Feed [url='" + m_URL + "', desc='" + m_Description + "']";
		}

		StringBuilder	sb = new StringBuilder();
		sb.append("Feed #").append(id).append(" [url='" + m_URL + "', desc='" + m_Description + "']");
		return sb.toString();
	}
}
