package Jvakt;

/**
 * @author  Michael Ekdal
 * @version 2022-JUN-22  
 */

/*
 * 2024-09-02 V2.25 Michael Ekdal SendSMSSTShttp,SendSMShttp	Added two new pgm to support mobile text messages through http calls to a RUT241 Teltonika box.
 * 2024-07-09 V2.24 Michael Ekdal Jvakr...		Added more info when reporting status to Jvakt server.
 * 2024-06-19 V2.23 Michael Ekdal consoleDM...	Improved error handling in consoleDM .
 * 2024-06-16 V2.22 Michael Ekdal CheckStatus	Fixed logic send rows directly to history. Duplicates in history.
 * 2024-05-29 V2.21 Michael Ekdal consoleSts,DBUpdate,CheckStatus	Added possibility to have a H in the state field to send rows directly to history
 * 2024-05-09 V2.20 Michael Ekdal MonAS400msgq	Added opportunity to change the default sts and sev in the MonAS400msgq.csv file
 * 2024-05-09 V2.20 Michael Ekdal CpyAS400TblToSQLsvr Added new pgm to copy AS400 tables from AS400 to SQL server.
 * 2024-02-24 V2.19 Michael Ekdal DBUpdate,CheckStatus	Added logic to restrict number of messages per ID in the console
 * 2023-11-25 V2.18 Michael Ekdal CheckLogs		Added a function to import logs file into the Jvakt database.
 * 2023-10-25 V2.17 Michael Ekdal CheckLogs		If there is at least a must line the program is not aborted.
 * 2023-10-04 V2.16 Michael Ekdal Console,DBupdate,plugIvantiSM	Added *MANUAL to trigger the plugins from the console
 * 2023-09-18 V2.15 Michael Ekdal CheckLogs 	Changed charset to default UTF8 before every new log file is checked.
 * 2023-09-01 V2.14 Michael Ekdal ManFiles		Added -fsize and -tsize to be able to select by size. 
 * 2023-08-23 V2.13 Michael Ekdal SendSMS, SendSMSSTS: Increased sleep times when waiting for the operator response.
 * 2023-08-09 V2.12 Michael Ekdal New plugin, new menus, new search button, new parameter to the mon* programs.
 * 2023-06-23 V2.11 Michael Ekdal PlugIvantiSM  Will send also the Type=I Status=INFO combo to cmdPlug1.
 * 2023-05-26 V2.10 Michael Ekdal GUI pgm    	Added menus to GUI programs. 
 * 2023-04-06 V2.9  Michael Ekdal PlugIvantiSM 	Completed *INSERT to create an incident and the *DELETE function to close an incident. 
 * 2023-02-28 V2.8  Michael Ekdal ManFiles   	Fixed: Not open socket to Jvakt twice. 
 * 2022-11-15 V2.7  Michael Ekdal MonAS400msgq	Fixed: When -dormant, "All reports will be forced to be 30 or higher" 
 * 2022-11-15 V2.6  Michael Ekdal *all 			Tested: Used with Java 17. 
 * 2022-06-23 V2.5  Michael Ekdal *all			Added getVersion() to get at consistent version throughout all classes.
 */

  
public class Version {

	static private String version = "2.25";

	public boolean isVersionOk()  { return true;  }

	public String getVersion()   { return version;  }

}