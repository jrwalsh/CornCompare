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
 * This object controls the comparison of compounds between the two provided pathway tools organisms.  While we should be able to compare compounds by their InChI
 * string values, inconsistencies in the implementation of the InChI's between CornCyc and MaizeCyc cause this to be unreliable.  Therefore, we match based on frameIDs
 * and simply warn when the CommonName or InChI does not match.
 * 
 * @author Jesse
 * @date 2/10/2016
 */
public class CompoundComparison {
	private final String ptoolsClass = "|Compounds|";
	private JavacycConnection conn;
	private boolean verbose;
	private String organismA;
	private String organismB;
	private File logFile;
	private String destinationFolder;
	
	private FrameList<CompoundItem> framesA;
	private FrameList<CompoundItem> framesB;
	
	public CompoundComparison (String host, String organismA, String organismB, int port, String outputDir, boolean verbose) {
		conn = new JavacycConnection(host, port);
		this.destinationFolder = outputDir + System.getProperty("file.separator") + "Compounds";
		this.logFile = new File(destinationFolder, "Compound_log.txt");
		this.verbose = verbose;
		this.organismA = organismA;
		this.organismB = organismB;
		this.framesA = new FrameList<CompoundItem>();
		this.framesB = new FrameList<CompoundItem>();
		
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
	private void loadItems(String organism, FrameList<CompoundItem> frameList) throws PtoolsErrorException {
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
			String inchi = node.getSlotValue("InChI");
			if (inchi == null) inchi = "";
			if (node.isClassFrame()) frameList.classList.add(new CompoundItem(node.getLocalID(), node.getLocalID(), node.getCommonName(), inchi));
			else frameList.instanceList.add(new CompoundItem(node.getLocalID(), node.getLocalID(), node.getCommonName(), inchi));
		}
		
		if (verbose) {
			System.out.println("Sorted " + frameList.classList.size() + " classes and " + frameList.instanceList.size() + " instances of type " + this.ptoolsClass + " for " + organism);
			appendLine(logFile, "Sorted " + frameList.classList.size() + " classes and " + frameList.instanceList.size() + " instances of type " + this.ptoolsClass + " for " + organism+"\n");
		}
	}
	
	/**
	 * Perform a data pre-processing step prior to comparison.  For Compound data we don't expect any preprocessing changes to data, but check for duplicate names as
	 * a sanity check.
	 * 
	 * @return
	 * @throws PtoolsErrorException
	 */
	private Set<CompoundItem> preProcess(String organism, FrameList<CompoundItem> frameList) throws PtoolsErrorException {
		if (verbose) {
			System.out.println("Performing preprocessing steps on " + ptoolsClass + " for " + organism + "...");
			appendLine(logFile, "Performing preprocessing steps on " + ptoolsClass + " for " + organism + "..."+"\n");
		}
		
		conn.selectOrganism(organism);
		
		Set<CompoundItem> tempRemoveDuplicateSet = new HashSet<CompoundItem>();
		
		// First remove any compounds with a duplicate common name.  Since we are matching on common names, duplicates will complicate the matching process.
		// Note that we do not expect any duplicate compound names.
		int countDuplicateNames = 0;
		for (CompoundItem item : frameList.instanceList) {
			if (!tempRemoveDuplicateSet.add(item) && verbose) {
//				System.out.println("Removing \"" + item.frameID + " - " + item.comparableField + "\" from set: duplicate common name");
				appendLine(logFile, "Removing \"" + item.frameID + " - " + item.comparableField + "\" from set: duplicate common name"+"\n");
				countDuplicateNames++;
			}
		}
		if (verbose) {
			System.out.println("Removed a total of " + countDuplicateNames + " objects due to duplicate common names");
			appendLine(logFile, "Removed a total of " + countDuplicateNames + " objects due to duplicate common names"+"\n");
		}
		
		Set<CompoundItem> processedList = new HashSet<CompoundItem>();
		processedList.addAll(tempRemoveDuplicateSet);
		
		// Print the sorted and processed lists
		printSet(new File(this.destinationFolder, "Compound_Classes_"+organism+".tab"), "ClassFrames", frameList.classList);
		printSet(new File(this.destinationFolder, "Compound_Instances_"+organism+".tab"), "InstanceFrames", frameList.instanceList);
		printSet(new File(this.destinationFolder, "Compound_Filtered_"+organism+".tab"), "Filtered InstanceFrames", processedList);
		
		return processedList;
	}
	
	public void compare() {
		Set<CompoundItem> matched = new HashSet<CompoundItem>();
		Set<CompoundItem> uniqueListA = new HashSet<CompoundItem>();
		Set<CompoundItem> uniqueListB = new HashSet<CompoundItem>();
		HashMap<CompoundItem,CompoundItem> setB = new HashMap<CompoundItem,CompoundItem>(); // Needed a get method for uniqueList.  Quick solution: make a matching HashMap.
		
		try {
			uniqueListA = preProcess(organismA, framesA);
			uniqueListB = preProcess(organismB, framesB);
			for (CompoundItem item : uniqueListB) {
				setB.put(item, item); 
			}
		} catch (PtoolsErrorException e) {
			e.printStackTrace();
		}
		
		String matchSetOutput = organismA + "\t" + organismB + "\n";
		for (CompoundItem item : uniqueListA) {
			if (setB.containsValue(item)) {
				
				if (!item.commonName.equalsIgnoreCase(setB.get(item).commonName)) {
					System.err.println("Warning! No match in common names for " + item.frameID + " : " + item.commonName + " != " + setB.get(item).commonName);
					appendLine(logFile, "Warning! No match in common names for " + item.frameID + " : " + item.commonName + " != " + setB.get(item).commonName+"\n");
				}
				
				if (!item.inchi.equalsIgnoreCase(setB.get(item).inchi)) {
					System.err.println("Warning! No match in inchi for " + item.frameID + " : " + item.inchi + " != " + setB.get(item).inchi);
					appendLine(logFile, "Warning! No match in inchi for " + item.frameID + " : " + item.inchi + " != " + setB.get(item).inchi+"\n");
				}
				
				
				matched.add(item);
				matchSetOutput = matchSetOutput + item.frameID + "\t" + item.comparableField + "\t" + setB.get(item).frameID + "\t" + setB.get(item).comparableField + "\n";
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
		printString(new File(this.destinationFolder, "Compound_Matching_"+organismA+"_vs_"+organismB+".tab"), matchSetOutput);
		printSet(new File(this.destinationFolder, "Compound_Unique_"+organismA+".tab"), "Unique InstanceFrames", uniqueListA);
		printSet(new File(this.destinationFolder, "Compound_Unique_"+organismB+".tab"), "Unique InstanceFrames", uniqueListB);
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
	
	protected void printSet(File file, String columnName, Set<CompoundItem> set) {
		ArrayList<CompoundItem> list = new ArrayList<CompoundItem>();
		list.addAll(set);
		printSet(file, columnName, list);
	}
	
	protected void printSet(File file, String columnName, ArrayList<CompoundItem> set) {
		PrintStream o = null;
		try {
			o = new PrintStream(file);
			o.println(columnName);
			for (CompoundItem item : set) {
				o.println(item.frameID + "\t" + item.comparableField);
			}
			o.close();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
