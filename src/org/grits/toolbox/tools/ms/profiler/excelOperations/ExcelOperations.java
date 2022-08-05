package org.grits.toolbox.tools.ms.profiler.excelOperations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.grits.toolbox.tools.ms.profiler.om.Database;
import org.grits.toolbox.tools.ms.profiler.om.MassEntry;
import org.grits.toolbox.tools.ms.profiler.util.Constants;

/**
 * ExcelOperation class is used to perfor reading and writing operation to the
 * excelfile. It reads the entries from the table and writes them to the excel
 * file or vice versa
 *
 * @author Lovina
 *
 */
public class ExcelOperations
{
	private Database			importDatabase	= null;
	private final String		READ_FILE_NAME	= "C:/Users/Mermaid/Dropbox/GRITS Maldi Profiler/Example data/Glycan list with GWS sequence.xlsx";
	private final String		WRITE_FILE_NAME	= "C:/Users/Mermaid/Dropbox/GRITS Maldi Profiler/Example data/WriteCopyTestNew.xlsx";
	private Database			db1				= new Database();
	private List<MassEntry>		excelEntries	= new ArrayList<MassEntry>();
	private List				labelsList		= new ArrayList<String>();
	private boolean				saved			= false;
	private static final Logger	logger			= Logger.getLogger(ExcelOperations.class);

	/**
	 * Reads the selected file and extracts the database name description and
	 * all the entries for mass structure and Gws.
	 *
	 * @param a_completeFilePath
	 *            selected file path
	 * @return true if all the contents are correct and valid otherwise false
	 */
	public boolean parseImportFile(String a_completeFilePath)
	{
		importDatabase = new Database();
		try
		{
			logger.info("Parsing the excel file : parseImportFile");
			FileInputStream excelFile = new FileInputStream(new File(a_completeFilePath));
			@SuppressWarnings("resource")
			Workbook workbook = new XSSFWorkbook(excelFile);
			Sheet glycanSequenceSheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = glycanSequenceSheet.iterator();
			int rowIndex = 0;
			while (rowIterator.hasNext())
			{
				Row currentRow = rowIterator.next();
				rowIndex = currentRow.getRowNum();
				Iterator<Cell> columnIterator = currentRow.iterator();
				Cell currentCell = columnIterator.next();

				switch (rowIndex)
				{
					case 0:
						validateFirstRow(currentCell, columnIterator);
						break;
					
					case 1:
						if (saved == true)
						{
							validateSecondRow(currentCell, columnIterator);
						}
						break;
					
					case 2:
						if (saved == true)
						{
							validateThirdRow(currentCell, columnIterator);
						}
						break;
					
					default:
						if (saved == true)
						{
							boolean empty = checkifEmpty(currentCell, currentRow);
							if (!empty)
							{
								saveExcelEntries(currentRow);
							}
						}
						break;
				}
			}

			if (saved)
			{
				saved = validateData();
				if (saved)
				{
					importDatabase.setEntries(excelEntries);
				}
			}
		}
		catch (NoSuchElementException e)
		{
			logger.error("NoSuchElementException" + e);
		}
		catch (FileNotFoundException e)
		{
			logger.error("FileNotFoundException" + e);
		}
		catch (IOException e)
		{
			logger.error("IOException" + e);
		}
		return saved;
	}

	/**
	 * WriteExcelFile takes the database object and write the database name,
	 * description and all the entries to the excel file.
	 *
	 * @param a_exportDatabase
	 *            the database we want to write to the excel file.
	 * @param a_exportFileName
	 *            the name of the excel file by which we want to save it.
	 * @param a_exportFilePath
	 *            the folder location where we want to save the excel file.
	 */
	public void WriteExcelFile(Database a_exportDatabase, String a_exportFileName, String a_exportFilePath)
	{
		logger.info("Writing the excel file : In WriteExcelFile method.");
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("GlycanSequence");
		int rowNum = 0;

		Row row = sheet.createRow(rowNum++);
		int colNum = 0;
		Cell currentCell = row.createCell(colNum++);
		currentCell.setCellValue("Name");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue(a_exportDatabase.getName());

		row = sheet.createRow(rowNum++);
		colNum = 0;
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("Description");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue(a_exportDatabase.getDescription());

		row = sheet.createRow(rowNum++);
		colNum = 0;
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("Mass");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("Structure");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("GWS");
		
		for (MassEntry entry : a_exportDatabase.getEntries())
		{
			row = sheet.createRow(rowNum++);
			colNum = 0;
			currentCell = row.createCell(colNum++);
			currentCell.setCellValue(entry.getMass());
			currentCell = row.createCell(colNum++);
			currentCell.setCellValue(entry.getStructure());
			currentCell = row.createCell(colNum++);
			currentCell.setCellValue(entry.getGws());
		}

		try
		{
			FileOutputStream outputStream = new FileOutputStream(a_exportFilePath + "/" + a_exportFileName);
			workbook.write(outputStream);
			workbook.close();
		}
		catch (FileNotFoundException e)
		{
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot export",
					"The process cannot access the file because it is being used by another process or open.");
		}
		catch (IOException e)
		{
			logger.error("IOException" + e);
		}
		
	}
	
	/**
	 * validateFirstRow will check the format of first row and make sure that
	 * there is name keyword and databaseName in the next column.
	 *
	 * @param currentCell
	 *            the active cell
	 * @param columnIterator
	 *            Iterator used to go through all the columns
	 */
	public void validateFirstRow(Cell currentCell, Iterator<Cell> columnIterator)
	{
		logger.info("validating First row : In validateFirstRow method.");
		if (currentCell.getStringCellValue().equalsIgnoreCase("name"))
		{
			if (columnIterator.hasNext())
			{
				Cell nameCell = columnIterator.next();
				String name = nameCell.getStringCellValue();
				if (validateName(name))
				{
					importDatabase.setName(name);
					saved = true;
				}
				else
				{
					saved = false;
				}
			}
		}
		else
		{
			saved = false;
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
					"Selected File either does not have database name in first row");
		}
	}
	
	/**
	 * validateName makes sure name is not emoty and is with it the mentioned
	 * range.
	 *
	 * @param name
	 *            the value we want to validate
	 * @return true if vaid otherwise false.
	 */
	private boolean validateName(String name)
	{
		logger.info("validating Name : In validateName method.");
		String currentNameText = name;
		if (currentNameText.trim().isEmpty())
		{
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
					"Selected File has a database name field which is empty.");
			return false;
		}
		if (currentNameText.trim().length() > Constants.DATABASE_NAME_LENGTH)
		{
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
					"Selected File has a database name whose length is more then: " + Constants.DATABASE_NAME_LENGTH);
			return false;
		}
		return true;
		
	}
	
	/**
	 * validateSecondRow method checks if there is a keyword description in the
	 * second row.
	 *
	 * @param currentCell
	 *            the active cell
	 * @param columnIterator
	 *            Iterator used to go through all the columns
	 */
	public void validateSecondRow(Cell currentCell, Iterator<Cell> columnIterator)
	{
		logger.info("In the validateSecondRow method");
		if (currentCell.getStringCellValue().equalsIgnoreCase("description"))
		{
			Cell descCell = columnIterator.next();
			if (validateDescription(descCell.getStringCellValue()))
			{
				importDatabase.setDescription(descCell.getStringCellValue());
				saved = true;
			}
			else
			{
				saved = false;
			}
		}
		else
		{
			saved = false;
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
					"Selected File does not have database description in second row");
		}
	}

	/**
	 * validateDescription makes sure the description length is not greater then
	 * maximum allwable.
	 *
	 * @param a_stringCellValue
	 *            the description text.
	 * @return true if valid otherwise false.
	 */
	private boolean validateDescription(String a_stringCellValue)
	{
		logger.info("In the validateDescription method");
		String currentDesText = a_stringCellValue;
		if (currentDesText.trim().length() > Constants.DATABASE_DESCRIPTION_LENGTH)
		{
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
					"Description in the imported file exceeds: " + Constants.DATABASE_DESCRIPTION_LENGTH);
			return false;
		}
		return true;
	}
	
	/**
	 * validateThirdRow makes sure there are three important keyword mass,
	 * structure and gws .
	 *
	 * @param currentCell
	 *            the active cell
	 * @param columnIterator
	 *            Iterator used to go through all the columns
	 */
	public void validateThirdRow(Cell currentCell, Iterator<Cell> columnIterator)
	{
		logger.info("In the validateThirdRow method");
		if (currentCell.getCellType() == CellType.STRING)
		{
			int columnIndex = 0;
			while (columnIndex < 2)
			{
				columnIndex = currentCell.getColumnIndex();
				switch (columnIndex)
				{
					case 0:
						if (!currentCell.getStringCellValue().equalsIgnoreCase("Mass"))
						{
							saved = false;
							MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
									"Selected File does not have \"mass\" Label in the 3rd Row 1st column");
						}
						break;
					case 1:
						if (!currentCell.getStringCellValue().equalsIgnoreCase("Structure"))
						{
							saved = false;
							MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
									"Selected File does not have \"structure\" Label in the 3rd Row 2nd column");
						}
						break;
					case 2:
						if (!currentCell.getStringCellValue().equalsIgnoreCase("Gws"))
						{
							saved = false;
							MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
									"Selected File does not have gws \"Label\" in the 3rd Row 3rd column");
						}
						break;
					default:
						break;
				}
				if (columnIterator.hasNext())
				{
					currentCell = columnIterator.next();
				}
			}
		}
		else
		{
			saved = false;
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
					"Selected File has format error in 3rd row");
		}
		
	}
	
	/**
	 * this method makes sure that gws can be empty but mass and structure are
	 * mandatory. Also if there is an entry for Gws cell then there has to be an
	 * entry for mass and structure.
	 *
	 * @param currentCell
	 *            active cell
	 * @param currentRow
	 *            active row
	 * @return true if there is mass and structure for gws.
	 * @throws NoSuchElementException
	 */
	public boolean checkifEmpty(Cell currentCell, Row currentRow) throws NoSuchElementException
	{
		logger.info("checking if there exist mass and structure value for every gws value");
		int rowNumber = currentCell.getRowIndex();
		Row row = currentRow;
		Cell massCell = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
		Cell strucCell = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
		Cell gwsCell = row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
		boolean blankMass = blankAfterRemovingSpaces(massCell);
		boolean blankStruc = blankAfterRemovingSpaces(strucCell);
		boolean blankGws = blankAfterRemovingSpaces(gwsCell);
		// boolean blankMass = massCell.getCellType() == Cell.CELL_TYPE_BLANK;
		// boolean blankStruc = strucCell.getCellType() == Cell.CELL_TYPE_BLANK;
		// boolean blankGws = gwsCell.getCellType() == Cell.CELL_TYPE_BLANK;
		if (blankGws && blankMass && blankStruc)
		{
			return true;
		}
		if (!blankMass && !blankStruc)
		{
			return false;
		}
		if (blankMass && !blankStruc)
		{
			saved = false;
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
					"Selected File has structure value but no mass value at row number :" + (rowNumber + 1));
			return false;
		}
		if (blankStruc && !blankMass)
		{
			saved = false;
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
					"Selected File has mass value but no structure value at row number :" + (rowNumber + 1));
			return false;
		}
		if (!blankGws)
		{
			if (blankMass)
			{
				saved = false;
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
						"Selected File has no mass value at row number :" + (rowNumber + 1));
				return false;
			}
			if (blankStruc)
			{
				saved = false;
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
						"Selected File has no structure value at row number :" + (rowNumber + 1));
				return false;
			}
		}
		return true;
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
	 * this method write the content of database object to the excel file.
	 *
	 * @param fileName
	 *            the name by which we want to save the excel file.
	 */
	public void WriteExcelFile(String fileName)
	{
		logger.info("In the WriteExcelFile");
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("GlycanSequence");
		int rowNum = 0;
		
		Row row = sheet.createRow(rowNum++);
		int colNum = 0;
		Cell currentCell = row.createCell(colNum++);
		currentCell.setCellValue("Name");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue(db1.getName());
		
		row = sheet.createRow(rowNum++);
		colNum = 0;
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("Description");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue(db1.getDescription());
		
		row = sheet.createRow(rowNum++);
		colNum = 0;
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("Mass");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("Structure");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("GWS");
		
		for (MassEntry entry : excelEntries)
		{
			row = sheet.createRow(rowNum++);
			colNum = 0;
			currentCell = row.createCell(colNum++);
			currentCell.setCellValue(entry.getMass());
			currentCell = row.createCell(colNum++);
			currentCell.setCellValue(entry.getStructure());
			currentCell = row.createCell(colNum++);
			currentCell.setCellValue(entry.getGws());
		}
		
		try
		{
			FileOutputStream outputStream = new FileOutputStream(WRITE_FILE_NAME);
			workbook.write(outputStream);
			workbook.close();
			
		}
		catch (FileNotFoundException e)
		{
			logger.error("FileNotFoundException" + e);
		}
		catch (IOException e)
		{
			logger.error("IOException" + e);
		}
		
	}

	/**
	 * save the entries from the each excel row into a list
	 *
	 * @param currentRow
	 *            the active row whoes entries we want to save
	 * @return true if saved else false.
	 */
	public boolean saveExcelEntries(Row currentRow)
	{
		try
		{
			MassEntry entryObj = new MassEntry();
			Iterator<Cell> columnIterator = currentRow.iterator();
			Double massTemp = null;
			String massString = null;
			String strucTemp = null;
			String gwsTemp = null;
			
			while (columnIterator.hasNext())
			{
				Cell currentCell = columnIterator.next();
				if (currentCell.getCellType() == CellType.STRING)
				{
					int colIndex = currentCell.getColumnIndex();
					switch (colIndex)
					{
						case 0:
							massString = currentCell.getStringCellValue();
							massTemp = Double.parseDouble(massString);
							entryObj.setMass(massTemp);
							break;
						case 1:
							strucTemp = currentCell.getStringCellValue();
							entryObj.setStructure(strucTemp);
							break;
						case 2:
							gwsTemp = currentCell.getStringCellValue();
							entryObj.setGws(gwsTemp);
							break;
					}
				}
				else
					if (currentCell.getCellType() == CellType.NUMERIC)
					{
						massTemp = currentCell.getNumericCellValue();
						entryObj.setMass(massTemp);
					}
			}
			excelEntries.add(entryObj);
			
		}
		catch (NoSuchElementException e)
		{
			logger.error("NoSuchElementException" + e);
		}
		catch (NumberFormatException e)
		{
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
					"There is an entry for mass with which is not a number");
			saved = false;
			return false;
		}
		return true;
	}

	public Database getDatabase()
	{
		return importDatabase;
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
	 * checks if the Gws value is valid, the Gws value can be null or empty.
	 *
	 * @return true if it is valid otherwise false.
	 */
	private boolean validateGws()
	{
		logger.info("validating Gws");
		String t_gws = null;
		for (MassEntry t_massEntry : excelEntries)
		{
			t_gws = t_massEntry.getGws();
			if (t_gws.trim().isEmpty())
			{
				return true;
			}
			else
			{
				Glycan t_glycan = Glycan.fromString(t_gws);
				if (t_glycan == null)
				{
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
							"There is an entry for Gws which is invalid");
					return false;
				}
			}
			
		}
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
		logger.info("validating Structure");
		String t_structure = null;
		// if (!checkDuplicateStruc())
		// {
		// return false;
		// }
		for (MassEntry t_massEntry : excelEntries)
		{
			t_structure = t_massEntry.getStructure();
			if (t_structure.trim().isEmpty())
			{
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
						"There is an entry for structure with length zero or empty cell in the excel file");
				return false;
			}
			if (t_structure.length() > Constants.MAXIMUM_STRUCTURE_LENGTH)
			{
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
						"There is an entry for structure with length greater then: "
								+ Constants.MAXIMUM_STRUCTURE_LENGTH
								+ " all entries for structure should be less then this value.");
				return false;
			}
		}
		return true;
	}

	/**
	 * This method makes sure there are no two structures which are same.
	 *
	 * @return true if there exist same two values for structure otherwise
	 *         false.
	 */
	private boolean checkDuplicateStruc()
	{
		logger.info("checking duplicate structure");
		HashMap<String, Boolean> t_existingNames = new HashMap<>();
		String t_strucName = null;
		for (MassEntry t_massEntry : excelEntries)
		{
			t_strucName = t_massEntry.getStructure();
			if (t_existingNames.get(t_strucName) == null)
			{
				t_existingNames.put(t_massEntry.getStructure(), Boolean.TRUE);
			}
			else
			{
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
						"There exist duplicate structure names ");
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
		Double enteredMass = null;
		for (MassEntry t_massEntry : excelEntries)
		{
			enteredMass = t_massEntry.getMass();
			if (enteredMass == null)
			{
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
						"Selected File has Mass which is empty ");
				return false;
			}
			if (enteredMass <= 0)
			{
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Cannot Import File",
						"Selected File has Mass with value less then zero");
				return false;
			}
		}
		return true;
	}

}
