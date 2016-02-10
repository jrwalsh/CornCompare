package edu.iastate;

import java.io.File;
import edu.iastate.CornCompare.CompoundComparison;
import edu.iastate.CornCompare.GeneComparison;
import edu.iastate.CornCompare.PathwayComparison;
import edu.iastate.CornCompare.ProteinComparison;
import edu.iastate.CornCompare.ReactionComparison;

public class Main {
	public static void main(String[] args) {
		try {
			Long start = System.currentTimeMillis();
			
			// Process input
			String host = "";
			String outputDir = "";;
			if(args.length < 1) {
				System.out.println("Usage: Main HOST_ADDRESS OUTPUT_DIR");
				System.exit(0);
			} else if(args.length == 1) {
				host = args[0];
				outputDir = System.getProperty("user.dir");
			} else {
				host = args[0];
				outputDir = args[1];
			}
			
			int port = 4444;
			String organismCorn = "CORN";
			String organismMaize = "MAIZE";
			boolean verbose = true; 
					
			// Run program
			initiateFileStructure(outputDir);
			run(host, organismCorn, organismMaize, port, verbose, outputDir);
			
			Long stop = System.currentTimeMillis();
			Long runtime = (stop - start) / 1000;
			System.out.println("Runtime is " + runtime + " seconds.");
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Caught a "+e.getClass().getName()+". Shutting down...");
		}
	}
	
	private static void initiateFileStructure(String outputDir) {
        System.out.print("Testing file structure...");
        File file = new File(outputDir + System.getProperty("file.separator") + "Genes" + System.getProperty("file.separator") + "Transcripts" + System.getProperty("file.separator") + "test.txt");
        file.getParentFile().mkdirs();

        file = new File(outputDir + System.getProperty("file.separator") + "Proteins" + System.getProperty("file.separator") + "test.txt");
        file.getParentFile().mkdirs();
        
        file = new File(outputDir + System.getProperty("file.separator") + "Compounds" + System.getProperty("file.separator") + "test.txt");
        file.getParentFile().mkdirs();
        
        file = new File(outputDir + System.getProperty("file.separator") + "Reactions" + System.getProperty("file.separator") + "test.txt");
        file.getParentFile().mkdirs();
        
        file = new File(outputDir + System.getProperty("file.separator") + "Pathways" + System.getProperty("file.separator") + "test.txt");
        file.getParentFile().mkdirs();
        System.out.println(" done");
	}
	
	private static void run(String host, String organismCorn, String organismMaize, int port, boolean verbose, String outputDir) {
		GeneComparison geneCompare = new GeneComparison(host, organismCorn, organismMaize, port, outputDir, verbose);
		geneCompare.compare(false);
		geneCompare.compare(true);
		
		ProteinComparison proteinCompare = new ProteinComparison(host, organismCorn, organismMaize, port, outputDir, verbose);
		proteinCompare.compare();
		
		CompoundComparison compoundCompare = new CompoundComparison(host, organismCorn, organismMaize, port, outputDir, verbose);
		compoundCompare.compare();
		
		ReactionComparison reactionCompare = new ReactionComparison(host, organismCorn, organismMaize, port, outputDir, verbose);
		reactionCompare.compare();
		
		PathwayComparison pathwayCompare = new PathwayComparison(host, organismCorn, organismMaize, port, outputDir, verbose);
		pathwayCompare.compare();
	}
}
