package edu.iastate.CornCompare;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import edu.iastate.javacyco.Frame;

public class FrameList <T> {
	public Set<Frame> rawList;
	public ArrayList<T> classList;
	public ArrayList<T> instanceList;
	
	public FrameList () {
		this.rawList = new HashSet<Frame>();
		this.classList = new ArrayList<T>();
		this.instanceList = new ArrayList<T>();
	}
}
