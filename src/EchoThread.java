import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

public class EchoThread extends Thread {
    protected Socket socket;
    
    private DataOutputStream out;
    
    private String information = "";
    private boolean connected;
    private String score = "0";
    
    private Player player;
    
    private boolean removeSnack;
    private String snackToRemove;

    public EchoThread(Socket clientSocket) {
        this.socket = clientSocket;
    }

    public void run() {
        InputStream inp = null;
        BufferedReader brinp = null;
        out = null;
        connected = true;
        try {
            inp = socket.getInputStream();
            brinp = new BufferedReader(new InputStreamReader(inp));
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            return;
        }
        String line;
        while (true) {
            try {
                line = brinp.readLine();
                information = line;
                //System.out.println(line);
                if ((line == null) || line.equalsIgnoreCase("QUIT")) {
                	connected = false;
                    socket.close();
                    return;
                }
                else if(line.contains("JOIN:")){
                	String name = line.substring(line.indexOf(':') + 1, line.indexOf(';'));
                	
                	int skin = Integer.parseInt(line.substring(line.indexOf(';') + 1));
                	player = new Player(name, skin);
                }
                else if(line.contains("UPDATE:")){
                	player.update(line);
                }
                else if(line.contains("DEATH:")){
                	score = "0";
                	String deadEnemy = line.substring(line.indexOf(":") + 1);
                	if(player.getName().equals(deadEnemy)){
                		// Send message to all other clients to remove from enemy list
                		sendDeathMessage();
                	}
                }
                else if (line.contains("SCORE:")) {	//Score info sent!
                	score = line.substring(line.indexOf(':') + 1);
                }
                else if(line.contains("EATEN:")){
                	snackToRemove = line.substring(line.indexOf(":") + 1);
                	removeSnack = true;
                }
                else {
                    out.writeBytes(line + "\n\r");
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }
    
    public String getRemoveSnack(){
    	removeSnack = false;
    	return snackToRemove;
    }
    
    public boolean canRemoveSnack(){
    	return removeSnack;
    }
    
    public void removeSnack(String snackToRemove){
    	try {
    		out.writeBytes("SNACKREMOVE:" + snackToRemove + "\n\r");
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }
    
    public void sendDeathMessage(){
    	try {
    		removePlayer();
			out.writeBytes("DEATH:" + player.getName() + "\n\r");
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void sendPlayerInfo(Player p){
    	try {
    		String body = "";
    		String startString = "^";
    		String endString = "$";
    		
    		for(int i = 0; i < p.getBody().size(); i++){
    			body += startString;
    			body += p.getBody().get(i).getX() + "#" + p.getBody().get(i).getY();
    			body += endString;
    			startString += "^";
        		endString += "$";
    		}
			out.writeBytes("PMOVE:" + p.getName()+ "*" + p.getSkin() + "=" + p.getX() + ";" + p.getY() + ")" + p.getBody().size() + "%" + body + "\n\r");
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} catch(IndexOutOfBoundsException e) {
    		e.printStackTrace();
    	}
    }
    
    public void sendSnacks(ArrayList<Snack> snacks){
    	
    	if(snacks.size() <= 0){
    		return;
    	}
    	try{
    		String message = "";
    		for(int i = 0; i < snacks.size(); i++){
    			message += "*";
    			message += snacks.get(i).getID();
    			message += "#";
    			message += snacks.get(i).getX();
    			message += ":";
    			message += snacks.get(i).getY();
    			message += "$";
    		}
    		if(out != null){
    			out.writeBytes("SNACKS:" + message + "\n\r");
        		out.flush();
    		}
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }
    
    public void removePlayer(){
    	player = new Player(player.getName(), player.getSkin());
    }
    
    public String getInfo() {
    	return information;
    }
    
    public boolean isConnected(){
    	return connected;
    }
    
    public Player getPlayer(){
    	return player;
    }
    
    public String getScore() {
    	return score;
    }
}