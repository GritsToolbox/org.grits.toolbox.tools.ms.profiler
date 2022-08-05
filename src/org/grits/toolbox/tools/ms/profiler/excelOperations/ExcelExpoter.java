package org.grits.toolbox.tools.ms.profiler.excelOperations;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eurocarbdb.application.glycanbuilder.GraphicOptions;
import org.grits.toolbox.tools.ms.profiler.om.MassEntry;
import org.grits.toolbox.tools.ms.profiler.om.Peak;
import org.grits.toolbox.utils.data.CartoonOptions;
import org.grits.toolbox.utils.image.GlycanImageProvider;
import org.grits.toolbox.utils.image.GlycanImageProvider.GlycanImageObject;
import org.grits.toolbox.utils.io.ExcelWriterHelper;

public class ExcelExpoter
{
	public List<Peak>						peakList				= new ArrayList<Peak>();
	public static XSSFWorkbook				workbook				= null;
	public static final GlycanImageProvider	glycanImageProvider		= new GlycanImageProvider();
	protected static ExcelWriterHelper		helper					= new ExcelWriterHelper();
	protected static List<Picture>			m_images				= new ArrayList<Picture>();
	private static Integer					numberOfannotatedPeaks	= 0;
	
	public ExcelExpoter()
	{
	}
	
	private static final Logger	logger					= Logger.getLogger(ExcelExpoter.class);
	private static final Double	IMAGE_SCALING_FACTOR	= 0.5D;
	
	public static void writeResultFile(List<Peak> peakList, String resultFilePath,
			Map<String, String> a_sampleFileNameList, Integer a_totalPeak)
	{
		
		logger.info("Writing the excel file : In writeResultFileWithBlankMatched method.");
		CartoonOptions t_options = new CartoonOptions(GraphicOptions.NOTATION_CFG, GraphicOptions.DISPLAY_NORMALINFO,
				IMAGE_SCALING_FACTOR, GraphicOptions.RL, false, false, true);
		glycanImageProvider.setCartoonOptions(t_options);
		workbook = new XSSFWorkbook();
		createFirstSheet(peakList, resultFilePath, a_sampleFileNameList, a_totalPeak);
		createSecondSheet(peakList, resultFilePath, a_sampleFileNameList, a_totalPeak, numberOfannotatedPeaks);
		try
		{
			FileOutputStream outputStream = new FileOutputStream(resultFilePath);
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
		catch (Exception t_e)
		{
			// TODO: handle exception
		}
		
	}
	
	private static void createFirstSheet(List<Peak> peakList, String resultFilePath,
			Map<String, String> a_sampleFileNameList, Integer a_totalPeak)
	{
		Map<String, Double> t_Hashmap = new LinkedHashMap<String, Double>();
		Integer t_sizeOfHashmap = null;
		Map<String, String> sampleAreaColumnAddress = new LinkedHashMap<String, String>();
		Map<String, String> sampleIntensityColumnAddress = new LinkedHashMap<String, String>();
		sampleAreaColumnAddress = a_sampleFileNameList;
		XSSFSheet sheet = workbook.createSheet("Complete");
		int rowNum = 0;
		int totalRow = 0;
		int totalCol = 0;
		Row row = sheet.createRow(rowNum++);
		int colNum = 0;
		Cell currentCell = row.createCell(colNum++);
		currentCell.setCellValue("Observed Mass");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("Thoeretical Mass");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("Deviation ppm");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("Deviation dalton");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("Structure");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("Gws");
		int count = 0;
		Set<String> sampleList = sampleAreaColumnAddress.keySet();
		for (String sample : sampleList)
		{
			currentCell = row.createCell(colNum++);
			currentCell.setCellValue(sample);
			sheet.addMergedRegion(new CellRangeAddress(0, 0, (6 + 2 * count), (7 + 2 * count)));
			currentCell = row.createCell(colNum++);
			count++;
		}

		row = sheet.createRow(rowNum++);
		colNum = 0;
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("");
		for (String sample : sampleList)
		{
			currentCell = row.createCell(colNum++);
			currentCell.setCellValue("Area");
			String areaColumnAlphabet = CellReference.convertNumToColString(currentCell.getColumnIndex());
			sampleAreaColumnAddress.put(sample, areaColumnAlphabet);
			currentCell = row.createCell(colNum++);
			currentCell.setCellValue("Intensity");
			String intensityColumnAlphabet = CellReference.convertNumToColString(currentCell.getColumnIndex());
			sampleIntensityColumnAddress.put(areaColumnAlphabet, intensityColumnAlphabet);
		}

		for (Peak t_peak : peakList)
		{
			if (t_peak.getAnnotation().isEmpty())
			{
				row = sheet.createRow(rowNum++);
				colNum = 0;
				currentCell = row.createCell(colNum++);
				currentCell.setCellValue(t_peak.getCentroidMass());
				currentCell = row.createCell(colNum++);
				currentCell.setCellValue("");
				currentCell = row.createCell(colNum++);
				currentCell.setCellValue("");
				currentCell = row.createCell(colNum++);
				currentCell.setCellValue("");
				currentCell = row.createCell(colNum++);
				currentCell.setCellValue("");
				currentCell = row.createCell(colNum++);
				currentCell.setCellValue("");

				t_Hashmap = t_peak.getSampleNameAreaHashmap();
				Set<String> sampleNames = t_Hashmap.keySet();
				for (String t_sampleName : sampleNames)
				{
					String areaColumnAddress = getColumnAddress(t_sampleName, sampleAreaColumnAddress);
					currentCell = row.createCell(CellReference.convertColStringToIndex(areaColumnAddress));
					currentCell.setCellValue(t_Hashmap.get(t_sampleName));
				}

				// currentCell = row.createCell(colNum++);
				// currentCell.setCellValue(getAreaFromHashmap(t_peak));
				// currentCell = row.createCell(colNum++);
				// currentCell.setCellValue(getSampleName(t_peak));
			}
			else
			{
				for (MassEntry t_entry : t_peak.getAnnotation())
				{
					row = sheet.createRow(rowNum++);
					colNum = 0;
					currentCell = row.createCell(colNum++);
					currentCell.setCellValue(t_peak.getCentroidMass());
					currentCell = row.createCell(colNum++);
					currentCell.setCellValue(t_entry.getMass());
					currentCell = row.createCell(colNum++);
					currentCell.setCellValue(getDeviationppm(t_entry.getMass(), t_peak.getCentroidMass()));
					currentCell = row.createCell(colNum++);
					currentCell.setCellValue(getDeviationDalton(t_entry.getMass(), t_peak.getCentroidMass()));
					currentCell = row.createCell(colNum++);
					currentCell.setCellValue(t_entry.getStructure());
					currentCell = row.createCell(colNum++);
					try
					{
						if (!((t_entry.getGws() == null) || (t_entry.getGws().trim().isEmpty())))
						{
							GlycanImageObject gio = glycanImageProvider.getImage(t_entry.getGws());
							if (gio == null)
							{
								glycanImageProvider.addImageToProvider(t_entry.getGws(), t_entry.getGws());
								gio = glycanImageProvider.getImage(t_entry.getGws());
							}
							helper.writeCellImage(workbook, sheet, currentCell.getRowIndex(),
									currentCell.getColumnIndex(), gio.getAwtBufferedImage(), m_images);
						}
						else
						{
							currentCell.setCellValue("");
						}

					}
					catch (Exception e)
					{
						logger.error("Image generation failed" + e.getMessage() + "string" + t_entry.getGws(), e);
					}
					t_Hashmap = t_peak.getSampleNameAreaHashmap();
					Set<String> sampleNames = t_Hashmap.keySet();
					for (String t_sampleName : sampleNames)
					{
						String areaColumnAddress = getColumnAddress(t_sampleName, sampleAreaColumnAddress);
						currentCell = row.createCell(CellReference.convertColStringToIndex(areaColumnAddress));
						currentCell.setCellValue(t_Hashmap.get(t_sampleName));
					}
				}
			}
		}

		Set<String> sampleAreaList = sampleIntensityColumnAddress.keySet();

		for (String AreaColumn : sampleAreaList)
		{
			int count1 = 2;
			totalRow = sheet.getPhysicalNumberOfRows();
			String currentColumn = sampleIntensityColumnAddress.get(AreaColumn);
			while (count1 < (totalRow))
			{
				Row newRow = sheet.getRow(count1);
				if (newRow == null)
				{
					newRow = sheet.createRow(count1);
				}
				Cell newCurrentCell = newRow.createCell(CellReference.convertColStringToIndex(currentColumn));
				newCurrentCell.setCellType(CellType.FORMULA);
				String intensityPercentage = AreaColumn + (count1 + 1) + "/" + "SUM(" + AreaColumn + "3" + ":"
						+ AreaColumn + totalRow + ")*100";
				count1++;
				newCurrentCell.setCellFormula(intensityPercentage);
				logger.info("Processing" + (count1 - 2));
			}
		}
	}
	
	private static String getColumnAddress(String a_sampleName, Map<String, String> a_sampleAreaColumnAddress)
	{
		Set<String> areaAddressMap = a_sampleAreaColumnAddress.keySet();
		String areaAddressAlphabet = null;
		for (String t_samplename : areaAddressMap)
		{
			if (a_sampleName.equals(t_samplename))
			{
				areaAddressAlphabet = a_sampleAreaColumnAddress.get(t_samplename);

			}
		}

		return areaAddressAlphabet;
	}
	
	private static void createSecondSheet(List<Peak> peakList, String a_resultFilePath,
			Map<String, String> a_sampleFileNameList, Integer a_totalPeak, Integer a_numberOfannotatedPeaks)
	{
		Map<String, Double> t_Hashmap = new LinkedHashMap<String, Double>();
		Integer t_sizeOfHashmap = null;
		Map<String, String> sampleAreaColumnAddress = new LinkedHashMap<String, String>();
		Map<String, String> sampleIntensityColumnAddress = new LinkedHashMap<String, String>();
		sampleAreaColumnAddress = a_sampleFileNameList;
		logger.info("Writing the excel file : In writeResultFileWithBlankMatched method.");
		XSSFSheet sheet = workbook.createSheet("Annotated");
		int rowNum = 0;
		int totalRow = 0;
		int totalCol = 0;

		Row row = sheet.createRow(rowNum++);
		int colNum = 0;
		Cell currentCell = row.createCell(colNum++);
		currentCell.setCellValue("Observed Mass");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("Thoeretical Mass");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("Deviation ppm");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("Deviation dalton");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("Structure");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("Gws");
		int count = 0;
		Set<String> sampleList = sampleAreaColumnAddress.keySet();
		for (String sample : sampleList)
		{
			currentCell = row.createCell(colNum++);
			currentCell.setCellValue(sample);
			sheet.addMergedRegion(new CellRangeAddress(0, 0, (6 + 2 * count), (7 + 2 * count)));
			currentCell = row.createCell(colNum++);
			count++;
		}

		row = sheet.createRow(rowNum++);
		colNum = 0;
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("");
		currentCell = row.createCell(colNum++);
		currentCell.setCellValue("");
		for (String sample : sampleList)
		{
			currentCell = row.createCell(colNum++);
			currentCell.setCellValue("Area");
			String areaColumnAlphabet = CellReference.convertNumToColString(currentCell.getColumnIndex());
			sampleAreaColumnAddress.put(sample, areaColumnAlphabet);
			currentCell = row.createCell(colNum++);
			currentCell.setCellValue("Intensity");
			String intensityColumnAlphabet = CellReference.convertNumToColString(currentCell.getColumnIndex());
			sampleIntensityColumnAddress.put(areaColumnAlphabet, intensityColumnAlphabet);
		}

		for (Peak t_peak : peakList)
		{
			
			if (t_peak.getAnnotation().isEmpty())
			{
				
			}
			else
			{
				for (MassEntry t_entry : t_peak.getAnnotation())
				{
					
					row = sheet.createRow(rowNum++);
					colNum = 0;
					currentCell = row.createCell(colNum++);
					currentCell.setCellValue(t_peak.getCentroidMass());
					currentCell = row.createCell(colNum++);
					currentCell.setCellValue(t_entry.getMass());
					currentCell = row.createCell(colNum++);
					currentCell.setCellValue(getDeviationppm(t_entry.getMass(), t_peak.getCentroidMass()));
					currentCell = row.createCell(colNum++);
					currentCell.setCellValue(getDeviationDalton(t_entry.getMass(), t_peak.getCentroidMass()));
					currentCell = row.createCell(colNum++);
					currentCell.setCellValue(t_entry.getStructure());
					currentCell = row.createCell(colNum++);

					try
					{
						if (!((t_entry.getGws() == null) || (t_entry.getGws().trim().isEmpty())))
						{
							GlycanImageObject gio = glycanImageProvider.getImage(t_entry.getGws());
							if (gio == null)
							{
								glycanImageProvider.addImageToProvider(t_entry.getGws(), t_entry.getGws());
								gio = glycanImageProvider.getImage(t_entry.getGws());
							}
							helper.writeCellImage(workbook, sheet, currentCell.getRowIndex(),
									currentCell.getColumnIndex(), gio.getAwtBufferedImage(), m_images);
						}

					}
					catch (Exception e)
					{
						logger.error("Image generation failed" + e.getMessage() + "string" + t_entry.getGws(), e);
					}
					// currentCell.setCellValue(t_entry.getGws());
					// currentCell = row.createCell(colNum++);
					// currentCell.setCellValue(getAreaFromHashmap(t_peak));
					// currentCell = row.createCell(colNum++);
					// currentCell.setCellValue(getSampleName(t_peak));
					t_Hashmap = t_peak.getSampleNameAreaHashmap();
					Set<String> sampleNames = t_Hashmap.keySet();
					for (String t_sampleName : sampleNames)
					{
						String areaColumnAddress = getColumnAddress(t_sampleName, sampleAreaColumnAddress);
						currentCell = row.createCell(CellReference.convertColStringToIndex(areaColumnAddress));
						currentCell.setCellValue(t_Hashmap.get(t_sampleName));
					}
					numberOfannotatedPeaks++;
				}
			}
		}

		Set<String> sampleAreaList = sampleIntensityColumnAddress.keySet();

		for (String AreaColumn : sampleAreaList)
		{
			int count1 = 2;
			totalRow = sheet.getPhysicalNumberOfRows();
			String currentColumn = sampleIntensityColumnAddress.get(AreaColumn);
			while (count1 < totalRow)
			{
				Row newRow = sheet.getRow(count1);
				Cell newCurrentCell = newRow.createCell(CellReference.convertColStringToIndex(currentColumn));
				newCurrentCell.setCellType(CellType.FORMULA);
				String intensityPercentage = AreaColumn + (count1 + 1) + "/" + "SUM(" + AreaColumn + "3" + ":"
						+ AreaColumn + totalRow + ")*100";
				count1++;
				newCurrentCell.setCellFormula(intensityPercentage);
				logger.info("Processing" + (count1 - 2));
			}
		}

	}
	
	private static Double getDeviationppm(Double theoreticalMass, Double peakMass)
	{
		Double t_dev = Math.abs(theoreticalMass - peakMass);
		Double deviationn = t_dev * 1000000 / peakMass;
		return deviationn;
		
	}
	
	private static Double getDeviationDalton(Double theoreticalMass, Double peakMass)
	{
		Double t_deviation = peakMass - theoreticalMass;
		return t_deviation;
	}
	
	private static Double getAreaFromHashmap(Peak a_peakObj)
	{
		Map<String, Double> t_hashMap = a_peakObj.getSampleNameAreaHashmap();
		Map.Entry<String, Double> t_entry = t_hashMap.entrySet().iterator().next();
		Double t_area = t_entry.getValue();
		return t_area;
	}
	
	private static String getSampleName(Peak a_peakObj)
	{
		Map<String, Double> t_hashMap = a_peakObj.getSampleNameAreaHashmap();
		Map.Entry<String, Double> t_entry = t_hashMap.entrySet().iterator().next();
		String t_value = t_entry.getKey();
		return t_value;
	}

}
