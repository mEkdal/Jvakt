package Jvakt;
/*
 * 2023-11-21 V.01 Michael Ekdal		New pgm to list the logs in the DB
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.io.*;
import java.util.*;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.Timer;
import javax.swing.border.*;

//Extend of Jframe to get access to the swing metohods in Jframe. 
//Jframe is the base in the windows management.
//implementing TableModelListener to use this class as listener to Jtables datamodell class via the method tableChanged.
//implementing WindowListener to use this class as lyssnare to Jframe with the method windowClosing
public class consoleLogsView extends JFrame implements TableModelListener, WindowListener {
 
	static final long serialVersionUID = 42L;
	private JPanel topPanel;
	private JPanel usrPanel;
	private JPanel srchPanel;

	private JTable table;
	private JScrollPane scrollPane;
	private JButton bu1;
	private JToggleButton buSrch;

	private JMenuBar menuBar;      
	private JMenu menu, menuPgm, menuRow, menuAbout;   
	private JMenuItem menuItem;    
	private JTextField where;
	private JTableHeader header;
	private consoleLogsViewDM wD;
	private Boolean swAuto = true;
	private Boolean swRed = true; 
	private Boolean swDBopen = true; 
	private Boolean swServer = true; 
	private Boolean swDormant = true; 
	private  String jvhost = "127.0.0.1";
	private  String jvport = "1956";
	private  int port = 1956; 
	private  int deselectCount = 0; 
	private  int jvconnectCount = 0; 
	
	static String id;
	static String origin;
	static String credat;


	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws IOException {
		
		System.out.println("consoleLogsView 2023-11-21.");
		
		if (args.length < 1) {
			System.out.println("\n\nconsoleLogsView 2023-11-21. The parameters and their meaning are:\n"+
					"\n-id     \t"+
					"\n-origin \t."+
					"\n-credat \t."
					);

			System.exit(4);
		}

		// reads command line arguments
		for ( int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-id")) {
				id = args[++i];
			}
			if (args[i].equalsIgnoreCase("-origin"))  {
				origin = args[++i];
			}
			if (args[i].equalsIgnoreCase("-credat")) { 
				credat = args[++i];
			}
		}
		
		System.out.println("param: "+id+" "+origin+" "+credat);


		consoleLogsView mainFrame = new consoleLogsView();  // Creates an object of the current class 
		mainFrame.pack();                         // calling method pack which is ingetiyed from Jframe 
		mainFrame.setVisible(true);  	    	  // calling method setVisible so show all findows to the user
		
	}  // main is now in waiting mode waiting for all the other objects to end.


	// this is the constructor which starts from the static main method.
	// it creates all needed objects and connects them.
	// it also calls methods inherited from Jframe to set certain values.
	public consoleLogsView() throws IOException {

		ImageIcon img = new ImageIcon("console.png");
		setIconImage(img.getImage());

		// get the parameters from the console.properties file
		getProps();
		port = Integer.parseInt(jvport);

		// funktion in Jframe to set the title
		setTitle("Jvakt consoleLogsView "+getVersion()+".01  --- "+id+" - "+origin+" - "+credat);
		//	        setSize(5000, 5000);

		// get the screen size as a java dimension
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		// get 2/5 of the height, and 2/3 of the width
		int height = screenSize.height * 2 / 5;
		int width = screenSize.width * 7 / 10;
		
		setLocation(60,60);    // Setting upper left corner 

		// set the jframe height and width
		setPreferredSize(new Dimension(width, height));


		// function in Jframe to set colors
		setBackground(Color.gray);
		setUndecorated(false);
		// creates a new Jpanel and saves the reference in topPanel
		topPanel = new JPanel();
		// tells topPanel which layout to use by create a BorderLayout object with no name.
		topPanel.setLayout(new BorderLayout());
		//		topPanel.setLayout(new FlowLayout());
		// gets Jpanels simple content handler and inserts topPanel in stead to handle the rest of the objects
		getContentPane().add(topPanel);
		//topPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		// creates a new Jpanel and saves the reference in topPanel
		srchPanel = new JPanel();
		// tells topPanel which layout to use by create a BorderLayout object with no name.
		srchPanel.setLayout(new FlowLayout());

		// creates a data model to handle the data in the table
		wD = new consoleLogsViewDM();
		System.out.println("param wD: "+id+" "+origin+" "+credat);
		wD.setId(id);
		wD.setOrigin(origin);
		wD.setCredat(credat);
		// creates a Jtable and add the reference to wD via the Jtable contructor
		table = new JTable(wD);
		table.setVisible(true);

		header = table.getTableHeader();
		header.setBackground(Color.LIGHT_GRAY);

		bu1 = new JButton();
		buSrch = new JToggleButton();
		buSrch.setText("Search");
		
		//Create the menu .
		menuBar = new JMenuBar();
		//Build the first menu.
		menu    = new JMenu("File");
		menuPgm = new JMenu("Programs");
		menuRow = new JMenu("Rows");
		menuAbout = new JMenu("About");
//		menu.setMnemonic(KeyEvent.VK_A);
//		menu.getAccessibleContext().setAccessibleDescription("The only menu in this program that has menu items");
		menuBar.add(menu);
		menuBar.add(menuRow);
		menuBar.add(menuAbout);
		//a group of JMenuItems
		menuItem = new JMenuItem("Unselect row(s) (ESC)");
		menuItem.addActionListener(clearSel());
		menuRow.add(menuItem);
		menuItem = new JMenuItem("Increase font (F3)");
		menuItem.addActionListener(increaseH());
		menuRow.add(menuItem);
		menuItem = new JMenuItem("Decrease font (F4)");
		menuItem.addActionListener(decreaseH());
		menuRow.add(menuItem);
		menuItem = new JMenuItem("Show selected row in separate window (F7)");
		menuItem.addActionListener(showLine());
		menuRow.add(menuItem);
		menuItem = new JMenuItem("Help (F1)");
		menuItem.addActionListener(showHelp());
		menu.add(menuItem);
		menuItem = new JMenuItem("Help (F1)");
		menuItem.addActionListener(showHelp());
		menuAbout.add(menuItem);
		menuItem = new JMenuItem("About");
		menuItem.addActionListener(showAbout());
		menuAbout.add(menuItem);
		
		setJMenuBar(menuBar);
		
		where = new JTextField(40);
		where.setText("row ilike '%error%'");

		System.out.println("screenHeightWidth :" +screenSize.height+" " +screenSize.width);
		System.out.println("param: "+id+" "+origin+" "+credat);
		if (screenSize.height > 1200) {
			table.setRowHeight(table.getRowHeight()*2);

//			table.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
//			table.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
			
			header.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
			bu1.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
			where.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
		}
		else 
			if (screenSize.height > 1080) {
				table.setRowHeight(table.getRowHeight()*1,5);

//				table.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
//				table.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
				
				header.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
				bu1.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
				where.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
			}


		swServer = true;
		try {
			SendMsg jm = new SendMsg(jvhost, port);  // check if Jvakt.Server is accessible
			String oSts = jm.open();
			//			System.out.println("#1 "+oSts);
			if (oSts.startsWith("failed")) 	swServer  = false;
			if (oSts.startsWith("DORMANT")) swDormant = true;
			else 							swDormant = false;
			jm.close();
		} 
		catch (NullPointerException npe2 )   {
			swServer = false;
			System.out.println("-- Rpt Failed --" + npe2);
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
			// interna classens metod som tar fram vilken rad som valts
			public void valueChanged(ListSelectionEvent e)   {
				// Ignore extra messages.
				if (e.getValueIsAdjusting())
					return;

				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty()) {
//					System.out.println("No rows are selected.");
				} else {
//					int selectedRow = lsm.getMinSelectionIndex();
//					System.out.println("Row " + selectedRow + " is now selected.");
					deselectCount = 0;
				}
			}

		}    );
		// NB internal class end---
		//


		// sets auto sorting in the table      
		//	        table.setAutoCreateRowSorter(true);
		
		// tells the table data model object (wD) this object is listening; method tableChanged
		table.getModel().addTableModelListener(this);

		// consoleCR selects color on the rows
		consoleLogsViewCR cr = new consoleLogsViewCR();

		for (int i=0; i < 2 ; i++ ) {      
			table.getColumn(table.getColumnName(i)).setCellRenderer(cr);
		}

		// creates new JScrollPane and adds the table via the constructor. To be able to scroll the tables.

		scrollPane = new JScrollPane(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

		TableColumn column = null;
		column = table.getColumnModel().getColumn(0);
		column.setResizable(true);
		column.setPreferredWidth(15);
		column.setMaxWidth(555);
		column = table.getColumnModel().getColumn(1);
		column.setResizable(true);
		column.setPreferredWidth(445);
		column.setMaxWidth(12555);

		addKeyBindings();

		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		//	        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		// Creates new JPanels to be used inside topPanel, also a JPanel
		usrPanel = new JPanel();
		usrPanel.setLayout(new BorderLayout());
		usrPanel.add(bu1, BorderLayout.NORTH);
//		usrPanel.add(where, BorderLayout.CENTER);
		
		srchPanel.add(buSrch);
		srchPanel.add(where);
		usrPanel.add(srchPanel, BorderLayout.CENTER);
		
		topPanel.add(usrPanel, BorderLayout.NORTH);
		topPanel.add(scrollPane, BorderLayout.CENTER);
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
					swAuto=false;
					jvconnectCount++;
					if (jvconnectCount > 5 || !swServer) {    // keep the number of connections down because of limitations in Win10
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
							System.out.println("-- Rpt Failed --" + npe2);
						}
					}

					if (where.getText().length() > 5) {
//						wD.setWhere(where.getText());
						if (buSrch.isSelected()) wD.setWhere(where.getText().trim()+" ");
						else      				 wD.setWhere(where.getText().trim());
					}
					else {			
						where.setText("row ilike '%error%'");
					}
					wD.setId(id);
					wD.setOrigin(origin);
					wD.setCredat(credat);

					swDBopen = wD.refreshData();
//					table.setVisible(true);
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

	} // end construktor


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
//			bu1.setBackground(Color.yellow);
			bu1.setBackground(Color.green);
			txt = "Auto Update OFF.";
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
		table.getActionMap().put("clearSel", clearSel());
		table.getActionMap().put("increaseH", increaseH());
		table.getActionMap().put("decreaseH", decreaseH());
		table.getActionMap().put("showHelp", showHelp());
		table.getActionMap().put("showLine", showLine());

		KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE , 0);
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "clearSel");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0); 
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "showHelp");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_HELP, 0); 
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "showHelp");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "increaseH");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0);
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "decreaseH");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
		table.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "showLine");

	}  

	private AbstractAction showHelp()  {
		AbstractAction save = new AbstractAction() {
			static final long serialVersionUID = 43L;
			@Override
			public void actionPerformed(ActionEvent e)  {
				//					                 JOptionPane.showMessageDialog(TestTableKeyBinding.this.table, "Action Triggered.");
//				System.out.println("ShowHelp");
//				JOptionPane pane = new JOptionPane("Jvakt help");
				JOptionPane.showMessageDialog(getContentPane(),
						"F1 : Help \nF3 : Increase font size \nF4 : Decrease font size\nF7 : Show line \n\nESC : Unselect " +
								"\n\nThe SEARCH field (a PostgreSQL 'where' statement) is active when the search button is active" + 
								"\n\nJvakt Help\n" 
								,"Jvakt Help",
								JOptionPane.INFORMATION_MESSAGE);

			}
		};
		return save;
	}

	private AbstractAction showLine()  {
		AbstractAction save = new AbstractAction() {
			static final long serialVersionUID = 44L;
			@Override
			public void actionPerformed(ActionEvent e)  {
				System.out.println("ShowLine");
				table.editingCanceled(null);
				table.editingStopped(null);
				int[] selectedRow = table.getSelectedRows();

				try {
					for (int i = 0; i <  selectedRow.length; i++) {
						System.out.println("*** Row to show :" + selectedRow[i]);
						Object ValueId   = table.getValueAt(selectedRow[i],table.getColumnModel().getColumnIndex("Row"));
						System.out.println("Row: "+ValueId);
						String row = (String) ValueId;
						if (row == null) continue;
						ValueId   = table.getValueAt(selectedRow[i],table.getColumnModel().getColumnIndex("Line"));
						System.out.println("Line: "+ValueId);
						int line = (int) ValueId;
						JOptionPane.showMessageDialog(getContentPane(),
//								"- Row  -\n"+row+"\n\n" ,						
								"Line "+line+":\n"+row ,						
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
				//	                 JOptionPane.showMessageDialog(TestTableKeyBinding.this.table, "Action Triggered.");
				table.getSelectionModel().clearSelection();  // clear selected rows.
			}
		};
		return save;
	}

	private AbstractAction increaseH()  {
		AbstractAction save = new AbstractAction() {
			static final long serialVersionUID = 46L;
			@Override
			public void actionPerformed(ActionEvent e)  {
				if (table.getRowHeight()<100) {
					table.setRowHeight(table.getRowHeight()+1);
					header.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
					bu1.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
					where.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
				}
			}
		};
		return save;
	}

	private AbstractAction decreaseH()  {
		AbstractAction save = new AbstractAction() {
			static final long serialVersionUID = 47L;
			
			@Override
			public void actionPerformed(ActionEvent e)  {
				//								System.out.println("getRowHeight :" + table.getRowHeight());
				if (table.getRowHeight()>10) {
					table.setRowHeight(table.getRowHeight()-1);
					header.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
					bu1.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
					where.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
				}
			}
		};
		return save;
	}



	// windows listeners
	// wei implemented WindowListener and added "this" to make this method to be called at the normal end of Jframe 
	public void windowClosing(WindowEvent e) {
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
			input.close();
		} catch (IOException ex) {
			// ex.printStackTrace();
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


	// we implemented WindowListener but the following methods is not used 
	public void windowClosed(WindowEvent e) {    }
	public void windowOpened(WindowEvent e) {    }
	public void windowIconified(WindowEvent e) {    }
	public void windowDeiconified(WindowEvent e) {    }
	public void windowActivated(WindowEvent e) {    }
	public void windowDeactivated(WindowEvent e) {    }
	
	private AbstractAction showAbout()  {
		AbstractAction save = new AbstractAction() {
			static final long serialVersionUID = 43L;
			@Override
			public void actionPerformed(ActionEvent e)  {
				//					                 JOptionPane.showMessageDialog(TestTableKeyBinding.this.table, "Action Triggered.");
				//				System.out.println("ShowHelp");
				JOptionPane.showMessageDialog(getContentPane(),
						"Version: "+getVersion()+ 
						"\n\nJvakt is a simple reactive monitoring system/toolbox." +
						"\n\nJvakt is distributed under the MIT License (i.e. It is free of charge to use)"+
						"\nhttps://github.com/mEkdal/Jvakt/blob/master/LICENSE"+
						"\n\nDownload Jvakt and read the wiki documentation at the Github site"+
						"\nhttps://github.com/mEkdal/Jvakt/wiki" +  
						"\n\nby Michael Ekdal" 
						,"Jvakt About",
								JOptionPane.INFORMATION_MESSAGE);
			}
		};
		return save;
	}


}
