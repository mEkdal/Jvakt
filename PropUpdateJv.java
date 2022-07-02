package Jvakt;
/*
 * 2022-06-23 V.54 Michael Ekdal		Added getVersion() to get at consistent version throughout all classes.
 */

import java.io.*;
import java.io.InputStream;
import java.net.*;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.apache.commons.codec.binary.Hex;

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
	static Boolean swUTF8BOM = false;
	static Boolean swUTF16BEBOM = false;
	static Boolean swUTF16LEBOM = false;
	static Boolean swCharset = false;
	static BufferedWriter out;
	static String charset = "UTF8";
	static int line = 0;

	public static void main(String[] args ) throws IOException, UnknownHostException {

		String version = "PropUpdateJv ";
		version += getVersion()+".54";
		System.out.println("----- Jvakt: "+new Date()+"  Version: "+version);

		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-filename")) fileName = args[++i];
			if (args[i].equalsIgnoreCase("-encode")) {
				swEncode = true;
				System.out.println("-encode : "+swEncode);
			}
			if (args[i].equalsIgnoreCase("-decode")) {
				swDecode = true;
				System.out.println("-decode : "+swDecode);
			}
			if (args[i].equalsIgnoreCase("-charset")) {
				charset = args[++i];
				System.out.println("-charset : "+charset);
				swCharset = true;
			}
			if (args[i].equalsIgnoreCase("-mode")) { 
				swMode = true;
				newmode = args[++i];
				if (newmode.compareToIgnoreCase("active") != 0 && newmode.compareToIgnoreCase("dormant") != 0) {
					newmode="active";
				}
				System.out.println("-mode : "+newmode);
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
					+ "\n-decode   \tUsed to decode the passwords found in the properties file."
					+ "\n-charset  \tDefault input file charset is UTF8. It could be UTF-16, UTF_16BE, UTF_16LE, UTF-32, ASCII, ISO8859_1..."
					+ "\n          \tIf not used the PropUpdateJv will try to figure out the proper charset."
					+ "\n          \tThe output file charset will always be UTF8!"
					+ "\n          \thttps://docs.oracle.com/javase/8/docs/technotes/guides/intl/encoding.doc.html"
					);
			System.exit(4);
		}



		if (config == null ) 	configF = new File(fileName);
		else 					configF = new File(config,fileName);
		
		System.out.println("-config file: "+configF);

		if (!swCharset) checkFileForBOM(); 


		//		BufferedReader in = new BufferedReader(new FileReader(configF));
		FileInputStream fis = new FileInputStream(configF); 
		//		InputStreamReader isr = new InputStreamReader(fis, "UTF8"); 
		//		InputStreamReader isr = new InputStreamReader(fis,StandardCharsets.UTF_16LE); 
		InputStreamReader isr = new InputStreamReader(fis,charset); 
		//		System.out.println("Encoding "+isr.getEncoding());
		BufferedReader in = new BufferedReader(isr);

		configFo = new File(configF +".new.properties");

		FileOutputStream fos = new FileOutputStream(configFo, false);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
		out = new BufferedWriter(osw);


		while ((s = in.readLine()) != null) {
			line++;
			swComment=false;
			s = s.trim();

//			System.out.println(s.length() +"  -  "+s);

//			if (line==1) {
//
//				if (s.length() >= 2) { 
//					if (s.substring(0,1).matches("[^a-zA-Z0-9:;_%@#/><åäöÅÄÖ\"\\,\\.\\!\\?\\*\\$\\)\\(\\-\\=\\{\\}\\]\\[]") &&
//							s.substring(1,2).matches("[^a-zA-Z0-9:;_%@#/><åäöÅÄÖ\"\\,\\.\\!\\?\\*\\$\\)\\(\\-\\=\\{\\}\\]\\[]") 	
//							)  {
//						System.out.println("Length:"+s.length() +"  Data:"+s);
//						System.out.println("1==>"+s.substring(0, 1));
//						System.out.println("2==>"+s.substring(1, 2));
//						System.out.println("3==>"+s.substring(2, 3)); 
//						System.out.println("\nFailure in file syntax!");
//						System.out.println("\nAre you sure the input file Encoding is "+isr.getEncoding()+"?");
//						System.exit(12);
//
//					}
//				}
//
//			}

			if (s.length() <= 4) {
				swComment=true; 
			}
			else if (s.startsWith("#")) {
				swComment=true; 
			}
			else if (s.indexOf('#') >= 0 && s.indexOf('#') <= 1) {
				swComment=true;  
			}
			else if (s.indexOf('=') < 0) { 
				swComment=true;  
			}

			if (!swComment) {
				try {
					tab = s.split("=" , 2);
					propname = tab[0].trim();
					propvalue = tab[1].trim();
				} catch (Exception e) {
					System.out.println("\n"+s);
					System.out.println("\nFailure in file syntax or charset!");
					System.out.println("\nAre you sure the input file Encoding is "+isr.getEncoding()+"?");
					System.out.println("\n"+e);
					System.exit(12);
				} 
				//				System.out.println("propname:"+propname+"  propvalue:"+propvalue);
				if (propname.equalsIgnoreCase("mode") && swMode) {
					System.out.println("SET "+propname+" to "+newmode);
					s=propname+" = "+newmode;
				}
				// encode or decode dbpassword
				if (propname.equalsIgnoreCase("dbpassword") && swEncode && !propvalue.startsWith("==y")) {
					System.out.println("Encoded "+propname);
					String encodedString = Base64.getEncoder().encodeToString(propvalue.getBytes());
					encodedString="==y"+encodedString;
					s=propname+" = "+encodedString;
				}
				if (propname.equalsIgnoreCase("dbpassword") && swDecode && propvalue.startsWith("==y")) {
					System.out.println("Decoded "+propname);
					byte[] decodedBytes = Base64.getDecoder().decode(propvalue.substring(3));
					String decodedString = new String(decodedBytes);
					s=propname+" = "+decodedString;
				}
				// encode or decode smtppwd
				if (propname.equalsIgnoreCase("smtppwd") && swEncode && !propvalue.startsWith("==y")) {
					System.out.println("Encoded "+propname);
					String encodedString = Base64.getEncoder().encodeToString(propvalue.getBytes());
					encodedString="==y"+encodedString;
					s=propname+" = "+encodedString;
				}
				if (propname.equalsIgnoreCase("smtppwd") && swDecode && propvalue.startsWith("==y")) {
					System.out.println("Decoded "+propname);
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
	
	static void checkFileForBOM() {

		byte[] bom = new byte[3];

		Path path = Paths.get(configF.getPath());

//		if(!configF.exists()){
//			throw new IllegalArgumentException("Path: " + configF + " does not exists!");
//		}

		try {
			InputStream is = new FileInputStream(path.toFile()); 

			is.read(bom);
			
	          String content = new String(Hex.encodeHex(bom));
//	          System.out.println("content "+content);
	          if ("00".equalsIgnoreCase(content.substring(0, 2))) {
	        	  System.out.println("** Found out the input file probably is UCS-2 BE without BOM. Trying -charset UTF-16");
	        	  swUTF16BEBOM = true;
	        	  charset = "UTF-16";
	          }
	          if ("feff".equalsIgnoreCase(content.substring(0, 4))) {
	        	  System.out.println("** Found the input file to be UCS-2 BE BOM. Using -charset UTF-16");
	        	  swUTF16BEBOM = true;
	        	  charset = "UTF-16";
	          }
	          if ("00".equalsIgnoreCase(content.substring(2, 4))) {
	        	  System.out.println("** Found out the input file probably is UCS-2 LE without BOM. Trying -charset UTF-16");
	        	  swUTF16LEBOM = true;
	        	  charset = "UTF-16";
	          }
	          if ("fffe".equalsIgnoreCase(content.substring(0, 4))) {
	        	  System.out.println("** Found the input file to be UCS-2 LE BOM. Using -charset UTF-16");
	        	  swUTF16LEBOM = true;
	        	  charset = "UTF-16";
	          }
	          if ("efbbbf".equalsIgnoreCase(content.substring(0, 6))) {
	        	  System.out.println("** Found the input file to be UCS8 BOM. Using -charset UTF8");
	        	  swUTF8BOM = true;
	        	  charset = "UTF8";
	          }
			
			
			is.close();
		} catch (FileNotFoundException fnfe) {
			System.out.println("File not found "+configF.getPath()+"\n"+fnfe);
		}
		catch (IOException ioe) {
			System.out.println("IO error "+configF.getPath()+"\n"+ioe);
		}
	}

	static private String getVersion() {
		String version = "0";
		try { 
			Class<?> c1 = Class.forName("Jvakt.Version",false,ClassLoader.getSystemClassLoader());
			Version ver = new Version();
			version = ver.getVersion();
 		} 
		catch (java.lang.ClassNotFoundException ex) {
			version = "?";
		}
		return version;
	}

}