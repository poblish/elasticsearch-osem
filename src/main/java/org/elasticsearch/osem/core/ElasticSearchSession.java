/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.elasticsearch.osem.core;

import org.compass.core.CompassException;
import org.compass.core.CompassSearchSession;
import org.compass.core.CompassSession;
import org.compass.core.config.CompassSettings;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

/**
 *
 * @author andrewregan
 */
public class ElasticSearchSession implements CompassSession
{
	private final ObjectContext	m_Ctxt;
	private final Client		m_Client;

	private static final ESLogger	logger = Loggers.getLogger( ElasticSearchSession.class );

	/****************************************************************************
	****************************************************************************/
	public ElasticSearchSession( final ObjectContext inCtxt, final Client inClient)
	{
		m_Ctxt = inCtxt;

		m_Client = inClient;	// new TransportClient().addTransportAddress( new InetSocketTransportAddress("10.10.10.107", 9300));	// FIXME!
	}

	/****************************************************************************
	****************************************************************************/
	public static void createEntity( final CompassSession inSsn, final Object inEntity)
	{
		final ElasticSearchSession	theESsn = (ElasticSearchSession) inSsn;	// (AGR) FIXME

		theESsn.create(inEntity);
	}

	/****************************************************************************
	****************************************************************************/
	public static void saveEntity( final CompassSession inSsn, final Object inEntity)
	{
		final ElasticSearchSession	theESsn = (ElasticSearchSession) inSsn;	// (AGR) FIXME

		theESsn.save(inEntity);
	}

	/****************************************************************************
	****************************************************************************/
	public static void deleteEntity( final CompassSession inSsn, final Object inEntity)
	{
		final ElasticSearchSession	theESsn = (ElasticSearchSession) inSsn;	// (AGR) FIXME

		theESsn.delete(inEntity);
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public void create( final Object inEntity) throws CompassException
	{
		logger.debug("create() theSsn = " + this);

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

		final IndexResponse	theResponse = m_Client.prepareIndex( theIdx.toLowerCase(), "xxx", theId)
								.setSource( m_Ctxt.write(inEntity) )
								.execute()
								.actionGet();

		logger.debug("create() DONE: " + theResponse.type() + " #" + theResponse.id() + " @ v." + theResponse.version());
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

		final IndexResponse	theResponse = m_Client.prepareIndex( theIdx.toLowerCase(), "xxx", theId)
								.setSource( m_Ctxt.write(inEntity) )
								.execute()
								.actionGet();

		logger.debug("save() DONE: " + theResponse.type() + " #" + theResponse.id() + " @ v." + theResponse.version());
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

		logger.debug("delete() DONE: " + theResponse.type() + " #" + theResponse.id() + " @ v." + theResponse.version());
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
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void evict(String alias, Object id)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void evictAll()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void rollback() throws CompassException
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void commit() throws CompassException
	{
		throw new UnsupportedOperationException("Not supported yet.");
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
	public <T> T get(Class<T> clazz, Object id) throws CompassException
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
	public <T> T load(Class<T> clazz, Object id) throws CompassException
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
		throw new UnsupportedOperationException("Not supported yet.");
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