package src;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class GarbageCollector extends Thread{
	public static final int GABAGE_COLLECTION_INTERVAL = 30000;
	//key:sessionID_versionNum, value:session object
	public HashMap<String, Session> sessionTable;
	
	public GarbageCollector(HashMap<String, Session> sessionTable){
		this.sessionTable = sessionTable;
	}
	
	
	@Override
	public void run(){
		while(true)
		{
			ArrayList<String> removelist = new ArrayList<>();
			for(String key: sessionTable.keySet()){
				Session s = sessionTable.get(key);
				if(s.getDiscardTime().before(new Date()))
				{
					removelist.add(key);
				}
			}
			
			for(String key: removelist){
				sessionTable.remove(key);
			}
			
			for(String key: sessionTable.keySet()){
				Session s = sessionTable.get(key);
				System.out.println("current live sessionID: " + s.toString());
			}
			
			try{
				Thread.sleep(GABAGE_COLLECTION_INTERVAL);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}
}
