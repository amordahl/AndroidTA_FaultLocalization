package edu.utdallas.amordahl;

import java.lang.reflect.Field;

import edu.utdallas.objectutils.InclusionPredicate;
import edu.utdallas.objectutils.Wrapped;
import edu.utdallas.objectutils.Wrapper;

public class WrappedAndLocation {
	// Class for a record that contains a wrapped object and a location.
	
	public Wrapped getReference() {
		return reference;
	}

	public String getLocation() {
		return location;
	}

	private Wrapped reference;
	private String location;
	
	public WrappedAndLocation(Object[] obj, String location) throws Exception {
		reference = Wrapper.wrapObject(obj, new InclusionPredicate() {
			@Override
			public boolean test(Field field) {
				// TODO Auto-generated method stub
				return true;
			}
			
		});
		this.location = location;
	}
	
	public WrappedAndLocation(Wrapped obj, String location) throws Exception {
		reference = obj;
		this.location = location;
	}
}
