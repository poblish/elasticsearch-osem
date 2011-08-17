/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.elasticsearch.osem.test.entities.impl;

import org.elasticsearch.osem.annotations.Indexable;
import org.elasticsearch.osem.test.entities.interfaces.ProfileIF;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Temporal;
import org.elasticsearch.osem.annotations.Index;
import org.elasticsearch.osem.test.entities.interfaces.ActorIF;
import org.elasticsearch.osem.test.entities.interfaces.BlogIF;
import org.elasticsearch.osem.test.entities.interfaces.FeedIF;
import static org.elasticsearch.osem.test.entities.interfaces.FieldConstants.*;

/**
 *
 * @author andrewregan
 */
@Entity
public class Actor implements ActorIF, Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@Indexable
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name=ACTOR_ID)
	private Long					m_Id;

	@Column(name=ACTOR_USERNAME,unique=true)
	@Indexable(index=Index.NOT_ANALYZED)
	private String					m_UserName;

	@Column(name=ACTOR_FORENAMES)
	@Indexable
	private String					m_Forenames;

	@Column(name=ACTOR_SURNAME)
	@Indexable
	private String					m_Surname;

	@ManyToMany(/* cascade=CascadeType.ALL, */ fetch=FetchType.LAZY, targetEntity=Feed.class)
	@JoinTable(name="ActorNonBlogFeeds")
	@Indexable
	private Set<FeedIF>				m_NonBlogFeeds = new HashSet<FeedIF>();

	@ManyToMany(/* cascade=CascadeType.ALL, */ fetch=FetchType.LAZY, targetEntity=Blog.class)
	@JoinTable(name="ActorBlogs")
	@Indexable
	private Set<BlogIF>				m_Blogs = new HashSet<BlogIF>();

	@Column(name="activationDate", nullable=true)
	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	private Date					m_ActivationDate;

	@Column(name="emailAddress")
	// Not searchable!
	private String					m_EmailAddress;		// No, don't *think* this belongs in Profile

	/****************************************************************************
	****************************************************************************/
	public Actor()
	{
	}

	/****************************************************************************
	****************************************************************************/
	public Actor( final String inUserName, final String inForenames, final String inSurname, final String inEmailAddr,
			final ProfileIF inProfile)
	{
		m_UserName = inUserName;
		m_Forenames = inForenames;
		m_Surname = inSurname;
		m_EmailAddress = inEmailAddr;
	}

	/****************************************************************************
	****************************************************************************/
	public ProfileIF getProfile()
	{
		return null;
	}

	/****************************************************************************
	****************************************************************************/
	public Long getId()
	{
		return m_Id;
	}

	/****************************************************************************
	****************************************************************************/
	public Date getActivationDate()
	{
		return m_ActivationDate;
	}

	/****************************************************************************
	****************************************************************************/
	public boolean isActive()
	{
		return ( m_ActivationDate != null);
	}

	/****************************************************************************
	****************************************************************************/
	public String getEmailAddress()
	{
		return m_EmailAddress;
	}

	/****************************************************************************
	****************************************************************************/
	public boolean hasValidEmailAddress()
	{
		return true;
	}

	/****************************************************************************
	****************************************************************************/
	public void activate()
	{
		if ( m_ActivationDate == null)
		{
			m_ActivationDate = new Date();
		}
	}

	/****************************************************************************
	****************************************************************************/
	public String getUserName()
	{
		return m_UserName;
	}

	/****************************************************************************
	****************************************************************************/
	public void setUserName( final String inUserName)
	{
		m_UserName = inUserName;
	}

	/****************************************************************************
	****************************************************************************/
	public Set<FeedIF> getNonBlogFeeds()
	{
		return m_NonBlogFeeds;
	}

	/****************************************************************************
	****************************************************************************/
	public void setNonBlogFeeds( final Set<FeedIF> inFeeds)
	{
		m_NonBlogFeeds = inFeeds;
	}

	/****************************************************************************
	****************************************************************************/
	public boolean addNonBlogFeed( final FeedIF inFeed)
	{
		return m_NonBlogFeeds.add((Feed) inFeed);
	}

	/****************************************************************************
	****************************************************************************/
	public boolean addBlog( final BlogIF inBlog)
	{
		return m_Blogs.add(inBlog);
	}

	/****************************************************************************
	****************************************************************************/
	public Collection<BlogIF> getBlogs()
	{
		return m_Blogs;
	}

	/****************************************************************************
		FIXME
	****************************************************************************/
	public Locale getPreferredLocale()
	{
		return Locale.ENGLISH;
	}

	/****************************************************************************
	****************************************************************************/
	public String getNameForChannel()
	{
		return m_UserName;
	}

	/****************************************************************************
	****************************************************************************/
	public ActorIF getActor()
	{
		return this;
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public int hashCode()
	{
		int hash = 0;
		hash += ( m_UserName != null ? m_UserName.hashCode() : 0);
		return hash;
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public boolean equals( final Object object)
	{
		if ( object == null)
		{
			return false;
		}
		if ( this == object)
		{
			return true;
		}

		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof Actor))
		{
			return false;
		}
		Actor other = (Actor) object;
		if (( m_UserName == null && other.m_UserName != null) || ( m_UserName != null && !m_UserName.equals(other.m_UserName)))
		{
			return false;
		}
		return true;
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public String toString()
	{
		try
		{
			if ( m_Id == null)
			{
				return "NEW Actor: ['" + m_UserName + "', " + m_Blogs.size() + " blogs, " + m_NonBlogFeeds.size() + " feeds]";
			}

			return "Actor #" + m_Id + ": ['" + m_UserName + "', " + m_Blogs.size() + " blogs, " + m_NonBlogFeeds.size() + " feeds]";
		}
		catch (Throwable t)	// NOPMD. Yuk! But the last thing we want is to propagate lazy-instantiation problems.
		{
			return super.toString();
		}
	}
}
