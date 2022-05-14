package environnementburger;

public class Slab {
	private String id;
	private int width;
	private int height;
	private int positionX;
	private int positionY;
	
	public Slab(String id, int width, int height, int positionX, int positionY)
	{
		this(id,width,height);
		this.positionX = positionX;
		this.positionY = positionY;
	}
	
	public Slab(String id, int width, int height)
	{
		this.id = id;
		this.width = width;
		this.height = height;
	}

	public void setPositionX(int positionX)
	{
		this.positionX = positionX;
	}
	
	public void setPositionY(int positionY)
	{
		this.positionY = positionY;
	}

	public String getId()
	{
		return id;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public int getPositionX()
	{
		return positionX;
	}
	
	public int getPositionY()
	{
		return positionY;
	}
	
	public String toString()
	{
		return "[Slab] id: " + id + " - width: " + width + " - height: " + height + " - position: " + positionX + "," + positionY;
	}
}
