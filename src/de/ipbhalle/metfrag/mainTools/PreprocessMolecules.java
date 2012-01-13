package de.ipbhalle.metfrag.mainTools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.aromaticity.AromaticityCalculator;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IRing;
import org.openscience.cdk.interfaces.IRingSet;
import org.openscience.cdk.io.CMLWriter;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import de.ipbhalle.metfrag.bondPrediction.BondPrediction;
import de.ipbhalle.metfrag.bondPrediction.CalculationResult;
import de.ipbhalle.metfrag.tools.MoleculeTools;
import de.ipbhalle.metfrag.tools.Writer;

public class PreprocessMolecules {
	
	// /home/swolf/MOPAC/ProofOfConcept/pubchem/CID_20097272_spectrum/20097272.sdf /home/swolf/MOPAC/ProofOfConcept/pubchem/CID_20097272_spectrum/mopac/ 600 600 /home/swolf/MOPAC/ProofOfConcept/pubchem/CID_20097272_spectrum/mopac/mopacDebug.txt
	// LARGE BUG: /home/swolf/MOPAC/ProofOfConcept/pubchem/CID_3002977_spectrum/3002977.sdf /home/swolf/MOPAC/ProofOfConcept/pubchem/CID_3002977_spectrum/mopac/ 600 600 /home/swolf/MOPAC/ProofOfConcept/pubchem/CID_3002977_spectrum/mopac/mopacDebug.txt
	public static void main(String[] args) {
		
		String homeFolder = "/home/ftarutti/MetFrag/alanine/pubchem/AlaninTest/";
		
		File f = new File(homeFolder);
		File files[] = f.listFiles();
		
		
		
		//File file = null;
		
		int mopacRuntime = 4800;
		int ffSteps = 4800;		
		//String outputMOPACDebug = "/home/ftarutti/MetFrag/alanine/mopac/debug.info";
		
//		if(args.length < 3)
//		{
//			System.err.println("Not all parameters given!");
//			System.exit(1);
//		}
//		else
//		{
//			//file = new File(args[0]);
//			outputFolder = args[1];
//			mopacRuntime = Integer.parseInt(args[2]);
//			if(args.length > 3)
//				ffSteps = Integer.parseInt(args[3]);
//			if(args.length > 4)
//				outputMOPACDebug = args[4];
//		}
		
		int counter = 0;
		
		for (File file : files) {
			
		
		
		if(file.isFile()  && file.toString().endsWith("sdf"))
		{
			String outputFolder = "/home/ftarutti/MetFrag/alanine/mopac/";
			
			String outputMOPACDebug = "/home/ftarutti/MetFrag/alanine/mopac/debug"+counter+".info";
			
			System.out.println("################################  "+counter);
			counter++;
			try {
				MDLV2000Reader reader = new MDLV2000Reader(new FileReader(file));
//				MDLReader reader = new MDLReader(new FileReader(new File("/vol/mirrors/kegg/mol/C00509.mol")));
				ChemFile chemFile = (ChemFile)reader.read((ChemObject)new ChemFile());
		        List<IAtomContainer> molList = ChemFileManipulator.getAllAtomContainers(chemFile);
		        IAtomContainer molecule = molList.get(0);
		        
				//add hydrogens
		        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
		        CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(molecule.getBuilder());
		        hAdder.addImplicitHydrogens(molecule);
		        AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);
		        
		        outputFolder += MolecularFormulaManipulator.getString(MolecularFormulaManipulator.getMolecularFormula(molecule)) + "/";
		        
		        //Skip already calculated files!
		        File tempFileWoExtentension = new File(outputFolder + file.getName().split("\\.")[0] + "_Combined.cml");
		        File tempFile = new File(outputFolder + file.getName() + "_Combined.cml");
		        if(tempFile.exists() || tempFileWoExtentension.exists())
		        	System.exit(0);
		        
		        
		        //mark all the bonds and atoms with numbers --> identify them later on        
		        molecule = MoleculeTools.moleculeNumbering(molecule);
		        
		        //do ring detection with the original molecule
		        AllRingsFinder allRingsFinder = new AllRingsFinder();
		        allRingsFinder.setTimeout(100000);
		        IRingSet allRings = allRingsFinder.findAllRings(molecule);
		        List<IBond> aromaticBonds = new ArrayList<IBond>();
		        
		        CDKHueckelAromaticityDetector.detectAromaticity(molecule);
		        
		    	for (IBond bond : molecule.bonds()) {
		            //lets see if it is a ring and aromatic
		            IRingSet rings = allRings.getRings(bond);
		            //don't split up aromatic rings...see constructor for option
		            for (int i = 0; i < rings.getAtomContainerCount(); i++) {
		            	if(MoleculeTools.ringIsAromatic((IRing)rings.getAtomContainer(i)))
		            	{
		            		aromaticBonds.add(bond);
		            	}
		            }
		        }
		    	
		    	BondPrediction bp = new BondPrediction(aromaticBonds);
			    bp.debug(false);
//			    System.out.println("MOPAC runtime: " + mopacRuntime + " FFSteps: " + ffSteps);
			    //use babel version 2.3.0
				bp.calculateBondsToBreak(file.getName(), "/vol/local/bin/","run_mopac7", molecule, 4800, "UFF", "AM1, GEO-OK, ECHO, MMOK, XYZ, BONDS", 4800, false, true);
				List<CalculationResult> results = bp.getResults();
				
				if(molecule.getProperty("candidatesClustered") != null)
				{
					String temp = (String)molecule.getProperty("candidatesClustered");
					String[] clusteredCompounds = temp.split("_");
					
					for (int c = 0; c < clusteredCompounds.length; c++) {
						for (int i1 = 0; i1 < results.size(); i1++) {
							try {
								new File(outputFolder).mkdirs();
								CMLWriter writerCML = new CMLWriter(new FileOutputStream(new File(outputFolder + clusteredCompounds[c] + "_" + results.get(i1).getProtonatedAtom() + ".cml")));
								//thats the molecule containing the all the bond length changes from all protonation sites
								
								if(i1 == 0)
								{
									writerCML.write(results.get(i1).getOriginalMol());
								}
								else
								{
									results.get(i1).getOriginalMol().setID(Double.toString((Double)results.get(i1).getOriginalMol().getProperty("HeatOfFormation")));
									writerCML.write(results.get(i1).getOriginalMol());
								}
								writerCML.close();
								
								//only do this for the one compound and not for every isomorph structure
								//write the mopac debug messages in one file
								// Create file 
								if(c == 0)
								{
									if(!outputMOPACDebug.equals(""))
									{
										Writer.writeToFile(outputMOPACDebug, file.getName() + "_" +  results.get(i1).getDebugMessages());
									}
								}								
							} catch (CDKException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
				else
				{
					for (int i1 = 0; i1 < results.size(); i1++) {
						try {
							new File(outputFolder).mkdirs();
							CMLWriter writerCML = new CMLWriter(new FileOutputStream(new File(outputFolder + file.getName() + "_" + results.get(i1).getProtonatedAtom() + ".cml")));
							//thats the molecule containing the all the bond length changes from all protonation sites
							if(i1 == 0)
							{
								writerCML.write(results.get(i1).getOriginalMol());
							}
							//thats the mol containing the individual changes from one protonation site
							else
							{
								results.get(i1).getOriginalMol().setID(Double.toString((Double)results.get(i1).getOriginalMol().getProperty("HeatOfFormation")));
								writerCML.write(results.get(i1).getOriginalMol());
							}
							writerCML.close();
							
							//write the mopac debug messages in one file
							// Create file 
							if(!outputMOPACDebug.equals(""))
							{
								Writer.writeToFile(outputMOPACDebug, file.getName() + "_" +  results.get(i1).getDebugMessages());
							}
						    
							
						} catch (CDKException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
				
			} catch (FileNotFoundException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			} catch (CDKException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
			
		}
	}}
}
