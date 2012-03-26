/*
*
* Copyright (C) 2009-2010 IPB Halle, Sebastian Wolf
*
* Contact: swolf@ipb-halle.de
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package de.ipbhalle.metfrag.read;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

public class CMLTools {
	
	/**
	 * Read all cml files in given folder and return a List.
	 * file extension has to be .cml!
	 *
	 * @param folder the folder
	 * @return the list
	 * @throws FileNotFoundException 
	 * @throws CDKException 
	 */
	public static List<CMLMolecule> readFolder(File folder) throws FileNotFoundException, CDKException
	{
		CMLReader reader;
		List<IAtomContainer> containersList;
		List<CMLMolecule> ret = new ArrayList<CMLMolecule>();
		
		File files[] = folder.listFiles();

		for(int i=0;i<files.length;i++)
		{
			int dotPos = files[i].getName().lastIndexOf(".");
		    String extension = files[i].getName().substring(dotPos);
			if(files[i].isFile() && extension.equals(".cml") && files[i].getName().contains("Combined"))
			{
		        ret.add(new CMLMolecule(files[i], files[i].getName())); //one container per file
			}
		}
        return ret;  
	}
	
	
	/**
	 * Read all cml files in given folder and return a List.
	 * file extension has to be .cml! Assuming the folder containes
	 * only protonated structures from 1 molecule. It returns the
	 * mol with the lowest heat of formation!
	 *
	 * @param folder the folder
	 * @param correctCandidateString the correct candidate string
	 * @return the list
	 * @throws FileNotFoundException the file not found exception
	 * @throws CDKException the cDK exception
	 */
	public static CMLMolecule readFolderReturnLowestHoFOnlyCorrect(File folder, String correctCandidateString) throws FileNotFoundException, CDKException
	{
		CMLReader reader;
		List<IAtomContainer> containersList;
		List<CMLMolecule> ret = new ArrayList<CMLMolecule>();
		
		File files[] = folder.listFiles();

		for(int i=0;i<files.length;i++)
		{
			if(!files[i].isFile())
				continue;
			
			if(!files[i].getName().startsWith(correctCandidateString))
				continue;
			
			int dotPos = files[i].getName().lastIndexOf(".");
		    String extension = files[i].getName().substring(dotPos);
			if(extension.equals(".cml") && !files[i].getName().contains("Combined"))
			{
		        ret.add(new CMLMolecule(files[i], files[i].getName())); //one container per file
			}
		}
		
		double minHof = Double.MAX_VALUE;
		CMLMolecule currentBestSolution = null;
		for (CMLMolecule mol : ret) {
			IAtomContainer temp = mol.getMolStructure();
			double currentHof = Double.parseDouble(temp.getID());
			if(currentHof < minHof)
			{
				minHof = currentHof;
				currentBestSolution = mol;
			}
		}
		
        return currentBestSolution;  
	}
	
	
	
	/**
	 * Read all cml files in given folder and its mol. formula subfolders given as array and return a List.
	 * File extension has to be .cml! It returns the mols with the lowest heat of formation!
	 * Hill Data!
	 *
	 * @param folder the folder
	 * @param correctCandidateString the correct candidate string
	 * @return the list
	 * @throws FileNotFoundException the file not found exception
	 * @throws CDKException the cDK exception
	 */
	public static List<CMLMolecule> readFoldersLowestHoF(File folder, String[] sumFormula) throws FileNotFoundException, CDKException
	{
		
		//test: /home/swolf/MOPAC/Hill-Riken-MM48_POSITIVE_PubChem_LocalMass2009_CHONPS_NEW/mopac_4800/C40H39N3O3
		
		CMLReader reader;
		List<CMLMolecule> containersList = null;
		List<CMLMolecule> ret = new ArrayList<CMLMolecule>();
		
		
		for (int j = 0; j < sumFormula.length; j++) {
			File folderTemp = new File(folder.getAbsolutePath() + "/" + sumFormula[j]);
			File files[] = folderTemp.listFiles();
			if(files == null)
			{
				System.err.println("Error: " + folder.getAbsolutePath() + "/" + sumFormula[j] + " does not exist...missing candidate(s)!");
				continue;
			}
			
			Arrays.sort(files);
			String currentMoleculeID = "";
			for(int i=0;i<files.length;i++)
			{
				if(!files[i].isFile())
					continue;
				
				if(files[i].getName().contains("Combined"))
					continue;
				
				int dotPos = files[i].getName().lastIndexOf(".");
			    String extension = files[i].getName().substring(dotPos);
			    
			    if(currentMoleculeID.equals(""))
			    {
			    	containersList = new ArrayList<CMLMolecule>();
			    	containersList.add(new CMLMolecule(files[i], files[i].getName()));
			    	currentMoleculeID = files[i].getName().split("_")[0];
			    }
			    //single last molecule differs from previous
			    else if(!currentMoleculeID.equals(files[i].getName().split("_")[0]) && i == (files.length - 1))
				{
			    	if(i == (files.length - 1))
			    		ret.add(new CMLMolecule(files[i], files[i].getName()));
			    	
					double minHof = Double.MAX_VALUE;
					CMLMolecule currentBestSolution = null;
					for (CMLMolecule mol : containersList) {
//						System.out.println(mol.getMolFile().toString());
						IAtomContainer temp = mol.getMolStructure();
						double currentHof = Double.parseDouble(temp.getID());
						if(currentHof < minHof)
						{
							minHof = currentHof;
							currentBestSolution = mol;
						}
					}
					ret.add(currentBestSolution); //one container per file
					containersList = new ArrayList<CMLMolecule>();
			    	containersList.add(new CMLMolecule(files[i], files[i].getName()));
					currentMoleculeID = files[i].getName().split("_")[0];
				}
			    //read also last molecule
			    else if(currentMoleculeID.equals(files[i].getName().split("_")[0]) && i == (files.length - 1))
				{
			    	if(i == (files.length - 1))
			    		containersList.add(new CMLMolecule(files[i], files[i].getName()));
			    	
					double minHof = Double.MAX_VALUE;
					CMLMolecule currentBestSolution = null;
					for (CMLMolecule mol : containersList) {
//						System.out.println(mol.getMolFile().toString());
						IAtomContainer temp = mol.getMolStructure();
						double currentHof = Double.parseDouble(temp.getID());
						if(currentHof < minHof)
						{
							minHof = currentHof;
							currentBestSolution = mol;
						}
					}
					ret.add(currentBestSolution); //one container per file
					containersList = new ArrayList<CMLMolecule>();
			    	containersList.add(new CMLMolecule(files[i], files[i].getName()));
					currentMoleculeID = files[i].getName().split("_")[0];
				}
			    else if(!currentMoleculeID.equals(files[i].getName().split("_")[0]))
				{
			    	if(i == (files.length - 1))
			    		containersList.add(new CMLMolecule(files[i], files[i].getName()));
			    	
					double minHof = Double.MAX_VALUE;
					CMLMolecule currentBestSolution = null;
					for (CMLMolecule mol : containersList) {
//						System.out.println(mol.getMolFile().toString());
						IAtomContainer temp = mol.getMolStructure();
						double currentHof = Double.parseDouble(temp.getID());
						if(currentHof < minHof)
						{
							minHof = currentHof;
							currentBestSolution = mol;
						}
					}
					ret.add(currentBestSolution); //one container per file
					containersList = new ArrayList<CMLMolecule>();
			    	containersList.add(new CMLMolecule(files[i], files[i].getName()));
					currentMoleculeID = files[i].getName().split("_")[0];
				}
				else if(extension.equals(".cml"))
				{
					containersList.add(new CMLMolecule(files[i], files[i].getName()));
				}				
			}
		}
		
        return ret;  
	}
	
	
	/**
	 * Read all cml files in given folder and its mol. formula subfolders given as array and return a List.
	 * File extension has to be .cml! It returns the mols with the lowest heat of formation!
	 * Hill Data!
	 *
	 * @param folder the folder
	 * @param correctCandidateString the correct candidate string
	 * @return the list
	 * @throws FileNotFoundException the file not found exception
	 * @throws CDKException the cDK exception
	 */
	public static List<CMLMolecule> readIDLowestHoF(File folder, String sumFormula, String ID) throws FileNotFoundException, CDKException
	{
		
		//test: /home/swolf/MOPAC/Hill-Riken-MM48_POSITIVE_PubChem_LocalMass2009_CHONPS_NEW/mopac_4800/C40H39N3O3
		
		System.out.println("CMLTools: "+folder.toString()+"\t"+sumFormula+"\t"+ID);
		
		CMLReader reader;
		List<CMLMolecule> containersList = null;
		List<CMLMolecule> ret = new ArrayList<CMLMolecule>();

		File folderTemp = new File(folder.getAbsolutePath() + "/" + sumFormula);
		
	
		
		File files[] = folderTemp.listFiles();
		if(files == null)
		{
			System.err.println("Error: " + folder.getAbsolutePath() + "/" + sumFormula + " does not exist...missing candidate(s)!");
			return null;
		}
		
//		System.out.println("CMLTools "+files.length);
		
		Arrays.sort(files);
		String currentMoleculeID = "";
		for(int i=0;i<files.length;i++)
		{
			if(!files[i].isFile())
				continue;
			
			if(files[i].getName().contains("Combined"))
				continue;
			
			
			
			if(!currentMoleculeID.equals("") && !currentMoleculeID.equals(files[i].getName().split("_")[0]))
			{
				
				
				
		    	if(i == (files.length - 1))
		    		containersList.add(new CMLMolecule(files[i], files[i].getName()));
		    	
				double minHof = Double.MAX_VALUE;
				CMLMolecule currentBestSolution = null;
				for (CMLMolecule mol : containersList) {
//						System.out.println(mol.getMolFile().toString());
					IAtomContainer temp = mol.getMolStructure();
					double currentHof = Double.parseDouble(temp.getID());
					if(currentHof < minHof)
					{
						minHof = currentHof;
						currentBestSolution = mol;
					}
				}
				ret.add(currentBestSolution); //one container per file
				containersList = new ArrayList<CMLMolecule>();
		    	containersList.add(new CMLMolecule(files[i], files[i].getName()));
				currentMoleculeID = files[i].getName().split("_")[0];
				
				
				return ret;
			}
			
			
			if(!files[i].getName().split("_")[0].equals(ID))
				continue;
			
			
			int dotPos = files[i].getName().lastIndexOf(".");
		    String extension = files[i].getName().substring(dotPos);
		    
		    if(currentMoleculeID.equals(""))
		    {
		    	System.out.println("ID "+currentMoleculeID);
		    	containersList = new ArrayList<CMLMolecule>();
		    	containersList.add(new CMLMolecule(files[i], files[i].getName()));
		    	currentMoleculeID = files[i].getName().split("_")[0];
		    }
		    //single last molecule differs from previous
		    else if(!currentMoleculeID.equals(files[i].getName().split("_")[0]) && i == (files.length - 1))
			{
		    	if(i == (files.length - 1))
		    		ret.add(new CMLMolecule(files[i], files[i].getName()));
		    	
				double minHof = Double.MAX_VALUE;
				CMLMolecule currentBestSolution = null;
				for (CMLMolecule mol : containersList) {
//						System.out.println(mol.getMolFile().toString());
					IAtomContainer temp = mol.getMolStructure();
					double currentHof = Double.parseDouble(temp.getID());
					if(currentHof < minHof)
					{
						minHof = currentHof;
						currentBestSolution = mol;
					}
				}
				ret.add(currentBestSolution); //one container per file
				containersList = new ArrayList<CMLMolecule>();
		    	containersList.add(new CMLMolecule(files[i], files[i].getName()));
				currentMoleculeID = files[i].getName().split("_")[0];
			}
		    //read also last molecule
		    else if(currentMoleculeID.equals(files[i].getName().split("_")[0]) && i == (files.length - 1))
			{
		    	if(i == (files.length - 1))
		    		containersList.add(new CMLMolecule(files[i], files[i].getName()));
		    	
				double minHof = Double.MAX_VALUE;
				CMLMolecule currentBestSolution = null;
				for (CMLMolecule mol : containersList) {
//						System.out.println(mol.getMolFile().toString());
					IAtomContainer temp = mol.getMolStructure();
					double currentHof = Double.parseDouble(temp.getID());
					if(currentHof < minHof)
					{
						minHof = currentHof;
						currentBestSolution = mol;
					}
				}
				ret.add(currentBestSolution); //one container per file
				containersList = new ArrayList<CMLMolecule>();
		    	containersList.add(new CMLMolecule(files[i], files[i].getName()));
				currentMoleculeID = files[i].getName().split("_")[0];
			}
		    else if(!currentMoleculeID.equals(files[i].getName().split("_")[0]))
			{
		    	if(i == (files.length - 1))
		    		containersList.add(new CMLMolecule(files[i], files[i].getName()));
		    	
				double minHof = Double.MAX_VALUE;
				CMLMolecule currentBestSolution = null;
				for (CMLMolecule mol : containersList) {
//						System.out.println(mol.getMolFile().toString());
					IAtomContainer temp = mol.getMolStructure();
					double currentHof = Double.parseDouble(temp.getID());
					if(currentHof < minHof)
					{
						minHof = currentHof;
						currentBestSolution = mol;
					}
				}
				ret.add(currentBestSolution); //one container per file
				containersList = new ArrayList<CMLMolecule>();
		    	containersList.add(new CMLMolecule(files[i], files[i].getName()));
				currentMoleculeID = files[i].getName().split("_")[0];
			}
			else if(extension.equals(".cml"))
			{
				containersList.add(new CMLMolecule(files[i], files[i].getName()));
			}				
		}
	
        return ret;  
	}
	
	
	/**
	 * Read all cml files in given folder and its mol. formula subfolders given as array and return a List.
	 * File extension has to be .cml! It returns the mols with the lowest heat of formation!
	 * Emma GC Data!
	 *
	 * @param folder the folder
	 * @param correctCandidateString the correct candidate string
	 * @return the list
	 * @throws FileNotFoundException the file not found exception
	 * @throws CDKException the cDK exception
	 */
	public static List<CMLMolecule> readFoldersLowestHoF(File folder) throws FileNotFoundException, CDKException
	{
		
		//test: /home/swolf/MOPAC/Hill-Riken-MM48_POSITIVE_PubChem_LocalMass2009_CHONPS_NEW/mopac_4800/C40H39N3O3
		
		CMLReader reader;
		List<CMLMolecule> containersList = null;
		List<CMLMolecule> ret = new ArrayList<CMLMolecule>();
		
		
		File folderTemp = new File(folder.getAbsolutePath() + "/");
		Collection<File> filesRecursively = (Collection<File>)FileUtils.listFiles(folderTemp, new String[]{"cml"}, true);
		File files[] = new File[filesRecursively.size()];
		files = filesRecursively.toArray(files);
		if(files == null)
		{
			System.err.println("Error: " + folder.getAbsolutePath() + "/" + " does not exist...missing candidate(s)!");
			return null;
		}
		
		Arrays.sort(files);
		String currentMoleculeID = "";
		for(int i=0;i<files.length;i++)
		{
			if(!files[i].isFile())
				continue;
			
//			if(files[i].getName().contains("Combined"))
//				continue;
			
			int dotPos = files[i].getName().lastIndexOf(".");
		    String extension = files[i].getName().substring(dotPos);
		    int dashPos = files[i].getName().lastIndexOf("_");
		    
		    if(currentMoleculeID.equals(""))
		    {
		    	containersList = new ArrayList<CMLMolecule>();
		    	containersList.add(new CMLMolecule(files[i], files[i].getAbsolutePath()));
		    	
		    	currentMoleculeID = files[i].getName().substring(0,dashPos);
		    }
		    else if(!currentMoleculeID.equals(files[i].getName().substring(0,dashPos)) && i == (files.length - 1))
			{
		    	//last file
		    	if(i == (files.length - 1))
		    	{
		    		ret.add(new CMLMolecule(files[i], files[i].getAbsolutePath()));
		    		continue;
		    	}
		    	
				double minHof = Double.MAX_VALUE;
				CMLMolecule currentBestSolution = null;
				if(containersList.size() == 1)
					ret.add(containersList.get(0));
				
				for (CMLMolecule mol : containersList) {
//						System.out.println(mol.getMolFile().toString());
					IAtomContainer temp = mol.getMolStructure();
					
					if(mol.getFileName().contains("Combined"))
						continue;
					
					double currentHof = Double.parseDouble(temp.getID());
					
					if(currentHof < minHof)
					{
						minHof = currentHof;
						currentBestSolution = mol;
					}
				}
				ret.add(currentBestSolution); //one container per file
				containersList = new ArrayList<CMLMolecule>();
		    	containersList.add(new CMLMolecule(files[i], files[i].getAbsolutePath()));
				currentMoleculeID = files[i].getName().substring(0,dashPos);
			}
		    else if(currentMoleculeID.equals(files[i].getName().substring(0,dashPos)) && i == (files.length - 1))
			{
		    	containersList.add(new CMLMolecule(files[i], files[i].getAbsolutePath()));
		    	
				double minHof = Double.MAX_VALUE;
				CMLMolecule currentBestSolution = null;
				if(containersList.size() == 1)
				{
					ret.add(containersList.get(0));
					containersList = new ArrayList<CMLMolecule>();
					currentMoleculeID = files[i].getName().substring(0,dashPos);
					continue;
				}
				
				for (CMLMolecule mol : containersList) {
//						System.out.println(mol.getMolFile().toString());
					IAtomContainer temp = mol.getMolStructure();
					
					if(mol.getFileName().contains("Combined"))
						continue;
					
					double currentHof = Double.parseDouble(temp.getID());
					
					if(currentHof < minHof)
					{
						minHof = currentHof;
						currentBestSolution = mol;
					}
				}
				ret.add(currentBestSolution); //one container per file
				containersList = new ArrayList<CMLMolecule>();
		    	containersList.add(new CMLMolecule(files[i], files[i].getAbsolutePath()));
				currentMoleculeID = files[i].getName().substring(0,dashPos);
			}
		    else if(!currentMoleculeID.equals(files[i].getName().substring(0,dashPos)))
		    {
		    	double minHof = Double.MAX_VALUE;
				CMLMolecule currentBestSolution = null;
				if(containersList.size() == 1)
				{
					ret.add(containersList.get(0));
					containersList = new ArrayList<CMLMolecule>();
					containersList.add(new CMLMolecule(files[i], files[i].getAbsolutePath()));
					currentMoleculeID = files[i].getName().substring(0,dashPos);
					continue;
				}
				
				for (CMLMolecule mol : containersList) {
//						System.out.println(mol.getMolFile().toString());
					IAtomContainer temp = mol.getMolStructure();
					
					if(mol.getFileName().contains("Combined"))
						continue;
					
					double currentHof = Double.parseDouble(temp.getID());
					
					if(currentHof < minHof)
					{
						minHof = currentHof;
						currentBestSolution = mol;
					}
				}
				ret.add(currentBestSolution); //one container per file
				containersList = new ArrayList<CMLMolecule>();
		    	containersList.add(new CMLMolecule(files[i], files[i].getAbsolutePath()));
				currentMoleculeID = files[i].getName().substring(0,dashPos);
		    }
			else if(extension.equals(".cml"))
			{
				containersList.add(new CMLMolecule(files[i], files[i].getAbsolutePath()));
			}				
		}
		
		
        return ret;  
	}
	
	
	
	/**
	 * Read single molecule from cml file.
	 *
	 * @param file the file
	 * @return the list
	 * @throws FileNotFoundException the file not found exception
	 * @throws CDKException the cDK exception
	 */
	public static IAtomContainer read(File file) throws FileNotFoundException, CDKException
	{
		CMLReader reader;	
		reader = new CMLReader(new FileInputStream(file));
		ChemFile chemFile = (ChemFile)reader.read((ChemObject)new ChemFile());
		List<IAtomContainer> containersList = ChemFileManipulator.getAllAtomContainers(chemFile);
		return containersList.get(0);
	}
	
	
	/**
	 * Read files from temp folder.
	 * 
	 * @param fragments the fragments
	 * 
	 * @return the i atom container set
	 * 
	 * @throws CDKException the CDK exception
	 * @throws FileNotFoundException the file not found exception
	 */
	public static List<IAtomContainer> ReadfolderTemp (List<File> fragments) throws CDKException, FileNotFoundException
	{
		CMLReader reader;
		List<IAtomContainer> containersList;
		List<IAtomContainer> ret = new ArrayList<IAtomContainer>();
		

		for(int i = 0; i < fragments.size(); i++)
		{
			reader = new CMLReader(new FileInputStream(fragments.get(i)));
	        ChemFile chemFile = (ChemFile)reader.read((ChemObject)new ChemFile());
	        containersList = ChemFileManipulator.getAllAtomContainers(chemFile);
	        ret.add(containersList.get(0)); //one container per file
		}
        return ret;    
	}
	
	
	public static void main(String[] args) {
		try {
			String[] molFormulas = new String[] {"C10H14N10", "C40H39N3O3"};
//			CMLMolecule test = CMLTools.readFolderReturnLowestHoFOnlyCorrect(new File("/home/swolf/MOPAC/BondOrderTests/"), "CID_932.sdf_NEW_AM1_withoutSCFRT_withoutGNORM_aromatic_LONG_FIXED_FINAL");
//			List<CMLMolecule> list = CMLTools.readFoldersLowestHoF(new File("/home/swolf/MOPAC/Hill-Riken-MM48_POSITIVE_PubChem_LocalMass2009_CHONPS_NEW/mopac_4800/"), new String[]{"C26H25NP2","C23H27NO2S2","C18H21N8O2S","C23H19N5O3","C18H19N7O5","C16H24N5O6P","C23H27O3P2","C21H25N4OS2","C17H25N4O6S","C16H27N7S3","C26H23NO2S","C21H24N3O4P","C15H28NO10P","C18H27N3O4S2","C19H27NO7S","C20H21N4O6","C20H24N5OPS","C17H23N3O9","C19H25O10","C19H23N7S2","C21H17N8O2","C22H20N2O4S","C25H21N2O4","C15H23N7O5S","C14H27N3O9S","C22H23NO7","C24H21N4OS"});
//			List<CMLMolecule> list = CMLTools.readFoldersLowestHoF(new File("/home/swolf/MOPAC/100spec_SDF_GC-EIMS/sdf_calculated/013_38120_C1H5Si1Br1/"));
			List<CMLMolecule> list = CMLTools.readFoldersLowestHoF(new File("/home/swolf/MOPAC/Hill-Riken-MM48_POSITIVE_PubChem_LocalMass2009_CHONPS_NEW/mopac_4800/"), new String[]{"C40H39N3O3"});
			for (CMLMolecule cmlMolecule : list) {
				System.out.println(cmlMolecule.getFileName());
				cmlMolecule.getMolStructure();
			}
			
//			CMLMolecule test = CMLTools.readFolderReturnLowestHoFOnlyCorrect(new File("/home/swolf/MOPAC/BondOrderTests/"), "CID_932.sdf_NEW_AM1_withoutSCFRT_withoutGNORM_aromatic_LONG_FIXED_FINAL");
//			IAtomContainer tmp = test.getMolStructure();
//			System.out.println(tmp.getID());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
