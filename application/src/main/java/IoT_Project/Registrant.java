package IoT_Project;

import java.net.InetAddress;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class Registrant extends CoapResource {

	public Registrant(String name) {
		super(name);
	}
	
	public void handleGET(CoapExchange exchange) {
		exchange.accept();
		InetAddress address = exchange.getSourceAddress();
		boolean obs;
		String[] path;
		String title;
		//System.out.println("Registering " + address);
		
		/*Retrieve all the resource of the node*/
		CoapClient req = new CoapClient("coap://["+address.getHostAddress()+"]:5683/.well-known/core");
		String response = req.get().getResponseText();
		
		//System.out.println(response);
		String[] resources = response.split("\n");
		for(int i = 0; i < resources.length; i++) {
			
			String[] parameters = resources[i].split(";");
			if(resources[i].contains("</.well-known/core>;")){
				path = parameters[1].split(",");
				title = parameters[2];
				if(parameters.length == 6) {
					obs = true;
				}else {
					obs = false;
				}
			}else {
				path = parameters[0].split(",");
				title = parameters[1];
				if(parameters.length == 5) {
					obs = true;
				}else {
					obs = false;
				}
			}
			RegisteredResource newOne = new RegisteredResource(path[1].replace("<", "").replace(">", ""), title, address.toString().replace("/",""), obs );
			if(!isPresent(newOne)) {
				Server.regResources.add(newOne);
				//System.out.println("New resource " + newOne.getTitle() + " registered");
			}
		}	
	}
	
	public boolean isPresent(RegisteredResource res) {
		for(int j = 0; j < Server.regResources.size(); j++) {
			if(res.getAddr().equals(Server.regResources.get(j).getAddr()) && res.getPath().equals(Server.regResources.get(j).getPath()))
				return true;
		}
		return false;
		
	}
	
	
	
	
	
	
	
	
}
