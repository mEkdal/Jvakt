package Jvakt;

/**
 * @author  Michael Ekdal
 * @version 2022-JUN-22  
 */

/*
 * 2023-04-06 V2.9 Michael Ekdal PlugIvantiSM  	Completed *INSERT to create an incident and the *DELETE function to close an incident. 
 * 2023-02-28 V2.8 Michael Ekdal ManFiles   	Fixed: Not open socket to Jvakt twice. 
 * 2022-11-15 V2.7 Michael Ekdal MonAS400msgq	Fixed: When -dormant, "All reports will be forced to be 30 or higher" 
 * 2022-11-15 V2.6 Michael Ekdal *all 			Tested: Used with Java 17. 
 * 2022-06-23 V2.5 Michael Ekdal *all			Added getVersion() to get at consistent version throughout all classes.
 */

  
public class Version {

	static private String version = "2.9";

	public boolean isVersionOk()  { return true;  }

	public String getVersion()   { return version;  }

}