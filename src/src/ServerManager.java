package src;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public class ServerManager{
	private static final String cookieName = "CS5300PROJ1SESSION";
	private static final int cookieLength = 6;
	
	private RPC_Client rpc_client;
	private RPC_Server rpc_server;
	private GarbageCollector garbageCollector;
	
	private InetAddress localIp;
	private String localIPString;
	public String amiIndex;
	
	//key: ip, value:launch index
	public HashMap<String, String> amiIpTable;
	
	//key:sessionID_versionNum, value: session object
	public HashMap<String, Session> sessionTable;
	
	public int sessionNum = 0;
	//init reboot num as -1 because the first time init is not considered as reboot
	//reboot num should be stored in file system and retrive from file system
	public int rebootNum = 0;
	
	public static final int SESSION_TIMEOUT_SECS = 30000;
	public static final int DELTA = 1000;
	
	
	
	public ServerManager(){
		//if test on local, set local Ip address
		this.localIPString = getIpLocalhost();
		try {
			this.localIp= InetAddress.getByName(this.localIPString);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("server manager: local IP" + this.localIp);
		
		//if test on ec2
		//this.localIPString = getIPFromFile();
				
		//sync amiIpTable from simpleDB
		try {
			this.amiIpTable = getAMIIPTableFromSDB();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("server manager: localAmiIPtable size " + this.amiIpTable.size());
		
		//get this server's amiIndex from SimpleDB and store in the amiIndex variable
		this.amiIndex = this.getLoclAmi();
		
		//get reboot num from file system
		try {
			//TODO
			this.rebootNum = getRebootNumFromSDB();
			//this.rebootNum = 0;
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		this.sessionTable = new HashMap<String, Session>();
		try {
			this.rpc_client = new RPC_Client(getSystemIPAdds());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.rpc_server = new RPC_Server(sessionTable, this.amiIndex);
		this.garbageCollector = new GarbageCollector(sessionTable);
		
		//start rpc_server listening and garbageCollector work
		rpc_server.start();
		garbageCollector.start();
	}
	
	//get Session 
	public Session getOrUpdate(HttpServletRequest request){
		//find if CS5300PROJ1SESSION cookie exists
		Cookie[] cookies = request.getCookies();
		//cookie format: serverami-launch-index_rebootnum_sessionNum_versionNum_wq0serverami_wq1serverami
		Cookie cookie = null;	
		if(cookies != null) {
			for(Cookie c: cookies){
				if(c.getName().equals(cookieName)) {
					cookie = c;
				}
			}
		}
		
		Session exceptionSession = null;
		
		
		//not first time user
		if(cookie != null && cookie.getValue().length() > 0)
		{
			//fake return Session
			Session resSession = null;
			
			String[] cookieValue = cookie.getValue().split("_");
			String amiInd;
			int rebootNum;
			int sessionNum;
			int versionNum;
			String wq0amiInd;
			String wq1amiInd;
			
			if(cookieValue.length != cookieLength)
			{
				SessionID sid = initSessionID();
				Date discardDate = initDiscardTime();
				
				resSession = new Session(sid, 0, "Hello User!", discardDate);
				
				//first time user only send write request
				try {
					RPC_SessionWriteTuple swt = this.rpc_client.sessionWriteClient(sid, 0, "Hello User!", discardDate);
					ArrayList<String> locMetaData = swt.dataBrickLocation;
					for(String loc: locMetaData)
					{
						resSession.rpcDataBricks.add(loc);
					}
					
					
				} catch (ClassNotFoundException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return resSession;
			}
			else
			{
				amiInd = cookieValue[0];
				rebootNum = Integer.parseInt(cookieValue[1]);
				sessionNum = Integer.parseInt(cookieValue[2]);
				versionNum = Integer.parseInt(cookieValue[3]);
				wq0amiInd = cookieValue[4];
				wq1amiInd = cookieValue[5];
			}

			//check if current server is in the wq list
			boolean isInWq = false;
			if(this.amiIpTable.get(this.localIPString).equals(wq0amiInd) || this.amiIpTable.get(this.localIPString).equals(wq1amiInd))
			{
				isInWq = true;
			}
				
			//different operations by different user inputs
			String op = request.getParameter("op");
			if(op == null)
			{
				op = "Refresh";
			}
			
			if(op != null)
			{
				//write request version + 1 with new msg
				if(op.equals("Replace"))
				{
					String newMsg = request.getParameter("replaceMsg");
					
					SessionID sessionID = new SessionID(amiInd, rebootNum, sessionNum);
					Date discardDate = initDiscardTime();
					RPC_SessionWriteTuple swt = null;
					try {
						swt = this.rpc_client.sessionWriteClient(sessionID, versionNum + 1, newMsg, discardDate);
					} catch (ClassNotFoundException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					resSession = new Session(sessionID, versionNum + 1, newMsg, discardDate);
					ArrayList<String> locMetaData = swt.dataBrickLocation;
					for(String loc: locMetaData)
					{
						resSession.rpcDataBricks.add(loc);
					}
				}
				//read request, and write request version + 1
				else if(op.equals("Refresh"))
				{
					String prevMsg = "";
					String foundServerAmi = "";
					
					if(isInWq)
					{
						for(String key: this.sessionTable.keySet())
						{
							String[] keyVals = key.split("_");
							
							if(keyVals[0].equals(amiInd) && Integer.parseInt(keyVals[1]) == rebootNum && Integer.parseInt(keyVals[2]) == sessionNum && Integer.parseInt(keyVals[3]) == versionNum){
								String getKey = amiInd + "_" + rebootNum + "_" + sessionNum + "_" + versionNum;
								prevMsg = this.sessionTable.get(getKey).msg;
								foundServerAmi = this.amiIndex;
							}
							else
							{
								SessionID sessionID = new SessionID(amiInd, rebootNum, sessionNum);
								ArrayList<InetAddress> destIpAdds = null;
								try {
									destIpAdds = getDestIpAddListByCookieData(wq0amiInd, wq1amiInd);
								} catch (UnknownHostException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								try {
									Session temp = this.rpc_client.sessionReadClient(sessionID, versionNum, destIpAdds).data;
									prevMsg = temp.msg;
									foundServerAmi = temp.foundServerAmiInd;
								} catch (ClassNotFoundException | IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						
						//session expire
						if(prevMsg.equals(""))
						{
							SessionID sid = initSessionID();
							Date discardDate = initDiscardTime();
							
							resSession = new Session(sid, 0, "Hello User!", discardDate);
							resSession.createDate = new Date();
							
							//first time user only send write request
							try {
								RPC_SessionWriteTuple swt = this.rpc_client.sessionWriteClient(sid, 0, "Hello User!", discardDate);
								ArrayList<String> locMetaData = swt.dataBrickLocation;
								for(String loc: locMetaData)
								{
									resSession.rpcDataBricks.add(loc);
								}
								
								
							} catch (ClassNotFoundException | IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							resSession.foundServerAmiInd = foundServerAmi;
							return resSession;
						}
					}
					else
					{
						SessionID sessionID = new SessionID(amiInd, rebootNum, sessionNum);
						ArrayList<InetAddress> destIpAdds = null;
						try {
							destIpAdds = getDestIpAddListByCookieData(wq0amiInd, wq1amiInd);
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
							Session temp = this.rpc_client.sessionReadClient(sessionID, versionNum, destIpAdds).data;
							prevMsg = temp.msg;
							foundServerAmi = temp.foundServerAmiInd;
						} catch (ClassNotFoundException | IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					SessionID sessionID = new SessionID(amiInd, rebootNum, sessionNum);
					Date discardDate = initDiscardTime();
					RPC_SessionWriteTuple swt = null;
					try {
						swt = this.rpc_client.sessionWriteClient(sessionID, versionNum + 1, prevMsg, discardDate);
					} catch (ClassNotFoundException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					resSession = new Session(sessionID, versionNum + 1, prevMsg, discardDate);
					ArrayList<String> locMetaData = swt.dataBrickLocation;
					for(String loc: locMetaData)
					{
						resSession.rpcDataBricks.add(loc);
					}
					
					resSession.foundServerAmiInd = foundServerAmi;
				}
			}
			
			return resSession;
		}
		//TODO
		//first time user
		else
		{
			SessionID sid = initSessionID();
			Date discardDate = initDiscardTime();
			
			Session resSession = new Session(sid, 0, "Hello User!", discardDate);
			resSession.createDate = new Date();
			
			//first time user only send write request
			try {
				RPC_SessionWriteTuple swt = this.rpc_client.sessionWriteClient(sid, 0, "Hello User!", discardDate);
				ArrayList<String> locMetaData = swt.dataBrickLocation;
				for(String loc: locMetaData)
				{
					resSession.rpcDataBricks.add(loc);
				}
				
				
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return resSession;
		}
		
		//System.out.println("ServerManager: get or update why fall here? not in wq list, not read or write request...");
		//return exceptionSession;
	}
	
	
	//init a new session with a global unique ID
	public SessionID initSessionID() {
		//generate a global unique ID
		this.sessionNum++;
		SessionID sID = new SessionID(this.amiIndex, rebootNum, sessionNum);
		
		return sID;
	}
	
	public static Date initDiscardTime(){
		Date now = new Date();
		long longNow = now.getTime();
		Date discardTime = new Date(longNow + DELTA + SESSION_TIMEOUT_SECS);
		
		return discardTime;
	}
	
	
	
	/**Cookie 
	 * Related 
	 * Function**/
	public Cookie setCookie(HttpServletResponse response,Session session) throws ServletException {
		
		Cookie cookie = new Cookie(cookieName, cookieMaker(session));
		//manually set cookie expire
		cookie.setMaxAge(SESSION_TIMEOUT_SECS / 1000);
		//TODO: cookie set Domain， get root domain first
		//cookie.setDomain("server0.lz376.bigdata.systems");
		response.addCookie(cookie);
		
		return cookie;
	}
	
	//e.g: 1_2_4_11_0_3
	//ami-index: 1
	//rebootNum:2
	//sessionNum:4
	//versionNum:11
	//store in other ami-index server: 0
	//store in other ami-index server: 3
	public String cookieMaker(Session session) {
		if(session == null)
		{
			return "1";
		}
		
		StringBuilder res = new StringBuilder("");
		res.append(session.sessionID.toString());
		//TODO
		res.append("_" + session.version[0]);
		for(String loc: session.rpcDataBricks)
		{
			res.append("_" + loc);
		}
		
		return res.toString();
	}
	
	public String locMetaData(Cookie c) {
		String res = "";
		
		String cookieVal = c.getValue();
		String[] cookieVals = cookieVal.split("_");
		System.out.println("cookieVal's length:" + cookieVals.length);
		
		
		
		res += cookieVals[4];
		res += " and ";
		res += cookieVals[5];
		
		return res;
	}
	
	
	/**Independent 
	 * Helper 
	 * Function**/	
	
	//Get IP address if tested on localhost
	public static String getIpLocalhost()
	{
	    String ip = null;
	    try {
	        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
	        while (interfaces.hasMoreElements()) {
	            NetworkInterface iface = interfaces.nextElement();
	            // filters out 127.0.0.1 and inactive interfaces
	            if (iface.isLoopback() || !iface.isUp())
	                continue;

	            Enumeration<InetAddress> addresses = iface.getInetAddresses();
	            while(addresses.hasMoreElements()) {
	                InetAddress addr = addresses.nextElement();
	                ip = addr.getHostAddress();
	            }
	        }
	    } catch (SocketException e) {
	        throw new RuntimeException(e);
	    }
		return ip;
	}
	
	public int getRebootNumFromSDB() throws Exception{
		int res = 0;
		ArrayList<InstanceValues> list = SimpleDB.SimpleDBdownload();
		
		for(InstanceValues item: list)
		{
			if(item.getIP().equals(this.localIPString))
			{
				res = Integer.parseInt(item.getLaunchNumber());
			}
		}
		
		return res;
	}
	
	public static String getIPFromFile() {
		File file = new File("Home/local-ipv4");
		String res = "";
		int item = 0;
		int dotCount = 0;
		
		try (FileInputStream fis = new FileInputStream(file)) {

			//System.out.println("Total file size to read (in bytes) : "+ fis.available());

			int content;
			while ((content = fis.read()) != -1) {
				if((char)content != '.')
				{
					item = item * 10 + ((char)content - '0');
				}
				else
				{
					dotCount++;
					if(dotCount <= 3)
					{
						res += "" + item + ".";
						item = 0;	
					}
				}
			}
			
			res += item;

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	
	public String getLoclAmi() {
		String res = "no ami in table?";
		
		for(String key: this.amiIpTable.keySet())
		{
			if(this.localIPString.equals(key))
			{
				res = this.amiIpTable.get(key);
			}
		}
		
		return res;
	}
	
 	
	public static HashMap<String, String> getAMIIPTableFromSDB() throws Exception{
		ArrayList<InstanceValues> list = SimpleDB.SimpleDBdownload();
		HashMap<String, String> res = new HashMap<String, String>();
		
		for(InstanceValues item: list)
		{
			res.put(item.getIP(), item.getAMIindex());
		}
		
		return res;
	}
	
 	
	public ArrayList<InetAddress> getSystemIPAdds() throws UnknownHostException{
		ArrayList<InetAddress> res = new ArrayList<InetAddress>();
		
		for(String s: this.amiIpTable.keySet())
		{
			InetAddress ip = InetAddress.getByName(s);
			res.add(ip);
		}
		
		return res;
	}
	
	public ArrayList<InetAddress> getDestIpAddListByCookieData(String locAmi1, String locAmi2) throws UnknownHostException{
	 ArrayList<InetAddress> res = new ArrayList<InetAddress>();
	 
	 for(String ipKey: this.amiIpTable.keySet())
	 {
		 if(this.amiIpTable.get(ipKey).equals(locAmi1))
		 {
			 res.add(InetAddress.getByName(ipKey));
		 }
		 if(this.amiIpTable.get(ipKey).equals(locAmi2))
		 {
			 res.add(InetAddress.getByName(ipKey));
		 }
	 }
	 
	 return res;
 }
}
