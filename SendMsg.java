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
	String version = "SendMsg 1.4 Date 2019-JUL-18";

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
//			System.out.println(msg.getType()+"<;>"+msg.getId()+"<;>"+msg.getRptsts()+"<;>"+msg.getBody()+"<;>"+msg.getAgent()+"<;>"+ Integer.toString(msg.getPrio())+"<;>");
			ut.println(msg.getType()+"<;>"+msg.getId()+"<;>"+msg.getRptsts()+"<;>"+msg.getBody()+"<;>"+msg.getAgent()+"<;>"+ Integer.toString(msg.getPrio())+"<;>");
			ut.flush();
			line = in.readLine();
//			System.out.println(line);
			if ( line.startsWith("okay")) return true;
			else                         return false;
		}
		catch (Exception e) {
			System.err.println("Exeption i SendMsg  "+e);
			return false;
		}
	}

	public boolean close() throws IOException, UnknownHostException  {
//		try { Thread.currentThread().sleep(100); } catch (InterruptedException e) { e.printStackTrace();}
		try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace();}
		try {
		ut.close();
		in.close();
		cs.close(); 
		} catch (Exception e) {
//			System.err.println(e);
		}
		return true;
	}
}