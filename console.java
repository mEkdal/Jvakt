package Jvakt;

/*
 * 2022-06-23 V.54 Michael Ekdal		Added getVersion() to get at consistent version throughout all classes.
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.io.*;
import java.util.*;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.Timer;
import javax.swing.border.*;

// Extend of Jframe to get access to the swing metohods in Jframe. 
// Jframe is the base in the windows management.
//implementing TableModelListener to use this class as listener to Jtables datamodell class via the method tableChanged.
//implementing WindowListener to use this class as lyssnare to Jframe with the method windowClosing
public class console extends JFrame implements TableModelListener, WindowListener {

	// Creates variables
	static final long serialVersionUID = 42L;
	private JPanel topPanel;
	private JTable table;
	private JScrollPane scrollPane;
	private JButton bu1;
	private JTableHeader header;
	private consoleDM wD;
	private Boolean swAuto = true;
	private Boolean swRed = true; 
	private Boolean swDBopen = true; 
	private Boolean swServer = true; 
	private Boolean swDormant = true; 

	private Boolean swPropFile = true;

	private  String jvhost = "127.0.0.1";
	private  String jvport = "1956";
	private  int port = 1956; 
	private  String cmdHst  = "javaw -cp Jvakt.jar Jvakt.consoleHst";
	private  String cmdSts  = "javaw -cp Jvakt.jar Jvakt.consoleSts";
	private  String cmdStat = "javaw -cp Jvakt.jar Jvakt.StatisticsChartLauncher";

	private  int deselectCount = 0; 
	private  int jvconnectCount = 0; 

	private String infotxt; 

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws IOException {

		console mainFrame = new console();  // Creates an object of the current class  
		mainFrame.pack();                   // calling method pack which is ingetiyed from Jframe 
		mainFrame.setVisible(true);  	    // calling method setVisible so show all findows to the user

	}   // main is now in waiting mode waiting for all the other objects to end.


	// this is the constructor which starts from the static main method.
	// it creates all needed objects and connects them.
	// it also calls methods inherited from Jframe to set certain values.
	public console() throws IOException {

		ImageIcon img = new ImageIcon("console.png");
		setIconImage(img.getImage());

		// get the parameters from the console.properties file
		getProps();
		port = Integer.parseInt(jvport);

		// a function inherited from Jframe used to set a heading
		setTitle("Jvakt console "+getVersion()+".54 -  F1 = Help"); 

		//	        setSize(5000, 5000);

		// get the screen size as a java dimension
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		// get 2/5 of the height, and 2/3 of the width
		int height = screenSize.height * 1 / 5;
		int width = screenSize.width * 5 / 6;

		// set the jframe height and width
		setPreferredSize(new Dimension(width, height));

		// function in Jframe to set colors
		setBackground(Color.gray);
		setUndecorated(false);
		// creates a new Jpanel and saves the reference in topPanel
		topPanel = new JPanel();
		// tells topPanel which layout to use by create a BorderLayout object with no name.
		topPanel.setLayout(new BorderLayout());
		//topPanel.setLayout(new FlowLayout());
		// gets Jpanels simple content handler and inserts topPanel in stead to handle the rest of the objects
		getContentPane().add(topPanel);
		//topPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		// creates a data model to handle the data in the table
		wD = new consoleDM();
		// creates a Jtable and add the reference to wD via the Jtable contructor
		table = new JTable(wD);

		header = table.getTableHeader();
		header.setBackground(Color.LIGHT_GRAY);

		bu1 = new JButton();

		System.out.println("screenHeightWidth :" +screenSize.height+" " +screenSize.width);
		if (screenSize.height > 1200) {
			table.setRowHeight(table.getRowHeight()*2);
			header.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
			bu1.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
		}
		else 
			if (screenSize.height > 1080) {
				table.setRowHeight(table.getRowHeight()*1,5);
				header.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
				bu1.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
			}

		swServer = true;
		try {
			SendMsg jm = new SendMsg(jvhost, port);  // check if the Jvakt.Server is accessable
			String oSts = jm.open();
			//			System.out.println("#1 "+oSts);
			if (oSts.startsWith("failed")) 	swServer  = false;
			if (oSts.startsWith("DORMANT")) swDormant = true;
			else 							swDormant = false;
			jm.close();
		} 
		catch (NullPointerException npe2 )   {
			swServer = false;
			System.out.println("-- Rpt Failed 1 --" + npe2);
		}

		swDBopen = wD.refreshData(); // check if the DB is available
		setBu1Color();

		bu1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				swAuto = !swAuto;
				swDBopen = wD.refreshData();
				setBu1Color();
			}
		});

		// enables the table to accept multiple row selection
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// ask the table for the reference to the LIstSecectionModel object, the reference is saved in rowSM
		ListSelectionModel rowSM = table.getSelectionModel();

		//
		// NB internal class start---
		// Use the rowSM method to create a listener to the table to know which row is selected
		rowSM.addListSelectionListener(new ListSelectionListener()  {
			// the internal class method which gets the selected row
			public void valueChanged(ListSelectionEvent e)   {
				// Ignore extra messages.
				if (e.getValueIsAdjusting()) return;

				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty()) {
					//					System.out.println("No rows are selected.");
				} else {
					//					int selectedRow = lsm.getMinSelectionIndex();
					//					System.out.println("Row " + selectedRow + " is now selected.");
					deselectCount = 0;
				}
				return;
			}

		}   
				);
		// NB internal class end---
		//


		// sets auto sorting in the table    
		//	        table.setAutoCreateRowSorter(true);
		
		// tells the table data model object (wD) this object is listening; method tableChanged
		table.getModel().addTableModelListener(this);

		// consoleCR selects color on the rows
		consoleCR cr=new consoleCR();

		for (int i=0; i <= 7 ; i++ ) {
			table.getColumn(table.getColumnName(i)).setCellRenderer(cr);
		}


		// creates new JScrollPane and adds the table via the constructor. To be able to scroll the tables.

		scrollPane = new JScrollPane(table);
		table.setAutoResizeMode(JTable. 	AUTO_RESIZE_SUBSEQUENT_COLUMNS);

		TableColumn column = null;
		column = table.getColumnModel().getColumn(0);
		column.setPreferredWidth(400);
		column.setMaxWidth(1100);
		column = table.getColumnModel().getColumn(1);
		column.setPreferredWidth(30);
		column.setMaxWidth(65);
		column = table.getColumnModel().getColumn(2);
		column.setPreferredWidth(30);
		column.setMaxWidth(65);
		column = table.getColumnModel().getColumn(3);
		column.setPreferredWidth(255);
		column.setMaxWidth(895);
		column = table.getColumnModel().getColumn(4);
		column.setPreferredWidth(255);
		column.setMaxWidth(895);
		column = table.getColumnModel().getColumn(5);
		column.setPreferredWidth(100);
		column.setMaxWidth(420);
		column = table.getColumnModel().getColumn(6);
		column.setPreferredWidth(900);
		column.setMaxWidth(2800);
		column = table.getColumnModel().getColumn(7);
		column.setPreferredWidth(100);
		column.setMaxWidth(950);

		addKeyBindings();

		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		//	        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		// Creates two new JPanels to be used inside topPanel, also a JPanel
		topPanel.add(scrollPane, BorderLayout.CENTER);
		topPanel.add(bu1, BorderLayout.NORTH);
		// tells the current object to use itself as a listener. (methods for WindowListener)
		addWindowListener(this);

		Timer timer = new Timer(2000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (deselectCount > 10 ) {
					table.getSelectionModel().clearSelection();  // clear selected rows.
					deselectCount = 0;
				}
				deselectCount++;
				if (swAuto) {
					jvconnectCount++;
					if (jvconnectCount > 2 || !swServer) {    // keep the number of connection checks down because of limitations in Win10
						jvconnectCount = 0;
						try {
							swServer = true;
							SendMsg jm = new SendMsg(jvhost, port);  // check if the Jvakt.Server is started.
							String oSts = jm.open();
							if (oSts.startsWith("failed")) 	swServer  = false;
							if (oSts.startsWith("DORMANT")) swDormant = true;
							else 							swDormant = false;
							jm.close();
						} 
						catch (NullPointerException npe2 )   {
							swServer = false;
							System.out.println("-- Rpt Failed 2 --" + npe2);
						}
					}

					swDBopen = wD.refreshData();
					setBu1Color();
					if (swRed) scrollPane.setBorder(new LineBorder(Color.RED));
					else scrollPane.setBorder(new LineBorder(Color.CYAN));
					swRed = !swRed;
					scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
					scrollPane.validate();
					scrollPane.repaint();
					scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
					revalidate();
					repaint();	            
				}
			}
		});
		timer.start();

	} // end constructor


	// we implemented TableModelListener and added "this" so this method should be called at a change in the table
	// this is only used for logging
	public void tableChanged(TableModelEvent e)  {
		int row = e.getFirstRow();
		int column = e.getColumn();
		String ls ;
		TableModel model = (TableModel)e.getSource();
		//		String columnName = model.getColumnName(column);
		String data = (String)model.getValueAt(row, column);
		ls = "Workout tableChanged " + row + " " + column + " " +  data;
		System.out.println(ls);
	}

	public void setBu1Color()  {
		String txt = "";
		if (swAuto) {
			bu1.setBackground(Color.GRAY);
			txt = "Auto Update ON.";
		}
		else {
			bu1.setBackground(Color.yellow);
			txt = "Auto Update OFF.";
		}
		if (!swPropFile) {
			txt = txt + "  No console.properties file found. ";
		}
		if (!swDBopen) {
			bu1.setBackground(Color.RED);
			txt = txt + "  No connection with DB. ";
		}
		if (!swServer) {
			bu1.setBackground(Color.RED);
			txt = txt + "  No connection with JvaktServer. ";
		}
		else if (swDormant) {
			bu1.setBackground(Color.ORANGE);
			txt = txt + "  System DORMANT.";
		}
		else txt = txt +  "  System ACTIVE.";

		bu1.setText(txt);
	}

	private void addKeyBindings() {
		table.getActionMap().put("delRow", delRow());
		table.getActionMap().put("strHst", strHst());
		table.getActionMap().put("strSts", strSts());
		table.getActionMap().put("strStat", strStat());
		table.getActionMap().put("clearSel", clearSel());
		table.getActionMap().put("increaseH", increaseH());
		table.getActionMap().put("decreaseH", decreaseH());
		table.getActionMap().put("getInfo", getInfo());
		table.getActionMap().put("showHelp", showHelp());
		table.getActionMap().put("showLine", showLine());
		table.getActionMap().put("toggleDormant", toggleDormant());

		KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);  // delete key in mac
		table.getInputMap(JComponent.WHEN_FOCUSED).put(keyStroke, "delRow");

		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);  // delete key in win linux
		table.getInputMap(JComponent.WHEN_FOCUSED).put(keyStroke, "delRow");

		// Do not use VK_F2 beacuse JTable overides it sometimes
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0); 
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "showHelp");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_HELP, 0); 
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "showHelp");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "increaseH");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0);
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "decreaseH");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "strHst");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "strSts");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "showLine");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "toggleDormant");
//		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0);
//		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "strHst");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0); 
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "getInfo");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0);
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "strStat");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0);
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "strHst");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0);
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "delRow");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE , 0);
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "clearSel");

	}  

	private AbstractAction showHelp()  {
		AbstractAction save = new AbstractAction() {
			static final long serialVersionUID = 43L;
			@Override
			public void actionPerformed(ActionEvent e)  {
				//					                 JOptionPane.showMessageDialog(TestTableKeyBinding.this.table, "Action Triggered.");
				//				System.out.println("ShowHelp");
				JOptionPane.showMessageDialog(getContentPane(),
						"F1 : Help \nF3 : Increase font size \nF4 : Decrease font size \nF5 : History \nF6 : Status table \nF7 : Show row \nF8 : Toggle System active / dormant \nF9 : Enter info text \nF10 : Statistics  \n\nDEL : delete rows \nESC : Unselect\n" +
								"\nThis app shows the filtered reports/messages sent to the Jvakt server. OK messages of types 'R', 'T' and 'S' remains in the database." + 
								"\nThe upper bar acts a button to stop/start the automatic update. \nIt will also show the status of the server and database." + 
								"\n\nFields: " + 
								"\nId = The Id if the message. " + 
								"\nPrio = Prio 30 and higher is meant for office hours and messages will remain in the console. No mail or SMS." + 
								"\n       Below 30 is important and might trigger SMS and/or mail depending on chkday/chktim " + 
								"\n       Prio 10 or less is very important and will trigger SMS and/or mail 24/7. " +
								"\ntype = 'S' means a check that rptday is updated 'today'. The check is made once a day at the time in the chkday and chktim fields. " +
								"\n           When read and acted upon, the row may be selected and removed with the DEL button." +
								"\n           If not manually deleted it will be automatically removed the next time the check sends an OK report. Usually the next day." +
								"\ntype = 'R' means a check that rptdat is updated at least every 20 minute. The check starts from the time in chkday and chktim fields." +
								"\n           The message will disappear automatically when the issue is resolved. " +
								"\ntype = 'T' means no tome-out checks are made." +
								"\n           When read and acted upon the line may be selected and removed with the DEL button." +
								"\n           It will be automatically removed the next time the check sends an OK report." +
								"\n           When or if this will happen is unknown." +
								"\ntype = 'I' means impromptu messages. " +
								"\n           The 'I' type will not remain in the status table and can not be prepared in advance." +
								"\n           When read and acted upon the row must be selected and removed with the DEL button." +
								"\nCreDate = The inital time the message arrived the the console."+ 
								"\nConDate = The latest time the message was updated. "+ 
								"\nStatus  = ERR, INFO, OK or TOut."+ 
								"\n          TOut means the agent has stopped sending the expected status reports. This applied only to types 'S' and 'R'. "+ 
								"\nbody = Contains the text sent by the agent"+ 
								"\nagent = Contains the host name and IP address where the agent is executed."
								,"Jvakt Help",
								JOptionPane.INFORMATION_MESSAGE);
			}
		};
		return save;
	}


	private AbstractAction getInfo()  {
		AbstractAction save = new AbstractAction() {
			static final long serialVersionUID = 53L;
			@Override
			public void actionPerformed(ActionEvent e)  {

				table.getSelectionModel().clearSelection();  // clear selected rows.

				infotxt = JOptionPane.showInputDialog(getContentPane(),
						"Enter information text to be sent to the console\n" 
						,"Jvakt Info",
						JOptionPane.QUESTION_MESSAGE);
				if ((infotxt != null) && (infotxt.length() > 0)) {
					System.out.println("*** infotxt: " + infotxt);

					try {
						Message jmsg = new Message();
						SendMsg jm = new SendMsg(jvhost, port);
						System.out.println(jm.open());
						jmsg.setId("INFO-to-console");
						jmsg.setType("I");
						jmsg.setRptsts("INFO");
						jmsg.setBody(infotxt);
						jmsg.setAgent("GUI");
						if (jm.sendMsg(jmsg)) System.out.println("-- Rpt Delivered 5 --");
						else            	  System.out.println("-- Rpt Failed 5 --");
						jm.close();
					} 
					catch (Exception e2) {
						System.err.println(e2);
						System.err.println(e2.getMessage());
					}



				}
			}
		};
		return save;
	}


	private AbstractAction showLine()  {
		AbstractAction save = new AbstractAction() {
			static final long serialVersionUID = 44L;

			@Override
			public void actionPerformed(ActionEvent e)  {
				//				System.out.println("ShowLine");
				table.editingCanceled(null);
				table.editingStopped(null);
				int[] selectedRow = table.getSelectedRows();
				System.out.println("ShowLine: "+selectedRow.length);

				try {
					for (int i = 0; i <  selectedRow.length; i++) {
						//						System.out.println("*** Row to show :" + selectedRow[i]);
						Object ValueId   = table.getValueAt(selectedRow[i],table.getColumnModel().getColumnIndex("Id"));
						//						System.out.println(ValueId);
						String id = (String) ValueId;
						if (id == null) continue;
						//						System.out.println("*** " + selectedRow[i]);
						ValueId   = table.getValueAt(selectedRow[i],table.getColumnModel().getColumnIndex("Prio"));
						//						System.out.println(ValueId);
						int prio = (Integer) ValueId;
						ValueId   = table.getValueAt(selectedRow[i],table.getColumnModel().getColumnIndex("Type"));
						//						System.out.println(ValueId);
						String type = (String) ValueId;
						ValueId   = table.getValueAt(selectedRow[i],table.getColumnModel().getColumnIndex("CreDate"));
						//						System.out.println(ValueId);
						String credate = (String) ValueId;
						ValueId   = table.getValueAt(selectedRow[i],table.getColumnModel().getColumnIndex("ConDate"));
						//						System.out.println(ValueId);
						String condate = (String) ValueId;
						ValueId   = table.getValueAt(selectedRow[i],table.getColumnModel().getColumnIndex("Status"));
						//						System.out.println(ValueId);
						String status = (String) ValueId;
						ValueId   = table.getValueAt(selectedRow[i],table.getColumnModel().getColumnIndex("Body"));
						//						System.out.println(ValueId);
						String body = (String) ValueId;
						ValueId   = table.getValueAt(selectedRow[i],table.getColumnModel().getColumnIndex("Agent"));
						//						System.out.println(ValueId);
						String agent = (String) ValueId;
						JOptionPane.showMessageDialog(getContentPane(),
								"- ID (the id of the message. Together with prio it makes an unique id) -\n"+id+" \n\n" +
										"- Prio (the priority, part of the unique id. Below 30 trigger email and SMS text) -\n"+prio +"\n\n" + 
										"- Type (R=repeated, S=scheduled, I=immediate/impromptu, T=permanent with no time-out checks) -\n"+type +"\n\n" + 
										"- CreDate (the date it appeared in the console) -\n"+credate +"\n\n" + 
										"- ConDate (the date it updated in the console) -\n"+condate +"\n\n" + 
										"- Status (OK, INFO, TOut or ERR) -\n"+status +"\n\n" + 
										"- Body (any text) -\n"+body +"\n\n" + 
										"- Agent (description of the reporting agent) -\n"+agent  
										,						
										"Jvakt Show line",
										JOptionPane.INFORMATION_MESSAGE);
					}
				} 
				catch (Exception e2) {
					System.err.println(e2);
					System.err.println(e2.getMessage());
				}
				table.getSelectionModel().clearSelection();  // clear selected rows.				

			}
		};
		return save;
	}

	private AbstractAction clearSel()  {
		AbstractAction save = new AbstractAction() {
			static final long serialVersionUID = 45L;

			@Override
			public void actionPerformed(ActionEvent e)  {
				//					                 JOptionPane.showMessageDialog(TestTableKeyBinding.this.table, "Action Triggered.");
				table.getSelectionModel().clearSelection();  // clear selected rows.
			}
		};
		return save;
	}

	private AbstractAction increaseH()  {
		AbstractAction save = new AbstractAction() {
			static final long serialVersionUID = 52L;

			@Override
			public void actionPerformed(ActionEvent e)  {
				if (table.getRowHeight()<100) {
					table.setRowHeight(table.getRowHeight()+1);
					header.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
					bu1.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
				}
			}
		};
		return save;
	}

	private AbstractAction decreaseH()  {
		AbstractAction save = new AbstractAction() {
			static final long serialVersionUID = 46L;

			@Override
			public void actionPerformed(ActionEvent e)  {
				//				System.out.println("getRowHeight :" + table.getRowHeight());
				if (table.getRowHeight()>10) {
					table.setRowHeight(table.getRowHeight()-1);
					header.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
					bu1.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
				}
			}
		};
		return save;
	}


	private AbstractAction delRow()  {
		AbstractAction save = new AbstractAction() {
			static final long serialVersionUID = 47L;

			@Override
			public void actionPerformed(ActionEvent e)  {
				//	                 JOptionPane.showMessageDialog(TestTableKeyBinding.this.table, "Action Triggered.");
				table.editingCanceled(null);
				table.editingStopped(null);
				//				int selectedRow = table.getSelectedRow();
				int[] selectedRow = table.getSelectedRows();

				//				for (int i = 0; i <  selectedRow.length; i++) {
				//					System.out.println("*** Row do delete :" + selectedRow[i]);
				//				}

				//	                 if (selectedRow != -1) {
				//	                     ((DefaultTableModel) table.getModel()).removeRow(selectedRow);
				//	                 }

				try {
					for (int i = 0; i <  selectedRow.length; i++) {
						//						System.out.println("*** Row do delete :" + selectedRow[i]);
						Message jmsg = new Message();
						SendMsg jm = new SendMsg(jvhost, port);
						System.out.println("Response opening connection to Jvakt server: "+ jm.open());
						Object ValueId   = table.getValueAt(selectedRow[i],table.getColumnModel().getColumnIndex("Id"));
						//						System.out.println(ValueId);
						jmsg.setId(ValueId.toString());
						jmsg.setRptsts("OK");
						//						jmsg.setBody("Delete of row from GUI");
						ValueId   = table.getValueAt(selectedRow[i],table.getColumnModel().getColumnIndex("Body"));
//												System.out.println(ValueId);
						jmsg.setBody(ValueId.toString());
						//						jmsg.setBody("Delete of row from GUI");
						ValueId   = table.getValueAt(selectedRow[i],table.getColumnModel().getColumnIndex("Prio"));
						//						System.out.println(ValueId);
						jmsg.setPrio(Integer.parseInt(ValueId.toString()));
						jmsg.setType("D");
						jmsg.setAgent("GUI");
						//						jm.sendMsg(jmsg);
						if (jm.sendMsg(jmsg)) System.out.println("-- Rpt Delivered 3 --");
						else            	  System.out.println("-- Rpt Failed 3 --");
						jm.close();
					}
				} 
				catch (Exception e2) {
					System.err.println(e2);
					System.err.println(e2.getMessage());
				}
				table.getSelectionModel().clearSelection();  // clear selected rows.
			}
		};
		return save;
	}

	private AbstractAction toggleDormant()  {
		AbstractAction save = new AbstractAction() {
			static final long serialVersionUID = 48L;

			@Override
			public void actionPerformed(ActionEvent e)  {


				Object[] options = { "OK", "Cancel" };
				int n = JOptionPane.showOptionDialog(null, "Do you want to toggle System active / dormant?", "Toggle active / dormant",
						JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
						null, options, options[0]);

				if (n==1) {
					System.out.println("-- Cancel Toggle dormant ---");	
				} else {
//					System.out.println("-- OK to Toggle dormant ---");
					try {
						Message jmsg = new Message();
						SendMsg jm = new SendMsg(jvhost, port);
						System.out.println(jm.open());
						jmsg.setId("Jvakt");
						if (swDormant) jmsg.setType("Active");
						else jmsg.setType("Dormant");
						jmsg.setAgent("GUI");
						if (jm.sendMsg(jmsg)) System.out.println("-- Rpt Delivered --");
						else            	  System.out.println("-- Rpt Failed 4 --");
						jm.close();
					} 
					catch (Exception e2) {
						System.err.println(e2);
						System.err.println(e2.getMessage());
					}

				}

			}
		};
		return save;
	}

	//************
	private AbstractAction strHst()  {
		AbstractAction save = new AbstractAction() {
			static final long serialVersionUID = 49L;

			@Override
			public void actionPerformed(ActionEvent e)  {
				//				System.out.println("-- Start consoleHst: " + cmdHst);

				try {
					Runtime.getRuntime().exec(cmdHst);
				} catch (IOException e1) {
					System.err.println(e1);
					System.err.println(e1.getMessage());
				}

			}
		};
		return save;
	}
	//	************
	private AbstractAction strStat()  {
		AbstractAction save = new AbstractAction() {
			static final long serialVersionUID = 49L;

			@Override
			public void actionPerformed(ActionEvent e)  {
				//				System.out.println("-- Start consoleHst: " + cmdHst);

				try {
					//			       Runtime.getRuntime().exec("java -cp \"/Users/septpadm/OneDrive - Perstorp Group/JavaSrc;/Users/septpadm/OneDrive - Perstorp Group/JavaSrc/postgresql-42.1.3.jar\" Jvakt.consoleHst");
					Runtime.getRuntime().exec(cmdStat);
				} catch (IOException e1) {
					System.err.println(e1);
					System.err.println(e1.getMessage());
				}

			}
		};
		return save;
	}
	//************
	
	
	private AbstractAction strSts()  {
		AbstractAction save = new AbstractAction() {
			static final long serialVersionUID = 50L;

			@Override
			public void actionPerformed(ActionEvent e)  {
				//				System.out.println("-- Start consoleSts: " + cmdSts);

				try {
					//			       Runtime.getRuntime().exec("java -cp \"/Users/septpadm/OneDrive - Perstorp Group/JavaSrc;/Users/septpadm/OneDrive - Perstorp Group/JavaSrc/postgresql-42.1.3.jar\" Jvakt.consoleHst");
					Runtime.getRuntime().exec(cmdSts);
				} catch (IOException e1) {
					System.err.println(e1);
					System.err.println(e1.getMessage());
				}

			}
		};
		return save;
	}
	//	************


	// windows listeners
	// we implemented WindowListener and added "this" so this method should be used at a normal ending of Jframe 
	public void windowClosing(WindowEvent e) {
		//skriv userDB
		wD.closeDB();
		System.exit(0);
		// ...and now all ends..!!!... 
	}

	void getProps() {

		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("console.properties");
			prop.load(input);
			// get the property value and print it out
			jvport   = prop.getProperty("jvport");
			jvhost   = prop.getProperty("jvhost");
			cmdHst   = prop.getProperty("cmdHst");
			cmdSts   = prop.getProperty("cmdSts");
			cmdStat   = prop.getProperty("cmdStat");
			input.close();
		} catch (IOException ex) {
			swPropFile = false;
			System.out.println("The console.properties file was not found! Am using default values!  ");
			System.err.println(ex);
		}    	
	}

	static String getVersion() {
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

	
	// vi implementerade WindowListener men följande metoder avänds inte 
	public void windowClosed(WindowEvent e) {    }
	public void windowOpened(WindowEvent e) {    }
	public void windowIconified(WindowEvent e) {    }
	public void windowDeiconified(WindowEvent e) {    }
	public void windowActivated(WindowEvent e) {    }
	public void windowDeactivated(WindowEvent e) {    }

}
