package edu.iastate.CornCompare;

import java.util.ArrayList;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PathwayItem {
	public ArrayList<String> frameID = new ArrayList<String>();
	public String comparableField;
	
	// ComparableField in the case for reactions is expected to be the ???
	public PathwayItem (String frameID, String comparableField) {
		this.frameID.add(frameID);
		this.comparableField = comparableField;
	}
	
	@Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
            append(comparableField).
            toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
       if (!(obj instanceof PathwayItem))
            return false;
        if (obj == this)
            return true;

        PathwayItem rhs = (PathwayItem) obj;
        return new EqualsBuilder().
            append(comparableField, rhs.comparableField).
            isEquals();
    }
    
    public void addFrameID(String frameID) {
		this.frameID.add(frameID);
	}
    
    @Override
    public String toString() {
    	return frameID + " : " + comparableField;
    }
}
