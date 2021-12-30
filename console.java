package Jvakt;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Annika
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.io.*;
//import java.sql.SQLException;
import java.util.*;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.Timer;
import javax.swing.border.*;

// Extend av jframe för att få tillgång till swing metoderna i Jframe. 
// Jframe är basen i fönsterhanteringen.
//implementerar TableModelListener för att använda denna class som lyssnare till Jtables datamodellclass via metoden tableChanged.
//implementerar WindowListener f�r att använda denna class som lyssnare till Jframe med metoden windowClosing; och där tömma data till filer.
public class console extends JFrame implements TableModelListener, WindowListener {

	// Skapar diverse variabler
	static final long serialVersionUID = 42L;
	private JPanel topPanel;
	//	private JPanel usrPanel;
	//	private JPanel logPanel;
	private JTable table;
	private JScrollPane scrollPane;
	private JButton bu1;
	private JTableHeader header;
	//	    private JScrollPane scrollPane2;
	private consoleDM wD;
	private Boolean swAuto = true;
	//	private Boolean swAuto = false;
	private Boolean swRed = true; 
	private Boolean swDBopen = true; 
	private Boolean swServer = true; 
	private Boolean swDormant = true; 

	private Boolean swPropFile = true;

	//	private  String host = "193.234.149.176";
	private  String jvhost = "127.0.0.1";
	private  String jvport = "1956";
	private  int port = 1956; 
	private  String cmdHst  = "java -cp console.jar;postgresql.jar Jvakt.consoleHst";
	private  String cmdSts  = "java -cp console.jar;postgresql.jar Jvakt.consoleSts";
	private  String cmdStat = "java -cp console.jar;postgresql.jar Jvakt.StatisticsChartLauncher";

	private  int deselectCount = 0; 
	private  int jvconnectCount = 0; 

	private String infotxt; 

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws IOException {

		console mainFrame = new console();  // gör objekt av innevarande class 
		mainFrame.pack();                   // kallar på innevarande class metod pack som ärvts via Jframe 
		mainFrame.setVisible(true);  	    // kallar på innevarande class metod setVisible och nu visas fönster för användaren

	}   // main står nu och "väntar" vid slutet tills de andra objekten avslutas.


	// construktorn som startas i den statiska main metoden.
	// skapar alla inblandade objekt och kopplar ihop dom.
	// kallar också på metoder ärvda från Jframe att sätta vissa värden.
	public console() throws IOException {

		ImageIcon img = new ImageIcon("console.png");
		setIconImage(img.getImage());

		// get the parameters from the console.properties file
		getProps();
		port = Integer.parseInt(jvport);

		// funktion från Jframe att sätta rubrik
		setTitle("Jvakt console 2.52  -  F1 = Help"); 
		//	        setSize(5000, 5000);

		// get the screen size as a java dimension
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		// get 2/5 of the height, and 2/3 of the width
		int height = screenSize.height * 1 / 5;
		int width = screenSize.width * 5 / 6;

		// set the jframe height and width
		setPreferredSize(new Dimension(width, height));


		// funktion från Jframe att sätta färg
		setBackground(Color.gray);
		setUndecorated(false);
		// skapar ny Jpanel och sparar referensen i topPanel
		topPanel = new JPanel();
		// berättar för topPanel vilken layout den ska använda genom att skapa ett BorderLayout object utan namn.
		topPanel.setLayout(new BorderLayout());
		//topPanel.setLayout(new FlowLayout());
		// Hämtar Jpanels enkla content hanterare och lägger dit topPanel i stället att hantera resten av objekten
		getContentPane().add(topPanel);
		//topPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		// Skapar datamodel för datahanteringen av userDB i table
		wD = new consoleDM();
		// skapar en Jtable och lägger till referensen till wD via Jtables contructor
		// table kommer att visa userDB
		table = new JTable(wD);

		//		JTableHeader header = table.getTableHeader();
		header = table.getTableHeader();
		header.setBackground(Color.LIGHT_GRAY);
		//		header.setBackground(Color.white);

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
			SendMsg jm = new SendMsg(jvhost, port);  // kollar om JvaktServer är tillgänglig.
			String oSts = jm.open();
			//			System.out.println("#1 "+oSts);
			if (oSts.startsWith("failed")) 	swServer  = false;
			if (oSts.startsWith("DORMANT")) swDormant = true;
			else 							swDormant = false;
			jm.close();
		} 
//		catch (IOException e1) {
//			swServer = false;
//			System.err.println(e1);
//			//			System.err.println(e1.getMessage());
//		}
		catch (NullPointerException npe2 )   {
			swServer = false;
			System.out.println("-- Rpt Failed 1 --" + npe2);
		}

		//		System.out.println("swServer :" + swServer);

		swDBopen = wD.refreshData(); // kollar om DB �r tillgänglig
		setBu1Color();

		bu1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				swAuto = !swAuto;
				swDBopen = wD.refreshData();
				setBu1Color();
			}
		});

		// talar om för table att man bara får välja en rad i taget
		//		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// ber table om referensen till LIstSecectionModel objektet, sparar i rowSM
		ListSelectionModel rowSM = table.getSelectionModel();

		//
		// OBS intern class start---
		// Använder rowSM metod för att skapa lyssnare till table för att veta vilken rad som väljs.
		rowSM.addListSelectionListener(new ListSelectionListener()  {
			// interna classens metod som tar fram vilken rad som valts
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
		// OBS intern class end---
		//


		// s�tter automatsortering i tabellerna    
		//	        table.setAutoCreateRowSorter(true);
		// talar om för tabellernas datamodellobjekt (wD o wD2) att detta objekt lyssnar; metoden tableChanged
		table.getModel().addTableModelListener(this);

		// sätter färg på raderna
		consoleCR cr=new consoleCR();

		//		for (int i=0; i <= 8 ; i++ ) {      
		//			table.getColumn(table.getColumnName(i)).setCellRenderer(cr);
		//		}

		for (int i=0; i <= 7 ; i++ ) {
			table.getColumn(table.getColumnName(i)).setCellRenderer(cr);
		}


		// skapar nya JScrollPane och lägger till tabellerna via construktorn. För att kunna scrolla tabellerna.

		scrollPane = new JScrollPane(table);
		//		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setAutoResizeMode(JTable. 	AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		//	        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		TableColumn column = null;
		//		column = table.getColumnModel().getColumn(0);
		//		column.setPreferredWidth(30);
		//		column.setMaxWidth(85);
		//		column = table.getColumnModel().getColumn(1);
		//		column.setPreferredWidth(400);
		//		column.setMaxWidth(1100);
		//		column = table.getColumnModel().getColumn(2);
		//		column.setPreferredWidth(30);
		//		column.setMaxWidth(65);
		//		column = table.getColumnModel().getColumn(3);
		//		column.setPreferredWidth(30);
		//		column.setMaxWidth(65);
		//		column = table.getColumnModel().getColumn(4);
		//		column.setPreferredWidth(255);
		//		column.setMaxWidth(895);
		//		column = table.getColumnModel().getColumn(5);
		//		column.setPreferredWidth(255);
		//		column.setMaxWidth(895);
		//		column = table.getColumnModel().getColumn(6);
		//		column.setPreferredWidth(100);
		//		column.setMaxWidth(420);
		//		column = table.getColumnModel().getColumn(7);
		//		column.setPreferredWidth(900);
		//		column.setMaxWidth(2800);
		//		column = table.getColumnModel().getColumn(8);
		//		column.setPreferredWidth(100);
		//		column.setMaxWidth(950);

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

		// skapar två nya JPanel att användas inuti topPanel, som också är en JPanel
		//	        usrPanel = new JPanel();
		//	        usrPanel.setLayout(new BorderLayout());
		//	        logPanel = new JPanel();
		//	        logPanel.setLayout(new BorderLayout());
		// talar om för de nya JPanels vilka scrollPanes dom ska innehålla (scrollPanes innehåller tabellerna).
		//	        usrPanel.add(scrollPane, BorderLayout.CENTER);
		topPanel.add(scrollPane, BorderLayout.CENTER);
		// talar om för topPanel att den ska innehålla två JPanelobjekt NORTH och CENTER       
		//	        usrPanel.add(bu1, BorderLayout.NORTH);
		topPanel.add(bu1, BorderLayout.NORTH);
		//	        topPanel.add(usrPanel, BorderLayout.NORTH);
		//	        topPanel.add(logPanel, BorderLayout.CENTER);
		// talar om för innevarande object att den lyssnar på sig själv. (metoderna för WindowListener)
		addWindowListener(this);

		Timer timer = new Timer(2500, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//	              button.setBackground(flag ? Color.green : Color.yellow);
				//	              flag = !flag;
				if (deselectCount > 10 ) {
					table.getSelectionModel().clearSelection();  // clear selected rows.
					deselectCount = 0;
				}
				deselectCount++;
				if (swAuto) {
					jvconnectCount++;
					if (jvconnectCount > 5 || !swServer) {    // keep the number of connections down because of limitations in Win10
						jvconnectCount = 0;
						try {
							swServer = true;
							SendMsg jm = new SendMsg(jvhost, port);  // kollar om JvaktServer är tillgänglig.
							String oSts = jm.open();
							//						System.out.println("#1 "+oSts);
							if (oSts.startsWith("failed")) 	swServer  = false;
							if (oSts.startsWith("DORMANT")) swDormant = true;
							else 							swDormant = false;
							jm.close();
						} 
//						catch (IOException e1) {
//							swServer = false;
//							System.err.println(e1);
//							//						System.err.println(e1.getMessage());
//						}
						catch (NullPointerException npe2 )   {
							swServer = false;
							System.out.println("-- Rpt Failed 2 --" + npe2);
						}
						//						System.out.println("swServer 2 : " + swServer);
					}

					swDBopen = wD.refreshData();
					//	            	if (!swDBopen) {
					setBu1Color();
					//	            	}
					//	            	table.repaint();
					//	            	
					if (swRed) scrollPane.setBorder(new LineBorder(Color.RED));
					else scrollPane.setBorder(new LineBorder(Color.CYAN));
					swRed = !swRed;
					scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
					scrollPane.validate();
					scrollPane.repaint();
					//	            	scrollPane.setAutoscrolls(true);
					scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

					//	            	topPanel.repaint();
					//	            	pack();
					revalidate();
					repaint();	            
				}
			}
		});
		timer.start();

	} // slut construktor


	// vi implementerade TableModelListener och addade "this" f�r att denna metod skulle anropas vid änding av värde i tabellen
	// detta användas bara för loggning
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
			//			bu1.setBackground(Color.LIGHT_GRAY);
			txt = "Auto Update ON.";
			//	            bu1.setText("Auto Update ON");
		}
		else {
			bu1.setBackground(Color.yellow);
			txt = "Auto Update OFF.";
			//		              bu1.setText("Auto Update OFF");
		}
		if (!swPropFile) {
			txt = txt + "  No console.properties file found. ";
		}
		if (!swDBopen) {
			bu1.setBackground(Color.RED);
			//    		if (swAuto) bu1.setText("No connection with DB. Autoupdate ON");
			//    		else 		bu1.setText("No connection with DB. Autoupdate OFF");
			txt = txt + "  No connection with DB. ";

			//    		swAuto = false;
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
//					catch (IOException e1) {
//						System.err.println(e1);
//						System.err.println(e1.getMessage());
//					}
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
						//						System.out.println(ValueId);
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
//				catch (IOException e1) {
//					System.err.println(e1);
//					System.err.println(e1.getMessage());
//				}
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
//					catch (IOException e1) {
//						System.err.println(e1);
//						System.err.println(e1.getMessage());
//					}
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
					//			       Runtime.getRuntime().exec("java -cp \"/Users/septpadm/OneDrive - Perstorp Group/JavaSrc;/Users/septpadm/OneDrive - Perstorp Group/JavaSrc/postgresql-42.1.3.jar\" Jvakt.consoleHst");
					Runtime.getRuntime().exec(cmdHst);
				} catch (IOException e1) {
					System.err.println(e1);
					System.err.println(e1.getMessage());
				}

				//			       String[] par = new String[] { "One", "Two", "Three" };
				//			       try {
				//			    	   consoleHst.main(par);
				//			       } catch (IOException e1) {
				//			    	   System.err.println(e1);
				//			    	   System.err.println(e1.getMessage());
				//			       }

				//			       new Thread() {
				//			    	   public void run(){
				//			    		   String[] par = new String[] { "One", "Two", "Three" };
				//			    		   try {
				//			    			   consoleHst.main(par);
				//			    		   } 		catch (IOException e1) {
				//			    			   System.err.println(e1);
				//			    			   System.err.println(e1.getMessage());
				//			    		   }
				//
				//			    	   }
				//			       }.start();

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
	// vi implementerade WindowListener och addade "this" för att denna metod skulle anropas vid normalt avslut av Jframe 
	public void windowClosing(WindowEvent e) {
		//skriv userDB
		wD.closeDB();
		System.exit(0);
		// ...och h�r �r det slut i rutan..!!!... 
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
			// ex.printStackTrace();
		}    	
	}


	// vi implementerade WindowListener men följande metoder avänds inte 
	public void windowClosed(WindowEvent e) {    }
	public void windowOpened(WindowEvent e) {    }
	public void windowIconified(WindowEvent e) {    }
	public void windowDeiconified(WindowEvent e) {    }
	public void windowActivated(WindowEvent e) {    }
	public void windowDeactivated(WindowEvent e) {    }

}
