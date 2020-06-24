package IoT_Project;

import java.util.ArrayList;

import org.eclipse.californium.core.CaliforniumLogger;
import org.eclipse.californium.core.CoapServer;

public class Server extends CoapServer {
	static {
		CaliforniumLogger.disableLogging();
	}
	
	static public ArrayList<RegisteredResource> regResources = new ArrayList<RegisteredResource>();
	static private Registrant reg;
	static public int room=0;
	
	public Server(int p) {
		super(p);
		reg = new Registrant("registrant");
		this.add(reg);
	}
	
	
	
	
	
	
	
	
	
}
