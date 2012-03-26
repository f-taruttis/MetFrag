package de.ipbhalle.metfrag.mainTools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import de.ipbhalle.metfrag.fragmenter.Candidates;
import de.ipbhalle.metfrag.main.Config;
import de.ipbhalle.metfrag.pubchem.PubChemWebService;
import de.ipbhalle.metfrag.spectrum.WrapperSpectrum;


public class RetrieveCompoundsByMass {
	
	private static IAtomContainer getPubChemEntryFromSdfGZ(String pubChemCID, File[] pubchemFiles) throws IOException
	{
		IAtomContainer molToReturn = null;
		String pathToFile = "";
		int pid = Integer.parseInt(pubChemCID);
		
		for (int i = 0; i < pubchemFiles.length; i++) {
			
			if(pubchemFiles[i].getName().startsWith("README"))
				continue;
			
			String[] zipRange = pubchemFiles[i].getName().split("\\.")[0].split("_");
			int start = Integer.parseInt(zipRange[1]);
			int end = Integer.parseInt(zipRange[2]);

			if(pid >= start && pid <= end)
			{
				pathToFile = pubchemFiles[i].getPath();
				break;
			}
		}
		
		File sdfFile = new File(pathToFile);
		IteratingMDLReader reader=null;
		try {
			reader = new IteratingMDLReader(new GZIPInputStream(new FileInputStream(sdfFile)), DefaultChemObjectBuilder.getInstance());
			while (reader.hasNext()) {
				IAtomContainer molecule = (IAtomContainer)reader.next();
				
				if(Integer.parseInt((String)molecule.getProperty("PUBCHEM_COMPOUND_CID")) > pid)
					return null;
				
				if(molecule.getProperty("PUBCHEM_COMPOUND_CID").equals(pubChemCID))
				{
					molToReturn = molecule;
					break;
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		reader.close();
		return molToReturn;
	}
	
	
	/**
	 * This method downloads every structure and saves it to the specified location
	 * @throws Exception 
	 *
	 */
	public static void main(String[] args) throws Exception {
		
		//parameters
		String database = "pubchem";
		double searchPPM = Double.parseDouble(args[1]);//20.0;
		boolean isOnline = true;
		
		double mass = Double.parseDouble(args[0]);
		
		String home=  args[2];//"/home/ftarutti/Desktop/";
		String out = args[3];//"out.out";
		
		StringBuffer output = new StringBuffer();
		
//		output.append("Database: "+database+"\n");
//		output.append("Exact mass:"+mass+" (search ppm: "+searchPPM+" )\n\n");
//		
		output.append("Formula\tExactMass\tSmiles\tDatabaseID\n");
		
//		File f = new File("/home/swolf/MOPAC/Hill-Riken-MM48_POSITIVE_PubChem_Formula/");
//		File f = new File("/home/swolf/MOPAC/Hill-Riken-MM48_POSITIVE_PubChem_LocalMass2009_CHONPS_NEW/spectra/");
//		File f = new File("/home/ftarutti/testspectra/Progress/Merged/");
//		File files[] = f.listFiles();
		
//		File[] files = new File[]{new File(args[0])};
		
//		Config config = null;
//		try {
//			config = new Config("outside");
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
//		File[] pubchemFiles = new File("/vol/mirrors/pubchem").listFiles();
//		Arrays.sort(pubchemFiles);
		
//		for (int i = 0; i < pubchemFiles.length; i++) {
//			System.out.println(pubchemFiles[i]);
//		}
		
		
		
//		for(int i=0;i<files.length;i++)
		{
			
			
			
//			if(files[i].isFile() && files[i].getName().split("\\.")[1].equals("txt"))
			{
				
//				WrapperSpectrum spectrum = new WrapperSpectrum(files[i].toString());
				List<String> candidates = null;
				
//				String filePath = files[i].getParent();
//				String fileName = files[i].getName().split("\\.")[0];
				
				
//				if(new File(filePath + "/" + database + "/" + fileName).isDirectory())
//					continue;
				
				candidates = new ArrayList<String>();
				
//				PubChemWebService pw = new PubChemWebService();
				PubChemWebService pubchem=null;
				if(isOnline)
				{
					 pubchem = new PubChemWebService();
					candidates = Candidates.queryOnline(database, "", "", mass, searchPPM, false, pubchem);
				}
//				else
//				{
////					System.out.println(database);
//					candidates = Candidates.queryLocally(database, mass, searchPPM, config.getJdbc(), config.getUsername(), config.getPassword());
//				}
				
				System.out.println("Prepare file...");
				for (String candString : candidates) {
//					System.out.println("PW "+config.getPassword().toString());
//					IAtomContainer mol = Candidates.getCompoundLocally(database, candString, config.getJdbc(), config.getUsername(), config.getPassword(), false, config.getChemspiderToken());
					
					IAtomContainer molecule = pubchem.getMol(candString);
					
					if(molecule == null)
						continue;
					
					//get Smiles
					
					IAtomContainer mol = AtomContainerManipulator.removeHydrogens(molecule);
					SmilesGenerator sg = new SmilesGenerator();
			    	
			    	String smiles = sg.createSMILES(mol);
					
			    	
			    	
					try
					{
						AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
				        CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(mol.getBuilder());
				        hAdder.addImplicitHydrogens(mol);
				        AtomContainerManipulator.convertImplicitToExplicitHydrogens(mol);
					}
					catch(CDKException e)
					{
						System.err.println(e.getMessage());
					}
			    	
					//get Formula
					//get Mass 
					
			    	IMolecularFormula formula =MolecularFormulaManipulator.getMolecularFormula(mol);
		    		String formulaString = MolecularFormulaManipulator.getString(formula);
		    		
			    	double emass = MolecularFormulaManipulator.getTotalExactMass(formula);
			    	
			    	


					
					
											
//					try {
//						new File(filePath + "/" + database + "/" + fileName).mkdirs();
//						
//						IAtomContainer molwith2D = getPubChemEntryFromSdfGZ(candString, pubchemFiles);			
//						if(molwith2D != null)
//						{
//							SDFWriter writer = new SDFWriter(new FileWriter(new File(filePath + "/" + database + "/" + fileName + "/" + candString + ".sdf")));
//							writer.write(molwith2D);
//							writer.close();
//						}
//						else
//						{
//							System.err.println("Mol not found! " + candString);
//
//							SDFWriter writer = new SDFWriter(new FileWriter(new File(filePath + "/" + database + "/" + fileName + "/" + candString + ".sdf")));
//							writer.write(mol);
//							writer.close();
//						}
//						
//					} catch (CDKException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//			    	output.append("Formula\tExact Mass\tSmiles\tDatabase ID\tName");
			    	output.append(formulaString+"\t"+emass+"\t"+smiles+"\t"+candString+"\n");
			    	
//			    	System.out.println(formulaString+"\t"+emass+"\t"+smiles+"\t"+candString+"\t");
			    	
			    
			    	
				}
				
				BufferedWriter bw = new BufferedWriter(new FileWriter(home+out));
				bw.write(output.toString());
				bw.flush();
				bw.close();
				
				System.out.println("Output written to "+home+out+".");
			}
		}
	}
}
