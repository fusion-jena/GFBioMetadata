package de.unijena.cs.fusion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Utils {
	
	
	//print input stream
    //code from https://mkyong.com/java/java-read-a-file-from-resources-folder/
    //adapted to a textBuilder
    //MIT License
    //Copyright (c) 2020 Mkyong.com
    public static String printInputStream(InputStream is) {

    	StringBuilder textBuilder = new StringBuilder();
        try (InputStreamReader streamReader =
                    new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {

            String line;
            while ((line = reader.readLine()) != null) {
                //System.out.println(line);
            	textBuilder.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return textBuilder.toString();
    }
    
    // read the properties
    public static Properties readProperties(InputStream is) {

    	final Properties p = new Properties();
        try (InputStreamReader streamReader =
                    new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {

            p.load(reader);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return p;
    }

}
