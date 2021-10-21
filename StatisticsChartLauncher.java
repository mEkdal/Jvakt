package Jvakt;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.MaskFormatter;

import Jvakt.ManFiles.StreamGobbler;
import Jvakt.ManFiles.runCMD;

public class StatisticsChartLauncher extends JFrame {

	JTextField textField = new JTextField("", 25);
	JButton button = new JButton("Launch");
	JButton filebutton = new JButton("Statistics file");
	JFileChooser fileChooser;
	static File dir = null;
	static String statsDir;


	static String from = "2021-09-28 00:00:00";
	static String tom  = "2021-09-28 24:00:00";

	static String[] tab = new String [1];

	static String chdat = new String("yyyy-MM-dd HH:mm:ss");
	static SimpleDateFormat dat_form;

	static Date now;
	static Date then;

	static JFormattedTextField fomTextField = null;
	static JFormattedTextField tomTextField = null;

	private  String cmdStats = null;
	private  String cmd; 

	public StatisticsChartLauncher() {
		super("Jvakt.StatisticsChartLauncher 2021-10-14");
		setLayout(new FlowLayout());

		getProps();

//		// customizes appearance: font, foreground, background
//		textField.setFont(new java.awt.Font("Arial", Font.ITALIC | Font.BOLD, 12));
//		textField.setForeground(Color.BLUE);
//		textField.setBackground(Color.YELLOW);
//
//		// customizes text selection
//		textField.setSelectionColor(Color.CYAN);
//		textField.setSelectedTextColor(Color.RED);

		// sets initial selection
//		textField.setSelectionStart(8);
//		textField.setSelectionEnd(12);

		// adds event listener which listens to Enter key event
//		textField.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent event) {
//				JOptionPane.showMessageDialog(StatisticsChartLauncher.this, 
//						"You entered text #1:\n" + textField.getText());
//			}
//		});

		// adds key event listener. Disables the button if input file name is not present
		button.setEnabled(false);
		textField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent event) {
				String content = textField.getText();
				System.out.println(" content.length() : "+content.length());
				if (content.length()>5) {
					button.setEnabled(true);
				} else {
					button.setEnabled(false);
				}
			}			
		});

		// adds action listener for the button
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				cmd= cmdStats+" -statF \""+textField.getText()+"\" -from \""+fomTextField.getText()+"\" -to \""+tomTextField.getText()+"\"";

				if (cmdStats == null) {
					JOptionPane.showMessageDialog(StatisticsChartLauncher.this,"Property \"cmdStats\" in command.properties file not found!");
					System.out.println("cmdStats in command.properties file not found");

				} else {

					runCMD pp = new runCMD(cmd);  // start the renderer program
					if (pp.runCMDfile()) {
						System.out.println(" -successfull cmd: "+cmd);
					}
					else {
						JOptionPane.showMessageDialog(StatisticsChartLauncher.this,"Command Failed!\n"+cmd);
						System.out.println(" -failed cmd: "+cmd);
					}
				}
			}				
		});	

		// adds action listener for the file button
		filebutton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				// Open the save dialog
				System.out.println("* dir  -> " + dir );
				fileChooser = new JFileChooser();
				if (statsDir!=null) {
					dir  = new File(statsDir);
					fileChooser.setCurrentDirectory(dir);
				} else fileChooser.setCurrentDirectory(null);
				fileChooser.showOpenDialog(null);

				//				JOptionPane.showMessageDialog(test09.this, 
				//						"Content of the text field:\n" + j.getSelectedFile().getAbsolutePath());

				textField.setText(fileChooser.getSelectedFile().getAbsolutePath());  
				
				button.setEnabled(true);
			}
		});

		JLabel fileLabel = new JLabel("Input file:");

		// from date field
		JLabel fomLabel = new JLabel("From Date :");
		try {
			fomTextField = new JFormattedTextField(
					new MaskFormatter("####-##-## ##:##:##")); 
			fomTextField.setColumns(11);
			fomTextField.setText(from.toString());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		fomLabel.setLabelFor(fomTextField);

		// to date field
		JLabel tomLabel = new JLabel("To Date   :");
		//		JFormattedTextField tomTextField = null;
		try {
			tomTextField = new JFormattedTextField(
					new MaskFormatter("####-##-## ##:##:##")); 
			tomTextField.setColumns(11);
			tomTextField.setText(tom.toString());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		tomLabel.setLabelFor(tomTextField);

		add(fileLabel);		
		add(textField);
		add(filebutton);
		add(fomLabel);
		add(fomTextField);
		add(tomLabel);
		add(tomTextField);
		add(button);

		setSize(1200, 120);		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);		
		setVisible(true);
	}

	public static void main(String[] args) {

		dat_form = new SimpleDateFormat(chdat);
		now  = new Date();
		then = new Date();
		then.setTime(now.getTime()-86400000);   // subtract 24 hours 
		now.setTime(now.getTime()+86400000);   // add 24 hours 

		tom = dat_form.format(now);
		from= dat_form.format(then);

		System.out.println("Now   :"+now);
		System.out.println("Then  :"+then);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new StatisticsChartLauncher();
			}
		});
	}

	void getProps() {

		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("console.properties");
			prop.load(input);
			// get the property value and print it out
			cmdStats   = prop.getProperty("cmdStats");
			statsDir   = prop.getProperty("statsDir");
			input.close();
		} catch (IOException ex) {
			// ex.printStackTrace();
		}    	
	}



	// Class to Launch the renderer program ( start )
	public class runCMD {

		Date	now;

		String cmd;

		boolean swSettings = false;
		boolean swError = false;
		boolean swDestroy = false;
		boolean swGoon = false;
		int nuWait = 0;
		int exitVal;

		public runCMD(String c) {

			cmd = c;
			now = new Date();

		}

		public boolean runCMDfile() {

			now = new Date();

			Process p;

			// execute the command if there is one.
			// default command handling
			swError = false;
			swDestroy = false;
			System.out.println("* runCMD "+ now + " -> " + cmd );
			try {
				exitVal = 0;
				p = Runtime.getRuntime().exec(cmd);

				// any error message?
				StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");            
				// any output?
				StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");
				// kick them off
				errorGobbler.start();
				outputGobbler.start();
				Long w = new Long(2);
				if (p.waitFor(w,TimeUnit.SECONDS)) { 
					swError = true;
					System.out.println("** exitVal: " + exitVal);
				}
			}
			catch (Exception e) {
				//				swError = true;
				e.printStackTrace();
				System.out.println("** exeption (p)  ");
			}

			if (swError) {
				System.out.println("-Unsuccessfull cmd: "+ cmd);  
				return false;
			}
			else {
				//				if (swList)
				//				System.out.println("-Successfull cmd: "+ cmd);
				return true;
			}

		}
	}	

	class StreamGobbler extends Thread
	{
		InputStream is;
		String type;

		StreamGobbler(InputStream is, String type)
		{
			this.is = is;
			this.type = type;
		}

		public void run()
		{
			try
			{
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line=null;
				while ( (line = br.readLine()) != null)
					System.out.println(type + "> " + line);    
			} catch (IOException ioe)
			{
				ioe.printStackTrace();  
			}
		}
	}

// Launch the renderer program ( stop )

}