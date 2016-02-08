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
 * This object controls the comparison of Genes between the two provided pathway tools organisms.  This object also controls comparison of gene transcripts.
 * While transcripts are not conceptually the same as genes, they are stored in the same object type in the Pathway Tools Ontology.
 * 
 * This object is specifically tailored to the data in the MaizeCyc and CornCyc databases and is not guaranteed to work properly with other organisms.
 * 
 * @author Jesse
 *
 */
public class GeneComparison {
	private final String ptoolsClass = "|Genes|";
	private JavacycConnection conn;
	private String fileName;
	private boolean verbose;
	private String organismA;
	private String organismB;
	private String logFile = "Genes\\log.txt";
	private String destination = "Genes";
	
	private FrameList<GeneItem> framesA;
	private FrameList<GeneItem> framesB;
	
//	private Set<Frame> rawListA;
//	private ArrayList<GeneItem> classListA;
//	private ArrayList<GeneItem> instanceListA;
//	private Set<Frame> rawListB;
//	private ArrayList<GeneItem> classListB;
//	private ArrayList<GeneItem> instanceListB;
	
	public GeneComparison (String host, String organismA, String organismB, int port, String fileName, boolean verbose) {
		conn = new JavacycConnection(host, port);
		this.fileName = fileName;
		this.verbose = verbose;
		this.organismA = organismA;
		this.organismB = organismB;
		this.framesA = new FrameList<GeneItem>();
		this.framesB = new FrameList<GeneItem>();
		
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
	private void loadItems(String organism, FrameList<GeneItem> frameList) throws PtoolsErrorException {
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
			if (node.isClassFrame()) frameList.classList.add(new GeneItem(node.getLocalID(), node.getCommonName()));
			else frameList.instanceList.add(new GeneItem(node.getLocalID(), node.getCommonName()));
		}
		
		if (verbose) {
			System.out.println("Sorted " + frameList.classList.size() + " classes and " + frameList.instanceList.size() + " instances of type " + this.ptoolsClass + " for " + organism);
			appendLine(logFile, "Sorted " + frameList.classList.size() + " classes and " + frameList.instanceList.size() + " instances of type " + this.ptoolsClass + " for " + organism+"\n");
		}
	}
	
	/**
	 * Perform a data pre-processing step prior to comparison.  For gene data, this means we first filter out genes with duplicate common names,
	 * then remove the _P## and _T## suffixes on the common names in order to specifically address the gene questions without complicating the data with transcript
	 * information.
	 * 
	 * @return
	 * @throws PtoolsErrorException
	 */
	private Set<GeneItem> preProcess(String organism, FrameList<GeneItem> frameList, boolean includeTranscripts) throws PtoolsErrorException {
		if (verbose) {
			if (includeTranscripts) {
				System.out.println("Performing preprocessing steps on " + ptoolsClass + " for " + organism + " including transcripts...");
				appendLine(logFile, "Performing preprocessing steps on " + ptoolsClass + " for " + organism + " including transcripts..."+"\n");
			} else {
				System.out.println("Performing preprocessing steps on " + ptoolsClass + " for " + organism + " excluding transcripts...");
				appendLine(logFile, "Performing preprocessing steps on " + ptoolsClass + " for " + organism + " excluding transcripts..."+"\n");
			}
		}
		
		conn.selectOrganism(organism);
		
		Set<GeneItem> tempRemoveDuplicateSet = new HashSet<GeneItem>();
		
		// First remove any gene with a duplicate common name.  Since we are matching on common names, these will complicate the matching process.
		int countDuplicateNames = 0;
		for (GeneItem item : frameList.instanceList) {
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
		
		// Next, we only consider genes with some form of annotation.  We define annotation as having a link to at least one GO annotation and/or 1 
		// reaction catalyzing gene product.
		int countNoAnnotation = 0;
		Set<GeneItem> tempRemoveMissingAnnotationSet = new HashSet<GeneItem>();
		for (GeneItem gene : tempRemoveDuplicateSet) {
			boolean foundGood = false;
			try {
				ArrayList<String> productsOfGene = conn.allProductsOfGene(gene.frameID);
				for (String productID : productsOfGene) {
					Frame product = Frame.load(conn, productID);
					if (!product.getSlotValues("CATALYZES").isEmpty() || !product.getSlotValues("GO-TERMS").isEmpty()) {
						foundGood = true;
						break;
					}
				}
			} catch (PtoolsErrorException e) {
				// Sometimes hit this due to inconsistencies in database interacting with allProductsOfGene method.
				// See GBWI-74347 for an example.  Just assume no annotation and move on.
				foundGood = false;
			}
			if (foundGood) {
				tempRemoveMissingAnnotationSet.add(gene);
				foundGood = false;
			}
			else {
				if (verbose) {
//					System.out.println("Removing " + gene.frameID + ": no annotation");
					appendLine(logFile, "Removing " + gene.frameID + ": no annotation"+"\n");
					countNoAnnotation++;
				}
			}
		}
		if (verbose) {
			System.out.println("Removed a total of " + countNoAnnotation + " objects due to no annotation");
			appendLine(logFile, "Removed a total of " + countNoAnnotation + " objects due to no annotation"+"\n");
		}
		
		Set<GeneItem> processedList = new HashSet<GeneItem>();
		if (!includeTranscripts) {
			// We are considering genes only, not transcripts at this point.  Remove transcript identifier and keep a record of the gene only.  Does not do
			// any fancy merging of protein/reaction/pathway associations across all transcript variants as that is not needed at this point.
			int countSpliceVariant = 0;
			for (GeneItem item : tempRemoveMissingAnnotationSet) {
				String itemGeneID = item.comparableField;
				itemGeneID = itemGeneID.replaceAll("_P..$", ""); //Remove _P## suffixs, which indicate this is a transcript
				itemGeneID = itemGeneID.replaceAll("_T..$", ""); //Remove _T## suffixs, which also indicate this is a transcript
				itemGeneID = itemGeneID.replaceAll("_FGP...$", ""); //Remove FGP#### suffixs, which also indicate this is a transcript
				itemGeneID = itemGeneID.replaceAll("_FGT...$", ""); //Remove FGP#### suffixs, which also indicate this is a transcript
				GeneItem geneItem = new GeneItem(item.frameID, itemGeneID);
				if (!processedList.add(geneItem) && verbose) {
	//				System.out.println("Removing \"" + item.frameID + " - " + item.comparableField + "\" from set: this frame is a splice variant");
					appendLine(logFile, "Removing \"" + item.frameID + " - " + item.comparableField + "\" from set: this frame is a splice variant"+"\n");
					countSpliceVariant++;
				}
			}
			if (verbose) {
				System.out.println("Removed a total of " + countSpliceVariant + " objects due to being a transcript and not a gene");
				appendLine(logFile, "Removed a total of " + countSpliceVariant + " objects due to being a transcript and not a gene"+"\n");
			}
		} else {
			// Skip the step of filtering out transcripts
			processedList = tempRemoveMissingAnnotationSet;
		}
		
		// Print the sorted and processed lists
		if (includeTranscripts) {
			printSet("Genes\\Transcripts\\Classes_"+organism+".tab", "ClassFrames", frameList.classList);//TODO user-defined locations
			printSet("Genes\\Transcripts\\Instances_"+organism+".tab", "InstanceFrames", frameList.instanceList);
			printSet("Genes\\Transcripts\\Filtered_"+organism+".tab", "Filtered InstanceFrames", processedList);
			
			return processedList;
		} else {
			printSet("Genes\\Classes_"+organism+".tab", "ClassFrames", frameList.classList);//TODO user-defined locations
			printSet("Genes\\Instances_"+organism+".tab", "InstanceFrames", frameList.instanceList);
			printSet("Genes\\Filtered_"+organism+".tab", "Filtered InstanceFrames", processedList);
			
			return processedList;
		}
	}
	
	public void compare(boolean includeTranscripts) {
		Set<GeneItem> matched = new HashSet<GeneItem>();
		Set<GeneItem> uniqueListA = new HashSet<GeneItem>();
		Set<GeneItem> uniqueListB = new HashSet<GeneItem>();
//		HashMap<GeneItem,GeneItem> setA = new HashMap<GeneItem,GeneItem>();
		HashMap<GeneItem,GeneItem> setB = new HashMap<GeneItem,GeneItem>(); // Needed a get method for uniqueList.  Quick solution: make a matching HashMap.
		
		try {
			uniqueListA = preProcess(organismA, framesA, includeTranscripts);
//			for (GeneItem item : uniqueListA) {
//				setA.put(item, item); 
//			}
			uniqueListB = preProcess(organismB, framesB, includeTranscripts);
			for (GeneItem item : uniqueListB) {
				setB.put(item, item); 
			}
		} catch (PtoolsErrorException e) {
			e.printStackTrace();
		}
		
		String matchSetOutput = organismA + "\t" + organismB + "\n";
		for (GeneItem item : uniqueListA) {
			if (setB.containsValue(item)) {
				matched.add(item);
				matchSetOutput = matchSetOutput + item.frameID + "\t" + setB.get(item).frameID;
			}
		}
//		for (GeneItem item : uniqueListB) {
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
		if (includeTranscripts) {
			printSet("Genes\\Transcripts\\Matching_"+organismA+"_vs_"+organismB+".tab", "ClassFrames", matched);
			printSet("Genes\\Transcripts\\UniqueA_"+organismA+".tab", "Unique InstanceFrames", uniqueListA);
			printSet("Genes\\Transcripts\\UniqueB_"+organismB+".tab", "Unique InstanceFrames", uniqueListB);
		} else {
			printSet("Genes\\Matching_"+organismA+"_vs_"+organismB+".tab", "ClassFrames", matched);
			printSet("Genes\\UniqueA_"+organismA+".tab", "Unique InstanceFrames", uniqueListA);
			printSet("Genes\\UniqueB_"+organismB+".tab", "Unique InstanceFrames", uniqueListB);
		}
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
	
	protected void printSet(String fileName, String columnName, ArrayList<GeneItem> set) {
		PrintStream o = null;
		try {
			o = new PrintStream(new File(fileName));
			o.println(columnName);
			for (GeneItem item : set) {
				o.println(item.frameID);
			}
			o.close();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	protected void printSet(String fileName, String columnName, Set<GeneItem> set) {
		PrintStream o = null;
		try {
			o = new PrintStream(new File(fileName));
			o.println(columnName);
			for (GeneItem item : set) {
				o.println(item.frameID);
			}
			o.close();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
