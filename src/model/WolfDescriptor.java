package model;

import org.json.simple.JSONObject;
import java.util.Map;
import java.util.HashMap;

public class WolfDescriptor extends RobotDescriptor implements Situated{
	
	protected Map<String,String> properties;	
	
	public WolfDescriptor (int [] location, int id, String name) {
		super(location, id, name);
	}
	
	@Override
	public ComponentType getComponentType(){
		return ComponentType.wolf;
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSON() {
		JSONObject jo = new JSONObject();
		jo.put("type","wolf");
		jo.put("name",getName());
		jo.put("id",getId()+"");
		jo.put("x",""+x);
		jo.put("y",""+y);
		return jo;
	}
}
