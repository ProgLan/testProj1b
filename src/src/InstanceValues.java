package src;
public class InstanceValues {
	private String IPaddr;
	private String AMIindex;
	private String LaunchNumber;
	
	public InstanceValues(String IPaddr,String AMIindex,String LaunchNumber){
		this.IPaddr=IPaddr;
		this.AMIindex=AMIindex;
		this.LaunchNumber=LaunchNumber;
	}
	
	public String getIP(){
		return IPaddr;
	}
	
	public String getAMIindex(){
		return AMIindex;
	}
	
	public String getLaunchNumber(){
		return LaunchNumber;
	}
}
