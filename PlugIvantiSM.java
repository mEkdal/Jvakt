package Jvakt;

/*
 * 2023-04-06 V.3 Michael Ekdal		Completed the *DELETE function to close the incidents.  
 * 2023-03-28 V.2 Michael Ekdal		Added possibility to exclude all by default using E;* in the PlugIvantiSM.csv file  
 * 2022-08-11 V.1 Michael Ekdal		New plugin to update Ivanti Serrvice Manager with input from the console 
 */

import java.io.*;
import java.net.*;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.text.DateFormat;
import java.util.Date;
import java.text.SimpleDateFormat;


public class PlugIvantiSM {

	static String DBUrl = "jdbc:postgresql://localhost:5433/Jvakt";
	static Connection conn = null;

	static String version = "PlugIvantiSM ";
	static String database = "jVakt";
	static String dbuser   = "jVakt";
	static String dbpassword = "";
	static String dbhost   = "localhost";
	static String dbport   = "5433";
	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static int port ;

	static boolean swDormant = false;

	static String row = "";
	static FileOutputStream fis;
	static OutputStreamWriter osw;
	static BufferedWriter logg;

	static String id = null;
	static String status = "OK";
	static String body = " ";
	static String agent = " ";
	static String type = "R";  // repeating
	static String prio = "30";  
	static String recid;  
	static boolean swInsert = false;  
	static boolean swDelete = false;  
	static boolean swLogg = true;  
	static boolean swRun = true;  
	static boolean swShow = true;  
	static boolean swLogged = false;  
	static boolean swClosed = false;  
	static boolean swExcludeAll = false;  

	static String auth;
	static String host;
	static String path;
	static String ivaport;
	static String Category;
	static String Impact;
	static String OwnerTeam;
	static String ProfileLink_RecID;
	static String Service_Valid;
	static String Service;
	static String Source;
	static String Status2;
	static String Urgency;
	static String CauseCode;

	static String Resolution;
	static String FirstCallResolution;
	static String Owner;

	static String json_insert;
	static String json_patch;
	static String hosturl;
	static String inputLine;
	static boolean state = false;

	static String config = null;
	static File configF;
	static File ivantiPropF;



	static String[] etab; 
	static String[] ctab; 
	static String[] ltab; 
	static String[] etabSplit; 
	static String[] ltabSplit; 
	static String[] ctabSplit;
	static int ecount = 0;
	static int lcount = 0;
	static int ccount = 0;

	public static void main(String[] args ) throws UnknownHostException, IOException, Exception, FileNotFoundException {

		version += getVersion()+".3";

		if (args.length < 1) {
			System.out.println("\n " +version);
			System.out.println("The Ivanti rest API is used to post new incidents.");
			System.out.println("A plugin to update Ivanti Serrvice Manager with input from the console.");

			System.out.println("\n\nThe parameters and their meaning are:\n"+
					"\n-config \tThe dir of the input files. Like: \"-dir c:\\Temp\" "+
					"\n*INSERT \tIndicates it is an insert of a new incident into Ivanti."+
					"\n*DELETE \tIndicates it is a close of an incident. (not implemented yet) "+
					"\n-prio   \tField from the Jvakt console table."+
					"\n-type   \tField from the Jvakt console table."+
					"\n-sts    \tField from the Jvakt console table." +
					"\n-body   \tField from the Jvakt console table." +
					"\n-agent  \tField from the Jvakt console table." +
					"\n-recid  \tField from the Jvakt console table. Used with the *DELETE function" +
					"\n        \tthe recid field is populated by the *INSERT and used in the *DELETE function" +
					"\n-log    \tWrite to specific file. like \"-log c:\\logg.txt\" " +
					"\n-show   \tShows more in the log file."
					);

			System.exit(4);
		}

		for (int i=0; i<args.length; i++) {
			//			System.out.println(args[i]);
			if (args[i].equalsIgnoreCase("-id")) id = args[++i];
			else if (args[i].equalsIgnoreCase("-prio")) prio = args[++i];
			else if (args[i].equalsIgnoreCase("-type")) type = args[++i];
			else if (args[i].equalsIgnoreCase("-sts")) status = args[++i];
			else if (args[i].equalsIgnoreCase("-body")) body = args[++i];
			else if (args[i].equalsIgnoreCase("-agent")) agent = args[++i];
			else if (args[i].equalsIgnoreCase("*INSERT")) swInsert = true;
			else if (args[i].equalsIgnoreCase("*DELETE")) swDelete = true;
			else if (args[i].equalsIgnoreCase("-recid")) recid = args[++i];
			else if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			else if (args[i].equalsIgnoreCase("-norun")) swRun = false;
		}		

		fis = new FileOutputStream("PlugIvantiSM.log", true);
		osw = new OutputStreamWriter(fis, "Cp850");
		logg = new BufferedWriter(osw);

		if (config == null ) {
			configF = new File("Jvakt.properties");
			ivantiPropF = new File("PlugIvantiSM.properties");
		}
		else {
			configF = new File(config,"Jvakt.properties");
			ivantiPropF = new File(config,"PlugIvantiSM.properties");
		}

		System.out.println("----------- Jvakt PlugIvantiSM "+new Date()+"  Version: "+version +"  -  config file: "+configF);
		logg.write("----------- Jvakt PlugIvantiSM "+new Date()+"  Version: "+version +"  -  config file: "+configF);
		logg.newLine();

		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(configF);
			prop.load(input);
			// get the property value and print it out
			database = prop.getProperty("database");
			dbuser   = prop.getProperty("dbuser");
			dbpassword = prop.getProperty("dbpassword");
			if (dbpassword.startsWith("==y")) {
				byte[] decodedBytes = Base64.getDecoder().decode(dbpassword.substring(3));
				String decodedString = new String(decodedBytes);
				dbpassword=decodedString;
			}
			dbhost   = prop.getProperty("dbhost");
			dbport   = prop.getProperty("dbport");
			jvport   = prop.getProperty("jvport");
			jvhost   = prop.getProperty("jvhost");
			String	mode 	 =  prop.getProperty("mode");
			if (!mode.equalsIgnoreCase("active"))  swDormant = true;
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		if (swInsert) row += "*INSERT ";
		if (swDelete) row += "*DELETE ";
		row += " -id "+id;
		row += " -prio "+prio;
		row += " -type "+type;
		row += " -sts "+status;
		row += " -body "+body;
		row += " -agent "+agent;
		row += " -recid "+recid;
		row += " -config "+config;

		System.out.println(row);
		logg.write(row);
		logg.newLine(); 

		// Read the PlugIvantiSM.properties
		prop = new Properties();
		try {
			input = new FileInputStream(ivantiPropF);
			prop.load(input);
			auth = prop.getProperty("auth");
			host = prop.getProperty("host");
			path = prop.getProperty("path");
			ivaport = prop.getProperty("port");
			Category   = prop.getProperty("Category");
			Impact = prop.getProperty("Impact");
			OwnerTeam   = prop.getProperty("OwnerTeam");
			ProfileLink_RecID   = prop.getProperty("ProfileLink_RecID");
			//			Service_Valid   = prop.getProperty("Service_Valid");
			Service   = prop.getProperty("Service");
			Source   = prop.getProperty("Source");
			Status2  = prop.getProperty("Status");
			Urgency   = prop.getProperty("Urgency");
			CauseCode   = prop.getProperty("CauseCode");
			Resolution   = prop.getProperty("Resolution");
			FirstCallResolution   = prop.getProperty("FirstCallResolution");
			Owner   = prop.getProperty("Owner");
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		//		row = "";
		//		row += " auth="+auth;
		//		row += " host="+host;
		//		row += " ivaport="+ivaport;
		//		row += " Category="+Category;
		//		row += " Impact="+Impact;
		//		row += " OwnerTeam="+OwnerTeam;
		//		row += " ProfileLink_RecID="+ProfileLink_RecID;
		//		row += " Service="+Service;
		//		row += " Service_Valid="+Service_Valid;
		//		row += " Source="+Source;
		//		row += " Urgency="+Urgency;
		//		System.out.println(row);


		if (prio.compareTo("30") >= 0) Urgency = "Low";
		else if (prio.compareTo("20")>= 0 && prio.compareTo("30")<0) Urgency = "Medium";
		else Urgency = "High";

		if (getCsv()) System.out.println("-Filter file found"); 
		else {
			logg.write("-No Filter file found");
			logg.newLine();
			System.out.println("-No Filter file found"); 
		}

		//		System.out.println("ecount "+ecount+" ccount "+ccount+" lcount "+lcount); 
		// Checking if ID is to be excluded from creating an incident in Ivanti
		for ( int k = 0; k < ecount ; k++) {  
			etabSplit = etab[k].split("&");
			int eTabWarn= 0;
			for ( int j = 0; j < etabSplit.length ; j++) { 
				if (id.toUpperCase().indexOf(etabSplit[j]) >= 0) eTabWarn++;
			}
			if (eTabWarn == etabSplit.length) {
				logg.write("-- Filter Excluded :"+id);
				logg.newLine();
				System.out.println("-- Filter Excluded: "+id);
				System.exit(4);
			}
		}

		// Checking if incident is to be "Closed" 
		for ( int k = 0; k < ccount ; k++) {  
			ctabSplit = ctab[k].split("&");
			int cTabWarn= 0;
			for ( int j = 0; j < ctabSplit.length ; j++) { 
				if (id.toUpperCase().indexOf(ctabSplit[j]) >= 0) cTabWarn++;
			}
			if (cTabWarn == ctabSplit.length) {
				logg.write("-- Filter set "+id+" to Closed");
				logg.newLine();
				swClosed=true;
				swExcludeAll=false;
				System.out.println("-- Filter set "+id+" to Closed");
			}
		}

		// Checking if incident is to be "Logged" 
		for ( int k = 0; k < lcount ; k++) {  
			ltabSplit = ltab[k].split("&");
			int lTabWarn= 0;
			for ( int j = 0; j < ltabSplit.length ; j++) { 
				if (id.toUpperCase().indexOf(ltabSplit[j]) >= 0) lTabWarn++;
			}
			if (lTabWarn == ltabSplit.length) {
				logg.write("-- Filter set "+id+" to Logged");
				logg.newLine();
				swLogged=true;
				swClosed=false;
				swExcludeAll=false;
				System.out.println("-- Filter set "+id+" to Logged");
			}
		}

		if (swClosed) Status2 = "Closed";
		else if (swLogged) Status2 = "Logged";

		json_insert = "{\n"+
				" \"Category\":\""+ Category+"\",\r\n" +
				" \"Impact\":\""+Impact+"\",\r\n" +
				" \"OwnerTeam\":\""+OwnerTeam+"\",\r\n" +
				" \"ProfileLink_RecID\":\""+ProfileLink_RecID+"\",\r\n" +
				" \"Service\":\""+Service+"\",\r\n" +
				" \"CauseCode\":\""+CauseCode+"\",\r\n" +
				" \"Resolution\":\""+Resolution+"\",\r\n" +
				" \"Urgency\":\""+Urgency+"\",\r\n" +
				" \"FirstCallResolution\":\""+FirstCallResolution+"\",\r\n" +
				" \"Owner\":\"\",\r\n" +
				" \"Source\":\""+Source+"\",\r\n" +
				" \"Subject\":\""+id+" - "+status+" -\",\r\n" +
				" \"Symptom\":\""+body+"\",\r\n" +
				" \"Status\":\""+Status2+"\"\r\n" +
				"}";

		System.out.println(json_insert);
		logg.write(json_insert);
		logg.newLine(); 

		json_patch = "{\n"+
				" \"Status\":\"Closed\"\r\n" +
				"}";

		System.out.println(json_patch);
		logg.write(json_patch);
		logg.newLine(); 

		//		System.exit(4);

		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		}
		};

		// Install the all-trusting trust manager
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};

		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);




		if (swInsert && !swExcludeAll) {

			if (post2Ivanti()) {  

				try {
					String SQL_UPDATE;
					PreparedStatement StmtUpdate;
					Class.forName("org.postgresql.Driver").newInstance();
					DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
					conn = DriverManager.getConnection(DBUrl,dbuser,dbpassword);
					conn.setAutoCommit(false);

					SQL_UPDATE ="UPDATE CONSOLE SET RECID=? WHERE ID ilike '"+id+"' AND PRIO="+Integer.parseInt(prio)+" AND TYPE='"+type+"'"+" AND BODY ilike '"+body+"'";
					System.out.println(new Date()+ " > "+SQL_UPDATE);
					logg.write(new Date()+" > "+SQL_UPDATE);
					logg.newLine(); 

					StmtUpdate = conn.prepareStatement(SQL_UPDATE);
					StmtUpdate.setString(1, recid);
					System.out.println(new Date()+" Updated records > "+StmtUpdate.executeUpdate());
					logg.write(new Date()+" Updated records > "+StmtUpdate.executeUpdate());
					logg.newLine();
					StmtUpdate.close();
					conn.commit();
					conn.close();
				}
				catch (SQLException e) {
					System.out.println(new Date()+" SQLExeption ");
					System.err.println(e);
					System.err.println(e.getMessage());
				}
				catch (Exception e) {
					System.out.println(new Date()+" Exeption");
					System.err.println(e);
					System.err.println(e.getMessage());
				}
			}
			else {
				System.out.println(new Date()+" post2Ivanti failed");
			}
		} else {
			if (swExcludeAll) {
				System.out.println(new Date()+ " -  No INSERT done because of the excluding E;* line in the CSV file  ");
				logg.write(new Date()+ " - No INSERT done because of the excluding E;* line in the CSV file  ");
				logg.newLine();		
			}
		}

		if (swDelete) {
			if (patchIvanti()) {
			System.out.println(new Date()+ " *PATCHED:  recid = "+recid);
			logg.write(new Date()+ "  *PATCHED: recid = "+recid);
			logg.newLine();
			}
		}
		logg.close();
	}

	public static boolean post2Ivanti() {
		// First set the default cookie manager.
		java.net.CookieManager cm = new java.net.CookieManager(null, CookiePolicy.ACCEPT_ALL);
		java.net.CookieHandler.setDefault(cm);
		try {
			//			hosturl ="https://"+host+":"+ivaport+"/api/odata/businessobject/incidents?api_key="+auth;
			//			hosturl ="https://"+host+":"+ivaport+"/api/odata/businessobject/incidents";
			hosturl ="https://"+host+":"+ivaport+path;

			if (swShow) System.out.println("URL: "+hosturl);
			logg.write("URL: "+hosturl);
			logg.newLine();

			URL url = new URL(hosturl); 
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection(); 

			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			con.setReadTimeout(15000);
			con.setConnectTimeout(15000);
			con.setDoOutput(true); 
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			//			con.setRequestProperty("Content-Length", Integer.toString(json_insert.length()));
			//			System.out.println("Content-Length "+Integer.toString(json_insert.length()));
			//			con.setChunkedStreamingMode(json_insert.length());
			//			con.setFixedLengthStreamingMode(json_insert.length());
			//			con.setRequestProperty("Authorization", "Bearer "+auth);
			con.setRequestProperty("Authorization", "rest_api_key="+auth);
			//			con.setRequestProperty("Authorization", "Basic "+auth); 
			//			System.out.println("#3 innan json write   " );
			con.setRequestMethod("POST");
			OutputStream httpout = con.getOutputStream();
			httpout.write(json_insert.getBytes());
			httpout.flush();
			httpout.close();
			//			System.out.println("#3 efter json write   " );

			int responseCode = con.getResponseCode();
			String responseMsg = con.getResponseMessage();
			System.out.println("POST Response Code :  " + responseCode);
			System.out.println("POST Response Message : " + responseMsg);
			logg.write("POST Response Code :  " + responseCode);
			logg.newLine();
			logg.write("POST Response Message : " + responseMsg);
			logg.newLine();


			if (responseCode < 200 || responseCode >= 300 ) {
				System.out.println("#F1: HTTP error code: "	+ responseCode +" "+responseMsg);		
				logg.write("#F1: HTTP error code: "	+ responseCode +" "+responseMsg);
				logg.newLine();

				return false;
			}

			BufferedReader httpin = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			//			if (swShow)	System.out.println("-- OK, got in-stream");

			//			if (swShow)	System.out.println("-- start read lines");
			while ((inputLine = httpin.readLine()) != null  && !state) {
				String parseResult = parseInputlineOverview(inputLine); 
				if (parseResult.startsWith("OK")) {
					state = true;
				}
			}
			httpin.close();
			con.disconnect();
		} 
		catch (Exception e) { System.out.println(e); state = false;   }

		return state; 
	}

	public static boolean patchIvanti() {
		// First set the default cookie manager.
		java.net.CookieManager cm = new java.net.CookieManager(null, CookiePolicy.ACCEPT_ALL);
		java.net.CookieHandler.setDefault(cm);
		try {
			//			hosturl ="https://"+host+":"+ivaport+"/api/odata/businessobject/incidents?api_key="+auth;
			//			hosturl ="https://"+host+":"+ivaport+"/api/odata/businessobject/incidents";
			hosturl ="https://"+host+":"+ivaport+path+"('"+recid+"')";

			if (swShow) System.out.println("URL: "+hosturl);
			logg.write("URL: "+hosturl);
			logg.newLine();

			URL url = new URL(hosturl); 
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection(); 

			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			con.setReadTimeout(15000);
			con.setConnectTimeout(15000);
			con.setDoOutput(true); 
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Authorization", "rest_api_key="+auth);
			con.setRequestMethod("PUT");
			OutputStream httpout = con.getOutputStream();
			httpout.write(json_patch.getBytes());
			httpout.flush();
			httpout.close();
//						System.out.println("#z3 efter json write   " );

			int responseCode = con.getResponseCode();
			String responseMsg = con.getResponseMessage();
			System.out.println("PATCH Response Code :  " + responseCode);
			System.out.println("PATCH Response Message : " + responseMsg);
			logg.write("PATCH Response Code :  " + responseCode);
			logg.newLine();
			logg.write("PATCH Response Message : " + responseMsg);
			logg.newLine();


			if (responseCode < 200 || responseCode >= 300 ) {
				System.out.println("#ZF1: HTTP error code: "	+ responseCode +" "+responseMsg);		
				logg.write("#ZF1: HTTP error code: "	+ responseCode +" "+responseMsg);
				logg.newLine();

				return false;
			}

			BufferedReader httpin = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			//			if (swShow)	System.out.println("-- OK, got in-stream");

			//			if (swShow)	System.out.println("-- start read lines");
			while ((inputLine = httpin.readLine()) != null  && !state) {
				String parseResult = parseInputlineOverview(inputLine); 
				if (parseResult.startsWith("OK")) {
					state = true;
				}
			}
			httpin.close();
			con.disconnect();
		} 
		catch (Exception e) { System.out.println(e); state = false;   }

		return state; 
	}


	static String parseInputlineOverview(String in) {

		if (swShow)	System.out.println("Response: "+ in );

		JsonElement jsonElement = JsonParser.parseString(in);

		JsonObject  jsonObject = jsonElement.getAsJsonObject();
		//		JsonElement overView =  jsonObject.get("RecId"); 

		recid = jsonObject.get("RecId").toString();
		System.out.println("RecId = "+ recid);

		return "OK";        

	}

	static Boolean getCsv() throws IOException {

		String s;
		String[] tab = new String[1000];
		etab = new String[1000];
		ctab = new String[1000];
		ltab = new String[1000];
		Boolean swFound=false;

		File dircsv = new File(".");
		if (config != null ) dircsv = new File(config);

		String sufcsv = ".csv";
		String poscsv = "PlugIvantiSM";

		DirFilter dfcsv = new DirFilter(sufcsv, poscsv);

		File[] listfcsv = dircsv.listFiles(dfcsv);

		System.out.println("-- Number of csv files found:"+ listfcsv.length);

		for (int i = 0; i < listfcsv.length; i++) {

			System.out.println("-- Importing: "+listfcsv[i]+"\n");

			BufferedReader in = new BufferedReader(new FileReader(listfcsv[i]));

			while ((s = in.readLine()) != null) {
				if (s.length() == 0) continue; 
				if (s.startsWith("#")) continue; 
				//				System.out.println("-- Row: "+s);
				// splittar rad från fil
				swFound=true;  // csv file found
				tab = s.split(";" , 2); 
				if (tab[0].startsWith("E")) {
					if (tab[1].toUpperCase().startsWith("*")) swExcludeAll = true;
					etab[ecount++] = tab[1].toUpperCase();
				}
				if (tab[0].startsWith("C")) ctab[ccount++] = tab[1].toUpperCase();
				if (tab[0].startsWith("L")) ltab[lcount++] = tab[1].toUpperCase();

			}
			in.close();
		}
		return swFound;
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