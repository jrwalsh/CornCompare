package edu.iastate.CornCompare;

import java.util.ArrayList;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ReactionItem {
	public ArrayList<String> frameID = new ArrayList<String>();
	public String comparableData;
	
	// ComparableField in the case for reactions is expected to be the ???
	public ReactionItem (String frameID, String comparableData) {
		this.frameID.add(frameID);
		this.comparableData = comparableData;
	}
	
	public void addFrameID(String frameID) {
		this.frameID.add(frameID);
	}
	
	@Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
            append(comparableData).
            toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
       if (!(obj instanceof ReactionItem))
            return false;
        if (obj == this)
            return true;

        ReactionItem rhs = (ReactionItem) obj;
        return new EqualsBuilder().
            append(comparableData, rhs.comparableData).
            isEquals();
    }
    
    @Override
    public String toString() {
    	return frameID + " : " + comparableData;
    }
}
