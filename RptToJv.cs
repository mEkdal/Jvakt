using System;
using System.Net;

namespace RptToJv
{
    public class RptToJv
	{
		public static void Main(string[] args)
		{
			string version = "RptToJv C# (2020-NOV-20)";
			string host = "127.0.0.1";
			int port = 1956;
			string id = null;
			string status = "OK";
			string body = " ";
			string agent = " ";
			string type = "R"; // repeating
			string prio = "30";
			string reply = "";

			try
			{
				//Console.WriteLine("-- Inet: " + Dns.GetHostName().ToString());
				agent = Dns.GetHostName().ToString();
			}
			catch (Exception e)
			{
				Console.WriteLine(e);
			} 

			for (int i = 0; i < args.Length; i++)
			{
				if (args[i].Equals("-host", StringComparison.OrdinalIgnoreCase))
				{
					host = args[++i];
				}
				else if (args[i].Equals("-port", StringComparison.OrdinalIgnoreCase))
				{
					port = int.Parse(args[++i]);
				}
				else if (args[i].Equals("-id", StringComparison.OrdinalIgnoreCase))
				{
					id = args[++i];
				}
				else if (args[i].Equals("-sts", StringComparison.OrdinalIgnoreCase))
				{
					status = args[++i];
				}
				else if (args[i].Equals("-body", StringComparison.OrdinalIgnoreCase))
				{
					body = args[++i];
				}
				else if (args[i].Equals("-type", StringComparison.OrdinalIgnoreCase))
				{
					type = args[++i];
				}
				else if (args[i].Equals("-ok", StringComparison.OrdinalIgnoreCase))
				{
					status = "OK";
				}
				else if (args[i].Equals("-err", StringComparison.OrdinalIgnoreCase))
				{
					status = "ERR";
				}
				else if (args[i].Equals("-info", StringComparison.OrdinalIgnoreCase))
				{
					status = "INFO";
				}
				else if (args[i].Equals("-prio", StringComparison.OrdinalIgnoreCase))
				{
					prio = args[++i];
				}
			}
			if (args.Length < 1 || string.ReferenceEquals(id, null))
			{
				Console.WriteLine("\n\n- "+version);
				Console.WriteLine("- by Michael Ekdal Perstorp Sweden.\n");
				Console.WriteLine("-host \t - default is 127.0.0.1");
				Console.WriteLine("-port \t - default is 1956");
				Console.WriteLine("-id ");
				Console.WriteLine("-ok   \t -> sts=OK");
				Console.WriteLine("-err  \t -> sts=ERR");
				Console.WriteLine("-info \t -> sts=INFO");
				Console.WriteLine("-sts  \t - default is OK");
				Console.WriteLine("-body \t - Any descriptive text");
				Console.WriteLine("-prio \t - default is 30");
				Console.WriteLine("-type \t - R, S, T, I, D");
				Environment.Exit(4);
			}
			if (string.ReferenceEquals(id, null))
			{
				Console.WriteLine(">>> Failure! The -id switch must contain a value! <<<");
				Environment.Exit(8);
			}
			if (!type.ToUpper().Equals("T") && !type.ToUpper().Equals("R") && !type.ToUpper().Equals("I") && !type.ToUpper().Equals("S") && !type.ToUpper().Equals("D") && !type.ToUpper().Equals("P") && !type.Equals("Active", StringComparison.OrdinalIgnoreCase) && !type.Equals("Dormant", StringComparison.OrdinalIgnoreCase))
			{
				Console.WriteLine(">>> Failure! The type must be R, I, S, T or D <<<");
				Environment.Exit(8);
			}
			if (status.Equals("INFO") && type.ToUpper().Equals("R"))
			{
				type = "I";
			}
			Message jmsg = new Message();
			SendMsg jm = new SendMsg(host, port);
			try
			{
				reply = jm.open();
				//Console.WriteLine("Status: open " + reply);
				if (!reply.Equals("failed", StringComparison.OrdinalIgnoreCase))
				{
                    jmsg.setId(id);
					jmsg.setRptsts(status);
					//if (body == null) { body = " "; }
					jmsg.setBody(body);
					jmsg.setType(type);
					jmsg.setAgent(agent);
					jmsg.setPrio(int.Parse(prio));
					if (jm.sendMsg(jmsg))
					{
						Console.WriteLine("-- Rpt Delivered --");
					}
					else
					{
						Console.WriteLine("-- Rpt Failed --");
					}
					jm.close();
				}
				else
				{
					Console.WriteLine("-- Rpt Failed --");
				}
			}
            catch (System.Exception e)
			{
				Console.WriteLine("-- Rpt Failed --" + e);
			}
		}
	}
	
	
	// class message
	public class Message
	{
		private string type = " ";
		private string id = "";
		private string rptsts = " ";
		private string body = " ";
		private string agent = " ";
		private int prio = 30;
		// private int len = 0; 

		public virtual bool setType(string type)
		{
			//type = type.replaceAll("[^a-zA-Z]", "");
			if (type.Length > 10)
			{
				type = type.Substring(0, 10);
			}
			this.type = type;
			return true;
		}
		public virtual bool setId(string id)
		{
			id = id.Trim();
			if (id.Length > 255)
			{
				id = id.Substring(0, 255);
			}
			//id = id.replaceAll("[^a-zA-Z0-9.:*_$#-]", "");
			id = id.Replace('å', 'a');
			id = id.Replace('ä', 'a');
			id = id.Replace('ö', 'o');
			id = id.Replace('Å', 'A');
			id = id.Replace('Ä', 'A');
			id = id.Replace('Ö', 'O');
			id = id.Replace(' ', '_');


			string[] separatingStrings = { "<;>" };
			string[] tab = id.Split(separatingStrings, System.StringSplitOptions.RemoveEmptyEntries);
            this.id = tab[0];
			return true;
		}
		public virtual bool setRptsts(string rptsts)
		{
			rptsts = rptsts.Trim();
			if (rptsts.Length > 255)
			{
				rptsts = rptsts.Substring(0, 255);
			}
			//rptsts = rptsts.replaceAll("[^a-zA-Z]", "");
			string[] separatingStrings = { "<;>" };
			string[] tab = rptsts.Split(separatingStrings, System.StringSplitOptions.RemoveEmptyEntries);
			this.rptsts = tab[0];
			if (this.rptsts.ToUpper().StartsWith("ERR", StringComparison.Ordinal))
			{
				this.rptsts = "ERR";
			}
			else if (this.rptsts.ToUpper().StartsWith("OK", StringComparison.Ordinal))
			{
				this.rptsts = "OK";
			}
			else
			{
				this.rptsts = "INFO ";
			}
			return true;
		}
		public virtual bool setBody(string body)
		{
			//		 regex metacharacters: <([{\^-=$!|]})?*+.>
			body = body.Trim();
			if (body.Length > 255)
			{
				body = body.Substring(0, 255);
			}
			if (body.Length == 0) body = " ";
			//body = body.replaceAll("\\\\", "/");
			//body = body.replaceAll("[^a-zA-Z0-9:;_#/><åäöÅÄÖ\"\\,\\.\\!\\?\\*\\$\\)\\(\\-\\=\\{\\}\\]\\[]", " ");
			//body = body.replaceAll(" {2,}", " "); // replace multiple spaces with one
			//string[] tab = body.Split("<;>", 2);

			string[] separatingStrings = { "<;>" };
			string[] tab = body.Split(separatingStrings, System.StringSplitOptions.RemoveEmptyEntries);
			this.body = tab[0];

			return true;
		}
		public virtual bool setAgent(string agent)
		{
			agent = agent.Trim();
			if (agent.Length > 255)
			{
				agent = agent.Substring(0, 255);
			}
			//agent = agent.replaceAll("[^a-zA-Z0-9.:!?;*_$#)(//\"><-=]", " ");
			//string[] tab = agent.Split("<;>", 2);
			//this.agent = tab[0];
			string[] separatingStrings = { "<;>" };
			string[] tab = agent.Split(separatingStrings, System.StringSplitOptions.RemoveEmptyEntries);
			this.agent = tab[0];
			return true;
		}
		public virtual bool setPrio(int prio)
		{
			this.prio = prio;
			return true;
		}

		public virtual bool MsgOk
		{
			get
			{
				return true;
			}
		}

		public virtual string Id
		{
			get
			{
				return id;
			}
		}
		public virtual string Body
		{
			get
			{
				return body;
			}
		}
		public virtual string Rptsts
		{
			get
			{
				return rptsts;
			}
		}
		public virtual string Type
		{
			get
			{
				return type;
			}
		}
		public virtual string Agent
		{
			get
			{
				return agent;
			}
		}
		public virtual int Prio
		{
			get
			{
				return prio;
			}
		}

	}

}