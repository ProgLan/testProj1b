package src;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;


public class RPC_Server extends Thread{
	public static final int portProj1bRPC = 5300;
	public static final int maxPacketSize = 1000;
	//key: sessionID_versionNum, value: Session object
	public HashMap<String, Session> sessionTable;
	//TODO
	public String amiInd;
	
	public RPC_Server() {
		this.sessionTable = new HashMap<String, Session>();
	}
	
	public RPC_Server(HashMap<String, Session> table, String amiInd) {
		this.sessionTable = table;
		this.amiInd = amiInd;
	}
	
	@Override
	public void run(){
		
		DatagramSocket rpcSocket = null;
		try{
			rpcSocket = new DatagramSocket(portProj1bRPC); 
		}catch(SocketException e){
			e.printStackTrace();
		}
		
		while(true) {
		    try{
		    	byte[] inBuf = new byte[maxPacketSize];
			    DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
			    rpcSocket.receive(recvPkt);
			    System.out.println("package received");
			    
			    InetAddress returnAddr = recvPkt.getAddress();
			    int returnPort = recvPkt.getPort();
			    ByteArrayInputStream input = new ByteArrayInputStream(inBuf);
				ObjectInputStream object = new ObjectInputStream(input);
				RPC_Tuple recvTuple = (RPC_Tuple)object.readObject();
				int returnCallId = recvTuple.getCallID();
			    
				// here inBuf contains the callID and operationCode
			    int operationCode = recvTuple.getOpCode(); // get requested operationCode
			    int versionNum = recvTuple.getVersionNum();
			    SessionID sID = recvTuple.getSessionID();
			    String msg = recvTuple.getWrittenMsg();
			    Date discardDate = recvTuple.getDiscardTime();
			    
			    byte[] outBuf = null;
			    
			    switch(operationCode) {
			    	case RPC_OpCode.opRead:
			    		// SessionRead accepts call args and returns call results 
			    		outBuf = this.sessionRead(sID, versionNum, returnCallId);
			    		break;
			    	case RPC_OpCode.opWrite:
			    		outBuf = this.sessionWrite(sID, versionNum, msg, returnCallId, discardDate);
			    		break;
			    }
			    // here outBuf should contain the callID and results of the call
			    DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length,returnAddr, returnPort);
			    rpcSocket.send(sendPkt);
		    }catch(Exception ex){
		    	ex.printStackTrace();
		    	System.out.println("rpcserver: server send fail");
		    }
		  }
	}
	
	public byte[] sessionRead(SessionID sessionID, int versionNum, int returnCallId) {
		RPC_SessionReadTuple srt = new RPC_SessionReadTuple();
		byte[] outByte;
		
		for(String key: sessionTable.keySet())
		{
			String[] keyVals = key.split("_");
			int version = Integer.parseInt(keyVals[3]);
			
			if(sessionID.equals(("" + keyVals[0]), Integer.parseInt(keyVals[1]), Integer.parseInt(keyVals[2])) && versionNum == version)
			{
				srt.setInfo(true, sessionTable.get(key), returnCallId);
				outByte = toByteArray(srt);
				System.out.println("read success");
				return outByte;
			}
		}
		
		srt.setInfo(false, null, returnCallId);
		outByte = toByteArray(srt);
		System.out.println("read fail");
		return outByte;
	}
	
	//when finish update, session will increment the version number
	public byte[] sessionWrite(SessionID SessionID, int versionNum, String msg, int returnCallId, Date discardTime) {
		RPC_SessionWriteSubTuple swst = new RPC_SessionWriteSubTuple();
		byte[] outByte;
		
		Session s = new Session(SessionID, versionNum, msg, discardTime, this.amiInd);
		String newKey = SessionID.toString() + "_" + (versionNum);
		this.sessionTable.put(newKey, s);
		
		//write successful
		swst.setInfo("ws", returnCallId, this.amiInd);
		outByte = toByteArray(swst);
		System.out.println("write success");
		return outByte;
	}
	
	public static <T> byte[] toByteArray(T srt){
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ObjectOutput out;
		try {
			out = new ObjectOutputStream(buffer);
			out.writeObject(srt);
			out.close();
			buffer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer.toByteArray();	
	}
}
