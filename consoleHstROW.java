package Jvakt;
/*
 * 2022-06-23 V.54 Michael Ekdal		Added getVersion() to get at consistent version throughout all classes.
 */

public class consoleHstROW {

	public int count;
	public String id;
	private int prio;
	private String type;
	private String credat;
	private String deldat;
	private String status;
	private String body;
	private String agent;

	public int row;
	public int col = 0;

	public consoleHstROW() {
		this.count = 0;
		this.id = " ";
		this.prio = 3;
		this.type = " ";
		this.credat = " ";
		this.deldat = " ";
		this.status = " ";
		this.body = " ";
		this.agent = " ";
	}

	public int getCount() {
		return count;
	}

	public String getId() {
		return id;
	}

	public int getPrio() {
		return prio;
	}

	public String getType() {
		return type;
	}

	public String getCredat() {
		return credat;
	}

	public String getDeldat() {
		return deldat;
	}

	public String getStatus() {
		return status;
	}

	public String getBody() {
		return body;
	}

	public String getAgent() {
		return agent;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setPrio(int prio) {
		this.prio = prio;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setDeldat(String deldat) {
		this.deldat = deldat;
	}

	public void setCredat(String credat) {
		this.credat = credat;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}
}
