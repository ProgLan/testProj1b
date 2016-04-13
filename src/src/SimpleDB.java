package src;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.services.simpledb.model.SelectRequest;


public class SimpleDB {
	private static final String DomainName = "IPaddrDM";

	/**
	 * Upload IP addresses to the SimpleDB domain "IPAddrDM". Will create a
	 * domain if not exists.
	 **/
	public static void SimpleDBupload(String IPaddrs, String amiidx, String launch) throws Exception {

		try {
			// log in with aws credential
			System.out.println("Begin login");
			AmazonSimpleDB sdb = new AmazonSimpleDBClient(
					new PropertiesCredentials(SimpleDB.class.getResourceAsStream("/AwsCredentials.properties")));
			System.out.println("Login succeed");

			// put all IPs into a ArrayList
			List<ReplaceableItem> newItem = new ArrayList<ReplaceableItem>();

			ReplaceableAttribute Attr1 = new ReplaceableAttribute("IP", IPaddrs, true);
			ReplaceableAttribute Attr2 = new ReplaceableAttribute("ami-launch-index", amiidx, true);
			ReplaceableAttribute Attr3 = new ReplaceableAttribute("Launch-number", launch, true);

			List<ReplaceableAttribute> newAttrs = new ArrayList<ReplaceableAttribute>();

			newAttrs.add(Attr1);
			newAttrs.add(Attr2);
			newAttrs.add(Attr3);

			newItem.add(new ReplaceableItem(IPaddrs).withAttributes(newAttrs));

			// Create a domain if not exists
			if (sdb.listDomains().getDomainNames().contains(DomainName) == false) {
				sdb.createDomain(new CreateDomainRequest(DomainName));
				System.out.println("New domain created");
			}

			// put the IPs to the domain
			sdb.batchPutAttributes(new BatchPutAttributesRequest(DomainName, newItem));
		} catch (AmazonServiceException ae) {
			System.out.println(ae.getMessage());
		} catch (AmazonClientException ae) {
			System.out.println(ae.getMessage());
		}
	}

	/**
	 * 
	 * @return InstanceValues(IP,AMIindex,LaunchNumber) ArrayList
	 * @throws Exception
	 */
	public static ArrayList<InstanceValues> SimpleDBdownload() throws Exception {

		// Login
		AmazonSimpleDB sdb = new AmazonSimpleDBClient(
				new PropertiesCredentials(SimpleDB.class.getResourceAsStream("/AwsCredentials.properties")));

		// SQL select expression
		String SelectExpression = "select*from`" + DomainName + "`";
		SelectRequest selectRequest = new SelectRequest(SelectExpression);

		ArrayList<InstanceValues> InstanceList = new ArrayList<InstanceValues>();

		// append all IPs from the domain to the output ArrayList
		for (Item item : sdb.select(selectRequest).getItems()) {
			String IPvalues = "";
			String Amiidxes = "";
			String LaunchNumber = "";
			for (Attribute attr : item.getAttributes()) {
				if (attr.getName().equals("IP")) {
					IPvalues = attr.getValue();
				} else if (attr.getName().equals("ami-launch-index")) {
					Amiidxes = attr.getValue();
				} else if (attr.getName().equals("Launch-number")) {
					LaunchNumber = attr.getValue();
				}

			}
			InstanceValues Instance = new InstanceValues(IPvalues, Amiidxes, LaunchNumber);
			InstanceList.add(Instance);
		}

		return InstanceList;
	}

	/**
	 * Delete the provided ip address from domain.
	 * 
	 * @param ip
	 * @throws Exception
	 */
	public static void SimpleDBdelete(String ip) throws Exception {

		AmazonSimpleDB sdb = new AmazonSimpleDBClient(
				new PropertiesCredentials(SimpleDB.class.getResourceAsStream("/AwsCredentials.properties")));
		sdb.deleteAttributes(new DeleteAttributesRequest(DomainName, ip));

	}

}

