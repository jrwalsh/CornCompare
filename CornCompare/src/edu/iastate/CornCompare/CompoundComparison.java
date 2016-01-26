package edu.iastate.CornCompare;

import java.util.ArrayList;
import java.util.Set;

import edu.iastate.javacyco.Frame;
import edu.iastate.javacyco.JavacycConnection;
import edu.iastate.javacyco.Network;
import edu.iastate.javacyco.PtoolsErrorException;

public class CompoundComparison {
	private static String ptoolsClass = "|Compounds|";
	private JavacycConnection conn;
	
	private ArrayList<CompoundItem> listA = new ArrayList<CompoundItem>();
	private ArrayList<CompoundItem> listB = new ArrayList<CompoundItem>();
	private String fileName;
	private boolean verbose;
	
	public CompoundComparison (String host, String organismA, String organismB, int port, String fileName, boolean verbose) {
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
	
	public Comparison<CompoundItem> compare() {
		ArrayList<CompoundItem> matched = new ArrayList<CompoundItem>();
		ArrayList<CompoundItem> uniqueListA = listA;
		ArrayList<CompoundItem> uniqueListB = listB;
		
		for (CompoundItem item : listA) {
			if (listB.contains(item)) {
				matched.add(item);
			}
		}
		for (CompoundItem item : listB) {
			if (listA.contains(item)) {
				matched.add(item);
			}
		}
		uniqueListA.removeAll(matched);
		uniqueListB.removeAll(matched);
		return null;
		
//		return new Comparison<CompoundItem>(matched, uniqueListA, uniqueListB);
	}
	
	private ArrayList<CompoundItem> LoadObjects() throws PtoolsErrorException {
		Network hierarchy = conn.getClassHierarchy(ptoolsClass, true, true);
		Set<Frame> nodes = hierarchy.getNodes();
		
		ArrayList<CompoundItem> list = new ArrayList<CompoundItem>();
		
		for (Frame node : nodes) {
			String comparableField = node.getSlotValue("InChI");
			if (comparableField == null || comparableField.length() == 0 || comparableField.equalsIgnoreCase("null")) comparableField = node.getCommonName();
			if (!node.isClassFrame()) list.add(new CompoundItem(node.getLocalID(), comparableField.replace("\"", "")));
		}
		
		return list;
	}
}
