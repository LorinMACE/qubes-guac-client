package resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.apache.guacamole.servlet.GuacamoleHTTPTunnelServlet;

public class TicketService extends GuacamoleHTTPTunnelServlet {

	private static final long serialVersionUID = 3493778309036478051L;
	List<String> allowed_protocols = Arrays.asList("ssh", "vnc");
	
	public String executeCommand(String command) {
		Process p;
		String s = null;
		String result = "";
		try {
			p = Runtime.getRuntime().exec(command);

			
	        BufferedReader stdInput = new BufferedReader(new 
	             InputStreamReader(p.getInputStream()));
	
	        BufferedReader stdError = new BufferedReader(new 
	             InputStreamReader(p.getErrorStream()));
	
	        while ((s = stdInput.readLine()) != null) {
	        	result += s;
	        }
	        
	        while ((s = stdError.readLine()) != null) {
	        	result += s;
	        }
	       
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public String preparePort (String vmame, String service) {
		String command = String.format("/home/user/QubesIncomming/work-lade/remote-session.py open %s %s", "vmname", "service");
		executeCommand(command);
        
		command = String.format("/home/user/QubesIncomming/work-lade/remote-session.py get %s %s", "vmname", "service");
		return executeCommand(command);
	}
	
	@Override
    protected GuacamoleTunnel doConnect(HttpServletRequest request) throws GuacamoleException {


		String vm = request.getHeader("vm");
		String protocol = request.getHeader("protocol");

		if (!allowed_protocols.contains(protocol)) {
	        return null;
	    }
		String port = preparePort(vm, protocol);

        GuacamoleConfiguration config = new GuacamoleConfiguration();
        config.setProtocol(protocol);
        config.setParameter("hostname", "localhost");
        config.setParameter("port", port);
        config.setParameter("username", "user");
        config.setParameter("password", "user");
        config.setParameter("enable-sftp", "true");
        
        // Connect to guacd, proxying a connection to the VNC server above
        GuacamoleSocket socket = new ConfiguredGuacamoleSocket(
                new InetGuacamoleSocket("localhost", 4822),
                config
        );

        // Create tunnel from now-configured socket
        GuacamoleTunnel tunnel = new SimpleGuacamoleTunnel(socket);
        return tunnel;

    }

}