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
	String version = "SendMsg 1.3 Date 2018-NOV-19";

	public SendMsg(String host, int port ) {
		this.port = port;
		this.host = host;
	}        
	public String open() throws IOException, UnknownHostException  {
		try {
//		cs = new Socket(host, port);
		cs = new Socket();
		cs.connect(new InetSocketAddress(host, port), 5000);
		cs.setSoTimeout(5000);

		sin = cs.getInputStream();
		in = new BufferedReader(new InputStreamReader(sin));
		sut = cs.getOutputStream();
		ut = new PrintWriter(new OutputStreamWriter(sut));
		ut.println(version);
		ut.flush(); }
		catch ( Exception e ) {
			return "failed";	
		}
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
//			System.err.println(e);
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