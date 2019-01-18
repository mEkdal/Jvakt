package Jvakt;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ManFiles {

	/**
	 * @param args
	 */
	static boolean swList = true, swDelete = false, swHelp = false,swParfile=false,
			swSub = true, swFirst = true, swCopy = false, swRun = false,
			swMove = false, swSettings = false, swSN = false, swDed = false,
			swRepl = true, swAppend = false, swUnique = false, swCountC = false;
	static boolean swExists = false, swFlat = false, swNew = false,
			swNrunq = false, swLogg = false, swNfile = false, swLoop = false, swArgs = true;

	static boolean moved = false;
	static File sdir, tdir, newfile;
	static String origdir, norigdir, suf, pos, pref, hou, min, sec, nfile,
	exfile, expath, inpath, unique, infTxt, parFile, scanstr;
	static String fdat = "00000000000000";
	static String tdat = "99999999999999";
	static Date now;
	static FileFilter ff;
	static Long lhou, lmin, Lsec;
	static int antal, antcopies, anterrors, antdeleted, antmoved, antded, antempty, p;
	static int antalT, antcopiesT, anterrorsT, antdeletedT, antmovedT, antdedT, antemptyT;
	static BufferedWriter logg;
	static List<String> listToS;
	static FileOutputStream fis;
	static OutputStreamWriter osw;
	static String element;
	//	static ManFiles x;
	static ManFiles x = new ManFiles();

	public static void main(String[] args) throws IOException,
	FileNotFoundException {
		//		ManFiles x = new ManFiles();
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
		swNrunq = false;

		parseParameters(args);
		swArgs = false;

		now = new Date();
		System.out.println("\n*** Jvakt.ManFiles starting --- " + now);
		//		if (swLogg && sLog) {
		//			logg.write("\n*** Starting " + now);
		//			logg.newLine();
		//		}
		if (swSettings) {
			infTxt = "\n swList=" + swList + "\t swDelete=" + swDelete
					+ "\t swSub=" + swSub + "\t swCopy=" + swCopy
					+ "\t swMove=" + swMove + "\t swRun=" + swRun
					+ "\t sdir=" + sdir + "\t tdir=" + tdir
					+ "\t Suf=" + suf + "\t Prefix=" + pref
					+ "\t Pos=" + pos + "\t Hours=" + hou
					+ "\t Minutes=" + min + "\t Seconds=" + sec + "\t swDed="
					+ "\t FromDate=" + fdat + "\t ToDAte=" + tdat
					+ swDed + "\t swSN=" + swSN + "\t nfile=" + nfile
					+ "\t exfile=" + exfile + "\t expath=" + expath
					+ "\t inpath=" + inpath + "\t swRepl=" + swRepl
					+ "\t swNrunq=" + swNrunq + "\t swFlat=" + swFlat
					+ "\t swAppend=" + swAppend + "\t swUnique=" + swUnique + "\t swLoop=" + swLoop
					+ "\t swNew=" + swNew + "\t swLogg=" + swLogg + "\t swCountC=" + swCountC +"\t scan=" + scanstr + "\t parfile=" + parFile;
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
			.println("\n*** Jvakt.ManFiles V2.3 2018-DEC-05 ***"
					+ "\n*** by Michael Ekdal, Sweden. ***");
			System.out
			.println("\nThe parameters and their meaning are:\n"
					+ "\n-parfile \tThe name prefix of the parameter file (default is ManFiles). The suffix must end with .par"
					+ "\n         \tIn the default case, files named ManFiles01.par, ManFiles02.par and so on will be found."
					+ "\n         \tThe files must reside in the current directory."
					+ "\n         \tNOTE: -sdir and -tdir can contain maximum one blank in the string."
					+ "\n         \tNOTE: No single or dubble quotes are allowed anyware in the file."
					+ "\n-sdir    \tThe name of the source directory, like \"-sdir c:\\Temp\" "
					+ "\n-tdir    \tThe name of the target directory, like \"-tdir c:\\Temp2\" "
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
					+ "\n-countc  \tShows the number of children in each traversed directory." 
					+ "\n-loop    \tExecutes every second in a never ending loop. No loop is the default value." 
					+ "\n\nComments:"
					+ "\nErrorlevel is set to the number of found files. Max value is 255 though.\n");

			System.exit(12);
		}
		for (;;) {
			if (swParfile) {
				//			System.out.println("--> innan readParFile");
				readParFile();  // reads the parameter files.
				for(Object object : listToS) { 
					//				String element = (String) object;
					element = (String) object;
					System.out.println("\n*** Executing ParRow: " + element);
					String[] tab = null;
					tab = element.split("\\s+"); // split on one or many white spaces
					parseParameters(tab);

					execOneParSet(); // execute one line set of parameters from the parfile.
				}
			}
			else execOneParSet(); // execute the set of parameters from the command line.   

			System.out.println("\nTotal  - Files found:" + antalT + "  deleted:" + antdeletedT
					+ "  copied:" + antcopiesT + "  moved:" + antmovedT + "  errors:"
					+ anterrorsT + "  empty:" + antemptyT + "  del dir:" + antdedT);
			//		if (antal>0) sLog=true;   // Hittades filer vill vi ha avslutande logg
			if (swLogg && antalT>0) {
				logg.newLine();
				logg.write("Total  - Files found:" + antalT + "  deleted:" + antdeletedT
						+ "  copied:" + antcopiesT + "  moved:" + antmovedT
						+ "  errors:" + anterrorsT + "  empty:" + antemptyT
						+ "  del dir:" + antdedT);
				logg.newLine();
			}

			// sleep for one second and then to it all over again.
			if (!swLoop) break;
			antalT=0;antdeletedT=0;antcopiesT=0;antmovedT=0;anterrorsT=0;antemptyT=0;antdedT=0;
			try {
				Thread.currentThread().sleep(1000);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}


		now = new Date();
		System.out.println("\n*** Finished " + now);
		if (swLogg && antalT>0) {
			logg.write("*** Finished " + now);
			logg.newLine();
			logg.close();
		}
		//		if (antal>0) System.exit(0);
		//		else 		System.exit(4);
		if (antalT > 255) antalT = 255;
		System.exit(antalT);
	}

	static void execOneParSet() throws IOException,	FileNotFoundException  {
		//		System.out.println("\n---> Execute  Parameter set " + ); 
		antal=0; antcopies=0; anterrors=0; antdeleted=0; antmoved=0; antded=0; antempty=0; p=0;
		swFirst = true;
		String dat = new String("yyyyMMdd");
		String tim = new String("HHmmss");
		SimpleDateFormat dat_form;
		if (swUnique || swNrunq) {
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
		ff = x.new FileFilter(lhou, lmin, Lsec, suf, pos, pref, expath, inpath,	swNew, exfile,scanstr,fdat,tdat);
		x.new VisitAllFiles(sdir);
		if (swParfile) {
			System.out.println("*** ParRow - Files found:" + antal + "  deleted:" + antdeleted
					+ "  copied:" + antcopies + "  moved:" + antmoved + "  errors:"
					+ anterrors + "  empty:" + antempty + "  del dir:" + antded);
			//		if (antal>0) sLog=true;   // Hittades filer vill vi ha avslutande logg
			if (swLogg && antal>0) {
				logg.write("*** ParRow - Files found:" + antal + "  deleted:" + antdeleted
						+ "  copied:" + antcopies + "  moved:" + antmoved
						+ "  errors:" + anterrors + "  empty:" + antempty
						+ "  del dir:" + antded);
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
		swMove = false; swSettings = false; swSN = false; swDed = false;
		swRepl = true; swAppend = false; swUnique = false; swCountC = false;
		swExists = false; swFlat = false; swNew = false;
		swNrunq = false; swLogg = false; swNfile = false;
		moved = false;
		sdir=null; tdir=null; newfile=null;
		origdir=null; norigdir=null; nfile=null;
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
				sdir = new File(ssdir);
				origdir = sdir.toString();
			} else if (args[i].equalsIgnoreCase("-tdir")) {
				ssdir = args[++i];
				while( args.length > i+1 ) {
					if ( args[i+1].length() > 2 && args[i+1].startsWith("-")) break;
					ssdir = ssdir+" "+args[++i];
				}
				tdir = new File(ssdir);
				norigdir = tdir.toString();
			} else if (args[i].equalsIgnoreCase("-suf"))
				suf = args[++i];
			else if (args[i].equalsIgnoreCase("-pos"))
				pos = args[++i];
			else if (args[i].equalsIgnoreCase("-pref"))
				pref = args[++i];
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
				System.out.println("---> parFile: " + parFile); 
			}
		}

		// if (swCopy && swDelete && !swAppend) { swCopy=false; swDelete=false; 
		// swMove=true;} //Use move instead of copy and delete.
		if (tdir == null) {
			swCopy = false;
			swMove = false;
		} // Use move instead of copy and delete.
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

	}

	static void readParFile() {
		File[] listf;
		DirFilter df;
		String s;
		File dir = new File(".");
		String suf = ".par";
		String pos = parFile;
		int antal = 0;

		listToS = new ArrayList();  // id:mailadress.

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
		antcopies++; antcopiesT++;
	}

	// Process only files under sdir
	protected class VisitAllFiles {

		VisitAllFiles(File sdir) throws IOException {
			//						System.out.println("---> class visitAllFiles " + dir); 
			boolean copyerror;
			boolean moveerror;
			String newDir2 = null;
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
					if (swLogg && antal == 0) {
						logg.newLine();
						logg.write("*** Starting " + new Date());
						logg.newLine();
						logg.write("*** Executing ParRow: "+element);
						logg.newLine();
					}
					antal++; antalT++;
					copyerror = false;
					moveerror = false;
					swExists = false;
					moved = false;
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
							}
						} catch (IOException e) {
							copyerror = true;
							System.out.println(e);
							if (swList)
								System.out.println("   *** copy error --> "	+ newfile);
						}
					}
					if (swDelete && !copyerror && !moveerror && !moved) {
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
								anterrors++; antdeletedT++;
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
				boolean swNew, String exfile, String scanstr, String fdat,String tdat) {
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
			// System.out.println("min (minutes): " + min);
			long sec = insec + (min * 60);
			// System.out.println("sec (seconds): " + sec);

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


			// System.out.println("nu " + nu + "   inhours " + inhours);
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
				try {okay = scanF(fi,scanstr); }
				catch (IOException e) {	
					e.printStackTrace(); 
					okay = false; }

			}

			return okay;
		}

		// searches for a string inside the file 
		protected boolean scanF(File src, String scanstr) throws IOException {
			boolean found = false;
			String s;
			BufferedReader in = new BufferedReader(new FileReader(src));
			while ((s = in.readLine()) != null && !found) {
				if (s.toLowerCase().indexOf(scanstr) >= 0) found = true;
			} 
			in.close();
			return found;
		}

	}

}
