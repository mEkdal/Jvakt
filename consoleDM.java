package Jvakt;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

class consoleDM extends AbstractTableModel {

	String afn;
//	String columnNames[] = {"Count", "Id", "Prio", "Type", "CreDate", "ConDate", "Status", "Body", "Agent"};
	String columnNames[] = { "Id", "Prio", "Type", "CreDate", "ConDate", "Status", "Body", "Agent"};
	static Vector map = new Vector(100,10);

	consoleROW rad;

	int i = 0;

	static String DBUrl = "jdbc:postgresql://localhost:5433/Jvakt";
	static Connection conn = null;

	String version = "jVakt 2.0 - consoleDM 1.2 Date 2017-08-25_01";
	String database = "Jvakt";
	String dbuser   = "console";
	String dbpassword = "Jvakt";
	String dbhost   = "localhost";
	String dbport   = "5433";

	String jvhost   = "localhost";
	String jvport   = "1956";

	Boolean swDBopen = false; 



	public consoleDM() throws IOException {

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
		// se till att objektet User hämtar värdet med rätt get metod ( if (col == 2) .........

		if ( row >= map.size()) return null;

		consoleROW rad;
		rad = (consoleROW) map.get(row);

		if (col == 0) {
			return rad.getId();
		} else if (col == 1) {
			return rad.getPrio();
		} else if (col == 2) {
			return rad.getType();
		} else if (col == 3) {
			return rad.getCredat();
		} else if (col == 4) {
			return rad.getCondat();
		} else if (col == 5) {
			return rad.getStatus();
		} else if (col == 6) {
			return rad.getBody();
		} else if (col == 7) {
			return rad.getAgent();
		} else {
			return null;
		}

		
//		if (col == 0) {
//			return rad.getCount();
//		} else if (col == 1) {
//			return rad.getId();
//		} else if (col == 2) {
//			return rad.getPrio();
//		} else if (col == 3) {
//			return rad.getType();
//		} else if (col == 4) {
//			return rad.getCredat();
//		} else if (col == 5) {
//			return rad.getCondat();
//		} else if (col == 6) {
//			return rad.getStatus();
//		} else if (col == 7) {
//			return rad.getBody();
//		} else if (col == 8) {
//			return rad.getAgent();
//		} else {
//			return null;
//		}

	}

//	public Class getColumnClass(int c) {
//		return getValueAt(0, c).getClass();
//	}
	public Class getColumnClass(int c) {
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

	public void setValueAt(Object value, int row, int col) {
		// se till att objektet User sparar värdet med rätt set metod ( if (col == 2) .........
		// 	System.out.println("wD setValueAt " + value + " " + row + " "+ col);

		consoleROW rad;
		if ( row >= map.size()) { 
			rad = new consoleROW(); 
			map.add(rad); 
		}
		else rad = (consoleROW) map.get(row);

		if (col == 0) {
			rad.setCount((Integer)value);
//			rad.setCount((int)value);
		} else if (col == 1) {
			rad.setId((String)value);
		} else if (col == 2) {
			rad.setPrio((Integer)value);
//			rad.setPrio((int)value);
		} else if (col == 3) {
			rad.setType((String)value);            
		} else if (col == 4) {
			rad.setCredat((String)value);
		} else if (col == 5) {
			rad.setCondat((String)value);
		} else if (col == 6) {
			rad.setStatus((String)value);
		} else if (col == 7) {
			rad.setBody((String)value);
		} else if (col == 8) {
			rad.setAgent((String)value);
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
			swDBopen = true;
		}
		catch (SQLException e) {
			System.err.println(e);
			System.err.println(e.getMessage());
			swDBopen = false;
			map.clear();
			createEmptyRow();
		}
		catch (Exception e) {
			System.err.println(e);
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
			String s = new String("select * from console order by credat desc;"); 

			map.clear();

//			System.out.println(s);
//			Statement stmt = conn.createStatement(ResultSet.CONCUR_READ_ONLY,ResultSet.TYPE_SCROLL_INSENSITIVE); 
//			Statement stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT ); 
			Statement stmt = conn.createStatement(ResultSet.CONCUR_READ_ONLY,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT ); 
			stmt.setFetchSize(1000);
			ResultSet rs = stmt.executeQuery(s);

//			if (!rs.first()) {
//				System.out.println("createemptyrow");
//				createEmptyRow();
//			}

			while (rs.next()) {
				//--
//				System.out.println("rs.next");
				rad = new consoleROW();

//				for (int i = 1; i <= 7; i++) {

					rad.setCount(rs.getInt("count"));
					rad.setId(rs.getString("id"));
					rad.setPrio(rs.getInt("prio"));
					rad.setType(rs.getString("type"));
					rad.setCredat(rs.getString("credat"));
					rad.setCondat(rs.getString("condat"));
					rad.setStatus(rs.getString("status"));
					rad.setBody(rs.getString("body"));
					rad.setAgent(rs.getString("agent"));

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

		rad = new consoleROW();

		rad.setCount(0);
		rad.setId("**<>**");
		rad.setPrio(0);
		rad.setType(" ");
		rad.setCredat(" ");
		rad.setCondat(" ");
		rad.setStatus(" ");
		rad.setBody(" ");
		rad.setAgent(" ");
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