package src;
import java.io.Serializable;
import java.util.ArrayList;

public class RPC_SessionWriteSubTuple implements Serializable{
	public String msg;
	public int returnCallID;
	public String amiInd;
	
	
	
	
	public void setInfo(String writeBackMsg, int returnCallID){
		this.msg = writeBackMsg;
		this.returnCallID = returnCallID;
	}
	
	public void setInfo(String writeBackMsg, int returnCallID, String amiInd){
		this.msg = writeBackMsg;
		this.returnCallID = returnCallID;
		this.amiInd = amiInd;
	}
}
