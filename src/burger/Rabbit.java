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

public class Rabbit extends Turtlebot{
	protected Random rnd;
	protected Grid grid;

	// added
	public double foodLevelInit=15;
	public double foodLevel=15;
	public double wolfWeight=20;
	public int xlast=x;
	public int ylast=y;
	public double penalityLastPosition=20;
	public int nbStep=0;
	//public int timeSinceLastGettingOut;

	//end
	public Rabbit(int id, String name, int seed, int field, Message clientMqtt, int debug, Grid grid) {
		super(id, name, seed, field, clientMqtt, debug);

		rnd = new Random(seed);
		this.grid= grid;
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
			this.foodLevel +=1.5;
			//this.waterLevel-=1;

		} else if (topic.contains("inform/grid/init")) {
			int rows = Integer.parseInt((String)content.get("rows"));
			int columns = Integer.parseInt((String)content.get("columns"));
			//grid = new Grid(rows, columns, seed);
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
		System.out.println("\n Move n");
		String actionr = "move_forward";
		String result = x + "," + y + "," + orientation + "," + grid.getCellsToString(y,x) + ",";
		int xo=x;
		int yo=y;
		Situated[] neighbor= new Situated[4];
		neighbor[0]=grid.getCell(x,y-1);
		neighbor[1]=grid.getCell(x,y+1);
		neighbor[2]=grid.getCell(x-1,y);
		neighbor[3]=grid.getCell(x+1,y);
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
		xlast=xo;
		ylast=yo;
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

	// robot type: unknown, wolf, rabbit or food
	@Override
	public RobotType getRobotType() {
		return RobotType.rabbit;
	}


	public ArrayList<int[]> locateWolf(){
		Grid gridAgent=this.getGrid();
		ArrayList<int[]> wolfsPos= new ArrayList<int[]>(); // location of all rabbits

		for(int i=0; i < gridAgent.getRows(); i++) {
			for(int j=0; j < gridAgent.getColumns(); j++) {

				//Situated s = getCell(i, j);
				if( (j!=x || i!=y) && (gridAgent.getCell(i,j).getComponentType() == ComponentType.robot)) {
					RobotDescriptor tb = (RobotDescriptor) gridAgent.getCell(i,j);

					if (tb.getRobotType()==RobotType.wolf){// the found robot is a "wolf"
						int [] pos1Wolf = {j,i};
						wolfsPos.add(pos1Wolf);
						System.out.println("\n[Rabbit "+ getId()+"]"+" Wolf: (xWolf :"+pos1Wolf[0]+", yWolf:"+ pos1Wolf[1]+")\n");
					}
				}
			}
		}

		return wolfsPos;
	}

	public ArrayList<int[]> locateFood(){
		Grid gridAgent=this.getGrid();
		ArrayList<int[]> foodsPos= new ArrayList<int[]>();
		for(int i=0; i < gridAgent.getRows(); i++) {
			for(int j=0; j < gridAgent.getColumns(); j++) {
				if( gridAgent.getCell(i, j).getComponentType() == ComponentType.robot ){

					try{
						Turtlebot tb = (Turtlebot) gridAgent.getCell(i,j);
						if (tb.getRobotType()==RobotType.food){
							int [] pos1food = {j,i}; // position of a food
							foodsPos.add(pos1food);
							System.out.println("\n[Rabbit "+getId()+"]Food: (xFood ,"+pos1food[0]+", yFood:"+pos1food[1]+")\n");
						}
					}

					catch (Exception e) {
						RobotDescriptor tb = (RobotDescriptor) gridAgent.getCell(i,j);
						if (tb.getRobotType()==RobotType.food){// the found robot is a "rabbit"
							int [] pos1food = {j,i}; // position of a food
							foodsPos.add(pos1food);
							System.out.println("\n[Rabbit "+ getId()+"] Food : (xFood :"+pos1food[0]+", yFood:"+pos1food[1]+")\n");
						}
					}

				}
			}
		}

		return foodsPos;
	}

	// Added part
	public void wellBeing(int step) {


		ArrayList<int[]> wolfPos =locateWolf();
		ArrayList<int[]> foodsPos= locateFood();

		//System.out.println("Rabbit: (xRabbit :"+x+", yRabbit :"+y+")\n");
		int xk=x;
		int yl=y;
		double foodLevel=this.foodLevel;
		double wolfWeight=this.wolfWeight;
		double distFood= distanceNearest(foodsPos, xk, yl);  // distance to the nearest food place
		double distWolf= distanceNearest(wolfPos, xk, yl); // distance to the nearest rabbit

		if( distFood==-1) // food place is not found yet
			foodLevel=0; // then food is not considered in the objective function

		if(distWolf==-1) // the wolf place is not found yet
			wolfWeight=0;// then is not considered in the objective function
		if( distWolf> 2*distFood)
			wolfWeight=0;
		if( distWolf<3)
			wolfWeight=2*this.wolfWeight;
		if( distFood<3)
			foodLevel=2*this.foodLevel;
		if( distFood<2) {
			foodLevel=10*this.foodLevel;
			this.foodLevel=foodLevelInit;
		}
		wolfWeight=0;

		String actionr = "move_forward";
		String result = x + "," + y + "," + orientation + "," + grid.getCellsToString(y,x) + ",";
		for(int ii = 0; ii < step; ii++) {
			//double bestWellBeing= wolfWeight*dist(xWolf, yWolf, xk, yl)-
			//		(foodLevel)*dist(xFood, yFood, xk, yl);
			double bestWellBeing= Double.NEGATIVE_INFINITY ;//foodLevel(foodLevel)*dist(xFood, yFood, xk, yl);
			EmptyCell[] ec = grid.getAdjacentEmptyCell(xk,yl);
			int bestMove=-1;
			for(int k=0; k<4; k++) {
				if( ec[k]!=null) {
					if (k==2) {
						xk=x-1;
						yl=y;
					}
					else if (k==0) {
						yl=y-1;
						xk=x;
					}
					else if (k==3) {
						xk=x+1;
						yl=y;
					}
					else {
						yl=y+1;
						xk=x;
					}
					// we penalize last position to avoid the wolf to loop for a long time
					double penality=0.0;
					if(xk==xlast && yl==ylast)
						penality=penalityLastPosition;
					distFood= distanceNearest(foodsPos, xk, yl);  // distance to the nearest food place
					distWolf= distanceNearest(wolfPos, xk, yl);
					double wellBeing= wolfWeight*distWolf-(foodLevel)*distFood-penality;
					if (wellBeing > bestWellBeing) {
						bestWellBeing=wellBeing;
						bestMove=k;
					}

				}
			}
			ec = grid.getAdjacentEmptyCell(x,y);
			double d = Math.random();
			if (distFood==-1 && distWolf==-1) // if food place and rabbit position are not found then random walk
				d=0.0;
			if( d<=0.1) {
				bestMove=rnd.nextInt(4);
				while( ec[bestMove]==null)
					bestMove=rnd.nextInt(4);
			}



			System.out.println("\n Best wb: "+bestWellBeing+"\n");
			if (bestMove==0) {
				if(orientation == Orientation.up) {
					moveLeft(1);
					moveForward();
					//moveForward();
					actionr = "turn_left";
				} if(orientation == Orientation.down) {
					moveRight(1);
					moveForward();
					//moveForward();
					actionr = "turn_right";
				} if(orientation==Orientation.right) {
					//moveRight(2); // demi-tour (about turn)
					//moveForward();
					//actionr = "turn_right";
					moveBackward();
				} if(orientation==Orientation.left)
					moveForward();
			}
			else if (bestMove==1) {
				if(orientation == Orientation.up) {
					moveRight(1); // demi-tour (about turn)
					moveForward();
					//moveForward();
					actionr = "turn_right";
				} if(orientation == Orientation.right)
					moveForward();
				if(orientation==Orientation.down) {
					moveLeft(1);
					moveForward();
					actionr = "turn_right";
				} if(orientation==Orientation.left) {
					moveBackward();
					//moveLeft(1);
					//actionr = "turn_left";
				}
			}
			else if (bestMove==2) {
				if(orientation == Orientation.right) {
					moveRight(1); // demi-tour (about turn)
					moveForward();
					//moveForward();
					actionr = "turn_right";
				} if(orientation == Orientation.down)
					moveForward();
				if(orientation==Orientation.left) {
					moveLeft(1);
					moveForward();
					actionr = "turn_right";
				} if(orientation==Orientation.up) {
					moveBackward();
					//moveLeft(1);
					//actionr = "turn_left";
				}
			}
			else if(bestMove==3) {
				if(orientation == Orientation.left) {
					moveRight(1); // demi-tour (about turn)
					moveForward();
					//moveForward();
					actionr = "turn_right";
				}
				if(orientation == Orientation.up)
					moveForward();
				if(orientation==Orientation.right) {
					moveLeft(1);
					moveForward();
					actionr = "turn_right";
				} if(orientation==Orientation.down) {
					moveBackward();
					//moveLeft(1);
					//actionr = "turn_left";
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

	// Compute the smallest distance between a set of points and a point Po={x,y}
	public double distanceNearest( ArrayList< int[]> points, int x, int y) {

		if (points.size()==0)
			return -1;

		double lowestDist= Double.POSITIVE_INFINITY;
		//int lowestDist=0;
		for( int k=0; k<points.size(); k++) {
			int [] point= points.get(k);
			double dist= Math.sqrt((x-point[0])*(x-point[0])+(y-point[1])*(y-point[1]));
			if (dist<lowestDist)
				lowestDist=dist;
		}
		return lowestDist;
	}
}