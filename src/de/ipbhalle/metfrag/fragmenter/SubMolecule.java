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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openscience.cdk.interfaces.IAtomContainer;

// TODO: Auto-generated Javadoc
/**
 * The Class SubMolecule.
 */
public class SubMolecule {
	
	/** The exact mass. */
	double eMass;
	
	/** The hydrogen difference. */
	int hydrogenDifference;
	
	/** The bonds in original molecule to break. */
	List<Integer[]> bondsInOriginalMoleculeToBreak;
	
	/**
	 * Instantiates a new sub molecule.
	 */
	public SubMolecule() {
		bondsInOriginalMoleculeToBreak = new ArrayList<Integer[]>();
	}

	/**
	 * Sets the exact mass.
	 *
	 * @param eMass the new e mass
	 */
	public void setEMass(double eMass)
	{
		this.eMass = eMass;
	}
	
	/**
	 * Gets the exact mass.
	 *
	 * @return the e mass
	 */
	public double getEMass()
	{
		return this.eMass;
	}
	
	/**
	 * Sets the hydrogen difference.
	 *
	 * @param hydrogenDifference the new hydrogen difference
	 */
	public void setHydrogenDifference(int hydrogenDifference)
	{
		this.hydrogenDifference = hydrogenDifference;
	}
	
	/**
	 * Gets the hydrogen difference.
	 *
	 * @return the hydrogen difference
	 */
	public int getHydrogenDifference()
	{
		return this.hydrogenDifference;
	}
	
	/**
	 * Adds the bond to break.
	 *
	 * @param from the from
	 * @param to the to
	 */
	public void addBondToBreak(int from, int to)
	{
		Integer[] bond = new Integer[2];
		bond[0]=from;
		bond[1]=to;
		
		Arrays.sort(bond);
		
		
		bondsInOriginalMoleculeToBreak.add(bond);
	}
	
	
	/**
	 * Gets the bond to break.
	 *
	 * @return the bond to break
	 */
	public List<Integer[]> getBondToBreak()
	{
		return this.bondsInOriginalMoleculeToBreak;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override public String toString()
	{
		StringBuffer res = new StringBuffer();
		
		res.append("exact mass: "+eMass+" # of hydrogens added: "+hydrogenDifference+"\n");
		
		for (Integer[] bonds : bondsInOriginalMoleculeToBreak) {
			
			res.append("atom1 "+bonds[0]+" atom2 "+bonds[1]+"\n");
			
		}
		
		return res.toString();
		
	}
	
	
}
