package edu.iastate.CornCompare;

import java.util.HashSet;
import java.util.Set;

import edu.iastate.javacyco.Frame;
import edu.iastate.javacyco.JavacycConnection;
import edu.iastate.javacyco.Network;
import edu.iastate.javacyco.PtoolsErrorException;

public class PathwayCounts extends Counter {
	private static String ptoolsClass = "|Pathways|";
	private JavacycConnection conn;
	private String fileName;
	private boolean verbose = false;
	
	
	public PathwayCounts(String host, String organism, int port, String fileName, boolean verbose) {
		conn = new JavacycConnection(host, port);
		conn.selectOrganism(organism);
		this.fileName = fileName;
		this.verbose = verbose;
	}
	
	@Override
	public Counts count() throws PtoolsErrorException {
		if (verbose) System.out.println("Counting objects under the GFPtype " + ptoolsClass + " for the organism " + conn.getOrganism().getLocalID());
		String printString = "";
		printString += "FrameID\tCommonName\tisClass?\tisSuperPwy?" + "\n";
		
		Network hierarchy = conn.getClassHierarchy(ptoolsClass, true, true);
		Set<Frame> nodes = hierarchy.getNodes();
		
		for (Frame node : nodes) {
			printString += node.getLocalID() + "\t" + node.getCommonName() + "\t" + node.isClassFrame() + "\t" + node.isGFPClass("|Super-Pathways|") + "\n";
		}
		
		HashSet<String> classes = new HashSet<String>();
		HashSet<String> supers = new HashSet<String>();
		HashSet<String> instances = new HashSet<String>();
		for (Frame node : nodes) {
			if (node.isClassFrame()) {
				classes.add(node.getLocalID());
			}
			else if (node.isGFPClass("|Super-Pathways|")) {
				supers.add(node.getLocalID());
			}
			else {
				instances.add(node.getLocalID());
			}
		}
		
		if (verbose) System.out.println("Classes: " + classes.size());
		if (verbose) System.out.println("Super Pathways: " + supers.size());
		if (verbose) System.out.println("Instances: " + instances.size());
		
		if (fileName.length() > 0) printString(fileName, printString);
		return new Counts();
	}
}
