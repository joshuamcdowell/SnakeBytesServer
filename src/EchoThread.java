import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class EchoThread extends Thread {
    protected Socket socket;
    
    private DataOutputStream out;
    
    private String information = "";
    private boolean connected;
    
    private Player player;

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
                	String deadEnemy = line.substring(line.indexOf(":") + 1);
                	if(player.getName().equals(deadEnemy)){
                		// Send message to all other clients to remove from enemy list
                		sendDeathMessage();
                	}
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
    
    public void sendDeathMessage(){
    	try {
    		removePlayer();
    		System.out.println("sending player death message");
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
}