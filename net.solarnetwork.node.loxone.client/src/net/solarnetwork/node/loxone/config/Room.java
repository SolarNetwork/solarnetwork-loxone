package net.solarnetwork.node.loxone.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Room {
	
//	Properties
	private String uuid;
	private String name;
	private String image;

//	Constructor
	@JsonCreator
	public Room(@JsonProperty("uuid") String uuid,
					@JsonProperty("name") String name,
					@JsonProperty("image") String image) {
		
		this.uuid = uuid;
		this.name = name;
		this.image = image;
		
	}
	
//	Getters and Setters
	
//	UUID
	public String getUuid() { return this.uuid; }
	public void setUuid(String uuid) { this.uuid = uuid; }
	
//	Name
	public String getName() { return this.name; }
	public void setName(String name) { this.name = name; }
	
//	Image
	public String getImage() { return this.image; }
	public void setImage(String image) { this.image = image; }
	
}