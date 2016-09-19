
package net.solarnetwork.node.loxone.client;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

import net.solarnetwork.node.loxone.config.Config;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.settings.support.BasicTextFieldSettingSpecifier;

public class LoxoneInterface implements SettingSpecifierProvider {

	private MessageSource messageSource;
	private String sourceId;
	private String host;
	private String username;
	private String password;
	private LoxoneWebsocketClient client = new LoxoneWebsocketClient();
	private Config config = new Config();

	private final Logger log = LoggerFactory.getLogger(getClass());

	public LoxoneInterface() {
		// TODO Auto-generated constructor stub
	}

	/* Setting Specifier Provider */

	@Override
	public String getSettingUID() {
		return "net.solarnetwork.node.loxone.client";
	}

	@Override
	public String getDisplayName() {
		return "Loxone Client";
	}

	@Override
	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		LoxoneInterface defaults = new LoxoneInterface();
		List<SettingSpecifier> results = new ArrayList<SettingSpecifier>(4);
		results.add(new BasicTextFieldSettingSpecifier("sourceId", defaults.getSourceId()));
		results.add(new BasicTextFieldSettingSpecifier("host", defaults.getHost()));
		results.add(new BasicTextFieldSettingSpecifier("username", defaults.getUsername()));
		results.add(new BasicTextFieldSettingSpecifier("password", defaults.getPassword(), true));
		return results;
	}

	public void shutdown() {
		log.debug("SHUTDOWN");
		if ( client != null ) {
			client.close();
		}
	}

	private void updateConnection() {
		log.debug("Updating Loxone connection host {} username {} password {}", host, username,
				password);
		if ( host == null || username == null || password == null || host.isEmpty() || username.isEmpty() || password.isEmpty() ) {
			return;
		}

		if ( client == null ) {
			log.debug("Creating Loxone websocket connection");
			client = new LoxoneWebsocketClient();
		}

		client.open(host, username, password);
	}

	//	Getters and setters

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		log.debug("set host: " + host);
		this.host = host;
		this.updateConnection();
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		log.debug("set username: " + username);
		this.username = username;
		this.updateConnection();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		log.debug("set password: " + password);
		this.password = password;
		this.updateConnection();
	}
}
