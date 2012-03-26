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

import java.util.HashMap;
import java.util.Map;

import org.freehep.graphicsio.swf.SWFAction.ToString;

// TODO: Auto-generated Javadoc
/**
 * The Class Bond.
 */
public class Bond {

	/** The atoms. */
	Integer[] atoms;
	
	/** The bond order. */
	double bondOrder;
	
	/**
	 * Instantiates a new bond.
	 *
	 * @param atom1 the atom1
	 * @param atom2 the atom2
	 */
	public Bond(int atom1, int atom2)
	{
		this.atoms = new Integer[2];
		
		this.atoms[0]=atom1;
		this.atoms[1]=atom2;	
		
	}
	
	/**
	 * Sets the bond order.
	 *
	 * @param bo the new bond order
	 */
	public void setBondOrder(double bo)
	{
		this.bondOrder=bo;
	}

	/**
	 * Gets the bond order.
	 *
	 * @return the bond order
	 */
	public double getBondOrder()
	{
		return this.bondOrder;
	}
	
	/**
	 * Gets the bond.
	 *
	 * @return the bond
	 */
	public Integer[] getBond()
	{
		return this.atoms;
	}
	
	/**
	 * Gets the bond name.
	 *
	 * @return the bond name
	 */
	public String getBondName()
	{
		String res = this.atoms[0]+":"+this.atoms[1];
		return res;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override public String toString()
	{
		StringBuffer res = new StringBuffer();
		
		res.append(this.atoms[0]+":"+this.atoms[1]+"\t"+this.bondOrder);
		
		return res.toString();
	}
	
}
