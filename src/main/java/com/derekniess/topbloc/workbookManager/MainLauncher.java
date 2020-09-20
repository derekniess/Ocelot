package com.derekniess.topbloc.workbookManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

/**
 * A class for sending challenge results to a TopBloc server for evaluation.
 * @author Derek
 *
 */
public class MainLauncher {
	private static Logger logger = LogManager.getLogger(MainLauncher.class);
	
	private static String basePath = "src/main/resources/";
	private static List<String> defaultFileNames = Arrays.asList("Test_Scores.xlsx", "Test_Retake_Scores.xlsx", "Student_Info.xlsx");

	public static void main(String[] args) throws IOException, InterruptedException {
    	Configurator.setRootLevel(Level.INFO);
    	
    	List<String> studentIDs;
    	double average = 0.0;
    	
    	WorkbookManager workbookManager = new WorkbookManager();
    	
    	logger.info("About to parse files...");
    	for (String fileName : defaultFileNames) {
    		try {
    			workbookManager.extractor.parseFile(basePath+fileName);
    			logger.info("File "+fileName+" successfully parsed!");
    			workbookManager.printTable();
    			average = workbookManager.aggregator.getAverage();
    			logger.info("Running average: " + average);
    		} catch (IOException e) {
    			logger.error(e);
    			return;
    		}
    	}
    	
    	studentIDs = workbookManager.aggregator.getFemaleCSStudents();
    	logger.info("Female CS students found: " + studentIDs);
    	
    	CloseableHttpClient client = HttpClients.createDefault();
    	
    	try {
    	    HttpPost httpPost = new HttpPost("https://postman-echo.com/post"); 

    	    StringBuilder jsonBuilder = new StringBuilder();
    	    jsonBuilder.append("{\"id\": \"dniess2@illinois.edu\", ");
    	    jsonBuilder.append( "\"name\": \"Derek Niess\", ");
    	    jsonBuilder.append( String.format("\"average\": %d, ", (int)average));
    	    jsonBuilder.append( String.format("\"studentIds\": %s}", studentIDs.toString()));
    	    String payload = jsonBuilder.toString();
    	    logger.info("JSON payload to be sent: " + payload);
    	    
    	    StringEntity entity = new StringEntity(payload.toString());
    	    httpPost.setEntity(entity);
    	    httpPost.setHeader("Accept", "application/json");
    	    httpPost.setHeader("Content-type", "application/json");
    	 
    	    CloseableHttpResponse response = client.execute(httpPost);
    	    if (response.getStatusLine().getStatusCode() == 200)
    	    	logger.info("POST successful.");
    	    else
    	    	logger.error("POST failed with status code: " + response.getStatusLine().getStatusCode());
    	    
    	    logger.info(response.toString());
    	    
    	} catch (Exception e) {
    		logger.error(e);
    	} finally {
    		client.close();
    	} 	
        return;
	}
}
