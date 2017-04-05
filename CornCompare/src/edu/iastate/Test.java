package edu.iastate;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import edu.iastate.javacyco.Frame;
import edu.iastate.javacyco.JavacycConnection;
import edu.iastate.javacyco.Pathway;
import edu.iastate.javacyco.Protein;
import edu.iastate.javacyco.PtoolsErrorException;

public class Test {
	public static void main(String[] args) {
		try {
			Long start = System.currentTimeMillis();
			
//			run();
//			test();
			comparePathwayGenes();
//			deleteGO();
			
			Long stop = System.currentTimeMillis();
			Long runtime = (stop - start) / 1000;
			System.out.println("Runtime is " + runtime + " seconds.");
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Caught a "+e.getClass().getName()+". Shutting down...");
		}
	}

	private static void test() {
		
	}

	private static void deleteGO() throws PtoolsErrorException {
		int port = 4444;
//		String organismCorn = "CORN";
		String organismMaize = "MAIZE";
		JavacycConnection conn = new JavacycConnection("jrwalsh.student.iastate.edu", port);
		conn.selectOrganism(organismMaize);
		
//		// Print out sizes of each database type
//		System.out.println(conn.getClassAllInstances(Gene.GFPtype).size());
//		System.out.println(conn.getClassAllInstances(Protein.GFPtype).size());
//		System.out.println(conn.getClassAllInstances(Compound.GFPtype).size());
//		System.out.println(conn.getClassAllInstances(Reaction.GFPtype).size());
//		System.out.println(conn.getClassAllInstances(Pathway.GFPtype).size());
		
		// Try printing a frame
//		Frame proteinFrame = Frame.load(conn, "GBWI-57641-MONOMER");
//		proteinFrame.putSlotValues("GO-TERMS", new ArrayList<String>());
//		proteinFrame.commit();
//		proteinFrame.print();
		
//		// Delete GO terms
//		ArrayList<Frame> proteinFrames = conn.getAllGFPInstances(Protein.GFPtype);
//		for (Frame proteinFrame : proteinFrames) {
//			proteinFrame.putSlotValues("GO-TERMS", new ArrayList<String>());
//			proteinFrame.commit();
//		}
		
//		// Delete non EV-COMP GO terms
//		ArrayList<Frame> proteinFrames = conn.getAllGFPInstances(Protein.GFPtype);
//		for (Frame proteinFrame : proteinFrames) {
//			HashMap<String, ArrayList<String>> goTermList = new HashMap<String, ArrayList<String>>();
//			for (Object goTerm : proteinFrame.getSlotValues("GO-TERMS")) {
//				ArrayList<String> goCitations = new ArrayList<String>();
//				for (Object annot : proteinFrame.getAnnotations("GO-TERMS", goTerm.toString(), "CITATIONS")) {
//					if (annot.toString().contains("EV-COMP")) goCitations.add(annot.toString());
//				}
//				if (!goCitations.isEmpty()) goTermList.put(goTerm.toString(), goCitations);
//			}
//			ArrayList<String> goTerms = new ArrayList<String>();
//			goTerms.addAll(goTermList.keySet());
//			proteinFrame.putSlotValues("GO-TERMS", goTerms);
//			for (String goTerm : goTerms) {
//				proteinFrame.putLocalSlotValueAnnotations("GO-TERMS", goTerm, "CITATIONS", goTermList.get(goTerm));
//			}
//			proteinFrame.commit();
//		}
		
		// Print GO terms
		String out = "";
		ArrayList<Frame> proteinFrames = conn.getAllGFPInstances(Protein.GFPtype);
		for (Frame proteinFrame : proteinFrames) {
//			System.out.println(proteinFrame.getSlotValues("GO-TERMS"));
			for (Object goTerm : proteinFrame.getSlotValues("GO-TERMS")) {
				for (Object annot : proteinFrame.getAnnotations("GO-TERMS", goTerm.toString(), "CITATIONS")) {
//					System.out.println(proteinFrame.getLocalID() + "\t" + goTerm.toString() + "\t" + annot.toString());
					out += proteinFrame.getLocalID() + "\t" + goTerm.toString() + "\t" + annot.toString() + "\n";
				}
			}
		}
		printString(new File("/home/jesse/Desktop/goterms.tab"), out);
		
	}

	private static void run() throws PtoolsErrorException {
		int port = 4444;
		String organismCorn = "CORN";
		String organismMaize = "MAIZE";
		JavacycConnection conn = new JavacycConnection("", port);//TODO set up way to specify server
	}
	
	private static void comparePathwayGenes() throws PtoolsErrorException {
		int port = 4444;
		String organismCorn = "CORN";
		String organismMaize = "MAIZE";
		JavacycConnection conn = new JavacycConnection("localhost", port);//TODO set up way to specify server
		
		// Compare pathway-gene membership
		conn.selectOrganism(organismCorn);
		ArrayList<Frame> cornPathways = conn.getAllGFPInstances(Pathway.GFPtype);
		HashMap<String,ArrayList<String>> cornPathwayGenes = new HashMap<String,ArrayList<String>>();
		String outString = "";
		for (Frame pathway : cornPathways) {
			for (Object genes : conn.genesOfPathway(pathway.getLocalID())) {
				String name = conn.getSlotValue(genes.toString(), "Common-Name").replaceAll("\"", "");
				name = name.replaceAll("_P..$", ""); //Remove _P## suffixs, which indicate this is a transcript
				name = name.replaceAll("_T..$", ""); //Remove _T## suffixs, which also indicate this is a transcript
				name = name.replaceAll("_FGP...$", ""); //Remove FGP#### suffixs, which also indicate this is a transcript
				name = name.replaceAll("_FGT...$", ""); //Remove FGP#### suffixs, which also indicate this is a transcript
				
				if (cornPathwayGenes.get(pathway.getLocalID()) == null) {
					ArrayList<String> temp = new ArrayList<String>();
					temp.add(name);
					cornPathwayGenes.put(pathway.getLocalID(), temp);
				} else {
					cornPathwayGenes.get(pathway.getLocalID()).add(name);
				}
			}
		}
		
		conn.selectOrganism(organismMaize);
		ArrayList<Frame> maizePathways = conn.getAllGFPInstances(Pathway.GFPtype);
		HashMap<String,ArrayList<String>> maizePathwayGenes = new HashMap<String,ArrayList<String>>();
		for (Frame pathway : maizePathways) {
			for (Object genes : conn.genesOfPathway(pathway.getLocalID())) {
				String name = conn.getSlotValue(genes.toString(), "Common-Name").replaceAll("\"", "");
				name = name.replaceAll("_P..$", ""); //Remove _P## suffixs, which indicate this is a transcript
				name = name.replaceAll("_T..$", ""); //Remove _T## suffixs, which also indicate this is a transcript
				name = name.replaceAll("_FGP...$", ""); //Remove FGP#### suffixs, which also indicate this is a transcript
				name = name.replaceAll("_FGT...$", ""); //Remove FGP#### suffixs, which also indicate this is a transcript
				
				if (maizePathwayGenes.get(pathway.getLocalID()) == null) {
					ArrayList<String> temp = new ArrayList<String>();
					temp.add(name);
					maizePathwayGenes.put(pathway.getLocalID(), temp);
				} else {
					maizePathwayGenes.get(pathway.getLocalID()).add(name);
				}
			}
		}
		
		for (Frame pathway : cornPathways) {
			if (cornPathwayGenes.containsKey(pathway.getLocalID()) && maizePathwayGenes.containsKey(pathway.getLocalID())) {
				ArrayList<String> cornGenes = cornPathwayGenes.get(pathway.getLocalID());
				ArrayList<String> maizeGenes = maizePathwayGenes.get(pathway.getLocalID());
				ArrayList<String> bothGenes = new ArrayList<String>();
				bothGenes.addAll(cornGenes);
				bothGenes.retainAll(maizeGenes);
				cornGenes.removeAll(bothGenes);
				maizeGenes.removeAll(bothGenes);
				
				for (String gene : bothGenes) {
					outString += pathway.getLocalID() + "\t" + pathway.getCommonName() + "\t" + gene + "\tx\tx\n";
				}
				for (String gene : cornGenes) {
					outString += pathway.getLocalID() + "\t" + pathway.getCommonName() + "\t" + gene + "\tx\t\n";
				}
				for (String gene : maizeGenes) {
					outString += pathway.getLocalID() + "\t" + pathway.getCommonName() + "\t" + gene + "\t\tx\n";
				}
			}
		}
		printString(new File("/home/jesse/Desktop/merge_out.tab"), outString);
	}
	
	/**
	 * Simple function to print a string to the specified file location.
	 * 
	 * @param fileName
	 * @param printString
	 */
	protected static void printString(File file, String printString) {
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
}
