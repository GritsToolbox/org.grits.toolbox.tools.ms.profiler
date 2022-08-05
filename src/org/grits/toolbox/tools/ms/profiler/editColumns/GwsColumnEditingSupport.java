package org.grits.toolbox.tools.ms.profiler.editColumns;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Display;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.grits.toolbox.tools.ms.profiler.dialog.ManageDbEntries;
import org.grits.toolbox.tools.ms.profiler.om.MassEntry;

/**
 * GwsColumnEditingSupport allows the column to be editable when double clicked.
 * It validates user entered value in the cell and if valid then sets it in the
 * particular cell.
 *
 * @author Lovina
 *
 */
public class GwsColumnEditingSupport extends EditingSupport
{
	private TableViewer			databaseViewer;
	private CellEditor			editor;
	private ManageDbEntries		dialog;
	private static final Logger	logger	= Logger.getLogger(GwsColumnEditingSupport.class);

	public GwsColumnEditingSupport(TableViewer databaseViewer, ManageDbEntries dialog)
	{
		super(databaseViewer);
		this.databaseViewer = databaseViewer;
		this.editor = new TextCellEditor(databaseViewer.getTable());
		this.dialog = dialog;
	}

	@Override
	protected CellEditor getCellEditor(Object element)
	{
		return editor;
	}

	/**
	 * makes the cell editable when set to true.
	 */
	@Override
	protected boolean canEdit(Object element)
	{
		return true;
	}
	
	/**
	 * returns the content from the particular cell
	 */
	@Override
	protected Object getValue(Object element)
	{
		return ((MassEntry) element).getGws();
	}

	/**
	 * setValue method will get the user entered data from the cell and set it
	 * in the cell if the data is appropriate and valid.
	 */
	@Override
	protected void setValue(Object element, Object userInputValue)
	{
		logger.info("setting the Gws value.");
		boolean valid = false;
		String enteredGws = String.valueOf(userInputValue);
		Double currentMass = ((MassEntry) element).getMass();
		((MassEntry) element).setGws(enteredGws);
		databaseViewer.update(element, null);
		Glycan t_glycan = Glycan.fromString(enteredGws);
		valid = dialog.validateForm();
		if (valid)
		{
			((MassEntry) element).setGws(enteredGws);
			double calculateMass = t_glycan.computeMass();
			if (calculateMass > 0 && calculateMass != currentMass)
			{
				String confirmMessage = "DO you want to replace the current Mass : " + currentMass
						+ " with new calculated Mass: " + calculateMass;
				boolean choice = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Confirm",
						confirmMessage);
				if (choice)
				{
					((MassEntry) element).setMass(calculateMass);
				}
			}
		}
		databaseViewer.update(element, null);
	}

}
