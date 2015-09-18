package edu.iastate.CornCount;

import java.util.HashSet;
import java.util.Set;

import edu.iastate.javacyco.Frame;
import edu.iastate.javacyco.JavacycConnection;
import edu.iastate.javacyco.Network;
import edu.iastate.javacyco.PtoolsErrorException;

public class CountGenes extends Counter {
	private static String ptoolsClass = "|Genes|";
	private JavacycConnection conn;
	private String fileName;
	private boolean verbose = false;
	
	
	public CountGenes(String host, String organism, int port, String fileName, boolean verbose) {
		conn = new JavacycConnection(host, port);
		conn.selectOrganism(organism);
		this.fileName = fileName;
		this.verbose = verbose;
	}
	
	@Override
	public Counts count() throws PtoolsErrorException {
		if (verbose) System.out.println("Counting genes under the GFPtype " + ptoolsClass + " for the organism " + conn.getOrganism().getLocalID());
		
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
		if (verbose) System.out.println("Gene Classes: " + geneClasses.size());
		if (verbose) System.out.println("Gene Instances: " + geneInstances.size());
		if (verbose) System.out.println("Gene Unique Classes without suffix: " + geneClasses.size());
		if (verbose) System.out.println("Gene Unique Instances without suffix: " + geneUniqueInstances.size());
		
		
		if (verbose) System.out.println("Printing genes under the GFPtype " + ptoolsClass + " for the organism " + conn.getOrganism().getLocalID());
		
		String printString = "";
		printString += "FrameID\tCommonName\tisClass?" + "\n";
		for (Frame node : geneNodes) {
			printString += node.getLocalID() + "\t" + node.getCommonName() + "\t" + node.isClassFrame() + "\n";
		}
		
		printString(fileName, printString);
		
		return null;
		// TODO Auto-generated method stub
		
	}
}
