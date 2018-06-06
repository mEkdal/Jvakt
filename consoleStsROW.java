package Jvakt;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//package projektarbetegr11;

/**
 *
 * @author Annika
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
	public void setCondat(String console) { this.condat = condat;	}
	public void setInfo(String info)   { this.info = info;	}
	public void setPlugin(String plugin) { this.plugin = plugin;	}
	public void setAgent(String agent)  { this.agent = agent; }
	public void setSms(String sms)   { this.sms = sms;	}
	public void setSmsdat(String smsdat) { this.smsdat = smsdat;	}
	
}
