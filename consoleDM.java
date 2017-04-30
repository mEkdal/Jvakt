package Jvakt;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.io.*;
import java.util.*;

class consoleDM extends AbstractTableModel {

    String afn;
    private BufferedReader userDB;
    private File userDBFile;
    private String row;
    private String[] fields;
    String columnNames[] = {"Användarnamn", "Förnamn", "Efternamn", "hej", "hå", "till", "gruvan"};
    static Vector map = new Vector(100,10);
    int i = 0;
    private PrintStream ut ;
    private File wrk ;

    public consoleDM() throws IOException {

    	openDB();
    	
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
        // se till att objektet User hämtar vädet med rätt get metod ( if (col == 2) .........

    	if ( row >= map.size()) return null;

        consoleROW rad;
    	rad = (consoleROW) map.get(row);
    	
        if (col == 0) {
        	return rad.getuserName();
        } else if (col == 1) {
        	return rad.getforName();
        } else if (col == 2) {
        	return rad.getlastName();
        } else if (col == 3) {
        	return rad.getheight();
        } else if (col == 4) {
        	return rad.getweight();
        } else if (col == 5) {
        	return rad.getDOB();
        } else if (col == 6) {
        	return rad.getkommentar();
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
        return true;
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
        	rad.setuserName((String)value);
        } else if (col == 1) {
        	rad.setforName((String)value);
        } else if (col == 2) {
        	rad.setlastName((String)value);
        } else if (col == 3) {
        	rad.setheight((String)value);            
        } else if (col == 4) {
        	rad.setweight((String)value);
        } else if (col == 5) {
        	rad.setDOB((String)value);
        } else if (col == 6) {
        	rad.setkommentar((String)value);
        }
        fireTableCellUpdated(row, col);
    }
    
    private boolean openDB() throws IOException {
        userDBFile = new File("c:\\temp\\userDB.txt");
        userDB = new BufferedReader(new FileReader(userDBFile));
        i = 0;


        while ((row = userDB.readLine()) != null) {
            // använd alla fäten från fields för att fylla i alla fält i User objektet
            fields = row.split(";", 8);

            consoleROW rad = new consoleROW();
            rad.setuserName(fields[0]);
            rad.setforName(fields[1]);
            rad.setlastName(fields[2]);
            rad.setheight(fields[3]);
            rad.setweight(fields[4]);
            rad.setDOB(fields[5]);
            rad.setkommentar(fields[6]);
            
            map.add(rad);
            
        }
        userDB.close();

    	return true;
    }
    
    public boolean closeDB() {
    	//skriv userDB
        int y=0;
        int x=0; 
        int z=getRowCount();;
        String s = null; 
        try {
        ut  = new PrintStream(new BufferedOutputStream(new FileOutputStream("c:\\temp\\userDB.txt.wrk", false))); 
    	} catch (IOException f) { }
        
        for (y=0;y<z;y++) {
    	 s = (String)getValueAt(y, 0) + ";";
    	 if (!s.startsWith("null")) {
         for (x=1; x<7  ;x++) {
         	s = s + (String)getValueAt(y, x) + ";";
         }
         ut.println(s);
    	 }
        }
        ut.close();
        // gör delete och rename på filerna när utdata lyckosamt skrivits
        wrk = new File("c:\\temp\\userDB.txt");
        wrk.delete();
        wrk = new File("c:\\temp\\userDB.txt.wrk");
        wrk.renameTo( new File("c:\\temp\\userDB.txt"));
         

    	return true;
    }
}