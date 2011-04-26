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
package de.ipbhalle.metfrag.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Vector;


public class GenerateShScripts {
	
	public GenerateShScripts(String folder, String outputFolder, String writePath, boolean writeSDF) throws IOException
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	    java.util.Date date = new java.util.Date();
		String dateString = dateFormat.format(date);
		
		
		File f = new File(folder);
		File files[] = f.listFiles();
		Arrays.sort(files);
		//remove the folders from the array
		Vector<File> onlyFiles = new Vector<File>();
		
		for (File file : files) {
			if(file.isFile())
				onlyFiles.add(file);
		}
		
		for (int i = 0; i < onlyFiles.size(); i++) {
			File f2 = new File(writePath + "/sunGrid_" + onlyFiles.get(i).getName() + ".sh"); 
			
			BufferedWriter out = new BufferedWriter(new FileWriter(f2));
			out.write("#!/bin/bash");
			out.newLine();
			//get available ram
//			out.write("RamTest=`free -m | awk 'NR==2' | awk '{ram = $2} END {print (ram > 5000 ? ram = 5000 : ram = (ram - 500))}'`;");
//			out.newLine();
//			out.write("RamTest=$RamTest'm';");
//			out.newLine();
			if(writeSDF)
				out.write("java -Dproperty.file.path=" + outputFolder + " -Xms1500m -Xmx5500m -jar " + outputFolder + "MetFragScript.jar " + onlyFiles.get(i).getName() + " " + dateString + " 1");
			else
				out.write("java -Dproperty.file.path=" + outputFolder + " -Xms1500m -Xmx5500m -jar " + outputFolder + "MetFragScript.jar " + onlyFiles.get(i).getName() + " " + dateString);
			
		  	out.close();
		}
	}
	
	public static void main(String[] args) {
		
		String folder = "";
		if(args[0] != null)
			folder = args[0];
		else
		{
			System.err.println("Error: No folder with Massbank files given given!");
			System.exit(1);
		}
		
		
		String outputFolder = "";
		if(args[1] != null)
			outputFolder = args[1];
		else
		{
			System.err.println("Error: No output folder given!");
			System.exit(1);
		}
		
		String writePath = "";
		if(args[2] != null)
			writePath = args[2];
		else
		{
			System.err.println("Error: no write path given!");
			System.exit(1);
		}
		
		boolean writeSDF = false;
		if(args[3] != null)
			writeSDF = true;
		else
		{
			System.err.println("Error: no write path given!");
			System.exit(1);
		}
		
		try {
			GenerateShScripts g = new GenerateShScripts(folder, outputFolder, writePath, writeSDF);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
