package org.grits.toolbox.tools.ms.profiler.dialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.grits.toolbox.tools.ms.profiler.editColumns.GwsColumnEditingSupport;
import org.grits.toolbox.tools.ms.profiler.editColumns.MassColumnEditingSupport;
import org.grits.toolbox.tools.ms.profiler.editColumns.StructureColumnEditingSupport;
import org.grits.toolbox.tools.ms.profiler.om.Database;
import org.grits.toolbox.tools.ms.profiler.om.MassEntry;
import org.grits.toolbox.tools.ms.profiler.sorter.MyDbEntryComparator;
import org.grits.toolbox.tools.ms.profiler.util.Constants;

/**
 * ManageDbEntries creates a dialog for the user to enter the details of the new
 * database which the user wants to create.
 *
 * @author Lovina
 *
 */
public class ManageDbEntries extends TitleAreaDialog
{
	private static final Logger	logger				= Logger.getLogger(ManageDbEntries.class);
	private Label				dbNameLabel			= null;
	private Text				dbNameText			= null;
	private Label				dbDescriptionLabel	= null;
	private Text				dbDescriptionText	= null;
	private TableViewer			databaseViewer		= null;
	private Database			database			= new Database();
	private List<Database>		preference			= new ArrayList<Database>();
	private Button				addButton			= null;
	private Button				removeButton		= null;
	private Button				browseGwsButton		= null;
	private List<MassEntry>		entries				= new ArrayList<MassEntry>();
	private MyDbEntryComparator	comparator			= null;
	private Integer				count				= 0;

	/**
	 * This constructor is used to create the new dialog when a new database
	 * needs to be created.
	 *
	 * @param a_parent
	 *            It is the parent shell
	 * @param dbList
	 *            It is the list of existing databases
	 */
	public ManageDbEntries(Shell a_parent, List<Database> dbList)
	{
		super(a_parent);
		this.preference = dbList;

	}
	
	/**
	 * This constructor is used to edit the existing database.
	 *
	 * @param a_parent
	 *            It is the parent shell
	 * @param dbList
	 *            It is the list of existing databases
	 * @param database
	 *            It is the database from the database list which the user wants
	 *            to edit.
	 */
	public ManageDbEntries(Shell a_parent, List<Database> dbList, Database database)
	{
		super(a_parent);
		this.preference = dbList;
		this.database = database;

	}
	
	/**
	 * This method sets the title and the message of the dialog.
	 */
	@Override
	public void create()
	{
		
		super.create();
		setMessage("Enter/Edit  Name, Description and entries of the table");
		setTitle("Create/Edit Database");
		
		// super.create();
		// setMessage("Perform the edits you wish to");
		// setTitle("Edit Database" + database.getName());

	}

	/**
	 * Creates all the components of the dialog which include the database name,
	 * description and the database entry table. Also a Browse button to select
	 * the gws sequence from enternal file. Add and Remove button to append a
	 * row to the database entry table or remove button to delete the row from
	 * the entry table.
	 *
	 * @param a_parent
	 *            It is the Parent Composite Holder
	 * @return the new Container which holds all the elements in that new/edit
	 *         dialog area.
	 */

	@Override
	protected Control createDialogArea(Composite a_parent)
	{
		logger.info("Creation of Dialog box started");
		Composite area = (Composite) super.createDialogArea(a_parent);
		Composite container = new Composite(area, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		container.setLayoutData(gridData);
		container.setLayout(layout);
		// creates all the visual components
		createFirstRow(container);
		createOtherTwoRows(container);
		createDatabaseViewer(container);
		createAddRemoveBrowseButtons(container);
		logger.info("Completed creation of all rows for the dialog");
		return area;

	}

	/**
	 * This method creates add remove and a browse button. Browse button to get
	 * a gws sequence from an external file. add botton to append a row to the
	 * table. and remove button to delete the selected row.
	 *
	 * @param a_container
	 *            It is the composite holder to which this three buttons are
	 *            added.
	 *
	 */
	private void createAddRemoveBrowseButtons(Composite a_container)
	{
		
		GridData layoutData = new GridData();
		Label emptyLabel1 = new Label(a_container, SWT.NONE);
		emptyLabel1.setText("");
		layoutData = new GridData();
		layoutData.widthHint = Constants.BUTTON_WIDTH;
		layoutData.heightHint = 30;
		emptyLabel1.setLayoutData(layoutData);

		// create a button and name it browse
		browseGwsButton = new Button(a_container, SWT.None);
		browseGwsButton.setText("Browse GWS");
		layoutData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		layoutData.widthHint = Constants.BUTTON_WIDTH;
		layoutData.verticalSpan = 1;
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
		layoutData.grabExcessVerticalSpace = true;
		browseGwsButton.setLayoutData(layoutData);

		// browsw a gws file and gets the contents of file to be placed in gws
		// cell if its valid
		browseGwsButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent a_e)
			{
				logger.info("browseGwsButton clicked");
				IStructuredSelection selection = databaseViewer.getStructuredSelection();

				if (selection.size() != 0)
				{
					final FileDialog browseGwsFileDialog = new FileDialog(getShell(), SWT.OPEN);
					browseGwsFileDialog.setText("Select the file contaning the Gws Structure");
					browseGwsFileDialog.setFilterExtensions(new String[] { "*.gws", "*.*", "*.gwp" });

					if (browseGwsFileDialog.open() != null)
					{
						String selectedFilePath = browseGwsFileDialog.getFilterPath();
						String selectedFileName = browseGwsFileDialog.getFileName();
						try
						{
							// It will first check if the selected file has only
							// one gws sequence length of the file if all the
							// validations are passed it will return the file
							// contents else this function will return null.
							String fileContent = validateBrowsedFile(selectedFilePath, selectedFileName);
							if (fileContent != null)
							{
								// validate the file content to check if it is
								// valid GWS sequence.
								validateGlycan(fileContent, selection);
							}
						}
						catch (FileNotFoundException e)
						{
							logger.error("Unable To find File", e);
						}
						catch (IOException e)
						{
							logger.error("Unable To Read  File", e);
						}
					}
				}
				else
				{
					setErrorMessage("You need to select row first");
					getButton(IDialogConstants.OK_ID).setEnabled(false);
				}
				logger.info("BrowseGwsButton operation completed");
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent a_e)
			{
				
			}
		});

		// create a button and name it add
		addButton = new Button(a_container, SWT.None);
		addButton.setText("Add");
		layoutData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		layoutData.widthHint = Constants.BUTTON_WIDTH;
		layoutData.verticalSpan = 1;
		layoutData.grabExcessHorizontalSpace = false;
		layoutData.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
		layoutData.grabExcessVerticalSpace = true;
		addButton.setLayoutData(layoutData);

		// adds a new row to the database tabel
		addButton.addSelectionListener(new SelectionListener()
		{
			
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				logger.info("Add button clicked");
				List<MassEntry> listOfEntries = (List<MassEntry>) databaseViewer.getInput();
				MassEntry newEntry = new MassEntry();
				newEntry.setMass(null);
				newEntry.setStructure("Enter all the details for new entry" + ++count);
				newEntry.setGws("");
				listOfEntries.add(newEntry);
				databaseViewer.refresh();
				for (TableItem tableItem : databaseViewer.getTable().getItems())
				{
					if (matchDocumentType((MassEntry) tableItem.getData(), newEntry))
					{
						databaseViewer.setSelection(new StructuredSelection(newEntry));
						databaseViewer.reveal(newEntry);
						break;
					}
				}
				logger.info("Add button operation completed");
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
			}
		});

		// create a button and names it remove
		removeButton = new Button(a_container, SWT.None);
		removeButton.setText("Remove");
		layoutData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		layoutData.widthHint = Constants.BUTTON_WIDTH;
		layoutData.verticalSpan = 1;
		layoutData.grabExcessHorizontalSpace = false;
		layoutData.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
		layoutData.grabExcessVerticalSpace = true;
		removeButton.setLayoutData(layoutData);

		// removes the selected row from the database table
		removeButton.addSelectionListener(new SelectionListener()
		{
			
			@Override
			public void widgetSelected(SelectionEvent a_e)
			{
				logger.info("Remove button clicked");
				ISelection selection = databaseViewer.getSelection();

				if (selection != null && selection instanceof IStructuredSelection)
				{
					IStructuredSelection sel = (IStructuredSelection) selection;

					for (@SuppressWarnings("unchecked")
					Iterator<MassEntry> iterator = sel.iterator(); iterator.hasNext();)
					{
						MassEntry entryToRemove = iterator.next();
						@SuppressWarnings("unchecked")
						List<MassEntry> listEntries = (List<MassEntry>) databaseViewer.getInput();
						listEntries.remove(entryToRemove);
					}
					databaseViewer.refresh();
				}
				logger.info("Remove button operation completed");
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent a_e)
			{
			}
		});
	}
	
	/**
	 * This method creates the database description label and a database
	 * description text field where the user enters the database description.
	 *
	 * @param a_container
	 *            Composite to which the two fiels are added.
	 */
	private void createOtherTwoRows(Composite a_container)
	{
		logger.info("creating database text and description field");
		Label dbDescriptionLabel = new Label(a_container, SWT.NONE);
		dbDescriptionLabel.setText("Description : ");
		GridData layoutData = new GridData();
		layoutData = new GridData(SWT.LEAD);
		layoutData.widthHint = 85;
		layoutData.heightHint = 30;
		layoutData.horizontalSpan = 3;
		dbDescriptionLabel.setLayoutData(layoutData);

		dbDescriptionText = new Text(a_container, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 4;
		layoutData.heightHint = 60;
		layoutData.grabExcessHorizontalSpace = true;
		dbDescriptionText.setLayoutData(layoutData);

		if (!database.getDescription().equals(""))
		{
			dbDescriptionText.setText(this.database.getDescription());
		}
		// Check whether the description is within the mentioned length.
		dbDescriptionText.addModifyListener(new ModifyListener()
		{
			
			@Override
			public void modifyText(ModifyEvent a_e)
			{
				logger.info("Database description Text  filed listner started");
				validateDescription();
				String currentDesText = ((Text) a_e.widget).getText();
				// Display error if database description is greater then the
				// maximum allowed length.
				if (currentDesText.trim().length() > Constants.DATABASE_DESCRIPTION_LENGTH)
				{
					setErrorMessage("Description cannot be more then" + Constants.DATABASE_DESCRIPTION_LENGTH);
					getButton(IDialogConstants.OK_ID).setEnabled(false);
					return;
				}
				setErrorMessage(null);
				getButton(IDialogConstants.OK_ID).setEnabled(true);
				logger.info("Database description Text  filed listner finished");
			}
		});
	}
	
	/**
	 * The database name label and the text field to enter the database name are
	 * created by this method. Whenever a change is made to the database name
	 * entire form is validated. i.e all the field is the dialog are validated
	 * so that user has to correct all the errors before saving the database.
	 *
	 * @param a_container
	 *            composite to which we add the database name label and the
	 *            database name text field.
	 * @return Composite which holds all the dialogue elements.
	 */
	private Composite createFirstRow(Composite a_container)
	{
		logger.info("Creating the Database name labe and text field");
		dbNameLabel = new Label(a_container, SWT.NONE);
		dbNameLabel.setText("Name : ");
		GridData layoutData = new GridData();
		layoutData.widthHint = 80;
		layoutData.heightHint = 30;
		dbNameLabel.setLayoutData(layoutData);

		dbNameText = new Text(a_container, SWT.BORDER);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 3;
		layoutData.grabExcessHorizontalSpace = true;
		dbNameText.setLayoutData(layoutData);

		if (!database.getName().equals(""))
		{
			dbNameText.setText(this.database.getName());
		}
		// validing entire form when a change is made to the name field.
		dbNameText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent a_e)
			{
				logger.info("Modify Listener for Database name label and text field started");
				validateForm();
				logger.info("Modify Listener for Database name label and text field finished");
			}
		});
		logger.info("finished creating the Database name labe and text field");
		return a_container;
	}

	/**
	 * Database table with three columns is created by this method.
	 *
	 * @param a_container
	 *            composite to which we add the database table
	 * @return container2 which is the composite holding the database table.
	 */
	private Composite createDatabaseViewer(Composite a_container)
	{
		logger.info("Creating the preview uneditable text field");
		Composite container2 = new Composite(a_container, SWT.NONE);
		GridLayout layout = new GridLayout();
		// layout.numColumns = 3;
		GridData layoutData = new GridData();
		layoutData.horizontalSpan = 4;
		layoutData.horizontalAlignment = GridData.FILL;
		layoutData.verticalAlignment = GridData.FILL;
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		container2.setLayoutData(layoutData);
		container2.setLayout(layout);

		databaseViewer = new TableViewer(container2,
				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER_SOLID);
		createColumns(container2, databaseViewer);
		final Table table = databaseViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		databaseViewer.setContentProvider(new ArrayContentProvider());
		List<MassEntry> t_list = new ArrayList<>();
		if (!(database.getEntries() == null))
		{
			List<MassEntry> t_entries = database.getEntries();
			t_list.addAll(t_entries);
		}
		else
		{
			t_list.addAll(database.getEntries());
		}
		databaseViewer.setInput(t_list);
		databaseViewer.refresh();

		databaseViewer.addDoubleClickListener(new IDoubleClickListener()
		{
			@Override
			public void doubleClick(DoubleClickEvent a_event)
			{
				@SuppressWarnings("unused")
				IStructuredSelection selection = (IStructuredSelection) a_event.getSelection();
			}
		});
		
		layoutData = new GridData();
		layoutData.verticalAlignment = GridData.FILL;
		layoutData.horizontalSpan = 3;
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.horizontalAlignment = GridData.FILL;
		layoutData.minimumHeight = 250;
		databaseViewer.getControl().setLayoutData(layoutData);
		// defines the method for sorting each column of the database table.
		comparator = new MyDbEntryComparator();
		databaseViewer.setComparator(comparator);

		return container2;

	}
	
	/**
	 * This method returns the tableViewer.
	 *
	 * @return TableViever
	 */
	public TableViewer getViewer()
	{
		logger.info("In getViewer method");
		return databaseViewer;
	}

	/**
	 * This method creates columns to the database viewer. with title and
	 * editing support for each column.
	 *
	 * @param container2
	 *            composite to which we make changes.
	 * @param a_databaseViewer
	 *            the table viewer to which column needs to be created
	 * @return composite containing the table created.
	 */
	private Composite createColumns(final Composite container2, TableViewer a_databaseViewer)
	{
		String[] titles = { "Mass(m/z)", "Structure", "Gws" };
		int[] bounds = { 270, 270, 300 };
		TableViewerColumn colMass = createTableViewerColumn(titles[0], bounds[0], 0);
		logger.info("Creating Mass column");
		colMass.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				MassEntry e = (MassEntry) element;
				if (e.getMass() == null)
				{
					return "";
				}
				else
				{
					return Double.toString(e.getMass());
				}

			}
		});

		logger.info("creating Structure column");
		TableViewerColumn colStructure = createTableViewerColumn(titles[1], bounds[1], 1);
		colStructure.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				MassEntry e = (MassEntry) element;
				return e.getStructure();
			}
		});

		logger.info("Creating Gws column");
		TableViewerColumn colGws = createTableViewerColumn(titles[2], bounds[2], 2);
		colGws.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				MassEntry e = (MassEntry) element;
				return e.getGws();
			}
		});

		colMass.setEditingSupport(new MassColumnEditingSupport(databaseViewer, this));
		colStructure.setEditingSupport(new StructureColumnEditingSupport(databaseViewer, this));
		colGws.setEditingSupport(new GwsColumnEditingSupport(databaseViewer, this));

		return container2;

	}

	/**
	 * This method creates individual column and set various attributes to the
	 * column
	 *
	 * @param title
	 *            The column Title
	 * @param bound
	 *            sets the column width
	 * @param colNumber
	 *            its the column number of the current column to be created
	 * @return viewer containing the newly created column
	 */
	private TableViewerColumn createTableViewerColumn(String title, int bound, int colNumber)
	{
		logger.info("Creating Column : In the createTableViewerColumn method");
		final TableViewerColumn viewerColumn = new TableViewerColumn(databaseViewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		column.addSelectionListener(getSelectionAdapter(column, colNumber));
		return viewerColumn;
	}

	/**
	 * This method is used to set the sorter for the individual columns. when
	 * clicked on the column title this method can sort the column in ascending
	 * as well as descending order.
	 *
	 * @param column
	 *            the column to which we want to set the sorter
	 * @param index
	 *            index of the column
	 *
	 */
	private SelectionListener getSelectionAdapter(final TableColumn column, final int index)
	{
		SelectionListener selectionAdapter = new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				logger.info("Setting sorter for each column : In the getSelectionAdapter method");
				comparator.setColumn(index);
				int dir = comparator.getDirection();
				databaseViewer.getTable().setSortDirection(dir);
				databaseViewer.getTable().setSortColumn(column);
				databaseViewer.refresh();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent a_e)
			{
				
			}
		};
		return selectionAdapter;
	}

	public void setFocus()
	{
		databaseViewer.getControl().setFocus();
	}
	
	/**
	 * Saves all the information from the dialog box into database object.
	 * Database object has database name which cannot be empty, description
	 * which can be empty and all the entries.
	 */
	@SuppressWarnings("unchecked")
	private void saveInput()
	{
		logger.info("Saving the database : In the SaveInput method.");
		database.setName(dbNameText.getText().trim());
		database.setDescription(dbDescriptionText.getText().trim());
		entries = (List<MassEntry>) databaseViewer.getInput();
		database.setEntries(entries);
	}
	
	/**
	 * when Ok button is pressed this button calls the save method.
	 */
	@Override
	protected void okPressed()
	{
		logger.info("Ok button clicked : In the ok Pressed method.");
		saveInput();
		super.okPressed();
	}
	
	/**
	 * Allows the dialog box to be resized when set to true otherwise does not
	 * allow when set to false.
	 *
	 * @return
	 */
	@Override
	protected boolean isResizable()
	{
		return true;
	}

	public Database getDatabase()
	{
		return database;
	}

	public void setDatabase(Database a_database)
	{
		this.database = a_database;
	}

	/**
	 * This method validates the entire dialog box and keeps displayings the
	 * first encountered error untill it is corrected.
	 *
	 */
	public boolean validateForm()
	{
		logger.info("validating Form :In the validateForm method of ManageDbEntries class");
		if (!this.validateNameText())
		{
			return false;
		}
		if (!this.validateDescription())
		{
			return false;
		}
		if (!this.validateData())
		{
			return false;
		}
		return true;
	}

	/**
	 * validateNameText method takes the content of the database name field and
	 * validates it. It makes sure that the content is not empty, not duplicate
	 * i.e database name does not already exist and the database length is less
	 * then the maximum allowed length. If the content passes all this test it
	 * returns true otherwise false.
	 *
	 * @return true if valid name otherwise false.
	 */
	private boolean validateNameText()
	{
		logger.info("Validating Database Name : In the validateNameText method.");
		String currentNameText = dbNameText.getText();

		// checks if empty
		if (currentNameText.trim().isEmpty())
		{
			setErrorMessage("Name cannot be empty");
			getButton(IDialogConstants.OK_ID).setEnabled(false);
			return false;
		}
		
		List<Database> allDatabaseNames = new ArrayList<Database>();
		allDatabaseNames = preference;
		allDatabaseNames.remove(database);
		
		// checks if database name already exist.
		for (Database db : allDatabaseNames)
		{
			if (db.getName().equals(currentNameText))
			{
				setErrorMessage("Duplicate name: Database name have to be unique");
				
				getButton(IDialogConstants.OK_ID).setEnabled(false);
				return false;
			}
		}
		// checks if database name length is within the allowed value.
		if (currentNameText.trim().length() > Constants.DATABASE_NAME_LENGTH)
		{
			setErrorMessage("Name length cannot be greater then" + Constants.DATABASE_NAME_LENGTH);
			getButton(IDialogConstants.OK_ID).setEnabled(false);
			return false;
		}
		setErrorMessage(null);
		getButton(IDialogConstants.OK_ID).setEnabled(true);
		return true;
	}

	/**
	 * This method is used to validate the description field. This can be empty
	 * but its length has to be less then the maximum description legth.
	 *
	 * @return true if the description length is less then the maximum allowed
	 *         legth otherwise false.
	 */
	private boolean validateDescription()
	{
		logger.info("validating Desccription : In the validateDescription method");
		String currentDesText = dbDescriptionText.getText();
		// checks the length
		if (currentDesText.trim().length() > Constants.DATABASE_DESCRIPTION_LENGTH)
		{
			setErrorMessage("Description cannot be more then" + Constants.DATABASE_DESCRIPTION_LENGTH);
			getButton(IDialogConstants.OK_ID).setEnabled(false);
			return false;
		}
		setErrorMessage(null);
		getButton(IDialogConstants.OK_ID).setEnabled(true);
		return true;
	}
	
	/**
	 * This method validates the mass column the structure and the gws column.
	 *
	 * @return true if all the column entries are valid otherwise false.
	 */
	private boolean validateData()
	{
		logger.info("Validating Data : In the validateData method.");
		if (!this.validateMass())
		{
			return false;
		}
		if (!this.validateStructure())
		{
			return false;
		}
		if (!this.validateGws())
		{
			return false;
		}
		return true;
	}

	/**
	 * This method makes sure that the mass value is greater then 0 and not
	 * empty.
	 *
	 * @return true if there is a value for mass greater then 0 otherwise false.
	 */
	private boolean validateMass()
	{
		logger.info("Validating Mass : In the validateMass method.");
		@SuppressWarnings("unchecked")
		List<MassEntry> massEntries = (List<MassEntry>) databaseViewer.getInput();
		Double enteredMass = null;
		
		// checks each entry in the mass column
		for (MassEntry t_massEntry : massEntries)
		{
			enteredMass = t_massEntry.getMass();
			// checks if mass is null
			if (enteredMass == null)
			{
				setErrorMessage("Mass cannot be empty enter value greater then 1 and less then "
						+ Constants.MAXIMUM_MASS_VALUE);
				getButton(IDialogConstants.OK_ID).setEnabled(false);
				return false;
			}
			// makes sure mass is greater then 1
			if (enteredMass <= 0)
			{
				setErrorMessage("Mass value must be greater then 0");
				getButton(IDialogConstants.OK_ID).setEnabled(false);
				return false;
			}
		}
		setMessage("");
		getButton(IDialogConstants.OK_ID).setEnabled(true);
		return true;
	}
	
	/**
	 * This method makes sure that the structure values are valid. Structure
	 * cannot be empty and structure length has to be within the mentioned
	 * limits.
	 *
	 * @return true if the structure values are present and their lengths are
	 *         within the allowed range.
	 */
	private boolean validateStructure()
	{
		logger.info("validating Structure : In he validateStructure method.");
		String t_structure = null;
		@SuppressWarnings("unchecked")
		List<MassEntry> massEntries = (List<MassEntry>) databaseViewer.getInput();
		// if (!this.checkDuplicateStruc())
		// {
		// return false;
		// }
		for (MassEntry t_massEntry : massEntries)
		{
			t_structure = t_massEntry.getStructure();
			
			// checks if structure value is empty
			if (t_structure.trim().isEmpty())
			{
				setErrorMessage("Structure Length Cannot be Zero");
				getButton(IDialogConstants.OK_ID).setEnabled(false);
				return false;
			}
			
			// checks if structure value is within the allowed range.
			if (t_structure.length() > Constants.MAXIMUM_STRUCTURE_LENGTH)
			{
				setErrorMessage("Structure Length Cannot be greater then " + Constants.MAXIMUM_STRUCTURE_LENGTH);
				getButton(IDialogConstants.OK_ID).setEnabled(false);
				return false;
			}
		}
		setMessage("");
		getButton(IDialogConstants.OK_ID).setEnabled(true);
		return true;
	}
	
	/**
	 * This method makes sure there are no two structures which are same.
	 *
	 * @return true if there exist same two values for structure otherwise
	 *         false.
	 */
	public boolean checkDuplicateStruc()
	{
		logger.info("Checking Duplicate structure : In the checkDuplicateStruc method.");
		@SuppressWarnings("unchecked")
		List<MassEntry> massEntries = (List<MassEntry>) databaseViewer.getInput();
		HashMap<String, Boolean> t_existingNames = new HashMap<>();
		String t_strucName = null;
		for (MassEntry t_massEntry : massEntries)
		{
			t_strucName = t_massEntry.getStructure();
			if (t_existingNames.get(t_strucName) == null)
			{
				t_existingNames.put(t_massEntry.getStructure(), Boolean.TRUE);
			}
			else
			{
				setErrorMessage("Duplicate structure name :" + t_strucName + ". Structure name has to be unique");
				getButton(IDialogConstants.OK_ID).setEnabled(false);
				return false;
			}
		}
		setMessage("");
		getButton(IDialogConstants.OK_ID).setEnabled(true);
		return true;
	}

	/**
	 * checks if the Gws value is valid, the Gws value can be null or empty.
	 *
	 * @return true if it is valid otherwise false.
	 */
	private boolean validateGws()
	{
		logger.info("validating Gws : In the validateGws method.");
		@SuppressWarnings("unchecked")
		List<MassEntry> massEntries = (List<MassEntry>) databaseViewer.getInput();
		String t_gws = null;
		for (MassEntry t_massEntry : massEntries)
		{
			t_gws = t_massEntry.getGws();
			Glycan t_glycan = Glycan.fromString(t_gws);
			if (t_glycan == null)
			{
				setErrorMessage("Invalid Gws sequence:\t\"" + t_glycan + "\"");
				getButton(IDialogConstants.OK_ID).setEnabled(false);
				return false;
			}
		}
		setMessage("");
		getButton(IDialogConstants.OK_ID).setEnabled(true);
		return true;
	}
	
	/**
	 * This method is used when a user click on browse button to select an
	 * external file that has Gws sequence. That external file needs to be
	 * validated to check if it has valid Gws sequence and only one valid
	 * sequence. Also the length of the file has to be within some mentioned
	 * limits.
	 *
	 * @param a_fileContent
	 *            content of the external file selected.
	 * @param a_selection
	 *            the GWS cell to which we need to place the Gws sequence to.
	 */

	private void validateGlycan(String a_fileContent, IStructuredSelection a_selection)
	{
		logger.info("Validating Glycan : in the validateGlycan method");
		Glycan t_glycan = Glycan.fromString(a_fileContent);
		if (t_glycan == null)
		{
			String errorMessage = "Invalid Glycan sequence in the selected file ";
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", errorMessage);
		}
		else
		{
			for (Object elem : a_selection.toList())
			{
				if (elem instanceof MassEntry)
				{
					Double currentMass = ((MassEntry) elem).getMass();
					((MassEntry) elem).setGws(a_fileContent);
					Double calculateMass = Glycan.fromString(a_fileContent).computeMass();
					if (calculateMass > 0 && calculateMass != currentMass)
					{
						String confirmMessage = "Do you want to replace the current Mass : " + currentMass
								+ " with new calculated Mass: " + calculateMass;
						boolean choice = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Confirm",
								confirmMessage);
						if (choice)
						{
							((MassEntry) elem).setMass(calculateMass);
						}
					}
				}
			}
			databaseViewer.refresh();
		}
	}

	/**
	 * This method is used when a user click on browse button to select an
	 * external file that has Gws sequence. That external file needs to be
	 * validated to check if it has valid Gws sequence and only one valid
	 * sequence. Also the length of the file has to be within some mentioned
	 * limits.
	 *
	 * @param a_selectedFilePath
	 *            path of the file which the user selects
	 * @param a_selectedFileName
	 *            file name of the file selected by the user
	 * @return filecontent if thhe content is valid otherwise null
	 * @throws IOException
	 */

	private String validateBrowsedFile(String a_selectedFilePath, String a_selectedFileName) throws IOException
	{
		logger.info("validating Browse File : In the validateBrowsedFile.");
		String completeFilePath = a_selectedFilePath + "\\" + a_selectedFileName;
		Path path = Paths.get(completeFilePath);
		File f = path.toFile();
		f.length();
		long fileSizeInBytes = f.length();

		if (fileSizeInBytes == 0)
		{
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "File Content Error",
					"Selected file is empty.");
			// setErrorMessage("Selected file is empty.");
			// getButton(IDialogConstants.OK_ID).setEnabled(false);
			return null;
		}

		if (fileSizeInBytes > Constants.MAXIMUM_FILE_SIZE)
		{
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "File Content Error",
					"File size is greater then 1 Mb.");
			// setErrorMessage("File size is greater then 1 Mb");
			// getButton(IDialogConstants.OK_ID).setEnabled(false);
			return null;
		}

		byte[] readData = Files.readAllBytes(path);
		String fileContent = new String(readData);

		if (fileContent.contains(";"))
		{
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "File Content Error",
					"File has more then one sequence.");
			// setErrorMessage("File has more then one sequence");
			// getButton(IDialogConstants.OK_ID).setEnabled(false);
			return null;
		}
		return fileContent;
	}

	/**
	 * checks if both the structure are same
	 *
	 * @param selectedEntry
	 *            current structure
	 * @param newEntry
	 *            the new structure
	 * @return true if both are same otherwise false.
	 */
	private boolean matchDocumentType(MassEntry selectedEntry, MassEntry newEntry)
	{
		logger.info("In the matchDocumentType method");
		boolean match = selectedEntry.getStructure().equals(newEntry.getStructure());
		return match;
	}
}
