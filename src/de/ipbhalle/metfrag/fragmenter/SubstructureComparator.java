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

import java.util.Comparator;

import org.openscience.cdk.interfaces.IAtomContainer;


// TODO: Auto-generated Javadoc
/**
 * The Class SubstructureComparator.
 */
public class SubstructureComparator implements Comparator<IAtomContainer>  {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(IAtomContainer arg0, IAtomContainer arg1) {
		int x = arg0.getAtomCount() - arg1.getAtomCount();
		int val =0;
		if (x<0)
			val = -1;
		if(x==0)
			val = 0;
		if(x>0)
			val = 1;
		
		return val;
	}

}
