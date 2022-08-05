package org.grits.toolbox.tools.ms.profiler.dialog;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.tools.ms.profiler.om.Database;
import org.grits.toolbox.tools.ms.profiler.preference.DatabasePreferencePage;
import org.grits.toolbox.tools.ms.profiler.util.Constants;

/**
 * ReplaceDatabaseName Class creates a new dialog when we try to save a database
 * with existing database name. This dialog tells the user that there is alreay
 * a database with same name and if he wants to save it with some other name.
 *
 * @author Lovina
 *
 */
public class ReplaceDatabaseName extends TitleAreaDialog
{
	private Text				databaseNameText	= null;
	private String				databaseName		= null;
	private Database			renamedDb			= new Database();
	List<Database>				tempdbList			= null;
	private static final Logger	logger				= Logger.getLogger(ReplaceDatabaseName.class);

	public ReplaceDatabaseName(Shell a_parent, Database a_importedDatabase, List<Database> a_dbList)
	{
		super(a_parent);
		this.renamedDb = a_importedDatabase;
		this.tempdbList = a_dbList;
	}

	/**
	 * This method sets the Title and the message of the dialog box.
	 */
	@Override
	public void create()
	{
		super.create();
		setTitle("\"Database with name : " + renamedDb.getName() + " already exist\"");
		setMessage("Enter some other name if you want to save it otherwise press cancel");
		logger.info("Finished setting the title and the message of the dialog box.");
	}

	/**
	 * Creates the dialog box
	 *
	 * @param a_parent
	 *            the parent dialog shell.
	 * @return composite the new created dialog area
	 */
	@Override
	protected Control createDialogArea(Composite a_parent)
	{
		logger.info("Creating the Dialog layout : createDialogArea");
		Composite area = (Composite) super.createDialogArea(a_parent);
		Composite container = new Composite(area, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		
		createFirstRow(container);
		
		return area;
	}

	/**
	 * createFirstRow Dialog creates the databaseName Labe and the Text area to
	 * enter the database name.
	 *
	 * @param container
	 *            composite to which we want to add the fields.
	 */
	private void createFirstRow(Composite container)
	{
		logger.info("Creating First Row : createFirstRow");
		Label nameLabel = new Label(container, SWT.NONE);
		nameLabel.setText("New Name for the database");

		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;

		databaseNameText = new Text(container, SWT.BORDER);
		databaseNameText.setLayoutData(gridData);
		// checks database name whenever it is changes to see if it already
		// exists.
		databaseNameText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent a_e)
			{
				logger.info("In the modify Listener for the databaseNameText field.");
				if (validateName())
				{
					setErrorMessage(null);
					getButton(IDialogConstants.OK_ID).setEnabled(true);
				}
			}
		});
	}
	
	/**
	 * This method checks whether the database name is not empty, is not
	 * duplicate and is with the allowed length.
	 *
	 * @return
	 */
	private boolean validateName()
	{
		logger.info("Validating Name : In the validateName method");
		DatabasePreferencePage object = new DatabasePreferencePage();
		String currentNameText = databaseNameText.getText();
		// check if name is empty
		if (currentNameText.trim().isEmpty())
		{
			setErrorMessage("Name cannot be empty");
			getButton(IDialogConstants.OK_ID).setEnabled(false);
			return false;
		}
		
		// check if database name is duplicate
		List<Database> allDatabaseNames = new ArrayList<Database>();
		allDatabaseNames = object.dbList;

		for (Database db : allDatabaseNames)
		{
			if (db.getName().equals(currentNameText))
			{
				setErrorMessage("Duplicate name: Database name have to be unique");
				
				getButton(IDialogConstants.OK_ID).setEnabled(false);
				return false;
			}
		}
		// check if the database name is within the limits
		if (currentNameText.trim().length() > Constants.DATABASE_NAME_LENGTH)
		{
			setErrorMessage("Name length cannot be greater then" + Constants.DATABASE_NAME_LENGTH);
			getButton(IDialogConstants.OK_ID).setEnabled(false);
			return false;
		}
		return true;
	}

	/**
	 * allows the dialog to be resized
	 *
	 * @return
	 */
	@Override
	protected boolean isResizable()
	{
		return true;
	}
	
	/**
	 * This method saves all the entries in the database object.
	 */
	private void saveInput()
	{
		logger.info("Saving the information");
		databaseName = databaseNameText.getText();
		renamedDb.setName(databaseName);
	}
	
	/**
	 * when OK button is pressed the dialog saves the input.
	 */
	@Override
	protected void okPressed()
	{
		logger.info("Saves the input when Ok pressed : okPressed");
		saveInput();
		super.okPressed();
	}

	/**
	 * This method returns the new renamed database.
	 *
	 * @return renamdDb the new database with new name which is unique.
	 */
	public Database getDatabase()
	{
		logger.info("Returning renamed database : getDatabase");
		return renamedDb;
	}

}
