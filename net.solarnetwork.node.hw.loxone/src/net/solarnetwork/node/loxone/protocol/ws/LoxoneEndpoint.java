/* ==================================================================
 * LoxoneEndpoint.java - 17/09/2016 7:40:39 AM
 * 
 * Copyright 2007-2016 SolarNetwork.net Dev Team
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 * 02111-1307 USA
 * ==================================================================
 */

package net.solarnetwork.node.loxone.protocol.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.HmacUtils;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.container.jdk.client.JdkClientContainer;
import org.joda.time.DateTime;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.FileCopyUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.solarnetwork.node.loxone.dao.ConfigAuthenticationTokenDao;
import net.solarnetwork.node.loxone.dao.ConfigDao;
import net.solarnetwork.node.loxone.domain.AuthenticationKey;
import net.solarnetwork.node.loxone.domain.AuthenticationToken;
import net.solarnetwork.node.loxone.domain.AuthenticationTokenPermission;
import net.solarnetwork.node.loxone.domain.Config;
import net.solarnetwork.node.loxone.domain.ConfigApi;
import net.solarnetwork.node.loxone.domain.ConfigAuthenticationToken;
import net.solarnetwork.node.loxone.protocol.ws.handler.BaseCommandHandler;
import net.solarnetwork.node.loxone.util.SecurityUtils;
import net.solarnetwork.util.JsonUtils;
import net.solarnetwork.util.OptionalService;

/**
 * Endpoint for the Loxone miniserver websocket API.
 * 
 * The Loxone server responds to all websocket message requests with a pair of
 * response messages: first a binary "message header" message and then another
 * binary or text message with the actual payload of the response. This class
 * acts as a broker for the messages, and handles the authentication of the
 * connection. It then relies on {@link CommandHandler} instances to deal with
 * all other messages, configured via {@link #setCommandHandlers(List)}.
 * 
 * This class also relies on a list of {@link BinaryFileHandler} instances to
 * deal with the {@code BinaryFile} message type, configured via
 * {@link #setBinaryFileHandlers(List)}.
 * 
 * This class listens for the
 * {@code LoxoneEvents#STRUCTURE_FILE_MODIFICATION_DATE_EVENT} event and if that
 * date changes will request the structure file again from the Loxone server.
 * 
 * @author matt
 * @version 1.8
 */
public class LoxoneEndpoint extends Endpoint implements MessageHandler.Whole<ByteBuffer>, EventHandler {

	private static final String WEBSOCKET_CONNECT_PATH = "/ws/rfc6455";

	/**
	 * A session user property key that must provide the {@link Config} ID value
	 * to use.
	 */
	public static final String CONFIG_ID_USER_PROPERTY = "config-id";

	/**
	 * A session user property key that can provide the {@link SecurityHelper}
	 * ID value to use.
	 * 
	 * @since 1.8
	 */
	public static final String SECURITY_HELPER_USER_PROPERTY = "security-helper";

	/**
	 * An internal CloseCode for a user-initiated disconnection, where a
	 * connection retry should not be attempted.
	 */
	public static final CloseReason.CloseCode DISCONNECT_USER_INITIATED = CloseReason.CloseCodes
			.getCloseCode(3000);

	private static final Set<CommandType> INTERNAL_CMDS = EnumSet.of(CommandType.GetAuthenticationKey,
			CommandType.Authenticate, CommandType.Auth, CommandType.EnableInputStatusUpdate,
			CommandType.KeepAlive, CommandType.KeyExchange, CommandType.EncryptedCommand,
			CommandType.GetTokenKey, CommandType.GetToken, CommandType.RefreshToken,
			CommandType.AuthenticateWithToken);

	private static enum AuthKeyState {

		None(0),

		TokenRefresh(1),

		TokenAuthenticate(2);

		private final int key;

		AuthKeyState(int key) {
			this.key = key;
		}

		public int getKey() {
			return key;
		}

		public static AuthKeyState forKey(int key) {
			switch (key) {
				case 1:
					return TokenRefresh;

				case 2:
					return TokenAuthenticate;

				default:
					return None;
			}
		}
	}

	private String host = "10.0.0.1:3000";
	private String username = null;
	private String password = null;
	private String configKey = null;

	private ObjectMapper objectMapper = new ObjectMapper();
	private CommandHandler[] commandHandlers = null;
	private BinaryFileHandler[] binaryFileHandlers = null;
	private OptionalService<EventAdmin> eventAdmin = null;
	private final int keepAliveSeconds = 240;
	private TaskScheduler taskScheduler;
	private ConfigDao configDao;
	private ConfigAuthenticationTokenDao configAuthTokenDao;
	private int statusMessageCount = 500;
	private boolean authenticationFailure = false;
	private Map<String, Object> clientProperties = null;
	private AuthenticationType authenticationType = AuthenticationType.Auto;
	private AuthenticationTokenPermission tokenRequestPermission = AuthenticationTokenPermission.Web;
	private int tokenRefreshOffsetHours = 12;

	/** A class-level logger. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	// create a fixed size queue for handling message headers
	private final Queue<MessageHeader> headerQueue = new ArrayBlockingQueue<>(1, true);

	// create an internal handler for authentication, etc.
	private final InternalCommandHandler internalCommandHandler = new InternalCommandHandler();

	private final ReconnectHandler reconnectHandler = new ReconnectHandler();

	private Session session;
	private ScheduledFuture<?> keepAliveFuture = null;
	private ScheduledFuture<?> connectFuture = null;
	private ScheduledFuture<?> tokenRefreshFuture = null;
	private Config configuration = null;
	private ConfigApi apiConfiguration = null;
	private long messageCount = 0;
	private ConfigAuthenticationToken configAuthToken = null;

	private final AtomicInteger authKeyState = new AtomicInteger(0);

	private synchronized void connect() {
		ClientManager container = ClientManager.createClient(JdkClientContainer.class.getName());
		if ( clientProperties != null ) {
			container.getProperties().putAll(clientProperties);
		}
		container.getProperties().put(ClientProperties.RECONNECT_HANDLER, reconnectHandler);
		ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
				.preferredSubprotocols(Arrays.asList("remotecontrol")).build();
		apiConfiguration = null;
		session = null;
		connectFuture = null;
		ConfigApi configApi;
		try {
			configApi = getConfigApi();
		} catch ( Exception e ) {
			logConciseException("Error establishing websocket connection to {}", e, host);
			reconnectHandler.onConnectFailure(e);
			return;
		}

		// load any saved token if DAO available
		configAuthToken = null;
		if ( configAuthTokenDao != null ) {
			Long configId = configuredConfigId();
			ConfigAuthenticationToken authToken = configAuthTokenDao
					.getConfigAuthenticationToken(configId);
			if ( authToken != null ) {
				configAuthToken = authToken;
			}
		}

		try {
			log.debug("Opening Loxone websocket connection to {} (API version {})",
					configApi.getWebsocketUri(), configApi.getVersion());
			apiConfiguration = configApi;
			session = container.connectToServer(this, config, configApi.getWebsocketUri());
		} catch ( Exception e ) {
			logConciseException("Error connecting to {}", e, configApi.getWebsocketUri());
		}
	}

	/**
	 * Get the URI to connect to a Loxone websocket, with support for Loxone's
	 * CloudDNS style redirects.
	 * 
	 * @return The API configuration to use.
	 * @throws IOException
	 *         If a problem occurs connecting to the host.
	 * @throws URISyntaxException
	 *         If a problem occurs parsing the host string.
	 */
	private ConfigApi getConfigApi() throws IOException, URISyntaxException {
		return getConfigApiForHost(this.host);
	}

	private ConfigApi getConfigApiForHost(String host) throws IOException, URISyntaxException {
		log.debug("Testing Loxone connection to {}", host);
		URL connUrl = new URL("http://" + host + "/jdev/cfg/api");
		HttpURLConnection conn = (HttpURLConnection) connUrl.openConnection();
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(30000);
		conn.setReadTimeout(10000);
		conn.setUseCaches(false);
		conn.setInstanceFollowRedirects(false);
		conn.setDoInput(true);

		switch (conn.getResponseCode()) {
			case 301:
			case 302:
			case 303:
			case 307:
				String loc = conn.getHeaderField("Location");
				if ( loc != null ) {
					URL locURL = new URL(loc);
					return getConfigApiForHost(locURL.getHost() + ":" + locURL.getPort());
				}
				break;
		}
		URI uri = new URI(
				"ws://" + host + (host.contains(WEBSOCKET_CONNECT_PATH) ? "" : WEBSOCKET_CONNECT_PATH));
		String resp = FileCopyUtils.copyToString(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		log.debug("Got API configuration response: {}", resp);
		String respValue = extractJsonResponseValue(resp);
		Map<String, Object> infoMap = Collections.emptyMap();
		if ( respValue != null ) {
			// the "value" property is not actually JSON; it has ' instead of " so we can do a quick replace here
			infoMap = JsonUtils.getStringMap(respValue.replace('\'', '"'));
		}

		// now get public key
		URL pubKeyUrl = new URL("http://" + host + "/jdev/sys/getPublicKey");
		conn = (HttpURLConnection) pubKeyUrl.openConnection();
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(30000);
		conn.setReadTimeout(10000);
		conn.setUseCaches(false);
		conn.setInstanceFollowRedirects(false);
		conn.setDoInput(true);

		if ( conn.getResponseCode() != 200 ) {
			throw new IOException("Public key request [" + pubKeyUrl + "] returned non-success result: "
					+ conn.getResponseCode());
		}

		resp = FileCopyUtils.copyToString(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		log.debug("Got public key response: {}", resp);
		respValue = extractJsonResponseValue(resp);
		PublicKey publicKey = null;
		if ( respValue != null ) {
			publicKey = SecurityUtils.parsePublicKey(respValue);
			log.debug("Got Loxone {} public key: {}", host, publicKey);
		}

		return new ConfigApi(uri, publicKey,
				infoMap.get("snr") instanceof String ? (String) infoMap.get("snr") : null,
				infoMap.get("version") instanceof String ? (String) infoMap.get("version") : null);
	}

	private String extractJsonResponseValue(String resp) {
		Map<String, Object> responseMap = JsonUtils.getStringMap(resp);
		if ( responseMap.get("LL") instanceof Map ) {
			@SuppressWarnings("unchecked")
			Map<String, Object> llMap = (Map<String, Object>) responseMap.get("LL");
			if ( llMap.get("value") instanceof String ) {
				return (String) llMap.get("value");
			}
		}
		return null;
	}

	/**
	 * Initialize after all properties are configured.
	 */
	public void init() {
		// subslasses might do something
	}

	/**
	 * Get the configuration for this service.
	 * 
	 * @return The configuration.
	 */
	public Config getConfiguration() {
		return configuration;
	}

	private void setConfiguration(Config config) {
		if ( config == configuration ) {
			return;
		}
		boolean newId = false;
		if ( configuration == null && config != null && config.getId() != null ) {
			newId = true;
		} else if ( configuration != null && ((configuration.getId() == null && config.getId() != null)
				|| !configuration.getId().equals(config.getId())) ) {
			newId = true;
		}
		configuration = config;
		if ( newId ) {
			Config changed = configurationIdDidChange();
			if ( changed != null ) {
				configuration = changed;
			}
		}
	}

	/**
	 * Get the API configuration of this service.
	 * 
	 * @return the configuration, or {@literal null} if not known
	 */
	public ConfigApi getApiConfiguration() {
		return apiConfiguration;
	}

	/**
	 * Called when the configuration at {@link #getConfiguration()} ID has
	 * changed.
	 * 
	 * @return a new {@link Config} if it should be changed in some way,
	 *         {@code null} otherwise
	 */
	protected Config configurationIdDidChange() {
		// subclasses can do something interesting here
		return null;
	}

	/**
	 * Disconnect from the Loxone server.
	 */
	public synchronized void disconnect() {
		if ( session != null && session.isOpen() == true ) {
			try {
				session.close(new CloseReason(DISCONNECT_USER_INITIATED, "All done."));
				log.debug("Connection closed");
			} catch ( IOException e ) {
				log.debug("IOException closing websocket Session", e);
			} finally {
				session = null;
			}
		} else {
			session = null;
		}
	}

	private synchronized void connectionDetailsChanged() {
		if ( host == null || username == null || password == null || host.isEmpty() || username.isEmpty()
				|| password.isEmpty() ) {
			return;
		}

		if ( connectFuture != null && !connectFuture.isDone() ) {
			connectFuture.cancel(true);
			connectFuture = null;
		}
		disconnect();
		if ( taskScheduler == null ) {
			return;
		}
		if ( authenticationFailure ) {
			log.warn(
					"Will not reconnect to {} after authenticaiton failure; update the username/password settings.",
					host);
			return;
		}
		connectFuture = taskScheduler.schedule(new Runnable() {

			@Override
			public void run() {
				connect();
			}
		}, new Date(
				System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(reconnectHandler.getDelay())));
	}

	@Override
	public void onMessage(ByteBuffer buf) {
		if ( buf.order() != ByteOrder.LITTLE_ENDIAN ) {
			buf.order(ByteOrder.LITTLE_ENDIAN);
		}
		MessageHeader h = headerQueue.poll();
		if ( h != null ) {
			log.trace("Got binary message {}", h);
			if ( log.isTraceEnabled() ) {
				try {
					log.trace("Binary message {} payload (Base64):\n{}", h, new String(
							Base64.getMimeEncoder().encode(buf.duplicate()).array(), "UTF-8"));
				} catch ( UnsupportedEncodingException e ) {
					// ignore
				}
			}
			if ( !handleBinaryFileIfPossible(h, buf) ) {
				// hmm, seems sometimes we get more than one response from Loxone
				log.debug("Dropping message: {}", h);
			}
		} else {
			// this should be a message header message, and another message will follow 
			// from the Loxone that logically refers to this header
			try {
				MessageHeader header = new MessageHeader(buf);
				log.trace("Got message header {}", header);

				incrementMessageCount();

				// skip keep-alive headers, there is no follow on message
				if ( MessageType.Keepalive.equals(header.getType()) ) {
					log.info("Received keepalive message from Loxone "
							+ configuredConfigIdExternalForm());
				} else if ( !headerQueue.offer(header) ) {
					log.warn("Dropping message header: {}", header);
				}
			} catch ( IllegalArgumentException e ) {
				log.warn("Dropping unsupported message header: {}", e.getMessage());
			}
		}
	}

	private void incrementMessageCount() {
		messageCount += 1;
		if ( messageCount % statusMessageCount == 0 ) {
			log.info("Loxone {} processed {} messages", configuredConfigIdExternalForm(), messageCount);
		}
	}

	@Override
	public void onClose(Session session, CloseReason closeReason) {
		super.onClose(session, closeReason);
		log.debug("Session closed: {}", closeReason);
		stopKeepAliveTask();
		stopTokenRefreshTask();
		this.session = null;
		this.configAuthToken = null;
	}

	@Override
	public void onError(Session session, Throwable throwable) {
		super.onError(session, throwable);
		logConciseException("Unknown websocket error", throwable);
	}

	private Long configIdFromBytes(byte[] bytes) {
		if ( bytes == null || bytes.length < 1 ) {
			return null;
		}
		if ( bytes.length > 8 ) {
			byte[] truncated = new byte[8];
			System.arraycopy(bytes, 0, truncated, 0, 8);
			bytes = truncated;
		}
		return Long.parseUnsignedLong(Hex.encodeHexString(bytes), 16);
	}

	private Long configuredConfigId() {
		// setup our Config based on an ID derived from the host value
		Long configId = null;
		if ( configKey != null && configKey.length() > 0 ) {
			try {
				configId = configIdFromBytes(configKey.getBytes("UTF-8"));
			} catch ( UnsupportedEncodingException e ) {
				log.warn("Error getting UTF-8 string from configKey [{}]", configKey);
			}
		}
		if ( configId == null ) {
			configId = configIdFromBytes(DigestUtils.sha1(host != null ? host : ""));
		}
		return configId;
	}

	private String configuredConfigIdExternalForm() {
		Long configId = configuredConfigId();
		return Config.idToExternalForm(configId);
	}

	@Override
	public void onOpen(Session session, EndpointConfig config) {
		this.session = session;

		Long configId = configuredConfigId();
		session.getUserProperties().put(CONFIG_ID_USER_PROPERTY, configId);

		if ( authenticationType == AuthenticationType.Token
				|| (authenticationType == AuthenticationType.Auto && apiConfiguration != null
						&& apiConfiguration.isVersionAtLeast(9)) ) {
			TokenSecurityHelper securityHelper = new TokenSecurityHelper(apiConfiguration);
			session.getUserProperties().put(SECURITY_HELPER_USER_PROPERTY, securityHelper);

			// the connect() method will have populated a saved auth token if available, so apply that now
			securityHelper.setAuthenticationToken(configAuthToken);
			this.configAuthToken = null;
		}

		Config cfg = configDao.getConfig(configId);
		if ( cfg == null ) {
			cfg = new Config(configId);
			configDao.storeConfig(cfg);
		}

		setConfiguration(cfg);

		// add binary handler to decode message headers and other binary messages
		session.addMessageHandler(this);

		// add a streaming text handler to support very large responses (such as structure file)
		session.addMessageHandler(new TextMessageHandler());

		reconnectHandler.connected();

		// authenticate
		try {
			log.info("Connected to Loxone server, will authenticate now.");
			internalCommandHandler.authenticate();
		} catch ( IOException e ) {
			log.error("Communication error authenticating: {}", e.getMessage());
		}
	}

	/**
	 * Command handler to deal with authentication and internal bookeeping
	 * tasks.
	 */
	private class InternalCommandHandler extends BaseCommandHandler {

		// Use the logger from LoxoneEndpoint directly
		private final Logger log = LoxoneEndpoint.this.log;

		private void authenticate() throws IOException {
			SecurityHelper securityHelper = getSecurityHelper(session);
			if ( securityHelper != null ) {
				// exchange keys for token auth
				sendCommand(CommandType.KeyExchange, session, securityHelper.generateSessionKey());
			} else {
				sendCommand(CommandType.GetAuthenticationKey, session);
			}
		}

		@Override
		public boolean supportsCommand(CommandType command) {
			return INTERNAL_CMDS.contains(command);
		}

		@Override
		public boolean handleCommand(CommandType command, MessageHeader header, Session session,
				JsonNode tree) throws IOException {
			// look specifically for authentication failure, to prevent re-trying to connect
			if ( command == CommandType.Authenticate || command == CommandType.AuthenticateWithToken
					|| command == CommandType.GetToken || command == CommandType.RefreshToken ) {
				int status = extractResponseCode(tree);
				if ( status >= 400 && status < 500 ) {
					log.warn("Loxone authentication failure to {}: wrong username/password?", host);
					authenticationFailure = true;
					return false;
				}
			}
			return super.handleCommand(command, header, session, tree);
		}

		private void handlePostAuthenticationTasks(CommandType command, Session session)
				throws IOException {
			// immediately after authentication, check for last modification date of structure file
			sendCommandIfPossible(CommandType.StructureFileLastModifiedDate);

			// also schedule a keepalive message
			scheduleKeepAliveTask();

			if ( command == CommandType.AuthenticateWithToken || command == CommandType.GetToken ) {
				SecurityHelper helper = getSecurityHelper(session);
				AuthenticationToken token = (helper != null ? helper.getAuthenticationToken() : null);
				if ( token != null ) {
					scheduleTokenRefreshTask(token);
				}
			}
		}

		@Override
		public boolean handleCommandValue(CommandType command, MessageHeader header, Session session,
				JsonNode json, String value) throws IOException {
			if ( log.isTraceEnabled() ) {
				log.trace("Handling message {} command {} data: {}", header, command, value);
			} else {
				log.debug("Handling message {} command {}", header, command);
			}
			if ( command == CommandType.GetAuthenticationKey ) {
				byte[] key;
				try {
					key = Hex.decodeHex(value.toCharArray());
				} catch ( DecoderException e ) {
					throw new RuntimeException(e);
				}
				AuthKeyState authState = AuthKeyState
						.forKey(authKeyState.getAndSet(AuthKeyState.None.getKey()));
				if ( authState != AuthKeyState.None ) {
					SecurityHelper helper = getSecurityHelper(session);
					AuthenticationToken token = (helper != null ? helper.getAuthenticationToken()
							: null);
					if ( token != null ) {
						if ( authState == AuthKeyState.TokenRefresh ) {
							sendCommand(CommandType.RefreshToken, session, token.hashToken(key),
									getUsername());
						} else {
							sendCommand(CommandType.AuthenticateWithToken, session, token.hashToken(key),
									getUsername());
						}
					} else {
						log.warn("{} token not available, cannot {}", configuredConfigIdExternalForm(),
								authState);
					}
				} else {
					String authString = getUsername() + ":" + getPassword();
					if ( log.isDebugEnabled() ) {
						log.debug("{} authenticating using key {} and user {}",
								configuredConfigIdExternalForm(), new String(key, "UTF-8"),
								getUsername());
					}
					sendCommand(CommandType.Authenticate, session,
							HmacUtils.hmacSha1Hex(key, authString.getBytes("UTF-8")));
				}
				return true;
			} else if ( command == CommandType.Authenticate
					|| command == CommandType.AuthenticateWithToken ) {
				handlePostAuthenticationTasks(command, session);
				return true;
			} else if ( command == CommandType.Auth ) {
				return true;
			} else if ( command == CommandType.KeyExchange ) {
				// acquire a token if we don't have one, otherwise authenticate with token
				SecurityHelper helper = getSecurityHelper(session);
				if ( helper != null ) {
					helper.keyExchangeComplete();
					AuthenticationToken token = helper.getAuthenticationToken();
					if ( token != null && !token.isExpired() ) {
						authKeyState.set(AuthKeyState.TokenAuthenticate.getKey());
						// must get authentication key to use token with
						sendCommand(CommandType.GetAuthenticationKey, session);
					} else {
						sendCommand(CommandType.GetTokenKey, session, getUsername());
					}
				} else {
					log.warn("{} SecurityHelper not available, cannot request token authentication key",
							configuredConfigIdExternalForm());
				}
				return true;
			} else if ( command == CommandType.GetTokenKey ) {
				SecurityHelper helper = getSecurityHelper(session);
				if ( helper != null ) {
					// "value" is actual JSON object here
					Map<String, Object> data = JsonUtils.getStringMapFromTree(json.path("value"));
					AuthenticationKey key = helper.extractAuthenticationKey(data);
					if ( key != null ) {
						log.debug("{} requesting {} token using key {} and user {}",
								configuredConfigIdExternalForm(), tokenRequestPermission, key.getKey(),
								getUsername());
						Config cfg = (configuration != null ? configuration
								: new Config(null, null, UUID.randomUUID()));
						sendCommand(CommandType.GetToken, session,
								key.hash(getUsername(), getPassword()), getUsername(),
								tokenRequestPermission.getCode(), cfg.getClientUuidString(),
								"SolarNode");
					} else {
						log.warn("{} authentication key cannot be determined from {}",
								configuredConfigIdExternalForm(), data);
					}
				} else {
					log.warn("{} SecurityHelper not available, cannot get authentication token",
							configuredConfigIdExternalForm());
				}
				return true;
			} else if ( command == CommandType.GetToken ) {
				SecurityHelper helper = getSecurityHelper(session);
				if ( helper != null ) {
					// "value" is actual JSON object here
					Map<String, Object> data = JsonUtils.getStringMapFromTree(json.path("value"));
					AuthenticationToken token = helper.extractTokenValue(data);
					if ( token != null ) {
						log.info("Got autentication token {} for Loxone {}, valid until {}",
								token.getToken(), configuredConfigIdExternalForm(),
								token.getValidUntil());
						if ( configAuthTokenDao != null ) {
							configAuthTokenDao.storeConfigAuthenticationToken(
									new ConfigAuthenticationToken(configuration.getId(), token));
						}
						handlePostAuthenticationTasks(command, session);
					}
				} else {
					log.warn("{} SecurityHelper not available, cannot save authentication token",
							configuredConfigIdExternalForm());
				}
				return true;
			} else if ( command == CommandType.RefreshToken ) {
				SecurityHelper helper = getSecurityHelper(session);
				if ( helper != null ) {
					// "value" is actual JSON object here
					Map<String, Object> data = JsonUtils.getStringMapFromTree(json.path("value"));
					AuthenticationToken token = helper.extractTokenRefreshValue(data);
					if ( token != null ) {
						log.info("Got refreshed autentication token {} for Loxone {}, valid until {}",
								token.getToken(), configuredConfigIdExternalForm(),
								token.getValidUntil());
						if ( configAuthTokenDao != null ) {
							configAuthTokenDao.storeConfigAuthenticationToken(
									new ConfigAuthenticationToken(configuration.getId(), token));
						}
						scheduleTokenRefreshTask(token);
						return true;
					}
				} else {
					log.warn("{} SecurityHelper not available, cannot save authentication token",
							configuredConfigIdExternalForm());
				}
			} else if ( command == CommandType.KeepAlive ) {
				log.debug("Keepalive response received");
				return true;
			}
			return false;
		}

	}

	private synchronized void scheduleKeepAliveTask() {
		if ( keepAliveFuture == null || keepAliveFuture.isDone() && taskScheduler != null ) {
			long period = TimeUnit.SECONDS.toMillis(keepAliveSeconds);
			keepAliveFuture = taskScheduler.scheduleAtFixedRate(new KeepAliveTask(),
					new Date(System.currentTimeMillis() + period), period);
		}
	}

	private synchronized void stopKeepAliveTask() {
		if ( keepAliveFuture != null ) {
			keepAliveFuture.cancel(true);
		}
	}

	private synchronized void scheduleTokenRefreshTask(AuthenticationToken token) {
		stopTokenRefreshTask();
		if ( token == null || token.getValidUntil() == null ) {
			return;
		}
		DateTime expires = token.getValidUntil();
		long expiresAt = expires.getMillis();
		long now = System.currentTimeMillis();
		Date runTime;
		if ( expiresAt <= now ) {
			// token already expired; schedule refresh in a few seconds
			runTime = new Date(System.currentTimeMillis() + 10000L);
		} else {
			long runAt = expiresAt;
			long offset = TimeUnit.HOURS.toMillis(tokenRefreshOffsetHours);
			while ( runAt - offset < now ) {
				offset /= 2;
			}
			runAt -= offset;
			runTime = new Date(runAt);
		}
		log.info("Scheduling {} token refresh for {}", configuredConfigIdExternalForm(), runTime);
		tokenRefreshFuture = taskScheduler.schedule(new TokenRefreshTask(), runTime);
	}

	private synchronized void stopTokenRefreshTask() {
		if ( tokenRefreshFuture != null ) {
			tokenRefreshFuture.cancel(true);
			tokenRefreshFuture = null;
		}
	}

	private class KeepAliveTask implements Runnable {

		@Override
		public void run() {
			try {
				sendCommandIfPossible(CommandType.KeepAlive);
			} catch ( IOException e ) {
				logConciseException("Error sending keepalive message to Loxone server", e);
			}
		}

	}

	private class TokenRefreshTask implements Runnable {

		@Override
		public void run() {
			if ( session == null || configuration == null ) {
				return;
			}
			try {
				if ( !authKeyState.compareAndSet(AuthKeyState.None.getKey(),
						AuthKeyState.TokenRefresh.getKey()) ) {
					return;
				}
				sendCommandIfPossible(CommandType.GetAuthenticationKey);
			} catch ( Exception e ) {
				authKeyState.compareAndSet(AuthKeyState.TokenRefresh.getKey(),
						AuthKeyState.None.getKey());
				logConciseException("Error refreshing Loxone {} token", e,
						configuredConfigIdExternalForm());
			} finally {
			}
		}

	}

	// sometimes we don't seem to get a header in a response message, but we can see this
	// is a control response still
	private static final Pattern LL_JSON_PAT = Pattern.compile("^\\s*\\{\\s*\"LL\"\\s*:");

	/**
	 * Text message handler that acts as a broker for {@link CommandHandler}
	 * instances to process messages.
	 */
	private class TextMessageHandler implements javax.websocket.MessageHandler.Whole<String> {

		@Override
		public void onMessage(String payload) {
			// take our corresponding message header
			final MessageHeader header = headerQueue.poll();
			if ( header == null ) {
				// we expect to have that header, but we should also be able to continue 
				// without it so just log a message
				log.debug("MessageHeader not available for text message!");
			}

			log.debug("Got text message {}: {}", header, payload);

			if ( (header != null && header.getType() == MessageType.TextMessage)
					|| (header == null && payload != null && LL_JSON_PAT.matcher(payload).find()) ) {
				// start inspecting the message to know what to do
				try {
					JsonNode json = getObjectMapper().readTree(payload);
					if ( json.hasNonNull("LL") ) {
						JsonNode root = json.path("LL");
						String control = root.path("control").textValue();
						CommandType command = CommandType.forControlValue(control);

						if ( command == CommandType.EncryptedCommand ) {
							SecurityHelper helper = (SecurityHelper) session.getUserProperties()
									.get(LoxoneEndpoint.SECURITY_HELPER_USER_PROPERTY);
							if ( helper != null ) {
								control = helper.decryptCommand(control);
								command = CommandType.forControlValue(control);
								log.debug("Decrypted command {} message: {}: {}", command, control,
										root.path("value").toString());
							}
						}

						handleCommandIfPossible(command, header, root);
					} else {
						log.debug("Unknown command message {}: {}", header, payload);
					}
				} catch ( IOException e ) {
					logConciseException("Error parsing text command {}", e, header);
				}
			} else if ( header == null || header.getType() == MessageType.BinaryFile
					|| header.getType() == MessageType.Unknown ) {
				try (Reader reader = new StringReader(payload)) {
					handleBinaryFileIfPossible(header, reader);
				} catch ( IOException e ) {
					logConciseException("Error parsing text file {}", e, header);
				}
			}
		}
	}

	private class ReconnectHandler extends org.glassfish.tyrus.client.ClientManager.ReconnectHandler {

		private int counter = 0;

		private void connected() {
			counter = 0;
		}

		@Override
		public boolean onDisconnect(CloseReason closeReason) {
			if ( DISCONNECT_USER_INITIATED.equals(closeReason.getCloseCode()) ) {
				counter = 0;
				return false;
			}
			log.warn("Loxone {} disconnected ({}), will attempt to reconnect...", configuration,
					closeReason);
			counter++;
			connectionDetailsChanged();
			return false;
		}

		@Override
		public boolean onConnectFailure(Exception exception) {
			counter++;
			log.warn("Loxone {} connect failure {} ({}), will try reconnecting in {}s", host, counter,
					exception.getMessage(), getDelay());
			connectionDetailsChanged();
			return false;
		}

		@Override
		public long getDelay() {
			return (super.getDelay() * (counter < 1 ? 1 : counter));
		}

	}

	/**
	 * Attempt to send a command to the Loxone device using a configured
	 * {@link CommandHandler}.
	 * 
	 * @param command
	 *        The command to send.
	 * @param args
	 *        Optional arguments to send with the command
	 * @return <em>true</em> if a handler was found and it handled the command
	 * @throws IOException
	 *         if a communication error occurs
	 */
	protected Future<?> sendCommandIfPossible(CommandType command, Object... args) throws IOException {
		CommandHandler handler = getCommandHandlerForCommand(command);
		if ( handler != null ) {
			return handler.sendCommand(command, session, args);
		}
		return null;
	}

	private boolean handleCommandIfPossible(CommandType command, MessageHeader header, JsonNode tree)
			throws IOException {
		CommandHandler handler = getCommandHandlerForCommand(command);
		if ( handler != null ) {
			return handler.handleCommand(command, header, session, tree);
		}
		return false;
	}

	private CommandHandler getCommandHandlerForCommand(CommandType command) {
		// first check if the command is supported directly, and if so handle it
		if ( internalCommandHandler.supportsCommand(command) ) {
			return internalCommandHandler;
		} else {
			CommandHandler[] list = commandHandlers;
			if ( list != null ) {
				for ( CommandHandler handler : list ) {
					if ( handler.supportsCommand(command) ) {
						return handler;
					}
				}
			}
		}
		return null;
	}

	private static final int BINARY_BUFFER_SIZE = 4096;

	/**
	 * Attempt to handle a {@code BinaryFile} message type with text content.
	 * 
	 * @param header
	 *        The current message header.
	 * @param reader
	 *        The reader to read from. The reader may not be used further after
	 *        calling this method.
	 * @return <em>true</em> if the message was handled
	 * @throws IOException
	 *         if any communication error occurs
	 */
	private boolean handleBinaryFileIfPossible(MessageHeader header, Reader reader) throws IOException {
		BinaryFileHandler[] list = binaryFileHandlers;
		if ( list != null ) {
			Reader r = (reader.markSupported() ? reader
					: new BufferedReader(reader, BINARY_BUFFER_SIZE));
			r.mark(BINARY_BUFFER_SIZE);
			for ( BinaryFileHandler handler : list ) {
				boolean supports = handler.supportsTextMessage(header, r, BINARY_BUFFER_SIZE);
				r.reset();
				if ( supports ) {
					return handler.handleTextMessage(header, session, r);
				}
			}
		}
		return false;
	}

	/**
	 * Attempt to handle a {@code BinaryFile} message type with text content.
	 * 
	 * @param header
	 *        The current message header.
	 * @param buffer
	 *        The buffer to read from.
	 * @return <em>true</em> if the message was handled
	 * @throws IOException
	 *         if any communication error occurs
	 */
	private boolean handleBinaryFileIfPossible(MessageHeader header, ByteBuffer buffer) {
		if ( header.getType() == MessageType.Keepalive ) {
			return true;
		}
		BinaryFileHandler[] list = binaryFileHandlers;
		if ( list != null ) {
			buffer.mark();
			for ( BinaryFileHandler handler : list ) {
				boolean supports = handler.supportsDataMessage(header, buffer);
				buffer.reset();
				if ( supports ) {
					return handler.handleDataMessage(header, session, buffer);
				}
			}
		}
		return false;
	}

	@Override
	public void handleEvent(Event event) {
		final Session sess = session;
		final Long configId = (sess != null
				? (Long) session.getUserProperties().get(CONFIG_ID_USER_PROPERTY)
				: null);
		final Long eventConfigId = (Long) event.getProperty(LoxoneEvents.EVENT_PROPERTY_CONFIG_ID);
		if ( eventConfigId == null || !eventConfigId.equals(configId) ) {
			// not for this Loxone configuration
			return;
		}
		final String topic = event.getTopic();
		final long lastStructureFileModificationDate = (configuration == null
				|| configuration.getLastModified() == null ? -1
						: configuration.getLastModified().getTime());
		if ( LoxoneEvents.STRUCTURE_FILE_MODIFICATION_DATE_EVENT.equals(topic) ) {
			Object prop = event.getProperty(LoxoneEvents.EVENT_PROPERTY_DATE);
			if ( prop instanceof Number ) {
				final long date = ((Number) prop).longValue();
				if ( date != lastStructureFileModificationDate ) {
					log.info("Loxone configuration date different than local copy: will download now.");
					try {
						sendCommandIfPossible(CommandType.GetStructureFile);
					} catch ( IOException e ) {
						logConciseException("Communication problem requesting structure file", e);
					}
				} else {
					log.info("Loxone configuration up to date; enabling status updates.");
					try {
						sendCommandIfPossible(CommandType.EnableInputStatusUpdate);
					} catch ( IOException e ) {
						logConciseException("Communication problem requesting status updates", e);
					}
				}
			}
		} else if ( LoxoneEvents.STRUCTURE_FILE_SAVED_EVENT.equals(topic) ) {
			log.info("Loxone configuration saved; enabling status updates.");
			setConfiguration(configDao.getConfig(configId));
			try {
				sendCommandIfPossible(CommandType.EnableInputStatusUpdate);
			} catch ( IOException e ) {
				logConciseException("Communication problem requesting status updates", e);
			}
		}
	}

	private void logConciseException(String msg, Throwable t, Object... params) {
		// if DEBUG enabled, print exception stack trace, otherwise just the exception message
		if ( log.isDebugEnabled() ) {
			log.error(msg, params, t);
		} else {
			Object[] newParams;
			if ( params != null ) {
				newParams = new Object[params.length + 1];
				System.arraycopy(params, 0, newParams, 0, params.length);
				newParams[params.length] = t.getMessage();
			} else {
				newParams = new Object[] { t.getMessage() };
			}
			log.error(msg + ": {}", newParams);
		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		if ( this.host == null || !this.host.equals(host) ) {
			this.host = host;
			connectionDetailsChanged();
		}
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		if ( this.username == null || !this.username.equals(username) ) {
			this.username = username;
			this.authenticationFailure = false;
			connectionDetailsChanged();
		}
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		if ( this.password == null || !this.password.equals(password) ) {
			this.password = password;
			this.authenticationFailure = false;
			connectionDetailsChanged();
		}
	}

	/**
	 * Get the config key.
	 * 
	 * @return The config key, or {@code null}.
	 * @since 1.1
	 */
	public String getConfigKey() {
		return configKey;
	}

	/**
	 * Set an explicit config ID value to use, as a string.
	 * 
	 * At most 8 characters will be used from this value, which will be turned
	 * into a {@code Long} {@code configId} value.
	 * 
	 * @param configKey
	 *        The config ID to use, in string form.
	 * @since 1.1
	 */
	public void setConfigKey(String configKey) {
		if ( this.configKey == null || !this.configKey.equals(configKey) ) {
			this.configKey = configKey;
			connectionDetailsChanged();
		}
	}

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public CommandHandler[] getCommandHandlers() {
		return commandHandlers;
	}

	public void setCommandHandlers(CommandHandler[] commandHandlers) {
		this.commandHandlers = commandHandlers;
	}

	public BinaryFileHandler[] getBinaryFileHandlers() {
		return binaryFileHandlers;
	}

	public void setBinaryFileHandlers(BinaryFileHandler[] binaryFileHandlers) {
		this.binaryFileHandlers = binaryFileHandlers;
	}

	public OptionalService<EventAdmin> getEventAdmin() {
		return eventAdmin;
	}

	public void setEventAdmin(OptionalService<EventAdmin> eventAdmin) {
		this.eventAdmin = eventAdmin;
		internalCommandHandler.setEventAdmin(eventAdmin);
	}

	public TaskScheduler getTaskScheduler() {
		return taskScheduler;
	}

	public void setTaskScheduler(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

	public ConfigDao getConfigDao() {
		return configDao;
	}

	public void setConfigDao(ConfigDao configDao) {
		this.configDao = configDao;
	}

	/**
	 * Get the status message count.
	 * 
	 * @return The status message count.
	 */
	public int getStatusMessageCount() {
		return statusMessageCount;
	}

	/**
	 * Set a frequency of processed messages at which to log a status message.
	 * 
	 * @param statusMessageCount
	 *        The message frequency.
	 */
	public void setStatusMessageCount(int statusMessageCount) {
		this.statusMessageCount = statusMessageCount;
	}

	/**
	 * Test if an authentication failure has occurred.
	 * 
	 * If an authentication error occurs when connecting to the Loxone device,
	 * this flag will be set to <em>true</em>. It will be reset when either
	 * {@link #setUsername(String)} or {@link #setPassword(String)} are called
	 * with updated values.
	 * 
	 * @return authentication failure flag
	 * @since 1.3
	 */
	public final boolean isAuthenticationFailure() {
		return authenticationFailure;
	}

	/**
	 * Get the configured extra websocket client properties.
	 * 
	 * @return the client properties, or {@literal null}
	 */
	public Map<String, Object> getClientProperties() {
		return clientProperties;
	}

	/**
	 * Set extra websocket client properties to use when connecting.
	 * 
	 * @param clientProperties
	 *        the properties to use, or {@literal null}
	 * @since 1.7
	 */
	public void setClientProperties(Map<String, Object> clientProperties) {
		this.clientProperties = clientProperties;
	}

	/**
	 * Set the authentication type to use.
	 * 
	 * @param authenticationType
	 *        the type to use; defaults to {@link AuthenticationType#Auto}
	 * @since 1.8
	 */
	public void setAuthenticationType(AuthenticationType authenticationType) {
		this.authenticationType = (authenticationType != null ? authenticationType
				: AuthenticationType.Auto);
	}

	/**
	 * Set the auth token DAO to use.
	 * 
	 * @param configAuthTokenDao
	 *        the DAO to use
	 * @since 1.8
	 */
	public void setConfigAuthTokenDao(ConfigAuthenticationTokenDao configAuthTokenDao) {
		this.configAuthTokenDao = configAuthTokenDao;
	}

	/**
	 * Set the permission to request for authentication tokens.
	 * 
	 * @param tokenRequestPermission
	 *        the permission to use; defaults to {@literal App}
	 * @throws IllegalArgumentException
	 *         if {@code tokenRequestPermission} is {@literal null}
	 * @since 1.8
	 */
	public void setTokenRequestPermissions(AuthenticationTokenPermission tokenRequestPermission) {
		if ( tokenRequestPermission == null ) {
			throw new IllegalArgumentException("Token request permission must not be null");
		}
		this.tokenRequestPermission = tokenRequestPermission;
	}

	/**
	 * Set the number of hours before a token expires to try and refresh it.
	 * 
	 * <p>
	 * This time will be adjusted smaller if less time is available before the
	 * token expires.
	 * </p>
	 * 
	 * @param tokenRefreshOffsetHours
	 *        the maximum number of hours before a token expires to refresh it
	 * @throws IllegalArgumentException
	 *         if {@code tokenRefreshOffsetHours} is less than {@literal 0}
	 * @since 1.8
	 */
	public void setTokenRefreshOffsetHours(int tokenRefreshOffsetHours) {
		if ( tokenRefreshOffsetHours < 0 ) {
			throw new IllegalArgumentException("Token refresh offset hours must be at least 0");
		}
		this.tokenRefreshOffsetHours = tokenRefreshOffsetHours;
	}

}
