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
import model.RobotType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FoodTurtlebot extends Turtlebot{
	protected Random rnd;
	protected Grid grid;
	public int LastMeet=0;

	public FoodTurtlebot(int id, String name, int seed, int field, Message clientMqtt, int debug) {
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

	public ArrayList<int[]> locateRabbit(){

		ArrayList<int[]> rabbitsPos= new ArrayList<int[]>(); // location of all rabbits

		for(int i=0; i < grid.getRows(); i++) {
			for(int j=0; j < grid.getColumns(); j++) {


				if( (j!=x || i!=y) && (grid.getCell(i,j).getComponentType() == ComponentType.robot)) {
					RobotDescriptor tb = (RobotDescriptor) grid.getCell(i,j);

					if (tb.getRobotType()==RobotType.rabbit){// the found robot is a "rabbit"
						int [] pos1Rabbit = {j,i};
						rabbitsPos.add(pos1Rabbit);
						return rabbitsPos;
					}
				}
			}
		}
		return rabbitsPos;
	}
	public void move(int step) {
		ArrayList<int[]> rabbitWolfPos =locateRabbit();

		int xo=x;
		int yo=y;
		Situated[] neighbor= new Situated[4];
		neighbor=grid.getAdjacentRobot(x,y);
		/*
		neighbor[0]=grid.getCell(y-1,x);
		neighbor[1]=grid.getCell(y+1,x);
		neighbor[2]=grid.getCell(y,x-1);
		neighbor[3]=grid.getCell(y,x+1);
		neighbor[4]=grid.getCell(y-1,x-1);
		neighbor[5]=grid.getCell(y+1,x+1);
		neighbor[6]=grid.getCell(y-1,x+1);
		neighbor[7]=grid.getCell(y+1,x-1);

		 */
		for (Situated s: neighbor ){
			if(s!=null) {
				if (s.getComponentType() == ComponentType.robot) {
					RobotDescriptor tb = (RobotDescriptor) s;
					if (tb.getRobotType() == RobotType.rabbit) {// the found robot is a "rabbit"
						int[] pos = grid.locate();

						this.setX(pos[0]);
						this.setY(pos[1]);

						LastMeet = 0;
						grid.moveSituatedComponent(xo, yo, x, y);

						JSONObject robotj = new JSONObject();
						robotj.put("name", name);
						robotj.put("id", "" + id);
						robotj.put("x", "" + x);
						robotj.put("y", "" + y);
						robotj.put("xo", "" + xo);
						robotj.put("yo", "" + yo);
						//System.out.println("MOVE MOVE " + xo + " " + yo + " --> " + x + " " + y);
						clientMqtt.publish("robot/nextPosition", robotj.toJSONString());


						//Initialize the grid again
						int rows = grid.getRows();
						int col = grid.getColumns();
						//this.grid=new Grid(rows, col, seed);


					}
				}
			}
		}


	}

	public ArrayList<int[]> locateWolfRabbit(){
   	 
    	ArrayList<int[]> rabbitsWolfsPos= new ArrayList<int[]>(); // location of all rabbits
        
    	for(int i=0; i < grid.getRows(); i++) {
            for(int j=0; j < grid.getColumns(); j++) {
            	
            	//Situated s = getCell(i, j); 
        		if( (j!=x || i!=y) && (grid.getCell(i,j).getComponentType() == ComponentType.robot)) {
        			RobotDescriptor tb = (RobotDescriptor) grid.getCell(i,j);
        		
        			if (tb.getRobotType()==RobotType.wolf || tb.getRobotType()==RobotType.rabbit){// the found robot is a "rabbit" 		
	        			int [] pos1Rabbit = {j,i};
	        	    	rabbitsWolfsPos.add(pos1Rabbit);
	        	    	//System.out.println("\n[W "+getId()+"] Food: ( xRabbit :"+pos1Rabbit[0]+", yRabbit:"+pos1Rabbit[1]+")\n");
        			}
        		}
            }
        }
    	return rabbitsWolfsPos;
	}
	
	// Compute the smallest distance between a set of points and a point Po={x,y}
	public int distanceNearest( ArrayList< int[]> points, int x, int y) {
		
		if (points.size()==0)
			return -1;
		
		int lowestDist=0;
		for( int k=0; k<points.size(); k++) {
			int [] point= points.get(k);
			int dist= (x-point[0])^2+(y-point[1])^2;
			if (dist<lowestDist)
				lowestDist=dist;
		}
		return lowestDist;
	}


	/*
	public void move(int step) {
	}
	*/

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
}