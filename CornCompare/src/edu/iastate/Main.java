package edu.iastate;

import java.util.HashSet;

import edu.iastate.CornCompare.Comparison;
import edu.iastate.CornCompare.CompoundComparison;
import edu.iastate.CornCompare.CompoundItem;
import edu.iastate.CornCompare.GeneComparison;
import edu.iastate.CornCompare.GeneItem;
import edu.iastate.CornCount.CountCompounds;
import edu.iastate.CornCount.CountEnzymaticReactions;
import edu.iastate.CornCount.CountGOAnnotations;
import edu.iastate.CornCount.CountGenes;
import edu.iastate.CornCount.CountPathways;
import edu.iastate.CornCount.CountProteins;
import edu.iastate.CornCount.CountReactions;
import edu.iastate.javacyco.PtoolsErrorException;

public class Main {
	private static String host = "jrwalsh-server.student.iastate.edu";
	private static String organismCorn = "CORN";
	private static String organismMaize = "MAIZE";
	private static int port = 4444;
	
	public static void main(String[] args) {
		try {
			Long start = System.currentTimeMillis();
			
			count();
			compare();
			
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
		GeneComparison geneCompare = new GeneComparison(host, organismMaize, organismCorn, port, "geneCompare.tab", true);
		Comparison<GeneItem> geneComparison = geneCompare.compare();
		
		CompoundComparison compoundCompare = new CompoundComparison(host, organismMaize, organismCorn, port, "compoundCompare.tab", true);
		Comparison<CompoundItem> compoundComparison = compoundCompare.compare();
		
		
		testOutput(compoundComparison);
	}
	
	static private void testOutput(Comparison<CompoundItem> comparison) {
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
		
		HashSet<Object> matched = new HashSet<Object>();
		matched.addAll(comparison.matched);
		HashSet<Object> uniqueListA = new HashSet<Object>();
		uniqueListA.addAll(comparison.uniqueListA);
		HashSet<Object> uniqueListB = new HashSet<Object>();
		uniqueListB.addAll(comparison.uniqueListB);
		System.out.println("Matching Frames: " + comparison.matched.size() + " : (" + matched.size() + " unique)");
		System.out.println("Frames in A: " + comparison.uniqueListA.size() + " : (" + uniqueListA.size() + " unique)");
		System.out.println("Frames in B: " + comparison.uniqueListB.size() + " : (" + uniqueListB.size() + " unique)");
	}
}
