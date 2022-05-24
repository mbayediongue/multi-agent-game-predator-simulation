package components;

import mqtt.Message;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import burger.SmartTurtlebot;
import burger.SmartWolfTurtlebot2;
import burger.WolfTurtlebot;
import main.IniFile;
import main.TestAppli;
import model.Grid;
import burger.RandomTurtlebot;
import burger.RealTurtlebot;
import burger.FoodTurtlebot;
import burger.Orientation;
import burger.Rabbit;
import mqtt.Message;

import java.util.HashMap;
import java.util.Map;
import java.io.File;

/* This class defines the different operations that the robot can do on the grid */

public class TurtlebotFactory implements SimulationComponent {	
	
	private HashMap<String, Turtlebot> mesRobots;
	private final String turtlebotName = "burger_";	
	protected Message clientMqtt;
	protected int simulation;
	protected int debug;
	protected int display;
	protected int waittime;
	protected int seed;
	protected int field;
	protected String sttime;

	
	// added instances:
	protected int field_wolf=3;
	protected int ID_robot=2;
	protected int ID_food=4;
	public Grid GridSharedRabbits;
	
	public TurtlebotFactory(String sttime) {
		this.simulation = 0;
		this.debug = 0;
		this.display = 0;
		this.waittime = 0;
		this.sttime = sttime;
		mesRobots = new HashMap<String, Turtlebot>();
	}
	
	public int [] getGridSize() throws Exception {

		IniFile ifile= new IniFile("prop.ini");
		int rows =  ifile.getIntValue("environment", "rows");
		int columns =  ifile.getIntValue("environment", "columns");
		
		int [] size= new int[2];
		size[0]=rows;
		size[1]=columns;
		return size;
	}
	
	public void setMessage(Message mqtt) {
		clientMqtt = mqtt;
	}

	public void handleMessage(String topic, JSONObject content){
		if (topic.contains("configuration/nbRobot")) {
           	initRobots(content);
        }
        else if (topic.contains("configuration/debug")) {
           	debug = Integer.parseInt((String)content.get("debug"));
        }
        else if (topic.contains("configuration/field")) {
           	field = Integer.parseInt((String)content.get("field"));
        }
        else if (topic.contains("configuration/seed")) {
           	seed = Integer.parseInt((String)content.get("seed"));
        }
        else if (topic.contains("configuration/display")) {
           	display = Integer.parseInt((String)content.get("display"));
        }
        else if (topic.contains("configuration/simulation")) {
           	simulation = Integer.parseInt((String)content.get("simulation"));
        }
        else if (topic.contains("configuration/waittime")) {
    	    waittime = Integer.parseInt((String)content.get("waittime"));
        }
	}

	public void moveRobot(Turtlebot t) {
		JSONObject jo = new JSONObject();
       	jo.put("name", t.getName());
       	jo.put("action", "move");
       	jo.put("step", "1");
      	clientMqtt.publish(t.getName() +"/action", jo.toJSONString());
	}

	public void schedule(int nbStep) {
		for(int i = 0; i < nbStep; i++){
			for(Turtlebot t: mesRobots.values()) {
				updateGrid(t);
				moveRobot(t);
			}
			try {
				Thread.sleep(waittime);
			}catch(InterruptedException ie){
				System.out.println(ie);
			}
		}
		for(Turtlebot t: mesRobots.values()) {
			t.setGoalReached(true);
		}
		System.out.println("END");
	}

	public void updateGrid(Turtlebot t) {
		JSONObject jo = new JSONObject();
       	jo.put("name", t.getName());
      	jo.put("field",t.getField()+"");
       	jo.put("x",t.getX()+"");
       	jo.put("y",t.getY()+"");
       	clientMqtt.publish("robot/grid", jo.toJSONString());
	}

	public void next(int id) {
		if(!finish()) {
			int next = id + 1;
			while(true) {
				JSONObject message = new JSONObject();
				if (next == mesRobots.size() + 2)
					next = 2;
				String stn = turtlebotName + next;
				Turtlebot t = mesRobots.get(stn);
				if(t.isGoalReached()) {
					next++;
					continue;
				}
				JSONObject robot = new JSONObject();
				robot.put("id", turtlebotName + next);
				message.put("robot", robot);
				message.put("next", next);
				clientMqtt.publish(stn + "/nextStep", message.toJSONString());
				return;
			}
		}
		JSONObject msg = new JSONObject();
		msg.put("end", 1+"");
		clientMqtt.publish("/end", msg.toJSONString());
	}

	public boolean finish() {
		int i = 0;
		for (Turtlebot t : mesRobots.values())
			if (!t.isGoalReached())
				return false;
		return true;
	}

	/*public void testMove(String robotN){
		Turtlebot t = mesRobots.get(robotN);
		JSONObject pos = new JSONObject();
		pos.put("x1", t.getX()+"");
		pos.put("y1", t.getY()+"");
		t.setLocation(1,7);
		pos.put("x2", t.getX()+"");
		pos.put("y2", t.getY()+"");
		clientMqtt.publish(robotN+"/position", pos.toJSONString());
	}*/

	public void initSubscribe() {		
		clientMqtt.subscribe("configuration/nbRobot");
		clientMqtt.subscribe("configuration/debug");
		clientMqtt.subscribe("configuration/display");
		clientMqtt.subscribe("configuration/simulation");
		clientMqtt.subscribe("configuration/waittime");
		clientMqtt.subscribe("configuration/seed");
		clientMqtt.subscribe("configuration/field");
	}

	public Turtlebot factory(int id, String name, Message clientMqtt) {
		if (mesRobots.containsKey(name))
	    	return mesRobots.get(name);	    
	    Turtlebot turtle;
	    if(simulation == 0) {
	    	if(debug == 1) {
	    		System.out.println("Create real robot");
	    	}
	    	//turtle = new RealTurtlebot(id, name, seed, field, clientMqtt, debug);
	    	turtle =new RealTurtlebot(id,  name, seed,field, clientMqtt, debug);

	    	if(debug==2 && sttime != null) {
	    		turtle.setLog(sttime);
	    	}
	    } else {
	    	if(debug == 1) {
	    		System.out.println("Create simulated robot");
	    	}
	    	
	    	if( ID_robot%3==2) {
	    		int[] size= {20, 20};
	    		try {
	    		  size = getGridSize();
	    		}
	    		catch (Exception e) {
	    		}
	    		int rows=size[0];
	    		int columns=size[1];
	    		
	    		if (ID_robot==2)
	    			GridSharedRabbits= new Grid(rows, columns, seed);
	    		
	    		turtle = new Rabbit(id, name, seed, field, clientMqtt, debug, GridSharedRabbits);
	    		ID_robot+=1;
	    	}
	    	//turtle = new RandomTurtlebot(id, name, seed, field, clientMqtt, debug);
	    	else if (ID_robot%3==0) {
	    		turtle = new SmartWolfTurtlebot2(id, name, seed, field_wolf, clientMqtt, debug);
	    		ID_robot+=1;
	    	}
	    	else {
	    		turtle = new FoodTurtlebot(	ID_food, name, seed, field, clientMqtt, debug);
	    		ID_robot+=1;
	    	}
	    	
	    	if(debug==2 && sttime != null) {
	    		turtle.setLog(sttime);
	    	}	    	
	    }
	    mesRobots.put(name, turtle);
	    return turtle;
	}

	public String toString() {
		String st = "{";
		for(Map.Entry<String, Turtlebot> entry : mesRobots.entrySet()) {
    		String key = entry.getKey();
    		Turtlebot value = entry.getValue();
    		st += "{" + key + " : " + value + "}";
    		st += "\n";
		}
		st += "}";
		return st;
	}

	public void initTurtle(){
		for(Turtlebot t: mesRobots.values()) {
    		t.init();
    		clientMqtt.setAppli(t);
		}
	}

	public void initTurtleGrid(){
		for(Turtlebot t: mesRobots.values()) {
    		JSONObject jo = new JSONObject();
        	jo.put("name", t.getName());
        	jo.put("field",t.getField()+"");
        	jo.put("x",t.getX()+"");
        	jo.put("y",t.getY()+"");
        	clientMqtt.publish("configuration/robot/grid", jo.toJSONString());
        }
    }

	public Turtlebot get(String idRobot) {
		return mesRobots.get(idRobot);
	}

	public void initRobots(JSONObject nbRobot) {
		int nbr = Integer.parseInt((String) nbRobot.get("nbRobot"));
		if( debug == 1) {
			System.out.println(nbr);
		}
		for (int i = 2; i < 2 + nbr; i++) {
			factory(i, turtlebotName + i, clientMqtt);
		}
	}
}