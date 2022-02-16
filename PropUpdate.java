package Jvakt;
import java.io.*;
import java.net.*;
import java.util.*;
//import java.time.*;

public class PropUpdate {

	static String newmode = "";
	static String oldmode = "";
	static String config = null;
	static File configF;

	public static void main(String[] args ) throws IOException, UnknownHostException {

		String version = "PropUpdate 1.0 # 2018-05-07";

		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-mode")) newmode = args[++i];
		}
 
		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");
		System.out.println("----- Jvakt: "+new Date()+"  Version: "+version);
		System.out.println("-config file: "+configF);
		
		Properties prop = new Properties();

		InputStream input = null;
		try {
		input = new FileInputStream(configF);
		prop.load(input);
		// get the property value and print it out
		oldmode 	 =  prop.getProperty("mode");
		System.out.println("Old mode: " + oldmode);
		input.close();
		} catch (IOException ex) {
    		ex.printStackTrace();
    	}
		
//		System.exit(0);
		
		OutputStream output = null;
//		FileWriter output = null;
		try {
		output = new FileOutputStream(configF);
//		output = new FileWriter(configF);
		// get the property value and print it out
		System.out.println("New mode: " + newmode);
		prop.setProperty("mode",newmode);
		prop.store(output,null);
		output.close();
		} catch (IOException ex) {
    		ex.printStackTrace();
    	}
		
		
	}
}