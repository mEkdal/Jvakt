package Jvakt;

import java.awt.*;
import java.io.BufferedReader;
//import java.io.BufferedWriter;
import java.io.File;
//import java.io.FileOutputStream;
import java.io.FileReader;
//import java.io.IOException;
//import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.JPanel;
import org.jfree.chart.*;
//import org.jfree.chart.axis.CategoryAxis;
//import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
//import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import org.jfree.chart.ui.*;
//import org.jfree.chart.renderer.*;

public class StatisticsChart extends ApplicationFrame
{

	static String statF;
	static File statIn;

	static Vector<String> map = new Vector<String>(100,100);
	
	static String from = "2021-09-28 00:00:00";
	static String tom  = "2021-09-28 24:00:00";

	static String[] tab = new String [1];

	static String chdat = new String("yyyy-MM-dd HH:mm:ss");
	static SimpleDateFormat dat_form;
	
	static Date now;
	static Date then;
	
	public static void main(String args[])
	{
		
		dat_form = new SimpleDateFormat(chdat);
		now  = new Date();
		then = new Date();
		then.setTime(now.getTime()-86400000);

		tom = dat_form.format(now);
		from= dat_form.format(then);
		
//		System.out.println("Now   :"+now);
//		System.out.println("Then  :"+then);
		
		if (args.length < 1) {
			System.out.println("\n\nStatisticsChart 2021-10-08. The parameters and their meaning are:\n"+
					"\n-statf  \tThe input file to analyze. Like: \"-dir c:\\Temp\\monHttp-test.csv\" "+
					"\n-from   \tDate and time in format yyyy-MM-dd HH:mm:ss. The default is 24 hours back in time."+
					"\n-to     \tDate and time in format yyyy-MM-dd HH:mm:ss. The default is the actual time."
					);

			System.exit(4);
		}
	
		// reads command line arguments
		for ( int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-from")) from = args[++i];
			if (args[i].equalsIgnoreCase("-to"))   tom = args[++i];
			if (args[i].equalsIgnoreCase("-statf")) statF = args[++i];
		}

		System.out.println("statF: " + statF);
		statIn = new File(statF);
		
//		StatisticsChart PCategory = new StatisticsChart(statIn.getName());
		StatisticsChart PCategory = new StatisticsChart("Jvakt.StatisticsChart renderer 2021-10-06");
		PCategory.pack();
		PCategory.setVisible(true);

//		System.out.println("Slut i rutan!");
	}

	private static final long serialVersionUID = 1L;
	public StatisticsChart(String s)
	{
		super(s);
		JPanel jpanel = createDemoPanel();
		jpanel.setPreferredSize(new Dimension(1700, 800));
		setContentPane(jpanel);
	}

	public static JPanel createDemoPanel()
	{
		JFreeChart jfreechart = createChart(createDataset());
		ChartPanel chartpanel = new ChartPanel(jfreechart);
		chartpanel.setMouseWheelEnabled(true);

		return chartpanel;
	}

	private static JFreeChart createChart(CategoryDataset categorydataset)
	{
		JFreeChart jfreechart = ChartFactory.createLineChart("Response times of "+statIn.getName(), "time", "ms", categorydataset, PlotOrientation.VERTICAL, true, true, false);
		jfreechart.addSubtitle(new TextTitle("From:  "+from+"      to:  "+tom));
		CategoryPlot categoryplot = (CategoryPlot)jfreechart.getPlot();
		// Set font for category labels.
		categoryplot.getDomainAxis().setTickLabelsVisible(false);
		categoryplot.getDomainAxis().setTickMarksVisible(false);
		categoryplot.getDomainAxis().setLowerMargin(0);
		categoryplot.getDomainAxis().setUpperMargin(0);
		ChartUtils.applyCurrentTheme(jfreechart);

		return jfreechart;
	}

	private static CategoryDataset createDataset() 
	{

		String s;    	
		double rowCount = 0;
		double average = 0;

		DefaultCategoryDataset defaultcategorydataset = new DefaultCategoryDataset();

		try {
			BufferedReader in = new BufferedReader(new FileReader(statIn));

			while ((s = in.readLine()) != null) {
				boolean swComment=false;
				//			System.out.println(s);

				if (s.length() == 0) swComment=true; 
				if (s.startsWith("#")) swComment=true; 

				tab = s.split(";" , 2);

				if (!swComment && from.compareTo(tab[0])<=0 && tom.compareTo(tab[0])>=0) {
					map.add(s); 
					average += Integer.parseInt(tab[1]);
					rowCount++;
				}
			}
			in.close();
		} catch (Exception ex) {
			System.out.println("-- Exeption when open the statistical file "+statIn);
			ex.printStackTrace();
		}

		System.out.println("rowCount : " + rowCount);
		System.out.println("mapSize : "+map.size());
		rowCount=0;
		for (int i = 0; i < map.size(); i++) {
			boolean swComment=false;
			//			System.out.println(s);
			s = map.get(i);

			if (!swComment) {
				tab = s.split(";" , 2);
				if (rowCount<=14400) {
					defaultcategorydataset.addValue(Integer.parseInt(tab[1]),"Response time", tab[0].trim());
					if (Integer.parseInt(tab[1])>0) rowCount++;
				}
			}
			if (rowCount > 14400) break;
		}

		System.out.println("rowCount : " + rowCount);
		System.out.println("average : " + (average/rowCount));
		
		map.clear();
		
		return defaultcategorydataset;
	}


}