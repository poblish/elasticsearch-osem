package org.compass.gps.device.hibernate.cascade.inheritance;

/**
 * @author Maurice Nicholson
 */
public class Location
{
	public Long id;
	public Long version;
	public String name;

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}
}