
package burger;

import model.Situated;
import components.Turtlebot;
import model.EmptyCell;
import mqtt.Message;
import java.util.Random;
import model.ObstacleDescriptor;
import model.RobotDescriptor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FoodTurtlebot extends Turtlebot{
	protected Random rnd;
	protected List<Situated> grid;
	protected int rows;
	protected int columns;
	protected int ID_food=4;

	public FoodTurtlebot(int id, String name, int seed, int field, Message clientMqtt, int debug) {
		super(id, name, seed, field, clientMqtt, debug);
		rnd = new Random(seed);
		grid = new ArrayList<Situated>();	
		id=ID_food;
	}

	protected void init() {
		clientMqtt.subscribe("inform/grid/init");
    	clientMqtt.subscribe(name + "/position/init");		
		clientMqtt.subscribe(name + "/grid/init");		
		clientMqtt.subscribe(name + "/grid/update");		
		clientMqtt.subscribe(name + "/action");		
	}

	public void handleMessage(String topic, JSONObject content){				
		if (topic.contains(name+"/grid/update")) {
			grid = new ArrayList<Situated>();
      		JSONArray ja = (JSONArray)content.get("cells");
      		for(int i=0; i < ja.size(); i++) {
      			JSONObject jo = (JSONObject)ja.get(i);
	        	String typeCell = (String)jo.get("type");
    	    	int xo = Integer.parseInt((String)jo.get("x"));
    	    	int yo = Integer.parseInt((String)jo.get("y"));
        		int[] to = new int[]{xo,yo};
           		if(typeCell.equals("robot")) {
           			int idr = Integer.parseInt((String)jo.get("id"));
           			String namer = (String)jo.get("name");
           			if(idr != id){
	    	    		grid.add(new RobotDescriptor(to, idr, namer));
	    	    	}
        		} else if(typeCell.equals("obstacle")){
					//System.out.println("Add ObstacleCell");
        			grid.add(new ObstacleDescriptor(to));
        		} else {
        			//System.out.println("Add EmptyCell " + xo + ", " + yo);
        			grid.add(new EmptyCell(xo,yo));
    			}
    		}
      		if(debug == 1) {
		   		System.out.println("---- " + name + " ----");
        		for(Situated s:grid){
        			s.display();
        		}
        	}
        } else if (topic.contains(name+"/action")) {
    	    int stepr = Integer.parseInt((String)content.get("step"));
        	move(stepr);
        } 
        else if (topic.contains("inform/grid/init")) {
        	rows = Integer.parseInt((String)content.get("rows"));
        	columns = Integer.parseInt((String)content.get("columns"));
        }
        else if (topic.contains(name+"/position/init")) {
      		x = Integer.parseInt((String)content.get("x"));
        	y = Integer.parseInt((String)content.get("y"));
        }
        else if (topic.contains(name+"/grid/init")) {
        	grid = new ArrayList<Situated>();
      		JSONArray ja = (JSONArray)content.get("cells");
      		for(int i=0; i < ja.size(); i++) {
      			JSONObject jo = (JSONObject)ja.get(i);
	        	String typeCell = (String)jo.get("type");
    	    	int xo = Integer.parseInt((String)jo.get("x"));
    	    	int yo = Integer.parseInt((String)jo.get("y"));
        		int[] to = new int[]{xo,yo};
           		if(typeCell.equals("robot")) {
           			int idr = Integer.parseInt((String)jo.get("id"));
           			boolean findr = false;
           			String namer = (String)jo.get("name");
    	    		if(idr != id){
	    	    		grid.add(new RobotDescriptor(to, idr, namer));
	    	    	}
        		} else if(typeCell.equals("obstacle")){
					//System.out.println("Add ObstacleCell");
        			grid.add(new ObstacleDescriptor(to));
        		} else {
        			//System.out.println("Add EmptyCell " + xo + ", " + yo);
        			grid.add(new EmptyCell(xo,yo));
    			}
    		}
      		if(debug == 1) {
		   		System.out.println("---- " + name + " ----");
        		for(Situated s:grid){
        			s.display();
        		}
        	}
        }
	}

	public void setLocation(int x, int y) {
		int xo = this.x;
		int yo = this.y;
		this.x = x;
		this.y = y;		
	}

	public List<Situated> getGrid() {
		return grid;
	}

	public void setGrid(List<Situated> grid) {
		this.grid = grid;
	}

	public void randomOrientation() {
		double d = Math.random();
		if(d < 0.25) {
			if(orientation != Orientation.up) 
				orientation = Orientation.up;
			else 
				orientation = Orientation.down;
		}
		else if(d < 0.5) {
			if(orientation != Orientation.down) 
				orientation = Orientation.down;
			else 
				orientation = Orientation.up;
		}
		else if(d < 0.75) {
			if(orientation != Orientation.left) 
				orientation = Orientation.left;
			else 
				orientation = Orientation.right;
		}
		else {
			if(orientation != Orientation.right) 
				orientation = Orientation.right;
			else 
				orientation = Orientation.left;
		}
	}

	public void move(int step) {	
	}

	public void moveLeft(int step) {
		Orientation oldo = orientation;
		for(int i = 0; i < step; i++){
			if(orientation == Orientation.up) {
				orientation = Orientation.left;
			}
			if(orientation == Orientation.left) {
				orientation = Orientation.down;
			}
			if(orientation == Orientation.right) {
				orientation = Orientation.up;
			}
			else {
				orientation = Orientation.right;
			}
		}
	}

	public void moveRight(int step) {
		Orientation oldo = orientation;
		for(int i = 0; i < step; i++){
			if(orientation == Orientation.up) {
				orientation = Orientation.right;
			}
			if(orientation == Orientation.left) {
				orientation = Orientation.up;
			}
			if(orientation == Orientation.right) {
				orientation = Orientation.down;
			}
			else {
				orientation = Orientation.left;
			}
		}	
	}

	public void moveForward() {
		int xo = x;
		int yo = y;
		if(orientation == Orientation.up) {
			x += 1;
			x = Math.min(x,columns-1);
		}
		else if(orientation == Orientation.left) {
			y -= 1;
			y = Math.max(y,0);
		}
		else if(orientation == Orientation.right) {
			y += 1;
			y = Math.min(y,rows-1);
		}
		else {
			x -= 1;
			x = Math.max(x,0);
		}	
		JSONObject robotj = new JSONObject();
		robotj.put("name", name);
		robotj.put("id", ""+id);
		robotj.put("x", ""+x);
		robotj.put("y", ""+y);
		robotj.put("xo", ""+xo);
		robotj.put("yo", ""+yo);
		//System.out.println("MOVE MOVE " + xo + " " + yo + " --> " + x + " " + y);
		clientMqtt.publish("robot/nextPosition", robotj.toJSONString());
	}

	public void moveBackward() {

	}
}