package de.unijena.cs.fusion;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;


public class GFBioDatasets {	
	
	//configure your output path in this config file
	public static String config = "config.properties"; 
	
	//put all your queries into src/main/resources (default resource folder)
	public static String QUERY = "query.json"; 
	
	//properties 
	public static Properties properties = null;
	
	
	public static void main(String[] args) throws Exception {
	  
		//start the app and load the query and the properties
		GFBioDatasets app = new GFBioDatasets();
		
		InputStream is = app.getFileFromResourceAsStream(QUERY);
        String searchQuery = Utils.printInputStream(is);
        
        InputStream isProp = app.getFileFromResourceAsStream(config);
        properties = Utils.readProperties(isProp);
        
        //System.out.println(properties);
		getAllGFBioDatatsets(searchQuery);
	}

	/**
	 * gets all GFBio datasets based on the given query, the retrieved XML files are stored in the configured output folder
	 * @param query
	 * @throws Exception
	 */
	private static void getAllGFBioDatatsets(String query) throws Exception{
		//String host_scrollID = "http://ws.pangaea.de/es/dataportal-gfbio/pansimple/_search?scroll=1m";
		//String host_search = "http://ws.pangaea.de/es/_search/scroll";
		String host_scrollID = properties.getProperty("host_scrollID");
		String host_search = properties.getProperty("host_search");
		
		String scroll_id = null;
		String parameter1 = null;
		
		//counterMax = maximum request (e.g., 10,100,1000), see getScrollId.json for setting the size per request
		int counterMax = Integer.valueOf(properties.getProperty("counterMax")); // 10 requests and 1000 datasets per request (see getScrollId.json) =  10000 files
		int counter = Integer.valueOf(properties.getProperty("counter"));
		
		//Read parameter 1 from file
		Gson gson = new Gson();
		
        JsonObject obj = (JsonObject) gson.fromJson(query, JsonObject.class);
		parameter1 = obj.toString();
		//System.out.println(obj.toString());
		
		//get scroll Id 
		JSONObject resultGetScrollId = callGFBioSearchAPI(host_scrollID, parameter1);
        scroll_id = resultGetScrollId.getString("_scroll_id");
        String parameter2="{ \"scroll\":\"1m\",\"scroll_id\":\""+scroll_id+"\"}";
		
        //System.out.println(parameter2);
        JSONObject result = callGFBioSearchAPI(host_search, parameter2);
        
        while(result.length()>0 && counter <=counterMax){
        	result = callGFBioSearchAPI(host_search, parameter2);
        	//System.out.println(result);
        	
        	JSONObject hits = result.getJSONObject("hits");            
            JSONArray arrayHits = hits.getJSONArray("hits");
            
            
           for(int i=0; i< arrayHits.length(); i++){      	
			 
        	 //get _source
           	 JSONObject o = arrayHits.getJSONObject(i);
           	 if(o !=null && o.getJSONObject("_source")!=null){
               	 JSONObject fields = o.getJSONObject("_source");
               	 
               	 
               	 //xml:String
               	 String xml = fields.getString("xml");
               	 String idS = o.getString("_id");
               	 //System.out.println(idS);
       	     	 
               	 //ID for collection data
               	 if(o.getString("_id").matches("urn:gfbio\\.org:abcd:.+")){
            		 String[] regex = o.getString("_id").split("urn:gfbio\\.org:abcd:");
            		 //System.out.println(regex[1]);
   	 
            		 if(regex!=null && regex.length>1){
            			 
            			 String[] abcdID = regex[1].split(":");
            			 idS= abcdID[1].replace("+", "").replace("/", "_");
            			 
            			 //System.out.println(idS);
            		 }
            		 
            	 }
               	 //ID for ENA data
               	 if(o.getString("_id").matches("oai:ena2pansimple\\.gfbio\\.org:[A-Za-z0-9]+")) {
               		String[] regex = o.getString("_id").split("oai:ena2pansimple\\.gfbio\\.org:");
           		    //System.out.println(regex[1]);
               		idS = regex[1];
               	 }
            	 
            	 //create file for each dataset
            	 createDatasetFile(idS, xml);
                     	               	
           	   }//end if
       	    }//end for 
       
            counter++;
        }//end while
	}
	
	/**
	 * call to the searchAPI
	 * @param host
	 * @param parameter
	 * @return JSONObject
	 * @throws Exception
	 */
	private static JSONObject callGFBioSearchAPI(String host, String parameter) throws Exception{
		JSONObject result =null;
		
        CloseableHttpClient httpclient = HttpClients.createDefault();
        StringEntity postingString =null;
		
        try {
        	HttpPost httppost;
        	
        	httppost = new HttpPost(host);
            postingString =new StringEntity(parameter);//convert your pojo to   json
        
           
           httppost.setEntity(postingString);
           httppost.setHeader("Content-type", "application/json");
           httppost.addHeader("Accept","application/json");
  
            System.out.println("Executing request: " + httppost.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httppost);
            HttpEntity responseEntity = null;
            
           
            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());
            //EntityUtils.consume(response.getEntity());
            responseEntity = response.getEntity();
            
            if(responseEntity!=null) {           	 
                result = new JSONObject(EntityUtils.toString(responseEntity));
            }
            
        }catch(Exception e){
        	e.printStackTrace();
        }finally {
            httpclient.close();
        }
		
		return result;
	}
	
	/**
	 * creates an XML file based on the retrieved string, file is stored in the configured output path (see src/main/resources/config.properties)
	 * default path: C:/tmp/output
	 * @param ID
	 * @param datasetString
	 */
	private static void createDatasetFile(String ID, String datasetString) {
		Writer writer = null;
		String datasetXMLStart = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		String outputPath = properties.getProperty("output");
		
		try {
		    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath+ID+".xml"), "utf-8"));
		    writer.write(datasetXMLStart);
		    writer.write(System.getProperty("line.separator"));
		    writer.write(datasetString);
		    
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
		   try {writer.close();} catch (Exception ex) {ex.printStackTrace();}
		}
		
	}
	
	/**
	 * get a file from the resources folder,     
	 * works everywhere, IDEA, unit test and JAR file.
	 * @param fileName
	 * @return
	 * code from https://mkyong.com/java/java-read-a-file-from-resources-folder/
     * MIT License
     * Copyright (c) 2020 Mkyong.com
	 */
    private InputStream getFileFromResourceAsStream(String fileName) {

        // The class loader that loaded the class
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }

    }
    
  }
