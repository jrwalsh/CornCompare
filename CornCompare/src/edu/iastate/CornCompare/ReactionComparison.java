package edu.iastate.CornCompare;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import edu.iastate.javacyco.Frame;
import edu.iastate.javacyco.JavacycConnection;
import edu.iastate.javacyco.Network;
import edu.iastate.javacyco.PtoolsErrorException;
 
/**
  * This object controls the comparison of reactions between the two provided pathway tools organisms.  We also output EC numbers in order to compare them externally.
  * 
  * @author Jesse R Walsh
  * @date 2/10/2016
  */
public class ReactionComparison {
	private final String ptoolsClass = "|Reactions|";
	private JavacycConnection conn;
	private boolean verbose;
	private String organismA;
	private String organismB;
	private File logFile;
	private String destinationFolder;
	
	private FrameList<ReactionItem> framesA;
	private FrameList<ReactionItem> framesB;
	
	public ReactionComparison (String host, String organismA, String organismB, int port, String outputDir, boolean verbose) {
		conn = new JavacycConnection(host, port);
		this.destinationFolder = outputDir + System.getProperty("file.separator") + "Reactions";
		this.logFile = new File(destinationFolder, "Reaction_log.txt");
		this.verbose = verbose;
		this.organismA = organismA;
		this.organismB = organismB;
		this.framesA = new FrameList<ReactionItem>();
		this.framesB = new FrameList<ReactionItem>();
		
		try {
			loadItems(organismA, framesA);
			loadItems(organismB, framesB);
		} catch (PtoolsErrorException e) {
			System.err.println("Failed to load all the " + ptoolsClass + " objects... aborting");
			e.printStackTrace();
		}
	}
	
	/**
	 * Load in the frames of this data type from pathway tools organism.  Start with the raw frame list, then separate the frames into class frames and instances.
	 * @throws PtoolsErrorException
	 */
	private void loadItems(String organism, FrameList<ReactionItem> frameList) throws PtoolsErrorException {
		if (verbose) {
			System.out.print("Loading all " + ptoolsClass + " objects from the " + organism + " database...");
			appendLine(logFile, "Loading all " + ptoolsClass + " objects from the " + organism + " database...");
		}
		
		conn.selectOrganism(organism);
		Network hierarchy = conn.getClassHierarchy(ptoolsClass, true, true);
		frameList.rawList = hierarchy.getNodes();
		
		if (verbose) {
			System.out.print("loaded " + frameList.rawList.size() + " " + ptoolsClass + " objects.\nSorting objects...");
			appendLine(logFile, "loaded " + frameList.rawList.size() + " " + ptoolsClass + " objects.\nSorting objects...");
		}
		
		for (Frame node : frameList.rawList) {
			String EC = node.getSlotValue("EC-NUMBER");
			if (EC == null) EC = "";
			
			if (node.isClassFrame()) frameList.classList.add(new ReactionItem(node.getLocalID(), node.getLocalID(), node.getCommonName(), EC));
			else frameList.instanceList.add(new ReactionItem(node.getLocalID(), node.getLocalID(), node.getCommonName(), EC));
		}
		
		if (verbose) {
			System.out.println("Sorted " + frameList.classList.size() + " classes and " + frameList.instanceList.size() + " instances of type " + this.ptoolsClass + " for " + organism);
			appendLine(logFile, "Sorted " + frameList.classList.size() + " classes and " + frameList.instanceList.size() + " instances of type " + this.ptoolsClass + " for " + organism+"\n");
		}
	}
	
	/**
	 * Perform a data pre-processing step prior to comparison.  For reactions, we check for duplicate names.
	 * 
	 * @return
	 * @throws PtoolsErrorException
	 */
	private Set<ReactionItem> preProcess(String organism, FrameList<ReactionItem> frameList) throws PtoolsErrorException {
		if (verbose) {
			System.out.println("Performing preprocessing steps on " + ptoolsClass + " for " + organism);
			appendLine(logFile, "Performing preprocessing steps on " + ptoolsClass + " for " + organism+"\n");
		}
		
		conn.selectOrganism(organism);
		
		Set<ReactionItem> tempRemoveDuplicateSet = new HashSet<ReactionItem>();
		
		// First remove any item with a duplicate common name.  Since we are using frameIDs, there should be no duplicates (they are already unique keys).
		int countDuplicateNames = 0;
		for (ReactionItem item : frameList.instanceList) {
			if (!tempRemoveDuplicateSet.add(item) && verbose) {
				appendLine(logFile, "Removing \"" + item.frameID + " - " + item.comparableField + "\" from set: duplicate common name"+"\n");
				countDuplicateNames++;
			}
		}
		if (verbose) {
			System.out.println("Removed a total of " + countDuplicateNames + " objects due to duplicate common names");
			appendLine(logFile, "Removed a total of " + countDuplicateNames + " objects due to duplicate common names"+"\n");
		}
		
		Set<ReactionItem> processedList = new HashSet<ReactionItem>();
		processedList.addAll(tempRemoveDuplicateSet);
		
		// Print the sorted and processed lists
		printSet(new File(this.destinationFolder, "Reaction_Classes_"+organism+".tab"), "ClassFrames", frameList.classList);
		printSet(new File(this.destinationFolder, "Reaction_Instances_"+organism+".tab"), "InstanceFrames", frameList.instanceList);
		printSet(new File(this.destinationFolder, "Reaction_Filtered_"+organism+".tab"), "Filtered InstanceFrames", processedList);
		
		return processedList;
	}
	
	public void compare() {
		Set<ReactionItem> matched = new HashSet<ReactionItem>();
		Set<ReactionItem> uniqueListA = new HashSet<ReactionItem>();
		Set<ReactionItem> uniqueListB = new HashSet<ReactionItem>();
		HashMap<ReactionItem,ReactionItem> setB = new HashMap<ReactionItem,ReactionItem>(); // Needed a get method for uniqueList.  Quick solution: make a matching HashMap.
		
		try {
			uniqueListA = preProcess(organismA, framesA);
			uniqueListB = preProcess(organismB, framesB);
			for (ReactionItem item : uniqueListB) {
				setB.put(item, item); 
			}
		} catch (PtoolsErrorException e) {
			e.printStackTrace();
		}
		
		String matchSetOutput = organismA + "\t\t" + organismB + "\t\n";
		for (ReactionItem item : uniqueListA) {
			if (setB.containsValue(item)) {
				if (!item.commonName.equalsIgnoreCase(setB.get(item).commonName)) {
					System.err.println("Warning! No match in common names for " + item.frameID + " : " + item.commonName + " != " + setB.get(item).commonName);
					appendLine(logFile, "Warning! No match in common names for " + item.frameID + " : " + item.commonName + " != " + setB.get(item).commonName+"\n");
				}
				
				if (!item.EC.equalsIgnoreCase(setB.get(item).EC)) {
					System.err.println("Warning! No match in EC for " + item.frameID + " : " + item.EC + " != " + setB.get(item).EC);
					appendLine(logFile, "Warning! No match in EC for " + item.frameID + " : " + item.EC + " != " + setB.get(item).EC+"\n");
				}
				
				matched.add(item);
				matchSetOutput = matchSetOutput + item.frameID + "\t" + item.comparableField + "\t" + item.EC + "\t" + setB.get(item).frameID + "\t" + setB.get(item).comparableField + "\t" + setB.get(item).EC + "\n";
			}
		}
		uniqueListA.removeAll(matched);
		uniqueListB.removeAll(matched);
		
		if (verbose) {
			System.out.println("Matching Frames: " + matched.size());
			System.out.println("Frames in A: " + uniqueListA.size());
			System.out.println("Frames in B: " + uniqueListB.size());
			appendLine(logFile, "Matching Frames: " + matched.size()+"\n");
			appendLine(logFile, "Frames in A: " + uniqueListA.size()+"\n");
			appendLine(logFile, "Frames in B: " + uniqueListB.size()+"\n");
		}
		
		// Print matching results
		printString(new File(this.destinationFolder, "Reaction_Matching_"+organismA+"_vs_"+organismB+".tab"), matchSetOutput);
		printSet(new File(this.destinationFolder, "Reaction_Unique_"+organismA+".tab"), "Unique InstanceFrames", uniqueListA);
		printSet(new File(this.destinationFolder, "Reaction_Unique_"+organismB+".tab"), "Unique InstanceFrames", uniqueListB);
	}
	
	/**
	 * Simple function to print a string to the specified file location.
	 * 
	 * @param fileName
	 * @param printString
	 */
	protected void printString(File file, String printString) {
		PrintStream o = null;
		try {
			o = new PrintStream(file);
			o.println(printString);
			o.close();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	/**
	 * Simple function to append a string to the specified file location.
	 * 
	 * @param file
	 * @param printString
	 */
	protected void appendLine(File file, String printString) {
		PrintStream o = null;
		try {
			o = new PrintStream(new FileOutputStream(file, true));
			o.append(printString);
			o.close();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	protected void printSet(File file, String columnName, Set<ReactionItem> set) {
		ArrayList<ReactionItem> list = new ArrayList<ReactionItem>();
		list.addAll(set);
		printSet(file, columnName, list);
	}
	
	protected void printSet(File file, String columnName, ArrayList<ReactionItem> set) {
		PrintStream o = null;
		try {
			o = new PrintStream(file);
			o.println(columnName);
			for (ReactionItem item : set) {
				o.println(item.frameID + "\t" + item.comparableField + "\t" + item.EC);
			}
			o.close();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
