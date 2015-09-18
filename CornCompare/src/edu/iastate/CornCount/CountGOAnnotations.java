package edu.iastate.CornCount;

import java.util.HashSet;
import java.util.Set;

import edu.iastate.javacyco.Frame;
import edu.iastate.javacyco.JavacycConnection;
import edu.iastate.javacyco.Network;
import edu.iastate.javacyco.PtoolsErrorException;

public class CountGOAnnotations extends Counter {
	private static String ptoolsClass = "|Polypeptides|"; //TODO |Polypeptides| or |Proteins|?
	private JavacycConnection conn;
	private String fileName;
	private boolean verbose = false;
	
	
	public CountGOAnnotations(String host, String organism, int port, String fileName, boolean verbose) {
		conn = new JavacycConnection(host, port);
		conn.selectOrganism(organism);
		this.fileName = fileName;
		this.verbose = verbose;
	}
	
	@Override
	public Counts count() throws PtoolsErrorException {
		//TODO what to count, exactly?  assignments? citations? unique or not? etc...
		Network pathwaysHierarchy = conn.getClassHierarchy(ptoolsClass, true, true);
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
		return new Counts();
	}
}
