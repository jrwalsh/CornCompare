package edu.iastate;

import java.io.File;
import java.io.PrintStream;
import edu.iastate.CornCompare.CompoundComparison;
import edu.iastate.CornCompare.GeneComparison;
import edu.iastate.CornCompare.PathwayComparison;
import edu.iastate.CornCompare.ProteinComparison;
import edu.iastate.CornCompare.ReactionComparison;
import edu.iastate.javacyco.PtoolsErrorException;

public class Main {
	public static void main(String[] args) {
		try {
			Long start = System.currentTimeMillis();
			
			// Process input
			String host = "jrwalsh-server.student.iastate.edu";
			int port = 4444;
			String organismCorn = "CORN";
			String organismMaize = "MAIZE";
			boolean verbose = true;
			
			// Run program
//			test();
			initiateFileStructure();
			run(host, organismCorn, organismMaize, port, verbose);
			
			Long stop = System.currentTimeMillis();
			Long runtime = (stop - start) / 1000;
			System.out.println("Runtime is " + runtime + " seconds.");
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Caught a "+e.getClass().getName()+". Shutting down...");
		}
	}
	
	private static void initiateFileStructure() {
        System.out.print("Testing file structure...");
        File file = new File("Genes\\Transcripts\\test.txt");
        file.getParentFile().mkdirs();

        file = new File("Proteins\\test.txt");
        file.getParentFile().mkdirs();
        
        file = new File("Compounds\\test.txt");
        file.getParentFile().mkdirs();
        
        file = new File("Reactions\\test.txt");
        file.getParentFile().mkdirs();
        
        file = new File("Pathways\\test.txt");
        file.getParentFile().mkdirs();
        System.out.println(" done");
	}
	
	private static void run(String host, String organismCorn, String organismMaize, int port, boolean verbose) {
		GeneComparison geneCompare = new GeneComparison(host, organismCorn, organismMaize, port, "geneCompare.tab", verbose);
		geneCompare.compare(false);
		geneCompare.compare(true);
		
		ProteinComparison proteinCompare = new ProteinComparison(host, organismCorn, organismMaize, port, "proteinCompare.tab", verbose);
		proteinCompare.compare();
		
		CompoundComparison compoundCompare = new CompoundComparison(host, organismCorn, organismMaize, port, "compoundCompare.tab", verbose);
		compoundCompare.compare();
		
		ReactionComparison reactionCompare = new ReactionComparison(host, organismCorn, organismMaize, port, "compoundCompare.tab", verbose);
		reactionCompare.compare();
		
		PathwayComparison pathwayCompare = new PathwayComparison(host, organismCorn, organismMaize, port, "compoundCompare.tab", verbose);
		pathwayCompare.compare();
	}


	private static void test() throws PtoolsErrorException {
//		JavacycConnection conn = new JavacycConnection("jrwalsh-server.student.iastate.edu", 4444);
//		conn.selectOrganism("MAIZE");
//		
//		String printString = "";
//		for (Frame gene : conn.getAllGFPInstances("|Genes|")) {
//			printString += gene.getCommonName()+"\n";
//		}
//		printString("geneNames.txt", printString);
		
		
//		ArrayList<Frame> frames = conn.search("_FG", Gene.GFPtype);
//		for (Frame frame : frames) {
//			if (!frame.getCommonName().contains("_FGT")) System.out.println(frame.getCommonName());  
////			System.out.println(frame.getCommonName());
//			String itemGeneID = frame.getCommonName();
//			itemGeneID = itemGeneID.replaceAll("_P..$", ""); //Remove _P## suffixs, which indicate this is a transcript
//			itemGeneID = itemGeneID.replaceAll("_T..$", ""); //Remove _T## suffixs, which also indicate this is a transcript
//			itemGeneID = itemGeneID.replaceAll("_FGP...$", ""); //Remove FGP#### suffixs, which also indicate this is a transcript
//			itemGeneID = itemGeneID.replaceAll("_FGT...$", ""); //Remove FGP#### suffixs, which also indicate this is a transcript
////			itemGeneID = itemGeneID.replaceAll("_FG...$", ""); //Having manually looked at the IDs, only one gene has an _FG number, so removing it isn't necessary
////			System.out.println(itemGeneID);
//		}
//		System.out.println("Test done");
		
		
		
		
		
//		//REGEX test
//		String s = "GRMZM2G062416_P02";
//		String p = "(.*)(_P)(\\d\\d$)";
//		Pattern r = Pattern.compile(p);
//		Matcher m = r.matcher(s);
//		if (m.find()) {
//			System.out.println("Found value: " + m.group(0));
//			String output = m.replaceFirst("$1_T$3");
//			System.out.println(output);
//		} else {
//			System.out.println("No Match");
//		}
		
		
		
	}


//	static private void count() throws PtoolsErrorException {
//		CountCompounds compoundCountsMaize = new CountCompounds(host, organismMaize, port, "compoundCounts_Maize.tab", true);
//		compoundCountsMaize.count();
//		CountCompounds compoundCountsCorn = new CountCompounds(host, organismCorn, port, "compoundCounts_Corn.tab", true);
//		compoundCountsCorn.count();
//		
//		
//		CountGenes geneCountsMaize = new CountGenes(host, organismMaize, port, "geneCounts_Maize.tab", true);
//		geneCountsMaize.count();
//		CountGenes geneCountsCorn = new CountGenes(host, organismCorn, port, "geneCounts_Corn.tab", true);
//		geneCountsCorn.count();
//		
//		CountReactions reactionCountsMaize = new CountReactions(host, organismMaize, port, "reactionCounts_Maize.tab", true);
//		reactionCountsMaize.count();
//		CountReactions reactionCountsCorn = new CountReactions(host, organismCorn, port, "reactionCounts_Corn.tab", true);
//		reactionCountsCorn.count();
//		
//		CountPathways pathwayCountsMaize = new CountPathways(host, organismMaize, port, "pathwayCounts_Maize.tab", true);
//		pathwayCountsMaize.count();
//		CountPathways pathwayCountsCorn = new CountPathways(host, organismCorn, port, "pathwayCounts_Corn.tab", true);
//		pathwayCountsCorn.count();
//		
//		
//		CountEnzymaticReactions enzyRxnCountsMaize = new CountEnzymaticReactions(host, organismMaize, port, "enzyRxnCounts_Maize.tab", true);
//		enzyRxnCountsMaize.count();
//		CountEnzymaticReactions enzyRxnCountsCorn = new CountEnzymaticReactions(host, organismCorn, port, "enzyRxnCounts_Corn.tab", true);
//		enzyRxnCountsCorn.count();
//		
//		
//		CountGOAnnotations GOAnnotationsCountsMaize = new CountGOAnnotations(host, organismMaize, port, "GOAnnotationCounts_Maize.tab", true);
//		GOAnnotationsCountsMaize.count();
//		CountGOAnnotations GOAnnotationsCountsCorn = new CountGOAnnotations(host, organismCorn, port, "GOAnnotationCounts_Corn.tab", true);
//		GOAnnotationsCountsCorn.count();
//		
//		
//		CountProteins proteinCountsMaize = new CountProteins(host, organismMaize, port, "proteinCounts_Maize.tab", true);
//		proteinCountsMaize.count();
//		CountProteins proteinCountsCorn = new CountProteins(host, organismCorn, port, "proteinCounts_Corn.tab", true);
//		proteinCountsCorn.count();
//	}
	
//	static private void compare() throws PtoolsErrorException {
		// Convert Comparison lists to sets in order to count overlap, otherwise it gives all frames involved in the overlap
//		GeneComparison geneCompare = new GeneComparison(host, organismMaize, organismCorn, port, "geneCompare.tab", true);
//		Comparison<GeneItem> geneComparison = geneCompare.compare();
//		testOutputGene(geneComparison);
//		
//		CompoundComparison compoundCompare = new CompoundComparison(host, organismMaize, organismCorn, port, "compoundCompare.tab", true);
//		Comparison<CompoundItem> compoundComparison = compoundCompare.compare();
		
		
		// ---------- Do reaction Comparison -----------
//		ReactionComparison reactionCompare = new ReactionComparison(host, organismMaize, organismCorn, port, "reactionCompare.tab", true);
//		
//		System.out.println("Comparing by FrameID");
//		Comparison<String> reactionComparisonOnFrameID = reactionCompare.compareByFrameID();
//		System.out.println("Matching Frames: " + reactionComparisonOnFrameID.matchedClasses.size() + " Classes and " + reactionComparisonOnFrameID.matchedInstances.size() + " Instances");
//		System.out.println("Frames in A only: " + reactionComparisonOnFrameID.uniqueClassesA.size() + " Classes and " + reactionComparisonOnFrameID.uniqueInstancesA.size() + " Instances");
//		System.out.println("Frames in B only: " + reactionComparisonOnFrameID.uniqueClassesB.size() + " Classes and " + reactionComparisonOnFrameID.uniqueInstancesB.size() + " Instances");
		
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
//	}
	
//	static private void testOutputGene(Comparison<GeneItem> comparison) {
//		HashSet<Object> matched = new HashSet<Object>();
//		matched.addAll(comparison.matchedInstances);
//		HashSet<Object> uniqueListA = new HashSet<Object>();
//		uniqueListA.addAll(comparison.uniqueInstancesA);
//		HashSet<Object> uniqueListB = new HashSet<Object>();
//		uniqueListB.addAll(comparison.uniqueInstancesB);
//		System.out.println("Matching Frames: " + comparison.matchedInstances.size() + " : (" + matched.size() + " unique)");
//		System.out.println("Frames in A: " + comparison.uniqueInstancesA.size() + " : (" + uniqueListA.size() + " unique)");
//		System.out.println("Frames in B: " + comparison.uniqueInstancesB.size() + " : (" + uniqueListB.size() + " unique)");
//	}
	
//	static private void testOutput(Comparison<ReactionItem> comparison) {
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
//	}
	
//	protected static String arrayToString(ArrayList array) {
//		String out = "";
//		for (Object item : array) {
//			out += item + "\n";
//		}
//		return out;
//	}
	
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
