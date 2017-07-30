package Jvakt;

/**
 * @author  Michael Ekdal
 * @version 0.1
 */

public class Message {
 private String type;
 private String id;
 private String rptsts;
 private String body;
 private String agent;
 private int prio = 30;
 
 public boolean setType( String type )     { this.type = type; return true;  }
 public boolean setId( String id )       { this.id = id; return true;  }
 public boolean setRptsts( String rptsts ) { this.rptsts = rptsts; return true;  }
 public boolean setBody( String body )     { this.body = body; return true;  }
 public boolean setAgent( String agent )     { this.agent = agent; return true;  }
 public boolean setPrio( int prio )     { this.prio = prio; return true;  }
  
 public boolean isMsgOk()  { return true;  }
 
 public String getId()    { return id;  }
 public String getBody()   { return body; }
 public String getRptsts() { return rptsts; }
 public String getType()   { return type;  }
 public String getAgent()   { return agent;  }
 public int getPrio()   { return prio;  }
  
}