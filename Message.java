package Jvakt;
/*
 * 2023-03-20 V.55 Michael Ekdal		Added a trim() in setRptsts()
 * 2022-06-23 V.54 Michael Ekdal		Added getVersion() to get at consistent version throughout all classes.
 */

public class Message {
	private String type = " ";
	private String id   = "";
	private String rptsts = " ";
	private String body = " ";
	private String agent = " ";
	private int prio = 30;
	// private int len = 0; 

	public boolean setType( String type )     { 
		type = type.replaceAll("[^a-zA-Z]" , "");
		if (type.length() > 10) type = type.substring(0, 10);
		this.type = type; 
		return true;  
	}
	public boolean setId( String id )       { 
		if (id.length() > 255) id = id.substring(0, 255);
		id = id.replaceAll("[^a-zA-Z0-9.:*_$#-]" , "");
		String[] tab = id.split("<;>",2);
		this.id = tab[0];
		return true;  
	}
	public boolean setRptsts( String rptsts ) { 
		if (rptsts.length() > 255) rptsts = rptsts.substring(0, 255);
		rptsts = rptsts.replaceAll("[^a-zA-Z]" , "");
		String[] tab = rptsts.split("<;>",2);
		this.rptsts = tab[0].trim();
//		if (this.rptsts.toUpperCase().startsWith("ERR"))  this.rptsts = "ERR";   
//		else if (this.rptsts.toUpperCase().startsWith("OK"))   this.rptsts = "OK";   
//		else this.rptsts = "INFO";   
		if (this.rptsts.toUpperCase().lastIndexOf("ERR") >= 0)  this.rptsts = "ERR";   
		else if (this.rptsts.toUpperCase().lastIndexOf("OK") >= 0)   this.rptsts = "OK";   
		else this.rptsts = "INFO";   
		return true;  
	}
	public boolean setBody( String body )     { 
		//		 regex metacharacters: <([{\^-=$!|]})?*+.>
		//	 if (body == null) { body = " "; }
		if (body.length() > 255) body = body.substring(0, 255);
		body = body.replaceAll("\\\\" , "/");
//		body = body.replaceAll("[^a-zA-Z0-9:;_%@#/><åäöÅÄÖ\"\\,\\.\\!\\?\\*\\$\\)\\(\\-\\=\\{\\}\\]\\[]" , " ");
		body = body.replaceAll("[^a-zA-Z0-9:;_%@#/><\"\\,\\.\\!\\?\\*\\$\\)\\(\\-\\=\\{\\}\\]\\[]" , " ");
		body = body.replaceAll(" {2,}", " "); // replace multiple spaces with one
		body = body.trim();
		String[] tab = body.split("<;>",2);
		this.body = tab[0];
		//		System.out.println("Body length>> " + this.body.length());
		//		System.out.println("Body>> " + this.body);
		return true;  
	}
	public boolean setAgent( String agent )     { 
		if (agent.length() > 255) agent = agent.substring(0, 255);
		agent = agent.replaceAll("[^a-zA-Z0-9.:!?;*_$#)(//\"><-=]" , " ");
		String[] tab = agent.split("<;>",2);
		this.agent = tab[0];
		return true;  
	}
	public boolean setPrio( int prio )     { this.prio = prio; return true;  }

	public boolean isMsgOk()  { return true;  }

	public String getId()    { return id;  }
	public String getBody()   { return body; }
	public String getRptsts() { return rptsts; }
	public String getType()   { return type;  }
	public String getAgent()   { return agent;  }
	public int getPrio()   { return prio;  }

}