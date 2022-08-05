package org.grits.toolbox.tools.ms.profiler.wordOperations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.grits.toolbox.tools.ms.profiler.om.Peak;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;

public class WordExpoter
{
	static String				wordFilePath					= null;
	static String				folderFilePath					= null;
	static XWPFDocument			document						= null;
	static XWPFTable			table							= null;
	static Map<String, String>	sampleAreaColumnAddress			= null;
	static Map<String, String>	sampleIntensityColumnAddress	= null;

	public static void writeResultFIle(List<Peak> a_masterPeakList, String a_completeresultFilePath,
			Map<String, String> a_sampleFileNameList, Integer a_totalPeak)
	{
		try
		{
			createWordFile(a_masterPeakList, a_completeresultFilePath, a_sampleFileNameList, a_totalPeak);

		}
		catch (Exception e)
		{
			
		}
	}
	
	private static void createWordFile(List<Peak> a_masterPeakList, String a_completeresultFilePath,
			Map<String, String> a_sampleFileNameList, Integer a_totalPeak)
	{
		try
		{
			Map<String, Double> t_Hashmap = new LinkedHashMap<String, Double>();
			Integer t_sizeOfHashmap = null;
			sampleAreaColumnAddress = new LinkedHashMap<String, String>();
			sampleIntensityColumnAddress = new LinkedHashMap<String, String>();
			sampleAreaColumnAddress = a_sampleFileNameList;
			File t_file = new File(a_completeresultFilePath);
			folderFilePath = t_file.getParent();
			int last = a_completeresultFilePath.lastIndexOf(".");
			String subString = a_completeresultFilePath.substring(0, last);
			wordFilePath = subString + ".docx";

			document = new XWPFDocument();
			FileOutputStream out = new FileOutputStream(wordFilePath);
			table = document.createTable();
			createTableFirstRowHeadings(table);
			createTableSecondRow(table);
			
			document.write(out);
			out.close();
			
		}
		catch (NullPointerException | SecurityException | IOException e3)
		{
			
		}

	}

	private static void createTableFirstRowHeadings(XWPFTable a_table)
	{
		XWPFTableRow tableRowOne = a_table.getRow(0);
		tableRowOne.getCell(0).setText("Observed Mass");
		tableRowOne.addNewTableCell().setText("Theoretical Mass");
		tableRowOne.addNewTableCell().setText("Deviation ppm");
		tableRowOne.addNewTableCell().setText("Deviation dalton");
		tableRowOne.addNewTableCell().setText("Structure");
		tableRowOne.addNewTableCell().setText("Gws");
		Set<String> sampleList = sampleAreaColumnAddress.keySet();
		int count = 0;
		for (String sample : sampleList)
		{
			tableRowOne.addNewTableCell().setText("sample");
			tableRowOne.addNewTableCell().setText("sample");
			
			if (table.getRow(0).getCell((6 + (count * 2))).getCTTc().getTcPr() == null)
				table.getRow(0).getCell((6 + (count * 2))).getCTTc().addNewTcPr();
			
			if (table.getRow(0).getCell((7 + (count * 2))).getCTTc().getTcPr() == null)
				table.getRow(0).getCell((7 + (count * 2))).getCTTc().addNewTcPr();
			
			CTHMerge hMerge = CTHMerge.Factory.newInstance();
			hMerge.setVal(STMerge.RESTART);
			table.getRow(0).getCell((6 + (count * 2))).getCTTc().getTcPr().setHMerge(hMerge);
			
			CTHMerge hMerge1 = CTHMerge.Factory.newInstance();
			hMerge1.setVal(STMerge.CONTINUE);
			table.getRow(0).getCell((7 + (count * 2))).getCTTc().getTcPr().setHMerge(hMerge1);
			count++;
		}

		// for (String sample : sampleList)
		// {
		// tableRowOne.addNewTableCell();
		// tableRowOne.addNewTableCell();
		// spanCellsAcrossRow(table, 0, (6 + (count * 2)), 2);
		// }
	}
	
	private static void createTableSecondRow(XWPFTable a_table)
	{
		XWPFTableRow tableRowtwo = a_table.createRow();
		tableRowtwo.getCell(0).setText("");
		tableRowtwo.getCell(1).setText("");
		tableRowtwo.getCell(2).setText("");
		tableRowtwo.getCell(3).setText("");
		tableRowtwo.getCell(4).setText("");
		tableRowtwo.getCell(5).setText("");
		// tableRowtwo.addNewTableCell().setText("");
		// tableRowtwo.addNewTableCell().setText("");
		// tableRowtwo.addNewTableCell().setText("");
		// tableRowtwo.addNewTableCell().setText("");
		// tableRowtwo.addNewTableCell().setText("");
		Set<String> sampleList = sampleAreaColumnAddress.keySet();
		int count = 0;
		for (String sample : sampleList)
		{
			tableRowtwo.getCell(6 + (count * 2)).setText("Area");
			tableRowtwo.getCell(7 + (count * 2)).setText("Intensity");
			count++;
		}

	}
	
	private static void spanCellsAcrossRow(XWPFTable a_table, int rowNum, int colNum, int span)
	{
		XWPFTableCell cell = a_table.getRow(rowNum).getCell(colNum);
		cell.getCTTc().getTcPr().addNewGridSpan();
		cell.getCTTc().getTcPr().getGridSpan().setVal(BigInteger.valueOf(span));

	}
}
