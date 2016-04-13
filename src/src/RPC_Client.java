package src;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.net.InetAddress;



//R = 2 = F + 1
//W = 3 = 2 * F + 1
//WQ = 2 = R
//N = 3 >= W

public class RPC_Client{
	private int callIDGenerator = 0;
	public static final int portProj1bRPC = 5300;
	public static final int maxPacketSize = 1000;
	
	//system ip address list
	public ArrayList<InetAddress> ipAdds;
	
	private Lock lock = new ReentrantLock();
	
	public static final int R = 2;
	public static final int WQ = 2;
	public static final int W = 3;
	
	public RPC_Client(){}
	
	public RPC_Client(ArrayList<InetAddress> ipAdds){
		this.ipAdds = ipAdds;
	}
	
	//send read request to R data bricks randomly choose from WQ
	//use first response received
	//destIpAdd get from user cookie
	public RPC_SessionReadTuple sessionReadClient(SessionID sessionID, int versionNum, ArrayList<InetAddress> destIpAdds) throws IOException, ClassNotFoundException {
		boolean flagVal = false;
		RPC_SessionReadTuple srt = null;
		
		DatagramSocket rpcSocket = new DatagramSocket();
		
		lock.lock();
		int callID = this.callIDGenerator++;
		lock.unlock();
		
		for(InetAddress destIpAddress: destIpAdds)
		{
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(buffer);
			RPC_Tuple data;
			data = new RPC_Tuple(callID, RPC_OpCode.opRead, sessionID, versionNum);
			
			out.writeObject(data);
			out.close();
			buffer.close();
			
			byte[] outBuf = buffer.toByteArray();
			
			DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, destIpAddress, portProj1bRPC);
			rpcSocket.send(sendPkt);
			
			byte[] inBuf = new byte[maxPacketSize];
			DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
			int returnCallId;
			try{
				do{
					recvPkt.setLength(inBuf.length);
					rpcSocket.receive(recvPkt);
					ByteArrayInputStream input = new ByteArrayInputStream(inBuf);
					ObjectInputStream object = new ObjectInputStream(input);
					
					RPC_SessionReadTuple recvTuple = (RPC_SessionReadTuple)object.readObject();
					returnCallId = recvTuple.returnCallID;
					//System.out.println("rpcclient: read return callID: " + returnCallId);
					boolean flag = recvTuple.flag;
					//System.out.println("rpcclient: read return flag value: " + flag);
					
					if(flag)
					{
						flagVal = true;
						//construct a new srt and setInfo
						srt = new RPC_SessionReadTuple();
						srt.setInfo(flag, recvTuple.data, returnCallId, recvTuple.amiInd);
						
						System.out.println("rpcclient: flagVal becomes true");
					}
				}while(returnCallId != callID);
				
			}catch(SocketTimeoutException stoe){
				//time out
				recvPkt = null;
				System.out.println("rpc client: SocketTimeoutException");
			}catch(IOException ioe){
				//other error
				System.out.println("rpc client: IOException");
			}
			
			
			if(flagVal)
			{
				break;
			}
		}
		
		rpcSocket.close();

		return srt;
	}
	
	//send write requests to W data bricks randomly choose from all data bricks
	//wait for first WQ successful response
	public RPC_SessionWriteTuple sessionWriteClient(SessionID sessionID, int versionNum, String msg, Date discardTime) throws IOException, ClassNotFoundException {
		int writeSucNum = 0;
		RPC_SessionWriteTuple swt = null;
		DatagramSocket rpcSocket = new DatagramSocket();
		
		lock.lock();
		int callID = this.callIDGenerator++;
		lock.unlock();
		
		//use ipAdd to determine destIpAdds
		ArrayList<InetAddress> destIpAdds = initRandomWBricks();
		//construct a new swt and set info
		swt = new RPC_SessionWriteTuple();
		
		HashSet<String> visited = new HashSet<String>(); 
		

	
		for(InetAddress destIpAddress: destIpAdds)
		{
			
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(buffer);
			RPC_Tuple data;
			
			data = new RPC_Tuple(callID, RPC_OpCode.opWrite, sessionID, versionNum, msg, discardTime);	
			
			out.writeObject(data);
			out.close();
			buffer.close();
			
			byte[] outBuf = buffer.toByteArray();
			
			DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, destIpAddress, portProj1bRPC);
			rpcSocket.send(sendPkt);
			
			byte[] inBuf = new byte[maxPacketSize];
			DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
			int returnCallId;
			
			try{
				do{
					recvPkt.setLength(inBuf.length);
					rpcSocket.receive(recvPkt);
					ByteArrayInputStream input = new ByteArrayInputStream(inBuf);
					ObjectInputStream object = new ObjectInputStream(input);
					
					RPC_SessionWriteSubTuple recvTuple = (RPC_SessionWriteSubTuple)object.readObject();
					returnCallId = recvTuple.returnCallID;
					String amiInd = recvTuple.amiInd;
					System.out.println("rpcclient: write return callID: " + returnCallId);
					
					
					if(recvTuple.msg.equals("ws"))
					{
						if(!visited.contains(amiInd))
						{
							writeSucNum++;
							
							swt.msg = "AtleastOneSuccess";
							swt.returnCallID = returnCallId;
							if(swt.dataBrickLocation == null)
							{
								swt.dataBrickLocation = new ArrayList<String>();
								swt.dataBrickLocation.add(amiInd);
							}
							else
							{
								swt.dataBrickLocation.add(amiInd);
							}
							
							visited.add(amiInd);
							System.out.println("rpc client write requests " + writeSucNum + " time success");
						}
						
					}
				}while(returnCallId != callID);
			}catch(SocketTimeoutException stoe){
				//time out
				recvPkt = null;
				System.out.println("rpc client: SocketTimeoutException");
			}catch(IOException ioe){
				//other error
				System.out.println("rpc client: IOException");
			}
			
			if(writeSucNum == WQ)
			{
				break;
			}
		}
		
		
		rpcSocket.close();
		
		return swt;
	}
	
	//return a W databrick ip address list 
	public ArrayList<InetAddress> initRandomWBricks(){
		ArrayList<InetAddress> resList = new ArrayList<InetAddress>();
		HashSet<InetAddress> visited = new HashSet<InetAddress>();
		
		for(int i = 0; i < W; i++)
		{
			int randomPick = (int) (Math.random() * (this.ipAdds.size()));		
			
			if(!visited.contains(this.ipAdds.get(randomPick)))
			{
				resList.add(this.ipAdds.get(randomPick));
				visited.add(this.ipAdds.get(randomPick));
			}
		}
		
		return resList;
	}
}
