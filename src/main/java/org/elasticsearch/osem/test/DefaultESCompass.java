/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.elasticsearch.osem.test;

import javax.naming.NamingException;
import javax.naming.Reference;
import org.compass.core.Compass;
import org.compass.core.CompassException;
import org.compass.core.CompassIndexSession;
import org.compass.core.CompassSearchSession;
import org.compass.core.CompassSession;
import org.compass.core.config.CompassSettings;
import org.compass.core.events.RebuildEventListener;
import org.compass.core.spi.InternalCompass;
import org.elasticsearch.client.Client;
import org.elasticsearch.osem.core.ElasticSearchSession;
import org.elasticsearch.osem.core.ObjectContext;

/**
 *
 * @author andrewregan
 */
public class DefaultESCompass implements InternalCompass
{
	private final Client		m_Client;
	private final ObjectContext	m_Ctxt;
	private final CompassSettings	m_Settings;

	private boolean			m_IsClosed;	// FWIW...

	private static final long		serialVersionUID = 1L;

	/****************************************************************************
	****************************************************************************/
	public DefaultESCompass( final Client inClient, final ObjectContext inCtxt)
	{
		this( inClient, inCtxt, new DefaultCompassSettings());
	}

	/****************************************************************************
	****************************************************************************/
	public DefaultESCompass( final Client inClient, final ObjectContext inCtxt, final CompassSettings inSettings)
	{
		m_Client = inClient;
		m_Ctxt = inCtxt;
		m_Settings = inSettings;
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public ObjectContext getObjectContext()
	{
		return m_Ctxt;
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public Client getClient()
	{
		return m_Client;
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public CompassSearchSession openSearchSession() throws CompassException
	{
		return openSession();
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public CompassSession openSession() throws CompassException
	{
		return new ElasticSearchSession( m_Ctxt, m_Client);
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public CompassSettings getSettings()
	{
		return m_Settings;
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public boolean isClosed()
	{
		return m_IsClosed;
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public void close() throws CompassException
	{
		m_IsClosed = true;
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public void addRebuildEventListener(RebuildEventListener eventListener)
	{
		// NOOP
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public void start()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/****************************************************************************
	****************************************************************************/
	@Override
	public void stop()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public CompassSession openSession(boolean allowCreate, boolean checkClosed)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getName()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void removeRebuildEventListener(RebuildEventListener eventListener)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public CompassIndexSession openIndexSession() throws CompassException
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Compass clone(CompassSettings addedSettings)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void rebuild() throws CompassException
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Reference getReference() throws NamingException
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
}