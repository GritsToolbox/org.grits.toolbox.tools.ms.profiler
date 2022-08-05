package org.grits.toolbox.tools.ms.profiler.om;

public class SelectedFileViewer
{
	private String	fileName	= null;
	private String	sampleName	= null;
	private Double	threshold	= null;
	
	public String getFileName()
	{
		return this.fileName;
	}
	
	public void setFileName(String a_fileName)
	{
		this.fileName = a_fileName;
	}
	
	public String getSampleName()
	{
		return this.sampleName;
	}
	
	public void setSampleName(String a_sampleName)
	{
		this.sampleName = a_sampleName;
	}
	
	public Double getThreshold()
	{
		return this.threshold;
	}
	
	public void setThreshold(Double a_threshold)
	{
		this.threshold = a_threshold;
	}
	
}
