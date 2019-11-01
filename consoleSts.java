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

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.Timer;
import javax.swing.border.*;

// Extend av jframe för att få tillgång till swing metoderna i Jframe. 
// Jframe är basen i fönsterhanteringen.  

//implementerar TableModelListener för att använda denna class som lyssnare till Jtables datamodellclass via metoden tableChanged.
//implementerar WindowListener för att använda denna class som lyssnare till Jframe med metoden windowClosing; och där tömma data till filer.
public class consoleSts extends JFrame implements TableModelListener, WindowListener {

	// Skapar diverse variabler
	static final long serialVersionUID = 42L;
	private JPanel topPanel;
	private JPanel usrPanel;
//	private JPanel logPanel;
	private JTable table;
	private JScrollPane scrollPane;
	private JButton bu1;
	private JTextField where;
	private JTableHeader header;
	//	    private JScrollPane scrollPane2;
	private consoleStsDM wD;
	private Boolean swAuto = true;
	//	private Boolean swAuto = false;
	private Boolean swRed = true; 
	private Boolean swDBopen = true; 
	private Boolean swServer = true; 
	private Boolean swDormant = true; 

	//	private  String host = "193.234.149.176";
	private  String jvhost = "127.0.0.1";
	private  String jvport = "1956";
	private  int port = 1956; 

	private  int deselectCount = 0; 
	private  int jvconnectCount = 0; 

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws IOException {
		consoleSts mainFrame = new consoleSts();  // gör objekt av innevarande class 
		mainFrame.pack();                   // kallar på innevarande class metod pack som ärvts via Jframe 
		mainFrame.setVisible(true);  	    // kallar på innevarande class metod setVisible och nu visas fönster för användaren

	}   // main st�r nu och "v�ntar" vid slutet tills de andra objekten avslutas.


	// construktorn som startas i den statiska main metoden.
	// skapar alla inblandade objekt och kopplar ihop dom.
	// kallar också på metoder ärvda från Jframe att sätta vissa värden.
	public consoleSts() throws IOException {

		ImageIcon img = new ImageIcon("console.png");
		setIconImage(img.getImage());

		// get the parameters from the console.properties file
		getProps();
		port = Integer.parseInt(jvport);

		// funktion fr�n Jframe att s�tta rubrik
		setTitle("Jvakt consoleSts 2.17  -  F1 = Help");
		//	        setSize(5000, 5000);

		// get the screen size as a java dimension
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		// get 2/5 of the height, and 2/3 of the width
		int height = screenSize.height * 3 / 5;
		int width = screenSize.width * 9 / 10;

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
		wD = new consoleStsDM();
		// skapar en Jtable och lägger till referensen till wD via Jtables contructor
		// table kommer att visa userDB
		table = new JTable(wD);

		//		JTableHeader header = table.getTableHeader();
		header = table.getTableHeader();
		header.setBackground(Color.LIGHT_GRAY);
		//		header.setBackground(Color.white);

		bu1 = new JButton();
		where = new JTextField(40);
		where.setText("id ilike '%search%'");

		System.out.println("screenHeightWidth :" +screenSize.height+" " +screenSize.width);
		if (screenSize.height > 1200) {
			table.setRowHeight(table.getRowHeight()*2);
			header.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
			bu1.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
			where.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
		}
		else 
			if (screenSize.height > 1080) {
				table.setRowHeight(table.getRowHeight()*1,5);
				header.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
				bu1.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
				where.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
			}


		swServer = true;
		try {
			SendMsg jm = new SendMsg(jvhost, port);  // kollar om JvaktServer �r tillgänglig.
			String oSts = jm.open();
			//			System.out.println("#1 "+oSts);
			if (oSts.startsWith("failed")) 	swServer  = false;
			if (oSts.startsWith("DORMANT")) swDormant = true;
			else 							swDormant = false;
			jm.close();
		} 
		catch (IOException e1) {
			swServer = false;
			System.err.println(e1);
			//			System.err.println(e1.getMessage());
		}
		catch (NullPointerException npe2 )   {
			swServer = false;
//			System.out.println("-- Rpt Failed --" + npe2);
		}
		//		System.out.println("swServer :" + swServer);
		wD.setEditable(!swAuto);
		swDBopen = wD.refreshData(); // kollar om DB �r tillgänglig
		setBu1Color();

		bu1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				swAuto = !swAuto;
				wD.setEditable(!swAuto);
				swDBopen = wD.refreshData();
				setBu1Color();
			}
		});

		// talar om för table att man bara får välja en rad i taget
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// ber table om referensen till LIstSecectionModel objektet, sparar i rowSM
		ListSelectionModel rowSM = table.getSelectionModel();

		//
		// OBS intern class start---
		// Använder rowSM metod för att skapa lyssnare till table för att veta vilken rad som väljs.
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
		// OBS intern class end---
		//


		// s�tter automatsortering i tabellerna    
		//	        table.setAutoCreateRowSorter(true);
		// talar om f�r tabellernas datamodellobjekt (wD o wD2) att detta objekt lyssnar; metoden tableChanged
		table.getModel().addTableModelListener(this);

		// s�tter f�rg p� raderna
		consoleStsCR cr=new consoleStsCR();

		for (int i=0; i <= 19; i++ ) {      
			table.getColumn(table.getColumnName(i)).setCellRenderer(cr);
		}

		// skapar nya JScrollPane och l�gger till tabellerna via construktorn. F�r att kunna scrolla tabellerna.

		scrollPane = new JScrollPane(table);
		//		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		//	        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		//state id prio type status body rptdat chkday chktim errors accerr msg msgdat console condat info plugin agent sms smsdat
		TableColumn column = null;
		column = table.getColumnModel().getColumn(0); // state
		column.setPreferredWidth(40);
		column.setMaxWidth(400);
		column = table.getColumnModel().getColumn(1); // id
		column.setPreferredWidth(445);
		column.setMaxWidth(1555);
		column = table.getColumnModel().getColumn(2); // prio
		column.setPreferredWidth(30);
		column.setMaxWidth(95);
		column = table.getColumnModel().getColumn(3); // type
		column.setPreferredWidth(40);
		column.setMaxWidth(300);
		column = table.getColumnModel().getColumn(4); // status
		column.setPreferredWidth(50);
		column.setMaxWidth(110);
		column = table.getColumnModel().getColumn(5); // body
		column.setPreferredWidth(300);
		column.setMaxWidth(1710);
		column = table.getColumnModel().getColumn(6); // rptdat
		column.setPreferredWidth(220);
		column.setMaxWidth(350);
		column = table.getColumnModel().getColumn(7); // chkday
		column.setPreferredWidth(90);
		column.setMaxWidth(800);
		column = table.getColumnModel().getColumn(8); // chktim
		column.setPreferredWidth(80);
		column.setMaxWidth(250);
		column = table.getColumnModel().getColumn(9); // errors
		column.setPreferredWidth(40);
		column.setMaxWidth(150);
		column = table.getColumnModel().getColumn(10); // accerr
		column.setPreferredWidth(40);
		column.setMaxWidth(150);
		column = table.getColumnModel().getColumn(11); // msg
		column.setPreferredWidth(40);
		column.setMaxWidth(150);
		column = table.getColumnModel().getColumn(12); // msgdat
		column.setPreferredWidth(120);
		column.setMaxWidth(350);
		column = table.getColumnModel().getColumn(13); // console
		column.setPreferredWidth(40);
		column.setMaxWidth(150);
		column = table.getColumnModel().getColumn(14); // condat
		column.setPreferredWidth(120);
		column.setMaxWidth(350);
		column = table.getColumnModel().getColumn(15); // info
		column.setPreferredWidth(120);
		column.setMaxWidth(350);
		column = table.getColumnModel().getColumn(16); // plugin
		column.setPreferredWidth(120);
		column.setMaxWidth(350);
		column = table.getColumnModel().getColumn(17); // agent
		column.setPreferredWidth(120);
		column.setMaxWidth(350);
		column = table.getColumnModel().getColumn(18); // sms
		column.setPreferredWidth(40);
		column.setMaxWidth(150);
		column = table.getColumnModel().getColumn(19); // smsdat
		column.setPreferredWidth(120);
		column.setMaxWidth(350);

		addKeyBindings();

		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		//	        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		// skapar tv� nya JPanel att anv�ndas inuti topPanel, som ocks� �r en JPanel
		usrPanel = new JPanel();
		usrPanel.setLayout(new BorderLayout());
		//	        logPanel = new JPanel();
		//	        logPanel.setLayout(new BorderLayout());
		// talar om f�r de nya JPanels vilka scrollPanes dom ska inneh�lla (scrollPanes inneh�ller tabellerna).
		//	        usrPanel.add(scrollPane, BorderLayout.CENTER);
		//		topPanel.add(scrollPane, BorderLayout.CENTER);
		// talar om f�r topPanel att den ska inneh�lla tv� JPanelobjekt NORTH och CENTER       
		//	        usrPanel.add(bu1, BorderLayout.NORTH);
		//		topPanel.add(bu1, BorderLayout.NORTH);

		usrPanel.add(bu1, BorderLayout.NORTH);
		usrPanel.add(where, BorderLayout.CENTER);
		topPanel.add(usrPanel, BorderLayout.NORTH);
		topPanel.add(scrollPane, BorderLayout.CENTER);

		//	        topPanel.add(usrPanel, BorderLayout.NORTH);
		//	        topPanel.add(logPanel, BorderLayout.CENTER);
		// talar om f�r innevarande object att den lyssnar p� sig sj�lv. (metoderna f�r WindowListener)
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
							SendMsg jm = new SendMsg(jvhost, port);  // kollar om JvaktServer �r tillg�nglig.
							String oSts = jm.open();
							//						System.out.println("#1 "+oSts);
							if (oSts.startsWith("failed")) 	swServer  = false;
							if (oSts.startsWith("DORMANT")) swDormant = true;
							else 							swDormant = false;
							jm.close();
						} 
						catch (IOException e1) {
							swServer = false;
							System.err.println(e1);
							//						System.err.println(e1.getMessage());
						}
						catch (NullPointerException npe2 )   {
							swServer = false;
//							System.out.println("-- Rpt Failed --" + npe2);
						}
						//					System.out.println("swServer 2 : " + swServer);
					}

					if (where.getText().length() > 5) {
						//						System.out.println("swServer :" + swServer);
						wD.setWhere(where.getText());
					}
					else {			
						where.setText("id ilike '%search%'");
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


	// vi implementerade TableModelListener och addade "this" för att denna metod skulle anropas vidä�nding av värde i tabellen
	// detta användas bara för loggning
	public void tableChanged(TableModelEvent e)  {
		int row = e.getFirstRow();
		int column = e.getColumn();
		String ls ;
		TableModel model = (TableModel)e.getSource();
		if (column==2 || column==9 ||column==10  ) {
//			String columnName = model.getColumnName(column);
			int datai = (Integer)model.getValueAt(row, column);
			ls = "Workout tableChanged " + row + " " + column + " " +  datai;		} 
		else {
//			String columnName = model.getColumnName(column);
			String data = (String)model.getValueAt(row, column);
			ls = "Workout tableChanged " + row + " " + column + " " +  data;
		}
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
		else if (swDormant) txt = txt + "  System DORMANT.";
		else txt = txt +  "  System ACTIVE.";

		bu1.setText(txt);
	}

	private void addKeyBindings() {

		table.getActionMap().put("clearSel", clearSel());
		table.getActionMap().put("increaseH", increaseH());
		table.getActionMap().put("decreaseH", decreaseH());
		table.getActionMap().put("showHelp", showHelp());
		table.getActionMap().put("delRow", delRow());
		table.getActionMap().put("cpyRow", cpyRow());

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

		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);  	// delete key in Mac
		table.getInputMap(JComponent.WHEN_FOCUSED).put(keyStroke, "delRow");
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);  		// delete key in Win  Linux
		table.getInputMap(JComponent.WHEN_FOCUSED).put(keyStroke, "delRow");

		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0);  		// copyRow
		table.getInputMap(JComponent.WHEN_FOCUSED).put(keyStroke, "cpyRow");

	}  

	private AbstractAction showHelp()  {
		AbstractAction save = new AbstractAction() {
			static final long serialVersionUID = 43L;

			@Override
			public void actionPerformed(ActionEvent e)  {
				//					                 JOptionPane.showMessageDialog(TestTableKeyBinding.this.table, "Action Triggered.");
//				System.out.println("ShowHelp");
//				JOptionPane pane = new JOptionPane("Jvakt help");
//				pane.showMessageDialog(getContentPane(),
				JOptionPane.showMessageDialog(getContentPane(),
						"F1 : Help \nF3 : Increase font size \nF4 : Decrease font size \nF9 : Copy row \nDEL : Mark for deletion \n\nESC : Unselect"+
								"\n\nThe SEARCH field (a where statement) is active when an ending space is present"+
								"\n\nSome fields are updatable when the 'Auto Update' is off!\n Please heed the rules for the fields!" +
								"\n\nstate  - Must be A (active), I (inactive) or D (dormant)" + 
								"\nprio   - Below 30   may trigger SMS or Mail depending on chkday/chktim " + 
								"\nprio   - 10 or less may trigger SMS or Mail 24/7. " +
								"\ntype   - R = rptdat must be updated at least every 20 minutes or a time out warning is issued. " +
								"\ntype   - S = rptday must be updated 'today' or a time out warning is issued. " +
								"\ntype   - T = no time out warning is issued. " +
								"\ntype   - I = a temporary type used for impromptu messages. " +
								"\ntype   - D = delete mark. the row will be purged by the nightly housekeeping routine. " +
								"\nchkday - Must be *ALL or a mix of MON TUE WED THU FRI SAT SUN"+ 
								"\nchktim - The time of day when the check starts. The format is HH:MM:TT"+ 
								"\naccerr - The number of acceptable errors before a warning is issued." 
								,"Jvakt Help",
								JOptionPane.INFORMATION_MESSAGE);

				//				JOptionPane.showMessageDialog(table,
				//					    "F1 : Help \nF3 : Increase font size \nF4 : Decrease font size \n\nESC : Unselect",
				//					    "Jvakt Help",
				//					    JOptionPane.INFORMATION_MESSAGE);
			}
		};
		return save;
	}

	private AbstractAction clearSel()  {
		AbstractAction save = new AbstractAction() {
			static final long serialVersionUID = 44L;

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
			static final long serialVersionUID = 45L;

			@Override
			public void actionPerformed(ActionEvent e)  {
				//	                 JOptionPane.showMessageDialog(TestTableKeyBinding.this.table, "Action Triggered.");
				//				table.getSelectionModel().clearSelection();  // clear selected rows.
				//				System.out.println("getRowHeight :" + table.getRowHeight());
				if (table.getRowHeight()<100) {
					table.setRowHeight(table.getRowHeight()+1);
					//								System.out.println("getRowHeight :" + table.getRowHeight());
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
			static final long serialVersionUID = 46L;

			@Override
			public void actionPerformed(ActionEvent e)  {
				//	                 JOptionPane.showMessageDialog(TestTableKeyBinding.this.table, "Action Triggered.");
				//				table.getSelectionModel().clearSelection();  // clear selected rows.
				//								System.out.println("getRowHeight :" + table.getRowHeight());
				if (table.getRowHeight()>10) {
					table.setRowHeight(table.getRowHeight()-1);
					header.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
					bu1.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
					where.setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, table.getRowHeight()));
				}
				//								System.out.println("getRowHeight :" + table.getRowHeight());

			}
		};
		return save;
	}
	
	private AbstractAction delRow()  {
		AbstractAction save = new AbstractAction() {
			static final long serialVersionUID = 47L;

			@Override
			public void actionPerformed(ActionEvent e)  {
				table.editingCanceled(null);
				table.editingStopped(null);
				int[] selectedRow = table.getSelectedRows();

				try {
					for (int i = 0; i <  selectedRow.length; i++) {
//						System.out.println("*** Row do delete :" + selectedRow[i]);
						wD.setValueAt("D", selectedRow[i], 3);   // Set the type field to D to mark it as deletable by housekeeping 
					}
				} 
				catch (Exception e2) {
//					System.out.println("-- Exeption delRow 1 --");
					System.err.println(e2);
					System.err.println(e2.getMessage());
				}
				table.getSelectionModel().clearSelection();  // clear selected rows.
			}
		};
		return save;
	}
	

	private AbstractAction cpyRow()  {
		AbstractAction save = new AbstractAction() {
			static final long serialVersionUID = 48L;

			@Override
			public void actionPerformed(ActionEvent e)  {
				table.editingCanceled(null);
				table.editingStopped(null);
				int[] selectedRow = table.getSelectedRows();

				try {
					for (int i = 0; i <  selectedRow.length; i++) {
//						System.out.println("*** Row do copy :" + selectedRow[i]);
						wD.cpyRow(selectedRow[i]); 
					}
				} 
				catch (Exception e2) {
//					System.out.println("-- Exeption cpyRow 1 --");
					System.err.println(e2);
					System.err.println(e2.getMessage());
				}
				table.getSelectionModel().clearSelection();  // clear selected rows.
			}
		};
		return save;
	}
	


	// windows listeners
	// vi implementerade WindowListener och addade "this" f�r att denna metod skulle anropas vid normalt avslut av Jframe 
	// v�rdena i tabellerna skrivt till var sin fil
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
			input.close();
		} catch (IOException ex) {
			// ex.printStackTrace();
		}    	
	}


	// vi implementerade WindowListener men f�ljande metoder av�nds inte 
	public void windowClosed(WindowEvent e) {    }
	public void windowOpened(WindowEvent e) {    }
	public void windowIconified(WindowEvent e) {    }
	public void windowDeiconified(WindowEvent e) {    }
	public void windowActivated(WindowEvent e) {    }
	public void windowDeactivated(WindowEvent e) {    }

}
