package model;

import org.json.simple.JSONObject;
import java.util.Map;
import java.util.HashMap;

public class RabbitDescriptor extends RobotDescriptor implements Situated{
	
	protected Map<String,String> properties;	
	
	public RabbitDescriptor (int [] location, int id, String name) {
		super(location, id, name);
	}
	
	@Override
	public ComponentType getComponentType(){
		return ComponentType.rabbit;
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSON() {
		JSONObject jo = new JSONObject();
		jo.put("type","rabbit");
		jo.put("name",getName());
		jo.put("id",getId()+"");
		jo.put("x",""+x);
		jo.put("y",""+y);
		return jo;
	}
}
