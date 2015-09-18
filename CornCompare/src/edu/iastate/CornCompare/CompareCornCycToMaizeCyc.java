package edu.iastate.CornCompare;


import java.io.File;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import edu.iastate.javacyco.Frame;
import edu.iastate.javacyco.JavacycConnection;
import edu.iastate.javacyco.Network;
import edu.iastate.javacyco.PtoolsErrorException;

/**
 * Written for Taner Sen of the MaizeGDB/CornCyc group.
 * 
 * 
 */
public class CompareCornCycToMaizeCyc {
	private static String host = "jrwalsh-server.student.iastate.edu";
	private static String organismCorn = "CORN";
	private static String organismMaize = "MAIZE";
	private static int port = 4444;
	
	public static void main(String[] args) {
		try {
			Long start = System.currentTimeMillis();
			
			// Look at a specific frame in detail
//			printFrame();
			
			// Just get a feel for what types of information I should look at for each frame type
//			getFrameByName(organismCorn, "|All-Genes|");
//			getFrameByName(organismCorn, "|Compounds|");
//			getFrameByName(organismCorn, "|Reactions|");
//			getFrameByName(organismCorn, "|Pathways|");
			
			
			// Collect info on specific frame times for CornCyc 5.0 and MaizeCyc 2.2
//			printGenesTab(organismCorn, "geneCounts_CornCyc5.tab");
//			printGenesTab(organismMaize, "geneCounts_MaizeCyc2.tab");
//			
//			printCompoundsTab(organismCorn, "compoundCounts_CornCyc5.tab");
//			printCompoundsTab(organismMaize, "compoundCounts_MaizeCyc2.tab");
//			
//			printReactionsTab(organismCorn, "reactionCounts_CornCyc5.tab");
//			printReactionsTab(organismMaize, "reactionCounts_MaizeCyc2.tab");
//			
//			printPathwaysTab(organismCorn, "pathwayCounts_CornCyc5.tab");
//			printPathwaysTab(organismMaize, "pathwayCounts_MaizeCyc2.tab");
			
			
			// Collect info on specific frame times for CornCyc 4.0.1
//			printGenesTab(organismCorn, "geneCounts_CornCyc4.tab");
//			printCompoundsTab(organismCorn, "compoundCounts_CornCyc4.tab");
//			printReactionsTab(organismCorn, "reactionCounts_CornCyc4.tab");
//			printPathwaysTab(organismCorn, "pathwayCounts_CornCyc4.tab");
			
			
			
			// This gets the counts to compare against Jackies counts.
//			getAllCounts(organismMaize);
//			getAllCounts(organismCorn);
			
			Long stop = System.currentTimeMillis();
			Long runtime = (stop - start) / 1000;
			System.out.println("Runtime is " + runtime + " seconds.");
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Caught a "+e.getClass().getName()+". Shutting down...");
		}
	}
	
	static private void getAllCounts(String organism) throws PtoolsErrorException {
		JavacycConnection conn = new JavacycConnection(host, port);
		conn.selectOrganism(organism);
		
//		getCompoundCounts(conn, organism);
//		getReactionCounts(conn, organism);
//		getEnzymaticReactionCounts(conn, organism);
//		getPathwayCounts(conn, organism);
//		getGOAnnotationCounts(conn, organism);
	}
	
	static private void getEnzymaticReactionCounts(JavacycConnection conn, String organism) throws PtoolsErrorException {
		conn.selectOrganism(organism);
		
		Network enzyRxnsHierarchy = conn.getClassHierarchy("|Enzymatic-Reactions|", true, true);
		Set<Frame> enzyRxnNodes = enzyRxnsHierarchy.getNodes();
		
		HashSet<String> enzyRxnClasses = new HashSet<String>();
		HashSet<String> enzyRxnInstances = new HashSet<String>();
		for (Frame enzyRxn : enzyRxnNodes) {
			if (enzyRxn.isClassFrame()) enzyRxnClasses.add(enzyRxn.getLocalID());
			else enzyRxnInstances.add(enzyRxn.getLocalID());
		}
		System.out.println("Enzymatic-Reactions Classes: " + enzyRxnClasses.size());
		System.out.println("Enzymatic-Reactions Instances: " + enzyRxnInstances.size());
	}
	
	
	static private void getGOAnnotationCounts(JavacycConnection conn, String organism) throws PtoolsErrorException {
		conn.selectOrganism(organism);
		
		
		//TODO what to count, exactly?  assignments? citations? unique or not? etc...
		Network pathwaysHierarchy = conn.getClassHierarchy("|Polypeptides|", true, true);
		Set<Frame> proteinNodes = pathwaysHierarchy.getNodes();
		
		
//		HashSet<String> gotermAssignments = new HashSet<String>();
//		HashSet<String> gotermCitations = new HashSet<String>();
		for (Frame protein : proteinNodes) {
			try {
				System.out.println(protein.getLocalID());
				for (Object goTerm : protein.getSlotValues("GO-TERMS")) {
					System.out.println("\t"+goTerm.toString());
					for(Object citation : protein.getAnnotations("GO-TERMS", goTerm.toString(), "CITATIONS")) {
						System.out.println("\t\t"+citation.toString());
					}
				}
//				if (protein.isClassFrame()) proteinClasses.add(protein.getLocalID());
//				else proteinInstances.add(protein.getLocalID());
			} catch (Exception e) {
				System.out.println("Problem with protein : " + protein.getLocalID() + " : " + protein.isClassFrame());
			}
		}
//		System.out.println("Protein Classes: " + proteinClasses.size());
//		System.out.println("Protein Instances: " + proteinInstances.size());
	}
}
