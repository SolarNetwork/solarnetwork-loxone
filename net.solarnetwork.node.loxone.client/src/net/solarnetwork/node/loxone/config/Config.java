package net.solarnetwork.node.loxone.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Config {
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	private final Logger log = LoggerFactory.getLogger(getClass());

//	Properties
	private String lastModified;
	private Room[] rooms;
	private Category[] categories;
	private Control[] controls;
	
//	Build config
	public void update(String loxapp) {
		JsonNode rootNode;
		
		try {
			rootNode = objectMapper.readTree(loxapp);
			if(rootNode == null) { return; }
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		this.lastModified = rootNode.get("lastModified").asText();
		
//		Maps
		JsonNode roomsNode = rootNode.get("rooms");
		JsonNode categoriesNode = rootNode.get("cats");
		JsonNode controlsNode = rootNode.get("controls");
		
		rooms = new Room[roomsNode.size()];
		categories = new Category[categoriesNode.size()];
		controls = new Control[controlsNode.size()];
		
//		Build rooms
		if(roomsNode.isObject()) {
			int count = 0;
			
			log.debug("Building " + roomsNode.size() + " rooms");
			
			for(final JsonNode roomNode : roomsNode) {
				addRoom(count, roomNode.toString());
				count++;
			}
			
		} else {
			log.error("ERROR: Invalid LoxAPP file, rooms is not an object");
		}
		
//		Build categories
		if(categoriesNode.isObject()) {
			int count = 0;
			
			log.debug("Building " + categoriesNode.size() + " categories");
			
			for(final JsonNode categoryNode : categoriesNode) {
				addCategory(count, categoryNode.toString());
				count++;
			}
			
		} else {
			log.error("ERROR: Invalid LoxAPP file, categories is not an object");
		}
		
//		Build controls
		if(controlsNode.isObject()) {
			int count = 0;
			
			log.debug("Building " + controlsNode.size() + " controls");
			
			for(final JsonNode controlNode : controlsNode) {
				addControl(count, controlNode.toString());
				count++;
			}
			
		} else {
			log.error("ERROR: Invalid LoxAPP file, controls is not an object");
		}
		
	}
	
//	Build room
	private void addRoom(int index, String json) {
		try {
			Room room = objectMapper.readValue(json, Room.class);
			log.debug(String.format("Room %d: %s, %s, %s", index, room.getName(), room.getUuid(), room.getImage()));
			rooms[index] = room;
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	Build category
	private void addCategory(int index, String json) {
		try {
			Category category = objectMapper.readValue(json, Category.class);
			log.debug(String.format("Category %d: %s, %s, %s", index, category.getName(), category.getType(), category.getUuid()));
			categories[index] = category;
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	Build control
	private void addControl(int index, String json) {
		try {
			Control control = objectMapper.readValue(json, Control.class);
			
			log.debug(String.format("Control %d: %s, %s, %s, %s, %s", index, control.getName(), control.getType(), 
					getControlRoomName(control), getControlCategoryName(control), control.getUuid()));
			
			controls[index] = control;
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getLastModified() {
		return lastModified;
	}
	
	public Room getRoom(String uuid) {
		for (int rm = 0; rm < rooms.length; rm++) {
			if (rooms[rm].getUuid().equals(uuid)) {
				return rooms[rm];
			}
		}
		return null;
	}
	
	public Category getCategory(String uuid) {
		for (int rm = 0; rm < categories.length; rm++) {
			if (categories[rm].getUuid().equals(uuid)) {
				return categories[rm];
			}
		}
		return null;
	}
	
	public Control getControl(String uuid) {
		for (int rm = 0; rm < controls.length; rm++) {
			if (controls[rm].getUuid().equals(uuid)) {
				return controls[rm];
			}
		}
		return null;
	}
	
	public String getControlRoomName(String uuid) {
		Control control = getControl(uuid);
		if(control != null) {
			Room room = getRoom(control.getRoom());
			if(room != null) {
				return room.getName();
			}
		}
		
		return "missing";
	}
	
	public String getControlRoomName(Control control) {
		Room room = getRoom(control.getRoom());
		if(room != null) {
			return room.getName();
		}
		
		return "missing";
	}
	
	public String getControlCategoryName(String uuid) {
		Control control = getControl(uuid);
		if(control != null) {
			Category category = getCategory(control.getCategory());
			if(category != null) {
				return category.getName();
			}
		}
		
		return "missing";
	}
	
	public String getControlCategoryName(Control control) {
		Category category = getCategory(control.getCategory());
		if(category != null) {
			return category.getName();
		} else {
			log.debug("Couldn't find category, " + control.getCategory());
		}
		
		return "missing";
	}
	
}
