package Jvakt;

//import java.awt.*;
//import java.awt.event.*;
//import javax.swing.event.*;
//import javax.swing.*;
import javax.swing.table.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.*;

class consoleStsDM extends AbstractTableModel {

	static final long serialVersionUID = 50L;
	String afn;
	String columnNames[] = {"state", "id","prio", "type", "status", "body",  "rptdat", "chkday", "chktim", "errors", "accerr", "msg", "msgdat", "console", "condat", "info", "plugin", "agent", "sms", "smsdat", "msg30", "msgdat30", "chktimto"};
//	static Vector map = new Vector(1000,100);
	static Vector<consoleStsROW> map = new Vector<consoleStsROW>(100,100);

	consoleStsROW rad;

	int i = 0;

	static String DBUrl = "jdbc:postgresql://localhost:5433/Jvakt";
	static Connection conn = null;
	PreparedStatement prepStmt = null;

	String version = "jVakt - consoleStsDM (2021-DEC-30)";
	String database = "Jvakt";
	String dbuser   = "console";
	String dbpassword = "";
	String dbhost   = "localhost";
	String dbport   = "5433";

	String jvhost   = "localhost";
	String jvport   = "1956";

	boolean swDBopen = false; 

	String where = "";
	String[] tab = new String [1];
	boolean editable = false; 

	Calendar cal = Calendar.getInstance();

	public consoleStsDM() throws IOException {

		getProps();
		if (openDB()) refreshData();

	}

	public int getColumnCount() {
		//		System.out.println("getColumnCount " + columnNames.length);
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

		consoleStsROW rad;
		rad = (consoleStsROW) map.get(row);

		if (col == 0) {
			return rad.getState();
		} else if (col == 1) {
			return rad.getId();
		} else if (col == 2) {
			return rad.getPrio();
		} else if (col == 3) {
			return rad.getType();
		} else if (col == 4) {
			return rad.getStatus();
		} else if (col == 5) {
			return rad.getBody();
		} else if (col == 6) {
			return rad.getRptdat();
		} else if (col == 7) {
			return rad.getChkday();
		} else if (col == 8) {
			return rad.getChktim();
		} else if (col == 9) {
			return rad.getErrors();
		} else if (col == 10) {
			return rad.getAccerr();
		} else if (col == 11) {
			return rad.getMsg();
		} else if (col == 12) {
			return rad.getMsgdat();
		} else if (col == 13) {
			return rad.getConsole();
		} else if (col == 14) {
			return rad.getCondat();
		} else if (col == 15) {
			return rad.getInfo();
		} else if (col == 16) {
			return rad.getPlugin();
		} else if (col == 17) {
			return rad.getAgent();
		} else if (col == 18) {
			return rad.getSms();
		} else if (col == 19) {
			return rad.getSmsdat();
		} else if (col == 20) {
			return rad.getMsg30();
		} else if (col == 21) {
			return rad.getMsgdat30();
		} else if (col == 22) {
			return rad.getChktimto();
		} else {
			return null;
		}

	}

	public Class<?> getColumnClass(int c) {
		try {
			//		System.out.println("getColumnClass c: " + c + "  "+getValueAt(0, c).getClass());
			return getValueAt(0, c).getClass();
		} catch (NullPointerException e) {
			return String.class;
		}
	}


	public boolean isCellEditable(int row, int col) {
		if (editable && (col==0||col==1||col==2||col==3||col==4||col==5||col==7||col==8||col==10||col==11||col==13||col==15||col==16||col==18||col==20||col==22)) return true;
		else return false;
	}

	public void setWhere(String where) {
		this.where = where;
		//		System.out.println("This where: " + this.where);
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
		System.out.println("Editable: " + this.editable);
	}

	public void setValueAt(Object value, int row, int col) {
		// se till att objektet User sparar värdet med rätt set metod ( if (col == 2) .........
		System.out.println("wD setValueAt " + value + " " + row + " "+ col);
		String tmp;
		consoleStsROW rad;

		if ( row >= map.size()) { 
			rad = new consoleStsROW(); 
			map.add(rad); 
		}
		else rad = (consoleStsROW) map.get(row);
		String oId   = rad.getId();
		int    oPrio = rad.getPrio();
		
		if (col == 0) {
			tmp =((String)value).trim().toUpperCase();
			if (tmp.length()<1) tmp="I";
			System.out.println("setState " + value + " -  " + row + " "+ col);
			System.out.println("setState rad " + rad.getId());
			rad.setState(tmp);
		} else if (col == 1) {
			tmp =((String)value).trim();
			if (tmp.length()<1) tmp=" ";
			rad.setId(tmp); 
		} else if (col == 2) {
			rad.setPrio((Integer)value);
		} else if (col == 3) {
			tmp =((String)value).trim().toUpperCase();
			if (tmp.length()<1) tmp="T";
			rad.setType(tmp);
		} else if (col == 4) {
			tmp =((String)value).trim().toUpperCase();
			if (tmp.length()<1) tmp="OK";
			rad.setStatus(tmp);
		} else if (col == 5) {
			tmp =((String)value).trim();
			if (tmp.length()<1) tmp=" ";
			rad.setBody(tmp);            
		} else if (col == 6) {
			rad.setRptdat((String)value);
		} else if (col == 7) {
			tmp =((String)value).trim().toUpperCase();
			if (tmp.length()<1) tmp="*ALL";
			rad.setChkday(tmp);
		} else if (col == 8) {
			rad.setChktim((String)value);
		} else if (col == 9) {
			rad.setErrors((Integer)value);
		} else if (col == 10) {
			rad.setAccerr((Integer)value);
		} else if (col == 11) {
			tmp =((String)value).trim().toUpperCase();
			if (tmp.length()<1) tmp=" ";
			rad.setMsg(tmp);
		} else if (col == 12) {
			rad.setMsgdat((String)value);
		} else if (col == 13) {
			tmp =((String)value).trim().toUpperCase();
			if (tmp.length()<1) tmp=" ";
			rad.setConsole(tmp);
		} else if (col == 14) {
			rad.setCondat((String)value);
		} else if (col == 15) {
			tmp =((String)value).trim();
			if (tmp.length()<1) tmp=" ";
			rad.setInfo(tmp);
		} else if (col == 16) {
			tmp =((String)value).trim();
			if (tmp.length()<1) tmp=" ";
			rad.setPlugin(tmp);
		} else if (col == 17) {
			tmp =((String)value).trim();
			if (tmp.length()<1) tmp=" ";
			rad.setAgent(tmp);
		} else if (col == 18) {
			tmp =((String)value).trim().toUpperCase();
			if (tmp.length()<1) tmp=" ";
			rad.setSms(tmp);
		} else if (col == 19) {
			rad.setSmsdat((String)value);
		} else if (col == 20) {
			tmp =((String)value).trim().toUpperCase();
			if (tmp.length()<1) tmp=" ";
			rad.setMsg30(tmp);
		} else if (col == 21) {			
			tmp =((String)value).trim().toUpperCase();
			if (tmp.length()<1) tmp=" ";
			rad.setMsgdat30(tmp);
		} else if (col == 22) {
			rad.setChktimto((String)value);
		}

		fireTableCellUpdated(row, col);
		//col==0||col==2||col==3||col==4||col==5||col==7||col==8||col==10||col==15||col==22		
		String updateTable = "UPDATE status SET state=?, id=?, prio=?, type=?, status=?, body=?, chkday=?, chktim=?, accerr=?, info=?, plugin=?, chktimto=?,msg=?,sms=?,msg30=?,console=?  "
				+ " WHERE ID = ? and prio = ? ";
		try {
			prepStmt = conn.prepareStatement(updateTable);
			prepStmt.setString(1, rad.getState());
			prepStmt.setString(2, rad.getId());
			prepStmt.setInt(   3, rad.getPrio());
			prepStmt.setString(4, rad.getType());
			prepStmt.setString(5, rad.getStatus());
			prepStmt.setString(6, rad.getBody());
			prepStmt.setString(7, rad.getChkday());
			if (rad.getChktim() != null) {
				tab = rad.getChktim().split(":" , 6);
				tab[2] = tab[2].substring(0, 2);
				cal.set(1970, 01, 01, Integer.valueOf(tab[0]), Integer.valueOf(tab[1]), Integer.valueOf(tab[2])); // only HH:MM:SS is used
//				prepStmt.setTime(7, new java.sql.Time(Integer.valueOf(tab[0]),Integer.valueOf(tab[1]),Integer.valueOf(tab[2]))); 
				prepStmt.setTime(8, new java.sql.Time( cal.getTime().getTime()));  
			}
			else {
				cal.set(1970, 01, 01, 6, 0, 0); // only HH:MM:SS is used
			//	prepStmt.setTime(7, new java.sql.Time(6,0,0)); 
				prepStmt.setTime(8, new java.sql.Time( cal.getTime().getTime())); // chktim 06:00:00

			}
			prepStmt.setInt(   9, Integer.valueOf(rad.getAccerr())); 
			prepStmt.setString(10, rad.getInfo());
			prepStmt.setString(11, rad.getPlugin());

			if (rad.getChktimto() != null) {
				tab = rad.getChktimto().split(":" , 6);
				tab[2] = tab[2].substring(0, 2);
				cal.set(1970, 01, 01, Integer.valueOf(tab[0]), Integer.valueOf(tab[1]), Integer.valueOf(tab[2])); // only HH:MM:SS is used
				prepStmt.setTime(12, new java.sql.Time( cal.getTime().getTime()));  
			}
			else {
				cal.set(1970, 01, 01, 23, 59, 59); // only HH:MM:SS is used
				prepStmt.setTime(12, new java.sql.Time( cal.getTime().getTime())); // chktim 06:00:00
			}
			prepStmt.setString(13, rad.getMsg());
			prepStmt.setString(14, rad.getSms());
			prepStmt.setString(15, rad.getMsg30());
			prepStmt.setString(16, rad.getConsole());
			
			prepStmt.setString(17, oId);
			prepStmt.setInt(   18, oPrio);

			System.out.println("Rows updated : "+prepStmt.executeUpdate());

		} catch (SQLException e) {
			System.out.println(e);
		} 
	}

	public void cpyRow(int row) {
		// se till att objektet User sparar v�rdet med r�tt set metod ( if (col == 2) .........
		System.out.println("wD cpyRow " + row);

		consoleStsROW rad;
		//		consoleStsROW radNew;

		rad = (consoleStsROW) map.get(row);
		int    oPrio = rad.getPrio();
		oPrio++;  // increase prio to create a unique key for the new row

		try {
			// insert new line with new timestamp and counter
			PreparedStatement st = conn.prepareStatement("INSERT INTO status (state,id,prio,type,status,body,rptdat,chkday,chktim,errors,accerr,msg,msgdat,console,condat,info,plugin,agent,sms,smsdat,msg30,msgdat30,chktimto) "
					+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			st.setString(1,"I"); // state
			st.setString(2,rad.getId()); // id
			st.setInt(3,oPrio); // prio
			st.setString(4,rad.getType().toUpperCase() ); // type
			st.setString(5,rad.getStatus().toUpperCase() ); // status 
			st.setString(6,rad.getBody() ); // 
//			st.setTimestamp(7,null); // rptdat
			cal.set(1970, 01, 01, 0, 0, 0); // set an old date to avoid null
			st.setTimestamp(7,new java.sql.Timestamp( cal.getTime().getTime())); // rptdat
			st.setString(8,rad.getChkday() ); // chkday

//						st.setTime(9, new java.sql.Time(6,0,0)); // chktim
			cal.set(1970, 01, 01, 6, 0, 0); // only HH:MM:SS is used
			st.setTime(9, new java.sql.Time( cal.getTime().getTime())); // chktim 06:00:00

			st.setInt(10,rad.getErrors()); // errors 
			st.setInt(11,rad.getAccerr()); // acceptable errors 
			st.setString(12," "); // msg 
			st.setTimestamp(13,null); // msgdat 
			st.setString(14," " ); // console
			st.setTimestamp(15,null); // condat
			st.setString(16,rad.getInfo()); // info
			st.setString(17,rad.getPlugin()); // plugin
			st.setString(18," "); // agent 
			st.setString(19," "); // sms 
			st.setTimestamp(20,null); // smsdat
			st.setString(21," "); // msg30 
			st.setTimestamp(22,null); // msgdat30
			cal.set(1970, 01, 01, 23, 59, 59); // only HH:MM:SS is used
			st.setTime(23, new java.sql.Time( cal.getTime().getTime())); // chktimto 23:59:59
			int rowsInserted = st.executeUpdate();
			st.close();
			System.out.println("Rows insterted : "+rowsInserted);

		} catch (SQLException e) {
			System.out.println(e);
		} 
	}

	public boolean openDB() {

		try {
			System.out.println("openDB " );
			Class.forName("org.postgresql.Driver").newInstance();
			DBUrl = "jdbc:postgresql://"+dbhost+":"+dbport+"/"+database;
			System.out.println(DBUrl);
			System.out.println("dbuser= " + dbuser +"  dbpassword "+ dbpassword);
			conn = DriverManager.getConnection(DBUrl,dbuser,dbpassword);
			conn.setAutoCommit(true);
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
				s = new String("select * from status order by id asc;"); 
			else
				s = new String("select * from status where "+ where +" order by id asc;");

			//			System.out.println("refreshData s: " + s);

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

			while (rs.next()) {
				//--
				//				System.out.println("rs.next");
				rad = new consoleStsROW();

				rad.setState(rs.getString("state"));
				rad.setId(rs.getString("id"));
				rad.setPrio(rs.getInt("prio"));
				rad.setType(rs.getString("type"));
				rad.setStatus(rs.getString("status"));
				rad.setBody(rs.getString("body"));
				rad.setRptdat(rs.getString("rptdat"));
				rad.setChkday(rs.getString("chkday"));
				rad.setChktim(rs.getString("chktim"));
				rad.setErrors(rs.getInt("errors"));
				rad.setAccerr(rs.getInt("accerr"));
				rad.setMsg(rs.getString("msg"));
				rad.setMsgdat(rs.getString("msgdat"));
				rad.setConsole(rs.getString("console"));
				rad.setCondat(rs.getString("condat"));
				rad.setInfo(rs.getString("info"));
				rad.setPlugin(rs.getString("plugin"));
				rad.setAgent(rs.getString("agent"));
				rad.setSms(rs.getString("sms"));
				rad.setSmsdat(rs.getString("smsdat"));
				rad.setMsg30(rs.getString("msg30"));
				rad.setMsgdat30(rs.getString("msgdat30"));
				rad.setChktimto(rs.getString("chktimto"));

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

		rad = new consoleStsROW();

		rad.setState(" ");
		rad.setId(" ");
		rad.setPrio(0);
		rad.setType(" ");
		rad.setStatus(" ");
		rad.setBody(" ");
		rad.setRptdat(" ");
		rad.setChkday(" ");
		rad.setChktim(" ");
		rad.setErrors(0);
		rad.setAccerr(0);
		rad.setMsg(" ");
		rad.setMsgdat(" ");
		rad.setConsole(" ");
		rad.setCondat(" ");
		rad.setInfo(" ");
		rad.setPlugin(" ");
		rad.setAgent(" ");
		rad.setSms(" ");
		rad.setSmsdat(" ");
		rad.setMsg30(" ");
		rad.setMsgdat30(" ");
		rad.setChktimto(" ");

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