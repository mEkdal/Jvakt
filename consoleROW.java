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
public class consoleROW {

	public int count;
	public String id;
	private int prio;
	private String type;
	private String credat;
	private String condat;
	private String status;
	private String body;
	private String agent;

	public int row;
	public int col = 0;

	public consoleROW() {
		this.count = 0;
		this.id = " ";
		this.prio = 3;
		this.type = " ";
		this.credat = " ";
		this.condat = " ";
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

	public String getCondat() {
		return condat;
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

	public void setCondat(String condat) {
		this.condat = condat;
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
