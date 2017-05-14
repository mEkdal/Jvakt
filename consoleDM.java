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
//    private BufferedReader userDB;
//    private File userDBFile;
//    private String row;
//    private String[] fields;
    String columnNames[] = {"Count", "Id", "Prio", "Type", "ConDate", "Status", "Body"};
    static Vector map = new Vector(100,10);
    
    consoleROW rad;
    
    int i = 0;
//    private PrintStream ut ;
//    private File wrk ;

    static String DBUrl = "jdbc:postgresql://localhost:5433/Jvakt";
	static Connection conn = null;

	String version = "jVakt 2.0 - consoleDM 1.0 Date 2017-05-05_01";
	String database = "Jvakt";
	String dbuser   = "console";
	String dbpassword = "Jvakt";
	String dbhost   = "localhost";
	String dbport   = "5433";

	String jvhost   = "localhost";
	String jvport   = "1956";
	
    Boolean swDBopen = false; 


	
    public consoleDM() throws IOException {

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
        	return rad.getCount();
        } else if (col == 1) {
        	return rad.getId();
        } else if (col == 2) {
        	return rad.getPrio();
        } else if (col == 3) {
        	return rad.getType();
        } else if (col == 4) {
        	return rad.getCondat();
        } else if (col == 5) {
        	return rad.getStatus();
        } else if (col == 6) {
        	return rad.getBody();
        } else {
            return null;
        }

    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    
    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears on screen.
//            if (col < 1) {
//                return false;
//            } else {
//                return true;
//            }
        return false;
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
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
        	rad.setCount((int)value);
        } else if (col == 1) {
        	rad.setId((String)value);
        } else if (col == 2) {
        	rad.setPrio((int)value);
        } else if (col == 3) {
        	rad.setType((String)value);            
        } else if (col == 4) {
        	rad.setCondat((String)value);
        } else if (col == 5) {
        	rad.setStatus((String)value);
        } else if (col == 6) {
        	rad.setBody((String)value);
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
		String s = new String("select * from console;"); 
		
		map.clear();
		
		System.out.println(s);
		Statement stmt = conn.createStatement(ResultSet.CONCUR_READ_ONLY,ResultSet.TYPE_SCROLL_INSENSITIVE); 
		stmt.setFetchSize(1000);
		ResultSet rs = stmt.executeQuery(s);
		
		if (!rs.first()) {
			createEmptyRow();
		}

		while (rs.next()) {
//--
			rad = new consoleROW();
        
		for (int i = 1; i <= 7; i++) {
			
            rad.setCount(rs.getInt("count"));
            rad.setId(rs.getString("id"));
            rad.setPrio(rs.getInt("prio"));
            rad.setType(rs.getString("type"));
            rad.setCondat(rs.getString("condat"));
            rad.setStatus(rs.getString("status"));
            rad.setBody(rs.getString("body"));

		}
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
            rad.setCondat(" ");
            rad.setStatus(" ");
            rad.setBody(" ");
            map.add(rad);
			
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
}