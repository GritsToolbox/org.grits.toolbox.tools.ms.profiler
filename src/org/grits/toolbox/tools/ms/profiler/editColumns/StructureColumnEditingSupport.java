package org.grits.toolbox.tools.ms.profiler.editColumns;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.grits.toolbox.tools.ms.profiler.dialog.ManageDbEntries;
import org.grits.toolbox.tools.ms.profiler.om.MassEntry;

/**
 * StructureColumnEditingSupport allows the column to be editable when double
 * clicked. It validates user entered value in the cell and if valid then sets
 * it in the particular cell.
 *
 * @author Lovina
 *
 */
public class StructureColumnEditingSupport extends EditingSupport
{
	private TableViewer			databaseViewer;
	private CellEditor			editor;
	private ManageDbEntries		dialog;
	private static final Logger	logger	= Logger.getLogger(StructureColumnEditingSupport.class);
	
	public StructureColumnEditingSupport(TableViewer databaseViewer, ManageDbEntries dialog)
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
		return ((MassEntry) element).getStructure();
	}

	/**
	 * setValue method will get the user entered data from the cell and set it
	 * in the cell if the data is appropriate and valid.
	 */
	@Override
	protected void setValue(Object element, Object userInputValue)
	{
		logger.info("setting the structure value");
		String enteredStructure = String.valueOf(userInputValue);
		String oldStructure = ((MassEntry) element).getStructure();
		if (!enteredStructure.equals(oldStructure))
		{
			((MassEntry) element).setStructure(enteredStructure);
			databaseViewer.update(element, null);
			dialog.validateForm();
		}
	}
}
