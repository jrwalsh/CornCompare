package edu.iastate.CornCompare;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import edu.iastate.javacyco.Frame;
import edu.iastate.javacyco.JavacycConnection;
import edu.iastate.javacyco.Network;
import edu.iastate.javacyco.PtoolsErrorException;

public class CopyOfReactionComparison {
	private static String ptoolsClass = "|Reactions|";
	private JavacycConnection conn;
	
	private HashSet<String> frameInstancesA = new HashSet<String>();
	private HashSet<String> frameClassesA = new HashSet<String>();
	private HashSet<String> frameInstancesB = new HashSet<String>();
	private HashSet<String> frameClassesB = new HashSet<String>();
	
	private ArrayList<ReactionItem> instancesA = new ArrayList<ReactionItem>();
	private ArrayList<ReactionItem> classesA = new ArrayList<ReactionItem>();
	private ArrayList<ReactionItem> instancesB = new ArrayList<ReactionItem>();
	private ArrayList<ReactionItem> classesB = new ArrayList<ReactionItem>();
	
	public CopyOfReactionComparison (String host, String organismA, String organismB, int port, String fileName, boolean verbose) {
		conn = new JavacycConnection(host, port);
		
		try {
			getFrames(organismA, organismB);
		} catch (PtoolsErrorException e) {
			System.err.println("Failed to load all the " + ptoolsClass + " objects... aborting");
			e.printStackTrace();
		}
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
}
