package org.grits.toolbox.tools.ms.profiler.om;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Entry")
@XmlType(propOrder = { "mass", "structure", "gws" })
public class MassEntry
{
	private Double	mass		= null;
	private String	structure	= null;
	private String	gws			= null;

	public MassEntry()
	{
		
	}
	
	public MassEntry(Double mass, String structure, String gws)
	{
		super();
		this.mass = mass;
		this.structure = structure;
		this.gws = gws;
	}

	@XmlAttribute(name = "mass")
	public Double getMass()
	{
		return this.mass;
	}
	
	public void setMass(Double a_mass)
	{
		this.mass = a_mass;
	}

	@XmlAttribute(name = "structure")
	public String getStructure()
	{
		return this.structure;

	}
	
	public void setStructure(String a_structure)
	{
		this.structure = a_structure;
	}

	@XmlAttribute(name = "gws")
	public String getGws()
	{
		return this.gws;
	}
	
	public void setGws(String a_gws)
	{
		this.gws = a_gws;
	}
	
}
