package IoT_Project;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Timestamp;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Interface {

	static Server regServer = new Server(5683);
	
	public static void main(String[] args) {
		new Thread() {
			public void run() {
				regServer.start(); 
			}
			
		}.start();
		
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader commandLine = new BufferedReader(input);
		showCommand();
		while(true) {
			String str = "";
			try {
				str = commandLine.readLine();
			}catch(Exception e) {
				System.out.println(e.toString());
			}
			String[] command = str.split(" ");
			switch(command[0]) {
			case "LIST": //show the list of all the resources registered
				showResources();
				break;
			case "GET": //get the livel of carbon dioxide from a sensor or the operation mode of an actuator
				if(command[3].equals("sensor"))
					getSensorStatus(command[1]+" "+command[2]);
				else
					getActuatorStatus(command[1]+" "+command[2]);
				break;
			case "SET": //set the status of a sensor/all sensors on/off
				if(!command[1].contentEquals("ALL")) {
					setStatus(command[1]+" "+command[2], command[3]); //set a specific actuator
				}else {
					setAll(command[1]); //set all actuators
				}
				break;
			case "HISTORIC": //show the historic of the sensing of a sensor
				viewHistoric(command[1]+" "+command[2]);
				break;
			case "CLOSE": //exit the application
				System.out.println("Closing the application");
				System.exit(0);
				break;
				
				
		}
			
			
		}
		
		

	}
	
	public static void showCommand() {
		System.out.println("The available commands are the following:");
		System.out.println("LIST: to see the resources available");
		System.out.println("GET node # sensor: to see the value of the carbon dioxide registered by the sensor choosen");
		System.out.println("GET node # actuator: to see the status of the actuator choosen (ON/OFF)");
		System.out.println("SET node # *status*: to set the status of the actuator identified by index (on/off)");
		System.out.println("HISTORIC node #: to see the historic of the latest sensing of the node");
		System.out.println("CLOSE: to close the application");			
	}
	
	public static void showResources() {
		for(int i = 0; i < Server.regResources.size(); i++) {
			System.out.println(Server.regResources.get(i).getName() + " " + Server.regResources.get(i).getPath());
		}
	}
	
	public static void getSensorStatus(String node) {
		boolean found = false;
		int i = 0;
		System.out.println(node);
		for(; i < Server.regResources.size(); i++) {
			System.out.println(Server.regResources.get(i).getName());
			if(node.equals(Server.regResources.get(i).getName())&&(Server.regResources.get(i).getPath().contains("sensors"))){
				found = true;
				break;
			}
		}
		
		if(found) {	
			RegisteredResource r = Server.regResources.get(i);
			String response = r.get(MediaTypeRegistry.APPLICATION_JSON).getResponseText();
			try {
				JSONObject msg = (JSONObject)JSONValue.parseWithException(response);
				
				String value = msg.get("carbon_dioxide").toString();
				double tmp = new Double(value);
				Timestamp timestamp = new Timestamp(((Long)msg.get("timestamp"))*1000);
				if(tmp >=0.03 && tmp<=0.08) {
					System.out.println("Sensor on " + node + " observe a carbon dioxide level of " + value + " at " + timestamp + "; alert level: GREEN");
				}else if(tmp>=0.08 && tmp <= 0.2) {
					System.out.println("Sensor on " + node + " observe a carbon dioxide level of " + value + " at " + timestamp + "; alert level: YELLOW");
				}else {
					System.out.println("Sensor on " + node + " observe a carbon dioxide level of " + value + " at " + timestamp + "; alert level: RED");
				}
				
			}catch(org.json.simple.parser.ParseException e) {
				e.printStackTrace();
			}
		}else {
			System.out.println("Resource not found");
		}
	}
	
	public static void getActuatorStatus(String node) {
		boolean found = false;
		int i = 0;
		System.out.println(node);
		for(; i < Server.regResources.size(); i++) {
			System.out.println(Server.regResources.get(i).getName());
			if(node.equals(Server.regResources.get(i).getName())&&(Server.regResources.get(i).getPath().contains("actuators"))){
				found = true;
				break;
			}
		}
		
		if(found) {	
			RegisteredResource r = Server.regResources.get(i);
			String response = r.get(MediaTypeRegistry.APPLICATION_JSON).getResponseText();
			try {
				//System.out.println(response);
				JSONObject msg = (JSONObject)JSONValue.parseWithException(response);
				long pur = (Long)msg.get("purification_mode");
				if(pur == 1) {
					System.out.println("Actuator on " + node + " is on");
				}else {
					System.out.println("Actuator on " + node + " is off");
				}
			}catch(org.json.simple.parser.ParseException e) {
				e.printStackTrace();
			}
		}else {
			System.out.println("Resource not found");
		}
	}
	
	
	public static void setStatus(String node, String mode) {
		boolean found = false;
		int i = 0;
		for(; i < Server.regResources.size(); i++) {
			if((node.equals(Server.regResources.get(i).getName()))&&(Server.regResources.get(i).getPath().contains("actuators"))){
				found = true;
				break;
			}
		}
		
		if(found) {		
			RegisteredResource r = Server.regResources.get(i);
			CoapResponse response = r.post("mode=" + mode, MediaTypeRegistry.TEXT_PLAIN);
			String resCode = response.getCode().toString();
			if(resCode.startsWith("2")) {
				System.out.println("Acturator " + node + " setted " + mode);
			}else {
				System.out.println("Error " + resCode);
			}
		}else {
			System.out.println("Error: resource not foud");
		}
	}
	
	public static void setAll(String mode) {
		for(int i = 0; i < Server.regResources.size(); i++) {
			RegisteredResource r = Server.regResources.get(i);
			if(!r.getPath().contains("actuators")) {
				continue;
			}else {
				CoapResponse response = r.post("mode=" + mode, MediaTypeRegistry.TEXT_PLAIN);
				String resCode = response.getCode().toString();
				if(resCode.startsWith("2")) {
					System.out.println("Acturator " + r.getName() + " setted " + mode);
				}else {
					System.out.println("Error " + resCode);
				}
			}
		}
	}
	
	public static void viewHistoric(String node) {
		boolean found = false;
		int i = 0;
		for(; i < Server.regResources.size(); i++) {
			if(node.equals(Server.regResources.get(i).getName()) && (Server.regResources.get(i).getPath().contains("sensors"))){
				found = true;
				break;
			}
		}
		
		if(found) {
			RegisteredResource r = Server.regResources.get(i);
			r.obsRes.printHistory();
		}
	}

}





























