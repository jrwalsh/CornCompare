package edu.iastate.CornCompare;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class GeneItem {
	public String frameID;
	public String comparableField;

	// ComparableField in the case for genes is expected to be the commonName minus the suffix _P## or _T##
	public GeneItem (String frameID, String comparableField) {
		this.frameID = frameID;
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
       if (!(obj instanceof GeneItem))
            return false;
        if (obj == this)
            return true;

        GeneItem rhs = (GeneItem) obj;
        return new EqualsBuilder().
            append(comparableField, rhs.comparableField).
            isEquals();
    }
    
    @Override
    public String toString() {
    	return frameID + " : " + comparableField;
    }
}
