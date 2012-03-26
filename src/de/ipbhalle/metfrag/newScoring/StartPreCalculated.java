package de.ipbhalle.metfrag.newScoring;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import de.ipbhalle.metfrag.fragmenter.Candidates;
import de.ipbhalle.metfrag.fragmenter.FragmenterResult;
import de.ipbhalle.metfrag.fragmenter.SubMolecule;
import de.ipbhalle.metfrag.fragmenter.SubstructureGenerator;
import de.ipbhalle.metfrag.main.Config;
import de.ipbhalle.metfrag.read.CMLMolecule;
import de.ipbhalle.metfrag.read.CMLTools;
import de.ipbhalle.metfrag.spectrum.WrapperSpectrum;
import de.ipbhalle.metfrag.tools.Constants;
import de.ipbhalle.metfrag.newScoring.NewScoring2;

public class StartPreCalculated {
	
	double mzabs;
	double mzppm;
	
	Map<Double , Set<String>> scoreMap;
	
	
	
	boolean useBondOrders;
	
	public StartPreCalculated(double mzabs, double mzppm, boolean useBondOrders)
	{
		this.mzabs=mzabs;
		this.mzppm=mzppm;
		
		this.useBondOrders = useBondOrders;
		
		this.scoreMap= new HashMap<Double, Set<String>>();
	
	}
	
	void start(String file, File folderToMopac, String tag) throws Exception
	{
		
		String[] path = file.split("/");
		
		String fileName = path[path.length-1];
		
		String resultsFile ="/home/ftarutti/ChemFrag/Scoring/alaninRes/res"+tag+"_"+fileName;
		String timesFile ="/home/ftarutti/ChemFrag/Scoring/alaninRes/time"+tag+"_"+fileName;
		
		StringBuffer time = new StringBuffer();

		// get configuration
		
	
		
		Config config = new Config("outside");
		WrapperSpectrum spectrum = new WrapperSpectrum(file,0.0);

		String database = config.getDatabase();
		List<CMLMolecule> candidates = new ArrayList<CMLMolecule>();

		//get correct candidate
		String correctCandidate = spectrum.getCID()+"";  
		
		// now get the possible mol formulas
		List<String> candidatesTemp = Candidates.queryLocally("pubchem",
				spectrum.getExactMass(), 10.0, config.getJdbc(),
				config.getUsername(), config.getPassword());
		Map<String, String> molFormulas = new HashMap<String, String>();
		for (String candString : candidatesTemp) {
			IAtomContainer mol = Candidates.getCompoundLocally("pubchem",
					candString, config.getJdbc(), config.getUsername(),
					config.getPassword(), false, config.getChemspiderToken());

			if (mol == null)
				continue;

			try {
				AtomContainerManipulator
						.percieveAtomTypesAndConfigureAtoms(mol);
				CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(mol
						.getBuilder());
				hAdder.addImplicitHydrogens(mol);
				AtomContainerManipulator
						.convertImplicitToExplicitHydrogens(mol);
				molFormulas.put(MolecularFormulaManipulator
						.getString(MolecularFormulaManipulator
								.getMolecularFormula(mol)), "");
			} catch (CDKException e) {
				System.err.println(e.getMessage());
			}
		}

		String[] molFormulaArray = new String[molFormulas.keySet().size()];
		int countTemp = 0;
		for (String molFormula : molFormulas.keySet()) {
			molFormulaArray[countTemp] = molFormula;
			countTemp++;
			System.out.print(molFormula + " ");
		}

		System.out.println();
		candidates = CMLTools.readFoldersLowestHoF(folderToMopac,
				molFormulaArray);

		
		//Iterate over candidates , fragmentation and scoring
		
		
		
		for (int c = 0; c < candidates.size(); c++) {
			

			IAtomContainer molecule = candidates.get(c).getMolStructure();
			
			if(ConnectivityChecker.isConnected(molecule))
			{

			IAtomContainer moleculeR = AtomContainerManipulator.removeHydrogens(molecule);
			
//			Fragmentation:
			
			long fragmentationTimeStart = System.currentTimeMillis();
			
			Map<IAtomContainer,SubMolecule> fragments = new HashMap<IAtomContainer, SubMolecule>(); 			
			SubstructureGenerator fragmenter = new SubstructureGenerator(moleculeR, spectrum.getPeakList());
			fragments = fragmenter.getMoleculeSet();
			
			long fragmentationTimeEnd = System.currentTimeMillis();
			
			long fragmentationTime = fragmentationTimeEnd - fragmentationTimeStart;
			
			Set<IAtomContainer> keys = fragments.keySet();
			

			
//			Scoring:
			
//			System.out.println("Scoring: ");
			
			String candidateID=candidates.get(c).getFileName().split("\\.")[0];
			
			long scoringTimeStart = System.currentTimeMillis();
			
			NewScoring2 scoring = new NewScoring2(candidateID, fragments, spectrum, mzabs, mzppm, moleculeR , this.useBondOrders);
		
			scoring.getOptimalAssignment();
			
			long scoringTimeEnd = System.currentTimeMillis();
			
			long scoringTime = scoringTimeEnd - scoringTimeStart;
			
			double score = Math.round(scoring.getScore()*1000000000.0)/1000000000.0;
			
			if(scoreMap.containsKey(score))
			{
				Set<String> candidateIDs = scoreMap.get(score);
				
				candidateIDs.add(candidateID);
							
				scoreMap.put(score, candidateIDs);
			}
			else
			{
				Set<String> candidateIDs = new HashSet<String>();
				
				candidateIDs.add(candidateID);
				
				scoreMap.put(score, candidateIDs);
			}
			
			String timeString = candidateID+"\t"+fragmentationTime+"\t"+scoringTime+"\n";
			
			time.append(timeString);
			
		}}
		
		
		Set<Double> keys = scoreMap.keySet();
		
		Double[] keyArray = new Double[keys.size()];
		keyArray = keys.toArray(keyArray);
		
		Arrays.sort(keyArray, Collections.reverseOrder());
		
		StringBuffer results = new StringBuffer();
		
		int rank=0;
		double betterCandidates = 0;
		double worseCandidates = 0;
		
		boolean correctNotFound=true;
		
		for (int i = 0; i < keyArray.length; i++) {
			
			Set<String> ids = scoreMap.get(keyArray[i]);
//			System.out.println(keyArray[i]+"\t"+scoreMap.get(keyArray[i]).toString());
			
			results.append(keyArray[i]+"\t"+scoreMap.get(keyArray[i]).toString()+"\n");
			
			if(!correctNotFound)
			{
				worseCandidates+=ids.size();
			}
			
			for (String string : ids) {
				
				if(string.equals(correctCandidate) && correctNotFound)
				{
					rank = i;
					correctNotFound=false;
				}
				
			}
			
			if(correctNotFound)
			{
				betterCandidates+=ids.size();
			}
			
			
			
		}
		
//		calculate Relative ranking position:

//		System.out.println("correct: "+correctCandidate);
		results.append(("correct: "+correctCandidate)+"\n");
		
//		System.out.println("rank: "+rank+"\t"+candidates.size());
		results.append("rank: "+rank+"\t"+candidates.size()+"\n");
		
//		System.out.println("better: "+betterCandidates +"\tworse "+worseCandidates+"");
		results.append("better: "+betterCandidates +"\tworse "+worseCandidates+"\n");
		
		double rrp = 0.5*(1.0 + (betterCandidates-worseCandidates)/(candidates.size()-1));
		
//		System.out.println("RRP: "+(rrp));
		
		results.append("###RRP "+rrp);
		
		
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(resultsFile));
		
		bw.write(results.toString());
		bw.flush();
		bw.close();
		
		BufferedWriter bwt = new BufferedWriter(new FileWriter(timesFile));
		
		bwt.write(time.toString());
		bwt.flush();
		bwt.close();
		
		
	}
	


	/**
     * Prints the smiles.
     *
     * @param atom the atom
     */
    private static String printSmiles(IAtomContainer atom)
    {
    	SmilesGenerator sg = new SmilesGenerator();
    	
    	String smiles = sg.createSMILES(atom);
		
		//System.out.println(smiles);
    	return smiles;
    }
	
    
	public static void main(String[] args) throws Exception {
		
		double mzabs=0.01;
		double mzppm=10;
		
		boolean useBondOrders=true;
		
		StartPreCalculated run = new StartPreCalculated(mzabs,mzppm,useBondOrders);

		
		String spectraPath = args[0];
		String date = args[1];
		String mopacPath = args[2];
		
		String tag;
		if(mzabs == 0.0)
		{
			tag="0";
		}
		else
		{
			tag="1";
		}
		
//		String home = "/home/ftarutti/MetFrag/";
//		String spectrum ="CO000411CO000412CO000413CO000414CO000415.txt";
		
//		String file = args[0];
		
//		File folderToMopac = new File( "/home/swolf/MOPAC/Hill-Riken-MM48_POSITIVE_PubChem_LocalMass2009_CHONPS_NEW/mopac_4800/");		
//		String spectraPath = "/home/swolf/MOPAC/Hill-Riken-MM48_POSITIVE_PubChem_LocalMass2009_CHONPS_NEW/spectra/"+file;
//		String mopacPath = "/home/ftarutti/MetFrag/alanine/mopac/";
		
		
		File folderToMopac = new File(mopacPath);
		
		run.start(spectraPath ,folderToMopac, tag);
		
	}

}
