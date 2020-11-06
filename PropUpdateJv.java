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
	static String propname;
	static String propvalue;
	static String[] tab = new String [1];
	static Boolean swComment;
	static BufferedWriter out;
	
	public static void main(String[] args ) throws IOException, UnknownHostException {

		String version = "PropUpdateJv 1.0 # 2020-10-18";


		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			if (args[i].equalsIgnoreCase("-mode")) newmode = args[++i];
		}

		if (config == null ) 	configF = new File("Jvakt.properties");
		else 					configF = new File(config,"Jvakt.properties");
		System.out.println("----- Jvakt: "+new Date()+"  Version: "+version);
		System.out.println("-config file: "+configF);

		BufferedReader in = new BufferedReader(new FileReader(configF));

		configFo = new File(configF +".new.properties");

	  	FileOutputStream fis = new FileOutputStream(configFo, false);
		OutputStreamWriter osw = new OutputStreamWriter(fis, "UTF8");
	    out = new BufferedWriter(osw);


		while ((s = in.readLine()) != null) {
			swComment=false;
//			System.out.println(s);

			if (s.length() == 0) swComment=true; 
			if (s.startsWith("#")) swComment=true; 

			if (!swComment) {
				tab = s.split("=" , 2);
				propname = tab[0].trim();
				propvalue = tab[1].trim();
//				System.out.println("propname:"+propname+"  propvalue:"+propvalue);
				if (propname.equalsIgnoreCase("mode")) {
					System.out.println("SET "+propname+" to "+newmode);
					s=propname+" = "+newmode;
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