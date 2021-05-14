using System;
using System.IO;
using System.Threading;
using System.Net.Sockets;
using System.Net;
using System.Text;

namespace RptToJv
{
    // class SendMsg


    public class SendMsg
    {
		string host;
		Socket cs;
		IPHostEntry ipHostInfo;
		IPAddress ipAddress;
		IPEndPoint remoteEP;

		int port;
		byte[] bytes = new byte[1024];
		private const string version = "SendMsg C# 2020-SEP-12\r\n";

		public SendMsg(string host, int port)
			{
				this.port = port;
				this.host = host;
			}
			public string open()
			{
			try
			{
				ipHostInfo = Dns.GetHostEntry(host);
				ipAddress = ipHostInfo.AddressList[0];
				remoteEP = new IPEndPoint(ipAddress, port);

				//		cs = new Socket(host, port);
				cs = new Socket(ipAddress.AddressFamily, SocketType.Stream, ProtocolType.Tcp);
				cs.SendTimeout = 5000;
				cs.ReceiveTimeout = 5000;
				cs.Connect(remoteEP);
				//Console.WriteLine("Socket connected to {0}", cs.RemoteEndPoint.ToString());

                byte[] Out = Encoding.ASCII.GetBytes(version);
                int bytesSent = cs.Send(Out);
                int bytesRec = cs.Receive(bytes);
				//Console.WriteLine("Echoed test = {0}",
				//    Encoding.ASCII.GetString(bytes, 0, bytesRec));
				return System.Text.Encoding.UTF8.GetString(bytes);

			}
			catch (Exception e)
				{
					Console.Error.WriteLine("Exeption i open i SendMsg  " + e);
                return "failed";
				}
            // return System.Text.Encoding.UTF8.GetString(bytes);
        }

		public virtual bool sendMsg(Message msg)
			{
				try
				{
				String msgS = msg.Type + "<;>" + msg.Id + "<;>" + msg.Rptsts + "<;>" + msg.Body + "<;>" + msg.Agent + "<;>" + Convert.ToString(msg.Prio) + "<;>\r\n";
				//Console.WriteLine("msgS: " + msgS);

                byte[] Out2 = Encoding.ASCII.GetBytes(msgS);
				//Console.WriteLine("Out2: " + System.Text.Encoding.UTF8.GetString(Out2));
				//cs.Connect(remoteEP);
				int bytesSent = cs.Send(Out2,0,msgS.Length,SocketFlags.None);
				//Console.WriteLine("Send 1 made");

				int bytesRec = cs.Receive(bytes);
				//Console.WriteLine("Received  = {0}",
				//	Encoding.ASCII.GetString(bytes, 0, bytesRec));

                //			System.out.println(line);
                if (System.Text.Encoding.UTF8.GetString(bytes).ToString().StartsWith("okay", StringComparison.OrdinalIgnoreCase))
					{
						return true;
					}
					else
					{
						return false;
					}
				}
				catch (Exception e)
				{
					Console.Error.WriteLine("Exeption i SendMsg  " + e);
					return false;
				}
			}

			public virtual Boolean close()
			{
				//		try { Thread.currentThread().sleep(100); } catch (InterruptedException e) { e.printStackTrace();}
				try
				{
					Thread.Sleep(100);
				}
				catch (Exception e)
				{
					Console.WriteLine(e.ToString());
					Console.Write(e.StackTrace);
				}
				try
				{
				cs.Shutdown(SocketShutdown.Both);
				cs.Close();
				}
				catch (Exception e)
				{
				Console.WriteLine(e.ToString());
				Console.Write(e.StackTrace);
			}
			return true;
			}
		}

}