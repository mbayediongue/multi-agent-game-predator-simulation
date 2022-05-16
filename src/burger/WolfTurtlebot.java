package burger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.ComponentType;
import model.EmptyCell;
import model.Situated;
import mqtt.Message;

public class WolfTurtlebot extends SmartTurtlebot {


	//protected List<Situated> grid;
	private double speed;
	private int changeOfOrientation = 3;


	public WolfTurtlebot(int id, String name, int seed, int field, Message clientMqtt, int debug, int speed) {
		super(id, name, seed, field, clientMqtt, debug);
		this.speed = speed;
	}


	/**
	 * Modif here
	 */
	public void randomOrientation() {
		double d = Math.random();
		String actionr = "move_forward";
		if (orientation == Orientation.up) {
			if (d < 0.33) {
				orientation = Orientation.up;
			} else if (d < 0.66) {
				orientation = Orientation.left;
				moveLeft(1);
				actionr = "turn_left";
			} else {
				orientation = Orientation.right;
			}
		} else if (orientation == Orientation.right) {
			if (d < 0.33) {
				orientation = Orientation.up;
			} else if (d < 0.66) {
				orientation = Orientation.down;
			} else {
				orientation = Orientation.right;
			}
		} else if (orientation == Orientation.left) {
			if (d < 0.33) {
				orientation = Orientation.up;
			} else if (d < 0.66) {
				orientation = Orientation.left;
			} else {
				orientation = Orientation.down;
			}
		} else {
			if (d < 0.33) {
				orientation = Orientation.down;
			} else if (d < 0.66) {
				orientation = Orientation.left;
			} else {
				orientation = Orientation.right;
			}
		}
	}

	public void changeOrientation(int index, double d, String actionr, EmptyCell[] ec) {
		changeOfOrientation = 3; //reinitialisation du compteur pour le futur changement d'orientation

		if (d < 0.33 && ec[index] != null) {
			moveForward(); // move one step ahead
		} else {
			if (d < 0.5) {
				moveLeft(1); //turn left
				actionr = "turn_left";
			} else {
				moveRight(1); //turn right
				actionr = "turn_right";
			}
		}

	}

	public void move(int step) {
		String actionr = "move_forward";
		String result = x + "," + y + "," + orientation + "," + grid.getCellsToString(y,x) + ",";

		for (int i = 0; i < step; i++) {
			EmptyCell[] ec = grid.getAdjacentEmptyCell(x,y);
			//System.out.println("myRobot (" + columns + "," + rows + "): " + getX() + " " + getY());
			int index_to_move=followRabbit();
			double d = Math.random();
			if (index_to_move==1000) {
				if (orientation == Orientation.up) {
					if (changeOfOrientation != 0 && ec[3] != null) {
						changeOfOrientation--;
						moveForward();
					} else {
						changeOrientation(3, d, actionr, ec);
					}
				} else if (orientation == Orientation.down) {
					if (changeOfOrientation != 0 && ec[2] != null) {
						changeOfOrientation--;
						moveForward();
					} else {
						changeOrientation(2, d, actionr, ec);
					}
				} else if (orientation == Orientation.right) {
					if (changeOfOrientation != 0 && ec[1] != null) {
						changeOfOrientation--;
						moveForward();
					} else {
						changeOrientation(1, d, actionr, ec);
					}
				} else if (orientation == Orientation.left) {
					if (changeOfOrientation != 0 && ec[0] != null) {
						changeOfOrientation--;
						moveForward();
					} else {
						changeOrientation(0, d, actionr, ec);
					}
				}
				if (debug == 2) {
					try {
						writer.write(result + actionr);
						writer.newLine();
						writer.flush();
					} catch (IOException ioe) {
						System.out.println(ioe);
					}
				}
			}else{
				{
					if (orientation == Orientation.up) {
						if (index_to_move==1) {
							moveRight();
						} else if (index_to_move==2){
							moveBackward();
						}else if (index_to_move==3){
							moveForward();
						}else{
							moveLeft();
						}
					} else if (orientation == Orientation.down) {
						if (index_to_move==0) {
							moveRight();
						} else if (index_to_move==3){
							moveBackward();
						}else if (index_to_move==2){
							moveForward();
						}else{
							moveLeft();
						}
					} else if (orientation == Orientation.right) {
						if (index_to_move==2) {
							moveRight();
						} else if (index_to_move==0){
							moveBackward();
						}else if (index_to_move==1){
							moveForward();
						}else{
							moveLeft();
						}
					} else if (orientation == Orientation.left) {
						if (index_to_move==3) {
							moveRight();
						} else if (index_to_move==1){
							moveBackward();
						}else if (index_to_move==0){
							moveForward();
						}else{
							moveLeft();
						}
					}
					if (debug == 2) {
						try {
							writer.write(result + actionr);
							writer.newLine();
							writer.flush();
						} catch (IOException ioe) {
							System.out.println(ioe);
						}
					}
				}
			}
		}
	}


	//Fonction qui renvoie l'index (0,1,2,ou 3) de la case adjacente à prendre pour être au plus proche du lapin.
	public int followRabbit (){
		int[] rabbit= grid.locateRabbit(x,y,field);
		int index_min=1000;//1000 est renvoyé par la fonction si la position du rabbit n'est pas connu du loup (rabbit={-1,-1})
		if (rabbit[0]!=-1 && rabbit[1]!=-1){
			EmptyCell[] ec=grid.getAdjacentEmptyCell(x,y);
			index_min=0;
			for (int i=1;i<4;i++){
				if (distanceToPoint(rabbit[0],rabbit[1],ec[index_min].getX(),ec[index_min].getY())>distanceToPoint(rabbit[0],rabbit[1],ec[i].getX(),ec[i].getY())){
					index_min=i;
				}
			}
		}
		return index_min;

	}
	public double distanceToPoint(int x_target,int y_target,int x_burger,int y_burger){
		return Math.sqrt(Math.pow(x_burger-x_target,2)+Math.pow(y_burger-y_target,2));
	}


}