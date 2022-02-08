package Jvakt;
import java.io.*;
import java.net.*;
import java.util.*;
//import java.time.*;

public class PropUpdateJv {

	static String newmode = "";
	static String oldmode = "";
	static String config = null;
	static File configF;
	static File configFo;
	static String s;
	static String fileName = "Jvakt.properties";
	static String propname;
	static String propvalue;
	static String[] tab = new String [1];
	static Boolean swComment;
	static Boolean swMode = false;
	static Boolean swEncode = false;
	static Boolean swDecode = false;
	static Boolean swHelp = false;
	static BufferedWriter out;
	
	public static void main(String[] args ) throws IOException, UnknownHostException {

		String version = "PropUpdateJv # 2022-02-08";


		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-filename")) fileName = args[++i];
			if (args[i].equalsIgnoreCase("-encode")) swEncode = true;
//			if (args[i].equalsIgnoreCase("-decode")) swDecode = true;
			if (args[i].equalsIgnoreCase("-mode")) { 
				swMode = true;
				newmode = args[++i];
			}
			if (args[i].equalsIgnoreCase("-help")) swHelp = true;
			if (args[i].equalsIgnoreCase("-?")) swHelp = true;
		}

		if (swHelp || args.length==0) {
			System.out.println("--- " + version + " ---");
			System.out.println("by Michael Ekdal, Sweden.\n");
			System.out.println("To change values in the Jvakt properties files.");

			System.out
			.println("\nThe parameters and their meaning are:\n"
					+ "\n-config   \tThe directory of the input file. Like: \"-dir c:\\Temp\" "
					+ "\n-filename \tThe full filename of the file to update. The default value is \"Jvakt.properties\""
					+ "\n-mode     \tChange the mode value in the properties file to active or dormant. Like \"-mode dormant\""
					+ "\n-encode   \tUsed to encode the passwords found in the properties file."
					);
			System.exit(4);
		}

		
		
		if (config == null ) 	configF = new File(fileName);
		else 					configF = new File(config,fileName);
		System.out.println("----- Jvakt: "+new Date()+"  Version: "+version);
		System.out.println("-config file: "+configF);

		BufferedReader in = new BufferedReader(new FileReader(configF));

		configFo = new File(configF +".new.properties");

	  	FileOutputStream fis = new FileOutputStream(configFo, false);
		OutputStreamWriter osw = new OutputStreamWriter(fis, "UTF8");
	    out = new BufferedWriter(osw);


		while ((s = in.readLine()) != null) {
			swComment=false;
			System.out.println(s);
			System.out.println("swComment " +swComment);

			if (s.length() == 0) swComment=true; 
			if (s.startsWith("#")) swComment=true; 
			if (s.indexOf('#') ==1) swComment=true;

			if (!swComment) {
				System.out.println(s);
				System.out.println("swComment " +swComment);
				tab = s.split("=" , 2);
				propname = tab[0].trim();
				propvalue = tab[1].trim();
//				System.out.println("propname:"+propname+"  propvalue:"+propvalue);
				if (propname.equalsIgnoreCase("mode") && swMode) {
					System.out.println("SET "+propname+" to "+newmode);
					s=propname+" = "+newmode;
				}
				// encode or decode dbpassword
				if (propname.equalsIgnoreCase("dbpassword") && swEncode && !propvalue.startsWith("==y")) {
					System.out.println("Encode "+propname);
					String encodedString = Base64.getEncoder().encodeToString(propvalue.getBytes());
					encodedString="==y"+encodedString;
					s=propname+" = "+encodedString;
				}
				if (propname.equalsIgnoreCase("dbpassword") && swDecode && propvalue.startsWith("==y")) {
					System.out.println("Decode "+propname);
				    byte[] decodedBytes = Base64.getDecoder().decode(propvalue.substring(3));
				    String decodedString = new String(decodedBytes);
					s=propname+" = "+decodedString;
				}
				// encode or decode smtppwd
				if (propname.equalsIgnoreCase("smtppwd") && swEncode && !propvalue.startsWith("==y")) {
					System.out.println("Encode "+propname);
					String encodedString = Base64.getEncoder().encodeToString(propvalue.getBytes());
					encodedString="==y"+encodedString;
					s=propname+" = "+encodedString;
				}
				if (propname.equalsIgnoreCase("smtppwd") && swDecode && propvalue.startsWith("==y")) {
					System.out.println("Decode "+propname);
				    byte[] decodedBytes = Base64.getDecoder().decode(propvalue.substring(3));
				    String decodedString = new String(decodedBytes);
					s=propname+" = "+decodedString;
				}

			}
			out.write(s); 
			out.newLine();

		}
		in.close();
		out.close();

		if (configF.delete()) {
			configFo.renameTo(configF);
		}

	}
}