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
import edu.iastate.javacyco.Reaction;

public class ReactionComparison {
	private static String ptoolsClass = "|Reactions|";
	private JavacycConnection conn;
	private String organismA;
	private String organismB;
	private boolean verbose;
	
	private HashSet<String> frameInstancesA = new HashSet<String>();
	private HashSet<String> frameClassesA = new HashSet<String>();
	private HashSet<String> frameInstancesB = new HashSet<String>();
	private HashSet<String> frameClassesB = new HashSet<String>();
	
	private ArrayList<ReactionItem> instancesA = new ArrayList<ReactionItem>();
	private ArrayList<ReactionItem> classesA = new ArrayList<ReactionItem>();
	private ArrayList<ReactionItem> instancesB = new ArrayList<ReactionItem>();
	private ArrayList<ReactionItem> classesB = new ArrayList<ReactionItem>();
	
	public ReactionComparison (String host, String organismA, String organismB, int port, String fileName, boolean verbose) {
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
						Reaction tempA = (Reaction) Reaction.load(conn, item);//	frameInstancesB.get(frameInstancesB.indexOf(item));
						
						conn.selectOrganism(organismB);
						Reaction tempB = (Reaction) Reaction.load(conn, item);
						
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
					Reaction temp = (Reaction) Reaction.load(conn, item);
					printString = "FrameID unique to A " + "\t" + temp.getLocalID() + "\t" + temp.getCommonName() + "\t" + temp.getEC() + "\t" + temp.isGFPClass("|Transport-Reactions|");
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
					Reaction temp = (Reaction) Reaction.load(conn, item);
					printString = "FrameID unique to B " + "\t" + temp.getLocalID() + "\t" + temp.getCommonName() + "\t" + temp.getEC() + "\t" + temp.isGFPClass("|Transport-Reactions|");
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
	
	public Comparison<ReactionItem> compareByData() {
		ArrayList<ReactionItem> matchedClasses = new ArrayList<ReactionItem>();
		matchedClasses.addAll(classesA);
		matchedClasses.retainAll(classesB);
		
		ArrayList<ReactionItem> uniqueClassesA = new ArrayList<ReactionItem>();
		uniqueClassesA.addAll(classesA);
		uniqueClassesA.removeAll(classesB);
		
		ArrayList<ReactionItem> uniqueClassesB = new ArrayList<ReactionItem>();
		uniqueClassesB.addAll(classesB);
		uniqueClassesB.removeAll(classesA);
		
		ArrayList<ReactionItem> matchedInstances = new ArrayList<ReactionItem>();
		matchedInstances.addAll(instancesA);
		matchedInstances.retainAll(instancesB);
		
		ArrayList<ReactionItem> uniqueInstancesA = new ArrayList<ReactionItem>();
		uniqueInstancesA.addAll(instancesA);
		uniqueInstancesA.removeAll(instancesB);
		
		ArrayList<ReactionItem> uniqueInstancesB = new ArrayList<ReactionItem>();
		uniqueInstancesB.addAll(instancesB);
		uniqueInstancesB.removeAll(instancesA);
		
		
		if (verbose) {
			for (ReactionItem item : instancesA) {
				if (instancesB.contains(item)) {
					ReactionItem temp = instancesB.get(instancesB.indexOf(item));
					String printString = "Match " + "A" + " to " + "B" + "\t" + item.frameID + "\t" + item.comparableData + "\t" + temp.frameID + "\t" + temp.comparableData;
					try {
						writeToFile(printString, "dataCompare.tab", true);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
//					System.out.println("Match " + "A" + " to " + "B" + "\t" + item.frameID + "\t" + item.comparableData + "\t" + temp.frameID + "\t" + temp.comparableData);
				}
			}
		}
		
		return new Comparison<ReactionItem>(matchedClasses, uniqueClassesA, uniqueClassesB, matchedInstances, uniqueInstancesA, uniqueInstancesB);
	}
	
//	public Comparison<ReactionItem> compareByData() {
//		ArrayList<ReactionItem> matched = new ArrayList<ReactionItem>();
//		ArrayList<ReactionItem> uniqueListA = instancesA;
//		ArrayList<ReactionItem> uniqueListB = instancesB;
//		
//		for (ReactionItem item : instancesA) {
//			if (instancesB.contains(item)) {
//				matched.add(item);
//			}
//		}
//		for (ReactionItem item : instancesB) {
//			if (instancesA.contains(item)) {
//				matched.add(item);
//			}
//		}
//		uniqueListA.removeAll(matched);
//		uniqueListB.removeAll(matched);
//		
//		return new Comparison<ReactionItem>(matched, uniqueListA, uniqueListB);
//	}
	
	private void getFrames(String organismA, String organismB) throws PtoolsErrorException {
		conn.selectOrganism(organismA);
		Network hierarchyA = conn.getClassHierarchy(ptoolsClass, true, true);
		Set<Frame> nodesA = hierarchyA.getNodes();
		
		for (Frame node : nodesA) {
			if (!node.isClassFrame()) {
				frameInstancesA.add(node.getLocalID());
				String comparableField = node.getCommonName();
				if (!instancesA.contains(comparableField)) {
					instancesA.add(new ReactionItem(node.getLocalID(), node.getCommonName()));
				} else {
					instancesA.get(instancesA.indexOf(comparableField)).addFrameID(node.getLocalID());
				}
			} else {
				frameClassesA.add(node.getLocalID());
				String comparableField = node.getCommonName();
				if (!classesA.contains(comparableField)) {
					classesA.add(new ReactionItem(node.getLocalID(), node.getCommonName()));
				} else {
					classesA.get(classesA.indexOf(comparableField)).addFrameID(node.getLocalID());
				}
			}
		}
		
		conn.selectOrganism(organismB);
		Network hierarchyB = conn.getClassHierarchy(ptoolsClass, true, true);
		Set<Frame> nodesB = hierarchyB.getNodes();
		
		for (Frame node : nodesB) {
			if (!node.isClassFrame()) {
				frameInstancesB.add(node.getLocalID());
				String comparableField = node.getCommonName();
				if (!instancesB.contains(comparableField)) {
					instancesB.add(new ReactionItem(node.getLocalID(), node.getCommonName()));
				} else {
					instancesB.get(instancesB.indexOf(comparableField)).addFrameID(node.getLocalID());
				}
			} else {
				frameClassesB.add(node.getLocalID());
				String comparableField = node.getCommonName();
				if (!classesB.contains(comparableField)) {
					classesB.add(new ReactionItem(node.getLocalID(), node.getCommonName()));
				} else {
					classesB.get(classesB.indexOf(comparableField)).addFrameID(node.getLocalID());
				}
			}
		}
	}
	
	public void writeToFile(String printString, String fileName, boolean append) throws IOException {
		PrintWriter pw = new PrintWriter(new FileWriter(fileName, append));
//		pw.write(printString);
		pw.println(printString);
		pw.close();
	}
}
