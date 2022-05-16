package model;

import org.json.simple.JSONObject;

import java.awt.*;
import java.util.Map;
import java.util.HashMap;

public class RobotDescriptor extends EntityDescriptor {
	
	protected Map<String,String> properties;
	private Color color;
	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}
	public RobotDescriptor (int [] location, int id, String name) {
		super(location);
		properties = new HashMap<String, String>();
		properties.put("id",id+"");
		properties.put("name",name);
	}

	public ComponentType getComponentType(){
		return ComponentType.robot;
	}

	public String display(){
		return getId()+"";
	}

	public int getId() {
		return Integer.parseInt(properties.get("id"));
	}

	public void setId(int id) {
		properties.put("id",id+"");
	}

	public String getName() {
		return properties.get("name");	
	}

	public void setName(String name) {
		properties.put("name", name);
	}

	public void set(String key,String value) {
		properties.put(key, value);
	}

	public String get(String key) {
		return properties.get(key);
	}

	public String toString() {
		return "{type: " + getComponentType() + ", name: " + getName()+ ", id: " + getId() + ", x: " + x  + ", y: " + y + "}"; 
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSON() {
		JSONObject jo = new JSONObject();
		jo.put("type","robot");
		jo.put("name",getName());
		jo.put("id",getId()+"");
		jo.put("x",""+x);
		jo.put("y",""+y);
		return jo;
	}
}
