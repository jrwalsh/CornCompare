package edu.iastate.CornCompare;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import edu.iastate.javacyco.Frame;
import edu.iastate.javacyco.JavacycConnection;
import edu.iastate.javacyco.Network;
import edu.iastate.javacyco.PtoolsErrorException;
import edu.iastate.javacyco.Pathway;

public class PathwayComparison {
	private static String ptoolsClass = "|Pathways|";
	private JavacycConnection conn;
	
	private ArrayList<PathwayItem> listA = new ArrayList<PathwayItem>();
	private ArrayList<PathwayItem> listB = new ArrayList<PathwayItem>();
	private String fileName;
	private boolean verbose;
	
	private String organismA;
	private String organismB;
	
	private HashSet<String> frameInstancesA = new HashSet<String>();
	private HashSet<String> frameClassesA = new HashSet<String>();
	private HashSet<String> frameInstancesB = new HashSet<String>();
	private HashSet<String> frameClassesB = new HashSet<String>();
	
	private ArrayList<PathwayItem> instancesA = new ArrayList<PathwayItem>();
	private ArrayList<PathwayItem> classesA = new ArrayList<PathwayItem>();
	private ArrayList<PathwayItem> instancesB = new ArrayList<PathwayItem>();
	private ArrayList<PathwayItem> classesB = new ArrayList<PathwayItem>();
	
	public PathwayComparison (String host, String organismA, String organismB, int port, String fileName, boolean verbose) {
		conn = new JavacycConnection(host, port);
		
		try {
			getFrames(organismA, organismB);
		} catch (PtoolsErrorException e) {
			System.err.println("Failed to load all the " + ptoolsClass + " objects... aborting");
			e.printStackTrace();
		}
		
		this.organismA = organismA;
		this.organismB = organismB;
		this.verbose = verbose;
	}
	
	public Comparison<PathwayItem> compare() {
		ArrayList<PathwayItem> matched = new ArrayList<PathwayItem>();
		ArrayList<PathwayItem> uniqueListA = listA;
		ArrayList<PathwayItem> uniqueListB = listB;
		
		for (PathwayItem item : listA) {
			if (listB.contains(item)) {
				matched.add(item);
			}
		}
		for (PathwayItem item : listB) {
			if (listA.contains(item)) {
				matched.add(item);
			}
		}
		uniqueListA.removeAll(matched);
		uniqueListB.removeAll(matched);
		return null;
		
//		return new Comparison<PathwayItem>(matched, uniqueListA, uniqueListB);
	}
	
	public Comparison<String> compareByFrameID() {
		ArrayList<String> matchedClasses = new ArrayList<String>();
		matchedClasses.addAll(frameClassesA);
		matchedClasses.retainAll(frameClassesB);
		
		ArrayList<String> uniqueClassesA = new ArrayList<String>();
		uniqueClassesA.addAll(frameClassesA);
		uniqueClassesA.removeAll(frameClassesB);
		
		ArrayList<String> uniqueClassesB = new ArrayList<String>();
		uniqueClassesB.addAll(frameClassesB);
		uniqueClassesB.removeAll(frameClassesA);
		
		ArrayList<String> matchedInstances = new ArrayList<String>();
		matchedInstances.addAll(frameInstancesA);
		matchedInstances.retainAll(frameInstancesB);
		
		ArrayList<String> uniqueInstancesA = new ArrayList<String>();
		uniqueInstancesA.addAll(frameInstancesA);
		uniqueInstancesA.removeAll(frameInstancesB);
		
		ArrayList<String> uniqueInstancesB = new ArrayList<String>();
		uniqueInstancesB.addAll(frameInstancesB);
		uniqueInstancesB.removeAll(frameInstancesA);
		
		if (verbose) {
			for (String item : frameInstancesA) {
				if (frameInstancesB.contains(item)) {
					String printString = "";
					try {
						conn.selectOrganism(organismA);
						Pathway tempA = (Pathway) Pathway.load(conn, item);//	frameInstancesB.get(frameInstancesB.indexOf(item));
						
						conn.selectOrganism(organismB);
						Pathway tempB = (Pathway) Pathway.load(conn, item);
						
						printString = "FrameID match " + "A" + " to " + "B" + "\t" + tempA.getLocalID() + "\t" + tempA.getCommonName() + "\t" + tempB.getLocalID() + "\t" + tempB.getCommonName();
					} catch (PtoolsErrorException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} 
					try {
						writeToFile(printString, "frameCompare.tab", true);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
//					System.out.println("Match " + "A" + " to " + "B" + "\t" + item.frameID + "\t" + item.comparableData + "\t" + temp.frameID + "\t" + temp.comparableData);
				}
			}
			
			conn.selectOrganism(organismA);
			for (String item : uniqueInstancesA) {
				String printString = "";
				try {
					Pathway temp = (Pathway) Pathway.load(conn, item);
					printString = "FrameID unique to A " + "\t" + temp.getLocalID() + "\t" + temp.getCommonName();
				} catch (PtoolsErrorException e1) {
					e1.printStackTrace();
				} 
				try {
					writeToFile(printString, "frameUniqueA.tab", true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			conn.selectOrganism(organismB);
			for (String item : uniqueInstancesB) {
				String printString = "";
				try {
					Pathway temp = (Pathway) Pathway.load(conn, item);
					printString = "FrameID unique to B " + "\t" + temp.getLocalID() + "\t" + temp.getCommonName();
				} catch (PtoolsErrorException e1) {
					e1.printStackTrace();
				} 
				try {
					writeToFile(printString, "frameUniqueB.tab", true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		return new Comparison<String>(matchedClasses, uniqueClassesA, uniqueClassesB, matchedInstances, uniqueInstancesA, uniqueInstancesB);
	}
	
	// Ignores superpathways, removing them from list
	private void getFrames(String organismA, String organismB) throws PtoolsErrorException {
		conn.selectOrganism(organismA);
		Network hierarchyA = conn.getClassHierarchy(ptoolsClass, true, true);
		Set<Frame> nodesA = hierarchyA.getNodes();
		
		for (Frame node : nodesA) {
			if (node.isGFPClass("|Super-Pathways|")) {
				if (verbose) System.out.println("Skipping superpathway : " + node.getLocalID());
			}
			else if (!node.isClassFrame()) {
				frameInstancesA.add(node.getLocalID());
				String comparableField = node.getCommonName();
				if (!instancesA.contains(comparableField)) {
					instancesA.add(new PathwayItem(node.getLocalID(), node.getCommonName()));
				} else {
					instancesA.get(instancesA.indexOf(comparableField)).addFrameID(node.getLocalID());
				}
			} else {
				frameClassesA.add(node.getLocalID());
				String comparableField = node.getCommonName();
				if (!classesA.contains(comparableField)) {
					classesA.add(new PathwayItem(node.getLocalID(), node.getCommonName()));
				} else {
					classesA.get(classesA.indexOf(comparableField)).addFrameID(node.getLocalID());
				}
			}
		}
		
		conn.selectOrganism(organismB);
		Network hierarchyB = conn.getClassHierarchy(ptoolsClass, true, true);
		Set<Frame> nodesB = hierarchyB.getNodes();
		
		for (Frame node : nodesB) {
			if (node.isGFPClass("|Super-Pathways|")) {
				if (verbose) System.out.println("Skipping superpathway : " + node.getLocalID());
			}
			else if (!node.isClassFrame()) {
				frameInstancesB.add(node.getLocalID());
				String comparableField = node.getCommonName();
				if (!instancesB.contains(comparableField)) {
					instancesB.add(new PathwayItem(node.getLocalID(), node.getCommonName()));
				} else {
					instancesB.get(instancesB.indexOf(comparableField)).addFrameID(node.getLocalID());
				}
			} else {
				frameClassesB.add(node.getLocalID());
				String comparableField = node.getCommonName();
				if (!classesB.contains(comparableField)) {
					classesB.add(new PathwayItem(node.getLocalID(), node.getCommonName()));
				} else {
					classesB.get(classesB.indexOf(comparableField)).addFrameID(node.getLocalID());
				}
			}
		}
	}
	
	private ArrayList<PathwayItem> LoadObjects() throws PtoolsErrorException {
		Network hierarchy = conn.getClassHierarchy(ptoolsClass, true, true);
		Set<Frame> nodes = hierarchy.getNodes();
		
		ArrayList<PathwayItem> list = new ArrayList<PathwayItem>();
		
		for (Frame node : nodes) {
			if (!node.isClassFrame()) list.add(new PathwayItem(node.getLocalID(), node.getCommonName()));
		}
		
		return list;
	}
	
	public void writeToFile(String printString, String fileName, boolean append) throws IOException {
		PrintWriter pw = new PrintWriter(new FileWriter(fileName, append));
//		pw.write(printString);
		pw.println(printString);
		pw.close();
	}
}
