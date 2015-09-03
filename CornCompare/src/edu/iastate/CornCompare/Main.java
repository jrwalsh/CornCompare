package edu.iastate.CornCompare;

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
			
			getAllCounts(organismMaize);
			printAllTypes(organismMaize);
			
			getAllCounts(organismCorn);
			printAllTypes(organismCorn);
			
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
//		GeneCounts.getGeneCounts(host, organism, port);
//		CompoundCounts.getCompoundCounts(host, organism, port);
		
		
		
//		getReactionCounts(conn, organism);
//		getEnzymaticReactionCounts(conn, organism);
//		getPathwayCounts(conn, organism);
//		getGOAnnotationCounts(conn, organism);
	}
	
	static private void printAllTypes(String organism) throws PtoolsErrorException {
//		GeneCounts.printGenesTab(host, organism, port, "geneCounts_"+organism+".tab");
		CompoundCounts.printCompoundsTab(host, organism, port, "compoundCounts_"+organism+".tab");
		
		
//		printCompoundsTab(organismCorn, "compoundCounts_CornCyc4.tab");
//		printReactionsTab(organismCorn, "reactionCounts_CornCyc4.tab");
//		printPathwaysTab(organismCorn, "pathwayCounts_CornCyc4.tab");
	}
}
