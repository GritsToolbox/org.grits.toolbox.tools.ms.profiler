package org.grits.toolbox.tools.ms.profiler.om;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "database")
@XmlType(propOrder = { "name", "description", "entries" })
public class Database
{
	private String			name		= "";
	private String			description	= "";
	private List<MassEntry>	entries		= new ArrayList<MassEntry>();
	
	@XmlAttribute(name = "dname")
	public String getName()
	{
		return this.name;
	}
	
	public void setName(String a_name)
	{
		this.name = a_name;
	}
	
	@XmlElement(name = "description")
	public String getDescription()
	{
		return this.description;
	}
	
	public void setDescription(String a_description)
	{
		this.description = a_description;
	}
	
	@XmlElement(name = "entries")
	public List<MassEntry> getEntries()
	{
		return this.entries;
	}
	
	public void setEntries(List<MassEntry> a_entries)
	{
		this.entries = a_entries;
	}

}
