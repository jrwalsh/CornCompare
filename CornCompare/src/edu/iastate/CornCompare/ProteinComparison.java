package edu.iastate.CornCompare;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.iastate.javacyco.Frame;
import edu.iastate.javacyco.Gene;
import edu.iastate.javacyco.JavacycConnection;
import edu.iastate.javacyco.Network;
import edu.iastate.javacyco.Protein;
import edu.iastate.javacyco.PtoolsErrorException;

/**
 * This object controls the comparison of Proteins between the two provided pathway tools organisms.
 * 
 * Proteins use a naming convention with a _P## or _T## suffix to denote alternate gene products for a gene with alternate splicing.  We do not remove this on proteins
 * as the proteins do represent different biological entities.  However, we convert _T## suffixes to  _P## suffixes for consistency.
 * 
 * This object is specifically tailored to the data in the MaizeCyc and CornCyc databases and is not guaranteed to work properly with other organisms.
 * 
 * @author Jesse
 * @date 2/10/2016
 */
public class ProteinComparison {
	private final String ptoolsClass = "|Polypeptides|"; //We do not start at |Proteins|, too broad
	private JavacycConnection conn;
	private boolean verbose;
	private String organismA;
	private String organismB;
	private File logFile;
	private String destinationFolder;
	
	private FrameList<ProteinItem> framesA;
	private FrameList<ProteinItem> framesB;
	
	public ProteinComparison (String host, String organismA, String organismB, int port, String outputDir, boolean verbose) {
		conn = new JavacycConnection(host, port);
		this.destinationFolder = outputDir + System.getProperty("file.separator") + "Proteins";
		this.logFile = new File(destinationFolder, "Protein_log.txt");
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
		
		// First convert any descriptive enzyme names into GRMZM id's.
		// We ran into a large problem where MaizeCyc had an unreasonable number of duplicated common names (up to 325 proteins with the 
		// same name affecting about 5800+ proteins). We handle this by converting those proteins with non-serialized names (i.e. names that
		// don't begin with GRMZM or AC####) into the name of the associated gene.  We checked and almost all proteins had genes (except ~20 in CornCyc
		// and ~1 in MaizeCyc).
		ArrayList<ProteinItem> fixName = new ArrayList<ProteinItem>();
		int countFixedNames = 0;
		for (ProteinItem item : frameList.instanceList) {
			if (item.comparableField.startsWith("AC") || item.comparableField.startsWith("GRMZM")) {
				fixName.add(item);
			} else {
				try {
					Protein protein = (Protein)Protein.load(conn, item.frameID);
					Gene gene = protein.getGenes().get(0);
					if (gene.getCommonName().startsWith("AC") || gene.getCommonName().startsWith("GRMZM")) {
						if (verbose) {
//							System.out.println("Converting protein ID for \"" + item.frameID + " - " + item.comparableField + "\" to \"" + item.frameID + " - " + gene.getCommonName() + "\"");
							appendLine(logFile, "Converting protein ID for \"" + item.frameID + " - " + item.comparableField + "\" to \"" + item.frameID + " - " + gene.getCommonName() + "\""+"\n");
						}
						item.comparableField = gene.getCommonName();
						countFixedNames++;
					}
				} catch (Exception e) {
					System.err.println("Warning: Unable to convert \"" + item.frameID + " - " + item.comparableField + "\" to a standardized name");
					appendLine(logFile, "Warning: Unable to convert \"" + item.frameID + " - " + item.comparableField + "\" to a standardized name"+"\n");
				}
				fixName.add(item);
			}
		}
		if (verbose) {
			System.out.println("Converted a total of " + countFixedNames + " protein names to serialized IDs");
			appendLine(logFile, "Converted a total of " + countFixedNames + " protein names to serialized IDs"+"\n");
		}
		
		Set<ProteinItem> tempRemoveDuplicateSet = new HashSet<ProteinItem>();
		// Next remove any Protein with a duplicate common name.  Since we are matching on common names, these will complicate the matching process.
		int countDuplicateNames = 0;
		for (ProteinItem item : fixName) {
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
		Set<ProteinItem> tempRemoveMissingAnnotationSet = new HashSet<ProteinItem>();
		for (ProteinItem item : tempRemoveDuplicateSet) {
			Frame product = Frame.load(conn, item.frameID);
			//TODO JRW 5/26/2016 need a switch which allows user to select either GO term association or reaction association as the criteria which defines an acceptably annotated protein
			if (!product.getSlotValues("CATALYZES").isEmpty()) { //|| !product.getSlotValues("GO-TERMS").isEmpty()) {
				tempRemoveMissingAnnotationSet.add(item);
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
		
		//We need to update names to make them consistent and therefore matchable between CornCyc and MaizeCyc
		Set<ProteinItem> processedList = new HashSet<ProteinItem>();
		int suffixConsistencyCount = 0;
		for (ProteinItem item : tempRemoveMissingAnnotationSet) {
			String itemProteinID = item.comparableField;
			
			//Replace any _T## with an _P## to make them consistent and therefore matchable between MaizeCyc and CornCyc
			String p = "(.*)(_T)(\\d\\d$)"; // 3 capture groups.  first group is any length of letters or numbers, followed by an _P, followed by 2 numbers and an end of line
			Pattern r = Pattern.compile(p);
			Matcher m = r.matcher(itemProteinID);
			if (m.find()) {
				itemProteinID = m.replaceFirst("$1_P$3");
				suffixConsistencyCount++;
			} else {
				//Replace any _FGT### with an _FGP### to make them consistent and therefore matchable between MaizeCyc and CornCyc
				p = "(.*)(_FGT)(\\d\\d\\d$)";
				r = Pattern.compile(p);
				m = r.matcher(itemProteinID);
				if (m.find()) {
					itemProteinID = m.replaceFirst("$1_FGP$3");
					suffixConsistencyCount++;
				}
			}
			
			ProteinItem proteinItem = new ProteinItem(item.frameID, itemProteinID);
			if (processedList.add(proteinItem)) {
				if (verbose && !item.comparableField.equalsIgnoreCase(proteinItem.comparableField)) {
//					System.out.println("Updating name from \"" + item.frameID + " - " + item.comparableField + "\" to \"" + proteinItem.frameID + " - " + proteinItem.comparableField + "\""");
					appendLine(logFile, "Updating name from \"" + item.frameID + " - " + item.comparableField + "\" to \"" + proteinItem.frameID + " - " + proteinItem.comparableField + "\""+"\n");
				}
			} else {
				System.err.println("Warning! Caught and removed an unexpected duplicate name during transcript name updating!");
			}
		}
		if (verbose) {
			System.out.println("Updated a total of " + suffixConsistencyCount + " objects due to transcript suffix inconsistency");
			appendLine(logFile, "Updated a total of " + suffixConsistencyCount + " objects due to transcript suffix inconsistency"+"\n");
		}
		
		// Print the sorted and processed lists
		printSet(new File(this.destinationFolder, "Protein_Classes_"+organism+".tab"), "ClassFrames", frameList.classList);
		printSet(new File(this.destinationFolder, "Protein_Instances_"+organism+".tab"), "InstanceFrames", frameList.instanceList);
		printSet(new File(this.destinationFolder, "Protein_Filtered_"+organism+".tab"), "Filtered InstanceFrames", processedList);
		
		return processedList;
	}
	
	public void compare() {
		Set<ProteinItem> matched = new HashSet<ProteinItem>();
		Set<ProteinItem> uniqueListA = new HashSet<ProteinItem>();
		Set<ProteinItem> uniqueListB = new HashSet<ProteinItem>();
		HashMap<ProteinItem,ProteinItem> setB = new HashMap<ProteinItem,ProteinItem>(); // Needed a get method for uniqueList.  Quick solution: make a matching HashMap.
		
		try {
			uniqueListA = preProcess(organismA, framesA);
			uniqueListB = preProcess(organismB, framesB);
			for (ProteinItem item : uniqueListB) {
				setB.put(item, item); 
			}
		} catch (PtoolsErrorException e) {
			e.printStackTrace();
		}
		
		String matchSetOutput = organismA + "\t\t" + organismB + "\n";
		for (ProteinItem item : uniqueListA) {
			if (setB.containsValue(item)) {
				matched.add(item);
				matchSetOutput = matchSetOutput + item.frameID + "\t" + item.comparableField + "\t";
				matchSetOutput = matchSetOutput + setB.get(item).frameID + "\t" + setB.get(item).comparableField;
				matchSetOutput = matchSetOutput + "\n";
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
		printString(new File(this.destinationFolder, "Protein_Matching_"+organismA+"_vs_"+organismB+".tab"), matchSetOutput);
		printSet(new File(this.destinationFolder, "Protein_Unique_"+organismA+".tab"), "Unique InstanceFrames", uniqueListA);
		printSet(new File(this.destinationFolder, "Protein_Unique_"+organismB+".tab"), "Unique InstanceFrames", uniqueListB);
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
	
	protected void printSet(File file, String columnName, Set<ProteinItem> set) {
		ArrayList<ProteinItem> list = new ArrayList<ProteinItem>();
		list.addAll(set);
		printSet(file, columnName, list);
	}
	
	protected void printSet(File file, String columnName, ArrayList<ProteinItem> set) {
		PrintStream o = null;
		try {
			o = new PrintStream(file);
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
	
	//TODO Add in GO citation counting/comparing
//	for (Frame protein : proteinNodes) {
//		try {
//			for (Object goTerm : protein.getSlotValues("GO-TERMS")) {
//				uniqueGoTerms.add(goTerm.toString());
//				gotermAssignments++;
//				for(Object citation : protein.getAnnotations("GO-TERMS", goTerm.toString(), "CITATIONS")) {
//					gotermCitations++;
//				}
//			}
//		} catch (Exception e) {
//			System.out.println("Problem with protein : " + protein.getLocalID() + " : " + protein.isClassFrame());
//		}
//	}
}
