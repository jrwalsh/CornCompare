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
  * This object controls the comparison of pathways between the two provided pathway tools organisms.  For pathways, we must be sure to remove any super pathways
  * from the list in order to avoid duplication of pathway data.  Super pathways are a set of normal pathways grouped together, representing an arbitrary understanding
  * of how parts of the network function together.  The Pathway Tools manual specifies a set of guidelines which are used by curators when deciding what pathway 
  * boundaries are appropriate and what pathways should be joined into a super pathway.
  * 
  * We expect most of the pathway data in CornCyc and MaizeCyc to have been automatically pulled from MetaCyc as needed during the pathway inference step of Pathologic.
  * 
  * @author Jesse R Walsh
  *
  */
public class PathwayComparison {
	private final String ptoolsClass = "|Pathways|";
	private final String ptoolsFilterClass = "|Super-Pathways|";
	private JavacycConnection conn;
	private boolean verbose;
	private String organismA;
	private String organismB;
	private File logFile;
	private String destinationFolder;
	
	private FrameList<PathwayItem> framesA;
	private FrameList<PathwayItem> framesB;
	
	public PathwayComparison (String host, String organismA, String organismB, int port, String outputDir, boolean verbose) {
		conn = new JavacycConnection(host, port);
		this.destinationFolder = outputDir + System.getProperty("file.separator") + "Pathways";
		this.logFile = new File(destinationFolder, "Pathway_log.txt");
		this.verbose = verbose;
		this.organismA = organismA;
		this.organismB = organismB;
		this.framesA = new FrameList<PathwayItem>();
		this.framesB = new FrameList<PathwayItem>();
		
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
	private void loadItems(String organism, FrameList<PathwayItem> frameList) throws PtoolsErrorException {
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
			if (node.isClassFrame()) frameList.classList.add(new PathwayItem(node.getLocalID(), node.getLocalID(), node.getCommonName()));
			else frameList.instanceList.add(new PathwayItem(node.getLocalID(), node.getLocalID(), node.getCommonName()));
		}
		
		if (verbose) {
			System.out.println("Sorted " + frameList.classList.size() + " classes and " + frameList.instanceList.size() + " instances of type " + this.ptoolsClass + " for " + organism);
			appendLine(logFile, "Sorted " + frameList.classList.size() + " classes and " + frameList.instanceList.size() + " instances of type " + this.ptoolsClass + " for " + organism+"\n");
		}
	}
	
	/**
	 * Perform a data pre-processing step prior to comparison.  For pathways, we check for duplicate names and remove super-pathways.
	 * 
	 * @return
	 * @throws PtoolsErrorException
	 */
	private Set<PathwayItem> preProcess(String organism, FrameList<PathwayItem> frameList) throws PtoolsErrorException {
		if (verbose) {
			System.out.println("Performing preprocessing steps on " + ptoolsClass + " for " + organism);
			appendLine(logFile, "Performing preprocessing steps on " + ptoolsClass + " for " + organism+"\n");
		}
		
		conn.selectOrganism(organism);
		
		Set<PathwayItem> tempRemoveDuplicateSet = new HashSet<PathwayItem>();
		
		// First remove any item with a duplicate common name.  Since we are using frameIDs, there should be no duplicates (they are already unique keys).
		int countDuplicateNames = 0;
		for (PathwayItem item : frameList.instanceList) {
			if (!tempRemoveDuplicateSet.add(item) && verbose) {
				appendLine(logFile, "Removing \"" + item.frameID + " - " + item.comparableField + "\" from set: duplicate common name"+"\n");
				countDuplicateNames++;
			}
		}
		if (verbose) {
			System.out.println("Removed a total of " + countDuplicateNames + " objects due to duplicate common names");
			appendLine(logFile, "Removed a total of " + countDuplicateNames + " objects due to duplicate common names"+"\n");
		}
		
		// Remove all super-pathways from this list
		int countSuperPathways = 0;
		Set<PathwayItem> processedList = new HashSet<PathwayItem>();
		for (PathwayItem item : tempRemoveDuplicateSet) {
			Frame itemFrame = Frame.load(conn, item.frameID);
			if (itemFrame.isGFPClass(ptoolsFilterClass)) {
				if (verbose) {
					System.out.println("Removing super pathway " + item.frameID);
					appendLine(logFile, "Removing super pathway " + item.frameID + "\n");
				}
				countSuperPathways++;
			} else {
				processedList.add(item);
			}
		}
		if (verbose) {
			System.out.println("Removed a total of " + countSuperPathways + " super pathways");
			appendLine(logFile, "Removed a total of " + countSuperPathways + " super pathways"+"\n");
		}
		
		// Print the sorted and processed lists
		printSet(new File(this.destinationFolder, "Pathway_Classes_"+organism+".tab"), "ClassFrames", frameList.classList);
		printSet(new File(this.destinationFolder, "Pathway_Instances_"+organism+".tab"), "InstanceFrames", frameList.instanceList);
		printSet(new File(this.destinationFolder, "Pathway_Filtered_"+organism+".tab"), "Filtered InstanceFrames", processedList);
		
		return processedList;
	}
	
	public void compare() {
		Set<PathwayItem> matched = new HashSet<PathwayItem>();
		Set<PathwayItem> uniqueListA = new HashSet<PathwayItem>();
		Set<PathwayItem> uniqueListB = new HashSet<PathwayItem>();
		HashMap<PathwayItem,PathwayItem> setB = new HashMap<PathwayItem,PathwayItem>(); // Needed a get method for uniqueList.  Quick solution: make a matching HashMap.
		
		try {
			uniqueListA = preProcess(organismA, framesA);
			uniqueListB = preProcess(organismB, framesB);
			for (PathwayItem item : uniqueListB) {
				setB.put(item, item); 
			}
		} catch (PtoolsErrorException e) {
			e.printStackTrace();
		}
		
		String matchSetOutput = organismA + "\t\t\t" + organismB + "\t\n";
		for (PathwayItem item : uniqueListA) {
			if (setB.containsValue(item)) {
				matched.add(item);
				matchSetOutput = matchSetOutput + item.frameID + "\t" + item.comparableField + "\t" + item.commonName + "\t" + setB.get(item).frameID + "\t" + setB.get(item).comparableField + "\t" + setB.get(item).commonName + "\n";
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
		printString(new File(this.destinationFolder, "Pathway_Matching_"+organismA+"_vs_"+organismB+".tab"), matchSetOutput);
		printSet(new File(this.destinationFolder, "Pathway_Unique_"+organismA+".tab"), "Unique InstanceFrames", uniqueListA);
		printSet(new File(this.destinationFolder, "Pathway_Unique_"+organismB+".tab"), "Unique InstanceFrames", uniqueListB);
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
	
	protected void printSet(File file, String columnName, Set<PathwayItem> set) {
		ArrayList<PathwayItem> list = new ArrayList<PathwayItem>();
		list.addAll(set);
		printSet(file, columnName, list);
	}
	
	protected void printSet(File file, String columnName, ArrayList<PathwayItem> set) {
		PrintStream o = null;
		try {
			o = new PrintStream(file);
			o.println(columnName);
			for (PathwayItem item : set) {
				o.println(item.frameID + "\t" + item.comparableField + "\t" + item.commonName);
			}
			o.close();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
