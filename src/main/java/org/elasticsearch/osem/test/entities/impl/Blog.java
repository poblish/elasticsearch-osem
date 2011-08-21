/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.elasticsearch.osem.test.entities.impl;

import org.elasticsearch.osem.annotations.Indexable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import org.elasticsearch.osem.annotations.Index;
import org.elasticsearch.osem.annotations.Searchable;
import org.elasticsearch.osem.test.entities.interfaces.ActorIF;
import org.elasticsearch.osem.test.entities.interfaces.BlogIF;
import org.elasticsearch.osem.test.entities.interfaces.FeedIF;
import org.hibernate.annotations.Cascade;
import static org.elasticsearch.osem.test.entities.interfaces.FieldConstants.*;

/**
 *
 * @author andrewregan
 */
@Searchable
@Entity
public class Blog implements BlogIF, Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name=BLOG_ID)
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Indexable
	private Long					m_Id;

	@Column(name="url")
	@Indexable
	private String					m_URL;

	@Column(name="name")
	@Indexable
	private String					m_Name;

	@Column(name="twitterAccount")
	@Indexable(index=Index.NOT_ANALYZED)
	private String					m_TwitterAccountName;

	@ManyToMany(mappedBy="m_Blogs", fetch=FetchType.LAZY, cascade=CascadeType.PERSIST, targetEntity=Actor.class)
	// Do *not* do this... @LazyCollection(LazyCollectionOption.EXTRA)
	@Indexable
	private Collection<ActorIF>			m_Actors = new HashSet<ActorIF>();

	@OneToMany( fetch=FetchType.LAZY, cascade=CascadeType.PERSIST, targetEntity=Feed.class)
	@Cascade(value=org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	private Collection<FeedIF>			m_Feeds = new HashSet<FeedIF>();

	@Column(name="submitted",columnDefinition="TINYINT(1) DEFAULT 0",nullable=false)
	private boolean					m_Submitted;

	@Column(name="locale",columnDefinition="VARCHAR(20)",nullable=true)
	@Indexable
	private Locale					m_Locale;

	@Column(name="isGroupBlog",columnDefinition="TINYINT(1) DEFAULT 0",nullable=false)
	@Indexable
	private boolean					m_IsGroupBlog;

	/****************************************************************************
	****************************************************************************/
	public Blog()
	{
	}

	/****************************************************************************
	****************************************************************************/
	public Blog( final ActorIF inActor, final String inURL, Locale inLocale)
	{
		this( inActor, inURL, null, false, inLocale);
	}

	/****************************************************************************
	****************************************************************************/
	public Blog( final ActorIF inActor, final String inURL, final String inName, boolean inIsGroupBlog, Locale inLocale)
	{
		m_Actors = Collections.singleton(inActor);
		m_URL = inURL;
		m_Name = inName;
		m_IsGroupBlog = inIsGroupBlog;
		m_Locale = inLocale;
	}

	/****************************************************************************
	****************************************************************************/
	public Long getId()
	{
		return m_Id;
	}

	/****************************************************************************
	****************************************************************************/
	public void setId( Long inId)
	{
		m_Id = inId;
	}

	/****************************************************************************
	****************************************************************************/
	@Override public void addFeed(FeedIF x)
	{
		m_Feeds.add(x);
	}

	/****************************************************************************
	****************************************************************************/
	@Override public String getURL()
	{
		return m_URL;
	}

	/****************************************************************************
	****************************************************************************/
	public String getName()
	{
		return m_Name;
	}

	/****************************************************************************
	****************************************************************************/
	public boolean isGroupBlog()
	{
		return m_IsGroupBlog;
	}

	/****************************************************************************
	****************************************************************************/
	@Override public boolean equals( final Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if ( this == obj)
		{
			return true;
		}

		if (!(obj instanceof BlogIF))
		{
			return false;
		}

		final BlogIF other = (BlogIF) obj;

		if ((this.m_URL == null) ? (other.getURL() != null) : !this.m_URL.equals( other.getURL() ))
		{
			return false;
		}
		return true;
	}

	/****************************************************************************
	****************************************************************************/
	@Override public int hashCode()
	{
		int hash = 5;
		hash = 97 * hash + (this.m_URL != null ? this.m_URL.hashCode() : 0);
		return hash;
	}

	/****************************************************************************
	****************************************************************************/
	@Override public String toString()
	{
		if ( m_Id == null)
		{
			return "NEW Blog [url='" + m_URL + "']";
		}

		StringBuilder	sb = new StringBuilder();
		sb.append("Blog #").append(m_Id).append(" [url='").append(m_URL).append("']");
		return sb.toString();
	}
}
