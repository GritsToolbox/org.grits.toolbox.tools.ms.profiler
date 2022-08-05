package org.grits.toolbox.tools.ms.profiler.om;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.preference.share.PreferenceEntity;
import org.grits.toolbox.core.preference.share.PreferenceReader;
import org.grits.toolbox.core.preference.share.PreferenceWriter;
import org.grits.toolbox.core.utilShare.XMLUtils;

/**
 * Storage object for a databases. This class corresponds to the XML file that
 * contains the database and can be (de)serialized from/to the XML file using
 * JAXB.
 *
 * @author lovina
 *
 */
@XmlRootElement(name = "preference")
public class DatabasePreference
{
	private String				CURRENT_VERSION		= "1.0";
	public static final String	PREFERENCE_NAME		= "org.grits.toolbox.tools.ms.profiler.preference.databases";
	private List<Database>		databaseList		= new ArrayList<Database>();
	private String				selectedDatabase	= null;

	@XmlAttribute(name = "selected")
	public String getSelectedDatabase()
	{
		return this.selectedDatabase;
	}
	
	public void setSelectedDatabase(String a_name)
	{
		this.selectedDatabase = a_name;
	}
	
	public List<Database> getDatabaseList()
	{
		return this.databaseList;
	}
	
	public void setDatabaseList(List<Database> dbList)
	{
		this.databaseList = dbList;
	}
	
	public static DatabasePreference loadPreference() throws UnsupportedVersionException, InvalidVersionException
	{
		PreferenceEntity preferenceEntity = PreferenceReader.getPreferenceByName(PREFERENCE_NAME);
		if (preferenceEntity == null)
		{
			return null;
		}
		if (preferenceEntity.getVersion().equals("1.0"))
		{
			DatabasePreference databases = (DatabasePreference) XMLUtils.getObjectFromXML(preferenceEntity.getValue(),
					DatabasePreference.class);
			return databases;
		}
		throw new InvalidVersionException(preferenceEntity.getVersion());
	}

	public boolean savePreferences()
	{
		PreferenceEntity preferenceEntity = new PreferenceEntity(PREFERENCE_NAME);
		preferenceEntity.setVersion(CURRENT_VERSION);
		String xmlString = XMLUtils.marshalObjectXML(this);
		preferenceEntity.setValue(xmlString);
		return PreferenceWriter.savePreference(preferenceEntity);
	}

}
