package net.solarnetwork.node.loxone.config;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Control {
	
//	Properties
	private String uuid;
	private String name;
	private String type;
	private String room;
	private String category;
	private Map<String, String> states;
	private Map<String, Object> details;

//	Constructor
	@JsonCreator
	public Control(
			@JsonProperty("uuidAction") String uuid,
			@JsonProperty("name") String name,
			@JsonProperty("type") String type,
			@JsonProperty("room") String room,
			@JsonProperty("cat") String category,
			@JsonProperty("states") Map<String, String> states,
			@JsonProperty("details") Map<String, Object> details) {
		
		this.uuid = uuid;
		this.name = name;
		this.type = type;
		this.room = room;
		this.category = category;
		this.states = states;
		this.details = details;
		
	}
	
//	Getters and Setters
	
//	UUID
	public String getUuid() { return this.uuid; }
	public void setUuid(String uuid) { this.uuid = uuid; }
	
//	Name
	public String getName() { return this.name; }
	public void setName(String name) { this.name = name; }
	
//	Type
	public String getType() { return this.type; }
	public void setType(String type) { this.type = type; }
	
//	Room
	public String getRoom() { return this.room; }
	public void setRoom(String room) { this.room = room; }
	
//	Category
	public String getCategory() { return this.category; }
	public void setCategory(String category) { this.category = category; }

//	States
	public Map<String, String> getStates() { return states; }
	public void setStates(Map<String, String> states) { this.states = states; }

//	Details
	public Map<String, Object> getDetails() { return details; }
	public void setDetails(Map<String, Object> details) { this.details = details; }
	
}