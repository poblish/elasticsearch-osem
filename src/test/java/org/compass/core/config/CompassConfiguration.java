/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.compass.core.config;

import org.compass.core.Compass;
import static org.easymock.EasyMock.*;

/**
 *
 * Purely for test purposes.
 * 
 * @author andrewregan
 */
public class CompassConfiguration
{
	public CompassConfiguration configure( final String inFileName)
	{
		return this;
	}

	public Compass buildCompass()
	{
		Compass		theCompass = createMock( Compass.class );

		return theCompass;
	}

	public CompassSettings getSettings()
	{
		CompassSettings		theCompassSettings = createMock( CompassSettings.class );

		return theCompassSettings;
	}
}