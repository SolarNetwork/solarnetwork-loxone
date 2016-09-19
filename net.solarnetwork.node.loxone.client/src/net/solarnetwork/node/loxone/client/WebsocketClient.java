package net.solarnetwork.node.loxone.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

@ClientEndpoint
public class WebsocketClient implements Runnable, BundleActivator {

	private Session session = null;

	public void go() {
		System.out.println("Starting client thread...");
		Thread t = new Thread(this);
		t.setContextClassLoader(Thread.currentThread().getContextClassLoader());
		t.start();
		try {
			System.out.println("Waiting for client thread to finish...");
			t.join();
		} catch ( InterruptedException e ) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		javax.websocket.WebSocketContainer container = org.glassfish.tyrus.client.ClientManager.createClient(
	      org.glassfish.tyrus.container.jdk.client.JdkClientContainer.class.getName());
		session = null;
		try {
			System.out.println("Opening websocket connection...");
			session = container.connectToServer(getClass(), new URI("ws://10.1.1.30:3000"));
			System.out.println("Sending greeting...");
			session.getBasicRemote().sendText("Hello, foobar.");
			synchronized ( session ) {
				Boolean done = (Boolean) session.getUserProperties().get("done");
				while ( done == null || !done.booleanValue() ) {
					System.out.println("Waiting for response...");
					session.wait(4000);
					done = (Boolean) session.getUserProperties().get("done");
				}
			}
		} catch ( DeploymentException e ) {
			e.printStackTrace();
		} catch ( IOException e ) {
			e.printStackTrace();
		} catch ( URISyntaxException e ) {
			e.printStackTrace();
		} catch ( InterruptedException e ) {
			e.printStackTrace();
		} finally {
			if ( session != null ) {
				try {
					System.out.println("Closing websocket connection...");
					session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "All done."));
				} catch ( IOException e ) {
					// ignore this
				}
			}
		}
	}

	@OnOpen
	public void myOnOpen(Session session) {
		this.session = session;
		System.out.println("WebSocket opened: " + session.getId());
	}

	@OnMessage
	public void myOnMessage(String txt) {
		System.out.println("WebSocket received message: " + txt);
		synchronized ( session ) {
			session.getUserProperties().put("done", Boolean.TRUE);
			session.notifyAll();
		}
	}

	@OnClose
	public void myOnClose(CloseReason reason) {
		System.out.println("Closing a WebSocket due to " + reason.getReasonPhrase());
	}

	public static final void main(String[] args) {
		WebsocketClient client = new WebsocketClient();
		client.go();
	}

	@Override
	public void start(BundleContext context) throws Exception {
		WebsocketClient client = new WebsocketClient();
		client.go();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// nothing
	}

}


