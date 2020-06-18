package Jvakt;

import java.io.*;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.*;

public class ManFiles {

	/**
	 * @param args
	 */
	static boolean swList = true, swDelete = false, swHelp = false,swParfile=false,
			swSub = true, swFirst = true, swCopy = false, swRun = false, swRunJvakt = false,
			swMove = false, swSettings = false, swSN = false, swDed = false, swArch = false, 
			swRepl = true, swAppend = false, swUnique = false, swCountC = false;
	static boolean swExists = false, swFlat = false, swNew = false, swCmd = false,
			swNrunq = false, swLogg = false, swNfile = false, swLoop = false, swArgs = true;

	static boolean moved = false;
	static File sdir, tdir, adir, newfile;
	static String origdir, norigdir, norigdirA, suf, pos, pref, hou, min, sec, nfile,
	exfile, expath, inpath, unique, infTxt, parFile, scanstr, cmd1, cmd2;
	static String fdat = "00000000000000";
	static String tdat = "99999999999999";
	static Date now;
	static FileFilter ff;
	static Long lhou, lmin, Lsec;
	static int antal, antcopies, anterrors, antdeleted, antmoved, antarchived, antded, antempty,antalCMD, p;
	static int antalT, antcopiesT, anterrorsT, antdeletedT, antmovedT, antarchivedT, antdedT, antemptyT, antalTCMD;
	static BufferedWriter logg;
	static List<String> listToS;
	static FileOutputStream fis;
	static OutputStreamWriter osw;
	static String element;
	String[] args2 = new String[3];

	static String jvhost   = "localhost";
	static String jvport   = "1956";
	static String jvtype = "T";
	static int port ;
	static String id = null;
	static String desc;
	static InetAddress inet;
	static String agent = null;
	static String config = null;
	static File configF;
	static boolean swJvakt = false;
	static int sleep = 1000;
	static String charset = "UTF8";

	//	static ManFiles x;
	static ManFiles x = new ManFiles();

	public static void main(String[] args) throws IOException,
	FileNotFoundException {
		suf = "*";
		pref = "*";
		pos = "*";
		inpath = "*";
		expath = "*";
		exfile = "*";
		hou = "0";
		min = "0";
		sec = "0";
		scanstr = null;
		charset = "UTF8";
		swNrunq = false;

		now = new Date();
		System.out.println("\n*** Jvakt.ManFiles starting --- " + now);
		
		parseParameters(args);
		swArgs = false;

		//		if (swLogg && sLog) {
		//			logg.write("\n*** Starting " + now);
		//			logg.newLine();
		//		}
		if (swSettings) {
			infTxt = "\n swList=" + swList + "\t swDelete=" + swDelete
					+ "\t swSub=" + swSub + "\t swCopy=" + swCopy
					+ "\t swMove=" + swMove + "\t swArch=" + swArch + "\t swRun=" + swRun
					+ "\t sdir=" + sdir + "\t tdir=" + tdir + "\t adir=" + adir
					+ "\t Suf=" + suf + "\t Prefix=" + pref
					+ "\t Pos=" + pos + "\t Hours=" + hou
					+ "\t Minutes=" + min + "\t Seconds=" + sec 
					+ "\t FromDate=" + fdat + "\t ToDAte=" + tdat + "\t swDed="
					+ swDed + "\t swSN=" + swSN + "\t nfile=" + nfile
					+ "\t exfile=" + exfile + "\t expath=" + expath
					+ "\t inpath=" + inpath + "\t swRepl=" + swRepl
					+ "\t swNrunq=" + swNrunq + "\t swFlat=" + swFlat
					+ "\t swAppend=" + swAppend + "\t swUnique=" + swUnique + "\t swLoop=" + swLoop
					+ "\t swNew=" + swNew + "\t swLogg=" + swLogg + "\t swCountC=" + swCountC +"\t scan=" + scanstr + "\t charset=" + charset+ "\t parfile=" + parFile;
			System.out.println(infTxt);
			if (swLogg) {
				logg.write(infTxt);
				logg.newLine();
			}
		}

		if (sdir == null && !swParfile) {
			System.out.println("\n>>> You must supply a source directory or a parfile <<<");
			swHelp = true;
		} else if (tdir != null && sdir.equals(tdir) && !swUnique ) {
			System.out
			.println("\n>>> You can't use the same directory as both soruce and target unless you use the -unique switch <<<");
			swHelp = true;
		}

		if (swHelp) {
			System.out
			.println("\n*** Jvakt.ManFiles (build 2020-JUN-02) ***"
					+ "\n*** by Michael Ekdal, Sweden. ***");
			System.out
			.println("\nThe parameters and their meaning are:\n"
					+ "\n-parfile \tThe name prefix of the parameter file (default is ManFiles). The suffix must end with .par"
					+ "\n         \tIn the default case, files named ManFiles01.par, ManFiles02.par and so on will be found."
					+ "\n         \tThe files must reside in the current directory."
					+ "\n         \tNOTE: -sdir and -tdir can contain maximum one consecutive space in the string."
					+ "\n         \tNOTE: Spaces can be substituted with a ? if more than one space are needed."
					+ "\n         \tNOTE: No single or dubble quotes are allowed anyware in the file."
					+ "\n-sdir    \tThe name of the source  directory, like \"-sdir c:\\Temp\" "
					+ "\n-tdir    \tThe name of the target  directory, like \"-tdir c:\\Temp2\" "
					+ "\n-adir    \tThe name of the archive directory, like \"-adir c:\\Temp3\" "
					+ "\n-sub     \tThe subdirectories are seached.(default) "
					+ "\n-nosub   \tThe subdirectories are NOT searched. "
					+ "\n-copy    \tCopy the files "
					+ "\n-move    \tMove the files "
					+ "\n-del     \tAll selected files are deleted from the source directory!"
					+ "\n-nodel   \tNo files are deleted. (default)"
					+ "\n-list    \tThe selected files are listed.(default) "
					+ "\n-nolist  \tThe selected files are NOT listed."
					+ "\n-log     \tWrite to specific file. like \"-log c:\\logg.txt\" "
					+ "\n-run     \tLive, no simulation"
					+ "\n-norun   \tSimulation. No copies, moves or deletions are made (default)"
					+ "\n-ded     \tEmpty sub-directories are deleted from the source directory."
					+ "\n-noded   \tNo delete of empty sub-directories. (default)"
					+ "\n-repl    \tReplace target files. (default)"
					+ "\n-norepl  \tDo NOT Replace target file."
					+ "\n-nrunq   \tUsed with -norepl to create a new unique file on tagret."
					+ "\n-append  \tAppend an existing target file"
					+ "\n-nfile   \tName of new file. Used with append to merge a number of related files."
					+ "\n-unique  \tThe moved or copied file is sufixed with the unique string _YYYYMMDDHHMMSS. like (_20100111113539)"
					+ "\n-flat    \tFiles are copied or moved to the -sdir without the original structure"
					+ "\n-noflat  \tFiles are copied or moved to the -sdir with the original structure (default)"
					+ "\n-suf     \tA file suffix to look for. like \"-suf .log \" "
					+ "\n-pref    \tA file prefix to look for. like \"-pref Z \" "
					+ "\n-pos     \tA any string in the file name to look for. like \"-pos per\" "
					+ "\n-inpath  \t(include) string in the path name. like \"-inpath INV\" "
					+ "\n-expath  \t(exclude) string in the path name. like \"-expath INV\" "
					+ "\n-exfile  \t(exclude) string in the file name. like \"-exfile TEMP01\" "
					+ "\n-fdat    \tSelect files from the date it was last changed . like (-fdat 20181101000000)"
					+ "\n-tdat    \tSelect files to   the date it was last changed . like (-tdat 20181205140000)"
					+ "\n-hou     \tHours since file last changed. like \"-hou 48\" (default=0) "
					+ "\n-min     \tMinutes since file last changed. like \"-min 8\" (default=0) "
					+ "\n-sec     \tSeconds since file last changed. like \"-sec 30\" (default=0) "
					+ "\n-nonewh  \tSelect files older than the -hou value (default) "
					+ "\n-newh    \tSelect files newer than the -hou value"
					+ "\n-sn      \tShow short file name when list of files. "
					+ "\n-nosn    \tShow long  file name when list of files. (default)"
					+ "\n-?       \tThis help text are shown."
					+ "\n-help    \tThis help text are shown."
					+ "\n-set     \tShows the program settings."
					+ "\n-scan    \tThe string that is searched for inside the files. It must be a single string with no blanks."
					+ "\n-charset \tUsed with -scan. Default is UTF8. It could be UTF-16, UTF-32, ASCII, ISO8859_1..."
					+ "\n-cmd1    \tThe command part1 to use on files."
					+ "\n         \t i.e. \"print /d:\\\\ptp165\\PBEdicom\\\" (The selected filename is used as a parameter to the command)"
					+ "\n-cmd2    \tThe command part2 to use on files."
					+ "\n-countc  \tShows the number of children in each traversed directory."
					+ "\n-jvakt  \tA switch to enable report to Jvakt. Default is no connection to Jvakt. The file Jvakt.properties must be provided" 
					+ "\n-id     \tUsed as identifier in the Jvakt monitoring system. Mandatory if -jvakt switch is used." 
					+ "\n-config \tThe directory where to find the Jvakt.properties file. like \"-config c:\\Temp\". Optional. Default is the current directory." 
					+ "\n-loop    \tExecutes every second in a never ending loop. -noloop is the default value."
					+ "\n-sleep  \tIn seconds. Used with loop to sleep between checks. The default is one second."
					+ "\n\nComments:"
					+ "\nErrorlevel is set to the number of found files. Max value is 255 though.\n");

			System.exit(12);
		}
		for (;;) {
			now = new Date();	
			if (swParfile) {
				//			System.out.println("--> innan readParFile");
				readParFile();  // reads the parameter files.
				for(Object object : listToS) { 
					//				String element = (String) object;
					element = (String) object;
					System.out.println("\n*** ParRow * "+ new Date()+" * " + element);
					String[] tab = null;
					tab = element.split("\\s+"); // split on one or many white spaces
					parseParameters(tab);

					execOneParSet(); // execute one line set of parameters from the parfile.
				}
			}
			else {
				element = Arrays.toString(args);
				System.out.println("\n*** ParRow * "+ new Date()+" * " + element);
				execOneParSet(); // execute the set of parameters from the command line.   
			}

			System.out.println("\n*** Total  - Files found:" + antalT + "  deleted:" + antdeletedT
					+ "  copied:" + antcopiesT + "  moved:" + antmovedT + "  archived:" + antarchivedT + "  errors:"
					+ anterrorsT + "  empty:" + antemptyT + "  del dir:" + antdedT+ "  cmd:"+antalTCMD);
			//		if (antal>0) sLog=true;   // Hittades filer vill vi ha avslutande logg
			if (swLogg && antalT>0 && !swParfile) {
				//				logg.newLine();
				logg.write("*** Total  - Files found:" + antalT + "  deleted:" + antdeletedT
						+ "  copied:" + antcopiesT + "  moved:" + antmovedT + "  archived:" + antarchivedT
						+ "  errors:" + anterrorsT + "  empty:" + antemptyT
						+ "  del dir:" + antdedT+ "  cmd:"+antalTCMD);
				logg.newLine();
			}

			// sleep for one second and then to it all over again.
			if (!swLoop) break;
			antalT=0;antdeletedT=0;antcopiesT=0;antmovedT=0;antarchivedT=0;anterrorsT=0;antemptyT=0;antdedT=0;antalTCMD=0;
			try {
				//				Thread.currentThread().sleep(1000);
				Thread.sleep(sleep);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (swRunJvakt && swJvakt) {
			desc = "Errors found in ManFiles: " + anterrorsT;
			if (anterrorsT == 0) sendSTS(true);
			else 				sendSTS(false);
		}
		
		now = new Date();
		System.out.println("\n*** Finished " + now);
		if (swLogg && antalT>0 && !swParfile) {
			//			logg.write("*** Finished " + now);
			//			logg.newLine();
			logg.close();
		}
		//		if (antal>0) System.exit(0);
		//		else 		System.exit(4);
		
		
		if (antalT > 255) antalT = 255;
		System.exit(antalT);
	}

	static void execOneParSet() throws IOException,	FileNotFoundException  {
		//		System.out.println("\n---> Execute  Parameter set " + ); 
		antal=0; antcopies=0; anterrors=0; antdeleted=0; antmoved=0; antarchived=0; antded=0; antempty=0; antalCMD=0; p=0;
		swFirst = true;
		String dat = new String("yyyyMMdd");
		String tim = new String("HHmmss");
		SimpleDateFormat dat_form;
		if (swUnique || swNrunq || swArch) {
			dat_form = new SimpleDateFormat(dat);
			unique = dat_form.format(now);
			dat_form = new SimpleDateFormat(tim);
			unique = "_" + unique + dat_form.format(now);
		}

		lhou = new Long(hou);
		lmin = new Long(min);
		Lsec = new Long(sec);
		//		x = new ManFiles();
		//		System.out.println("pref: "+pref+" inpath: "+inpath+" sdir "+ sdir); 
		ff = x.new FileFilter(lhou, lmin, Lsec, suf, pos, pref, expath, inpath,	swNew, exfile,scanstr,charset,fdat,tdat);
		x.new VisitAllFiles(sdir);
		if (swParfile) {

			System.out.println("*** ParRow - Files found:" + antal + "  deleted:" + antdeleted
					+ "  copied:" + antcopies + "  moved:" + antmoved + "  archived:" + antarchived + "  errors:"
					+ anterrors + "  empty:" + antempty + "  del dir:" + antded+ "  cmd:"+antalCMD);
			//		if (antal>0) sLog=true;   // Hittades filer vill vi ha avslutande logg
			if (swLogg && antal>0) {
				logg.write("*** ParRow - Files found:" + antal + "  deleted:" + antdeleted
						+ "  copied:" + antcopies + "  moved:" + antmoved + "  archived:" + antarchived
						+ "  errors:" + anterrors + "  empty:" + antempty
						+ "  del dir:" + antded+ "  cmd:"+antalCMD);
				logg.newLine();
				logg.flush();
			}
		}
	}

	static void parseParameters(String[] args ) throws IOException,	FileNotFoundException {
		//		System.out.println("---> parse " +args[0]);

		String ssdir = null;
		swList = true; swDelete = false; swHelp = false;
		swSub = true; swFirst = true; swCopy = false; swRun = false;
		swMove = false; swArch = false; swSettings = false; swSN = false; swDed = false;
		swRepl = true; swAppend = false; swUnique = false; swCountC = false;
		swExists = false; swFlat = false; swNew = false; swCmd = false;
		swNrunq = false; swLogg = false; swNfile = false;
		moved = false;
		sdir=null; tdir=null; newfile=null;
		origdir=null; norigdir=null; norigdirA=null; nfile=null;
		unique=null; infTxt=null; 
		suf = "*";
		pref = "*";
		pos = "*";
		inpath = "*";
		expath = "*";
		exfile = "*";
		hou = "0";
		min = "0";
		sec = "0";
		scanstr = null;
		charset = "UTF8";
		cmd1 = "";
		cmd2 = "";
		fdat = "00000000000000";
		tdat = "99999999999999";


		for (int i = 0; i < args.length; i++) {
			//						System.out.println("---> parse " + i +"  " +args[i]);
			if (args[i].equalsIgnoreCase("-list"))
				swList = true;
			else if (args[i].equalsIgnoreCase("-nolist"))
				swList = false;
			else if (args[i].equalsIgnoreCase("-copy"))
				swCopy = true;
			else if (args[i].equalsIgnoreCase("-move"))
				swMove = true;
			else if (args[i].equalsIgnoreCase("-del"))
				swDelete = true;
			else if (args[i].equalsIgnoreCase("-nodel"))
				swDelete = false;
			else if (args[i].equalsIgnoreCase("-sub"))
				swSub = true;
			else if (args[i].equalsIgnoreCase("-nosub"))
				swSub = false;
			else if (args[i].equalsIgnoreCase("-run"))
				swRun = true;
			else if (args[i].equalsIgnoreCase("-norun"))
				swRun = false;
			else if (args[i].equalsIgnoreCase("-help"))
				swHelp = true;
			else if (args[i].equalsIgnoreCase("-?"))
				swHelp = true;
			else if (args[i].equalsIgnoreCase("-set"))
				swSettings = true;
			else if (args[i].equalsIgnoreCase("-ded"))
				swDed = true;
			else if (args[i].equalsIgnoreCase("-noded"))
				swDed = false; 
			else if (args[i].equalsIgnoreCase("-loop"))
				swLoop = true;
			else if (args[i].equalsIgnoreCase("-sn"))
				swSN = true;
			else if (args[i].equalsIgnoreCase("-nosn"))
				swSN = false;
			else if (args[i].equalsIgnoreCase("-repl"))
				swRepl = true;
			else if (args[i].equalsIgnoreCase("-norepl"))
				swRepl = false;
			else if (args[i].equalsIgnoreCase("-nrunq"))
				swNrunq = true;
			else if (args[i].equalsIgnoreCase("-sdir")) {
				ssdir = args[++i];
				while( args.length > i+1 ) {
					if ( args[i+1].length() > 2 && args[i+1].startsWith("-")) break;
					ssdir = ssdir+" "+args[++i];
				}
//				Character in need of escapes <([{\^-=$!|]})?*+.> 
				ssdir = ssdir.replaceAll("[\\*\\?\\<\\>\\|]" , " ");
//				System.out.println("ssdir: "+ssdir);
				sdir = new File(ssdir);
				origdir = sdir.toString();
			} else if (args[i].equalsIgnoreCase("-tdir")) {
				ssdir = args[++i];
				while( args.length > i+1 ) {
					if ( args[i+1].length() > 2 && args[i+1].startsWith("-")) break;
					ssdir = ssdir+" "+args[++i];
				}
				ssdir = ssdir.replaceAll("[\\*\\?\\<\\>\\|]" , " ");
				tdir = new File(ssdir);
				norigdir = tdir.toString();
			} else if (args[i].equalsIgnoreCase("-adir")) {
				swArch = true;
				ssdir = args[++i];
				while( args.length > i+1 ) {
					if ( args[i+1].length() > 2 && args[i+1].startsWith("-")) break;
					ssdir = ssdir+" "+args[++i];
				}
				ssdir = ssdir.replaceAll("[\\*\\?\\<\\>\\|]" , " ");
				adir = new File(ssdir);
				norigdirA = adir.toString();
			} else if (args[i].equalsIgnoreCase("-suf"))
				suf = args[++i].trim();
			else if (args[i].equalsIgnoreCase("-pos"))
				pos = args[++i].trim();
			else if (args[i].equalsIgnoreCase("-pref")) 
				pref = args[++i].trim();
			else if (args[i].equalsIgnoreCase("-hou"))
				hou = args[++i];
			else if (args[i].equalsIgnoreCase("-min"))
				min = args[++i];
			else if (args[i].equalsIgnoreCase("-sec"))
				sec = args[++i];
			else if (args[i].equalsIgnoreCase("-newh"))
				swNew = true;
			else if (args[i].equalsIgnoreCase("-nonewh"))
				swNew = false;
			else if (args[i].equalsIgnoreCase("-nfile")) {
				nfile = args[++i];
				swNfile = true;
			} else if (args[i].equalsIgnoreCase("-exfile"))
				exfile = args[++i];
			else if (args[i].equalsIgnoreCase("-expath"))
				expath = args[++i];
			else if (args[i].equalsIgnoreCase("-inpath"))
				inpath = args[++i];
			else if (args[i].equalsIgnoreCase("-flat"))
				swFlat = true;
			else if (args[i].equalsIgnoreCase("-append"))
				swAppend = true;
			else if (args[i].equalsIgnoreCase("-unique"))
				swUnique = true;
			else if (args[i].equalsIgnoreCase("-log")) {
				swLogg = true;
				fis = new FileOutputStream(args[++i], true);
				osw = new OutputStreamWriter(fis, "Cp850");
				logg = new BufferedWriter(osw);
			}
			else if (args[i].equalsIgnoreCase("-countc"))
				swCountC = true;
			else if (args[i].equalsIgnoreCase("-scan"))
				scanstr = args[++i];
			else if (args[i].equalsIgnoreCase("-charset")) charset = args[++i];
			else if (args[i].equalsIgnoreCase("-cmd1")) {
				cmd1 = args[++i];  swCmd = true;
				while( args.length > i+1 ) {
					if ( args[i+1].length() > 2 && args[i+1].startsWith("-")) break;
					cmd1 = cmd1+" "+args[++i];
				}
			}
			else if (args[i].equalsIgnoreCase("-cmd2")) {
				cmd2 = args[++i];  swCmd = true;
				while( args.length > i+1 ) {
					if ( args[i+1].length() > 2 && args[i+1].startsWith("-")) break;
					cmd2 = cmd2+" "+args[++i];
				}
			}
			else if (args[i].equalsIgnoreCase("-fdat"))
				fdat = args[++i];
			else if (args[i].equalsIgnoreCase("-tdat"))
				tdat = args[++i];
			else if (args[i].equalsIgnoreCase("-parfile")) {
				swParfile = true;
				if (args.length == i+1) parFile = "ManFiles";
				else parFile = args[++i];
				if (parFile.startsWith("-")) {
					parFile = "ManFiles";
					i--;
				}
				//				System.out.println("---> parFile: " + parFile); 
			}
			else if (args[i].equalsIgnoreCase("-jvakt")) swJvakt=true;
			else if (args[i].equalsIgnoreCase("-config")) config = args[++i];
			else if (args[i].equalsIgnoreCase("-id"))  id  = args[++i];
			else if (args[i].equalsIgnoreCase("-sleep")) { sleep = Integer.valueOf(args[++i]); sleep = sleep *1000; }

		}

		if (tdir == null) {
			swCopy = false;
			swMove = false;
		} // Use copy if both is present.
		if (swCopy && swMove) {
			swCopy = true;
			swMove = false;
		} // Use move instead of copy and delete.
		if (swCopy && swDelete && !swAppend) {
			swCopy = false;
			swMove = true;
		} // Use move instead of copy and delete.

		if (sdir == null && !swArgs) {
			System.out.println("**** -sdir is missing!! Execution aborted!!!");	
			System.exit(12);
		}
		//		if (sdir == null && !swParfile) {
		//			System.out.println("**** Both -sdir and -parfile is missing!!");	
		//			System.exit(12);
		//		}
		
		if (swJvakt && id != null && swArgs ) {
			swRunJvakt = swRun;
			if (swParfile) swRunJvakt=true;
			if (config == null ) 	configF = new File("Jvakt.properties");
			else 					configF = new File(config,"Jvakt.properties");
			System.out.println("-config file: "+configF);
			getProps();
		}

	}

	static void readParFile() {
		File[] listf;
		DirFilter df;
		String s;
		File dir = new File(".");
		String suf = ".par";
		String pos = parFile;
		int antal = 0;

		listToS = new ArrayList<String>();  // id:mailadress.

		df = new DirFilter(suf, pos);

		listf = dir.listFiles(df);

		System.out.println("\n*** Number of parameter files found: "+ listf.length);
		try {
			BufferedReader in;

			for (int i = 0; i < listf.length; i++) {

				System.out.println("-- Importing parfile: "+listf[i]);
				in = new BufferedReader(new FileReader(listf[i]));

				while ((s = in.readLine()) != null) {
					if (s.length() == 0) continue; 
					if (s.startsWith("#")) continue; 

					listToS.add(s);
					antal++;
					//					System.out.println("-- add: "+s);
				}
				in.close();
			}
		} catch (Exception e) { System.out.println(e);  }
		System.out.println("-- Number of rows imported: "+ antal);

	}

	// Copies src file to dst file.
	// If the dst file does not exist, it is created.
	// if swAppend the file is appended it it exist.
	protected void copy(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out;
		if (swAppend)
			out = new FileOutputStream(dst, true);
		else
			out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}


	// sends status to the Jvakt server
	static protected void sendSTS( boolean STS) throws IOException {

		System.out.println("\n--- " + id + "  --  " + desc);
		System.out.println("--- Connecting to "+jvhost+":"+jvport);
		Message jmsg = new Message();
		SendMsg jm = new SendMsg(jvhost, port);
		//			try {
		System.out.println(jm.open()); 
		//			}
		//			catch (java.net.ConnectException e ) {System.out.println("-- Rpt Failed -" + e);    return;}
		//			catch (NullPointerException npe2 )   {System.out.println("-- Rpt Failed --" + npe2); return;}

		//		if (!swSlut) jmsg.setId(id+"-CheckLogs-"+aFile);
		//		else		 jmsg.setId(id+"-CheckLogs-"+aFile+"-JV");
		// 	    jmsg.setId(id+"-CheckLogs-"+aFile);
		if (STS) jmsg.setRptsts("OK");
		else jmsg.setRptsts("ERR");
		jmsg.setId(id);
		jmsg.setType(jvtype);
		jmsg.setId(id);
		jmsg.setType("T");
		jmsg.setBody(desc);
		jmsg.setAgent(agent);
		//		jm.sendMsg(jmsg);
		if (jm.sendMsg(jmsg)) System.out.println("--- Rpt Delivered --  " + id + "  --  " + desc);
		else           		  System.out.println("--- Rpt Failed ---");
		jm.close();
	}

	static void getProps() {

		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(configF);
			prop.load(input);
			// get the property value and print it out
			jvport   = prop.getProperty("jvport");
			jvhost   = prop.getProperty("jvhost");
			port = Integer.parseInt(jvport);
			System.out.println("getProps jvport: " + jvport + "    jvhost: "+jvhost) ;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		try {
			inet = InetAddress.getLocalHost();
			System.out.println("-- Inet: "+inet);
			agent = inet.toString();
		}
		catch (Exception e) { System.out.println(e);  }

	}




	// ** start internal classes **

	// Process only files under sdir
	protected class VisitAllFiles {

		VisitAllFiles(File sdir) throws IOException {
			//						System.out.println("---> class visitAllFiles " + dir); 
			boolean copyerror;
			boolean moveerror;
			boolean archiveerror;
			String newDir2 = null;
			String newDirA = null;
			if (sdir.isDirectory() && swFirst) {
				swFirst = swSub;
				String[] children = sdir.list(ff);
				if (children == null ) { 
					//					System.out.println("---> class visitAllFiles " + dir);
					return;
				} 
				if (swCountC) System.out.println("# Childrens; "+children.length+" ; "+sdir);
				for (int i = 0; i < children.length; i++) {
					new VisitAllFiles(new File(sdir, children[i]));
				}
				if (children.length == 0) {
					if (swDed) {
						antempty++; antemptyT++;
						if (swList) {
							System.out.println("Empty directory deleted: " + sdir);
							if (swLogg) {
								logg.write("Empty directory deleted: " + sdir);
								logg.newLine();
							}
						}
						if (swRun) {
							if (sdir.delete()) {
								antded++; antdedT++;
							}
							else {
								anterrors++; anterrorsT++;
							}
						}
					}
				}
			} else {
				if (sdir.isFile()) {
					if (swLogg && antal == 0 && element != null) {
						logg.newLine();
						//						logg.write("*** Starting " + new Date());
						//						logg.newLine();
						//						if (element != null) {
						logg.write("*** ParRow * "+ new Date()+" * "+element);
						logg.newLine();
						//						}
					}
					antal++; antalT++;
					copyerror = false;
					moveerror = false;
					archiveerror = false;
					swExists = false;
					moved = false;

					if (swCmd) {
						args2[0] = cmd1;
						args2[1] = sdir.getAbsolutePath();
						args2[2] = cmd2;

						if (swList) System.out.println("-cmd: "+args2[0]+" \""+args2[1]+"\" "+args2[2]);

						if (swRun) { 
							runCMD pp = new runCMD(args2);
							if (pp.runCMDfile()) {
								antalCMD++; antalTCMD++;
								if (swList) {
									System.out.println(" -successfull cmd: "+args2[0]+" \""+args2[1]+"\" "+args2[2]);
									if (swLogg) {
										logg.write(" -successfull cmd: "+args2[0]+" \""+args2[1]+"\" "+args2[2]);
										logg.newLine();
									}
								}
							}
							else {
								swRun = false;   // do not proceed if cmd failed
								anterrors++;
								if (swList) {
									System.out.println(" -failed cmd: "+args2[0]+" \""+args2[1]+"\" "+args2[2]);
									if (swLogg) {
										logg.write(" -failed cmd: "+args2[0]+" \""+args2[1]+"\" "+args2[2]);
										logg.newLine();
									}
								}
							}
						}
					}

					if (swList) {
						if (swSN) {
							if (swMove || swCopy || swDelete) System.out.print("-File: "+sdir.getName());
							else System.out.println("-File: "+sdir.getName());
							if (swLogg) {
								if (swMove || swCopy || swDelete) 
									logg.write("-File: "+sdir.getName());
								else {
									logg.write("-File: "+sdir.getName());
									logg.newLine();
								}
							}
						} else {
							if (swMove || swCopy || swDelete) System.out.print("-File: "+sdir);
							else System.out.println("-File: "+sdir);
							if (swLogg) {
								if (swMove || swCopy || swDelete) 
									logg.write("-File: "+sdir.getAbsolutePath());
								else {
									//									System.out.println("-FileXX: "+sdir.getAbsolutePath());
									logg.write("-File: "+sdir.getAbsolutePath());
									logg.newLine();
								}
							}
						}
					}

					if (swMove || swCopy) {
						if (!swFlat) {
							newDir2 = norigdir+sdir.getPath().substring(origdir.length(),(sdir.getPath().length()-sdir.getName().length()-1));
							tdir = new File(newDir2);
						} else {
							newDir2 = norigdir;
							tdir = new File(newDir2);
						}
						if (!tdir.exists() && swRun)
							tdir.mkdirs();
					}

					if (swArch) {

						if (!swFlat) {
							newDirA = norigdirA+sdir.getPath().substring(origdir.length(),(sdir.getPath().length()-sdir.getName().length()-1));
							adir = new File(newDirA);
						} else {
							newDirA = norigdirA;
							adir = new File(newDirA);
						}

						//						newDirA = norigdirA+sdir.getPath().substring(origdir.length(),(sdir.getPath().length()-sdir.getName().length()-1));
						//						adir = new File(newDirA);
						newfile = new File(newDirA, sdir.getName());

						if (!adir.exists() && swRun)
							adir.mkdirs();

						if (newfile.exists()) {
							p = sdir.getName().lastIndexOf(".");
							if (p >= 0)
								newfile = new File(newDirA, sdir.getName().substring(0, p)+ unique+ sdir.getName().substring(p));
							else
								newfile = new File(newDirA, sdir.getName()+ unique);
						}

						if (swList) {
							//							System.out.print(" -archived> " + newfile);
							if (!swMove && !swCopy) System.out.println(" -archived> " + newfile);
							else System.out.print(" -archived> " + newfile);
							if (swLogg) {
								logg.write(" -archived> " + newfile);
								if (!swMove && !swCopy) logg.newLine();
							}
						}
						try {
							if (swRun ) {
								copy(sdir, newfile);
								antarchived++; antarchivedT++;
							}
						} catch (IOException e) {
							swRun = false;   // do not proceed if arcive failed
							archiveerror = true;
							anterrors++;
							System.out.println(e);
							if (swList)
								System.out.println("   *** archive error --> "	+ newfile);
						}									
					}

					if (swMove) {
						// newfile = new File(newDir2,dir.getName());
						if (swUnique) {
							p = sdir.getName().lastIndexOf(".");
							if (p >= 0) newfile = new File(newDir2, sdir.getName().substring(0, p)+ unique+ sdir.getName().substring(p));
							else newfile = new File(newDir2, sdir.getName()+ unique);
						} else {
							if (swNfile) newfile = new File(newDir2, nfile);
							else 	newfile = new File(newDir2, sdir.getName());
						}

						if (!swRepl) { // If no-replace, do a check if target
							// exists.
							if (newfile.exists())
								swExists = true;
							if (swExists && swNrunq) {
								swExists = false;
								p = sdir.getName().lastIndexOf(".");
								if (p >= 0)
									newfile = new File(newDir2, sdir.getName().substring(0, p)+ unique+ sdir.getName().substring(p));
								else
									newfile = new File(newDir2, sdir.getName()+ unique);
							}
						}
						if (swList && (swRepl || !swExists)) {
							System.out.println(" -moved> " + newfile);
							if (swLogg) {
								logg.write(" -moved> " + newfile);
								logg.newLine();
							}
						}
						if (swRun && (swRepl || !swExists)) {
							if (sdir.renameTo(newfile)) {
								antmoved++; antmovedT++;
								moved = true;
							} else { // delete old target file and try rename a
								// second time
								newfile.delete();
								if (sdir.renameTo(newfile)) {
									antmoved++; antmovedT++;
									moved = true;
								} else {
									if (swList) {
										System.out.println("   *** move error --> "+ newfile);
										if (swLogg) {
											logg.write("   *** move error --> "	+ newfile);
											logg.newLine();
										}
									}
									moveerror = true;
									anterrors++;
									anterrorsT++;
								}
							}
						}
					}

					if (swCopy) {
						// newfile = new File(newDir2,dir.getName());
						if (swUnique) {
							p = sdir.getName().lastIndexOf(".");
							if (p >= 0)
								newfile = new File(newDir2, sdir.getName().substring(0, p)+ unique+ sdir.getName().substring(p));
							else
								newfile = new File(newDir2, sdir.getName()+ unique);
						}
						// else newfile = new File(newDir2,dir.getName());
						else {
							if (swNfile)
								newfile = new File(newDir2, nfile);
							else
								newfile = new File(newDir2, sdir.getName());
						}

						if (!swRepl) { // If no-replace, do a check if target
							// exists.
							if (newfile.exists())
								swExists = true;
							if (swExists && swNrunq) {
								swExists = false;
								p = sdir.getName().lastIndexOf(".");
								if (p >= 0)
									newfile = new File(newDir2, sdir.getName().substring(0, p)+ unique+ sdir.getName().substring(p));
								else
									newfile = new File(newDir2, sdir.getName()+ unique);
							}
						}
						if (swList) {
							System.out.println(" -copied> " + newfile);
							if (swLogg) {
								logg.write(" -copied> " + newfile);
								logg.newLine();
							}
						}
						try {
							//							System.out.println(" to copy: "+swRun+swRepl+swExists + newfile);
							if (swRun && (swRepl || !swExists)) {
								copy(sdir, newfile);
								antcopies++; antcopiesT++;
							}
						} catch (IOException e) {
							copyerror = true;
							anterrors++;
							anterrorsT++;
							System.out.println(e);
							if (swList)
								System.out.println("   *** copy error --> "	+ newfile);
						}
					}
					if (swDelete && !copyerror && !moveerror && !archiveerror && !moved) {
						if (swList) {
							System.out.println(" -deleted: " + sdir);
							if (swLogg) {
								logg.write(" -deleted: " + sdir);
								logg.newLine();
							}
						}
						if (swRun) {
							if (sdir.delete()) {
								antdeleted++; antdeletedT++;
							}
							else {
								anterrors++; 
							}
						}
					}
				}
			}
		}
	}

	// Class used to select files
	// ff = x.new FileFilter(lhou,lmin,suf,pos,pref,expath,inpath,swNew,exfile);

	protected class FileFilter implements FilenameFilter {
		String afn;
		String infix;
		String inpref;
		String exfile;
		String expath;
		String inpath;
		String scanstr;
		String charset;
		String tdat;
		String fdat;
		long inhours;
		long inmin;
		long insec;
		boolean okay;
		boolean swNew;
		File fi;

		FileFilter(Long inhours, Long inmin, Long insec, String afn,
				String infix, String inpref, String expath, String inpath,
				boolean swNew, String exfile, String scanstr, String charset, String fdat,String tdat) {
			this.afn = afn.toLowerCase();
			this.infix = infix.toLowerCase();
			this.inpref = inpref.toLowerCase();
			this.inhours = inhours.longValue();
			this.inmin = inmin.longValue();
			this.insec = insec.longValue();
			this.expath = expath.toLowerCase();
			this.inpath = inpath.toLowerCase();
			this.swNew = swNew;
			this.exfile = exfile.toLowerCase();
			if (scanstr==null) 	this.scanstr=null;
			else 				this.scanstr = scanstr.toLowerCase();
			this.fdat = fdat;
			this.tdat = tdat;
			this.charset = charset;

		}

		public boolean accept(File dir, String name) {
			// System.out.println("DifFilter name: " + name);
			// System.out.println("DifFilter dir: " + dir);
			okay = true;
			//
			String chdat = new String("yyyyMMddHHmmss");
			String changed;
			SimpleDateFormat dat_form;			
			//			
			fi = new File(dir + "\\" + name);
			String f = fi.getName();
			long lm = fi.lastModified();
			//
			dat_form = new SimpleDateFormat(chdat);
			changed = dat_form.format(lm);
			//			System.out.println(">>> "+fi+"   Changed : " +" "+ changed + "  Fdat:" +fdat + "  Tdat:" +tdat); 
			//
			if (changed.compareTo(fdat) <= 0 ) okay = false;
			if (changed.compareTo(tdat) >= 0 ) okay = false;

			if (fi.isDirectory())
				return true;
			// System.out.println("inhou, inmin, insec: " +" "+ inhours +" "+
			// inmin +" "+ insec);

			long min = inmin + (inhours * 60);
			//			 System.out.println("min (minutes): " + min);
			long sec = insec + (min * 60);
			//			 System.out.println("sec (seconds): " + sec);

			f = f.toLowerCase();
			afn = afn.toLowerCase();
			long nu = now.getTime();
			nu -= lm; // Get the difference in milliseconds between now and the
			// date the file was last modified.

			// nu = nu / 3600000; // number of hours since the file was last
			// changed.
			// nu = nu / 60000; // number of minutes since the file was last
			// changed.
			nu = nu / 1000; // number of seconds since the file was last
			// changed.

			// System.out.println("nu (seconds): " + nu);

			// System.out.println("Lm: " + lm);


			//			 System.out.println("nu " + nu + "   inhours " + inhours);
			if (swNew) { // if files newer or equal than hours is wished for
				// if ( nu >= inhours ) okay = false ;
				// if ( nu >= min ) okay = false ;
				if (nu >= sec)
					okay = false;
			} else { // if files older than hours is wished for
				// if ( nu < inhours ) okay = false ;
				// if ( nu < min ) okay = false ;
				if (nu < sec)
					okay = false;
			}

			if (!infix.equals("*")) {
				if (f.indexOf(infix) < 0)
					okay = false;
			}
			if (!inpref.equals("*")) {
				if (!f.startsWith(inpref))
					okay = false;
			}
			if (!afn.equals("*")) {
				if (!f.endsWith(afn))
					okay = false;
			}
			if (okay && !exfile.equals("*")) {
				if (f.indexOf(exfile) >= 0)
					okay = false;
			}
			if (okay && !inpath.equals("*")) {
				if (dir.toString().toLowerCase().indexOf(inpath) < 0)
					okay = false;
			}
			if (okay && !expath.equals("*")) {
				if (dir.toString().toLowerCase().indexOf(expath) >= 0)
					okay = false;
			}
			if (okay && scanstr!=null) {
				try {okay = scanF(fi,scanstr,charset); }
				catch (IOException e) {	
					e.printStackTrace(); 
					okay = false; }

			}

			return okay;
		}

		// searches for a string inside the file 
		protected boolean scanF(File src, String scanstr, String charset) throws IOException {
			boolean found = false;
			String s;
			FileInputStream fis = new FileInputStream(src);
			InputStreamReader isr = new InputStreamReader(fis, charset);
			BufferedReader in = new BufferedReader(isr);
			while ((s = in.readLine()) != null && !found) {
				if (s.toLowerCase().indexOf(scanstr) >= 0) found = true;
			} 
			in.close();
			return found;
		}

	}

	//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
	public class runCMD {

		Date	now;

		String cmd;

		String c1 = null; // command part1
		String c2 = null; // filename
		String c3 = null; // command part2

		boolean swSettings = false;
		boolean swError = false;
		boolean swDestroy = false;
		boolean swGoon = false;
		int nuWait = 0;
		int exitVal;

		public runCMD(String[] args) {

			now = new Date();

			// reads command line arguments
			c1 = args[0];
			c2 = args[1];
			c3 = args[2];
		}

		public boolean runCMDfile() {

			now = new Date();

			Process p;

			// execute the command if there is one.
			// default command handling
			swError = false;
			swDestroy = false;
			cmd =  c1 + " \"" + c2 + "\" " + c3 ;
			//			System.out.println("* runCMD "+ now + " -> " + cmd );
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
				// p.waitFor();

				nuWait = 0;
				// waits a number of seconds for command to end.
				swGoon = true;
				while (nuWait < 360 && swGoon && !swError) {
					swGoon = false;
					try { exitVal = p.exitValue(); } catch (Exception e) {swGoon = true;} ;
					if (swGoon) {
						Thread.currentThread();
						Thread.sleep(1000);
						exitVal = 0;
						nuWait++;
					}
					else {
						//						System.out.println("-  Got exitval: " + exitVal);
					}
				}
				//				System.out.println("-  Looped -- " + nuWait);
				if (nuWait >= 120) {
					System.out.println("** Timeout --: " + nuWait);
					swError = true;
					swDestroy = true;
				}

				if (swDestroy) {
					p.destroy(); System.out.println("**Destroy**:");
				}

				if (exitVal != 0) { 
					swError = true;
					System.out.println("** exitVal: " + exitVal);
				}
			}
			catch (Exception e) {
				swError = true;
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
					if (swList)
						System.out.println(type + "> " + line);    
			} catch (IOException ioe)
			{
				ioe.printStackTrace();  
			}
		}
	}


	//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
}
