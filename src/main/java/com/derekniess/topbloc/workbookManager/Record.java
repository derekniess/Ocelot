package com.derekniess.topbloc.workbookManager;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class Record {
	private String studentID;
	private Integer testScore;
	private String gender;
	private String major;
	
	public Record(String id) {
		this.studentID = id;
		this.testScore = null;
		this.gender = null;
		this.major = null;
	}
	
	@Override
	public String toString() {
		return "studentID : " + this.studentID + ", testScore : " + this.testScore + ", gender : " + this.gender + ", major : " + this.major;
	}
	
	public Integer getTestScore() {
		return this.testScore;
	}
	
	public String getGender() {
		return this.gender;
	}
	
	public String getMajor() {
		return this.major;
	}
	
	// For debugging and printing purposes.
	public static ArrayList<String> getFieldTypes() {
		Record tempRecord = new Record("");
		ArrayList<String> fieldTypes = new ArrayList<String>();
		for(Field field : tempRecord.getClass().getDeclaredFields()) {
			fieldTypes.add(field.getName());
		}
		tempRecord = null;
		return fieldTypes;
	}
	
	public void setAllFields(Integer testScore, String gender, String major) {
		/* TODO: Debating on using a hashtable as one param versus several objects as params.
		 * Pros: Scales cleanly with future columns. Just add if-branch for new record column. 
		 *       Easier preparation for client. No need for client to check fields beforehand.
		 * Cons: Creating a hashtable prior to each record looks excessive.
		 *       For the scope of this project, 3 objects is not too verbose.
		 */
		if(testScore != null)
			// Only want to keep the highest test score of any student.
			this.testScore = Math.max(this.testScore == null ? 0 : this.testScore, testScore);
		if(gender != null)
			this.gender = gender;
		if(major != null)
			this.major = major;
	}
}
