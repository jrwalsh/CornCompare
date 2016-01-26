package edu.iastate.CornCompare;

import java.util.ArrayList;
import java.util.Set;

import edu.iastate.javacyco.Frame;
import edu.iastate.javacyco.JavacycConnection;
import edu.iastate.javacyco.Network;
import edu.iastate.javacyco.PtoolsErrorException;

public class GeneComparison {
	private static String ptoolsClass = "|Genes|";
	private JavacycConnection conn;
	
	private ArrayList<GeneItem> listA = new ArrayList<GeneItem>();
	private ArrayList<GeneItem> listB = new ArrayList<GeneItem>();
//	private String host;
//	private String organismA;
//	private String organismB;
//	private int port;
	private String fileName;
	private boolean verbose;
	
	public GeneComparison (String host, String organismA, String organismB, int port, String fileName, boolean verbose) {
		conn = new JavacycConnection(host, port);
		this.fileName = fileName;
		this.verbose = verbose;
		
		try {
			conn.selectOrganism(organismA);
			listA = LoadObjects();
			conn.selectOrganism(organismB);
			listB = LoadObjects();
		} catch (PtoolsErrorException e) {
			System.err.println("Failed to load all the " + ptoolsClass + " objects... aborting");
			e.printStackTrace();
		}
	}
	
	public Comparison<GeneItem> compare() {
		ArrayList<GeneItem> matched = new ArrayList<GeneItem>();
		ArrayList<GeneItem> uniqueListA = listA;
		ArrayList<GeneItem> uniqueListB = listB;
		
		for (GeneItem item : listA) {
			if (listB.contains(item)) {
				matched.add(item);
			}
		}
		for (GeneItem item : listB) {
			if (listA.contains(item)) {
				matched.add(item);
			}
		}
		uniqueListA.removeAll(matched);
		uniqueListB.removeAll(matched);
		return null;
		
//		return new Comparison<GeneItem>(matched, uniqueListA, uniqueListB);
	}
	
	private ArrayList<GeneItem> LoadObjects() throws PtoolsErrorException {
		Network hierarchy = conn.getClassHierarchy(ptoolsClass, true, true);
		Set<Frame> nodes = hierarchy.getNodes();
		
		ArrayList<GeneItem> list = new ArrayList<GeneItem>();
		
		for (Frame node : nodes) {
			if (!node.isClassFrame()) list.add(new GeneItem(node.getLocalID(), node.getCommonName().replaceAll("_P..$|_T..$", "")));
		}
		
		return list;
	}
}
