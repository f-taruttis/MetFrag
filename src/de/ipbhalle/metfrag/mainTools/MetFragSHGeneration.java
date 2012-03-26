package de.ipbhalle.metfrag.mainTools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

import de.ipbhalle.metfrag.spectrum.WrapperSpectrum;

public class MetFragSHGeneration {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		//"/home/swolf/MOPAC/BATCH/jar/PreprocessMolecules.jar" "/home/swolf/MOPAC/ProofOfConcept/pubchem/" "/home/swolf/MOPAC/BATCH/sh/" 600 600
		
//		String writePath = "/home/swolf/MOPAC/BondOrderTests/Hill_ProofOfConcept/MFsh/";
//		String pathToSpectra = "/home/swolf/MOPAC/BondOrderTests/Hill_ProofOfConcept/testData/";
//		String pathToJar = "/home/swolf/MOPAC/BondOrderTests/Hill_ProofOfConcept/jar/MetFragPreCalculated.jar";
//		String pathToPreCalculatedCML = "/home/swolf/MOPAC/BondOrderTests/Hill_ProofOfConcept/mopac_2400/";
		
		String writePath = "/home/ftarutti/restsh/";
		String pathToSpectra = "/home/swolf/MOPAC/Hill-Riken-MM48_POSITIVE_PubChem_LocalMass2009_CHONPS_NEW/spectra/";
		String pathToJar = "/home/ftarutti/ChemFrag/Scoring/score.jar";
		String pathToPreCalculatedCML = "/home/swolf/MOPAC/Hill-Riken-MM48_POSITIVE_PubChem_LocalMass2009_CHONPS_NEW/mopac_4800/";
		String configPath = "/home/ftarutti/workspace/MetFrag/src/";
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	    java.util.Date date = new java.util.Date();
		String dateString = dateFormat.format(date);
		
		if(!writePath.contains("sh/"))
		{
			System.err.println("Please use a folder structure that uses /.../.../sh/ at the end");
			System.exit(1);
		}
		
		//loop over all files in folder
//		FileUtils.deleteDirectory(new File(writePath));
		new File(writePath).mkdirs();
		
		File[] spectraFolder = new File(pathToSpectra).listFiles();
		String spectra [] ={"CO000466CO000467CO000468CO000469CO000470=444.1532" ," CO000111CO000112CO000113CO000114CO000115=548.2985" ," CO000416CO000417CO000418CO000419CO000420=350.163" ," CO000386CO000387CO000388CO000389CO000390=608.2734" ," CO000161CO000162CO000163CO000164CO000165=348.1685" ," CO000216CO000217CO000218CO000219CO000220=598.2791" ," CO000066CO000067CO000068CO000069CO000070=267.1259" ," CO000071CO000072CO000073CO000074CO000075=539.2802" ," CO000251CO000252CO000253CO000254CO000255=429.2515" ," CO000331CO000332CO000333CO000334CO000335=460.1482" ," CO000171CO000172CO000173CO000174CO000175=609.2951" ," CO000246CO000247CO000248CO000249CO000250=555.2693" ," CO000276CO000277CO000278CO000279CO000280=339.1947" ," CO000256CO000257CO000258CO000259CO000260=241.1103" ," CO000186CO000187CO000188CO000189CO000190=287.1521" ," CO000271CO000272CO000273CO000274CO000275=454.1713" ," CO000241CO000242CO000243CO000244CO000245=255.0895" ," CO000311CO000312CO000313CO000314CO000315=274.143" ," CO000486CO000487CO000488CO000489CO000490=370.1537" ," CO000291CO000292CO000293CO000294CO000295=274.1933" ," CO000346CO000347CO000348CO000349CO000350=340.1907" ," CO000481CO000482CO000483CO000484CO000485=399.1803" ," CO000421CO000422CO000423CO000424CO000425=386.2028" ," CO000281CO000282CO000283CO000284CO000285=461.1686" ," CO000396CO000397CO000398CO000399CO000400=415.2722" ," CO000491CO000492CO000493CO000494CO000495=443.1701" ," CO000146CO000147CO000148CO000149CO000150=543.174"};
		
		Arrays.sort(spectraFolder);
//		for (File file : spectraFolder) 
		for (int i = 0; i < spectra.length; i++) 
		{
			
			
			String fileNameSpec = (spectra[i].split("=")[0]+".txt").trim();
			
			System.out.println(fileNameSpec);
			
			File file = new File(pathToSpectra + fileNameSpec);
			
			System.out.println(file.getPath());
			if(!file.isFile())
			{	
//				System.out.println(file.getName());
				continue;
				
			}
			String fileName = file.getName();
			int dotPos = fileName.indexOf(".");
			String extension = "";
			if(dotPos >= 0)
				extension = fileName.substring(dotPos);
			
			WrapperSpectrum spectrum = new WrapperSpectrum(file.toString());
			
			File f2 = new File(writePath + "sge_" + file.getName().split("\\.")[0] + ".sh"); 
			
			BufferedWriter out = new BufferedWriter(new FileWriter(f2));
			out.write("#!/bin/bash");
			out.newLine();
			//CID_3365_spectrum.txt 2011-02-02_16-00-00 /home/swolf/MOPAC/Hill_PubChem_Formula/pubchemClusteredMopac/mopac_600/C30H60N3O3/ 1
//			out.write("java -Dproperty.file.path=" + pathToSpectra + "config/" + " -Xms1500m -Xmx5500m -jar " + pathToJar + " \"" + file.getPath() + "\" \"" + dateString + "\" \"" + new File(pathToPreCalculatedCML) + "/" + spectrum.getFormula().trim() + "/\"");
			out.write("java -Dproperty.file.path=" + configPath + " -Xms1500m -Xmx5500m -jar " + pathToJar + " \"" + file.getPath() + "\" \"" + dateString + "\" \"" + new File(pathToPreCalculatedCML) + "/\"");
		  	out.close();

		}
	}
}
