package de.ipbhalle.metfrag.addDatabasIDs;
import java.io.FileNotFoundException;
import java.util.List;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.formula.MolecularFormula;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import de.ipbhalle.metfrag.read.SDFFile;


public class checkIsotopes {

	public static void main(String[] args) throws FileNotFoundException, CDKException {
		
		List<IAtomContainer> test = SDFFile.ReadSDFFile("/home/ftarutti/Desktop/CID_10654556.sdf");
		
		for (IAtomContainer iAtomContainer : test) {
			
			IMolecularFormula iformula = MolecularFormulaManipulator.getMolecularFormula(iAtomContainer);
    		double emass = MolecularFormulaManipulator.getTotalExactMass(iformula);
			
    		System.out.println(emass);
    		
		}
		
	}
	
}
