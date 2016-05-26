package edu.iastate.CornCompare;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PathwayItem {
	public String frameID;
	public String comparableField;
	public String commonName;

	// ComparableField in the case for pathways is expected to be the frameID
	public PathwayItem (String frameID, String comparableField, String commonName) {
		this.frameID = frameID;
		this.comparableField = comparableField;
		this.commonName = commonName;
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
    
    @Override
    public String toString() {
    	return frameID + " : " + comparableField;
    }
}
