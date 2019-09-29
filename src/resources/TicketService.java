package resources;

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
	
	@Override
    protected GuacamoleTunnel doConnect(HttpServletRequest request) throws GuacamoleException {


		String port = request.getHeader("port");
		String protocol = request.getHeader("protocol");

		if (!allowed_protocols.contains(protocol)) {
	        return null;
	    }

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