package de.ipbhalle.metfrag.addDatabasIDs;
import java.util.Map;

import de.ipbhalle.metfrag.massbankParser.DatabaseIDs;


public class SpectraWriter {
	
	Map<DatabaseIDs, String> detectedIDs;
	String fileName;
	
	String id;

	boolean iscid;
	
	
	public SpectraWriter(Map<DatabaseIDs,String> detectedIDs , String fileName , boolean iscid, String id )
	{
		this.detectedIDs = detectedIDs;
		
		this.fileName= fileName;
		this.id=id;

		this.iscid=iscid;
		
	}
	
	public String getName()
	{
		return this.fileName;
	}
	
	public boolean isCID()
	{
		return this.iscid;
	}

	public Map<DatabaseIDs,String> getDetectedIDsMap()
	{
		return detectedIDs;
	}
	
	public String getID()
	{
		return this.id;
	}
	
}
