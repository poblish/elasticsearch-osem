/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.elasticsearch.osem.test;

import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;

/**
 *
 * @author andrewregan
 */
public class DefaultCompassSettings extends CompassSettings
{
	/****************************************************************************
	****************************************************************************/
	public String getSetting(final String inSetting)
	{
		if (inSetting.equals( CompassEnvironment.CONNECTION ))
		{
			return "/tmp";
		}

		return super.getSetting(inSetting);
	}
}