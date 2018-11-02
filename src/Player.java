import java.util.ArrayList;

public class Player {

	private String name;
	private int skin;
	private boolean moved;
	
	//private int[][] body;
	private ArrayList<PlayerBody> body = new ArrayList<PlayerBody>();
	
	private int x;
	private int y;
	
	public Player(String name, int skin){
		this.name = name;
		this.skin = skin;
		moved = true;
		
		System.out.println("Creating player " + name + ":" + skin);
	}
	
	public void update(String info){
		int dx = Integer.parseInt(info.substring(info.indexOf('=') + 1, info.indexOf(',')));
		int dy = Integer.parseInt(info.substring(info.indexOf(',') + 1, info.indexOf("*")));
		
		if(dx == x && dy == y){
			moved = false;
		}
		else{
			moved = true;
		}
		
		int bodyLength = Integer.parseInt(info.substring(info.indexOf("*") + 1, info.indexOf("%")));
		if(bodyLength > 0){
			body = new ArrayList<PlayerBody>();
			String startString = "^";
			String endString = "$";
			
			for(int i = 0; i < bodyLength; i++){
				for(int j = 0; j < i; j++){
					startString += "^";
					endString += "$";
				}
				String coords = "";
				coords = info.substring(info.indexOf(startString) + 1, info.indexOf(endString));
				//System.out.println(info + "~~~~~~" + startString + ":" + endString);
				coords = coords.replace("^", "");
				coords = coords.replace("$", "");
				int bx = Integer.parseInt(coords.substring(0, coords.indexOf("#")));
				int by = Integer.parseInt(coords.substring(coords.indexOf("#") + 1));
				body.add(new PlayerBody(bx, by));
			}
		}
	}
	
	public String getName(){
		return name;
	}
	
	public int getSkin(){
		return skin;
	}
	
	public ArrayList<PlayerBody> getBody(){
		return body;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public boolean hasMoved(){
		return moved;
	}
}
