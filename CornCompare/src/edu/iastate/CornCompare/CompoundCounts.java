package edu.iastate.CornCompare;

import java.io.File;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import edu.iastate.javacyco.Compound;
import edu.iastate.javacyco.Frame;
import edu.iastate.javacyco.JavacycConnection;
import edu.iastate.javacyco.Network;
import edu.iastate.javacyco.PtoolsErrorException;

public class CompoundCounts {
	private static String ptoolsClass = "|Compounds|";
	
	public static void getCompoundCounts(String host, String organism, int port) throws PtoolsErrorException {
		JavacycConnection conn = new JavacycConnection(host, port);
		conn.selectOrganism(organism);
		
		System.out.println("Counting compounds under the GFPtype " + ptoolsClass + " for the organism " + organism);
		
		Network compoundHierarchy = conn.getClassHierarchy(ptoolsClass, true, true);
		Set<Frame> compoundNodes = compoundHierarchy.getNodes();
		
		HashSet<String> compoundClasses = new HashSet<String>();
		HashSet<String> compoundInstances = new HashSet<String>();
		for (Frame compound : compoundNodes) {
			if (compound.isClassFrame()) {
				compoundClasses.add(compound.getLocalID());
			}
			else {
				compoundInstances.add(compound.getLocalID());
			}
		}
		System.out.println("Compound Classes: " + compoundClasses.size());
		System.out.println("Compound Instances: " + compoundInstances.size());
	}
	
	public static void printCompoundsTab(String host, String organism, int port, String fileName) throws PtoolsErrorException {
		JavacycConnection conn = new JavacycConnection(host, port);
		conn.selectOrganism(organism);
		
		System.out.println("Printing compounds under the GFPtype " + ptoolsClass + " for the organism " + organism);
		
		Network hierarchy = conn.getClassHierarchy(ptoolsClass, true, true);
		Set<Frame> nodes = hierarchy.getNodes();
		
		String printString = "";
		printString += "FrameID\tCommonName\tisClass?" + "\n";
		for (Frame node : nodes) {
			printString += node.getLocalID() + "\t" + node.getCommonName() + "\t" + node.isClassFrame() + "\t" + node.getSlotValue("Has-No-Structure?") + "\t" + node.getSlotValue("InChI") + "\t" + node.getComment() + "\n";
		}
		// Consider slots: Smiles, Non-Standard-InChI, DBLinks
		// Consider reaction membership?
		
		printString(fileName, printString);
	}
	
	/**
	 * Simple function to print a string to the specified file location.
	 * 
	 * @param fileName
	 * @param printString
	 */
	private static void printString(String fileName, String printString) {
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
