/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.elasticsearch.osem.test.entities.impl;

/**
 *
 * @author andrewregan
 */
import org.elasticsearch.osem.test.entities.interfaces.ActorIF;
import org.elasticsearch.osem.test.entities.interfaces.ActorResourceIF;
import org.elasticsearch.osem.test.entities.interfaces.ArticleIF;
import org.elasticsearch.osem.test.entities.interfaces.BlogIF;
import org.elasticsearch.osem.test.entities.interfaces.FeedIF;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import org.hibernate.annotations.CollectionOfElements;
import org.elasticsearch.osem.annotations.Index;
import org.elasticsearch.osem.annotations.Searchable;
import org.elasticsearch.osem.annotations.Indexable;
import static org.elasticsearch.osem.test.entities.interfaces.FieldConstants.*;

/**
 *
 * @author andrewregan
 */
@Searchable
@Entity
public class TestArticle implements ArticleIF, Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@Indexable
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name=ARTICLE_ID)
	protected Long				id;

	@ManyToOne( cascade={CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REFRESH}, targetEntity=Blog.class)
	@JoinColumn(name="blogId")
	@Indexable
	protected BlogIF			m_Blog;

	@Column(name="title")
	@Indexable(indexName=INDEX_TITLE_COLUMN /*, analyzer=SNOWBALL_ANALYZER_EN */)
	protected String			m_Title;

	@Column(name="url")
	@Indexable(indexName=INDEX_URL_COLUMN)
	protected String			m_URL;

	@Lob
	@Column(name=INDEX_CONTENT_COLUMN)
//	@SearchableProperty( converter="htmlString", indexName=INDEX_CONTENT_COLUMN, analyzer=SNOWBALL_ANALYZER_EN)
	@Indexable(indexName=INDEX_CONTENT_COLUMN)
	protected String			m_Content;

	@Column(name="pubDate")
	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	protected Calendar			m_PublishDate = Calendar.getInstance( TimeZone.getTimeZone("UTC") );

	@Column(name=ARTICLE_SEMANTICALLY_ANALYSED,columnDefinition="TINYINT(1) DEFAULT 0",nullable=false)
	private boolean				m_SemanticallyAnalysed = false;

	////////////////////////////////////////////////////////////////////////////////////

	@Column(name="readCount",columnDefinition="MEDIUMINT DEFAULT 0",nullable=false)
	@Indexable(indexName=ARTICLE_READ_COUNT)
	private Integer				m_ReadCount = 0;

	////////////////////////////////////////////////////////////////////////////////////

	@CollectionOfElements
	@JoinTable(name="Article_ContentURLs")
	@Column(name="linkURLs", nullable=false)
	@Indexable(indexName="articleLinkURLs", index=Index.NOT_ANALYZED)
	protected final Set<String>		m_ContentURLs = new HashSet<String>();

	@Column(name="gotURLs",columnDefinition="TINYINT(1) DEFAULT 0",nullable=false)
	private boolean				m_GotContentURLs = false;

	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////

	@ManyToOne( /* cascade=CascadeType.ALL, */ /* This breaks Compass... fetch=FetchType.LAZY, */ targetEntity=Feed.class)
	@JoinColumn(name="feedId")
	@Indexable	// @SearchableComponent
	private FeedIF				m_ArticleFeed;


	public TestArticle() {}

	/****************************************************************************
	****************************************************************************/
	public Long getId()
	{
		return id;
	}

	/****************************************************************************
	****************************************************************************/
	public void setId( final Long id)
	{
		this.id = id;
	}

	/****************************************************************************
	****************************************************************************/
	public String getContent()
	{
		return m_Content;
	}

	/****************************************************************************
	****************************************************************************/
	public void setContent( final String inContent)
	{
		m_Content = inContent;

		// calculateContentURLs();
	}

	/****************************************************************************
	****************************************************************************/
	public String getTitle()
	{
		return m_Title;
	}

	/****************************************************************************
	****************************************************************************/
	public void setTitle( final String inTitle)
	{
		m_Title = inTitle;
	}

	/****************************************************************************
	****************************************************************************/
	public String getURL()
	{
		return m_URL;
	}

	public ActorResourceIF getResource()
	{
		return m_ArticleFeed;
	}

	public boolean isFromGroupResource()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public int getNumPotentialAuthors()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean isMultiAuthored()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean isPotentiallyAuthoredBy(ActorIF inActor)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public ActorIF getSingleAuthor()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public long getAge(TimeUnit inUnits)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public int getNumFavouriteFlags()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean isAFavouriteOf(ActorIF inActor)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public int getNumSimpleFlags()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean isFlaggedBy(ActorIF inActor)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public int getNumAssertionFlags()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Collection<String> getAssertionSnippets()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public int getReadCount()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean hasBeenSemanticallyAnalysed()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean hasContentURLs()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Collection<String> getContentURLs()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

/*
	public ArticleIF getPrevious(EntityManager inEM)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public ArticleIF getNext(EntityManager inEM)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public ArticleIF getLast(EntityManager inEM)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Collection<CategoryIF> categories()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Collection<TagIF> tags()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Iterable<FavouriteFlagIF> getFavouriteFlags()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}
	public Locale getContentLocale()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public ArticleVersionIF getParent()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Collection<ArticleVersionIF> getVersions()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Collection<ArticleRatingIF> getRatings()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Collection<JurisdictionIF> getJurisdictions()
	{
		return m_Jurisdictions;
	}

	public void setJurisdictions(Collection<JurisdictionIF> inColl)
	{
		m_Jurisdictions = inColl;
	}

	public Iterable<SimpleFlagIF> getSimpleFlags()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Iterable<AssertionFlagIF> getAssertionFlags()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Collection<ArticleVersionIF> getAllVersions(boolean inRecursive)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Iterable<TopicResultIF> getSemanticTopicResults()
	{
		return Collections.emptyList();	// FIXME
	}

	public void addCategory(CategoryIF inCat)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void addTag(TagIF inTag)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void addFlag(FlagIF inFlag)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void setSemanticTopicResults(TopicResultsIF inResults)
	{
		// NOOP
	}

*/
	public int getNumVersions()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public int getNumVersions(boolean inRecursive)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public double getAverageRating()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean incomingLinkMatchesOurs(String inURL)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Date getPublishDate()
	{
		return m_PublishDate.getTime();
	}

	public void setPublishDate(Date inDate)
	{
		m_PublishDate.setTime(inDate);
	}

	public void setResource(ActorResourceIF inRes)
	{
		m_ArticleFeed = (FeedIF) inRes;
	}

	public void setURL(String inURL)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public ArticleIF incrementReadCount()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void setSemanticallyAnalysed()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void calculateContentURLs()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/****************************************************************************
	****************************************************************************/
	@Override public String toString()
	{
		if ( id == null)
		{
			return "NEW TestArticle [title='" + m_Title + "']";
		}

		StringBuilder	sb = new StringBuilder();
		sb.append("TestArticle #").append(id).append(" [title='").append(m_Title).append("']");
		return sb.toString();
	}
}