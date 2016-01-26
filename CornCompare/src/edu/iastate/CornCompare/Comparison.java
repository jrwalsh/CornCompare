package edu.iastate.CornCompare;

import java.util.ArrayList;

public class Comparison<T> {
	public ArrayList<T> matchedClasses = new ArrayList<T>();
	public ArrayList<T> uniqueClassesA = new ArrayList<T>();
	public ArrayList<T> uniqueClassesB = new ArrayList<T>();
	public ArrayList<T> matchedInstances = new ArrayList<T>();
	public ArrayList<T> uniqueInstancesA = new ArrayList<T>();
	public ArrayList<T> uniqueInstancesB = new ArrayList<T>();
	
	public Comparison(ArrayList<T> matchedClasses, ArrayList<T> uniqueClassesA, ArrayList<T> uniqueClassesB, 
			ArrayList<T> matchedInstances, ArrayList<T> uniqueInstancesA, ArrayList<T> uniqueInstancesB) {
		this.matchedClasses = matchedClasses;
		this.uniqueClassesA = uniqueClassesA;
		this.uniqueInstancesB = uniqueInstancesB;
		this.matchedInstances = matchedInstances;
		this.uniqueInstancesA = uniqueInstancesA;
		this.uniqueInstancesB = uniqueInstancesB;
	}
}
