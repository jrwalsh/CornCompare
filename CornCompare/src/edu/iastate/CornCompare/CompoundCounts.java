package edu.iastate.CornCompare;

import java.util.HashSet;
import java.util.Set;

import edu.iastate.javacyco.Frame;
import edu.iastate.javacyco.JavacycConnection;
import edu.iastate.javacyco.Network;
import edu.iastate.javacyco.PtoolsErrorException;

public class CompoundCounts extends Counter {
	private static String ptoolsClass = "|Compounds|";
	private JavacycConnection conn;
	private String fileName;
	private boolean verbose = false;
	
	
	public CompoundCounts(String host, String organism, int port, String fileName, boolean verbose) {
		conn = new JavacycConnection(host, port);
		conn.selectOrganism(organism);
		this.fileName = fileName;
		this.verbose = verbose;
	}
	
	@Override
	public Counts count() throws PtoolsErrorException {
		if (verbose) System.out.println("Counting compounds under the GFPtype " + ptoolsClass + " for the organism " + conn.getOrganism().getLocalID());
		
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
		if (verbose) System.out.println("Compound Classes: " + compoundClasses.size());
		if (verbose) System.out.println("Compound Instances: " + compoundInstances.size());
		
		
		if (verbose) System.out.println("Printing compounds under the GFPtype " + ptoolsClass + " for the organism " + conn.getOrganism().getLocalID());
		
		String printString = "";
		printString += "FrameID\tCommonName\tisClass?" + "\n";
		for (Frame node : compoundNodes) {
			printString += node.getLocalID() + "\t" + node.getCommonName() + "\t" + node.isClassFrame() + "\t" + node.getSlotValue("Has-No-Structure?") + "\t" + node.getSlotValue("InChI") + "\t" + node.getComment() + "\n";
		}
		// Consider slots: Smiles, Non-Standard-InChI, DBLinks
		// Consider reaction membership?
		
		printString(fileName, printString);
		
		
		return null;
		// TODO Auto-generated method stub
		
	}
}
