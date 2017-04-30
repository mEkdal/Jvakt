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
	import java.util.*;
	import javax.swing.ListSelectionModel;
	import javax.swing.event.ListSelectionEvent;
	import javax.swing.event.ListSelectionListener;

	
	// Extend av jframe för att få tillgång till swing metoderna i Jframe. 
	// Jframe är basen i fönsterhanteringen.
	//implementerar TableModelListener för att använda denna class som lyssnare till Jtables datamodellclass via metoden tableChanged.
	//implementerar WindowListener för att använda denna class som lyssnare till Jframe med metoden windowClosing; och där tömma data till filer.
	public class console extends JFrame implements TableModelListener, WindowListener {

	// Skapar diverse variabler
		private JPanel topPanel;
	    private JPanel usrPanel;
	    private JPanel logPanel;
	    private JTable table;
	    private JScrollPane scrollPane;
	    private JScrollPane scrollPane2;
	    private consoleDM wD;

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
	    	
	    	// funktion från Jframe att sätta rubrik
	        setTitle("Jvakt consolev 2.0 alpha");
	        //setSize(600, 200);
	    	// funktion från Jframe att sätta färg
	        setBackground(Color.gray);
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

	        // talar om för table att man bara får välja en rad i taget
	        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	        
	        // ber table om referensen till LIstSecectionModel objektet, sparar i rowSM
	        ListSelectionModel rowSM = table.getSelectionModel();
	        
	        //
	        // OBS intern class start---
	        // Använder rowSM metod för att skapa lyssnare till table för att veta vilken rad som väljs.
	            rowSM.addListSelectionListener(new ListSelectionListener()  {
	             // interna classens metod som tar fram vilken rad som valts
	            	public void valueChanged(ListSelectionEvent e)  {
	                // Ignore extra messages.
	                if (e.getValueIsAdjusting())
	                  return;

	                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
	                if (lsm.isSelectionEmpty()) {
	                  System.out.println("No rows are selected.");
	                } else {
	                  int selectedRow = lsm.getMinSelectionIndex();
	                  System.out.println("Row " + selectedRow + " is now selected.");
	                }
	              }
	            	
	            }    );
	        // OBS intern class end---
	        //
	            
	            
	        // sätter automatsortering i tabellerna    
	        table.setAutoCreateRowSorter(true);
	        // talar om för tabellernas datamodellobjekt (wD o wD2) att detta objekt lyssnar; metoden tableChanged
	        table.getModel().addTableModelListener(this);
	        // skapar nya JScrollPane och lägger till tabellerna via construktorn. För att kunna scrolla tabellerna.
	        scrollPane = new JScrollPane(table);

	        // skapar två nya JPanel att användas inuti topPanel, som också är en JPanel
	        usrPanel = new JPanel();
	        usrPanel.setLayout(new BorderLayout());
	        logPanel = new JPanel();
	        logPanel.setLayout(new BorderLayout());
	        // talar om för de nya JPanels vilka scrollPanes dom ska innehålla (scrollPanes innehåller tabellerna).
	        usrPanel.add(scrollPane, BorderLayout.CENTER);
	        logPanel.add(scrollPane2, BorderLayout.CENTER);
	        // talar om för topPanel att den ska innehålla tcå JPanelobjekt NORTH och CENTER       
	        topPanel.add(usrPanel, BorderLayout.NORTH);
	        topPanel.add(logPanel, BorderLayout.CENTER);
	        // talar om för innevarande object att den lyssnar på sig själv. (metoderna för WindowListener)
	        addWindowListener(this);

	    } // slut construktor
	    
	     
	    // vi implementerade TableModelListener och addade "this" för att denna metod skulle anropas vid änding av värde i tabellen
	    // detta användas bara för loggning
	     public void tableChanged(TableModelEvent e)  {
	        int row = e.getFirstRow();
	        int column = e.getColumn();
	        String ls ;
	        TableModel model = (TableModel)e.getSource();
	        String columnName = model.getColumnName(column);
	        String data = (String)model.getValueAt(row, column);
	        ls = "Workout tableChanged " + row + " " + column + " " +  data;
	        System.out.println(ls);
	    }
	 
	    
	   // windows listeners
	   // vi implementerade WindowListener och addade "this" för att denna metod skulle anropas vid normalt avslut av Jframe 
	   // värdena i tabellerna skrivt till var sin fil
	     public void windowClosing(WindowEvent e) {
	    	//skriv userDB
	    	wD.closeDB();
	    // ...och här är det slut i rutan..!!!... 
	    }
	     
	   // vi implementerade WindowListener men följande metoder avänds inte 
	    public void windowClosed(WindowEvent e) {    }
	    public void windowOpened(WindowEvent e) {    }
	    public void windowIconified(WindowEvent e) {    }
	    public void windowDeiconified(WindowEvent e) {    }
	    public void windowActivated(WindowEvent e) {    }
	    public void windowDeactivated(WindowEvent e) {    }

}
