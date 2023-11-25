package Jvakt;
/*
 * 2023-11-20 V.01 Michael Ekdal		New pgm to list the logs in the DB
 */

public class consoleLogsViewROW {

	public String row;
	public int line;

	public consoleLogsViewROW() {
		this.row = " ";
		this.line = 0;
	}

	public String getRow() {
		return this.row;
	}
	public int getLine() {
		return this.line;
	}

	public void setRow(String row) {
		this.row = row;
	}
	public void setLine(int line) {
		this.line = line;
	}
}
