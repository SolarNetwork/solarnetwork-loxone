package net.solarnetwork.node.loxone.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Category {
	
//	Properties
	private String uuid;
	private String name;
	private String image;
	private String type;
	private String color;

//	Constructor
	@JsonCreator
	public Category(@JsonProperty("uuid") String uuid,
					@JsonProperty("name") String name,
					@JsonProperty("image") String image,
					@JsonProperty("type") String type,
					@JsonProperty("color") String color) {
		
		this.uuid = uuid;
		this.name = name;
		this.image = image;
		this.type = type;
		this.color = color;
		
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
	
//	Type
	public String getType() { return this.type; }
	public void setType(String type) { this.type = type; }
	
//	Color
	public String getColor() { return this.color; }
	public void setColor(String color) { this.color = color; }
	
}
