package src;
import java.io.Serializable;

public class RPC_SessionReadTuple implements Serializable{
	public boolean flag;
	public Session data;
	public String amiInd;
	public int returnCallID;
	
	public RPC_SessionReadTuple(){
		this.flag = false;
		this.data = null;
		this.amiInd = "0";
		this.returnCallID = 0;
	}
	
	public RPC_SessionReadTuple(boolean flag, Session data, int returnCallID, String amiInd){
		this.flag = flag;
		this.data = data;
		this.returnCallID = returnCallID;
		this.amiInd = amiInd;
	}
	
	public void setInfo(boolean flag, Session data, int returnCallID, String amiInd){
		this.flag = flag;
		this.data = data;
		this.returnCallID = returnCallID;
		this.amiInd = amiInd;
	}
}
