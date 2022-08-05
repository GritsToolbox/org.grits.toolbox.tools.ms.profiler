package org.grits.toolbox.tools.ms.profiler.preference;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.grits.toolbox.tools.ms.profiler.om.MassEntry;

/**
 * DatabaseEntryComparator is used to sort the columns
 * 
 * @author Lovina
 *
 */
public class DatabaseEntryComparator extends ViewerComparator
{
	@Override
	public int compare(Viewer viewer, Object o1, Object o2)
	{
		int comparision = 0;
		String string1 = o1.toString();
		String string2 = o2.toString();
		if (o1 instanceof MassEntry)
		{
			string1 = ((MassEntry) o1).getStructure();
		}
		if (o2 instanceof MassEntry)
		{
			string2 = ((MassEntry) o2).getStructure();
		}
		comparision = string1.compareToIgnoreCase(string2);
		return comparision;
	}
}
