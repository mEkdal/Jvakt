package Jvakt;
import java.io.*;
import java.net.*;

public class SendMsg {
	String host;
	Socket cs;
	InputStream sin;
	BufferedReader in;
	OutputStream sut;
	PrintWriter ut;
	int port;
	String version = "SendMsg 1.2 Date 2017-07-20";

	public SendMsg(String host, int port ) {
		this.port = port;
		this.host = host;
	}        
	public String open() throws IOException, UnknownHostException  {
		cs = new Socket(host, port);
		sin = cs.getInputStream();
		in = new BufferedReader(new InputStreamReader(sin));
		sut = cs.getOutputStream();
		ut = new PrintWriter(new OutputStreamWriter(sut));
		ut.println(version);
		ut.flush();
		return in.readLine();
	}        
	public boolean sendMsg(Message msg ) throws IOException, UnknownHostException  {
		String line = null;
		try {
			ut.println(msg.getType()+"<;>"+msg.getId()+"<;>"+msg.getRptsts()+"<;>"+msg.getBody()+"<;>"+msg.getAgent()+"<;>"+ Integer.toString(msg.getPrio())+"<;>");
			ut.flush();
			line = in.readLine();
			if ( line.startsWith("okay")) return true;
			else                         return false;
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
			return false;
		}
	}

	public boolean close() throws IOException, UnknownHostException  {
		try { Thread.currentThread().sleep(100); } catch (InterruptedException e) { e.printStackTrace();}
		ut.close();
		in.close();
		cs.close();
		return true;
	}
}