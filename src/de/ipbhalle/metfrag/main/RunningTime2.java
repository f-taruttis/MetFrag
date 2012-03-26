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
package de.ipbhalle.metfrag.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.formula.MolecularFormula;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import de.ipbhalle.metfrag.fragmenter.Fragmenter;
import de.ipbhalle.metfrag.fragmenter.SubMolecule;
import de.ipbhalle.metfrag.fragmenter.SubstructureGenerator;
import de.ipbhalle.metfrag.massbankParser.Peak;
import de.ipbhalle.metfrag.molDatabase.PubChemLocal;
import de.ipbhalle.metfrag.tools.MolecularFormulaTools;
import de.ipbhalle.metfrag.tools.PPMTool;

public class RunningTime2
{
	
	private static int sumArray(int[] bins)
	{
		int ret = 0;
		for (int i = 0; i < bins.length; i++) {
			ret += bins[i];
		}
		return ret;
	}
	
	
	/**
	 * The method automatically retrieves 
	 * 
	 * @param args the arguments
	 */
	public static void main(String[] args) {

		double minMass = Double.parseDouble(args[0]);//100.0;
		double maxMass = Double.parseDouble(args[1]);//1000.0;
//		int maxCount = 100;//500;
//		int maxCountGlobal = 1000;//500000;
		int treeDepth = 4;
//		int treeDepth2 = 2;
		
		String file = args[2];
		
		Vector<Peak> peakList = new Vector<Peak>();
		Peak peak = new Peak(30.0, 999.0, 10);
		peakList.add(peak);
		
		
		
		//bins from 1 to 100 ... each bin contains 1000 structures
		//first bin mass range: >=100 - <=110
		//second bin: >110 - <= 120
		//max bin size = 90 because min mass = 100
		int[] bins = new int[90];
		for (int i = 0; i < bins.length; i++) {
			System.out.print(i + "\t");
		}
		System.out.println();
		Map<Integer, Boolean> alreadyDone = new HashMap<Integer, Boolean>();
				
		// Create file 
		String home = "/home/ftarutti/ChemFrag/Fragmentation/";
	    FileWriter fstream;
		try {			
			Config c = new Config();
	        String url = c.getJdbcPostgres();
	        String username = c.getUsernamePostgres();
	        String password = c.getPasswordPostgres();
	        
			String driver = "org.postgresql.Driver"; 
			Driver driverPostgres = new org.postgresql.Driver();
			Class.forName(driver);
			DriverManager.registerDriver (driverPostgres);
			Connection con = DriverManager.getConnection(url + "?protocolVersion=2", username, password);
//	        IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
			
			fstream = new FileWriter(home+"MIN"+(int)minMass+"MAX"+(int)maxMass+"performance_td" + treeDepth + "Complete.log", true);
			
			int count = 0;
			
			BufferedWriter out = new BufferedWriter(fstream);
			
			//load needed IDs from file:
			BufferedReader in = new BufferedReader(new FileReader(home+file));
			String line="";
			
			while((line = in.readLine())!=null)
	//		while(sumArray(bins) < maxCountGlobal)
			{
				//random ID
				//long randomID = Math.round(Math.random()*2889221.0);
				//int randomCandidateID = Integer.parseInt(Long.toString(randomID));
			    int randomCandidateID = Integer.parseInt(line.trim());
				
				PreparedStatement pstmt = con.prepareStatement("SELECT compound_id, exact_mass, formula, smiles from compound " +
			    		"where compound_id = ? LIMIT 1;");
			    pstmt.setInt(1, randomCandidateID);
			    ResultSet res = pstmt.executeQuery();
			    
			    if(res == null)
			    	continue;
			    			    
		        res.next();
		        double exactMass = res.getDouble(2);
		        String formula = res.getString(3);
		        int bin = (int)(Math.floor((exactMass / 10.0) - 10.0));
		        
		        if(exactMass <= minMass || exactMass >= (maxMass-0.1))
		        	continue;
		        
//		        if(bins[bin] > maxCount)
//		        	continue;
		        
		        if(alreadyDone.containsKey(randomCandidateID))
		        	continue;
		        
		        if(count == 1000)
		        {
		        	count = 0;
		        	for (int i = 0; i < bins.length; i++) {
						System.out.print(bins[i] + "\t");
					}
		        	System.out.println();
		        }
		        else
		        	count++;
		        
		        SmilesParser sp = new SmilesParser(NoNotificationChemObjectBuilder.getInstance());
				IAtomContainer mol = sp.parseSmiles(res.getString(4));
				AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
		        CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(mol.getBuilder());
		        hAdder.addImplicitHydrogens(mol);
		        
		        int ringCount = 0;
		        try
		        {
		        	AllRingsFinder rings = new AllRingsFinder();
			        rings.setTimeout(100000);
			        ringCount = rings.findAllRings(mol).getAtomContainerCount();
		        }
		        catch (Exception e) {
		        	System.err.println("All rings finder timeout!");
					continue;
				}
		       
		        long start = System.currentTimeMillis(); // start timing
		        
				Fragmenter fragmenter = new Fragmenter(peakList, 0, 10, 1, true, false, true, true);
				List<IAtomContainer> results = new ArrayList<IAtomContainer>();
				
		        try {
					results = fragmenter.generateFragmentsInMemory(mol, false, treeDepth, false);
				} catch (CDKException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				}
		        long stop = System.currentTimeMillis(); // stop timing
					
//		        
//		        long start3 = System.currentTimeMillis(); // start timing
//		        
//				Fragmenter fragmenter2 = new Fragmenter(peakList, 0, 10, 1, true, false, true, true);
//				List<IAtomContainer> results2 = new ArrayList<IAtomContainer>();
//				
//		        try {
//					results2 = fragmenter2.generateFragmentsInMemory(mol, false, treeDepth2, false);
//				} catch (CDKException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					continue;
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					continue;
//				}
//		        long stop3 = System.currentTimeMillis(); // stop timing
		        
		        
		        //Here new Fragmenter :
		        
//		        long startNew = System.currentTimeMillis();
//		        
//		        SubstructureGenerator2 subgen = new SubstructureGenerator2(mol, peakList);
//		        
//		        Map<IAtomContainer,SubMolecule> resultsN = new HashMap<IAtomContainer, SubMolecule>();
//		        
//		        try {
//					resultsN = subgen.getMoleculeSetEfficent();
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//		        long stopNew = System.currentTimeMillis();
		        
		        //End new Fragmenter
		        
				out.write(randomCandidateID + "\t");
				out.write(formula + "\t");
				out.write(exactMass + "\t");
				out.write(results.size() + "\t");
//				out.write(results2.size() +"\t");
//				out.write(resultsN.size() + "\t");
				out.write((stop - start) + "\t");
//				out.write((stop3-start3) +"\t");
//				out.write((stopNew-startNew)+"\t");
				out.write(ringCount + "\t");
				out.newLine();	
				out.flush();
				
				
				bins[bin] += 1;
				alreadyDone.put(randomCandidateID, true);
				
			}
			out.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
