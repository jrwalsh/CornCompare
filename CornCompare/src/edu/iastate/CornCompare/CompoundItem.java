package edu.iastate.CornCompare;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CompoundItem {
	public String frameID;
	public String comparableField;
	
	// ComparableField in the case for compounds is expected to be the inchi string (fallback is commonName)
	public CompoundItem (String frameID, String comparableField) {
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
       if (!(obj instanceof CompoundItem))
            return false;
        if (obj == this)
            return true;

        CompoundItem rhs = (CompoundItem) obj;
        return new EqualsBuilder().
            append(comparableField, rhs.comparableField).
            isEquals();
    }
    
    @Override
    public String toString() {
    	return frameID + " : " + comparableField;
    }
}
