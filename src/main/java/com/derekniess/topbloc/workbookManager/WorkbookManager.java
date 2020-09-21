package com.derekniess.topbloc.workbookManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class WorkbookManager {	
	private static Logger logger = LogManager.getLogger(WorkbookManager.class);
	
	private HashMap<String, Record> records; // Acts as in-memory storage. Could replace with Redis.
	public Extractor extractor;
	public Aggregator aggregator;
	
	public WorkbookManager() {
		this.records = new HashMap<String, Record>();
		this.extractor = new Extractor();
		this.aggregator = new Aggregator();
	}
	
	/**
	 * Prints a pretty table of all of the records in memory associated with this manager.
	 */
	public void printTable() {
		System.out.println();
		Record.getFieldTypes().forEach(fieldType -> {System.out.printf("%-20s", fieldType);});
		System.out.println("\n--------------------------------------------------------------------------------");
		
		records.forEach((id, record) -> {
			// Only grab values of each record.
			for(String value : record.toString().split("(,\\s|^)[\\w]+\\s:\\s"))
				if (!value.isEmpty())
					System.out.printf("%-20s", value);
			System.out.println();	
		});
	}
	
	// A component for extracting relevant data from a file and importing as records in memory.
	public class Extractor {
		/**
		 * Parses an XLS/XLSX file for relevant student information and commits to key-value store.
		 * @param filename
		 * @throws IOException
		 */
		public void parseFile(String filename) throws IOException {
			logger.info("Parsing file " + filename + " ... ");
			int dRecordsAdded = 0;
			int dRecordsModified = 0;
			
		    File excelFile = new File(filename);
		    FileInputStream fileStream = new FileInputStream(excelFile);
		    XSSFWorkbook workbook = new XSSFWorkbook(fileStream);
		    XSSFSheet sheet = workbook.getSheetAt(0);
		    logger.info("File opened successfully.");
		    
		    try {
		    	Iterator<Row> rowIt = sheet.iterator();
		    
		    	// Grab all column headers of excel sheet.
		    	List<String> columnTypes = new ArrayList<String>();
		    	if (rowIt.hasNext()) {
		    		Row row = rowIt.next();
		    		Iterator<Cell> cellIterator = row.cellIterator();
			
		    		while (cellIterator.hasNext()) {
		    			Cell cell = cellIterator.next();
		    			columnTypes.add(cell.toString());
		    		}
		    	}
		    	logger.info("Column types collected: " + columnTypes.toString());
		    
		    	if (!columnTypes.contains("studentId")){
		    		logger.info("Returning... File does not contain unique ID: studentId");
		    		return;
		    	}
		      
		    	// Extract data in excel line-by-line.
		    	while(rowIt.hasNext()) {
		    		Row row = rowIt.next();
			        Iterator<Cell> cellIterator = row.cellIterator();
				    String studentID = null; Integer testScore = null; String gender = null; String major = null;
				    
				    int index = 0;
				    // Only extract relevant column types.
			        while (cellIterator.hasNext()) {
			        	Cell cell = cellIterator.next();
			        	switch (columnTypes.get(index)) {
			            	case "studentId": studentID = Integer.toString((int)cell.getNumericCellValue()); break;
			            	case "score": testScore = Integer.valueOf((int)cell.getNumericCellValue()); break;
			            	case "gender": gender = cell.toString(); break;
			            	case "major": major = cell.toString(); break;
			        	}
			        	index++;
			        }
			        
				    // Create or update existing record data.
			        Record record = WorkbookManager.this.records.get(studentID);
			        if (record == null) {
			        	record = new Record(studentID);
			        	logger.info("New record created: " + record);
			        	dRecordsAdded++;
			        	dRecordsModified--;
			        }
			        logger.info("Record prior state: "+record);
			        record.setAllFields(testScore, gender, major);
			        dRecordsModified++;
			        logger.info("Record new state: "+record);
			    	WorkbookManager.this.records.put(studentID, record);
		    	}	
		    	logger.info("Parsing complete. Added " + dRecordsAdded + " records and modified " + dRecordsModified + " records.");
			} catch (Exception e) {
				logger.error(e);
				
			} finally {
				workbook.close();
			    fileStream.close();
			}
    	}
	}
	
	// A component for performing aggregation queries on record data.
	public class Aggregator {
		/**
		 * Calculates the average test score of the student body in the key-value store.
		 * @return Student body average as a double.
		 */
		public double getAverage() {
			double sum = 0.0;
			int count = 0;
			
			for(Record record : WorkbookManager.this.records.values()) {
				if(record.getTestScore() != null) {
					sum += record.getTestScore();
					count++;
				}
			}
			return sum / count;
		}
		
		/**
		 * Finds the subset of students who are female and computer science majors in ascending order.
		 * @return subset of student IDs as a List.
		 */
		public List<String> getFemaleCSStudents() {
			List<String> studentIDs = new ArrayList<String>();
			
			for(Map.Entry<String, Record> row : WorkbookManager.this.records.entrySet()) {
				Record record = row.getValue();
				if(record.getGender().equals("F") && record.getMajor().contentEquals("computer science"))
					studentIDs.add(row.getKey());
			}
			Collections.sort(studentIDs);
			return studentIDs;
		}
	}
}
