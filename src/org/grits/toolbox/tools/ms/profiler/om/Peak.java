package org.grits.toolbox.tools.ms.profiler.om;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Peak
{
	private Integer				index					= null;
	private Double				centroidMass			= null;
	private Map<String, Double>	sampleNameAreaHashmap	= new LinkedHashMap<String, Double>();
	private List<MassEntry>		annotation				= new ArrayList<>();
	
	public Integer getIndex()
	{
		return this.index;
	}
	
	public void setIndex(Integer a_index)
	{
		this.index = a_index;
	}
	
	public Double getCentroidMass()
	{
		return this.centroidMass;
	}
	
	public void setCentroidMass(Double a_centroidMass)
	{
		this.centroidMass = a_centroidMass;
	}
	
	public List<MassEntry> getAnnotation()
	{
		return this.annotation;
	}
	
	public void setAnnotation(List<MassEntry> a_annotation)
	{
		this.annotation = a_annotation;
	}

	public Map<String, Double> getSampleNameAreaHashmap()
	{
		return this.sampleNameAreaHashmap;
	}
	
	public void setSampleNameAreaHashmap(Map<String, Double> a_sampleNameAreaHashmap)
	{
		this.sampleNameAreaHashmap = a_sampleNameAreaHashmap;
	}
	
}
