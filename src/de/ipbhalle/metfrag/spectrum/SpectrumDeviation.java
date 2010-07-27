package de.ipbhalle.metfrag.spectrum;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.ipbhalle.metfrag.fragmenter.Candidates;
import de.ipbhalle.metfrag.fragmenter.FragmenterThread;
import de.ipbhalle.metfrag.main.Config;
import de.ipbhalle.metfrag.main.MetFrag;
import de.ipbhalle.metfrag.main.MetFragResult;
import de.ipbhalle.metfrag.massbankParser.Peak;
import de.ipbhalle.metfrag.pubchem.PubChemWebService;
import de.ipbhalle.metfrag.tools.MolecularFormulaTools;
import de.ipbhalle.metfrag.tools.PPMTool;

public class SpectrumDeviation {
	
	
	private void analyseSpectra(String folder, String database, double mzabs, double mzppm) throws Exception
	{
		//loop over all files in folder
		File f = new File(folder);
		File files[] = f.listFiles();
		Arrays.sort(files);
		
		//create new folder
		String path = folder + "logs/deviation/";
		new File(path).mkdirs();
		
		FileWriter fstream = new FileWriter(path + "spectrumDeviation.txt", true);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write("File\tAverage\tMedian\tDev. Molpeak\n");
        out.close();
		
		for(int i=0;i<files.length-1;i++)
		{
			if(files[i].isFile())
			{
				WrapperSpectrum spectrum = new WrapperSpectrum(files[i].toString());
				
				//only 1 result because only the correct compound is fragmented
				List<MetFragResult> result = MetFrag.startConvenience(database, Integer.toString(spectrum.getCID()), "", spectrum.getExactMass(), new WrapperSpectrum(files[i].toString()), false, mzabs, mzppm, 0.0, true, true, 2, true, false, true, false, Integer.MAX_VALUE, true);
				List<Double> deviations = new ArrayList<Double>();
				
				for (PeakMolPair explainedPeak : result.get(0).getFragments()) {
					deviations.add(Math.abs(PPMTool.getPPMWeb(explainedPeak.getMatchedMass(), explainedPeak.getPeak().getMass())));
				}
				
				double total = 0.0;
				for (Double dev : deviations) {
					total += dev;
				}
				
				double average = total / deviations.size();
				double median = 0.0;
				Double[] devArray = new Double[deviations.size()]; 
				devArray = deviations.toArray(devArray);
				Arrays.sort(devArray);
				for (int j = 0; j < devArray.length; j++) {
					//median von gerader anzahl
					if(j == (devArray.length/2) && (j%2) == 0)
					{
						median = devArray[j];
					}
					else if(j == (devArray.length/2))
					{
						median = devArray[j];
					}
				}
				
				
				double calculatedPeak = spectrum.getExactMass() + MolecularFormulaTools.getMonoisotopicMass("H1"); 
				Peak closestPeak = new Peak(0.0, 1.0, 1);
				double minDist = Double.MAX_VALUE;
				for (Peak peak : spectrum.getPeakList()) {
					if(Math.abs((peak.getMass() - calculatedPeak)) < minDist)
					{
						minDist = Math.abs(peak.getMass() - calculatedPeak);
						closestPeak = peak;
					}
				}
				
				
				double deviation = 2.5;
				Double deviationMolPeak = 0.0;
				if(closestPeak.getMass() > (calculatedPeak - deviation) && closestPeak.getMass() < (calculatedPeak + deviation))
				{
					deviationMolPeak = Math.abs(PPMTool.getPPMWeb(calculatedPeak, closestPeak.getMass()));
				}
				
				
				//write everything to log file
				System.out.println(files[i].getName() + "\t" + average + "\t" + median + "\t" + deviationMolPeak);
				
				fstream = new FileWriter(path + "spectrumDeviation.txt", true);
		        out = new BufferedWriter(fstream);
				out.write(files[i].getName() + "\t" + average + "\t" + median + "\t" + deviationMolPeak + "\n");
				out.close();
			}
		}
		
		out.close();
	}
	
	
	public static void main(String[] args) {
//		String folder = "/home/swolf/MassBankData/MetFragSunGrid/RikenDataMerged/CHONPS/useable/";
		String folder = "/home/swolf/MassBankData/MetFragSunGrid/HillPaperDataMerged/";
		SpectrumDeviation sd = new SpectrumDeviation();
		try {
			sd.analyseSpectra(folder, "pubchem", 0.01, 10.0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}