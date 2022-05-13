package burger;
import model.ComponentType;
import model.Situated;
import components.Turtlebot;
import model.EmptyCell;
import model.UnknownCell;
import model.Grid;
import mqtt.Message;
import java.util.Random;
import model.ObstacleDescriptor;
import model.RobotDescriptor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.List;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Rabbit extends Turtlebot{
	protected Random rnd;
	protected Grid grid;
	
	// added
	public double waterLevel=20;
	public double foodLevel=20;
	public double wolfWeight=25;
	//public int timeSinceLastGettingOut;
	
	//end 
	public Rabbit(int id, String name, int seed, int field, Message clientMqtt, int debug) {
		super(id, name, seed, field, clientMqtt, debug);
		rnd = new Random(seed);	
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
        	//move(stepr);
    	    wellBeing(stepr);
    	    this.foodLevel-=1;
    	    this.waterLevel-=1;
    	    
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

	public void setLocation(int x, int y) {
		int xo = this.x;
		int yo = this.y;
		this.x = x;
		this.y = y;
		grid.moveSituatedComponent(xo, yo, x, y);
	}

	public Grid getGrid() {
		return grid;
	}

	public void setGrid(Grid grid) {
		this.grid = grid;
	}

	public void randomOrientation() {
		double d = Math.random();
		Orientation oldo = orientation;
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
		String actionr = "move_forward";
		String result = x + "," + y + "," + orientation + "," + grid.getCellsToString(y,x) + ",";
		for(int i = 0; i < step; i++) {
			EmptyCell[] ec = grid.getAdjacentEmptyCell(x,y);
			if(orientation == Orientation.up) {
				if(ec[3] != null) {
					moveForward();
				}
				else { 
					//randomOrientation();
					double d = Math.random();
					if(d < 0.5) {
						moveLeft(1);
						actionr = "turn_left";
					} else {
						moveRight(1);
						actionr = "turn_right";
					}
				}
			}
			else if(orientation == Orientation.down) {
				if(ec[2] != null) {
					moveForward();
				}
				else {
					//randomOrientation();
					double d = Math.random();
					if(d < 0.5) {
						moveLeft(1);
						actionr = "turn_left";
					} else {
						moveRight(1);
						actionr = "turn_right";
					}
				}
			}
			else if(orientation == Orientation.right) {
				if(ec[1] != null) { 
					moveForward();
				}
				else {
					//randomOrientation();
					double d = Math.random();
					if(d < 0.5) {
						moveLeft(1);
						actionr = "turn_left";
					} else {
						moveRight(1);
						actionr = "turn_right";
					}
				}
			}
			else if(orientation == Orientation.left) {
				if(ec[0] != null) {
					moveForward();
				}
				else {
					//randomOrientation();
					double d = Math.random();
					if(d < 0.5) {
						moveLeft(1);
						actionr = "turn_left";
					} else {
						moveRight(1);
						actionr = "turn_right";
					}
				}
			}
		}
		if(debug==2){
			try{
				writer.write(result + actionr); 
				writer.newLine();
				writer.flush();
			} catch(IOException ioe){
				System.out.println(ioe);
			}
		}
	}

	public void moveLeft(int step) {
		Orientation oldo = orientation;
		for(int i = 0; i < step; i++){
			if(orientation == Orientation.up) {
				orientation = Orientation.left;
			}
			else if(orientation == Orientation.left) {
				orientation = Orientation.down;
			}
			else if(orientation == Orientation.right) {
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
			else if(orientation == Orientation.left) {
				orientation = Orientation.up;
			}
			else if(orientation == Orientation.right) {
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
			x = Math.min(x,grid.getColumns()-1);
		}
		else if(orientation == Orientation.left) {
			y -= 1;
			y = Math.max(y,0);
		}
		else if(orientation == Orientation.right) {
			y += 1;
			y = Math.min(y,grid.getRows()-1);
		}
		else {
			x -= 1;
			x = Math.max(x,0);
		}	
		grid.moveSituatedComponent(xo,yo,x,y);
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
	
	
	// Added part
	public void wellBeing(int step) {

		int[] wolf =grid.locateWolf();
		int[] food= grid.locateFood();
		int xFood=food[0], yFood=food[1];
		int xWolf=wolf[0];
		int yWolf=wolf[1];
		
		int xk=x;
		int yl=y;
		
		String actionr = "move_forward";
		String result = x + "," + y + "," + orientation + "," + grid.getCellsToString(y,x) + ",";
		for(int i = 0; i < step; i++) {
			double bestWellBeing= wolfWeight*dist(xWolf, yWolf, xk, yl)-
					(1/foodLevel)*dist(xFood, yFood, xk, yl); 
			EmptyCell[] ec = grid.getAdjacentEmptyCell(x,y);
			int bestMove=-1;
			for(int k=0; k<4; k++) {
				if( ec[k]!=null) {
					if (k==0) {
						xk=x-1;
						yl=y;
					}
					else if (k==1) {
						yl=y-1;
						xk=x;
					}
					else if (k==2) {
						xk=x+1;
						yl=y;
					}
					else {
						yl=y+1;
						xk=x;
					}
					double wellBeing= wolfWeight*dist(xWolf, yWolf, xk, yl)-
							(1/foodLevel)*dist(xFood, yFood, xk, yl); 
					if (wellBeing > bestWellBeing) {
						bestWellBeing=wellBeing;
						bestMove=k;
					}
					
				}
			}
			
			if (bestMove==0) {
				if(orientation == Orientation.up) {
					moveLeft(1);
					actionr = "turn_left";
				} if(orientation == Orientation.down) {
					moveRight(1);
					actionr = "turn_right";
				} if(orientation==Orientation.right) {
					moveRight(2); // demi-tour (about turn)
					actionr = "turn_right";
				} if(orientation==Orientation.left)
					moveForward();
			}
			else if (bestMove==1) {
				if(orientation == Orientation.up) {
					moveRight(2); // demi-tour (about turn)
					actionr = "turn_right";
				} if(orientation == Orientation.down)
					moveForward();
				if(orientation==Orientation.right) {
					moveRight(1);
					actionr = "turn_right";
				} if(orientation==Orientation.left) {
					moveLeft(1);
					actionr = "turn_left";
				}
			}
			else if (bestMove==2) {
				if(orientation == Orientation.up) {
					moveRight(1);
					actionr = "turn_right";
				} if(orientation == Orientation.down) {
					moveLeft(1);
					actionr = "turn_left";
				}
				if(orientation==Orientation.right)
					moveForward();
				if(orientation==Orientation.left) {
					moveRight(2); // demi-tour (about turn)
					actionr = "turn_right";
				}
			}
			else if(bestMove==3) {
				if(orientation == Orientation.up)
					moveForward();
				if(orientation == Orientation.down) {
					moveRight(2); // demi-tour (about turn)
					actionr = "turn_right";
				} if(orientation==Orientation.right) {
					moveLeft(1);
					actionr = "turn_right";
				} if(orientation==Orientation.left) {
					moveRight(1);
					actionr = "turn_left";
				}
			}
		}
		if(debug==2){
			try{
				writer.write(result + actionr); 
				writer.newLine();
				writer.flush();
			} catch(IOException ioe){
				System.out.println(ioe);
			}
		}
	}
	
	public int dist(int x1, int y1, int x2, int y2) {
		return (x1-x2)^2+(y1-y2)^2;
	}
}