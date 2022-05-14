package components;

import mqtt.Message;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import burger.SmartTurtlebot;
import burger.RandomTurtlebot;
import burger.RealTurtlebot;
import burger.Orientation;
import mqtt.Message;
import org.json.simple.parser.JSONParser;

import java.util.HashMap;
import java.util.Map;

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
	
	public TurtlebotFactory(String sttime) {
		this.simulation = 0;
		this.debug = 0;
		this.display = 0;
		this.waittime = 0;
		this.sttime = sttime;
		mesRobots = new HashMap<String, Turtlebot>();
	}

	public void setMessage(Message mqtt) {
		clientMqtt = mqtt;
	}

	public void handleMessage(String topic, JSONObject content){
		if (topic.contains("configuration/nbRobot")) {
           	initRobots(content);
        }
        else if (topic.contains("configuration/real_robot")) {
           	initRealRobots(content);
        }
		else if (topic.contains("configuration/rabbit_robot")) {
			initRabbitRobots(content);
		}
		else if (topic.contains("configuration/wolf_robot")) {
			initWolfRobots(content);
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
		clientMqtt.subscribe("configuration/real_robot");
		clientMqtt.subscribe("configuration/rabbit_robot");
		clientMqtt.subscribe("configuration/wolf_robot");
		clientMqtt.subscribe("configuration/debug");
		clientMqtt.subscribe("configuration/display");
		clientMqtt.subscribe("configuration/simulation");
		clientMqtt.subscribe("configuration/waittime");
		clientMqtt.subscribe("configuration/seed");
		clientMqtt.subscribe("configuration/field");
	}

	public Turtlebot factory(int id, String name, Message clientMqtt, int x, int y, String type) {
		if (mesRobots.containsKey(name))
	    	return mesRobots.get(name);	    
	    Turtlebot turtle;
	    if(simulation == 0) {
	    	if(debug == 1) {
	    		System.out.println("Create real robot");
	    	}
	    	turtle = new RealTurtlebot(id, name, seed, field, clientMqtt, debug);
	    	if(debug==2 && sttime != null) {
	    		turtle.setLog(sttime);
	    	}
	    } else {
	    	if(debug == 1) {
	    		System.out.println("Create simulated robot");
	    	}
			switch(type){

				case "rabbit":
					turtle = new RabbitTurtlebot(id, name, seed, field, clientMqtt, debug);
					break;

				case "wolf":
					turtle = new WolfTurtlebot(id, name, seed, field, clientMqtt, debug);
					break;
			}
	    	//turtle = new SmartTurtlebot(id, name, seed, field, clientMqtt, debug);
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

	public void initRobots(JSONObject content) {
		int nbr = Integer.parseInt((String) content.get("nbRobot"));
		if( debug == 1) {
			System.out.println(nbr);
		}
		for (int i = 2; i < 2 + nbr; i++) {
			factory(i, turtlebotName + i, clientMqtt);
		}
	}

	public void initRealRobots(JSONObject content) {
		JSONArray jar = (JSONArray)content.get("jar");
		int nbr = jar.size();
		if( debug == 1) {
			System.out.println(nbr);
		}
        for(int i = 2; i < nbr+2; i++) {
        	factory(i, turtlebotName + i, clientMqtt);        	
		}			
	}
	public void initRabbitRobots(JSONObject content) {
		JSONArray jar = (JSONArray)content.get("jar");
		int nbr = jar.size();
		if( debug == 1) {
			System.out.println(nbr);
		}
		for(int i = 2; i < nbr+2; i++) {
			factory(i, turtlebotName + i, clientMqtt);
		}
	}
	public void initWolfRobots(JSONObject content) {
		JSONArray jar = (JSONArray)content.get("jar");
		int nbr = jar.size();
		if( debug == 1) {
			System.out.println(nbr);
		}
		JSONParser parser = new JSONParser();
		for(int i = 2; i < nbr+2; i++) {
			factory(i, turtlebotName + i, clientMqtt, ((JSONObject) jar.get(i)).get("x"), ((JSONObject) jar.get(i)).get("y"), ((JSONObject) jar.get(i)).get("type"));
		}
	}
}