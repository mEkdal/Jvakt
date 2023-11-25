package Jvakt;
/*
 * 2023-11-25 V.01 Michael Ekdal		New pgm to list the logs in the DB
 */

import javax.swing.table.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

class consoleLogsViewDM extends AbstractTableModel {

	static final long serialVersionUID = 50L;
	String afn;
	String columnNames[] = {"Line","Row"};
	//	static Vector map = new Vector(1000,100);
	static Vector<consoleLogsViewROW> map = new Vector<consoleLogsViewROW>(100,100);

	consoleLogsViewROW rad;

	int i = 0;

	static String DBUrl = "jdbc:postgresql://localhost:5433/Jvakt";
	static Connection conn = null;

	String version = "jVakt - consoleLogsViewDM ( 2023-NOV-21)";
	String database = "Jvakt";
	String dbuser   = "console";
	String dbpassword = "";
	String dbhost   = "localhost";
	String dbport   = "5433";

	String jvhost   = "localhost";
	String jvport   = "1956";

	String id;
	String origin;
	String credat;

	Boolean swDBopen = false; 

	String where = "";

	public consoleLogsViewDM() throws IOException {

		getProps();
		//		if (openDB()) refreshData();

	}

	public int getColumnCount() {
		//		System.out.println("ColumnCount: " + columnNames.length);
		return columnNames.length;
	}

	public int getRowCount() {
		//		System.out.println("RowCount: " + map.size()+1);
		return map.size()+1;
	}

	public String getColumnName(int col) {
		//		System.out.println("ColumnNames: " + columnNames[col]);
		return columnNames[col];
	}

	public Object getValueAt(int row, int col) {

		if ( row >= map.size()) return null;

		consoleLogsViewROW rad;
		rad = (consoleLogsViewROW) map.get(row);

		if (col == 0) {
			//			System.out.println("row: "+row+"  rad: "+rad.getRow());
			return rad.getLine();
		} else 
			if (col == 1) {
				//			System.out.println("row: "+row+"  rad: "+rad.getRow());
				return rad.getRow();
			} else	return null;
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

public void setId(String id) {
	this.id = id;
	//		System.out.println("This id: " + this.id);
}

public void setOrigin(String origin) {
	this.origin = origin;
	//		System.out.println("This origin: " + this.origin);
}

public void setCredat(String credat) {
	this.credat = credat;
	//		System.out.println("This credat: " + this.credat);
}



public void setValueAt(Object value, int row, int col) {
	// 	System.out.println("wD setValueAt " + value + " " + row + " "+ col);

	consoleLogsViewROW rad;
	if ( row >= map.size()) { 
		rad = new consoleLogsViewROW(); 
		map.add(rad); 
	}
	else rad = (consoleLogsViewROW) map.get(row);

	if (col == 0) {
		rad.setLine((int)value);
	} if (col == 1) {
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
			s = new String("SELECT line,row FROM public.logs where id='"+id+"' and origin='"+origin+"' and credat='"+credat+"' order by line;");
		else
			s = new String("SELECT line,row FROM public.logs where id='"+id+"' and origin='"+origin+"' and credat='"+credat+"' and "+where+" order by line;");

		map.clear();

		//			System.out.println(s);
		//			Statement stmt = conn.createStatement(ResultSet.CONCUR_READ_ONLY,ResultSet.TYPE_SCROLL_INSENSITIVE); 
		Statement stmt = conn.createStatement(ResultSet.CONCUR_READ_ONLY,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT ); 
		stmt.setFetchSize(1000);
		stmt.setQueryTimeout(5);
		ResultSet rs = stmt.executeQuery(s);

		//			if (!rs.first()) {
		//				System.out.println("createemptyrow");
		//				createEmptyRow();
		//			}
		int antal = 0;
		while (rs.next()) {
			//--
			//				System.out.println("rs.next "+rs.getString("row"));
			rad = new consoleLogsViewROW();

			//				for (int i = 1; i <= 7; i++) {

			rad.setLine(rs.getInt("line"));
			rad.setRow(rs.getString("row"));
			//				System.out.println("getRow "+rad.getRow());

			//				}
			map.add(rad);
			//				consoleLogsViewROW radX;
			//				radX = (consoleLogsViewROW) map.get(antal);
			//				System.out.println("radX "+radX.getRow());
			antal++;
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
	}
	catch (Exception e) {
		System.err.println(e);
		System.err.println(e.getMessage());
		swDBopen = false;
		createEmptyRow();
	}
	return true;
}

private boolean createEmptyRow() {

	rad = new consoleLogsViewROW();

	rad.setLine(0);
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