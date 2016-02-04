package edu.iastate.CornCount;
import java.util.ArrayList;

import edu.iastate.javacyco.Frame;

/**
 * The Counts class is a simple container to store and pass along simple database statistics about a generic group of Frames.
 * Includes a descriptive title and the list of frames that were counted.
 * @author Jesse
 *
 */
public class CountedFrames {
	public String groupTitle;
	public ArrayList<Frame> countedFrames;
	
	public CountedFrames (String groupTitle, ArrayList<Frame> countedFrames) {
		this.groupTitle = groupTitle;
		this.countedFrames = countedFrames;
	}
}
