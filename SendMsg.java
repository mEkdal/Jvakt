package Jvakt;
/*
 * 2022-06-23 V.54 Michael Ekdal		Added getVersion() to get at consistent version throughout all classes.
 */

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
	String version = "SendMsg 2.4.54";

	public SendMsg(String host, int port ) {
		this.port = port;
		this.host = host;
	}        
//	public String open() throws IOException, UnknownHostException  {
	public String open() {
		try {
//		cs = new Socket(host, port);
		cs = new Socket();
		cs.connect(new InetSocketAddress(host, port), 15000);
		cs.setSoTimeout(15000);

		sin = cs.getInputStream();
		in = new BufferedReader(new InputStreamReader(sin));
		sut = cs.getOutputStream();
		ut = new PrintWriter(new OutputStreamWriter(sut));
		ut.println(version);
		ut.flush(); 
		return in.readLine(); 
		}
		catch ( Exception e ) {
			System.err.println("Exeption i open SendMsg  "+e);
			return "failed";	
		}
	}        
//	public boolean sendMsg(Message msg ) throws IOException, UnknownHostException  {
	public boolean sendMsg(Message msg ) {
		String line = null; 
		try {
//			System.out.println(msg.getType()+"<;>"+msg.getId()+"<;>"+msg.getRptsts()+"<;>"+msg.getBody()+"<;>"+msg.getAgent()+"<;>"+ Integer.toString(msg.getPrio())+"<;>");
			ut.println(msg.getType()+"<;>"+msg.getId()+"<;>"+msg.getRptsts()+"<;>"+msg.getBody()+"<;>"+msg.getAgent()+"<;>"+ Integer.toString(msg.getPrio())+"<;>");
			ut.flush();
			line = in.readLine();
//			System.out.println("Svar: "+line);
			if ( line.startsWith("okay")) return true;
			else                         return false;
		}
		catch (Exception e) {
			System.err.println("Exeption i SendMsg  "+e);
			return false;
		}
	}

//	public boolean close() throws IOException, UnknownHostException  {
	public boolean close() {
		try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace();}
		try {
		in.close();
		ut.close();
		cs.close(); 
		return true;
		} catch (Exception e) {
//			System.err.println(e);
			return false;
		}
	}

}