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
import edu.iastate.javacyco.JavacycConnection;
import edu.iastate.javacyco.Network;
import edu.iastate.javacyco.PtoolsErrorException;

/**
 * This object controls the comparison of Genes between the two provided pathway tools organisms.  This object also controls comparison of gene transcripts.
 * While transcripts are not conceptually the same as genes, they are stored in the same object type in the Pathway Tools Ontology.
 * 
 * This object is specifically tailored to the data in the MaizeCyc and CornCyc databases and is not guaranteed to work properly with other organisms.
 * 
 * @author Jesse R Walsh
 * @date 2/10/2016
 */
public class GeneComparison {
	private final String ptoolsClass = "|Genes|";
	private JavacycConnection conn;
	private boolean verbose;
	private String organismA;
	private String organismB;
	private File logFile;
	private String destinationFolder;
	private String destinationFolderTranscripts;
	
	private FrameList<GeneItem> framesA;
	private FrameList<GeneItem> framesB;
	
	public GeneComparison (String host, String organismA, String organismB, int port, String outputDir, boolean verbose) {
		conn = new JavacycConnection(host, port);
		this.destinationFolder = outputDir + System.getProperty("file.separator") + "Genes";
		this.destinationFolderTranscripts = outputDir + System.getProperty("file.separator") + "Genes" + System.getProperty("file.separator") + "Transcripts";
		this.logFile = new File(destinationFolder, "Gene_and_Transcript_log.txt");
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
	 * information.  For transcripts, we instead convert _P## to _T## suffixes for consistency.
	 * 
	 * @return
	 * @throws PtoolsErrorException
	 */
	private Set<GeneItem> preProcess(String organism, FrameList<GeneItem> frameList, boolean includeTranscripts, boolean useGOAnnotation) throws PtoolsErrorException {
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
				appendLine(logFile, "Removing \"" + item.frameID + " - " + item.comparableField + "\" from set: duplicate common name"+"\n");
				countDuplicateNames++;
			}
		}
		if (verbose) {
			System.out.println("Removed a total of " + countDuplicateNames + " objects due to duplicate common names");
			appendLine(logFile, "Removed a total of " + countDuplicateNames + " objects due to duplicate common names"+"\n");
		}
		
		// Next, we only consider genes with some form of annotation.  We define annotation as having a link to at least one GO annotation 
		// or 1 reaction catalyzing gene product. The use selects which type of annotation to use
		int countNoAnnotation = 0;
		Set<GeneItem> tempRemoveMissingAnnotationSet = new HashSet<GeneItem>();
		for (GeneItem item : tempRemoveDuplicateSet) {
			boolean foundGood = false;
			try {
				@SuppressWarnings("unchecked")
				ArrayList<String> productsOfGene = conn.allProductsOfGene(item.frameID);
				for (String productID : productsOfGene) {
					Frame product = Frame.load(conn, productID);
					if (useGOAnnotation) {
						if (!product.getSlotValues("GO-TERMS").isEmpty()) {
							foundGood = true;
							break;
						}
					} else {
						if (!product.getSlotValues("CATALYZES").isEmpty()) {
							foundGood = true;
							break;
						}
					}
				}
			} catch (PtoolsErrorException e) {
				// Sometimes hit this due to inconsistencies in database interacting with allProductsOfGene method.
				// See GBWI-74347 for an example.  Just assume no annotation and move on.
				foundGood = false;
			}
			if (foundGood) {
				tempRemoveMissingAnnotationSet.add(item);
				foundGood = false;
			}
			else {
				if (verbose) {
//					System.out.println("Removing " + item.frameID + " - " + item.comparableField + ": no annotation");
					appendLine(logFile, "Removing " + item.frameID + " - " + item.comparableField + ": no annotation"+"\n");
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
//					System.out.println("Removing \"" + item.frameID + " - " + item.comparableField + "\" from set: this frame is a splice variant");
					appendLine(logFile, "Removing \"" + item.frameID + " - " + item.comparableField + "\" from set: this frame is a splice variant"+"\n");
					countSpliceVariant++;
				}
			}
			if (verbose) {
				System.out.println("Removed a total of " + countSpliceVariant + " objects due to being a transcript and not a gene");
				appendLine(logFile, "Removed a total of " + countSpliceVariant + " objects due to being a transcript and not a gene"+"\n");
			}
		} else {
			int suffixConsistencyCount = 0;
			for (GeneItem item : tempRemoveMissingAnnotationSet) {
				String itemGeneID = item.comparableField;
				
				//Replace any _P## with an _T## to make them consistent and therefore matchable between MaizeCyc and CornCyc
				String p = "(.*)(_P)(\\d\\d$)"; // 3 capture groups.  first group is any length of letters or numbers, followed by an _P, followed by 2 numbers and an end of line
				Pattern r = Pattern.compile(p);
				Matcher m = r.matcher(itemGeneID);
				if (m.find()) {
					itemGeneID = m.replaceFirst("$1_T$3");
					suffixConsistencyCount++;
				} else {
					//Replace any _FGP### with an _FGT### to make them consistent and therefore matchable between MaizeCyc and CornCyc
					p = "(.*)(_FGP)(\\d\\d\\d$)";
					r = Pattern.compile(p);
					m = r.matcher(itemGeneID);
					if (m.find()) {
						itemGeneID = m.replaceFirst("$1_FGT$3");
						suffixConsistencyCount++;
					}
				}
				
				GeneItem geneItem = new GeneItem(item.frameID, itemGeneID);
				if (processedList.add(geneItem)) {
					if (verbose && !item.comparableField.equalsIgnoreCase(geneItem.comparableField)) {
//						System.out.println("Updating name from \"" + item.frameID + " - " + item.comparableField + "\" to \"" + geneItem.frameID + " - " + geneItem.comparableField + "\""");
						appendLine(logFile, "Updating name from \"" + item.frameID + " - " + item.comparableField + "\" to \"" + geneItem.frameID + " - " + geneItem.comparableField + "\""+"\n");
					}
				} else {
					System.err.println("Warning! Caught and removed an unexpected duplicate name during transcript name updating!");
				}
			}
			if (verbose) {
				System.out.println("Updated a total of " + suffixConsistencyCount + " objects due to transcript suffix inconsistency");
				appendLine(logFile, "Updated a total of " + suffixConsistencyCount + " objects due to transcript suffix inconsistency"+"\n");
			}
		}
		
		// Print the sorted and processed lists
		if (includeTranscripts) {
			printSet(new File(this.destinationFolderTranscripts, "Transcript_Classes_"+organism+".tab"), "ClassFrames", frameList.classList);
			printSet(new File(this.destinationFolderTranscripts, "Transcript_Instances_"+organism+".tab"), "InstanceFrames", frameList.instanceList);
			printSet(new File(this.destinationFolderTranscripts, "Transcript_Filtered_"+organism+".tab"), "Filtered InstanceFrames", processedList);
			return processedList;
		} else {
			printSet(new File(this.destinationFolder, "Gene_Classes_"+organism+".tab"), "ClassFrames", frameList.classList);
			printSet(new File(this.destinationFolder, "Gene_Instances_"+organism+".tab"), "InstanceFrames", frameList.instanceList);
			printSet(new File(this.destinationFolder, "Gene_Filtered_"+organism+".tab"), "Filtered InstanceFrames", processedList);
			return processedList;
		}
	}
	
	public void compare(boolean includeTranscripts, boolean useGOAnnotation) {
		Set<GeneItem> matched = new HashSet<GeneItem>();
		Set<GeneItem> uniqueListA = new HashSet<GeneItem>();
		Set<GeneItem> uniqueListB = new HashSet<GeneItem>();
		HashMap<GeneItem,GeneItem> setB = new HashMap<GeneItem,GeneItem>(); // Needed a get method for uniqueList.  Quick solution: make a matching HashMap.
		
		try {
			uniqueListA = preProcess(organismA, framesA, includeTranscripts, useGOAnnotation);
			uniqueListB = preProcess(organismB, framesB, includeTranscripts, useGOAnnotation);
			for (GeneItem item : uniqueListB) {
				setB.put(item, item); 
			}
		} catch (PtoolsErrorException e) {
			e.printStackTrace();
		}
		
		String matchSetOutput = organismA + "\t\t" + organismB + "\t\n";
		for (GeneItem item : uniqueListA) {
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
		if (includeTranscripts) {
			printString(new File(this.destinationFolderTranscripts, "Transcript_Matching_"+organismA+"_vs_"+organismB+".tab"), matchSetOutput);
			printSet(new File(this.destinationFolderTranscripts, "Transcript_Unique_"+organismA+".tab"), "Unique InstanceFrames", uniqueListA);
			printSet(new File(this.destinationFolderTranscripts, "Transcript_Unique_"+organismB+".tab"), "Unique InstanceFrames", uniqueListB);
		} else {
			printString(new File(this.destinationFolder, "Gene_Matching_"+organismA+"_vs_"+organismB+".tab"), matchSetOutput);
			printSet(new File(this.destinationFolder, "Gene_Unique_"+organismA+".tab"), "Unique InstanceFrames", uniqueListA);
			printSet(new File(this.destinationFolder, "Gene_Unique_"+organismB+".tab"), "Unique InstanceFrames", uniqueListB);
		}
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
	
	protected void printSet(File file, String columnName, Set<GeneItem> set) {
		ArrayList<GeneItem> list = new ArrayList<GeneItem>();
		list.addAll(set);
		printSet(file, columnName, list);
	}
	
	protected void printSet(File file, String columnName, ArrayList<GeneItem> set) {
		PrintStream o = null;
		try {
			o = new PrintStream(file);
			o.println(columnName);
			for (GeneItem item : set) {
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
