package de.ipbhalle.metfrag.addDatabasIDs;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.ipbhalle.metfrag.spectrum.WrapperSpectrum;


public class CheckForSuccess {
	
	public static void main(String[] args) throws IOException {
		

		
		int hasAlreadyPubchem=0;
		int hasAlreadyKegg=0;
		int hasAlreadyChebi=0;
		
		int pubchemCounter=0;
		int keggCounter=0;
		int chebiCounter=0;
		
		int all=0;
		
		String nam="SC";
		
		for (int iter = 0; iter < 5; iter++) 
		{
			
		
		String kind = "testSC"+iter;
		String home =  "/home/ftarutti/testspectra/testSC/"+ kind+"/";
				
		String home2 ="/vol/massbank/data/records/";
		
		String entries[] = new File(home + ".").list();

		

		
		for (int j = 0; j < entries.length; j++) {
			
			if(entries[j].length()==12)
			{
				
			System.out.println(entries[j]);
			WrapperSpectrum spectrumNew = new WrapperSpectrum(home	+ entries[j]);
			WrapperSpectrum spectrumOld = new WrapperSpectrum(home2 + entries[j]);
			
			if(spectrumOld.getCID()!=0)
			{
				hasAlreadyPubchem++;
			}
			
			if(spectrumOld.getChebi()!=null && !spectrumOld.getChebi().equals("") && ! spectrumOld.getChebi().equals("none"))
			{
				hasAlreadyChebi++;
			}
			
			if(spectrumOld.getKEGG()!=null && !spectrumOld.getKEGG().equals("") && ! spectrumOld.getKEGG().equals("none"))
			{
				hasAlreadyKegg++;
			}
			
				
			if(spectrumOld.getCID()==0 && spectrumNew.getCID()!=0)
			{
				pubchemCounter++;
			}
			
			if((spectrumOld.getKEGG().equals("")||spectrumOld.getKEGG().equals("none")||spectrumOld.getKEGG()==null) && spectrumNew.getKEGG()!=null && !spectrumNew.getKEGG().equals("") && ! spectrumNew.getKEGG().equals("none"))
			{
				keggCounter++;
			}
			
			if((spectrumOld.getChebi().equals("")||spectrumOld.getChebi().equals("none")||spectrumOld.getChebi()==null) && spectrumNew.getChebi()!=null && !spectrumNew.getChebi().equals("") && ! spectrumNew.getChebi().equals("none"))
			{
				chebiCounter++;
			}
			
			all++;
			}
		}
		
		//all+=entries.length;
		
		}
		BufferedWriter out = new BufferedWriter(new FileWriter("/home/ftarutti/Desktop/"+nam+".txt"));
		out.write("Before: ");
		out.newLine();
		out.write("Pubchem: "+hasAlreadyPubchem);
		out.newLine();
		out.write("KEGG: "+hasAlreadyKegg);
		out.newLine();
		out.write("Chebi: "+hasAlreadyChebi);
		out.newLine();
		out.newLine();
		out.write("After treatment:");
		out.newLine();
		out.write("Pubchem: +"+pubchemCounter+" = "+(pubchemCounter+hasAlreadyPubchem));
		out.newLine();
		out.write("KEGG: +"+keggCounter+" = "+(keggCounter+hasAlreadyKegg));
		out.newLine();
		out.write("Chebi: +"+chebiCounter+" = "+(chebiCounter+hasAlreadyChebi));
		out.newLine();
		out.newLine();
		out.write("gesamt: "+all);
		
		out.close();
	}

}
