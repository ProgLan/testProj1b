package src;
import java.io.Serializable;

public class RPC_SessionReadTuple implements Serializable{
	public boolean flag;
	public Session data;
	public int returnCallID;
	
	public RPC_SessionReadTuple(){
		this.flag = false;
		this.data = null;
		this.returnCallID = 0;
	}
	
	public RPC_SessionReadTuple(boolean flag, Session data, int returnCallID){
		this.flag = flag;
		this.data = data;
		this.returnCallID = returnCallID;
	}
	
	public void setInfo(boolean flag, Session data, int returnCallID){
		this.flag = flag;
		this.data = data;
		this.returnCallID = returnCallID;
	}
}
