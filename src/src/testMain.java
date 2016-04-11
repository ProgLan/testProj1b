package src;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;

public class testMain {

	public static void main(String[] args) throws Exception {
		//print out localIP
		String localIP = getIpLocalhost();
		InetAddress ip = InetAddress.getByName(localIP);
		ArrayList<InetAddress> destIP = new ArrayList<>();
		destIP.add(ip);
		System.out.println("local IP:" + localIP);
		
		
		/**SimpleDB 
		 * unit 
		 * test**/
//		SimpleDB.SimpleDBupload("10.2.3.4", "0", "0");
//		SimpleDB.SimpleDBupload("100.32.33.44", "1", "2");
//		SimpleDB.SimpleDBupload("101.22.33.44", "2", "1");
//		SimpleDB.SimpleDBdelete("10,2,3,4");
//		ArrayList<InstanceValues> res = SimpleDB.SimpleDBdownload();
//		
//		for(InstanceValues iv: res)
//		{
//			System.out.println("ip: " + iv.getIP() + "amiInd: " + iv.getAMIindex() + "launchNum" + iv.getLaunchNumber());
//			
//		}
		
		
		/**RPC 
		 * unit 
		 * test**/
		
		//rpc_client
//		RPC_Client rpc_client = new RPC_Client();
//		ArrayList<InetAddress> ipAdds = new ArrayList<InetAddress>();
//		ipAdds.add(ip);
//		rpc_client.ipAdds = ipAdds;
		
		//rpc initrandomdest test
//		ArrayList<InetAddress> ipAdds = new ArrayList<InetAddress>();
//		String ip1 = "1.1.1.1";
//		String ip2 = "2.1.1.1";
//		String ip3 = "3.1.1.1";
//		String ip4 = "4.1.1.1";
//		String ip5 = "5.1.1.1";
//		ipAdds.add(InetAddress.getByName(ip1));
//		ipAdds.add(InetAddress.getByName(ip2));
//		ipAdds.add(InetAddress.getByName(ip3));
//		ipAdds.add(InetAddress.getByName(ip4));
//		ipAdds.add(InetAddress.getByName(ip5));
//		
//		rpc_client.ipAdds = ipAdds;
//		
//		ArrayList<InetAddress> randomRes = rpc_client.initRandomWBricks();
//		
//		for(int i = 0; i < randomRes.size(); i++)
//		{
//			System.out.println(randomRes.get(i).toString());
//		}
		
		
		//rpc_server
//		RPC_Server rpc_server = new RPC_Server();
//		rpc_server.sessionTable = new HashMap<String, Session>();
//		SessionID sID = new SessionID("0", 1, 1);
//		Session newS = new Session(sID);
//		rpc_server.sessionTable.put("0_1_1_0", newS);
//		rpc_server.start();
//		
//		//rpc communication
//		//read request
//		ArrayList<InetAddress> destIpAdds = new ArrayList<InetAddress>();
//		destIpAdds.add(ip);
//		rpc_client.sessionReadClient(sID, 0, destIpAdds);
//		
//		//write request
//		rpc_client.sessionWriteClient(sID, 1, "new Hello", new Date());
//		rpc_client.sessionWriteClient(sID, 2, "new new Hello", new Date());
//		
//		System.out.println("server session table size: " + rpc_server.sessionTable.size());
		
		
		
		
		/**ServerManager 
		 * unit 
		 * test**/
		
		
		
		/**Garbage 
		 * Collector 
		 * unit 
		 * test**/
		
		

		
		
	}

	/**
	 * Get IP address if tested on localhost
	 */
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

	
}
