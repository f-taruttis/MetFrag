/*
*
* Copyright (C) 2009-2010 IPB Halle, Franziska Taruttis
*
* Contact: ftarutti@ipb-halle.de
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

package de.ipbhalle.metfrag.fragmenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.openscience.cdk.Atom;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.interfaces.IBond.Order;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smsd.algorithm.rgraph.CDKMCS;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import de.ipbhalle.metfrag.massbankParser.Peak;
import de.ipbhalle.metfrag.spectrum.WrapperSpectrum;
import de.ipbhalle.metfrag.tools.Constants;
import de.ipbhalle.metfrag.tools.PPMTool;



// TODO: Auto-generated Javadoc
/**
 * The Class SubstructureGenerator.
 */
public class SubstructureGenerator {
	
	/** The peak list. */
	Vector<Peak> peakList;
	
	/** The mzabs. */
	double mzabs;
	
	/** The mzppm. */
	double mzppm;
	
	/** The min weight. */
	double minWeight= Double.MAX_VALUE;;
	
	/** The molecule. */
	private IAtomContainer molecule;
	
	/** The Constant INMOLECULE. */
	private static final String INMOLECULE="inMolecule";

	/** The result set. */
	Set<Set<String>> resultSet = new HashSet<Set<String>>();
	
	/** The result molecules. */
	//IAtomContainer[] resultMolecules;
	public Map<IAtomContainer,SubMolecule> resultMolecules;
	
	/** The mode. */
	int mode=1;

	  /**
  	 * Instantiates a new substructure generator.
  	 *
  	 * @param smiles the smiles
  	 * @param peakList the peak list
  	 */
    public SubstructureGenerator(String smiles, Vector<Peak> peakList)
    {
    	SmilesParser smi = new SmilesParser(NoNotificationChemObjectBuilder.getInstance());
    	try {
			molecule =smi.parseSmiles(smiles);
		} catch (InvalidSmilesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	this.peakList= peakList;
//		this.exactmass=exactmass;
    	
    	setMinWeight();
    }


    /**
     * Instantiates a new substructure generator.
     *
     * @param molecule the molecule
     * @param peakList the peak list
     */
    public SubstructureGenerator(IAtomContainer molecule, Vector<Peak> peakList)
    {
    	
		this.molecule = molecule;
		this.peakList=peakList;

		
		setMinWeight();
    }
	
	
    /**
     * Enumerate.
     */
    private void enumerate()
    {
    	for(int atom=this.molecule.getAtomCount()-1; atom>=0;atom--)
    	{

    		Set<String> current=new HashSet<String>(); 
    		Set <String> B = new  HashSet<String>(); 
    		
    		current.add(""+atom);
    		
    		for(int i=0;i<=atom;i++)
    		{
    			B.add(i+"");
    		}

    		resultSet.add(current);
    	
    		
    		enumeration(current,B);
    	}
    	
    	
    }
    
    /**
     * Enumeration.
     *
     * @param current the current
     * @param B the b
     */
    private void enumeration(Set<String> current,Set<String> B)
    {

    	Set<String> neighborSet =  new HashSet<String>();

    	for (String atom : current) {

    		
    		List<IAtom> neighbors = new ArrayList<IAtom>();
    	
    		
    		int atomNumber =  Integer.parseInt(atom);
    		neighbors =  molecule.getConnectedAtomsList(molecule.getAtom(atomNumber)); //all neighbor atoms
    		
    		
    		for (IAtom iAtom : neighbors) {
				neighborSet.add((iAtom.getProperty(INMOLECULE).toString())); // add to set all bfs numbers of the atoms
			}		

    	}
    	

    	
    	
    	neighborSet.removeAll(current); //atoms in current cannot be in in Neighborhood(current)
    	
		neighborSet.removeAll(B); // Calculating Neighborhood(current) \ B

		Set<Set<String>> subsets = new HashSet<Set<String>>();

		if (!neighborSet.isEmpty()) {

			subsets = powerSet(neighborSet);

			for (Set<String> set : subsets) {

				if (!set.isEmpty()) {


					Set<String> outputSet = new HashSet<String>();

					for (String string : current) {
						
						outputSet.add(string);
						
					}
					
					
					outputSet.addAll(set);

					resultSet.add(outputSet);

					Set<String> withoutSet = new HashSet<String>();
					withoutSet = B;
					withoutSet.addAll(set);
			
					
					enumeration(outputSet, withoutSet);
				}
			}
		}	
    }
    
    /**
     * Power set.
     *
     * @param originalSet the original set
     * @return the sets the power sets of the original set
     */
    public static  Set<Set<String>> powerSet(Set<String> originalSet) {
        Set<Set<String>> sets = new HashSet<Set<String>>();

		if ( originalSet.isEmpty() ) {
			sets.add(new HashSet<String>());
			return sets;
		} else {

			
			List<String> list = new ArrayList<String>(originalSet);

			String head = list.get(0);

			Set<String> rest = new HashSet<String>(list.subList(1, list.size()));

			for (Set<String> set : powerSet(rest)) {
				Set<String> newSet = new HashSet<String>();
				newSet.add(head);
				newSet.addAll(set);
				sets.add(newSet);
				sets.add(set);
			}

			return sets;
		}

	}
    

    
    
    /**
     * Gets the substructures.
     *
     * @return the substructures non unique
     * @throws CDKException the cDK exception
     * @throws Exception the exception
     */
    public void getSubstructures() throws CDKException, Exception
    {
    	
    	for(int iter=0; iter< molecule.getAtomCount(); iter++)
    	{
    		molecule.getAtom(iter).setProperty(INMOLECULE,iter);	
    	}
    	
    	
    	this.enumerate();
    	

    	
    }
    
    /**
     * Checks if the fragment is heavy enough.
     * 
     * @param mass the mass
     * 
     * @return true, if is heavy enough
     * 
     * @throws CDKException the CDK exception
     * @throws Exception the exception
     */
    private boolean isHeavyEnough(Double mass) throws CDKException, Exception
    {
    	boolean candidate = false;

    		double protonMass = Constants.PROTON_MASS * (double)mode;
    		double min = (this.minWeight - (mzabs + PPMTool.getPPMDeviation(this.minWeight, this.mzppm)));
	    	if((mass + protonMass) > min)
	    	{

	    		
	    		candidate = true;
	    		
	    	}

    
    	return candidate;
    	
    }
    
    
    /**
     * Sets the min weight.
     */
    private void setMinWeight()
    {
    	//set the minimum weight
    	for (int i = 0; i< peakList.size(); i++) {
			if(peakList.get(i).getMass() < this.minWeight)
				this.minWeight = peakList.get(i).getMass();
    	}
    	
    }

    
    
    /**
     * Prints the smiles.
     *
     * @param atom the atom
     * @return the smiles
     */
    public String getSmiles(IAtomContainer atom)
    {
    	SmilesGenerator sg = new SmilesGenerator();
    	
    	String smiles = sg.createSMILES(atom);
		
    	return smiles;
    }
    
    /**
     * Prints the smiles from set.
     *
     * @param set the set
     */
    public static void printSmilesFromSet(Set<IAtomContainer> set)
    {

		SmilesGenerator sg = new SmilesGenerator();
		
    	
    	for (IAtomContainer iAtomContainer : set) {

    		String smiles = sg.createSMILES(iAtomContainer);
    		
    		System.out.println(smiles);
    		
    		
		} 
    	
    }

    
    
    /**
     * Gets the molecule set.
     *
     * @return the molecule set
     * @throws CDKException the cDK exception
     * @throws Exception the exception
     */
    public Map<IAtomContainer, SubMolecule> getMoleculeSet() throws CDKException, Exception
    {

    
    	getSubstructures();
    	
    	resultMolecules = new HashMap<IAtomContainer, SubMolecule>();
    	
    	
    	int counter=0;
    	
    	Set<String> formulas = new HashSet<String>();
    	
    	for (Set<String> moleculeString : resultSet) {
    		
    		
    		
    		Set<String> moleculeIds = new HashSet<String>();
    		
    		
    		
    		for (String id : moleculeString) {
    			
    			moleculeIds.add(id); 			
			}
    		

    		IChemObjectBuilder builder = NoNotificationChemObjectBuilder.getInstance();
            IAtomContainer subMolecule = builder.newInstance(IAtomContainer.class);
    	
            SubMolecule sub= new SubMolecule();
    		
    		for (String molID : moleculeIds) {
				
    			IAtom atom= molecule.getAtom(Integer.parseInt(molID));
    					
    			subMolecule.addAtom(atom);
    			List<IBond> bonds = molecule.getConnectedBondsList(atom);
    			
    			for (IBond iBond : bonds) {
    		
    				IAtom connected =  iBond.getConnectedAtom(atom);
    		

    				 if(subMolecule.contains(connected))
    				 {
    					 
    					 Order ord = iBond.getOrder();

    					 
    					 subMolecule.addBond(builder.newInstance(IBond.class, atom, connected));
    					 subMolecule.getBond(subMolecule.getBondCount() - 1).setOrder(ord);
    				 }

				}
    			
    			
			}

    		
    		
    		for(IAtom atom : subMolecule.atoms())
    		{
    		
			List<IBond> bonds = molecule.getConnectedBondsList(atom);
			
			for (IBond iBond : bonds) {
		
				IAtom connected =  iBond.getConnectedAtom(atom);
		
				 if(!subMolecule.contains(connected))
				 {
					 sub.addBondToBreak(Integer.parseInt(atom.getProperty(INMOLECULE).toString()), molecule.getAtomNumber(connected));
				 }

			}
    		
    		}
    		

    		//add all implicit hydrogens
    		
    		int atomsWithoutHydrogens = subMolecule.getAtomCount();
    		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(subMolecule);

    		CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(subMolecule.getBuilder());
    		try {
    			hAdder.addImplicitHydrogens(subMolecule);
    		} catch (CDKException e) {

    			
    		}

    		
    		
    		
    		AtomContainerManipulator.convertImplicitToExplicitHydrogens(subMolecule );

    		IMolecularFormula iformula = MolecularFormulaManipulator.getMolecularFormula(subMolecule );
    		double emass = MolecularFormulaManipulator.getTotalExactMass(iformula);

    		if(isHeavyEnough(emass))
    		{

    			int hydrogenDifference = subMolecule .getAtomCount() - atomsWithoutHydrogens;
    			
    			sub.setEMass(emass);
    			sub.setHydrogenDifference(hydrogenDifference);
  	   		
    			
    			IMolecularFormula fragmentFormula = MolecularFormulaManipulator.getMolecularFormula(subMolecule);
            	String currentSumFormula = MolecularFormulaManipulator.getString(fragmentFormula);
    			
            	if(!formulas.contains(currentSumFormula))
            	{
            		resultMolecules.put(subMolecule , sub);
            		formulas.add(currentSumFormula);
            	}
    		}

    	}

    	return this.resultMolecules;
    }
    
    
    /**
     * The main method.
     *
     * @param args the arguments
     * @throws CDKException the cDK exception
     * @throws Exception the exception
     */
    public static void main(String[] args) throws CDKException, Exception {
		
    	String home = "/home/ftarutti/ChemFrag/Scoring/";
		WrapperSpectrum spectrum = new WrapperSpectrum(home+"AlaninTest.txt");
		
		
		
    	String smiles = "CC(N)C(=O)O";

    	IAtomContainer molecule = null;
    	SmilesParser smi = new SmilesParser(NoNotificationChemObjectBuilder.getInstance());
    	try {
			molecule =smi.parseSmiles(smiles);
		} catch (InvalidSmilesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   	

    	SubstructureGenerator sg2 = new SubstructureGenerator(molecule, spectrum.getPeakList());
    	Map<IAtomContainer,SubMolecule> resultsN = new HashMap<IAtomContainer, SubMolecule>();
    	resultsN = sg2.getMoleculeSet();//.keySet();

	}
    
}
