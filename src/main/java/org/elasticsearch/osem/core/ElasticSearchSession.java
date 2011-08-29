/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.elasticsearch.osem.core;

import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.compass.core.CompassException;
import org.compass.core.CompassSearchSession;
import org.compass.core.CompassSession;
import org.compass.core.config.CompassSettings;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 *
 * @author andrewregan
 */
public class ElasticSearchSession implements CompassSession
{
	private final ObjectContext			m_Ctxt;
	private final Client				m_Client;
//	private final ClassIndexNamingStrategyIF		m_NamingStrategy;

	private static final ESLogger			logger = Loggers.getLogger( ElasticSearchSession.class );

	/****************************************************************************
	****************************************************************************/
	public ElasticSearchSession( final ObjectContext inCtxt, final Client inClient)	//, final ClassIndexNamingStrategyIF inStrategy)
	{
		m_Ctxt = inCtxt;
		m_Client = inClient;
//		m_NamingStrategy = inStrategy;
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public void create( final Object inEntity) throws CompassException
	{
		logger.debug("create() for " + inEntity);	// theSsn = " + this);

		String		theId = m_Ctxt.getId(inEntity);

		if ( theId == null)
		{
			theId = m_Ctxt.getAttributeId(inEntity);

			if ( theId == null)
			{
				throw new RuntimeException("Null Id - cannot continue.");
			}
		}

		final String		theIdx = m_Ctxt.getType( inEntity.getClass() );
	//	final String		theIdx = m_NamingStrategy.toIndexName( inEntity.getClass() );

		final IndexResponse	theResponse = m_Client.prepareIndex( theIdx.toLowerCase(), "xxx", theId)
								.setSource( m_Ctxt.write(inEntity) )
								.execute()
								.actionGet();

		logger.debug("create() DONE: " + theIdx + "/" + theResponse.type() + " #" + theResponse.id() + " @ v." + theResponse.version());
	}

	/****************************************************************************
	 * FIXME: copy/paste from create()
	****************************************************************************/
	@Override
	public void save( final Object inEntity) throws CompassException
	{
		logger.debug("save() theSsn = " + this);

		String		theId = m_Ctxt.getId(inEntity);

		if ( theId == null)
		{
			theId = m_Ctxt.getAttributeId(inEntity);

			if ( theId == null)
			{
				throw new RuntimeException("Null Id - cannot continue.");
			}
		}

		final String		theIdx = m_Ctxt.getType( inEntity.getClass() );

		final IndexResponse	theResponse = m_Client.prepareIndex( theIdx, "xxx", theId)
								.setSource( m_Ctxt.write(inEntity) )
								.execute()
								.actionGet();

		logger.debug("save() DONE: " + theIdx + "/" + theResponse.type() + " #" + theResponse.id() + " @ v." + theResponse.version());
	}

	/****************************************************************************
	 * FIXME: largely copy/paste from create()
	****************************************************************************/
	@Override
	public void delete( final Object inEntity) throws CompassException
	{
		logger.debug("delete() theSsn = " + this);

		String		theId = m_Ctxt.getId(inEntity);

		if ( theId == null)
		{
			theId = m_Ctxt.getAttributeId(inEntity);

			if ( theId == null)
			{
				throw new RuntimeException("Null Id - cannot continue.");
			}
		}

		final String		theIdx = m_Ctxt.getType( inEntity.getClass() );

		final DeleteResponse	theResponse = m_Client.prepareDelete( theIdx.toLowerCase(), "xxx", theId).execute().actionGet();

		if (theResponse.notFound())
		{
			throw new RuntimeException("Could not find " + theIdx + " #" + theId + " to delete");
		}

		logger.debug("delete() DONE: " + theIdx + "/" + theResponse.type() + " #" + theResponse.id() + " @ v." + theResponse.version());
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public <T> T load( final Class<T> inClazz, final Object inId) throws CompassException
	{
		final String		theIdx = m_Ctxt.getType(inClazz);

	//	m_Client.admin().indices().prepareRefresh(theIdx).execute().actionGet();
		/* FIXME. Caution!!! */ logger.debug("load(): refresh got fails: " + m_Client.admin().indices().prepareRefresh(theIdx).execute().actionGet().getShardFailures());

		logger.debug("load(): WANT " + theIdx + " #" + inId);

		final SearchResponse	theSearchResponse = m_Client.prepareSearch(theIdx).setQuery( idsQuery("xxx").addIds( String.valueOf(inId) ) ).execute().actionGet();
		final SearchHits		theHits = theSearchResponse.getHits();

		logger.debug("load(): got " + theSearchResponse);

		if (theHits.getTotalHits() == 0)
		{
			return null;	// FIXME - dumb?
		}

		final SearchHit		theHit = theHits.iterator().next();
		final T			theObj = m_Ctxt.read(theHit);

		return theObj;
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public <T> T get( final Class<T> inClazz, final Object inId) throws CompassException
	{
		return load( inClazz, inId);
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public SearchHits find( final String inQuery)
	{
		// (AGR) FIXME. Need to *parse* inQuery

		/* FIXME. Caution!!! */ m_Client.admin().indices().prepareRefresh("_all").execute().actionGet();

		final SearchResponse theSearchResp = m_Client.prepareSearch("_all").setQuery( textQuery( "_all", inQuery) ).execute().actionGet();

		return theSearchResp.getHits();
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public void save(String alias, Object obj) throws CompassException
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public void create(String alias, final Object inEntity) throws CompassException
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setReadOnly()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isReadOnly()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void evict(Object obj)
	{
		logger.warn("evict(): *Ignoring* call to evict: " + obj);
	}

	@Override
	public void evict(String alias, Object id)
	{
		logger.warn("evict(): *Ignoring* call to evict " + alias + " #" + id);
	}

	@Override
	public void evictAll()
	{
		logger.warn("evict(): *Ignoring* call to evict all.");
	}

	@Override
	public void rollback() throws CompassException
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void commit() throws CompassException
	{
		// (AGR) (Deliberately) does nothing.
	}

	@Override
	public void close() throws CompassException
	{
		// (AGR) No, don't bloody do this... well, not unless we have some kind of pool of Clients...  m_Client.close();
	}

	@Override
	public boolean isClosed()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public CompassSettings getSettings()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public CompassSearchSession useLocalTransaction()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public <T> T get(Class<T> clazz, Object... ids) throws CompassException
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Object get(String alias, Object id) throws CompassException
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Object get(String alias, Object... ids) throws CompassException
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public <T> T load(Class<T> clazz, Object... ids) throws CompassException
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Object load(String alias, Object id) throws CompassException
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Object load(String alias, Object... ids) throws CompassException
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void flush() throws CompassException
	{
		logger.warn("flush(): *Ignoring* call to flush.");
	}

	@Override
	public void flushCommit(String... aliases) throws CompassException
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void delete(String alias, Object obj) throws CompassException
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void delete(String alias, Object... ids) throws CompassException
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void delete(Class clazz, Object obj) throws CompassException
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void delete(Class clazz, Object... ids) throws CompassException
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}
}