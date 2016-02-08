package edu.iastate.CornCompare;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import edu.iastate.javacyco.Frame;
import edu.iastate.javacyco.JavacycConnection;
import edu.iastate.javacyco.Network;
import edu.iastate.javacyco.PtoolsErrorException;

/**
 * This object controls the comparison of Proteins between the two provided pathway tools organisms.  This object also controls comparison of EC numbers.
 * EC numbers are stored in slots on the Protein objects.
 * 
 * Proteins use a naming convention with a _P## or _T## suffix to denote alternate gene products for a gene with alternate splicing.  We do not remove this on proteins
 * as the proteins do represent different biological entities.
 * 
 * This object is specifically tailored to the data in the MaizeCyc and CornCyc databases and is not guaranteed to work properly with other organisms.
 * 
 * @author Jesse
 *
 */
public class ProteinComparison {
	private final String ptoolsClass = "|Proteins|"; //TODO |Polypeptides| or |Proteins|?
	private JavacycConnection conn;
	private String fileName;
	private boolean verbose;
	private String organismA;
	private String organismB;
	private String logFile = "Proteins\\log.txt";
	private String destination = "Proteins";
	
	private FrameList<ProteinItem> framesA;
	private FrameList<ProteinItem> framesB;
	
	public ProteinComparison (String host, String organismA, String organismB, int port, String fileName, boolean verbose) {
		conn = new JavacycConnection(host, port);
		this.fileName = fileName;
		this.verbose = verbose;
		this.organismA = organismA;
		this.organismB = organismB;
		this.framesA = new FrameList<ProteinItem>();
		this.framesB = new FrameList<ProteinItem>();
		
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
	private void loadItems(String organism, FrameList<ProteinItem> frameList) throws PtoolsErrorException {
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
			if (node.isClassFrame()) frameList.classList.add(new ProteinItem(node.getLocalID(), node.getCommonName()));
			else frameList.instanceList.add(new ProteinItem(node.getLocalID(), node.getCommonName()));
		}
		
		if (verbose) {
			System.out.println("Sorted " + frameList.classList.size() + " classes and " + frameList.instanceList.size() + " instances of type " + this.ptoolsClass + " for " + organism);
			appendLine(logFile, "Sorted " + frameList.classList.size() + " classes and " + frameList.instanceList.size() + " instances of type " + this.ptoolsClass + " for " + organism+"\n");
		}
	}
	
	/**
	 * Perform a data pre-processing step prior to comparison.  For Protein data, this means we first filter out Proteins with duplicate common names.
	 * 
	 * @return
	 * @throws PtoolsErrorException
	 */
	private Set<ProteinItem> preProcess(String organism, FrameList<ProteinItem> frameList) throws PtoolsErrorException {
		if (verbose) {
			System.out.println("Performing preprocessing steps on " + ptoolsClass + " for " + organism + "...");
			appendLine(logFile, "Performing preprocessing steps on " + ptoolsClass + " for " + organism + "..."+"\n");
		}
		
		conn.selectOrganism(organism);
		
		Set<ProteinItem> tempRemoveDuplicateSet = new HashSet<ProteinItem>();
		
		// First remove any Protein with a duplicate common name.  Since we are matching on common names, these will complicate the matching process.
		int countDuplicateNames = 0;
		for (ProteinItem item : frameList.instanceList) {
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
		
		// Next, we only consider Proteins with some form of annotation.  We define annotation as having a link to at least one GO annotation and/or reaction link.
		int countNoAnnotation = 0;
		Set<ProteinItem> processedList = new HashSet<ProteinItem>();
		for (ProteinItem item : tempRemoveDuplicateSet) {
			Frame product = Frame.load(conn, item.frameID);
			if (!product.getSlotValues("CATALYZES").isEmpty() || !product.getSlotValues("GO-TERMS").isEmpty()) {
				processedList.add(item);
			} else {
				if (verbose) {
//					System.out.println("Removing " + item.frameID + ": no annotation");
					appendLine(logFile, "Removing " + item.frameID + ": no annotation"+"\n");
					countNoAnnotation++;
				}
			}
		}
		if (verbose) {
			System.out.println("Removed a total of " + countNoAnnotation + " objects due to no annotation");
			appendLine(logFile, "Removed a total of " + countNoAnnotation + " objects due to no annotation"+"\n");
		}
		
		//TODO i might need to replace any _T## with an _P## to make them consistent and therefore matchable
		
		// Print the sorted and processed lists
		printSet("Proteins\\Classes_"+organism+".tab", "ClassFrames", frameList.classList);//TODO user-defined locations
		printSet("Proteins\\Instances_"+organism+".tab", "InstanceFrames", frameList.instanceList);
		printSet("Proteins\\Filtered_"+organism+".tab", "Filtered InstanceFrames", processedList);
		
		return processedList;
	}
	
	public void compare() {
		Set<ProteinItem> matched = new HashSet<ProteinItem>();
		Set<ProteinItem> uniqueListA = new HashSet<ProteinItem>();
		Set<ProteinItem> uniqueListB = new HashSet<ProteinItem>();
//		HashMap<ProteinItem,ProteinItem> setA = new HashMap<ProteinItem,ProteinItem>();
		HashMap<ProteinItem,ProteinItem> setB = new HashMap<ProteinItem,ProteinItem>(); // Needed a get method for uniqueList.  Quick solution: make a matching HashMap.
		
		try {
			uniqueListA = preProcess(organismA, framesA);
//			for (ProteinItem item : uniqueListA) {
//				setA.put(item, item); 
//			}
			uniqueListB = preProcess(organismB, framesB);
			for (ProteinItem item : uniqueListB) {
				setB.put(item, item); 
			}
		} catch (PtoolsErrorException e) {
			e.printStackTrace();
		}
		
		String matchSetOutput = organismA + "\t" + organismB + "\n";
		for (ProteinItem item : uniqueListA) {
			if (setB.containsValue(item)) {
				matched.add(item);
				matchSetOutput = matchSetOutput + item.frameID + "\t" + item.comparableField + "\t" + setB.get(item).frameID + "\t" + setB.get(item).comparableField + "\n";
			}
		}
//		for (ProteinItem item : uniqueListB) {
//			if (uniqueListA.contains(item)) {
//				matched.add(item);
//			}
//		}
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
		printString("Proteins\\Matching_"+organismA+"_vs_"+organismB+".tab", matchSetOutput);
		printSet("Proteins\\UniqueA_"+organismA+".tab", "Unique InstanceFrames", uniqueListA);
		printSet("Proteins\\UniqueB_"+organismB+".tab", "Unique InstanceFrames", uniqueListB);
	}
	
	/**
	 * Simple function to print a string to the specified file location.
	 * 
	 * @param fileName
	 * @param printString
	 */
	protected void printString(String fileName, String printString) {
		PrintStream o = null;
		try {
			o = new PrintStream(new File(fileName));
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
	 * @param fileName
	 * @param printString
	 */
	protected void appendLine(String fileName, String printString) {
		PrintStream o = null;
		try {
			o = new PrintStream(new FileOutputStream(fileName, true));
			o.append(printString);
			o.close();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	protected void printSet(String fileName, String columnName, Set<ProteinItem> set) {
		ArrayList<ProteinItem> list = new ArrayList<ProteinItem>();
		list.addAll(set);
		printSet(fileName, columnName, list);
	}
	
	protected void printSet(String fileName, String columnName, ArrayList<ProteinItem> set) {
		PrintStream o = null;
		try {
			o = new PrintStream(new File(fileName));
			o.println(columnName);
			for (ProteinItem item : set) {
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
