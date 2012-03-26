package de.ipbhalle.metfrag.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import de.ipbhalle.metfrag.spectrum.WrapperSpectrum;

public class GetAllSpecsWithID {
	
	
	public static void main(String[] args) throws IOException {
		
		
		
		String folderIn = "/home/ftarutti/testspectra/testSC/testSC0/";
		
		String folderOut ="/home/ftarutti/testspectra/Progress/";
		
		String entries[] = new File(folderIn + ".").list();
		
		for (int i = 0; i < entries.length; i++) {
			if(entries[i].endsWith(".txt"))
			{
//				System.out.println(entries[i]);
				
				System.out.println("i "+folderIn+entries[i]);
				WrapperSpectrum spectrum = new WrapperSpectrum(folderIn+entries[i]);
				
				System.out.println(spectrum.getCID());
				
				if(spectrum.getCID()!=0)
				{
					StringBuffer spec= new StringBuffer();
					
					File in = new File(folderIn+entries[i]);
					File out= new File(folderOut+entries[i]);
					
					FileUtils.copyFile(in, out);
					
//					BufferedReader in = new BufferedReader(new FileReader(folderIn+entries[i]));
//					String line="";
//					while((line = in.readLine())!=null)
//					{
//						if (!line.contains("CH$LINK: PUBCHEM CID:: "))
//							spec.append(line+"\n");
//					}
//					in.close();
//					
//					BufferedWriter out = new BufferedWriter(new FileWriter(folderOut+entries[i]));
//					
//					out.write(spec.toString());
//					out.flush();
//					out.close();
					
					
				}
				
				
			}
		}
		
	}

}
