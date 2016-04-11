package src;
import java.io.Serializable;
import java.util.ArrayList;

public class RPC_SessionWriteTuple implements Serializable{
	public String msg;
	public int returnCallID;
	public ArrayList<String> dataBrickLocation;
	
	
	public void setInfo(String writeBackMsg, int returnCallID){
		this.msg = writeBackMsg;
		this.returnCallID = returnCallID;
	}
	
	public void setInfo(String writeBackMsg, int returnCallID, ArrayList<String> dataBrickLocation){
		this.msg = writeBackMsg;
		this.returnCallID = returnCallID;
		this.dataBrickLocation = dataBrickLocation;
	}
}
