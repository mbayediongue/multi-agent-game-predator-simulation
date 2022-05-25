package model;

public abstract class EntityDescriptor implements Situated{
	
	protected int y, x;
	public int y_food_last, x_food_last;
	//boolean which says if the burger is "died" or not.
	protected boolean isDead=false;
	
	public EntityDescriptor (int [] location) {
		this.x= location[0];
		this.y = location[1];
	}
	public void setDead(boolean b){
		this.isDead=b;
	}
	public boolean getDead(){
		return this.isDead;
	}
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}
}
