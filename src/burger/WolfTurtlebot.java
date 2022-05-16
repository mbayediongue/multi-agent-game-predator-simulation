package burger;

import java.io.IOException;

import model.ComponentType;
import model.EmptyCell;
import model.Situated;
import mqtt.Message;

public class WolfTurtlebot extends RandomTurtlebot {

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
		String result = x + "," + y + "," + orientation + ",";

		for (int i = 0; i < step; i++) {
			EmptyCell[] ec = new EmptyCell[4];
			ec[0] = null;
			ec[1] = null;
			ec[2] = null;
			ec[3] = null;
			//System.out.println("myRobot (" + columns + "," + rows + "): " + getX() + " " + getY());
			String st = "[";
			for (Situated s : grid) {
				//System.out.println("neighbour (" + s.getComponentType() + "): " + s.getX() + " " + s.getY());
				if (getX() > 0 && s.getX() == getX() - 1 && s.getY() == getY()) {
					if (s.getComponentType() == ComponentType.empty) {
						ec[2] = (EmptyCell) s;
					} else {
						ec[2] = null;
					}
				}

				if (getX() < columns - 1 && s.getX() == getX() + 1 && s.getY() == getY()) {
					if (s.getComponentType() == ComponentType.empty) {
						ec[3] = (EmptyCell) s;
					} else {
						ec[3] = null;
					}
				}

				if (getY() < rows - 1 && s.getY() == getY() + 1 && s.getX() == getX()) {
					if (s.getComponentType() == ComponentType.empty) {
						ec[1] = (EmptyCell) s;
					} else {
						ec[1] = null;
					}
				}

				if (getY() > 0 && s.getY() == getY() - 1 && s.getX() == getX()) {
					if (s.getComponentType() == ComponentType.empty) {
						ec[0] = (EmptyCell) s;
					} else {
						ec[0] = null;
					}
				}
				st += s.getX() + "," + s.getY() + ": " + s.display() + "; ";
			}
			st = st.substring(0, st.length() - 2);
			result += st + ",";

			double d = Math.random();

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
		}
	}


}