package src;
import java.io.Serializable;
import java.util.Date;

public class RPC_Tuple implements Serializable{
	public int callID;
	public int opCode;
	public SessionID sessionID;
	public int versionNum;
	public String msg;
	public Date discardTime;
	
	public RPC_Tuple(int callID, int opCode, SessionID sessionID, int versionNum) {
		this.callID = callID;
		this.opCode = opCode;
		this.sessionID = sessionID;
		this.versionNum = versionNum;
	}
	
	public RPC_Tuple(int callID, int opCode, SessionID sessionID, int versionNum, String msg, Date discardTime) {
		this.callID = callID;
		this.opCode = opCode;
		this.sessionID = sessionID;
		this.versionNum = versionNum;
		this.msg = msg;
		this.discardTime = discardTime;
	}
	
	public int getCallID(){
		return this.callID;
	}
	
	public int getOpCode(){
		return this.opCode;
	}
	
	public SessionID getSessionID(){
		return this.sessionID;
	}
	
	public int getVersionNum(){
		return this.versionNum;
	}
	
	public String getWrittenMsg(){
		return this.msg;
	}
	
	public Date getDiscardTime(){
		return this.discardTime;
	}
}
