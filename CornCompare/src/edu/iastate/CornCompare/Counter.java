package edu.iastate.CornCompare;

import java.io.File;
import java.io.PrintStream;

import edu.iastate.javacyco.PtoolsErrorException;

public abstract class Counter  {
	
	
	public abstract Counts count() throws PtoolsErrorException;
	
	/**
	 * Simple function to print a string to the specified file location.
	 * 
	 * @param fileName
	 * @param printString
	 */
	protected void printString(String fileName, String printString) {
		PrintStream o = null;
		try {
			o = new PrintStream(new File(fileName));
			o.println(printString);
			o.close();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
