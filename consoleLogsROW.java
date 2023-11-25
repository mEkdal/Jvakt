package Jvakt;
/*
 * 2023-11-20 V.01 Michael Ekdal		New pgm to list the logs in the DB
 */

public class consoleLogsROW {

	public String id;
	private String origin;
	private String credat;
	private String row;

	public consoleLogsROW() {
		this.id = " ";
		this.credat = " ";
		this.origin = " ";
		this.row = " ";
	}

	public String getId() {
		return id;
	}

	public String getCredat() {
		return credat;
	}

	public String getOrigin() {
		return origin;
	}

	public String getRow() {
		return row;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setCredat(String credat) {
		this.credat = credat;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public void setRow(String row) {
		this.row = row;
	}

}
