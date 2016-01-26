package edu.iastate;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;

import edu.iastate.CornCompare.Comparison;
import edu.iastate.CornCompare.CompoundComparison;
import edu.iastate.CornCompare.CompoundItem;
import edu.iastate.CornCompare.GeneComparison;
import edu.iastate.CornCompare.GeneItem;
import edu.iastate.CornCompare.PathwayComparison;
import edu.iastate.CornCompare.PathwayItem;
import edu.iastate.CornCompare.ReactionComparison;
import edu.iastate.CornCompare.ReactionItem;
import edu.iastate.CornCount.CountCompounds;
import edu.iastate.CornCount.CountEnzymaticReactions;
import edu.iastate.CornCount.CountGOAnnotations;
import edu.iastate.CornCount.CountGenes;
import edu.iastate.CornCount.CountPathways;
import edu.iastate.CornCount.CountProteins;
import edu.iastate.CornCount.CountReactions;
import edu.iastate.javacyco.Frame;
import edu.iastate.javacyco.JavacycConnection;
import edu.iastate.javacyco.PtoolsErrorException;

public class Main {
	private static String host = "jrwalsh-server.student.iastate.edu";
	private static String organismCorn = "CORN";
	private static String organismMaize = "MAIZE";
	private static int port = 4444;
	
	public static void main(String[] args) {
		try {
			Long start = System.currentTimeMillis();
			
//			count();
			compare();
			
//			JavacycConnection conn = new JavacycConnection(host, port);
//			conn.selectOrganism(organismMaize);
//			for (Frame item : conn.getAllGFPInstances("|Transport-Reactions|")) {
//				System.out.println(item.getLocalID() + " : is tranport? " + item.isGFPClass("|Transport-Reactions|"));
//			}
			
			
			Long stop = System.currentTimeMillis();
			Long runtime = (stop - start) / 1000;
			System.out.println("Runtime is " + runtime + " seconds.");
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Caught a "+e.getClass().getName()+". Shutting down...");
		}
	}
	
	static private void count() throws PtoolsErrorException {
		CountCompounds compoundCountsMaize = new CountCompounds(host, organismMaize, port, "compoundCounts_Maize.tab", true);
		compoundCountsMaize.count();
		CountCompounds compoundCountsCorn = new CountCompounds(host, organismCorn, port, "compoundCounts_Corn.tab", true);
		compoundCountsCorn.count();
		
		
		CountGenes geneCountsMaize = new CountGenes(host, organismMaize, port, "geneCounts_Maize.tab", true);
		geneCountsMaize.count();
		CountGenes geneCountsCorn = new CountGenes(host, organismCorn, port, "geneCounts_Corn.tab", true);
		geneCountsCorn.count();
		
		CountReactions reactionCountsMaize = new CountReactions(host, organismMaize, port, "reactionCounts_Maize.tab", true);
		reactionCountsMaize.count();
		CountReactions reactionCountsCorn = new CountReactions(host, organismCorn, port, "reactionCounts_Corn.tab", true);
		reactionCountsCorn.count();
		
		CountPathways pathwayCountsMaize = new CountPathways(host, organismMaize, port, "pathwayCounts_Maize.tab", true);
		pathwayCountsMaize.count();
		CountPathways pathwayCountsCorn = new CountPathways(host, organismCorn, port, "pathwayCounts_Corn.tab", true);
		pathwayCountsCorn.count();
		
		
		CountEnzymaticReactions enzyRxnCountsMaize = new CountEnzymaticReactions(host, organismMaize, port, "enzyRxnCounts_Maize.tab", true);
		enzyRxnCountsMaize.count();
		CountEnzymaticReactions enzyRxnCountsCorn = new CountEnzymaticReactions(host, organismCorn, port, "enzyRxnCounts_Corn.tab", true);
		enzyRxnCountsCorn.count();
		
		
		CountGOAnnotations GOAnnotationsCountsMaize = new CountGOAnnotations(host, organismMaize, port, "GOAnnotationCounts_Maize.tab", true);
		GOAnnotationsCountsMaize.count();
		CountGOAnnotations GOAnnotationsCountsCorn = new CountGOAnnotations(host, organismCorn, port, "GOAnnotationCounts_Corn.tab", true);
		GOAnnotationsCountsCorn.count();
		
		
		CountProteins proteinCountsMaize = new CountProteins(host, organismMaize, port, "proteinCounts_Maize.tab", true);
		proteinCountsMaize.count();
		CountProteins proteinCountsCorn = new CountProteins(host, organismCorn, port, "proteinCounts_Corn.tab", true);
		proteinCountsCorn.count();
	}
	
	static private void compare() throws PtoolsErrorException {
		// Convert Comparison lists to sets in order to count overlap, otherwise it gives all frames involved in the overlap
//		GeneComparison geneCompare = new GeneComparison(host, organismMaize, organismCorn, port, "geneCompare.tab", true);
//		Comparison<GeneItem> geneComparison = geneCompare.compare();
//		
//		CompoundComparison compoundCompare = new CompoundComparison(host, organismMaize, organismCorn, port, "compoundCompare.tab", true);
//		Comparison<CompoundItem> compoundComparison = compoundCompare.compare();
		
		
		// ---------- Do reaction Comparison -----------
		ReactionComparison reactionCompare = new ReactionComparison(host, organismMaize, organismCorn, port, "reactionCompare.tab", true);
		
		System.out.println("Comparing by FrameID");
		Comparison<String> reactionComparisonOnFrameID = reactionCompare.compareByFrameID();
		System.out.println("Matching Frames: " + reactionComparisonOnFrameID.matchedClasses.size() + " Classes and " + reactionComparisonOnFrameID.matchedInstances.size() + " Instances");
		System.out.println("Frames in A only: " + reactionComparisonOnFrameID.uniqueClassesA.size() + " Classes and " + reactionComparisonOnFrameID.uniqueInstancesA.size() + " Instances");
		System.out.println("Frames in B only: " + reactionComparisonOnFrameID.uniqueClassesB.size() + " Classes and " + reactionComparisonOnFrameID.uniqueInstancesB.size() + " Instances");
		
//		System.out.println("Comparing by data (common name)");
//		Comparison<ReactionItem> reactionComparisonOnData = reactionCompare.compareByData();
//		System.out.println("Matching Frames: " + reactionComparisonOnData.matchedClasses.size() + " Classes and " + reactionComparisonOnData.matchedInstances.size() + " Instances");
//		System.out.println("Frames in A only: " + reactionComparisonOnData.uniqueClassesA.size() + " Classes and " + reactionComparisonOnData.uniqueInstancesA.size() + " Instances");
//		System.out.println("Frames in B only: " + reactionComparisonOnData.uniqueClassesB.size() + " Classes and " + reactionComparisonOnData.uniqueInstancesB.size() + " Instances");
		
//		printString("uniqeReactionFrameIDs_Maize.tab", arrayToString(reactionComparisonOnFrameID.uniqueInstancesA));
//		printString("uniqeReactionFrameIDs_Corn.tab", arrayToString(reactionComparisonOnFrameID.uniqueInstancesB));
//		printString("uniqeReactionCommonNames_Maize.tab", arrayToString(reactionComparisonOnData.uniqueInstancesA));
//		printString("uniqeReactionCommonNames_Corn.tab", arrayToString(reactionComparisonOnData.uniqueInstancesB));
//		printString(arrayToString(), ".tab");
		
		
//		testOutput(reactionComparisonOnFrameID);
//		testOutput(reactionComparisonOnData);
		// --------------------------------------------
		
//		
//		// ---------- Do pathway Comparison -----------
//		PathwayComparison pathwayCompare = new PathwayComparison(host, organismMaize, organismCorn, port, "pathwayCompare.tab", true);
//		System.out.println("Comparing by FrameID");
//		Comparison<String> pathwayComparisonOnFrameID = pathwayCompare.compareByFrameID();
//		System.out.println("Matching Frames: " + pathwayComparisonOnFrameID.matchedClasses.size() + " Classes and " + pathwayComparisonOnFrameID.matchedInstances.size() + " Instances");
//		System.out.println("Frames in A only: " + pathwayComparisonOnFrameID.uniqueClassesA.size() + " Classes and " + pathwayComparisonOnFrameID.uniqueInstancesA.size() + " Instances");
//		System.out.println("Frames in B only: " + pathwayComparisonOnFrameID.uniqueClassesB.size() + " Classes and " + pathwayComparisonOnFrameID.uniqueInstancesB.size() + " Instances");
//		
//		for (String item : pathwayComparisonOnFrameID.uniqueClassesA) System.out.println(item);
//		for (String item : pathwayComparisonOnFrameID.uniqueInstancesA) System.out.println(item);
//		for (String item : pathwayComparisonOnFrameID.uniqueInstancesB) System.out.println(item);
//		// --------------------------------------------
	}
	
	static private void testOutput(Comparison<ReactionItem> comparison) {
		// Test Output
//		System.out.println("Matched:");
//		for (Object item : comparison.matched) {
//			System.out.println("	" + item);
//		}
//		System.out.println("uniqueListA:");
//		for (Object item : comparison.uniqueListA) {
//			System.out.println("	" + item);
//		}
//		System.out.println("uniqueListB:");
//		for (Object item : comparison.uniqueListB) {
//			System.out.println("	" + item);
//		}
		
//		HashSet<Object> matched = new HashSet<Object>();
//		matched.addAll(comparison.matched);
//		HashSet<Object> uniqueListA = new HashSet<Object>();
//		uniqueListA.addAll(comparison.uniqueListA);
//		HashSet<Object> uniqueListB = new HashSet<Object>();
//		uniqueListB.addAll(comparison.uniqueListB);
//		System.out.println("Matching Frames: " + comparison.matched.size() + " : (" + matched.size() + " unique)");
//		System.out.println("Frames in A: " + comparison.uniqueListA.size() + " : (" + uniqueListA.size() + " unique)");
//		System.out.println("Frames in B: " + comparison.uniqueListB.size() + " : (" + uniqueListB.size() + " unique)");
	}
	
	protected static String arrayToString(ArrayList array) {
		String out = "";
		for (Object item : array) {
			out += item + "\n";
		}
		return out;
	}
	
	protected static void printString(String fileName, String printString) {
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
