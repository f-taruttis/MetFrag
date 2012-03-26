package de.ipbhalle.metfrag.newScoring;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.ipbhalle.metfrag.spectrum.WrapperSpectrum;

public class ClaculateRRPFromResults {
	
	public static void main(String[] args) throws IOException {
		
		StringBuffer results = new StringBuffer();	
		results.append("ID\tRRP\tmass\n");
		
		StringBuffer eval2 = new StringBuffer();
		
		double counter99=0;
		double counter95=0;
		double counter90=0;
		double counter=0;
		
		String home = "/home/ftarutti/ChemFrag/Scoring/tmp/res1/";
		File fileDir = new File(home);
		
		
		String spectraHome = "/home/swolf/MOPAC/Hill-Riken-MM48_POSITIVE_PubChem_LocalMass2009_CHONPS_NEW/spectra/";
		File fileSpecs = new File(spectraHome);
		
		Map<String,Double> spectraToMass = new HashMap<String, Double>();
		
		String[] spectra = fileSpecs.list();
		
		for (int i = 0; i < spectra.length; i++) {
			
			if(spectra[i].endsWith(".txt"))
			{
			WrapperSpectrum spectrum = new WrapperSpectrum(spectraHome+spectra[i]);
			spectra[i] = spectra[i].replace(".txt", "");
			
			spectraToMass.put(spectra[i], spectrum.getExactMass());
			}
		}
		
		
		//Iterate over all files
		
		String[] files = fileDir.list();
		
		for (int j = 0; j < files.length; j++) {
			
		if(files[j].startsWith("res1_"))
		{
			
			String name = files[j].replace(".txt", "");
			name = name.replace("res1_","");
			
			double mass =0;
			if(spectraToMass.containsKey(name))
			{
				mass = spectraToMass.get(name);
				spectraToMass.remove(name);
			}
		
		String file =files[j];
		
		BufferedReader in = new BufferedReader(new FileReader(home+file));
		
		
		String line=null;
		
		boolean correctRead = false;
		
		Map< String , double[] > candidateToBetterAndEqual = new HashMap<String, double[]>();  
		Map<String,Double> candidateToScore = new HashMap<String, Double>();
		
		int better=0;
		int totalNumberOfCandidates=0;
		
		String correct="";
		
		while( (line=in.readLine())!=null )
		{
			
			if(!line.startsWith("correct") && !correctRead)
			{
				String splitLine[] = line.split("\t");
				double score = Double.parseDouble(splitLine[0]);
				
				String candidates = splitLine[1].replace("[", "");
				candidates = candidates.replace("]", "");
//				System.out.println(candidates);
				String []candidatesList = candidates.split(",");
				
//				System.out.println(score + "belongs to: ");
				
	
				int equal = candidatesList.length;
				double[] betterAndEqual = new double[2];
				betterAndEqual [0] = better;
				betterAndEqual [1] = equal;
				
				for (int i = 0; i < candidatesList.length; i++) {
					
					candidatesList[i] = candidatesList[i].split("_")[0].trim();	
//					System.out.println("\t"+candidatesList[i]);
					
					candidateToScore.put(candidatesList[i], Double.parseDouble(splitLine[0]));
					candidateToBetterAndEqual.put(candidatesList[i], betterAndEqual);
				}
				
				totalNumberOfCandidates+=candidatesList.length;
				
				better += candidatesList.length;
			}
			
			if(line.startsWith("correct"))
			{
				correct = line.replace("correct:", "");
				correct = correct.trim();
				
				System.out.println("Correct CID: "+correct);
				
				
				correctRead=true;
			}
		}
		
		in.close();
		
//		Set<String> candidates = candidateToBetterAndEqual.keySet();
//		
//		for (String string : candidates) {
//			
//			System.out.println(string+"\t better: "+candidateToBetterAndEqual.get(string)[0]+"\t equal: "+candidateToBetterAndEqual.get(string)[1]);
//			
//		}
//		
//		String certain = "24666685";
//		
//		System.out.println(certain+"\t"+candidateToBetterAndEqual.get(certain)[0]+"\t"+candidateToBetterAndEqual.get(certain)[1]);
//	

		double [] valuesForCorrect = new double[2];
		valuesForCorrect = candidateToBetterAndEqual.get(correct);
		
		System.out.println(correct+"\t"+valuesForCorrect[0]+"\t"+valuesForCorrect [1]);
		
		System.out.println(totalNumberOfCandidates);
		
		double worseCandidates = totalNumberOfCandidates-(valuesForCorrect[0]+valuesForCorrect[1]);
		
		double rrp = 0.5*(1.0 + (valuesForCorrect[0]-worseCandidates)/(totalNumberOfCandidates-1));
		
		results.append(correct+"\t"+rrp+"\t"+mass+"\n");
		
		double correctScore = candidateToScore.get(correct);
//		System.out.println("SCORE: "+correctScore);
		
		counter++;
		
		if(correctScore>0.99)
		{
			counter99++;
			counter95++;
			counter90++;
		}
		else
		{
			if (correctScore>0.95) {
				counter95++;
				counter90++;
			}
			else
			{
				if(correctScore>0.9)
				{
					counter90++;
				}
			}
		}
		
		
		System.out.println(rrp);
	}
		}
		
		eval2.append((counter99/counter)+"\t"+(counter95/counter)+"\t"+(counter90/counter));
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(home+"Results2.txt"));
		bw.write(results.toString());
		bw.flush();
		bw.close();
		
		BufferedWriter bw2 = new BufferedWriter(new FileWriter(home+"Eval2.txt"));
		bw2.write(eval2.toString());
		bw2.flush();
		bw2.close();
		
		System.out.println(spectraToMass.toString());
		System.out.println(spectraToMass.size());
	}

}
