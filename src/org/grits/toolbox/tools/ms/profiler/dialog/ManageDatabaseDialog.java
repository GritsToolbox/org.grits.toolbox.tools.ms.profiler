package org.grits.toolbox.tools.ms.profiler.dialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.tools.ms.profiler.editColumns.SampleNameEditingSupport;
import org.grits.toolbox.tools.ms.profiler.editColumns.ThresholdEditingSupport;
import org.grits.toolbox.tools.ms.profiler.excelOperations.ExcelExpoter;
import org.grits.toolbox.tools.ms.profiler.om.Database;
import org.grits.toolbox.tools.ms.profiler.om.DatabasePreference;
import org.grits.toolbox.tools.ms.profiler.om.InvalidVersionException;
import org.grits.toolbox.tools.ms.profiler.om.MassEntry;
import org.grits.toolbox.tools.ms.profiler.om.Peak;
import org.grits.toolbox.tools.ms.profiler.om.SelectedFileViewer;
import org.grits.toolbox.tools.ms.profiler.sorter.MyDbEntryComparator;
import org.grits.toolbox.tools.ms.profiler.util.Constants;
import org.grits.toolbox.tools.ms.profiler.wordOperations.WordExpoter;

/**
 * Dialog Box for selecting the data file, database and saving the result file.
 *
 * @author Lovina
 *
 */
public class ManageDatabaseDialog extends TitleAreaDialog
{
	private static final Logger			logger					= Logger.getLogger(ManageDatabaseDialog.class);
	private Label						datafileLabel			= null;
	// private Text datafileText = null;
	private Label						databaseLabel			= null;
	private Combo						databaseCombo			= null;
	private Label						resultFileLabel			= null;
	private Text						resultFileText			= null;
	// private Button dataFileButton = null;
	private Button						resultFileButton		= null;
	private Label						emptyLabel				= null;
	private Label						accuracyLabel			= null;
	private Text						accuracyText			= null;
	private Combo						accuracyCombo			= null;
	private String						dataFilePath			= null;
	private String						dataFileName			= null;
	private String						completeDataFilePath	= null;
	private String						resultFilepath			= null;
	private String						resultFileName			= null;
	private String						completeresultFilePath	= null;
	public List<Database>				dbList					= new ArrayList<Database>();
	private String						selectedDatabaseName	= null;
	private Database					selectedDatabase		= null;
	public List<Peak>					peakList				= new ArrayList<Peak>();
	private boolean						saved					= true;
	private Integer						indexColumnNumber		= null;
	private Integer						massColumnNumber		= null;
	private Integer						areaColumnNumber		= null;
	private TableViewer					selectedFileViewer		= null;
	private MyDbEntryComparator			comparator				= null;
	private Button						addFileButton			= null;
	private Button						removeFileButton		= null;
	private List<SelectedFileViewer>	t_list					= null;
	private List<Peak>					masterPeakList			= new ArrayList<Peak>();
	private Map<String, String>			sampleFileNameList		= new LinkedHashMap<String, String>();
	private Integer						totalPeak				= 0;

	/**
	 * default constructor used to create the dialog box and initialize the
	 * parent shell
	 *
	 * @param a_parentShell
	 */
	public ManageDatabaseDialog(Shell a_parentShell)
	{
		super(a_parentShell);
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
	 * This method sets the title and the message of the dialog.
	 */
	@Override
	public void create()
	{
		super.create();
		// sets the message and and the title of the dialog box
		setMessage("Please select the data file the corresponding database and the location to save it.");
		setTitle("Ms Annotate");
	}
	
	/**
	 * Creates the dialog box components.
	 */
	@Override
	protected Control createDialogArea(Composite a_parent)
	{
		logger.info("Creation of Dialog box started");
		Composite area = (Composite) super.createDialogArea(a_parent);
		Composite container = new Composite(area, SWT.NONE);
		GridLayout layout = new GridLayout();
		// sets the number of columns as 4
		layout.numColumns = 4;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		container.setLayoutData(gridData);
		container.setLayout(layout);

		// creates the three rows for the dialog box
		createFirstRow(container);
		createSelectedFileViewer(container);
		createAddRemoveButtons(container);
		createSecondRow(container);
		createThirdRow(container);
		createForthRow(container);
		logger.info("Done with creation of dialog box");

		// adding listeners to the buttons
		// when data file browse button is clicked this listener is triggered
		// which open a file open dialog.
		addFileButton.addSelectionListener(new SelectionListener()
		{
			
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent a_e)
			{
				StringBuffer allFilesSelected = new StringBuffer();
				logger.info("Data File Browse button clicked ");
				// create File open Dialog
				FileDialog dataFileBrowse = new FileDialog(getShell(), SWT.MULTI);
				@SuppressWarnings("rawtypes")
				List<String> files = new ArrayList();
				// set the text for the dialog
				dataFileBrowse.setText("Select the data file.");
				// set the extension for the dialog
				dataFileBrowse.setFilterExtensions(new String[] { "*.xlsx", ".xls", "*.*" });
				// open the dialog and if open is clicked what happens
				if (dataFileBrowse.open() != null)
				{
					String[] dataFileNames = dataFileBrowse.getFileNames();
					for (String dataFile : dataFileNames)
					{
						dataFilePath = dataFileBrowse.getFilterPath();
						completeDataFilePath = dataFilePath + "\\" + dataFile;
						SelectedFileViewer t_selectedFile = new SelectedFileViewer();
						t_selectedFile.setFileName(completeDataFilePath);
						t_selectedFile.setSampleName("Sample" + (t_list.size() + 1));
						t_selectedFile.setThreshold(0.0);
						t_list.add(t_selectedFile);
						selectedFileViewer.refresh();
						validateManageDbDialog();
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent a_e)
			{
				// TODO Auto-generated method stub
				
			}
		});

		removeFileButton.addSelectionListener(new SelectionListener()
		{
			
			@Override
			public void widgetSelected(SelectionEvent a_e)
			{
				logger.info("Remove button clicked");
				ISelection selection = selectedFileViewer.getSelection();
				
				if (selection != null && selection instanceof IStructuredSelection)
				{
					IStructuredSelection sel = (IStructuredSelection) selection;
					
					for (@SuppressWarnings("unchecked")
					Iterator<SelectedFileViewer> iterator = sel.iterator(); iterator.hasNext();)
					{
						SelectedFileViewer entryToRemove = iterator.next();
						@SuppressWarnings("unchecked")
						List<SelectedFileViewer> listSelectedFiles = (List<SelectedFileViewer>) selectedFileViewer
								.getInput();
						listSelectedFiles.remove(entryToRemove);
					}
					selectedFileViewer.refresh();
				}
				logger.info("Remove button operation completed");
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent a_e)
			{
				// TODO Auto-generated method stub

			}
		});

		// this action listener will allow to set the location to where we want
		// to save the result file.
		resultFileButton.addSelectionListener(new SelectionListener()
		{
			
			@Override
			public void widgetSelected(SelectionEvent a_e)
			{
				logger.info("Data File Browse button clicked ");
				// create file save dialog
				FileDialog resultFileBrowse = new FileDialog(getShell(), SWT.SAVE);
				// set the text for the dialog
				resultFileBrowse.setText("Where do you want to save the file.");
				// set filter names for the browse fialog
				resultFileBrowse.setFilterExtensions(new String[] { "*.xlsx", ".xls", "*.*" });
				// set the filter path
				resultFileBrowse.setFilterPath("c:\\");
				// ask before overwriting a file
				resultFileBrowse.setOverwrite(true);
				resultFileName = "resultFileTemp1";
				resultFileBrowse.setFileName(resultFileName);
				if (resultFileBrowse.open() != null)
				{
					resultFilepath = resultFileBrowse.getFilterPath();

					if (!resultFileName.equals(resultFileBrowse.getFileNames()))
					{
						resultFileName = resultFileBrowse.getFileName();
					}
					completeresultFilePath = resultFilepath + "\\" + resultFileName;
					resultFileText.setText(completeresultFilePath);
					validateManageDbDialog();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent a_e)
			{
				
			}
		});
		return a_parent;

	}

	/**
	 * Creating the two buttons: add and remove
	 *
	 * @param a_container
	 */
	private void createAddRemoveButtons(Composite a_container)
	{
		// TODO Auto-generated method stub
		GridData layoutData = new GridData();
		addFileButton = new Button(a_container, SWT.PUSH);
		addFileButton.setText("Add");
		layoutData.widthHint = Constants.BUTTON_WIDTH;
		addFileButton.setLayoutData(layoutData);
		
		layoutData = new GridData();
		removeFileButton = new Button(a_container, SWT.PUSH);
		removeFileButton.setText("Remove");
		layoutData.widthHint = Constants.BUTTON_WIDTH;
		removeFileButton.setLayoutData(layoutData);
		
		layoutData = new GridData();
		emptyLabel = new Label(a_container, SWT.NONE);
		emptyLabel.setText("");
		emptyLabel.setLayoutData(layoutData);
		
		layoutData = new GridData();
		emptyLabel = new Label(a_container, SWT.NONE);
		emptyLabel.setText("");
		emptyLabel.setLayoutData(layoutData);
	}
	
	/**
	 *
	 * @param a_container
	 */
	private void createSelectedFileViewer(Composite a_container)
	{
		// TODO Auto-generated method stub
		logger.info("Creating the preview selected file viewer");
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

		selectedFileViewer = new TableViewer(container2,
				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER_SOLID);
		createColumns(container2, selectedFileViewer);
		final Table table = selectedFileViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		selectedFileViewer.setContentProvider(new ArrayContentProvider());
		t_list = new ArrayList<>();
		selectedFileViewer.setInput(t_list);
		selectedFileViewer.refresh();

		selectedFileViewer.addDoubleClickListener(new IDoubleClickListener()
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
		layoutData.minimumHeight = 150;
		selectedFileViewer.getControl().setLayoutData(layoutData);
		// defines the method for sorting each column of the database table.
		// comparator = new MyDbEntryComparator();
		// selectedFileViewer.setComparator(comparator);
		
		// layoutData = new GridData();
		// emptyLabel = new Label(a_container, SWT.NONE);
		// emptyLabel.setText("");
		// emptyLabel.setLayoutData(layoutData);
		
		// return container2;
	}
	
	private Composite createColumns(Composite container2, TableViewer selectedFileViewer)
	{
		String[] titles = { "Selected Database File (Peak List)", "Sample Name", "Threshold" };
		int[] bounds = { 250, 250, 150 };
		TableViewerColumn colSelectedFile = createTableViewerColumn(titles[0], bounds[0], 0);
		logger.info("creating Selected Database File (Peak List) column");
		colSelectedFile.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				SelectedFileViewer e = (SelectedFileViewer) element;
				if (e.getFileName() == null)
				{
					return "";
				}
				else
				{
					return e.getFileName();
				}
				
			}
		});
		
		logger.info("creating Sample Name column");
		TableViewerColumn colSampleName = createTableViewerColumn(titles[1], bounds[1], 1);
		colSampleName.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				SelectedFileViewer e = (SelectedFileViewer) element;
				if (e.getSampleName() == null)
				{
					return "";
				}
				else
				{
					return e.getSampleName();
				}
				
			}
		});
		
		logger.info("Creating Threshold column");
		TableViewerColumn colThreshold = createTableViewerColumn(titles[2], bounds[2], 2);
		colThreshold.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				SelectedFileViewer e = (SelectedFileViewer) element;
				if (e.getThreshold() == null)
				{
					return "";
				}
				else
				{
					return e.getThreshold().toString();
				}
				
			}
		});

		colSampleName.setEditingSupport(new SampleNameEditingSupport(selectedFileViewer, this));
		colThreshold.setEditingSupport(new ThresholdEditingSupport(selectedFileViewer, this));
		// colGws.setEditingSupport(new
		// GwsColumnEditingSupport(selectedFileViewer, this));
		
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
		final TableViewerColumn viewerColumn = new TableViewerColumn(selectedFileViewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		// column.addSelectionListener(getSelectionAdapter(column, colNumber));
		return viewerColumn;
	}
	
	/**
	 * will create the DataFile label and the corresponding text field and the
	 * button to select the data file.
	 *
	 * @param a_container
	 *            the container to which we add all the controls i.e. the label,
	 *            text and the button.
	 */
	private void createFirstRow(Composite a_container)
	{
		GridData layoutData = new GridData();
		datafileLabel = new Label(a_container, SWT.NONE);
		datafileLabel.setText("Data File :");
		layoutData.widthHint = 100;
		datafileLabel.setLayoutData(layoutData);
		
	}
	
	/**
	 * will create the Database label and the corresponding combo.
	 *
	 * @param a_container
	 *            the container to which we add all the controls i.e. the label
	 *            and the combo.
	 */
	private void createSecondRow(Composite a_container)
	{
		GridData layoutData = new GridData();
		databaseLabel = new Label(a_container, SWT.NONE);
		databaseLabel.setText("Database :");
		layoutData.widthHint = 100;
		databaseLabel.setLayoutData(layoutData);
		
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		databaseCombo = new Combo(a_container, SWT.DROP_DOWN | SWT.READ_ONLY);
		layoutData.horizontalAlignment = GridData.FILL;
		layoutData.grabExcessHorizontalSpace = true;
		// layoutData.widthHint = 300;
		layoutData.horizontalSpan = 2;
		databaseCombo.setLayoutData(layoutData);

		if (!dbList.isEmpty())
		{
			populateDataBaseList();
			databaseCombo.setText(selectedDatabaseName);
			// selectedDatabaseName = maldiDbListCombo.getText();
			selectedDatabase = getDatabase(selectedDatabaseName);
		}
		
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		emptyLabel = new Label(a_container, SWT.NULL);
		emptyLabel.setText("");
		layoutData.grabExcessHorizontalSpace = true;
		emptyLabel.setLayoutData(layoutData);
	}
	
	/**
	 * will create the ResultFileFile label the corresponding text field and the
	 * button to save the result file.
	 *
	 * @param a_container
	 *            the container to which we add all the controls i.e. the label,
	 *            text and the button.
	 */
	private void createThirdRow(Composite a_container)
	{
		GridData layoutData = new GridData();
		resultFileLabel = new Label(a_container, SWT.NONE);
		resultFileLabel.setText("Result File :");
		layoutData.widthHint = 100;
		resultFileLabel.setLayoutData(layoutData);
		
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		resultFileText = new Text(a_container, SWT.BORDER | SWT.READ_ONLY);
		resultFileText.setEditable(false);
		layoutData.grabExcessHorizontalSpace = true;
		// layoutData.widthHint = 300;
		layoutData.horizontalSpan = 2;
		resultFileText.setLayoutData(layoutData);

		layoutData = new GridData();
		resultFileButton = new Button(a_container, SWT.PUSH);
		resultFileButton.setText("Browse");
		layoutData.widthHint = Constants.BUTTON_WIDTH;
		resultFileButton.setLayoutData(layoutData);
	}
	
	/**
	 * will create the Accuracy label the corresponding text field and the combo
	 * to select either dalton or ppm..
	 *
	 * @param a_container
	 *            the container to which we add all the controls i.e. the label,
	 *            text and the button.
	 */
	private void createForthRow(Composite a_container)
	{
		GridData layoutData = new GridData();
		accuracyLabel = new Label(a_container, SWT.NONE);
		accuracyLabel.setText("Accuracy :");
		layoutData.widthHint = 100;
		accuracyLabel.setLayoutData(layoutData);

		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		accuracyText = new Text(a_container, SWT.BORDER);
		accuracyText.setEditable(true);
		// layoutData.grabExcessHorizontalSpace = true;
		// layoutData.widthHint = 150;
		accuracyText.setLayoutData(layoutData);

		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		accuracyCombo = new Combo(a_container, SWT.DROP_DOWN | SWT.FILL);
		accuracyCombo.setItems(new String[] { "dalton", "ppm" });
		accuracyCombo.setText("ppm");
		layoutData.grabExcessHorizontalSpace = true;
		// layoutData.widthHint = 150;
		accuracyCombo.setLayoutData(layoutData);
		
		layoutData = new GridData();
		emptyLabel = new Label(a_container, SWT.NULL);
		emptyLabel.setText("");
		emptyLabel.setLayoutData(layoutData);
		
		accuracyText.addModifyListener(new ModifyListener()
		{
			
			@Override
			public void modifyText(ModifyEvent a_e)
			{
				validateManageDbDialog();
			}
		});
		
	}

	/**
	 * This method will check that all the controls of the dialog have valid and
	 * necessary information. It checks each control sequencially starting from
	 * top to bottom and left to right. First encoutered error is displayed
	 * first.
	 *
	 * @return true if all the controls have valid data else returns false.
	 */
	private boolean validateManageDbDialog()
	{
		// if (!this.validateDataFile())
		// {
		// return false;
		// }
		if (!this.validateDatabase())
		{
			return false;
		}
		if (!this.validateResultFile())
		{
			return false;
		}
		if (!this.validateAccuracyText())
		{
			return false;
		}
		if (!this.validateAccuracyDropDown())
		{
			return false;
		}
		return true;

	}
	
	/**
	 * validateDataFile method
	 *
	 * @return
	 */
	// private boolean validateDataFile()
	// {
	// logger.info("validating data file : In validateDataFile method");
	// String dataFileLocation = datafileText.getText();
	// if (!dataFileLocation.trim().isEmpty())
	// {
	// setErrorMessage(null);
	// getButton(IDialogConstants.OK_ID).setEnabled(true);
	// return true;
	// }
	// setErrorMessage("You need to select the data file first.");
	// getButton(IDialogConstants.OK_ID).setEnabled(false);
	// return false;
	// }
	
	private boolean validateDatabase()
	{
		logger.info("validating database : In validateDatabase method");
		String databaseName = databaseCombo.getText();
		if (!databaseName.trim().isEmpty())
		{
			setMessage(null);
			getButton(IDialogConstants.OK_ID).setEnabled(true);
			return true;
		}
		setErrorMessage("You need to select the database from the drop down first.");
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		return false;
	}
	
	private boolean validateResultFile()
	{
		String resultFileLocation = resultFileText.getText();
		if (!resultFileLocation.trim().isEmpty())
		{
			setMessage(null);
			getButton(IDialogConstants.OK_ID).setEnabled(true);
			return true;
		}
		setErrorMessage("You need to select the location for the result file first.");
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		return false;
	}
	
	private boolean validateAccuracyText()
	{
		Double accuracyValue = null;
		String accuracyString = null;
		try
		{
			accuracyString = accuracyText.getText();
			if (!accuracyString.trim().isEmpty())
			{
				accuracyValue = Double.parseDouble(accuracyString);
			}
			else
			{
				setErrorMessage("Enter the accuracy value it cannot be empty also the value has to be greater then 0.");
				getButton(IDialogConstants.OK_ID).setEnabled(false);
				return false;
			}
		}
		catch (NumberFormatException e)
		{
			setErrorMessage("Accuracy has to be an integer or a double value greater then 0.");
			getButton(IDialogConstants.OK_ID).setEnabled(false);
			logger.error("Error in the accuacy value: " + e.getMessage(), e);
			return false;
		}
		if (accuracyValue > 0)
		{
			setMessage(null);
			getButton(IDialogConstants.OK_ID).setEnabled(true);
			return true;
		}
		return false;
	}
	
	private boolean validateAccuracyDropDown()
	{
		String accuracyType = null;
		accuracyType = accuracyCombo.getText();
		if (!accuracyType.trim().isEmpty())
		{
			setMessage(null);
			getButton(IDialogConstants.OK_ID).setEnabled(true);
			return true;
		}
		return false;
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
		databaseCombo.setItems(list);
	}

	@Override
	public void okPressed()
	{
		// calls superOk only all the field are valid.
		if (validateManageDbDialog())
		{
			// if all the fields are valid it will save the list of peaks from
			// the excel datafile selected.
			boolean parsedAllSampleFile = readAllSampleFile();
			if (parsedAllSampleFile)
			{
				if (validatePeakEntries())
				{
					performEntryMatching();
					ExcelExpoter.writeResultFile(masterPeakList, completeresultFilePath, sampleFileNameList, totalPeak);
					WordExpoter.writeResultFIle(masterPeakList, completeresultFilePath, sampleFileNameList, totalPeak);
					logger.info("valiadated dialog successfully : okPressed");
					super.okPressed();
				}

			}
			else
			{
				saved = true;
				validateManageDbDialog();
			}
		}
	}
	
	/**
	 * This method takes all the file names from table viewer. Then for each
	 * file it checks if the first row has valid labels and for the rest of the
	 * excel rows it saves the peak entries in the master peak list.
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private boolean readAllSampleFile()
	{
		// TODO
		logger.info("Collecting the list of peaks for master peak list: readAllSampleFile");
		List<SelectedFileViewer> sampleFileList = (List<SelectedFileViewer>) selectedFileViewer.getInput();
		for (SelectedFileViewer sampleFile : sampleFileList)
		{
			sampleFileNameList.put(sampleFile.getSampleName(), "");
			String READ_DATA_FILE = sampleFile.getFileName();
			try
			{
				FileInputStream excelFile = new FileInputStream(new File(READ_DATA_FILE));
				@SuppressWarnings("resource")
				Workbook workbook = new XSSFWorkbook(excelFile);
				Sheet pekListSheet = workbook.getSheetAt(0);
				Iterator<Row> rowIterator = pekListSheet.iterator();
				int rowIndex = 0;
				while (rowIterator.hasNext())
				{
					Row currentRow = rowIterator.next();
					rowIndex = currentRow.getRowNum();
					Iterator<Cell> columnIterator = currentRow.iterator();
					Cell currentCell = columnIterator.next();
					boolean validFirstRow = true;
					switch (rowIndex)
					{
						case 0:
							validFirstRow = validateFirstRow(currentCell, columnIterator);
							break;
						
						default:
							if (validFirstRow)
							{
								if (saved == true)
								{
									boolean empty = checkifEmpty(currentCell, currentRow);
									if (!empty)
									{
										saveExcelEntries(currentRow, sampleFile.getSampleName());
									}
								}
							}
							break;
					}
				}
			}
			catch (FileNotFoundException e)
			{
				MessageDialog.openError(Display.getCurrent().getActiveShell(), "Cannotopen data file",
						"The requested data file does not exist at the given location.");
				logger.error("Requested file does not exist at the given location" + e);
				return false;
			}
			catch (IOException e)
			{
				logger.error("IOException while reading the data file" + e);
				return false;
			}
		}
		return saved;
	}
	
	/**
	 *
	 */
	private void performEntryMatching()
	{
		String accuracyType = accuracyCombo.getText();
		switch (accuracyType.toLowerCase())
		{
			case "ppm":
				double ppmPrecision = Double.parseDouble(accuracyText.getText());
				List<MassEntry> massentriesppm = selectedDatabase.getEntries();
				for (Peak t_peak : masterPeakList)
				{
					double tolerance = (t_peak.getCentroidMass() * ppmPrecision) / 1000000D;
					List<MassEntry> matchedEntries = new ArrayList<MassEntry>();
					for (MassEntry t_massentry : massentriesppm)
					{
						if (Math.abs(t_massentry.getMass() - t_peak.getCentroidMass()) < tolerance)
						{
							matchedEntries.add(t_massentry);
						}
					}
					t_peak.setAnnotation(matchedEntries);
				}
				break;
			case "dalton":
				double dalPrecision = Double.parseDouble(accuracyText.getText());
				List<MassEntry> massentriesdal = selectedDatabase.getEntries();
				for (Peak t_peak : masterPeakList)
				{
					double tolerance = dalPrecision;
					List<MassEntry> matchedEntries = new ArrayList<MassEntry>();
					for (MassEntry t_massentry : massentriesdal)
					{
						if (Math.abs(t_massentry.getMass() - t_peak.getCentroidMass()) < tolerance)
						{
							matchedEntries.add(t_massentry);
						}
					}
					t_peak.setAnnotation(matchedEntries);
				}
				break;
			default:
				break;
		}
	}

	/**
	 * This method validates the index column the mass and the area column.
	 *
	 * @return true if all the column entries are valid otherwise false.
	 */
	private boolean validatePeakEntries()
	{
		logger.info("Validating Data : In the validateData method.");
		if (!this.validateIndex())
		{
			return false;
		}
		if (!this.validateMass())
		{
			return false;
		}
		if (!this.validateArea())
		{
			return false;
		}
		return true;
	}

	/**
	 * This method makes sure that the Index value is greater then 0 and not
	 * empty.
	 *
	 * @return true if there is a value for area greater then 0 otherwise false.
	 */
	private boolean validateIndex()
	{
		logger.info("validating Index ");
		Integer t_index = null;
		for (Peak t_peakEntry : masterPeakList)
		{
			t_index = t_peakEntry.getIndex();
			if (t_index == null)
			{
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
						"Selected File has index which is empty ");
				return false;
			}
			if (t_index <= 0)
			{
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
						"Selected File has index wth value less then zero");
				return false;
			}
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
		logger.info("validating Mass ");
		Double t_mass = null;
		for (Peak t_peakEntry : masterPeakList)
		{
			t_mass = t_peakEntry.getCentroidMass();
			if (t_mass == null)
			{
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
						"Selected File has Mass which is empty ");
				return false;
			}
			if (t_mass <= 0)
			{
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
						"Selected File has Mass with value less then zero");
				return false;
			}
		}
		return true;
	}

	/**
	 * This method makes sure that the area value is greater then 0 and not
	 * empty.
	 *
	 * @return true if there is a value for area greater then 0 otherwise false.
	 */
	private boolean validateArea()
	{
		logger.info("validating Area ");
		Double t_area = null;
		for (Peak t_areaEntry : masterPeakList)
		{
			t_area = t_areaEntry.getCentroidMass();
			if (t_area == null)
			{
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
						"Selected File has Area which is empty ");
				return false;
			}
			if (t_area <= 0)
			{
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
						"Selected File has Area with value less then zero");
				return false;
			}
		}
		return true;
	}
	
	/**
	 * this method saves all the peak entries from the excel sheet into peakList
	 * object.
	 *
	 * @param currentRow
	 *            the current row
	 * @return true if saved all entres otherwise false.
	 */
	private boolean saveExcelEntries(Row currentRow, String sampleName)
	{
		
		try
		{
			Peak peakObj = new Peak();
			Iterator<Cell> columnIterator = currentRow.iterator();
			Integer indexTemp = null;
			Double massTemp = null;
			Double areaTemp = null;
			String indexString = null;
			String massString = null;
			String areaString = null;
			Cell currentCell = columnIterator.next();
			if (currentCell.getCellType() == CellType.STRING)
			{
				indexString = currentRow.getCell(indexColumnNumber).getStringCellValue();
				indexTemp = Integer.parseInt(indexString);
				peakObj.setIndex(indexTemp);

				massString = currentRow.getCell(massColumnNumber).getStringCellValue();
				massTemp = Double.parseDouble(massString);
				peakObj.setCentroidMass(massTemp);

				areaString = currentRow.getCell(areaColumnNumber).getStringCellValue();
				areaTemp = Double.parseDouble(areaString);
				Map<String, Double> t_Hashmap = new HashMap<String, Double>();
				t_Hashmap.put(sampleName, areaTemp);
				peakObj.setSampleNameAreaHashmap(t_Hashmap);
				addToMasterPeakList(peakObj, sampleName);
			}
			else
			{
				if (currentCell.getCellType() == CellType.NUMERIC)
				{
					indexString = currentRow.getCell(indexColumnNumber).getStringCellValue();
					indexTemp = Integer.parseInt(indexString);
					peakObj.setIndex(indexTemp);

					massString = currentRow.getCell(massColumnNumber).getStringCellValue();
					massTemp = Double.parseDouble(massString);
					peakObj.setCentroidMass(massTemp);

					areaString = currentRow.getCell(areaColumnNumber).getStringCellValue();
					areaTemp = Double.parseDouble(areaString);
					Map<String, Double> t_Hashmap = new HashMap<String, Double>();
					t_Hashmap.put(sampleName, areaTemp);
					peakObj.setSampleNameAreaHashmap(t_Hashmap);
					addToMasterPeakList(peakObj, sampleName);
				}
			}

			// peakList.add(peakObj);
		}
		catch (NoSuchElementException e)
		{
			logger.error("NoSuchElementException" + e);
		}
		catch (NumberFormatException e)
		{
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
					"The values for index mass and area have to be integer value.");
			saved = false;
			return false;
		}
		return true;
	}
	
	/**
	 * This method checks if the peakobjects area already exist in the master
	 * peaklist and if the area already exista then does not add it else it adds
	 * that peak to the masterpeaklist.
	 *
	 * @param a_peakObj
	 *            the peak which is read from excel file and needs to be added
	 *            to the master peaklist.
	 * @param a_sampleName
	 */
	private void addToMasterPeakList(Peak a_peakObj, String a_sampleName)
	{
		// TODO Auto-generated method stub
		Boolean peak_exist = false;
		Double area_peakToBeAdded = getAreaFromHashmap(a_peakObj);
		for (Peak t_peak : masterPeakList)
		{
			Double area_existingPeak = getAreaFromHashmap(t_peak);
			if ((area_existingPeak.equals(area_peakToBeAdded)))
			{
				peak_exist = true;
				Map<String, Double> t_Hashmap = new HashMap<String, Double>();
				t_Hashmap = t_peak.getSampleNameAreaHashmap();
				t_Hashmap.put(a_sampleName, area_existingPeak);
				t_peak.setSampleNameAreaHashmap(t_Hashmap);
			}
		}
		if (!peak_exist)
		{
			masterPeakList.add(a_peakObj);
			totalPeak++;
		}
		
	}
	
	/**
	 * This method takes the peak as input and extracts its area which is the
	 * the key in the hashmap and returns it.
	 *
	 * @param a_peakObj
	 *            the peak whoes area needs to be extracted from the hashmap.
	 * @return t_area the area from the hashmap associated with that particular
	 *         peak.
	 */
	private Double getAreaFromHashmap(Peak a_peakObj)
	{
		Map<String, Double> t_hashMap = a_peakObj.getSampleNameAreaHashmap();
		Map.Entry<String, Double> t_entry = t_hashMap.entrySet().iterator().next();
		Double t_area = t_entry.getValue();
		String t_value = t_entry.getKey();
		return t_area;
	}

	/**
	 * This method makes sure that the first row of the excel sheet has three
	 * main keywords i.e. index, mass and area.
	 *
	 * @param currentCell
	 *            the current cell which we are checking.
	 * @param columnIterator
	 *            Iterator used to go through all the columns in the excel
	 *            sheet.
	 */
	private boolean validateFirstRow(Cell currentCell, Iterator<Cell> columnIterator)
	{
		logger.info("In the validateFirstRow method");
		if (currentCell.getCellType() == CellType.STRING)
		{
			while (columnIterator.hasNext())
			{
				checkColumnName(currentCell);
				currentCell = columnIterator.next();
			}
			if (indexColumnNumber.equals(null))
			{
				saved = false;
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
						"Selected File does not have \"Index\" Label in the 1st Row");
				return false;
			}
			if (massColumnNumber.equals(null))
			{
				saved = false;
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
						"Selected File does not have \"Mass\" Label in the 1st Row");
				return false;
			}
			if (areaColumnNumber.equals(null))
			{
				saved = false;
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
						"Selected File does not have \"Area\" Label in the 1st Row");
				return false;
			}
		}
		else
		{
			saved = false;
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
					"Selected File has format error in 1st row");
			return false;
		}
		saved = true;
		return true;
	}
	
	/**
	 * This method checks whether the entire row is empty. If any particular
	 * cell among the three column has a value then it makes sure appropriate
	 * error message is displayed.
	 *
	 * @param currentCell
	 *            the current cell which is being checked.
	 * @param currentRow
	 *            the current row which we want to make sure is either
	 *            completely blank or has valid entry for all the three cells.
	 * @return true if entire row is empty or entire row has valid entries for
	 *         all the three columns.
	 * @throws NoSuchElementException
	 *             It is thrown when we try to get a cell or a row which does
	 *             not exist.
	 */

	public boolean checkifEmpty(Cell currentCell, Row currentRow) throws NoSuchElementException
	{
		logger.info("making sure there is some value for every index centroid mass and area.");
		int rowNumber = currentCell.getRowIndex();
		Row row = currentRow;
		Cell IndexCell = row.getCell(indexColumnNumber, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
		Cell massCell = row.getCell(massColumnNumber, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
		Cell areaCell = row.getCell(areaColumnNumber, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
		boolean blankIndex = blankAfterRemovingSpaces(IndexCell);
		boolean blankMass = blankAfterRemovingSpaces(massCell);
		boolean blankArea = blankAfterRemovingSpaces(areaCell);

		if (blankArea && blankIndex && blankMass)
		{
			return true;
		}
		if (!blankIndex && (blankMass || blankArea))
		{
			saved = false;
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
					"If there exists \"Index\" value at row number " + (rowNumber + 1)
							+ ". Please make sure there has to be a mass and area value. ");
			return true;
		}
		if (!blankMass && (blankIndex || blankArea))
		{
			saved = false;
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
					"If there exists \"mass\" value at row number " + (rowNumber + 1)
							+ ". Please make sure there has to be a index and area value. ");
			return true;
		}
		if (!blankArea && (blankMass || blankArea))
		{
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
					"If there exists \"area\" value at row number " + (rowNumber + 1)
							+ ". Please make sure there has to be a index and mass value. ");
			saved = false;
			return true;
		}
		if (!blankIndex && !blankMass && !blankArea)
		{
			saved = true;
			return false;
		}
		return saved;
	}
	
	/**
	 * checks if cell is empty after removing spaces
	 *
	 */
	private boolean blankAfterRemovingSpaces(Cell currentCell)
	{
		currentCell.setCellType(CellType.STRING);
		String cellContent = currentCell.getStringCellValue().trim();
		if (cellContent.isEmpty())
		{
			return true;
		}
		return false;
	}
	
	/**
	 * checks the column name and makes sure we have index mass and area. If
	 * found any of this columns then it gets their column number.
	 */
	private void checkColumnName(Cell currentCell)
	{
		
		if (currentCell.getCellType() == CellType.STRING)
		{
			// currentCell.getStringCellValue().equalsIgnoreCase("Index")
			String columnName = currentCell.getStringCellValue().toLowerCase();
			switch (columnName)
			{
				case "index":
					indexColumnNumber = currentCell.getColumnIndex();
					break;
				case "centroid mass":
					massColumnNumber = currentCell.getColumnIndex();
					break;
				case "area":
					areaColumnNumber = currentCell.getColumnIndex();
					break;
				default:
					break;
			}
		}
	}
}
