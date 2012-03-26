package de.ipbhalle.metfrag.newScoring;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;

import de.ipbhalle.metfrag.fragmenter.Bond;
import de.ipbhalle.metfrag.fragmenter.SubMolecule;
import de.ipbhalle.metfrag.massbankParser.Peak;
import de.ipbhalle.metfrag.spectrum.WrapperSpectrum;
import de.ipbhalle.metfrag.tools.Constants;
import de.ipbhalle.metfrag.tools.PPMTool;

public class NewScoring2 {
	
	String candidateID;
	WrapperSpectrum spectrum;
	
	IAtomContainer molecule;
	
//	String smiles;
	double score;
	
	Map<IAtomContainer,SubMolecule> fragments;
	//Map<IAtomContainer,Double[]> fragmentsToMass;
	
	double mzabs;
	double mzppm;
	
	private static final String BONDORDER ="bondOrder";
	//For precalculated Molecules:
	//First: load all precalculated Molecule (or precalculate all molecule)

	boolean useBondOrders;
	
	public NewScoring2(String candidateID, Map<IAtomContainer, SubMolecule> fragments, WrapperSpectrum spectrum, double mzabs, double mzppm , IAtomContainer molecule, boolean useBondOrder )
	{
		this.candidateID=candidateID;
		this.spectrum=spectrum;
		
		this.fragments=fragments;
		
		this.mzabs=mzabs;
		this.mzppm=mzppm;
		
		this.molecule=molecule;
		
		this.useBondOrders=useBondOrder;
		
	}
	
	
	public void getOptimalAssignment()
	{
		Vector<Peak>  peakList = this.spectrum.getPeakList();
		
		Set<IAtomContainer> fragments = this.fragments.keySet();
		
		int[][] hydrogenMode = new int[peakList.size()][fragments.size()];
		
		IAtomContainer[] frags = new IAtomContainer[fragments.size()];
		frags = fragments.toArray(frags);

		
		double [][] pFragmentPeak = new double [peakList.size()][fragments.size()];
		
		String [][] bondOrders = new String[peakList.size()][fragments.size()];
		
		for (int i = 0; i < bondOrders.length; i++) {
			
			for (int j = 0; j < bondOrders[i].length; j++) {
				
				bondOrders[i][j]="";
				
			}
			
		}
		
		double [][] fragmentToPeak =new double [peakList.size()][fragments.size()];
		
		int i=0; // control variable for peakList 
		
		Sigmoid sigmoidal = new Sigmoid(1.25, 6);
		
		for (Peak peak : peakList) {
			
			double pMass = peak.getMass();
			double intenstity = peak.getRelIntensity(); 
			
			int j=0; // control variable for fragments

			for (IAtomContainer iAtomContainer : fragments) {

				int numberOfHydrogens = this.fragments.get(iAtomContainer).getHydrogenDifference();////(int) fragmentsToMass.get(iAtomContainer)[1].doubleValue();//
				
				double scores[] = new double[numberOfHydrogens+2];
				
				double deviation=this.mzabs + PPMTool.getPPMDeviation(this.fragments.get(iAtomContainer).getEMass(), this.mzppm);
				double variance = Math.pow(deviation, 2);
								
				Gaussian gauss = new Gaussian(0, variance);
				
				
				SubMolecule mol = this.fragments.get(iAtomContainer);
				
				List<Integer[]> bondsToBreak = mol.getBondToBreak();
				
//				System.out.println("BondsToBreak: ");
//				for (Integer[] integers : bondsToBreak) {
//					
//					System.out.println(integers[0]+"\t"+integers[1]);
//					
//				}
//				
//				System.out.println("all Bonds: ");
//				
//				for (IBond bond : this.molecule.bonds()) {
//					
//					System.out.println(bond.getProperty(this.BONDORDER));
//				}
//				
				double bondOrderTerm = 1.0;
//				
				if (useBondOrders) {
					for (Integer[] integers : bondsToBreak) {

						IAtom atom1 = this.molecule.getAtom(integers[0]);
						IAtom atom2 = this.molecule.getAtom(integers[1]);

						IBond bond = this.molecule.getBond(atom1, atom2);

						// System.out.println("Bond order for: "+integers[0]+":"+integers[1]+"  is "+bond.getProperty(Constants.BONDORDER));

						double bo = Double.parseDouble(bond.getProperty(
								Constants.BONDORDER).toString());
			

						bondOrderTerm *= sigmoidal.getVal(bo);

						bondOrders[i][j] += bo+";";
						
						// for (Bond bond : origBondsInMolecule) {
						//
						// if
						// (de.ipbhalle.metfrag.util.Maths.isEqual(integers,bond.getBond()))
						// {
						// double bo = bond.getBondOrder();
						//
						// System.out.println("bond Order for Bond "+bond.toString()+"\t"+bo);
						//
						// bondOrders[i][j] += bo+";";
						//
						// bondOrderTerm *= sigmoidal.getVal(bo);
						// }
						//
						// }
						//
						//
						//
					}
				}
				
				
				//Calculates the score for each possible number of H atoms
				scores[0]= gauss.getLogVal(pMass-this.fragments.get(iAtomContainer).getEMass()) * bondOrderTerm;
				scores[1]= gauss.getLogVal(pMass-(this.fragments.get(iAtomContainer).getEMass()+Constants.PROTON_MASS)) * bondOrderTerm;

				for (int k = 0; k < numberOfHydrogens; k++) {
					
					scores[k+2]= gauss.getLogVal(pMass-(this.fragments.get(iAtomContainer).getEMass()-(k+1)*Constants.PROTON_MASS))*bondOrderTerm;
	
				}
////				System.out.println("var " + deviation);
////				
//				System.out.println("peak "+pMass+"\t fragment "+fragmentsToMass.get(iAtomContainer));
////				System.out.println((pMass-(this.fragmentsToMass.get(iAtomContainer))));
//				probabilities[0]= gauss.getLogProb(pMass-(this.fragmentsToMass.get(iAtomContainer)[0]-Constants.PROTON_MASS));
//				probabilities[1]= gauss.getLogProb(pMass-this.fragmentsToMass.get(iAtomContainer)[0]);
//				probabilities[2]= gauss.getLogProb(pMass-(this.fragmentsToMass.get(iAtomContainer)[0]+Constants.PROTON_MASS));
////
				
//				System.out.println((pMass-(this.fragmentsToMass.get(iAtomContainer)[0]-Constants.PROTON_MASS))+"\t"+(pMass-(this.fragmentsToMass.get(iAtomContainer)[0]))+"\t"+(pMass-(this.fragmentsToMass.get(iAtomContainer)[0]+Constants.PROTON_MASS)));
//				System.out.println(probabilities[0]+"\t"+probabilities[1]+"\t"+probabilities[2]);
//				
//				for (int k = 0; k < probabilities.length; k++) {
//					//System.out.println(probabilities[k]);
//				}
				
				//Gets the assignment with the best score
				double bestVal= this.getMax(scores);
				
				//Gets the number of hydrogens 
				hydrogenMode[i][j] = this.argmax(scores);
				
				
				

				
				pFragmentPeak[i][j]=(bestVal);//Math.exp(bestVal-norm);
				fragmentToPeak[i][j]=bestVal;
				
				
				j++;
			}
			
			i++;
			
			
		}
		
		double sumOfRelativeIntensities =0.0;
		
		int  l=0;
		
		double assignedPeakScores[] = new double[peakList.size()];
		
		for (Peak peak : peakList) {
			
			sumOfRelativeIntensities+=peak.getRelIntensity();
			
			int argmax = argmax(pFragmentPeak[l]);
			double max = getMax(pFragmentPeak[l]); 
			
//			int HydrogenMode = hydrogenMode[l][argmax]; 
//			
//			double prob =0.0;
//			switch (HydrogenMode) {
//			case 0:
//				
//				break;
//
//			default:
//				break;
//			}
			
			
			IAtomContainer assignedFragment = frags[argmax];
			double assignedMass = this.fragments.get(assignedFragment).getEMass(); 
			
//			Assignment 2
			
			double peakMass = /*Math.round*/(peak.getMass()*100000.0)/100000.0;
			double assigned = /*Math.round*/(assignedMass*100000.0)/100000.0;
			double probab = /*Math.round*/(Math.exp(max)*100000.0)/100000.0;
			
//			System.out.println(peakMass+"\t assigned to:\t  "+assigned+"\twith probability "+probab+" \t int: "+peak.getRelIntensity());
//			System.out.println("matching score: "+Math.exp(fragmentToPeak[l][argmax])+" bond Orders : "+bondOrders[l][argmax] + "  H-Mode "+hydrogenMode[l][argmax]);
//			
			assignedPeakScores[l] = probab;
			
			l++;
			
			
			
		}
		
		
		
		//preliminary scoring: calculate final score as weighted mass over intensities:
		
		double score = 0.0;
		int iter=0;
		
		int peaksExplained=0;
		
		for (Peak p : peakList) {
			
			double weight = p.getRelIntensity()/sumOfRelativeIntensities;
			
			score += weight * assignedPeakScores[iter];
			
			if (assignedPeakScores[iter]>0.5)
			{
				peaksExplained++;
			}	
			
			iter++;
		}
		
//		System.out.println(score+"\t"+peaksExplained);
//		
		this.score = score;
	}
	
	public double getScore ()
	{
		return this.score;
	}
	
	public static int argmax(final double vec[])
	{
		double max=vec[0];
		int argmax=0;
		for(int i=1;i<vec.length;i++)
		{
			if(vec[i]>max)
			{
				max=vec[i];
				argmax=i;
			}
		}
		
		return argmax;
		
	}
	public double getMax(double[] vec)
	{
		double max=vec[0];
		for(int i=1;i<vec.length;i++)
		{
			if(vec[i]>max)
			{
				max=vec[i];
			}
		}
		
		return max;
	}
	

//	public static void main(String[] args) {
//		System.out.println(Constants.HYDROGEN_MASS);
//		System.out.println(Constants.PROTON_MASS);
//	}
}
