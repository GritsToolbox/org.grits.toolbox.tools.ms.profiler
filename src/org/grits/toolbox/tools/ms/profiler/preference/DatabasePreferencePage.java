package org.grits.toolbox.tools.ms.profiler.preference;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eurocarbdb.application.glycanbuilder.GlycanRendererAWT;
import org.eurocarbdb.application.glycoworkbench.GlycanWorkspace;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.tools.ms.profiler.dialog.ManageDbEntries;
import org.grits.toolbox.tools.ms.profiler.dialog.ReplaceDatabaseName;
import org.grits.toolbox.tools.ms.profiler.excelOperations.ExcelOperations;
import org.grits.toolbox.tools.ms.profiler.om.Database;
import org.grits.toolbox.tools.ms.profiler.om.DatabasePreference;
import org.grits.toolbox.tools.ms.profiler.om.InvalidVersionException;
import org.grits.toolbox.tools.ms.profiler.util.Constants;

/**
 * Database Preference Class creates the preference named profile database and a
 * window which will allow user to create new, edit, remove, import and export
 * the MS profile database.
 */
public class DatabasePreferencePage extends PreferencePage
{
	private static final Logger	logger					= Logger.getLogger(DatabasePreferencePage.class);
	private Label				activeDbLabel			= null;
	private Combo				maldiDbListCombo		= null;
	private Button				editButton				= null;
	private Button				removeButton			= null;
	private Button				newButton				= null;
	private Button				importButton			= null;
	private Button				exportAllButton			= null;
	private Label				previewLabel			= null;
	private Text				uneditablePrevieText	= null;
	public List<Database>		dbList					= new ArrayList<Database>();
	private FileDialog			saveDialog				= null;
	@SuppressWarnings("unused")
	private GlycanWorkspace		m_gwb					= new GlycanWorkspace(null, false, new GlycanRendererAWT());
	private Database			exportDatabase			= null;
	private String				selectedDatabaseName	= null;
	private Database			selectedDatabase		= null;
	
	/**
	 * This methods sets the title for the database preference
	 *
	 * @param a_title
	 *            The title which we want to display for the database preference
	 *            page
	 */
	public DatabasePreferencePage(String a_title)
	{
		super(a_title);
		try
		{
			DatabasePreference pref = DatabasePreference.loadPreference();
			if (pref != null)
			{
				dbList = pref.getDatabaseList();
				selectedDatabaseName = pref.getSelectedDatabase();
				selectedDatabase = getDatabase(selectedDatabaseName);
			}
		}
		catch (UnsupportedVersionException | InvalidVersionException e)
		{
			logger.error(e.getMessage(), e);
		}
	}
	
	/**
	 * This methods sets the title for the database preference and image if we
	 * want to have one
	 *
	 * @param a_title
	 *            The title which we want to display for the database preference
	 *            page
	 * @param a_image
	 *            The image which we want to display for the database preference
	 *            page
	 */
	public DatabasePreferencePage(String a_title, ImageDescriptor a_image)
	{
		super(a_title, a_image);
	}
	
	public DatabasePreferencePage()
	{
		try
		{
			DatabasePreference pref = DatabasePreference.loadPreference();
			if (pref != null)
			{
				dbList = pref.getDatabaseList();
				selectedDatabaseName = pref.getSelectedDatabase();
			}
		}
		catch (UnsupportedVersionException | InvalidVersionException e)
		{
			logger.error(e.getMessage(), e);
		}
		
	}

	/**
	 * Creates all the components which include the Active profile label. Combo
	 * to Display the list of MS databases. Edit, Remove, New, Import and Export
	 * Buttons. And an uneditable text area which previews the selected database
	 * description.
	 *
	 * @param a_parent
	 *            It is the Parent Composite Holder
	 * @return returns the new Container which is the Composite Area
	 */
	@Override
	protected Control createContents(Composite a_parent)
	{
		
		Composite container = new Composite(a_parent, SWT.FILL);
		// Create 5 columns
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 5;
		container.setLayout(gridLayout);
		
		createFirstRow(container);
		createSecondRow(container);
		createThirdRow(container);
		createForthRow(container);
		createFifthRow(container);
		logger.info("Create Contents: finishes creating all the rows of the preference page");
		
		// selection listener for new button create a new database when new
		// button is clicked
		
		newButton.addSelectionListener(new SelectionListener()
		{
			
			@Override
			public void widgetSelected(SelectionEvent a_e)
			{
				logger.info("new Button Selection Listener started");
				ManageDbEntries newDialog = new ManageDbEntries(Display.getCurrent().getActiveShell(), dbList);
				if (newDialog.open() == Window.OK)
				{
					addDatabase(newDialog.getDatabase());
				}
				logger.info("new Button Selection Listener exited");
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent a_e)
			{
				
			}
		});
		
		// selection listener for edit button, edits selected database when edit
		// button is clicked. If no database is present in the maldi combo it
		// displays an error message.
		
		editButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent a_e)
			{
				logger.info("Edit Button Selection Listener started");
				String selection = maldiDbListCombo.getText();
				if (checkEmpty(selection))
				{
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Combo element error",
							"There is no database in the combo that can be edited");
				}
				else
				{
					Database dbToEdit = getDatabase(selection);
					ManageDbEntries editDialog = new ManageDbEntries(Display.getCurrent().getActiveShell(), dbList,
							dbToEdit);
					if (editDialog.open() == Window.OK)
					{
						if (databaseAlreadyExists(editDialog.getDatabase()))
						{
							dbList.remove(editDialog.getDatabase());
						}
						
						addDatabase(editDialog.getDatabase());
					}
				}
				logger.info("Edit Button Selection Listener exited");
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent a_e)
			{
				
			}
		});
		
		// selection listener for remove button, removes selected database by
		// asking the user to first confirm. If no database is present in the
		// maldi combo it
		// display an message.
		
		removeButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent a_e)
			{
				logger.info("Remove Button Selection Listener started");
				
				String selection = maldiDbListCombo.getText();
				if (checkEmpty(selection))
				{
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Remove",
							"There is no database in the combo that can be removed");
				}
				else
				{
					if (MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Confirm", "Please confirm"))
					{
						Database removeDatabase = getDatabase(selection);
						dbList.remove(removeDatabase);
						uneditablePrevieText.setText("");
						populateDataBaseList();
					}
				}
				logger.info("Remove Button Selection Listener exited");
				String comboItems[] = maldiDbListCombo.getItems();
				if (comboItems.length > 0)
				{
					maldiDbListCombo.setText(comboItems[0]);
					Database tempDatabase = getDatabase(comboItems[0]);
					uneditablePrevieText.setText(tempDatabase.getDescription());
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent a_e)
			{
				
			}
		});
		
		// selection listener for import button, imports the selected excel file
		// and saves it as a database object.
		
		importButton.addSelectionListener(new SelectionListener()
		{
			
			@Override
			public void widgetSelected(SelectionEvent a_e)
			{
				logger.info("Import Button Selection Listener started");
				boolean actionPerformed = false;
				Database importedDatabase = new Database();
				// creates a file dialog to select the excel file to import
				FileDialog importDatabase = new FileDialog(getShell(), SWT.OPEN);
				importDatabase.setText("Select the file to import");
				importDatabase.setFilterExtensions(new String[] { "*.xlsx", "*.*" });
				// if a file is selected then it performs the operations on the
				// selected file.
				if (importDatabase.open() != null)
				{
					String selectedFilePath = importDatabase.getFilterPath();
					String selectedFileName = importDatabase.getFileName();
					String completeFilePath = selectedFilePath + "\\" + selectedFileName;
					ExcelOperations exc = new ExcelOperations();
					// checks if the contents of the selected file is valid.
					actionPerformed = exc.parseImportFile(completeFilePath);
					// if the contents of the selected excel file are as
					// required then we import it
					if (actionPerformed)
					{
						importedDatabase = exc.getDatabase();
						// Before adding the database to the database list we
						// check if it already exists in the database list if
						// not then we add it to the list.
						if (databaseAlreadyExists(importedDatabase))
						{
							// If the database already exists in the database
							// list we ask the user if he wants to save it with
							// any other name and save it.
							ReplaceDatabaseName replaceDatabaseNameDialog = new ReplaceDatabaseName(
									Display.getCurrent().getActiveShell(), importedDatabase, dbList);
							if (replaceDatabaseNameDialog.open() == Window.OK)
							{
								Database renamedDatabase = replaceDatabaseNameDialog.getDatabase();
								addDatabase(renamedDatabase);
							}
						}
						else
						{
							addDatabase(importedDatabase);
						}
					}
				}
				logger.info("Import Button Selection Listener exited");
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent a_e)
			{
				
			}
		});
		
		// selection listener for export button it saves the selected database
		// in an excel file to the user specified location.
		
		exportAllButton.addSelectionListener(new SelectionListener()
		{
			
			@Override
			public void widgetSelected(SelectionEvent a_e)
			{
				logger.info("Export Button Selection Listener started");
				String selection = maldiDbListCombo.getText();
				if (checkEmpty(selection))
				{
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot export",
							"There is no database in the combo that can be exported");
				}
				else
				{
					ExcelOperations exc = new ExcelOperations();
					// creates a save dialog to select the location where to
					// save the database.
					saveDialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
					saveDialog.setFilterNames(new String[] { "Excel Workbook(*.xlsx)", "All Files (*.*)" });
					saveDialog.setFilterExtensions(new String[] { "*.xlsx", "*.*" });
					saveDialog.setFilterPath("c:\\");
					exportDatabase = getDatabase(selection);
					saveDialog.setFileName(exportDatabase.getName() + ".xlsx");
					saveDialog.setOverwrite(true);

					// saves the datase when okay button is pressed.
					if (saveDialog.open() != null)
					{
						String exportFileName = exportDatabase.getName() + ".xlsx";
						String exportFilePath = saveDialog.getFilterPath();
						// calls this method to write the database as an excel
						// file.
						exc.WriteExcelFile(exportDatabase, exportFileName, exportFilePath);
					}

				}
				logger.info("Export Button Selection Listener exited");
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent a_e)
			{
				
			}
		});
		
		// selection listener for maldi combo displays the selected database
		// description in the preview area.
		
		maldiDbListCombo.addSelectionListener(new SelectionListener()
		{
			
			@Override
			public void widgetSelected(SelectionEvent a_e)
			{
				logger.info("MaldiCombo Selection Listener started");
				for (Database database : dbList)
				{
					if (maldiDbListCombo.getText().equals(database.getName()))
					{
						uneditablePrevieText.setText(database.getDescription());
					}
				}
				logger.info("MaldiCombo Selection Listener exited");
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent a_e)
			{
				
			}
		});
		return container;
	}
	
	/**
	 * creates label named Active Database
	 *
	 * @param a_container
	 *            this is the dialog that holds the elements
	 */

	private void createFirstRow(Composite a_container)
	{
		// static label named activeDbLabel displayed using 5 columns
		activeDbLabel = new Label(a_container, SWT.NONE);
		activeDbLabel.setText("Active Database : ");
		GridData layoutData = new GridData();
		layoutData.horizontalSpan = 5;
		activeDbLabel.setLayoutData(layoutData);
	}

	/**
	 * creates the combo to display the drop down list of existing database and
	 * edit and the remove button.
	 *
	 * @param a_container
	 *            this is the dialog that holds the elements
	 */
	
	private void createSecondRow(Composite a_container)
	{
		// Combo named maldiDbListCombo as a dropBox extends and grabs excess
		// area when we resize the dialogue box
		GridData layoutData = new GridData();
		maldiDbListCombo = new Combo(a_container, SWT.DROP_DOWN | SWT.READ_ONLY);
		if (!dbList.isEmpty())
		{
			populateDataBaseList();
			maldiDbListCombo.setText(selectedDatabaseName);
			// selectedDatabaseName = maldiDbListCombo.getText();
			selectedDatabase = getDatabase(selectedDatabaseName);
		}

		layoutData = new GridData();
		layoutData.horizontalAlignment = GridData.FILL;
		layoutData.grabExcessHorizontalSpace = true;
		// layoutData.widthHint = 180;
		layoutData.heightHint = 30;
		layoutData.horizontalSpan = 3;
		maldiDbListCombo.setLayoutData(layoutData);

		// Button named editButton used to select the MS database which we want
		// to edit
		editButton = new Button(a_container, SWT.PUSH);
		layoutData = new GridData();
		layoutData.widthHint = Constants.BUTTON_WIDTH;
		layoutData.heightHint = 30;
		editButton.setText("Edit");
		editButton.setLayoutData(layoutData);

		// Button named removeButton used to select the MS database which we
		// want to delete
		removeButton = new Button(a_container, SWT.PUSH);
		layoutData = new GridData();
		layoutData.widthHint = Constants.BUTTON_WIDTH;
		layoutData.heightHint = 30;
		removeButton.setText("Remove");
		removeButton.setLayoutData(layoutData);
	}

	/**
	 * creates a new button import and an export button to create import and
	 * export database.
	 *
	 * @param a_container
	 *            the container to which we add all the controls.
	 */
	private void createThirdRow(Composite a_container)
	{
		GridData layoutData = new GridData();
		// Button named newButton used to create new MS database
		layoutData = new GridData();
		layoutData.widthHint = Constants.BUTTON_WIDTH;
		layoutData.heightHint = 30;
		newButton = new Button(a_container, SWT.PUSH);
		newButton.setText("new");
		newButton.setLayoutData(layoutData);
		
		// Button named importButton used to import the existing MS database
		// which we want to use
		layoutData = new GridData();
		layoutData.widthHint = Constants.BUTTON_WIDTH;
		layoutData.heightHint = 30;
		importButton = new Button(a_container, SWT.PUSH);
		importButton.setText("Import");
		importButton.setLayoutData(layoutData);
		
		// Button named exportAllButton used to select the MS database which we
		// want to export
		layoutData = new GridData();
		layoutData.widthHint = Constants.BUTTON_WIDTH;
		layoutData.heightHint = 30;
		exportAllButton = new Button(a_container, SWT.PUSH);
		exportAllButton.setText("Export All");
		exportAllButton.setLayoutData(layoutData);
		
		// Label named emptyLabel1 used to enter in a cell
		Label emptyLabel1 = new Label(a_container, SWT.NONE);
		emptyLabel1.setText("");
		layoutData = new GridData();
		layoutData.widthHint = Constants.BUTTON_WIDTH;
		layoutData.heightHint = 30;
		emptyLabel1.setLayoutData(layoutData);
		
		// Label named emptyLabel2 used to enter in a cell
		Label emptyLabel2 = new Label(a_container, SWT.NONE);
		emptyLabel2.setText("");
		layoutData = new GridData();
		layoutData.widthHint = 250;
		layoutData.heightHint = 30;
		emptyLabel2.setLayoutData(layoutData);
	}

	/**
	 * create the label named Preview.
	 *
	 * @param a_container
	 *            this is the dialog that holds the elements
	 */
	private void createForthRow(Composite a_container)
	{
		// Label named Preview placed above the uneditable text area
		GridData layoutData = new GridData();
		previewLabel = new Label(a_container, SWT.NONE);
		previewLabel.setText("Preview : ");
		layoutData = new GridData();
		layoutData.horizontalSpan = 5;
		layoutData.grabExcessHorizontalSpace = true;
		previewLabel.setLayoutData(layoutData);
	}
	
	/**
	 * creates the uneditable text area to preview the desciption of database
	 * selected.
	 *
	 * @param a_container
	 *            this is the dialog that holds the elements
	 */
	private void createFifthRow(Composite a_container)
	{
		// Uneditable Text area where we display the preview of the MS database
		// which we select from the Combo
		GridData layoutData = new GridData();
		uneditablePrevieText = new Text(a_container,
				SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
		uneditablePrevieText.setEditable(false);
		layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 5;
		layoutData.grabExcessHorizontalSpace = true;
		uneditablePrevieText.setLayoutData(layoutData);
		String activedb = maldiDbListCombo.getText();
		if (!activedb.trim().isEmpty())
		{
			Database activeDatabase = getDatabase(activedb);
			uneditablePrevieText.setText(activeDatabase.getDescription());
		}
		else
		{
			uneditablePrevieText.setText("");
		}
	}

	/**
	 * This method repopulates the combo with the modified database list to show
	 * any added database or removed database.
	 */
	public void populateDataBaseList()
	{
		String[] list = new String[dbList.size()];
		Integer counter = 0;
		for (Database database : dbList)
		{
			list[counter++] = database.getName();
		}
		maldiDbListCombo.setItems(list);
	}

	/**
	 * This methos takes the input as a database and checks in the databaseList
	 * if that database already exists.
	 *
	 * @return true if database exist in the database list otherwise false.
	 * @param database
	 *            database which needs to checked in the database list.
	 */
	private boolean databaseAlreadyExists(Database database)
	{
		for (Database db : dbList)
		{
			if (db.getName().equals(database.getName()))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * This method adds the database to the database list refreshes the combo to
	 * display the new database list. sets the active database name to the newly
	 * added database and sets the preview area to the database description.
	 *
	 * @param a_Database
	 */
	private void addDatabase(Database a_Database)
	{
		dbList.add(a_Database);
		populateDataBaseList();
		maldiDbListCombo.setText(a_Database.getName());
		uneditablePrevieText.setText(a_Database.getDescription());

	}

	/**
	 * This method check whether the given string is empty.
	 *
	 * @param a_selection
	 *            string which we want to check if empty
	 * @return true if string is empty or null otherwise false
	 */
	
	private boolean checkEmpty(String a_selection)
	{
		if (a_selection.trim().isEmpty())
		{
			return true;
		}
		if (a_selection.equals(null))
		{
			return true;
		}
		return false;
	}

	/**
	 * This method goes through the existing databse list and looks for a
	 * particular database
	 *
	 * @param a_selection
	 *            the database to be searched from the database list.
	 * @return database from the existing database list or null if the database
	 *         name does not match the given string.
	 */
	public Database getDatabase(String a_selection)
	{
		for (Database db : dbList)
		{
			if (db.getName().equalsIgnoreCase(a_selection))
			{
				return db;
			}
		}
		return null;
	}
	
	@Override
	public boolean performOk()
	{
		if (!dbList.isEmpty())
		{
			// populateDataBaseList();
			selectedDatabaseName = maldiDbListCombo.getText();
			selectedDatabase = getDatabase(selectedDatabaseName);
		}
		logger.info("ok pressed");
		logger.info("Errors on page : " + getErrorMessage());
		return getErrorMessage() == null ? saveValues() : false;
	}
	
	public boolean saveValues()
	{
		DatabasePreference dbPref = new DatabasePreference();
		dbPref.setSelectedDatabase(selectedDatabaseName);
		dbPref.setDatabaseList(dbList);
		return dbPref.savePreferences();
	}

	@Override
	protected void performApply()
	{
		if (!dbList.isEmpty())
		{
			// populateDataBaseList();
			selectedDatabaseName = maldiDbListCombo.getText();
			selectedDatabase = getDatabase(selectedDatabaseName);
			if (!selectedDatabaseName.trim().isEmpty())
			{
				uneditablePrevieText.setText(selectedDatabase.getDescription());
			}
			else
			{
				uneditablePrevieText.setText("");
			}

			logger.info("apply pressed");
			logger.info("Errors on page : " + getErrorMessage());
			saveValues();
		}
	}

	@Override
	protected void performDefaults()
	{
		try
		{
			DatabasePreference pref = DatabasePreference.loadPreference();
			if (pref != null)
			{
				dbList = pref.getDatabaseList();
				selectedDatabaseName = pref.getSelectedDatabase();
				selectedDatabase = getDatabase(selectedDatabaseName);
				populateDataBaseList();
				maldiDbListCombo.setText(selectedDatabaseName);
				if (!selectedDatabaseName.trim().isEmpty())
				{
					uneditablePrevieText.setText(selectedDatabase.getDescription());
				}
				else
				{
					uneditablePrevieText.setText("");
				}
				
			}
		}
		catch (UnsupportedVersionException | InvalidVersionException e)
		{
			logger.error(e.getMessage(), e);
		}
	}
}
