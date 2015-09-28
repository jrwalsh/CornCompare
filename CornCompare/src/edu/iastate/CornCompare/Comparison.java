package edu.iastate.CornCompare;

import java.util.ArrayList;

public class Comparison<T> {
	public ArrayList<T> matched = new ArrayList<T>();
	public ArrayList<T> uniqueListA = new ArrayList<T>();
	public ArrayList<T> uniqueListB = new ArrayList<T>();
	
	public Comparison(ArrayList<T> matched, ArrayList<T> uniqueListA, ArrayList<T> uniqueListB) {
		this.matched = matched;
		this.uniqueListA = uniqueListA;
		this.uniqueListB = uniqueListB;
	}
}
