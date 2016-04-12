package src;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;


public class Session implements Serializable{
	public SessionID sessionID;
	public int[] version = new int[1];
	public String msg;
	//session createTime
	public Date createDate;
	//discardTime
	public Date discardTime;
	//TODO: info about meta server location(ami-launch-index), WQ distinct bricks
	public ArrayList<String> rpcDataBricks = new ArrayList<String>();	
	//public int recentTouchedVersion;
	
	public static final int SESSION_TIMEOUT_SECS = 30000;
	public static final int DELTA = 1000;

	//constructor1
	public Session(SessionID sessionID) {
		this.sessionID = sessionID;
		this.version[0] = 0;
		version[0] = 0;
		this.msg = "Hello User!";
		
		Date cur = new Date();
		long discardDateLong = cur.getTime() + SESSION_TIMEOUT_SECS + DELTA;
		this.discardTime = new Date(discardDateLong);
		
		this.createDate = new Date();
	}
	
	//constructor2
	public Session(SessionID sessionID, int versionNum, String msg, Date discardTime, String locData) {
		this.sessionID = sessionID;
		this.version[0] = versionNum;
		this.msg = msg;
		this.discardTime = discardTime;
		
		this.rpcDataBricks.add(locData);
		
		this.createDate = new Date();
	}
	
	//constructor3
	public Session(SessionID sessionID, int versionNum, String msg, Date discardTime) {
		this.sessionID = sessionID;
		this.version[0] = versionNum;
		this.msg = msg;
		this.discardTime = discardTime;
		
		this.rpcDataBricks = new ArrayList<String>();
	}
	
	
	public void setDiscardTime(Date discardTime) {
		this.discardTime = discardTime;
	}
	
	public Date getDiscardTime() {
		return this.discardTime;
	}
	
	public String toString() {
		String res = "";
		res += this.sessionID.toString();
		res +=  "_" + this.version[0] + "";
		
		return res;
	}

	public String metaDataString() {
		String res = "";
		for(String s: this.rpcDataBricks)
		{
			res += s + "_";
		}
		
		return res;
	}
}
