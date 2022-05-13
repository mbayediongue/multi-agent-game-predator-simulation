package components;

import model.ComponentType;
import model.Situated;
import org.json.simple.JSONObject;

import static model.ComponentType.burrow;

// this case represents the hole of the rabbit : the Burrow.
public class Burrow implements Situated {
    protected int x, y;

    public Burrow(int [] location) {
        this.x = location[0];
        this.y = location[1];
    }

    @Override
    public ComponentType getComponentType() {
        return ComponentType.burrow;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setX(int x) {
        this.x = x;

    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String display() {
        return "B";
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jo = new JSONObject();
        jo.put("type","burrow");
        jo.put("x",""+x);
        jo.put("y",""+y);
        return jo;
    }

    public String toString() {
        return "{type: " + getComponentType() + ", x: " + x  + ", y: " + y + "}";
    }
}
