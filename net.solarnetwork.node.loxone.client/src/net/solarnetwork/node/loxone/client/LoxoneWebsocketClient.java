
package net.solarnetwork.node.loxone.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.solarnetwork.node.loxone.config.Config;

@ClientEndpoint
public class LoxoneWebsocketClient {

	private Session session = null;

	private String host = "test";
	private String username = "test";
	private String password = "test";
	
	private Config config = new Config();
	
	byte[] header;
	
	ObjectMapper objectMapper = new ObjectMapper();

	private final Logger log = LoggerFactory.getLogger(getClass());

	private synchronized void initiate() {
		javax.websocket.WebSocketContainer container = org.glassfish.tyrus.client.ClientManager
				.createClient(org.glassfish.tyrus.container.jdk.client.JdkClientContainer.class.getName());
		session = null;
		try {
			log.debug(String.format("Opening Loxone websocket connection to ws://", host));
			session = container.connectToServer(this, new URI("ws://" + host));
		} catch ( DeploymentException e ) {
			e.printStackTrace();
		} catch ( IOException e ) {
			e.printStackTrace();
		} catch ( URISyntaxException e ) {
			e.printStackTrace();
		}
	}
	
//	Send
	private void send(String message) {
		log.debug("Sending: " + message);
		
		try {
			session.getBasicRemote().sendText(message);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

//	OPEN
	@OnOpen
	public void onOpen(Session session) {
		this.session = session;
		log.debug("WebSocket opened: " + session.getId());

		send("jdev/sys/getkey");
	}

//	MESSAGE BINARY
	@OnMessage
	public void onMessage(ByteBuffer byteBuffer) {
		header = byteBuffer.array();
		log.debug(String.format("Recieved header %s, %s, %s, %s", header[0], header[1], header[2], header[3]));
	}

//	MESSAGE STRING
	@OnMessage
	public synchronized void onMessage(String message) {
		
		// LoxAPP file
		if(header[1] == 1) {
			log.debug("Recieved LoxAPP file");
			config.update(message);
			return;
		// Not a text message
		} else if(header[1] != 0) {
			return;
		}
		
		log.debug("Message recieved: " + message);
		
		JsonNode rootNode;
		
		try {
			rootNode = objectMapper.readTree(message).path("LL");
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		String control = rootNode.path("control").asText();
		String code = rootNode.path("Code").asText();
		
		if(!code.equals("200")){
			log.debug("Error: bad response(" + code + ")\n" + message);
			return;
		}
		
		log.debug("Control: " + control + ", Code: " + code);
		if (control.equals("jdev/sys/getkey")) {
			
			log.debug("Got authentication key");
			
			String key = rootNode.path("value").asText();
			key = Crypto.hexToString(key);
			
			String authString = this.username + ":" + this.password;
			String hash = Crypto.createHmacSha1Hash(authString, key);
			
			log.debug("Authenticating ...");
			
			send("authenticate/" + hash);
			
		}else if (control.contains("authenticate/")) {
			
			log.debug("Authenticated");
			log.debug("Checking LoxAPP version...");
			
			try {
				session.getBasicRemote().sendText("jdev/sps/LoxAPPversion3");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}else if (control.equals("dev/sps/LoxAPPversion3")) {
			
			String lastModified = rootNode.path("value").asText();
			
			log.debug(lastModified);
			
			String lastConfig = config.getLastModified();
			
			if(lastConfig == null || !lastConfig.equals(lastModified)) {
				log.debug("Get LoxAPP");
				send("data/LoxAPP3.json");
			} else {
				log.debug("Get updates");
				send("enablestatusupdates");
			}
			
		}
	}

//	CLOSE
	@OnClose
	public void onClose(CloseReason reason) {
		log.debug("Closing a WebSocket due to " + reason.getReasonPhrase());
	}

	public synchronized void open(String host, String username, String password) {
		// be smart about property updates: if they haven't changed, don't reset
		boolean changed = false;
		if( this.host == null || !this.host.equalsIgnoreCase(host) ) {
			changed = true;
		}else if(this.username == null || !this.username.equals(username)) {
			changed = true;
		}else if(this.password == null || !this.password.equals(password)) {
			changed = true;
		}
		if(!changed) {
			return;
		}

		close();

		this.host = host;
		this.username = username;
		this.password = password;
		
		log.debug("Opening connection with values: " + this.host + ", " + this.username + ", " + this.password);

		initiate();
	}

	public synchronized void close() {
		log.debug("Requested close connection");
		if ( session != null && session.isOpen() == true) {
			try {
				session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "All done."));
			} catch ( IOException e ) {
				log.debug("IOException closing websocket Session", e);
			} finally {
				session = null;
			}
		} else {
			session = null;
		}
		log.debug("Connection closed");
	}
}
