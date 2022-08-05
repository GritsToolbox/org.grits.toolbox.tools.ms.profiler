package org.grits.toolbox.tools.ms.profiler.editColumns;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.grits.toolbox.tools.ms.profiler.dialog.ManageDatabaseDialog;
import org.grits.toolbox.tools.ms.profiler.om.SelectedFileViewer;

public class SampleNameEditingSupport extends EditingSupport
{
	private TableViewer			selectedFileViewer;
	private CellEditor			editor;
	private static final Logger	logger	= Logger.getLogger(StructureColumnEditingSupport.class);

	public SampleNameEditingSupport(TableViewer selectedFileViewer, ManageDatabaseDialog dialog)
	{
		super(selectedFileViewer);
		this.selectedFileViewer = selectedFileViewer;
		this.editor = new TextCellEditor(selectedFileViewer.getTable());
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
		return ((SelectedFileViewer) element).getSampleName();
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
		String oldStructure = ((SelectedFileViewer) element).getSampleName();
		if (!enteredStructure.equals(oldStructure))
		{
			((SelectedFileViewer) element).setSampleName(enteredStructure);
			selectedFileViewer.update(element, null);
			selectedFileViewer.refresh();
		}
	}
}
