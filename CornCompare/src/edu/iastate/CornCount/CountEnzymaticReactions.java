package edu.iastate.CornCount;

import java.util.HashSet;
import java.util.Set;

import edu.iastate.javacyco.Frame;
import edu.iastate.javacyco.JavacycConnection;
import edu.iastate.javacyco.Network;
import edu.iastate.javacyco.PtoolsErrorException;

public class CountEnzymaticReactions extends Counter {
	private static String ptoolsClass = "|Enzymatic-Reactions|";
	private JavacycConnection conn;
	private String fileName;
	private boolean verbose = false;
	
	
	public CountEnzymaticReactions(String host, String organism, int port, String fileName, boolean verbose) {
		conn = new JavacycConnection(host, port);
		conn.selectOrganism(organism);
		this.fileName = fileName;
		this.verbose = verbose;
	}
	
	@Override
	public Counts count() throws PtoolsErrorException {
		if (verbose) System.out.println("Counting objects under the GFPtype " + ptoolsClass + " for the organism " + conn.getOrganism().getLocalID());
		
		Network enzyRxnsHierarchy = conn.getClassHierarchy(ptoolsClass, true, true);
		Set<Frame> enzyRxnNodes = enzyRxnsHierarchy.getNodes();
		
		HashSet<String> enzyRxnClasses = new HashSet<String>();
		HashSet<String> enzyRxnInstances = new HashSet<String>();
		for (Frame enzyRxn : enzyRxnNodes) {
			if (enzyRxn.isClassFrame()) enzyRxnClasses.add(enzyRxn.getLocalID());
			else enzyRxnInstances.add(enzyRxn.getLocalID());
		}
		System.out.println("Enzymatic-Reactions Classes: " + enzyRxnClasses.size());
		System.out.println("Enzymatic-Reactions Instances: " + enzyRxnInstances.size());
		
		return new Counts();
	}
}
