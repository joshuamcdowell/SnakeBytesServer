import java.util.Random;

public class Snack {

	private int x;
	private int y;
	
	private String id;
	
	public Snack(int x, int y){
		this.x = x;
		this.y = y;
		generateID();
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public String getID(){
		return id;
	}
	
	public void generateID(){ // Generates a unique 15 digit ID
		id = "";
		Random rand = new Random();
		for(int i = 0; i < 16; i++){
			id += rand.nextInt(10);
		}
	}
}
