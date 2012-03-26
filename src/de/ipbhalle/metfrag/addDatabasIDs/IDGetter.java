package de.ipbhalle.metfrag.addDatabasIDs;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.xml.crypto.Data;
import javax.xml.rpc.ServiceException;

import keggapi.KEGGLocator;
import keggapi.KEGGPortType;
import keggapi.LinkDBRelation;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.eclipse.swt.internal.C;

import sun.security.jca.GetInstance;

import de.ipbhalle.metfrag.massbankParser.DatabaseIDs;
import de.ipbhalle.metfrag.spectrum.WrapperSpectrum;

//tries to add missing database IDs to a given spectra
public class IDGetter {
	
	
	Map<String,Map<DatabaseIDs,String>> keggToDatabases;

	Map<String,String> knapsackToPubchem;
	
	Map<String,Map<DatabaseIDs,String>> linksToKEGG;
	Map<String,String> chebiKEGGcomp;
	Map<String,String> knapsackKEGGcomp;
	Map<String,String> pubchemKEGGcomp;
	
	Map<String,String> keggChebi;
	Map<String,String> keggPubchem;
	Map<String,String> keggKnapsack;
	
	Map<String,String> sidToCid = new HashMap<String, String>();
	
	Map<String,Set<String>> cidToSids = new HashMap<String, Set<String>>();
	
	public IDGetter()
	{
		keggToDatabases = new HashMap<String,Map<DatabaseIDs,String>>();
		
		Map<DatabaseIDs,String>linksToKEGGcomp = new HashMap<DatabaseIDs, String>();
		Map<DatabaseIDs,String>linksToKEGGdrug = new HashMap<DatabaseIDs, String>();
		
		String home="/home/ftarutti/Downloads/";
		
		linksToKEGGcomp.put(DatabaseIDs.CHEBI, home+"compound_chebi.list");
		linksToKEGGcomp.put(DatabaseIDs.KNAPSACK, home+"compound_knapsack.list");
		linksToKEGGcomp.put(DatabaseIDs.PUBCHEM_SID,home+"compound_pubchem.list");
		
		linksToKEGGdrug.put(DatabaseIDs.CHEBI, home+"drug_chebi.list");
		linksToKEGGdrug.put(DatabaseIDs.KNAPSACK, home+"drug_knapsack.list");
		linksToKEGGdrug.put(DatabaseIDs.PUBCHEM_SID,home+"drug_pubchem.list");
		
		linksToKEGG = new HashMap<String, Map<DatabaseIDs,String>>();
		
		linksToKEGG.put("C", linksToKEGGcomp);
		linksToKEGG.put("D", linksToKEGGdrug);
		
		chebiKEGGcomp= new HashMap<String, String>();
		knapsackKEGGcomp= new HashMap<String, String>();
		pubchemKEGGcomp= new HashMap<String, String>();
		
		keggChebi = new HashMap<String, String>();
		keggPubchem = new HashMap<String, String>();
		keggKnapsack= new HashMap<String, String>();
	}
	
	
	
	public void fillDBToKEGG(DatabaseIDs database) throws IOException
	{
		
		//System.out.println(database.toString());
		//System.out.println(linksToKEGG.get(database));
		Set<String> keys = linksToKEGG.keySet();
		
		for (String string : keys) {
			
		
		
		BufferedReader in = new BufferedReader(new FileReader(linksToKEGG.get(string).get(database)));
		String line="";
			
		while((line = in.readLine())!=null)
		{
		
			String columns[]=new String[line.split("\\s+").length];
			columns=line.split("\\s+");
				
			
			String keggID=columns[0];
			
			if(string.equals("C"))
			{
				keggID=keggID.replace("cpd:", "");
			}
			if(string.equals("D"))
			{
				keggID=keggID.replace("dr:","");
			}
			
			String dbID="";
			if(database.equals(DatabaseIDs.CHEBI))
			{
				dbID = columns[1].toUpperCase();
				//System.out.println(dbID);
				if(keggID.startsWith("C"))
					chebiKEGGcomp.put(dbID, keggID);
				keggChebi.put(keggID, dbID);
			}
			else
			{
				if(database.equals(DatabaseIDs.KNAPSACK))
				{
					dbID = columns[1].replace("knapsack:", "");
					if(keggID.startsWith("C"))
						knapsackKEGGcomp.put(dbID, keggID);
					keggKnapsack.put(keggID, dbID);
				}
				else{
					if(database.equals(DatabaseIDs.PUBCHEM_SID))
					{
						dbID = columns[1].replace("pubchem:","");
						if(keggID.startsWith("C"))
							pubchemKEGGcomp.put(dbID, keggID);
						keggPubchem.put(keggID, dbID);
					}
					else
					{
						System.out.println(database.toString()+" not supported yet.");
						break;
					}
				}
			}

		}
		in.close();
		}
		
		
	}
	

	
	public String getDBIDFromKEGG(String keggID,DatabaseIDs database)
	{

		
//		System.out.println(keggID);
//		System.out.println(keggChebi.toString());
		if(database.equals(DatabaseIDs.CHEBI))
		{
//			System.out.println("CHEBI "+keggID);
//			Set<String> keys = keggChebi.keySet();
//			for (String string : keys) {
//				System.out.println(string+"\t"+keggChebi.get(string));
//			}
			if(keggChebi.containsKey(keggID))
				return keggChebi.get(keggID).replace("CHEBI:", "");
			else
				return "";
		}
		
		if(database.equals(DatabaseIDs.PUBCHEM_SID))
		{
			if(keggPubchem.containsKey(keggID))
				return keggPubchem.get(keggID);
			else
				return "";
		}
		
		if(database.equals(DatabaseIDs.KNAPSACK))
		{
			if(keggKnapsack.containsKey(keggID))
				return keggKnapsack.get(keggID);
			else
				return "";
		}
		return "";
	}
	
	public String getKEGGFromDBID(String dbID, DatabaseIDs database)
	{ 
	
		
		if(database.equals(DatabaseIDs.CHEBI))
		{
			if(!dbID.toUpperCase().startsWith("CHEBI:"))
				dbID="CHEBI:"+dbID;
			
			
			if(chebiKEGGcomp.containsKey(dbID))
			{
				return chebiKEGGcomp.get(dbID.toUpperCase());
			}
			else
			{
				return "";
			}
		}
		if(database.equals(DatabaseIDs.PUBCHEM_SID)&&pubchemKEGGcomp.containsKey(dbID))
		{
			return pubchemKEGGcomp.get(dbID);
		}
		if(database.equals(DatabaseIDs.KNAPSACK)&&knapsackKEGGcomp.containsKey(dbID))
		{
			return knapsackKEGGcomp.get(dbID);
		}
		
		return "";
	}
	
	public String getIDFromMetlin(String metlinID,DatabaseIDs database) throws NumberFormatException, IOException
	{
		//
		
		String pubchemID="";
		
		boolean dbSupported=true;
		
		String checkString[]=null;
		String pattern="";
		
		String replaces[]=null;
		if(database.equals(DatabaseIDs.PUBCHEM_CID))
		{
			checkString=new String[2];
			checkString[0]="pubchem";
			checkString[1]="cid";
			
			pattern="cid=\\d+\"";
			
			replaces=new String[2];
			replaces[0]="cid=";
			replaces[1]="\"";
			
		}
		else{
			if(database.equals(DatabaseIDs.KEGG))
			{
				checkString=new String[2];
				checkString[0]="footer";
				checkString[1]="cpd";
				
				pattern="cpd:C\\d+\"";
				
				replaces=new String[2];
				replaces[0]="cpd:";
				replaces[1]="\"";
			}
			else
			{
				dbSupported=false;
			}
		}

		
		
		if(metlinID!= null && ! metlinID.equals("")
				&& ! metlinID.equals("none") && dbSupported)
		{
			String reqStr = "http://metlin.scripps.edu/metabo_info.php";
			HttpClient client = new HttpClient();

			PostMethod method = new PostMethod(reqStr);
			
			method.addParameter("molid", metlinID);
			
			
			try {
				client.executeMethod(method);
			} catch (HttpException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			InputStream is = method.getResponseBodyAsStream();
			String line = "";
			if (is != null) {
				try {
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(is, "UTF-8"));
					while ((line = reader.readLine()) != null) {

						if ( line.contains(checkString[0])
								&& line.contains(checkString[1])) {
				
							Matcher pubchem = Pattern.compile(
									pattern).matcher(line);
							while (pubchem.find()) {
								//System.out.println(pubchem.group().toString());
								String id = pubchem.group().replace(
										replaces[0], "");
								id = id.replace(replaces[1], "");
								pubchemID = id;// Integer.parseInt(id);
							}

						}
					}
				} finally {
					try {
						is.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			method.releaseConnection();
			
		}
		
		
		
		return pubchemID;
	
	}


	public void getCIDSIDMaps(Set<String> sids, Set<String> cids) throws FileNotFoundException, IOException
	{
		
		//System.out.println("FILL CID <-> SID:");
		String file="/home/ftarutti/Downloads/CID-SID.gz";
		
		GZIPInputStream gis = null;
		
		gis = new GZIPInputStream(new FileInputStream(file));
		
		BufferedReader in = new BufferedReader(new InputStreamReader(gis) );
		String line;
		
		//System.out.println("OUR CIDs: "+cids);
		
		//System.out.println((sids.size()!=0 || cids.size()!=0));
		
		while((line=in.readLine())!=null && (sids.size()!=0 || cids.size()!=0))
		{
			String []columns = new String[line.split("\\s+").length];
			columns=line.split("\\s+");
			
			if(sids.contains(columns[1]))
			{
				sidToCid.put(columns[1], columns[0]);
				
				sids.remove(columns[1]);
			}

			if(cids.contains(columns[0]))
			{
			 
				if(cidToSids.containsKey(columns[0]))
				{
					Set<String> sidsHere= cidToSids.get(columns[0]);
					sidsHere.add(columns[1]);
					
					cidToSids.put(columns[0], sidsHere);
				}
				else
				{
					Set<String> sidsHere = new HashSet<String>();
					sidsHere.add(columns[1]);
					
					cidToSids.put(columns[0], sidsHere);
				}
				
				cids.remove(columns[0]);
			}
			
		}
		
		
		in.close();
		gis.close();
	}
	
	
	public Map<String,String> getCIDFromSID(Set<String> sids ) throws FileNotFoundException, IOException
	{
		Map<String,String> res = new HashMap<String, String>();
		
		String file="/home/ftarutti/Downloads/CID-SID.gz";
		
		GZIPInputStream gis = null;
		
		gis = new GZIPInputStream(new FileInputStream(file));
		
		BufferedReader in = new BufferedReader(new InputStreamReader(gis) );
		String line;
		
		while((line=in.readLine())!=null && sids.size()!=0)
		{
			String []columns = new String[line.split("\\s+").length];
			columns=line.split("\\s+");
			
			if(sids.contains(columns[1]))
			{
				res.put(columns[1], columns[0]);
				
				sids.remove(columns[1]);
			}
			

			
		}
		
		
		in.close();
		gis.close();
		
		return res;
	}
	
	public Map<String,Set<String>> getSIDsFromCID(Set<String> cids ) throws FileNotFoundException, IOException
	{
		Map<String, Set<String>> res = new HashMap<String, Set<String>>();
		
		String file="/home/ftarutti/Downloads/CID-SID.gz";
		
		GZIPInputStream gis = null;
		
		gis = new GZIPInputStream(new FileInputStream(file));
		
		BufferedReader in = new BufferedReader(new InputStreamReader(gis) );
		String line;
		
	
		
		while((line=in.readLine())!=null )
		{
			String []columns = new String[line.split("\\s+").length];
			columns=line.split("\\s+");

			
			if(cids.contains(columns[0]))
			{
				if(res.containsKey(columns[0]))
				{
					Set<String> sids= res.get(columns[0]);
					sids.add(columns[1]);
					
					res.put(columns[0], sids);
				}
				else
				{
					Set<String> sids = new HashSet<String>();
					sids.add(columns[1]);
					
					res.put(columns[0], sids);
				}
			}
			
		}
		
		
		in.close();
		
		return res;
		
	}
	
	public static void main(String[] args) throws NumberFormatException, IOException, ServiceException, InterruptedException {
	
//		IDGetter get = new IDGetter();
//		
////		get.fillDBToKEGG(DatabaseIDs.PUBCHEM);
////		
////		System.out.println( get.getKEGGFromDBID("3329", DatabaseIDs.PUBCHEM));
//		
//		Set<String> cids = new HashSet<String>();
//		
//		cids.add("445675" +
//				"");
//		//cids.add("2");
//		
//		Map<String,Set<String>> res = get.getSIDsFromCID(cids);
//		
//		//Map<String,String> res = get.getCIDFromSID(cids);
//		
//		System.out.println(res.toString());
//		
//		Set<String> sids = res.get("445675");
//		
//		
//		Map<String,String> res2 = get.getCIDFromSID(sids);
//		
//		System.out.println(res2.toString());
//		
//		Set<String> result = new HashSet<String>();
//
//		get.fillDBToKEGG(DatabaseIDs.PUBCHEM);
//		
//		
//		for (String string : res2.keySet()) {
//			result.add(get.getKEGGFromDBID(string, DatabaseIDs.PUBCHEM));
//		}
//		
//		System.out.println(result.toString());
	
		
		Map <String,SpectraWriter> preProcessing = new  HashMap<String, SpectraWriter>();
		String home =  "/home/ftarutti/testspectra/testSC/testSC0/"+//testXX8" +
				"/" +
				"" +
				"" +
				"" +
				"";
		
		
		String entries[] = new File(home + ".").list();
		
		//this map contains all sids which have to be converged to a cid and their related spectrum file 
		Map<String,String> sidsMap = new HashMap<String, String>(); 
		
		//this map contains all cids which have to be converged to a sid and their related spectrum file
		Map<String,String> cidsMap = new HashMap<String, String>();
		
		for (int j = 0; j < entries.length; j++) 
		{

			
			System.out.println(entries[j]);
			WrapperSpectrum spectrum = new WrapperSpectrum(home	+ entries[j]);

			//Map<DatabaseIDs,Boolean> hasID = new HashMap<DatabaseIDs, Boolean>(); //stattdessen lieber ein Set in dem nur die DBs stehen zu denen wir noch IDs brauchen, und nach jeder neu gefundenen die DB entfernen
			Set<DatabaseIDs> neededIDs= new HashSet<DatabaseIDs>();
			
			Map<DatabaseIDs,String> detectedIDs = new HashMap<DatabaseIDs, String>();
			
			boolean allIDs=false;
			
			int counter = 0;
			
			if (spectrum.getCID()==0)
			{
				neededIDs.add(DatabaseIDs.PUBCHEM_CID);
			}
			else
			{
				counter++;
			}
			if(spectrum.getKEGG()==null || spectrum.getKEGG().equals("") || spectrum.getKEGG().equals("none"))
			{
				neededIDs.add(DatabaseIDs.KEGG);
			}
			else
			{
				counter++;
			}
			if(spectrum.getChebi()==null || spectrum.getChebi().equals("") || spectrum.getChebi().equals("none"))
			{
				neededIDs.add(DatabaseIDs.CHEBI);
			}
			else
			{
				counter++;
			}
			if(spectrum.getKnapsack()==null || spectrum.getKnapsack().equals("")|| spectrum.getKnapsack().equals("none"))
			{
				neededIDs.add(DatabaseIDs.KNAPSACK);
			}
			else
			{counter++;}

			
			
			//check if there are all IDs contained in the spectra
			if(counter==4)
			{
				allIDs=true;
			}
			
			IDGetter getID = new IDGetter();
			//getID.fillDBToKEGG(database);
			
			Set<String> keys = getID.linksToKEGG.keySet();
			
			
			//init connection from and to KEGG
			for (String string : keys) {

				Set<DatabaseIDs> DatabasesWithKEGGConnection = getID.linksToKEGG.get(string).keySet();

				for (DatabaseIDs compoundDatabases : DatabasesWithKEGGConnection) {

					if(compoundDatabases.equals(DatabaseIDs.PUBCHEM_CID))
						compoundDatabases = DatabaseIDs.PUBCHEM_SID;
					getID.fillDBToKEGG(compoundDatabases);

				}

			}
			
			int secureCounter =0; // to avoid an endless loop if one id cannot be found
			
			boolean somethingChanged = true;
			
			int iteration=0;
			
			while(neededIDs.size()>0 && somethingChanged )
			{
				somethingChanged=false;
				
				
				System.out.println(iteration);
				iteration++;
				
				//try to get needed IDs from Metlin DB
				if(spectrum.getMetlin()!=null && ! spectrum.getMetlin().equals("") && ! spectrum.getMetlin().equals("none") && neededIDs.size()>0)
				{
					String metlin= spectrum.getMetlin();

					for (Iterator iterator = neededIDs.iterator(); iterator
							.hasNext();) {
						
						DatabaseIDs compoundDatabases = (DatabaseIDs) iterator.next();
						
						String id = getID.getIDFromMetlin(metlin, compoundDatabases);
						
						if(!id.equals("")&&!id.equals("none"))
						{
							detectedIDs.put(compoundDatabases, id);
							//neededIDs.remove(compoundDatabases);
							iterator.remove();
							somethingChanged=true;
						}
						
						if(neededIDs.size()==0)
						{
							break;	
						}
					}

				}

				//try to get needed IDs from KEGG DB
				
				if(! neededIDs.contains(DatabaseIDs.KEGG) && neededIDs.size()>0 )
				{
					String kegg = spectrum.getKEGG();
					
				
					
					
					if (detectedIDs.containsKey(DatabaseIDs.KEGG))
					{
						kegg = detectedIDs.get(DatabaseIDs.KEGG);
					}
					//TODO: http://www.java-forum.org/allgemeine-java-themen/69675-java-util-concurrentmodificationexception-hashmap.html
					
					
					
					for (Iterator iterator = neededIDs.iterator(); iterator
							.hasNext();) {
						
						DatabaseIDs compoundDatabases = (DatabaseIDs) iterator.next();
						
//					}
//					for (DatabaseIDs compoundDatabases : neededIDs) {
						String id = getID.getDBIDFromKEGG(kegg, compoundDatabases);
						
						if(compoundDatabases.equals(DatabaseIDs.PUBCHEM_CID))
							id = getID.getDBIDFromKEGG(kegg, DatabaseIDs.PUBCHEM_SID);
						
						//System.out.println(id);
						
						if(!id.equals(""))
						{
							if(compoundDatabases.equals(DatabaseIDs.PUBCHEM_CID))
							{
								detectedIDs.put(DatabaseIDs.PUBCHEM_SID, id);
								somethingChanged=true;
								iterator.remove();
								
								sidsMap.put(entries[j], id);
							}	
							else
							{
							detectedIDs.put(compoundDatabases, id);
						//	neededIDs.remove(compoundDatabases);
							somethingChanged=true;
							iterator.remove();
							}
						}
						
						if(neededIDs.size()==0)
						{
							break;	
						}
					}
					
				}

				//try to get kegg ID from KnapSack
				if(neededIDs.size()>0 && neededIDs.contains(DatabaseIDs.KEGG) && spectrum.getKnapsack()!=null && !spectrum.getKnapsack().equals("") && !spectrum.getKnapsack().equals("none"))					
				{
					String knapsack=spectrum.getKnapsack();
					
					//System.out.println(knapsack);
					
					String kegg = getID.getKEGGFromDBID(knapsack, DatabaseIDs.KNAPSACK);
					
					if(!kegg.equals(""))
					{
						detectedIDs.put(DatabaseIDs.KEGG, kegg);
						neededIDs.remove(DatabaseIDs.KEGG);
						somethingChanged=true;
					}
					
				}
				//System.out.println();
				//System.out.println("after Knapsack search:");
//				System.out.println();
//				System.out.println("from knapsack: ");
//				status(detectedIDs, neededIDs, somethingChanged);
				
				//spectra contains Chebi ID, but no KEGG
				if(neededIDs.size()>0 && neededIDs.contains(DatabaseIDs.KEGG) && !neededIDs.contains(DatabaseIDs.CHEBI))
				{
					String chebi=spectrum.getChebi();
					
					
					
					String kegg = getID.getKEGGFromDBID(chebi, DatabaseIDs.CHEBI);
					
		
					
					if(!kegg.equals(""))
					{
						detectedIDs.put(DatabaseIDs.KEGG, kegg);
						neededIDs.remove(DatabaseIDs.KEGG);
						somethingChanged=true;
					}
					
				}
				//System.out.println();
//				System.out.println();
//				System.out.println("after chebi search:");
//				
//				status(detectedIDs, neededIDs, somethingChanged);
				
				//in this case spectra contains only a pubchemID
				if(neededIDs.size()>0 && neededIDs.contains(DatabaseIDs.KEGG) && (spectrum.getCID()!=0  ) && !spectrum.getDBLinks().containsKey(DatabaseIDs.PUBCHEM_SID))
				{
		
					int pubchem = spectrum.getCID();
					
					cidsMap.put(entries[j],pubchem+"");
					
				}
//				System.out.println();
//				System.out.println("after cid: ");
//				status(detectedIDs, neededIDs, somethingChanged);
				//System.out.println();
				//Spectrum contains SID
				if((spectrum.getDBLinks().containsKey(DatabaseIDs.PUBCHEM_SID)|| detectedIDs.containsKey(DatabaseIDs.PUBCHEM_SID)) )
				{
					String sid=spectrum.getDBLinks().get(DatabaseIDs.PUBCHEM_SID);
					
					if(sid==null)
					{
						sid = detectedIDs.get(DatabaseIDs.PUBCHEM_SID);
					}
					
					if(neededIDs.contains(DatabaseIDs.PUBCHEM_CID))
					{
						sidsMap.put(entries[j], sid);
					}
					
					detectedIDs.put(DatabaseIDs.PUBCHEM_SID,sid);
					
					if(neededIDs.contains(DatabaseIDs.KEGG))
					{
						String kegg = getID.getKEGGFromDBID(sid, DatabaseIDs.PUBCHEM_SID);
						if(!kegg.equals(""))
						{
							detectedIDs.put(DatabaseIDs.KEGG, kegg);
							neededIDs.remove(DatabaseIDs.KEGG);
							somethingChanged=true;
						}
					}
				}
				
				//after sid
//				System.out.println();
//				System.out.println("after sid: ");
//				status(detectedIDs, neededIDs, somethingChanged);
				
			}
			
			
//			if(detectedIDs.containsKey(DatabaseIDs.PUBCHEM_SID))		
//			{
//				if(detectedIDs.get(DatabaseIDs.PUBCHEM).startsWith("SID:"))
//				{
//					//System.out.println("put to sidsMap: "+entries[j]+"\t"+detectedIDs.get(DatabaseIDs.PUBCHEM));
//					sidsMap.put(entries[j], detectedIDs.get(DatabaseIDs.PUBCHEM).replace("SID:", ""));
//				}
//			}

			
			// read Spectrum again and write new spectra while reading
			if(!sidsMap.containsKey(entries[j]) && !cidsMap.containsKey(entries[j]))
			{
				rewriteSpectra( home,entries[j],  detectedIDs);
				System.out.println(entries[j]+" rewritten.");
				
			}
			else
			{
				SpectraWriter spec=null;
				if(sidsMap.containsKey(entries[j]))
					spec = new SpectraWriter(detectedIDs, entries[j] , false , sidsMap.get(entries[j]));
				if(cidsMap.containsKey(entries[j]))
					spec = new SpectraWriter(detectedIDs, entries[j], true, cidsMap.get(entries[j]));
				
				preProcessing.put(entries[j], spec);
				
			}

		}
		
		System.out.println("Postprocessing...");
		
		// Vorsicht! Was wenn bereits andere IDs gemappt wurden sind? Dies muss man hier irgendwie mitspeichern!!!
		
		// 1.) spectra which are included in sidsMap:
		//			- find cid belonging to sid and write the spectra
		// ADDITION: there are spectra, which dpo only contain pubchem SIDs, if that's the case you have to look for new information as well
		// 2.) spectra which are included in cidsMap:
		//			(chNCE TO GET MORE iNFORMATION!!!)
		//			- find sids belonging to the cid (more than one sids for one cid)
		//			- get exactly one(!!!) KEGG ID for that sids
		//			- try to get more information by using the KEGG Ids
		
		
		// First: search for mappings in both directions
		
		//Read CID-SID

		IDGetter getPubchem = new IDGetter();
		
		Set<String> sids=new HashSet<String>();
		Set<String> files = sidsMap.keySet();
		
		for (String string2 : files) {
			//System.out.println(string2);
			//System.out.println(sidsMap.get(string2));
			sids.add(sidsMap.get(string2));
		}
		
		Set<String> cids = new HashSet<String>();
		Set<String> fileswithCids = cidsMap.keySet();
		
		for (String string2 : fileswithCids) {
			cids.add(cidsMap.get(string2));
		}
		
		//System.out.println("CIDS: "+cids.toString());
		
		getPubchem.getCIDSIDMaps(sids,cids);
		
		//System.out.println("read.");
		
		// Second: 1.)
		
		//System.out.println(getPubchem.sidToCid.toString());
		
		for (String file : files) {
		
				
			System.out.println("PP SID"+file);
				String sid = sidsMap.get(file);

				//System.out.println(sid);
				String cid = getPubchem.sidToCid.get(sid);

				//System.out.println(cid);
				
				SpectraWriter spec = preProcessing.get(file);
				
				if(cid!=null)
				{
					spec.detectedIDs.put(DatabaseIDs.PUBCHEM_CID, cid);
				}
				//System.out.println("New detected IDs:");
				//System.out.println(spec.detectedIDs.toString());
				
				rewriteSpectra( home,file,  spec.detectedIDs);
				System.out.println(file+" rewritten.");
		}
		

		
		
		
		// Third: 2.) 

		getPubchem.fillDBToKEGG(DatabaseIDs.PUBCHEM_SID);
		getPubchem.fillDBToKEGG(DatabaseIDs.CHEBI);
		for (String file : fileswithCids) {
			
			
			System.out.println("PP CID"+file);
			
			boolean retry=false;
			
			String cid = cidsMap.get(file);
			
			// get sids for cid
			//System.out.println("ALL IDS: "+getPubchem.cidToSids.toString());
			Set<String> sidsFromCid = getPubchem.cidToSids.get(cid);
			
//			System.out.println(cid);
//			System.out.println("SIDS:  "+sidsFromCid.toString());
			
			Set<String> keggIds = new HashSet<String>();
			
			
			// get KEGG id from sids
			for (String string : sidsFromCid) {
				
				String kegg = getPubchem.getKEGGFromDBID(string,DatabaseIDs.PUBCHEM_SID);
				
				keggIds.add(kegg);
			}
			
//			System.out.println(keggIds.toString());
			
			SpectraWriter spec = preProcessing.get(file);
			
			keggIds.remove("");
			
			if(keggIds.size()==1)
			{
				
				
				for (String string : keggIds) {
					spec.detectedIDs.put(DatabaseIDs.KEGG,string );
					retry=true;
				}
				
				
				// try to get new information 
				if(retry)
				{
					//PubChemId is contained, KEGG ID is contained -> try to get a Chebi ID or a KnapSack ID
//					
//					System.out.println(spec.detectedIDs.get(DatabaseIDs.KEGG)+"\t");
					String chebiID = getPubchem.getDBIDFromKEGG(spec.detectedIDs.get(DatabaseIDs.KEGG), DatabaseIDs.CHEBI);
//					
//					System.out.println(chebiID);
					if(! chebiID.equals(""))
					{
						spec.detectedIDs.put(DatabaseIDs.CHEBI, chebiID);
					}
					
					String knapsack = getPubchem.getDBIDFromKEGG(spec.detectedIDs.get(DatabaseIDs.KNAPSACK), DatabaseIDs.KNAPSACK);
					
					if(!knapsack.equals(""))
					{
						spec.detectedIDs.put(DatabaseIDs.KNAPSACK, knapsack);
					}
					
				}
				
			}
			
			rewriteSpectra( home,file,  spec.detectedIDs);
			System.out.println(file+" rewritten.");
		}
		
	}
	
	public static void status(Map<DatabaseIDs, String> detectedIDs, Set<DatabaseIDs> neededIDs, boolean somethingChanged )
	{
		Set<DatabaseIDs> idsToAdd1 = detectedIDs.keySet();
		
		for (DatabaseIDs compoundDatabases : idsToAdd1) {
			
			System.out.println(compoundDatabases.toString()+"\t"+detectedIDs.get(compoundDatabases));
			
		}
		
		System.out.println("needed: "+neededIDs.toString());
		System.out.println("something changed? "+somethingChanged);
	}
	
	public static void rewriteSpectra(String home,String file, Map<DatabaseIDs,String> detectedIDs) throws IOException
	{
		BufferedReader in = new BufferedReader(new FileReader(home+ file));

		String line = "";

		StringBuffer newSpec = new StringBuffer();
		
		boolean linksSeen=false;
		
		
		
		while ((line = in.readLine()) != null) {
		
			if (line.contains("CH$LINK")) {
				linksSeen = true;
				
				boolean lineReplaced=false;
				
				Set<DatabaseIDs> idsToAdd = detectedIDs.keySet();
				
				for (Iterator iterator = idsToAdd.iterator(); iterator
						.hasNext();) {
					
					DatabaseIDs compoundDatabases = (DatabaseIDs) iterator.next();
					
					
					if (line.contains(compoundDatabases.toString()))
					{
						String x=" ";
						if(compoundDatabases.equals(DatabaseIDs.PUBCHEM_CID) || compoundDatabases.equals(DatabaseIDs.PUBCHEM_SID) )
						{

							x=":";
							
						}
						newSpec.append("CH$LINK: "+compoundDatabases.toString()+x+detectedIDs.get(compoundDatabases)+"\n");
						
						lineReplaced=true;
						
						iterator.remove();
						//detectedIDs.remove(compoundDatabases);
//						if(line.contains("SID"))
//						{
//							newSpec
//						}
					}
				}
				
				if(!lineReplaced)
				{
					newSpec.append(line+"\n");
				}
				
				
			}
			else
			{
				
//				Set<DatabaseIDs> idsToAdd1 = detectedIDs.keySet();
//				
//				for (DatabaseIDs compoundDatabases : idsToAdd1) {
//					
//					System.out.println(compoundDatabases.toString()+"\t"+detectedIDs.get(compoundDatabases));
//					
//				}
				
				
				if(linksSeen && detectedIDs.size()>0)
				{
					Set<DatabaseIDs> idsToAdd = detectedIDs.keySet();
					
					for (Iterator iterator = idsToAdd.iterator(); iterator
							.hasNext();) {
						
						DatabaseIDs compoundDatabases = (DatabaseIDs) iterator.next();
				
							String x=" ";
							if(compoundDatabases.equals(DatabaseIDs.PUBCHEM_CID) || compoundDatabases.equals(DatabaseIDs.PUBCHEM_SID) )
							{

								x=":";
								
							}
							newSpec.append("CH$LINK: "+compoundDatabases.toString()+x+detectedIDs.get(compoundDatabases)+"\n");
							
							iterator.remove();
							//detectedIDs.remove(compoundDatabases);
						
					}

				}
				else
				{
					newSpec.append(line+"\n");
				}
			}

		}
		
		in.close();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(home+file));
		//System.out.println(newSpec.toString());
		bw.write(newSpec.toString());
		bw.flush();
		bw.close();
		
		
	}
	
}
