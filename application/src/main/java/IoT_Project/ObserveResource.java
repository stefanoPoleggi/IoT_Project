package IoT_Project;


import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class ObserveResource implements CoapHandler {
	Queue<Timestamp> time = new LinkedList<Timestamp>();
	Queue<String> values = new LinkedList<String>();
	Queue<String> alert = new LinkedList<String>();
	private String name;
	int numIns=0;
	int maxSensing = 35;
	
	
	public ObserveResource(String name) {
		super();
		this.name = name;
	}

	@Override
	public void onLoad(CoapResponse response) {
		
		
		String value;
		Timestamp timestamp;
		String level;
		
		try {
			//System.out.println(response.getResponseText().toString());
			JSONObject msg = (JSONObject)JSONValue.parseWithException(response.getResponseText().toString());
			if(response.getResponseText().toString().contains("purification_mode")) {
				return;
			}
			value = msg.get("carbon_dioxide").toString();
			double tmp = new Double(value);
			timestamp = new Timestamp(((Long)msg.get("timestamp"))*1000);
			if(time.size()==maxSensing) {
				time.poll();
			}
			if(values.size()==maxSensing) {
				values.poll();
			}
			if(alert.size()==maxSensing) {
				alert.poll();
			}
			time.add(timestamp);
			values.add(value);
			if(tmp >=0.03 && tmp<=0.08) {
				alert.add("GREEN");
			}else if(tmp>=0.08 && tmp <= 0.2) {
				alert.add("YELLOW");
				printAlert("YELLOW", value, timestamp);
			}else {
				alert.add("RED");
				printAlert("RED", value, timestamp);
			}
			//System.out.println("Registered " + value + " timestamp " + timestamp);
		}catch(org.json.simple.parser.ParseException e) {
			e.printStackTrace();
		}	
	}
	
	public void printHistory() {
		Iterator itValues = values.iterator();
		Iterator itTime = time.iterator();
		Iterator itAlert = alert.iterator();
		
		while(itValues.hasNext()&&itTime.hasNext()&&itAlert.hasNext()) {
			System.out.println("Carbon_dioxide level: " + itValues.next() + ", timestamp: " + itTime.next() + ", alert level: "+ itAlert.next());
		}
	}
	
	public void printAlert(String lev, String val, Timestamp t) {
		System.out.println("Alert level: " + lev + " on " + name + ", carbon dioxide level:" + val + " at " + t);  
		
	}
	
	@Override
	public void onError() {
		System.out.println("An error occurred while observing");
		
	}
	
	
	
}
