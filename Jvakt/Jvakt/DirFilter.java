package Jvakt;

import java.io.File;
import java.io.FilenameFilter;

class DirFilter implements FilenameFilter {
 String afn;
 String infix;
 
 DirFilter(String afn) { this.afn = afn; }
 DirFilter(String afn, String infix) { this.afn = afn; this.infix = infix; }
 
 public boolean accept(File dir, String name) {
// System.out.println("DifFilter name: " + name); 
// System.out.println("DifFilter dir: " + dir);  
   String f   = new File(dir + "\\" + name).getName();
   long   lm  = new File(dir + "\\" + name).lastModified();
          f   = f.toLowerCase();
          afn = afn.toLowerCase();

// If lastmodified is older than (00:00:00 GMT, January 1, 1970) plus 12 years,
// the file will be ignored. Robocopy use Jan 1980 as work date.
   lm = lm / 3600000;   // to hours
   lm = lm / 24;        // to days
   lm = lm / 365;       // to years

// System.out.println("Lm: " + lm); 
 
   if ( lm < 12 )  return false;

   if ( infix != null ) {
           infix = infix.toLowerCase();
           if ( f.indexOf(infix) >= 0 && f.endsWith(afn) == true ) return true;
           else return false;
   }
   else return f.endsWith(afn);
 }

}
