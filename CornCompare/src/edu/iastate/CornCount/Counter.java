package edu.iastate.CornCount;

import java.io.File;
import java.io.PrintStream;

import edu.iastate.javacyco.Frame;
import edu.iastate.javacyco.JavacycConnection;
import edu.iastate.javacyco.PtoolsErrorException;

public abstract class Counter  {
	
	
	public abstract CountedFrames count() throws PtoolsErrorException;
	
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
	
	protected void printDirectSubs(JavacycConnection conn, Frame frame) {
		try {
			for (Object sub : conn.getClassDirectSubs(frame.getLocalID())) {
				Frame.load(conn, sub.toString()).print();
				printDirectSubs(conn, Frame.load(conn, sub.toString()));
			}
		} catch (PtoolsErrorException e) {
			System.out.println("Didin't like : " + frame.getLocalID());
		}
	}
		
}
