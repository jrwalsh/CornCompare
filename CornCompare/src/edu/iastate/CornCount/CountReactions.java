package edu.iastate.CornCount;

import java.util.HashSet;
import java.util.Set;

import edu.iastate.javacyco.Frame;
import edu.iastate.javacyco.JavacycConnection;
import edu.iastate.javacyco.Network;
import edu.iastate.javacyco.PtoolsErrorException;

public class CountReactions extends Counter {
	private static String ptoolsClass = "|Reactions|";
	private JavacycConnection conn;
	private String fileName;
	private boolean verbose = false;
	
	
	public CountReactions(String host, String organism, int port, String fileName, boolean verbose) {
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
		
//		for (Frame node : nodes) {
//			
//			String reactants = "";
//			for (Object reactant : node.getSlotValues("Left")) {
//				reactants += reactant + ", ";
//			}
//			if (reactants.length() > 0) reactants = reactants.substring(0, reactants.length()-2);
//			
//			String products = "";
//			for (Object product : node.getSlotValues("right")) {
//				products += product + ", ";
//			}
//			if (products.length() > 0) products = products.substring(0, products.length()-2);
//			
//			printString += node.getLocalID() + "\t" + node.getCommonName() + "\t" + node.getSlotValue("EC-NUMBER") + "\t" + node.getSlotValue("REACTION-DIRECTION") + "\t" + node.getSlotValues("ENZYMATIC-REACTION").size() + "\t" + node.isClassFrame() + "\t" + reactants + "\t" + products + "\n";
//		}
		
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
