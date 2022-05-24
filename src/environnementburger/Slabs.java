package environnementburger;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.awt.Color;
import mqtt.Mqtt;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import model.Situated;
import model.ComponentType;

public class Slabs extends ArrayList<Slab>{
	private Mqtt mqttClient;
	protected Color colorrobot;
	protected Color colorobstacle;
	protected Color colorgoal;
	protected Color colorother;
	protected Color colorunknown;

	public Slabs()
	{
		mqttClient = new Mqtt("android", 1, "tcp://10.200.3.101:1883");
		//mqttClient = new Mqtt("android", 1, "tcp://127.0.0.1:1883");
	}

	public void setColorrobot(Color c){
		colorrobot = c;
	}

	public void setColorobstacle(Color c){
		colorobstacle = c;
	}

	public void setColorgoal(Color c){
		colorgoal = c;
	}

	public void setColorother(Color c){
		colorother = c;
	}

	public void setColorunknown(Color c){
		colorunknown = c;
	}

	public void initSlab(){
		add(new Slab("table1",4,3,0,0));
		add(new Slab("table2",4,3,2,3));
	}
	
	public void initLEDTable(Situated[][] g){
		Map<String,JSONObject> l = initializeJsonTable(g);
        for(Map.Entry<String, JSONObject> elt:l.entrySet())
        {
        	//System.out.println(elt.getKey() + " --> " + elt.getValue().toJSONString());
        	mqttClient.publish(elt.getKey(), elt.getValue().toJSONString());
        }
	}

	public void setLed(int xo, int yo, int x, int y, Color cold, Color cnew) {
		boolean bo = false;
		boolean bn = false;
		Map<JSONObject,String> result = new HashMap<JSONObject,String>();
		for(Slab s:this){
			if(bo && bn){
				for(Map.Entry<JSONObject,String> elt:result.entrySet())
        		{
        			System.out.println(elt.getValue() + " --> " + elt.getKey().toJSONString());
        			mqttClient.publish(elt.getValue(), elt.getKey().toJSONString());
        		}
				return;
			}
			System.out.println("x0: " + xo + ", yo: " + yo);
			System.out.println("x: " + x + ", y: " + y);
			System.out.println("slab x: " + s.getPositionX() + ", slab y: " + s.getPositionY());
			System.out.println("slab height: " + s.getHeight() + ", slab width: " + s.getWidth());
			if(bo == false && xo>=s.getPositionX() && xo < s.getPositionX() + s.getWidth() && yo>=s.getPositionY() && yo < s.getPositionY() + s.getHeight()){
				JSONObject jcomp = new JSONObject();
				jcomp.put("x", yo-s.getPositionY());
				jcomp.put("y", xo-s.getPositionX());
				JSONObject jc = new JSONObject();
				jc.put("r", cold.getRed());
				jc.put("g", cold.getGreen());
				jc.put("b", cold.getBlue());
				jcomp.put("c", jc);
				result.put(jcomp, s.getId()+"/led");
				System.out.println(jcomp.toJSONString());
				bo = true;
			}
			if(bn == false && x>=s.getPositionX() && x < s.getPositionX() + s.getWidth() && y>=s.getPositionY() && y < s.getPositionY() + s.getHeight()){
				JSONObject jcomp = new JSONObject();
				jcomp.put("x", y-s.getPositionY());
				jcomp.put("y", x-s.getPositionX());
				JSONObject jc = new JSONObject();
				jc.put("r", cnew.getRed());
				jc.put("g", cnew.getGreen());
				jc.put("b", cnew.getBlue());
				jcomp.put("c", jc);
				result.put(jcomp, s.getId()+"/led");
				System.out.println(jcomp.toJSONString());
				bn = true;
			}
		}
		if(bo && bn){
			for(Map.Entry<JSONObject,String> elt:result.entrySet())
       		{
       			System.out.println(elt.getValue() + " --> " + elt.getKey().toJSONString());
       			mqttClient.publish(elt.getValue(), elt.getKey().toJSONString());
       		}
			return;
		}
	}
	
	public Map<String,JSONObject>  initializeJsonTable(Situated[][] grid)
	{
		Map<String,JSONObject> result = new HashMap<String,JSONObject>();
		
		for(Slab s:this)
		{
			JSONObject jslab = new JSONObject();
			JSONArray jcomps = new JSONArray();
			for(int i = 0; i < s.getHeight(); i++)
			{
				for(int j = 0; j < s.getWidth(); j++)
				{
					JSONObject jcomp = new JSONObject();
					jcomp.put("x", i);
					jcomp.put("y", j);
					Situated c = grid[i+s.getPositionY()][j+s.getPositionX()];
					if(c.getComponentType() == ComponentType.empty) {					
						/*if(isGoal(j,i) < 0) {							
							JSONObject jc = new JSONObject();
							jc.put("r", colorgoal.getRed());
							jc.put("g", colorgoal.getGreen());
							jc.put("b", colorgoal.getBlue());
							jcomp.put("c", jc);
						} else {							*/
							JSONObject jc = new JSONObject();
							jc.put("r", colorother.getRed());
							jc.put("g", colorother.getGreen());
							jc.put("b", colorother.getBlue());
							jcomp.put("c", jc);
						//}
					}
					else if(c.getComponentType() == ComponentType.robot) {
						JSONObject jc = new JSONObject();
						jc.put("r", colorrobot.getRed());
						jc.put("g", colorrobot.getGreen());
						jc.put("b", colorrobot.getBlue());
						jcomp.put("c", jc);
					}
					else if(c.getComponentType() == ComponentType.obstacle){
						JSONObject jc = new JSONObject();
						jc.put("r", colorobstacle.getRed());
						jc.put("g", colorobstacle.getGreen());
						jc.put("b", colorobstacle.getBlue());
						jcomp.put("c", jc);
					}
					else {
						JSONObject jc = new JSONObject();
						jc.put("r", colorunknown.getRed());
						jc.put("g", colorunknown.getGreen());
						jc.put("b", colorunknown.getBlue());
						jcomp.put("c", jc);
					}
					jcomps.add(jcomp);
				}
			}
			jslab.put("led", jcomps);
			result.put(s.getId()+"/leds", jslab);
		}
		return result;
	}
}
