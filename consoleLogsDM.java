package Jvakt;
/*
 * 2023-11-20 V.01 Michael Ekdal		New pgm to list the logs in the DB
 */

import javax.swing.table.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
 
class consoleLogsDM extends AbstractTableModel {

	static final long serialVersionUID = 50L;
	String afn;
	String columnNames[] = {"Id", "Origin", "Credat","Row"};
//	static Vector map = new Vector(1000,100);
	static Vector<consoleLogsROW> map = new Vector<consoleLogsROW>(100,100);
	
	consoleLogsROW rad;

	int i = 0;

	static String DBUrl = "jdbc:postgresql://localhost:5433/Jvakt";
	static Connection conn = null;

	String version = "jVakt - consoleLogsDM ( 2023-NOV-20)";
	String database = "Jvakt";
	String dbuser   = "console";
	String dbpassword = "";
	String dbhost   = "localhost";
	String dbport   = "5433";

	String jvhost   = "localhost";
	String jvport   = "1956";

	Boolean swDBopen = false; 

	String where = "";

	public consoleLogsDM() throws IOException {

		getProps();
		if (openDB()) refreshData();

	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return map.size()+1;
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Object getValueAt(int row, int col) {

		if ( row >= map.size()) return null;

		consoleLogsROW rad;
		rad = (consoleLogsROW) map.get(row);
		
		if (col == 0) {
//			System.out.println("rad: "+rad.getId());
			return rad.getId();
		} else if (col == 1) {
//			System.out.println("rad: "+rad.getOrigin());
			return rad.getOrigin();
		} else if (col == 2) {
//			System.out.println("rad: "+rad.getCredat());
			return rad.getCredat();
		} else if (col == 3) {
//			System.out.println("rad: "+rad.getCredat());
			return rad.getRow();
		} else {
			return null;
		}

	}

	public Class<?> getColumnClass(int c) {
		try {
			//		return getValueAt(0, c).getClass();
			return getValueAt(0, c).getClass();
		} catch (NullPointerException e) {
			return String.class;
		}
	}


	public boolean isCellEditable(int row, int col) {
		return false;
	}

	public void setWhere(String where) {
		this.where = where;
		//		System.out.println("This where: " + this.where);
	}



	public void setValueAt(Object value, int row, int col) {
		// 	System.out.println("wD setValueAt " + value + " " + row + " "+ col);

		consoleLogsROW rad;
		if ( row >= map.size()) { 
			rad = new consoleLogsROW(); 
			map.add(rad); 
		}
		else rad = (consoleLogsROW) map.get(row);

		if (col == 0) {
			rad.setId((String)value);
		} else if (col == 1) {
			rad.setOrigin((String)value);
		} else if (col == 2) {
			rad.setOrigin((String)value);
		} else if (col == 3) {
			rad.setRow((String)value);
		}
		fireTableCellUpdated(row, col);
	}

	public boolean openDB() {

		try {

			Class.forName("org.postgresql.Driver").newInstance();
			DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
			System.out.println(DBUrl);
			System.out.println("dbuser= " + dbuser +"  dbpassword "+ dbpassword);
			conn = DriverManager.getConnection(DBUrl,dbuser,dbpassword);
			conn.setAutoCommit(false);
			conn.setNetworkTimeout(null, 5000);
			swDBopen = true;
		}
		catch (SQLException e) {
//			System.err.println(e);
			System.err.println(e.getMessage());
			swDBopen = false;
			map.clear();
			createEmptyRow();
		}
		catch (Exception e) {
//			System.err.println(e);
			System.err.println(e.getMessage());
			swDBopen = false;
			map.clear();
			createEmptyRow();
		}
		return swDBopen;
	}

	public boolean getDBopen() {
		return swDBopen;
	}

	public boolean refreshData() {

		if (!swDBopen) {
			if (!openDB()) return false;
		}

		try {
			String s;
			if (where.length() < 5 || !where.endsWith(" ") )
//				s = new String("SELECT distinct id,origin,credat FROM public.logs order by credat desc;");
//				s = new String("SELECT id,origin,credat FROM public.logs where row ilike '%<CheckLogs status>%' order by credat desc;");
				s = new String("SELECT id,origin,credat,row FROM public.logs where line=0 order by credat desc;");
			else
				s = new String("SELECT id,origin,credat,row FROM public.logs where line=0 and "+ where +" order by credat desc;");

						System.out.println(s);
			//			Statement stmt = conn.createStatement(ResultSet.CONCUR_READ_ONLY,ResultSet.TYPE_SCROLL_INSENSITIVE); 
			Statement stmt = conn.createStatement(ResultSet.CONCUR_READ_ONLY,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT ); 
			stmt.setFetchSize(1000);
			stmt.setQueryTimeout(5);
			ResultSet rs = stmt.executeQuery(s);

			//			if (!rs.first()) {
			//				System.out.println("createemptyrow");
			//				createEmptyRow();
			//			}

			map.clear();

			while (rs.next()) {
				//--
//				System.out.println("rs.next "+rs.getString("id")+" "+rs.getString("origin")+" "+rs.getString("credat"));
				rad = new consoleLogsROW();

				//				for (int i = 1; i <= 7; i++) {

				rad.setId(rs.getString("id"));
				rad.setOrigin(rs.getString("origin"));
				rad.setCredat(rs.getString("credat"));
				rad.setRow(rs.getString("row"));

				//				}
				map.add(rad);
			}


			rs.close(); 
			stmt.close();
			//		fireTableDataChanged();
		}
		catch (SQLException e) {
			System.err.println(e);
			System.err.println(e.getMessage());
			swDBopen = false;
			createEmptyRow();
			return false;
		}
		catch (Exception e) {
			System.err.println(e);
			System.err.println(e.getMessage());
			swDBopen = false;
			createEmptyRow();
			return false;
		}
		return true;
	}

	private boolean createEmptyRow() {

		rad = new consoleLogsROW();

		rad.setId("**<>**");
		rad.setCredat(" ");
		rad.setOrigin(" ");
		rad.setRow(" ");
		//		map.add(rad);

		return true;	
	}

	public boolean closeDB() {

		try {
			if (swDBopen) 	conn.close();
		}
		catch (SQLException e) {
			System.err.println(e);
			System.err.println(e.getMessage());
			return true;
		}
		return true;
	}

	void getProps() {

		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("console.properties");
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
			input.close();
		} catch (IOException ex) {
			// ex.printStackTrace();
		}

	}

}