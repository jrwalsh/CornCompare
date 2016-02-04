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
	public CountedFrames count() throws PtoolsErrorException {
		if (verbose) System.out.println("Counting objects under the GFPtype " + ptoolsClass + " for the organism " + conn.getOrganism().getLocalID());
		
		Network pathwaysHierarchy = conn.getClassHierarchy(ptoolsClass, true, true);
		Set<Frame> proteinNodes = pathwaysHierarchy.getNodes();
		
		HashSet<String> uniqueGoTerms = new HashSet<String>();
		int gotermAssignments = 0;
		int gotermCitations = 0;
		for (Frame protein : proteinNodes) {
			try {
				for (Object goTerm : protein.getSlotValues("GO-TERMS")) {
					uniqueGoTerms.add(goTerm.toString());
					gotermAssignments++;
					for(Object citation : protein.getAnnotations("GO-TERMS", goTerm.toString(), "CITATIONS")) {
						gotermCitations++;
					}
				}
			} catch (Exception e) {
				System.out.println("Problem with protein : " + protein.getLocalID() + " : " + protein.isClassFrame());
			}
		}
		
		System.out.println("Unique GO Terms: " + uniqueGoTerms.size());
		System.out.println("GO Term Assignments: " + gotermAssignments);
		System.out.println("GO Term Citations: " + gotermCitations);
		return new CountedFrames();
	}
}
