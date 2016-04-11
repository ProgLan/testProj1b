package src;
import java.io.Serializable;

public class SessionID implements Serializable{
	//ami-launch-index
	public String serverID;
	public int rebootNum;
	public int sessionNum;
	
	public SessionID(String serverID, int rebootNum, int sessionNum){
		this.serverID = serverID;
		this.rebootNum = rebootNum;
		this.sessionNum = sessionNum;
	}
	
	public boolean equals(SessionID sessionId){
		if(sessionId.getServerID().equals(this.serverID) && sessionId.getRebootNum() == this.getRebootNum() && sessionId.getSessionNum() == this.sessionNum)
		{
			return true;
		}
		
		return false;
	}
	
	public String getServerID(){
		return this.serverID;
	}
	
	public int getRebootNum(){
		return this.rebootNum;
	}
	
	public int getSessionNum(){
		return this.sessionNum;
	}
	
	public boolean equals(String serverID, int rebootNum, int sessionNum){
		if(this.serverID.equals(serverID) && this.rebootNum == rebootNum && this.sessionNum == sessionNum)
		{
			return true;
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		String res = "";
		res += this.serverID + "_";
		res +=  "" + this.rebootNum + "_";
		res += "" + this.sessionNum;
		
		return res;
	}
}
