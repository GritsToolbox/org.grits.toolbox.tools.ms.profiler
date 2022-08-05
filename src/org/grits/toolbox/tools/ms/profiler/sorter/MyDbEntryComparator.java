package org.grits.toolbox.tools.ms.profiler.sorter;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.grits.toolbox.tools.ms.profiler.om.MassEntry;

public class MyDbEntryComparator extends ViewerComparator
{
	private int					propertyIndex;
	private static final int	DESCENDING	= 1;
	private int					direction	= DESCENDING;

	public MyDbEntryComparator()
	{
		this.propertyIndex = 0;
		direction = DESCENDING;
	}

	public int getDirection()
	{
		return direction == 1 ? SWT.DOWN : SWT.UP;
	}

	public void setColumn(int column)
	{
		if (column == this.propertyIndex)
		{
			// Same column as last sort; toggle the direction
			direction = 1 - direction;
		}
		else
		{
			// New column; do an ascending sort
			this.propertyIndex = column;
			direction = DESCENDING;
		}
	}

	@Override
	public int compare(Viewer viewer, Object o1, Object o2)
	{
		int rc = 0;
		try
		{
			MassEntry e1 = (MassEntry) o1;
			MassEntry e2 = (MassEntry) o2;
			switch (propertyIndex)
			{
				case 0:
					double m1 = e1.getMass();
					double m2 = e2.getMass();
					if (m1 == m2)
					{
						rc = 0;
					}
					else
					{
						rc = (int) (m1 - m2);
					}
					break;
				case 1:
					rc = e1.getStructure().compareTo(e2.getStructure());
					break;
				case 2:
					rc = e1.getGws().compareTo(e2.getGws());
					break;
				default:
					rc = 0;
			}
			// If descending order, flip the direction
			if (direction == DESCENDING)
			{
				rc = -rc;
			}
			return rc;

		}
		catch (NullPointerException e)
		{
			
		}
		return rc;
	}
}
