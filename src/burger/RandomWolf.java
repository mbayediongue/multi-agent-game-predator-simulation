package burger;

import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import model.ComponentType;
import model.EmptyCell;
import model.Grid;
import model.ObstacleDescriptor;
import model.RobotDescriptor;
import model.Situated;
import mqtt.Message;

public class RandomWolf extends SmartTurtlebot {

	public RandomWolf(int id, String name, int seed, int field, Message clientMqtt, int debug) {
		super(id, name, seed, field, clientMqtt, debug);
	}
	
	public void handleMessage(String topic, JSONObject content){				
		if (topic.contains(name+"/grid/update")) {
      		JSONArray ja = (JSONArray)content.get("cells");
      		List<Situated> ls = grid.get(ComponentType.robot);
      		for(int i=0; i < ja.size(); i++) {
      			JSONObject jo = (JSONObject)ja.get(i);
	        	String typeCell = (String)jo.get("type");
    	    	int xo = Integer.parseInt((String)jo.get("x"));
    	    	int yo = Integer.parseInt((String)jo.get("y"));
        		int[] to = new int[]{xo,yo};
           		if(typeCell.equals("robot")) {
           			int idr = Integer.parseInt((String)jo.get("id"));
           			boolean findr = false;
           			for(Situated sss:ls) {
           				if(sss != this){
	           				RobotDescriptor rd = (RobotDescriptor)sss;
    	       				if(rd.getId() == idr) {
        	   					grid.moveSituatedComponent(rd.getX(), rd.getY(), xo, yo);
           						findr = true;
           						break;
           					}
           				}
           			}
           			if(!findr) {
	           			String namer = (String)jo.get("name");
    	    			grid.forceSituatedComponent(new RobotDescriptor(to, idr, namer));
    	    		}
        		} else {
        			Situated sg = grid.getCell(yo,xo);
        			Situated s;
        			if(sg.getComponentType() == ComponentType.unknown) {
        				if(typeCell.equals("obstacle")){
							//System.out.println("Add ObstacleCell");
        					s = new ObstacleDescriptor(to);
        				} else {
        					//System.out.println("Add EmptyCell " + xo + ", " + yo);
        					s = new EmptyCell(xo,yo);
        				}
        				grid.forceSituatedComponent(s);
    				}
    			}
    		}
      		if(debug == 1) {
		   		System.out.println("---- " + name + " ----");
        		grid.display();
        	}
        } else if (topic.contains(name+"/action")) {
    	    int stepr = Integer.parseInt((String)content.get("step"));
        	move(stepr);
        } else if (topic.contains("inform/grid/init")) {
        	int rows = Integer.parseInt((String)content.get("rows"));
        	int columns = Integer.parseInt((String)content.get("columns"));
        	grid = new Grid(rows, columns, seed);
			grid.initUnknown();
		    grid.forceSituatedComponent(this);
		}
        else if (topic.contains(name+"/position/init")) {
      		x = Integer.parseInt((String)content.get("x"));
        	y = Integer.parseInt((String)content.get("y"));
        }
        else if (topic.contains(name+"/grid/init")) {
      		JSONArray ja = (JSONArray)content.get("cells");
      		for(int i=0; i < ja.size(); i++) {
      			JSONObject jo = (JSONObject)ja.get(i);
	        	String typeCell = (String)jo.get("type");
    	    	int xo = Integer.parseInt((String)jo.get("x"));
    	    	int yo = Integer.parseInt((String)jo.get("y"));
        		int[] to = new int[]{xo,yo};
        		Situated s;
				if(typeCell.equals("obstacle")){
					//System.out.println("Add ObstacleCell");
        			s = new ObstacleDescriptor(to);
        		}
        		else if(typeCell.equals("robot")){
        			//System.out.println("Add RobotCell");
        			int idr = Integer.parseInt((String)jo.get("id"));
        			String namer = (String)jo.get("name");
        			s = new RobotDescriptor(to, idr, namer);
        		}
        		else {
        			//System.out.println("Add EmptyCell " + xo + ", " + yo);
        			s = new EmptyCell(xo,yo);
        		}
        		grid.forceSituatedComponent(s);
      		}
        }
	}

	

}
