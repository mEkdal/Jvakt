package Jvakt;
/*
 * 2022-07-02 V.54 Michael Ekdal		Added getVersion() to get at consistent version throughout all classes.
 */

public class consoleStsROW {

	public String state;
	public String id;
	private int prio;
	private String type;
	private String status;
	private String body;
	private String rptdat;
	private String chkday;
	private String chktim;
	private int errors;
	private int accerr;
	private String msg;
	private String msgdat;
	private String console;
	private String condat;
	private String info;
	private String plugin;
	private String agent;
	private String sms;
	private String smsdat;
	private String msg30;
	private String msgdat30;
	private String chktimto;


	public int row;
	public int col = 0;

	public consoleStsROW() {
		this.state = " ";
		this.id = " ";
		this.prio = 30;
		this.type = " ";
		this.status = " ";
		this.body = " ";
		this.rptdat = " ";
		this.chkday = " ";
		this.chktim = " ";
		this.errors = 0;
		this.accerr = 0;
		this.msg = " ";
		this.msgdat = " ";
		this.console = " ";
		this.condat = " ";
		this.info = " ";
		this.plugin = " ";
		this.agent = " ";
		this.sms = " ";
		this.smsdat = " ";
		this.msg30 = " ";
		this.msgdat30 = " ";
		this.chktimto = " ";
	}
 
	public String getState()  { return state;}
	public String getId() 	  { return id; 	}
	public int    getPrio()   { return prio; }
	public String getType()   { return type; }
	public String getStatus() {	return status;}
	public String getBody()   {	return body;}
	public String getRptdat() { return rptdat; 	}
	public String getChkday() { return chkday; 	}
	public String getChktim() { return chktim; 	}
	public int    getErrors() { return errors; 	}
	public int    getAccerr() { return accerr; 	}
	public String getMsg()    { return msg; 	}
	public String getMsgdat() { return msgdat; 	}
	public String getConsole() { return console; 	}
	public String getCondat() { return condat; 	}
	public String getInfo()   { return info; 	}
	public String getPlugin() { return plugin; 	}
	public String getAgent()  { return agent;   }
	public String getSms()    { return sms; 	}
	public String getSmsdat() { return smsdat; 	}
	public String getMsg30()    { return msg30; 	}
	public String getMsgdat30() { return msgdat30; 	}
	public String getChktimto() { return chktimto; 	}

	public void setState(String state)  { this.state = state;}
	public void setId(String id) 	  { this.id = id; 	}
	public void setPrio(int prio)   { this.prio = prio; }
	public void setType(String type)   { this.type = type; }
	public void setStatus(String status) {	this.status = status;}
	public void setBody(String body)   {	this.body = body;}
	public void setRptdat(String rptdat) { this.rptdat = rptdat;	}
	public void setChkday(String chkday) { this.chkday = chkday;	}
	public void setChktim(String chktim) { this.chktim = chktim;	}
	public void setErrors(int errors) { this.errors = errors;	}
	public void setAccerr(int accerr) { this.accerr = accerr;	}
	public void setMsg(String msg)   { this.msg = msg;	}
	public void setMsgdat(String msgdat) { this.msgdat = msgdat;	}
	public void setConsole(String console) { this.console = console;	}
	public void setCondat(String condat) { this.condat = condat;	}
	public void setInfo(String info)   { this.info = info;	}
	public void setPlugin(String plugin) { this.plugin = plugin;	}
	public void setAgent(String agent)  { this.agent = agent; }
	public void setSms(String sms)   { this.sms = sms;	}
	public void setSmsdat(String smsdat) { this.smsdat = smsdat;	}
	public void setMsg30(String msg30)   { this.msg30 = msg30;	}
	public void setMsgdat30(String msgdat30) { this.msgdat30 = msgdat30;	}
	public void setChktimto(String chktimto) { this.chktimto = chktimto;	}
	
}
