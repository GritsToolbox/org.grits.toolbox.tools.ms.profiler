package org.grits.toolbox.tools.ms.profiler.handler;

import javax.inject.Named;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.tools.ms.profiler.dialog.ManageDatabaseDialog;

/**
 * Handler class for the creation of a dialig box. This class will open the
 * dialog box to collect from user data file database and to save the result
 * file.
 *
 * @author Lovina
 *
 */
public class ManageDbEntriesHandler
{
	private static final Logger logger = Logger.getLogger(ManageDbEntriesHandler.class);

	/**
	 * Method to create the database dialog open the dialog and allow the user
	 * to select data file, database and result file.
	 *
	 * @param a_shell
	 *            active shell that can be used to create dialogs (injected)
	 */
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell a_shell, EPartService a_partService,
			EModelService a_modelService, MApplication a_application)
	{
		logger.info("Started: ManageDbEntries handler started.");
		try
		{
			// create the dialog
			ManageDatabaseDialog newDialog = new ManageDatabaseDialog(Display.getCurrent().getActiveShell());
			// open the dialog
			if (newDialog.open() == Window.OK)
			{
				
			}
		}
		catch (Exception e)
		{
			logger.fatal("Error starting ManageDbEntriesHandler: " + e.getMessage(), e);
			MessageDialog.openError(a_shell, "Error starting ManageDbEntriesHandler",
					"Unable run ManageDbEntriesHandler due to an error:\n\n" + e.getMessage());
		}
		logger.info("Finshed Execution:  ManageDbEntries handler.");
	}
	
}
