package edu.usp.ime.revolution.scm;

import java.util.Calendar;

public interface ChangeSet {

	String getId();
	String getPath();
	
	Calendar getTime();
}
