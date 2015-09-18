package edu.iastate.CornCompare;

import java.util.HashSet;
import java.util.Set;

import edu.iastate.javacyco.Frame;
import edu.iastate.javacyco.JavacycConnection;
import edu.iastate.javacyco.Network;
import edu.iastate.javacyco.PtoolsErrorException;

public class ReactionCounts extends Counter {
	private static String ptoolsClass = "|Reactions|";
	private JavacycConnection conn;
	private String fileName;
	private boolean verbose = false;
	
	
	public ReactionCounts(String host, String organism, int port, String fileName, boolean verbose) {
		conn = new JavacycConnection(host, port);
		conn.selectOrganism(organism);
		this.fileName = fileName;
		this.verbose = verbose;
	}
	
	@Override
	public Counts count() throws PtoolsErrorException {
		if (verbose) System.out.println("Counting objects under the GFPtype " + ptoolsClass + " for the organism " + conn.getOrganism().getLocalID());
		String printString = "";
		printString += "FrameID\tCommonName\tisClass?\tNumberEnzymaticReactions" + "\n";
		
		Network reactionHierarchy = conn.getClassHierarchy(ptoolsClass, true, true);
		Set<Frame> reactionNodes = reactionHierarchy.getNodes();
		
		// consider node.getDirectSuperClasses()
		for (Frame node : reactionNodes) {
			printString += node.getLocalID() + "\t" + node.getCommonName() + "\t" + node.isClassFrame() + "\t" + node.getSlotValues("Enzymatic-Reaction").size() + "\t" + node.getSlotValue("Left") + "\t" + node.getSlotValue("Right") + "\t" + node.getSlotValue("In-Pathway") + "\n";
		}
		
		HashSet<String> reactionClasses = new HashSet<String>();
		HashSet<String> reactionInstances = new HashSet<String>();
		for (Frame reaction : reactionNodes) {
			if (reaction.isClassFrame()) {
				reactionClasses.add(reaction.getLocalID());
			}
			else {
				reactionInstances.add(reaction.getLocalID());
			}
		}
		
		if (verbose) System.out.println("Classes: " + reactionClasses.size());
		if (verbose) System.out.println("Instances: " + reactionInstances.size());
		
		if (fileName.length() > 0) printString(fileName, printString);
		return new Counts();
	}
}
