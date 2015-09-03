package edu.iastate.CornCompare;

import java.io.File;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import edu.iastate.javacyco.Frame;
import edu.iastate.javacyco.JavacycConnection;
import edu.iastate.javacyco.Network;
import edu.iastate.javacyco.PtoolsErrorException;

public class GeneCounts {
	private static String ptoolsClass = "|Genes|";
	
	public static void getGeneCounts(String host, String organism, int port) throws PtoolsErrorException {
		JavacycConnection conn = new JavacycConnection(host, port);
		conn.selectOrganism(organism);
		
		System.out.println("Counting genes under the GFPtype " + ptoolsClass + " for the organism " + organism);
		
		Network geneHierarchy = conn.getClassHierarchy(ptoolsClass, true, true);
		Set<Frame> geneNodes = geneHierarchy.getNodes();
		
		HashSet<String> geneClasses = new HashSet<String>();
		HashSet<String> geneInstances = new HashSet<String>();
		HashSet<String> geneUniqueClasses = new HashSet<String>();
		HashSet<String> geneUniqueInstances = new HashSet<String>();
		for (Frame gene : geneNodes) {
			if (gene.isClassFrame()) {
				geneClasses.add(gene.getLocalID());
				geneUniqueClasses.add(gene.getCommonName().replaceAll("_P..$|_T..$", ""));
			}
			else {
				geneInstances.add(gene.getLocalID());
				geneUniqueInstances.add(gene.getCommonName().replaceAll("_P..$|_T..$", ""));
			}
		}
		System.out.println("Gene Classes: " + geneClasses.size());
		System.out.println("Gene Instances: " + geneInstances.size());
		System.out.println("Gene Unique Classes without suffix: " + geneClasses.size());
		System.out.println("Gene Unique Instances without suffix: " + geneUniqueInstances.size());
	}
	
	public static void printGenesTab(String host, String organism, int port, String fileName) throws PtoolsErrorException {
		JavacycConnection conn = new JavacycConnection(host, port);
		conn.selectOrganism(organism);
		
		System.out.println("Printing genes under the GFPtype " + ptoolsClass + " for the organism " + organism);
		
		Network hierarchy = conn.getClassHierarchy(ptoolsClass, true, true);
		Set<Frame> nodes = hierarchy.getNodes();
		
		String printString = "";
		printString += "FrameID\tCommonName\tisClass?" + "\n";
		for (Frame node : nodes) {
			printString += node.getLocalID() + "\t" + node.getCommonName() + "\t" + node.isClassFrame() + "\n";
		}
		
		printString(fileName, printString);
	}
	
	/**
	 * Simple function to print a string to the specified file location.
	 * 
	 * @param fileName
	 * @param printString
	 */
	private static void printString(String fileName, String printString) {
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
