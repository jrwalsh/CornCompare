package edu.iastate;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import edu.iastate.javacyco.Frame;
import edu.iastate.javacyco.Gene;
import edu.iastate.javacyco.JavacycConnection;
import edu.iastate.javacyco.Pathway;
import edu.iastate.javacyco.PtoolsErrorException;

public class Test {
	public static void main(String[] args) {
		try {
			Long start = System.currentTimeMillis();
			
			run();
			
			Long stop = System.currentTimeMillis();
			Long runtime = (stop - start) / 1000;
			System.out.println("Runtime is " + runtime + " seconds.");
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Caught a "+e.getClass().getName()+". Shutting down...");
		}
	}
	
	private static void run() throws PtoolsErrorException {
		int port = 4444;
		String organismCorn = "CORN";
		String organismMaize = "MAIZE";
		JavacycConnection conn = new JavacycConnection("", port);//TODO set up way to specify server
		
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
					outString += pathway.getLocalID() + "\t" + gene + "\tx\tx\n";
				}
				for (String gene : cornGenes) {
					outString += pathway.getLocalID() + "\t" + gene + "\tx\t\n";
				}
				for (String gene : maizeGenes) {
					outString += pathway.getLocalID() + "\t" + gene + "\t\tx\n";
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
