package Jvakt;
/*
 * 2024-04-10 V.04 Michael Ekdal		Added the VARCHAR(max) if length of field are more than 8000 and the -Char2Varchar is used.
 * 2024-04-08 V.03 Michael Ekdal		renamed field name IN to IN_R. Added warning about max row size. Added -Char2Varchar function.
 * 2024-03-12 V.02 Michael Ekdal		Added "drop table if exists" logic
 * 2024-02-29 V.01 Michael Ekdal		Created.
 */

//import java.io.*;
//import java.net.*;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.*;
//import org.apache.commons.codec.binary.Hex;

public class CpyAS400TblToSQLsvr {
	static String version = "CpyAS400TblToSQLsvr ";

	public static void main (String[] args)
	{
		version += getVersion()+".04";

		if (args.length < 1) {
			System.out.println("\n --- " +version+" ---");
			System.out.println("\nThis program will copy an As400 table to a SQL-server database ");

			System.out.println("\nThe parameters and their meaning are:\n"+
					"\n-AS400host    \tThe As400 host name."+
					"\n-AS400port    \tThe AS400 port number. (optional, the default port is 8471)" +      
					"\n-AS400user    \tThe As400 user name."+
					"\n-AS400pw      \tThe As400 password."+
					"\n-AS400lib     \tThe AS400 library name."+
					"\n-AS400tbl     \tThe As400 table name."+
					"\n-AS400where   \tThe As400 where statement (optional, like: old='A' and lgtyp='L'."+
					"\n-SQLhost      \tThe SQL-server hostname." +
					"\n-SQLport      \tThe SQL-server port number. (optional, the default port is 1433)" +
					"\n-SQLisec      \tThe SQL integrated security Y/N" +
					"\n-SQLuser      \tThe SQL user name. (if integrated security is N)"+
					"\n-SQLpw        \tThe SQL password.  (if integrated security is N)"+
					"\n-SQLdbase     \tThe SQL-server database name" +
					"\n-SQLtbl       \tThe SQL-server table name. (optional, the default name is AS400 library + table)" +
					"\n-SQLdrop      \tThe a 'DROP TABLE IF EXISTS' is added to remove old table before creat of new table." +
					"\n-SQLdropOnly  \tThe a 'DROP TABLE IF EXISTS' is added to remove old table then the pgm stops." +
					"\n-Char2Varchar \tFields of type CHAR is converted to VARCHAR." +
					"\n-run          \tTo actually insert files on the status on the SQL-server side. "+
					"\n-numlog       \tHow often the commit is done and the insert statement is shown in the log. (default is 1) "
					);

			System.exit(4);
		}	

		System.out.println(new Date()+" " +version +"\n");

		boolean swRun = false;
		int antrows=0;
		int numlog=1;

		String sysAS  = null;
		String collAS = null;
		String tblAS  = null;
		String whereAS  = null;
		String ASuser  = null;
		String ASpw   = null;
		String SQLuser  = null;
		String SQLpw   = null;
		String SQLisec   = "N";
		int ASport = 8471;
		String sysSQL  = null;
		String collSQL = null;
		String tblSQL  = null;
		int SQLport = 1433;
		boolean swSQLdrop = false;
		boolean swSQLdropOnly = false;
		boolean swChar2Varchar = false;

		// reads command line arguments
		for ( int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-run")) swRun = true;
			if (args[i].equalsIgnoreCase("-AS400host")) sysAS = args[++i];
			if (args[i].equalsIgnoreCase("-AS400user")) ASuser = args[++i];
			if (args[i].equalsIgnoreCase("-AS400pw")) ASpw = args[++i];
			if (args[i].equalsIgnoreCase("-AS400lib")) collAS = args[++i];
			if (args[i].equalsIgnoreCase("-AS400tbl")) tblAS = args[++i];
			if (args[i].equalsIgnoreCase("-AS400where")) whereAS = args[++i];
			if (args[i].equalsIgnoreCase("-AS400port")) ASport = Integer.parseInt(args[++i]);
			if (args[i].equalsIgnoreCase("-SQLhost")) sysSQL = args[++i];
			if (args[i].equalsIgnoreCase("-SQLuser")) SQLuser = args[++i];
			if (args[i].equalsIgnoreCase("-SQLpw")) SQLpw = args[++i];
			if (args[i].equalsIgnoreCase("-SQLisec")) SQLisec = args[++i];
			if (args[i].equalsIgnoreCase("-SQLdbase")) collSQL = args[++i];
			if (args[i].equalsIgnoreCase("-SQLtbl")) tblSQL = args[++i];
			if (args[i].equalsIgnoreCase("-SQLport")) SQLport = Integer.parseInt(args[++i]);
			if (args[i].equalsIgnoreCase("-numlog")) numlog = Integer.parseInt(args[++i]);
			if (args[i].equalsIgnoreCase("-SQLdrop")) swSQLdrop=true;
			if (args[i].equalsIgnoreCase("-SQLdropOnly")) swSQLdropOnly=true;
			if (args[i].equalsIgnoreCase("-Char2Varchar")) swChar2Varchar=true;
		}

		if (tblSQL==null) tblSQL = collAS+"_"+tblAS;

		Properties p = new Properties();

		Connection conSQL   = null;
		Connection conAS    = null; 

		// Set the properties for the connection to AS400.
		p.put("naming", "sql");
		p.put("errors", "full");
		p.put("auto commit", "false");
		p.put("transaction isolation", "read uncommitted");
		p.put("translate hex", "character");
		p.put("date format", "iso");
		p.put("time format", "iso");
		p.put("block criteria", "1");
		p.put("block size", "512");
		p.put("user", ASuser);
		p.put("password", ASpw);
		p.put("translate binary", "true");
		//    p.put("block criteria", "1");

		try {
			//	    // Load the Microsoft for Java JDBC driver.
			//	    Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver");
			System.out.println("- connecting to "+sysSQL +":"+SQLport+";databaseName="+ collSQL);
			if (SQLisec.toUpperCase().startsWith("N"))
				conSQL = DriverManager.getConnection("jdbc:sqlserver://" + sysSQL +":"+SQLport+";databaseName="+ collSQL+";integratedSecurity=false;encrypt=true;trustServerCertificate=true", SQLuser, SQLpw );
			else 
				conSQL = DriverManager.getConnection("jdbc:sqlserver://" + sysSQL +":"+SQLport+";databaseName="+ collSQL+";integratedSecurity=true;encrypt=true;trustServerCertificate=true");

			System.out.println("- connected");
			conSQL.setAutoCommit(false);

			// Load the IBM Toolbox for Java JDBC driver.
			//			DriverManager.registerDriver(new com.ibm.as400.access.AS400JDBCDriver());

			// Get a connection to the AS400 database.
			//	    conAS = DriverManager.getConnection ("jdbc:as400://"+sysAS+";errors=full", "septpadm", "snape309" );
			System.out.println("- connecting to "+sysAS+":"+ASport);
			conAS = DriverManager.getConnection ("jdbc:as400://"+sysAS+":"+ASport+";errors=full", p);
			System.out.println("- connected\n");

			DatabaseMetaData dmd = conAS.getMetaData ();

			// Execute the query.
			Statement select = conAS.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			//	    ResultSet rs = select.executeQuery ("SELECT "+fieldsAS+" FROM "+collAS+dmd.getCatalogSeparator()+tblAS);
			ResultSet rs;
			String sel=null;
			if (whereAS==null) sel = "SELECT * FROM "+collAS+dmd.getCatalogSeparator()+tblAS+" for read only with nc";
			else               sel = "SELECT * FROM "+collAS+dmd.getCatalogSeparator()+tblAS+" where "+whereAS+" for read only with nc";
			System.out.println(sel);
			rs = select.executeQuery (sel);

			//			if (whereAS==null) rs = select.executeQuery ("SELECT * FROM "+collAS+dmd.getCatalogSeparator()+tblAS+" for read only with nc");
			//			else rs = select.executeQuery ("SELECT * FROM "+collAS+dmd.getCatalogSeparator()+tblAS+" where "+whereAS+" for read only with nc");

			// Get information about the result set.  
			String dropTab = "DROP TABLE IF EXISTS "+tblSQL+";";
			String creTab = "CREATE TABLE "+tblSQL+" (";
			String tabFields="";
			String fieldName;
			int fieldLength;
			int rowSize=0;
			ResultSetMetaData rsmd = rs.getMetaData ();
			int columnCount = rsmd.getColumnCount ();
			if (columnCount>1024) {
				System.out.println("ERROR: The number of columns are "+columnCount+" thus exeeding the 1024 allowed in SQL server");
				System.exit(8);
			}
			//			System.out.println("Number of columns "+columnCount);
			for (int i = 1; i <= columnCount; i++) {
				fieldName=rsmd.getColumnName(i);
				fieldLength = rsmd.getPrecision(i);
				rowSize += fieldLength;
				if (fieldLength>8000 & !swChar2Varchar) {
					System.out.println("WARNING: The length of the field "+fieldName+" is changed to 8000");
					fieldLength=8000; // no field is allowed to be longer that 8000 in SQL server
				}

				if (fieldName.contains("$")) {
					fieldName = fieldName.replaceAll("\\$" , "S"); //Replace $ with an S. SQL server doesn't allow $ in field name.
					fieldName+="_R";
					System.out.println("WARNING: The name of the field "+rsmd.getColumnName(i)+" is changed to "+fieldName);
				}
				if (fieldName.contains("@")) {
					fieldName = fieldName.replaceAll("@" , "A"); //Replace @ with an A. SQL server doesn't allow @ in field name.
					fieldName+="_R";
					System.out.println("WARNING: The name of the field "+rsmd.getColumnName(i)+" is changed to "+fieldName);
				}
				if (fieldName.equals("DESC")) {
					fieldName = "DESC_R"; //DESC is a reserved word in SQL server, replace with DESC_R
					System.out.println("WARNING: The name of the field "+rsmd.getColumnName(i)+" is changed to "+fieldName);
				}
				if (fieldName.equals("OPEN")) {
					fieldName = "OPEN_R"; //OPEN is a reserved word in SQL server, replace with OPEN_R
					System.out.println("WARNING: The name of the field "+rsmd.getColumnName(i)+" is changed to "+fieldName);
				}
				if (fieldName.equals("PROC")) {
					fieldName = "PROC_R"; //PROC is a reserved word in SQL server, replace with PROC_R
					System.out.println("WARNING: The name of the field "+rsmd.getColumnName(i)+" is changed to "+fieldName);
				}
				if (fieldName.equals("FROM")) {
					fieldName = "FROM_R"; //FROM is a reserved word in SQL server, replace with FROM_R
					System.out.println("WARNING: The name of the field "+rsmd.getColumnName(i)+" is changed to "+fieldName);
				}
				if (fieldName.equals("OPTION")) {
					fieldName = "OPTION_R"; 
					System.out.println("WARNING: The name of the field "+rsmd.getColumnName(i)+" is changed to "+fieldName);
				}
				if (fieldName.equals("USER")) {
					fieldName = "USER_R"; 
					System.out.println("WARNING: The name of the field "+rsmd.getColumnName(i)+" is changed to "+fieldName);
				}
				if (fieldName.equals("FILE")) {
					fieldName = "FILE_R"; 
					System.out.println("WARNING: The name of the field "+rsmd.getColumnName(i)+" is changed to "+fieldName);
				}
				if (fieldName.equals("KEY")) {
					fieldName = "KEY_R"; 
					System.out.println("WARNING: The name of the field "+rsmd.getColumnName(i)+" is changed to "+fieldName);
				}
				if (fieldName.equals("IN")) {
					fieldName = "IN_R"; 
					System.out.println("WARNING: The name of the field "+rsmd.getColumnName(i)+" is changed to "+fieldName);
				}
				if (fieldName.equals("IS")) {
					fieldName = "IS_R"; 
					System.out.println("WARNING: The name of the field "+rsmd.getColumnName(i)+" is changed to "+fieldName);
				}

				if (rsmd.getColumnTypeName(i).startsWith("DECIMAL") || rsmd.getColumnTypeName(i).startsWith("NUMERIC") )
					creTab+=fieldName+" "+rsmd.getColumnTypeName(i)+"("+fieldLength+","+rsmd.getScale(i)+")";
				else if (rsmd.getColumnTypeName(i).startsWith("CHAR") ) {
					if (swChar2Varchar) {
						if (rsmd.getPrecision(i)>8000) creTab+=fieldName+" VARCHAR(max)";
						else creTab+=fieldName+" VARCHAR("+fieldLength+")";
					}
					else 				creTab+=fieldName+" "+rsmd.getColumnTypeName(i)+"("+fieldLength+")";
				}
				else if (rsmd.getColumnTypeName(i).startsWith("VARCHAR") ) {
						if (rsmd.getPrecision(i)>8000) creTab+=fieldName+" VARCHAR(max)";
						else creTab+=fieldName+" VARCHAR("+fieldLength+")";
				}
				else if (rsmd.getColumnTypeName(i).startsWith("TIMESTAMP") )
					creTab+=fieldName+" DATETIME";
				else if (rsmd.getColumnTypeName(i).startsWith("TIME") )
					creTab+=fieldName+" "+rsmd.getColumnTypeName(i);
				else if (rsmd.getColumnTypeName(i).startsWith("DATE") )  
					creTab+=fieldName+" "+rsmd.getColumnTypeName(i);
				else if (rsmd.getColumnTypeName(i).startsWith("SMALLINT") )  
					creTab+=fieldName+" "+rsmd.getColumnTypeName(i);
				else if (rsmd.getColumnTypeName(i).startsWith("INTEGER") )  
					creTab+=fieldName+" "+rsmd.getColumnTypeName(i);
				else {
					System.out.println("-- ERROR #1: the field type "+rsmd.getColumnTypeName(i)+" in field "+rsmd.getColumnName(i)+" is not handeled. Pls add it to the pgm.");
					System.exit(8);
				}
				tabFields+=fieldName;
				//			System.out.println(rsmd.getColumnName(i)+" "+rsmd.getColumnTypeName(i)+" "+rsmd.getPrecision(i)+" "+rsmd.getScale(i));
				if (i<columnCount) {
					creTab+=",";
					tabFields+=",";
				}
			}
			creTab+=" );";
			
			if (rowSize>8060 & !swChar2Varchar) {
				System.out.println("WARNING: The maximum row size in SQL-server is 8060, this row size is "+rowSize+". Tip: the switch -char2varchar might solve the problem.");
			}

			if (swSQLdrop || swSQLdropOnly) {
				System.out.println("\n"+dropTab);
				Statement dropSQL = conSQL.createStatement();
				if (swRun) dropSQL.executeUpdate(dropTab); 				
			}
			
			if (swSQLdropOnly) {
				conSQL.commit();
				System.exit(0);
			}
			
			System.out.println("\n"+creTab+"\n");
			Statement creSQL = conSQL.createStatement();
			if (swRun) creSQL.executeUpdate(creTab); 



			String prepIns = "INSERT INTO "+ tblSQL + " ("+tabFields+")  VALUES (";
			for (int i = 1; i <= columnCount; i++) {
				prepIns+="?";
				if ( i<columnCount ) prepIns+= ", ";
			}
			prepIns+= ")";
			System.out.println("prepIns "+prepIns);
			//	    // Prepare a statement for inserting rows. 
			PreparedStatement insSQL = conSQL.prepareStatement(prepIns);
			System.out.println("prepIns ok");

			// Iterate through the rows in the result set.
			//			String value = "";
			//			String ins = "";
			int antal=numlog;
			while (rs.next ()) {
				for (int i = 1; i <= columnCount; i++) {
//					System.out.println(rsmd.getColumnName(i)+" ColumnTypeName: "+rsmd.getColumnTypeName(i));
					if (rsmd.getColumnTypeName(i).startsWith("CHAR"))
						insSQL.setString(i, rs.getString(i));
					else if (rsmd.getColumnTypeName(i).startsWith("VARCHAR"))
						insSQL.setString(i, rs.getString(i));
					else if (rsmd.getColumnTypeName(i).startsWith("TIMESTAMP"))
						insSQL.setTimestamp(i, rs.getTimestamp(i));
					else if (rsmd.getColumnTypeName(i).startsWith("TIME"))
						insSQL.setTime(i, rs.getTime(i));
					else if (rsmd.getColumnTypeName(i).startsWith("DATE"))
						insSQL.setDate(i, rs.getDate(i));
					else if (rsmd.getColumnTypeName(i).startsWith("SMALLINT") || rsmd.getColumnTypeName(i).startsWith("INTEGER"))
						insSQL.setInt(i, rs.getInt(i));
					else 
						insSQL.setBigDecimal(i, rs.getBigDecimal(i)); 
				}
				insSQL.addBatch(); 

				antal++; antrows++;

				if (antal>=numlog) {
					if (swRun) insSQL.executeBatch();
					System.out.println("#"+antrows+" "+insSQL.toString());
					antal=0;
					conSQL.commit();
				}
			}
			if (swRun) insSQL.executeBatch();
		}

		catch (Exception e) {
			System.out.println ();
			System.out.println ("ERROR #2: " + e.getMessage());
			System.exit(8);
		}

		finally {

			// Clean up.
			try {
				if (conAS != null)
					conAS.close ();
				if (conSQL != null) {
					conSQL.commit();
					conSQL.close ();
				}
			}
			catch (SQLException e) {
				System.out.println ("ERROR #3: " + e.getMessage());
				System.exit(8);
			}
		}

		if (swRun) System.out.println("\n"+new Date()+" - "+antrows+" rows inserted");
		else       System.out.println("\n"+new Date()+" - No rows inserted -norun was requested");
		System.exit (0);
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