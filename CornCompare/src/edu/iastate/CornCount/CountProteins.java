package edu.iastate.CornCount;

import java.util.HashSet;
import java.util.Set;

import edu.iastate.javacyco.Frame;
import edu.iastate.javacyco.JavacycConnection;
import edu.iastate.javacyco.Network;
import edu.iastate.javacyco.PtoolsErrorException;

public class CountProteins extends Counter {
	private static String ptoolsClass = "|Polypeptides|"; //TODO |Polypeptides| or |Proteins|?
	private JavacycConnection conn;
	private String fileName;
	private boolean verbose = false;
	
	
	public CountProteins(String host, String organism, int port, String fileName, boolean verbose) {
		conn = new JavacycConnection(host, port);
		conn.selectOrganism(organism);
		this.fileName = fileName;
		this.verbose = verbose;
	}
	
	@Override
	public Counts count() throws PtoolsErrorException {
		if (verbose) System.out.println("Counting objects under the GFPtype " + ptoolsClass + " for the organism " + conn.getOrganism().getLocalID());
		
		Network pathwaysHierarchy = conn.getClassHierarchy(ptoolsClass, true, true);
		Set<Frame> proteinNodes = pathwaysHierarchy.getNodes();
		
		HashSet<String> proteinClasses = new HashSet<String>();
		HashSet<String> proteinInstances = new HashSet<String>();
		for (Frame protein : proteinNodes) {
			try {
				if (protein.isClassFrame()) proteinClasses.add(protein.getLocalID());
				else proteinInstances.add(protein.getLocalID());
			} catch (Exception e) {
				System.out.println("Problem with protein : " + protein.getLocalID() + " : " + protein.isClassFrame());
			}
		}
		
		System.out.println("Protein Classes: " + proteinClasses.size());
		System.out.println("Protein Instances: " + proteinInstances.size());
		return new Counts();
	}
}
