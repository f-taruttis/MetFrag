package de.ipbhalle.metfrag.addDatabasIDs;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import de.ipbhalle.metfrag.massbankParser.DatabaseIDs;
import de.ipbhalle.metfrag.massbankParser.MassbankParser;
import de.ipbhalle.metfrag.massbankParser.NewMassbankParser;
import de.ipbhalle.metfrag.massbankParser.Peak;
import de.ipbhalle.metfrag.massbankParser.Spectrum;

//This class only adds KnapSack IDs
public class SmallIDGetter {
	
	DatabaseIDs searchDB;
	
	Map<String,Map<DatabaseIDs,String>> linksToKEGG;
	Map<String,String> knapsackKEGGcomp;
	Map<String,String> keggKnapsack;
	
	
	public SmallIDGetter(DatabaseIDs databaseToGet) {
		
		this.searchDB = databaseToGet;
		
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
		
		if(this.searchDB.equals(DatabaseIDs.KNAPSACK))
		{
			knapsackKEGGcomp= new HashMap<String, String>();

			keggKnapsack= new HashMap<String, String>();
		}
		
	}
	
	public void fillDBToKEGG(DatabaseIDs database) throws IOException
	{

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
			
				if(database.equals(DatabaseIDs.KNAPSACK))
				{
					dbID = columns[1].replace("knapsack:", "");
					if(keggID.startsWith("C"))
						knapsackKEGGcomp.put(dbID, keggID);
					keggKnapsack.put(keggID, dbID);
				}
				else
				{
					System.out.println(database.toString()+" not supported yet.");
					break;
				}
		}
		
		in.close();
		
		
		}

	}
	
	
	public String getKnapSack(Spectrum spectra)
	{
		System.out.println(spectra.getKEGG());
		System.out.println(spectra.getKnapsack().length());
		if(spectra.getKnapsack()!=null && !spectra.getKnapsack().equals("")&& !spectra.getKnapsack().equals("none"))
		{
			System.out.println("hier");
			
			return spectra.getKnapsack();
			
		}
		else
		{
			String kegg = spectra.getKEGG();
			
			if(kegg.equals("")|| kegg.equals("none")||kegg==null)
			{
				return null;
			}
			else
			{
				if(this.searchDB.equals(DatabaseIDs.KNAPSACK))
				{
					if(keggKnapsack.containsKey(kegg))
						return keggKnapsack.get(kegg);
					else
						return "";
				}
			}
			
		}
		return "";
		
	}
	

	
	static void rewriteSpectrum(String spectraFile,String file, String knapsack) throws IOException
	{
		boolean linkSet=false;
		
		BufferedReader in = new BufferedReader(new FileReader(spectraFile));
		
		StringBuffer out = new StringBuffer();
		
		String line="";
		while((line=in.readLine())!=null)
		{
			if(line.contains("CH$LINK") && linkSet==false)
			{
				out.append("CH$LINK:"+" KNAPSACK "+knapsack+"\n");
				out.append(line+"\n");
				
				linkSet=true;
				
			}
			else
			{
				out.append(line+"\n");
			}
		}
		
		in.close();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		
		bw.write(out.toString());
		bw.flush();
		bw.close();
		
		
	}
	
	public static void main(String[] args) {
		
		SmallIDGetter getKnapsack = new SmallIDGetter(DatabaseIDs.KNAPSACK);
		
		Vector<Spectrum> spectra = NewMassbankParser.Read("/home/ftarutti/Desktop/testSpec"+".txt");
		
		Spectrum thisSpec=null;
		for (Spectrum spectrum : spectra) {
			
			thisSpec = spectrum;
			
			
			}
		
		
	
		
		try {
			getKnapsack.fillDBToKEGG(DatabaseIDs.KNAPSACK);
			String knapsack = getKnapsack.getKnapSack(thisSpec);
			
			System.out.println(knapsack);
			
			rewriteSpectrum("/home/ftarutti/Desktop/testSpec"+".txt","/home/ftarutti/Desktop/testSpec"+"REWRITE.txt",knapsack);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}

}
