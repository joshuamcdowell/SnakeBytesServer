import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.JFrame;
//RUN THIS ONE
public class ThreadedEchoServer extends JFrame implements Runnable{

    static final int PORT = 3000;
    private Thread updater;
    private static ArrayList<EchoThread> threads = new ArrayList<EchoThread>();
    
    private int WIDTH = 1280;
    private int HEIGHT = 720;
    
    private String IP;

    public ThreadedEchoServer() {
    	updater = new Thread(this);
    	
    	setSize(WIDTH, HEIGHT);
    	setResizable(false);
    	setTitle("Snake Bytes Server");
    	setLocationRelativeTo(null);
    	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	
    	setVisible(true);
    	
    	updater.start();
    	
    	
    	// Get IP
    	InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getLocalHost();
			IP = inetAddress.getHostAddress();
			System.out.println("IP Address:- " + inetAddress.getHostAddress());
	        System.out.println("Host Name:- " + inetAddress.getHostName());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void main(String args[]) {
        ServerSocket serverSocket = null;
        Socket socket = null;

        ThreadedEchoServer server = new ThreadedEchoServer();
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();

        }
        while (true) {
        	//System.out.println("test");
            try {
                socket = serverSocket.accept();
                System.out.print("CLIENT CONNECTED\n");
                // new thread for a client
                EchoThread newThread = new EchoThread(socket);
                newThread.start();
                threads.add(newThread);
                
            } catch (Exception e) {
                System.out.println("I/O error: " + e);
            }
        }
    }

	@Override
	public void run() {
		while(true) {
			System.out.print("");
			for(int i = 0; i < threads.size(); i++) {
				for(int j = 0; j < threads.size(); j++){
					if(i != j){
						if(threads.get(i).getPlayer() != null && threads.get(j).getPlayer() != null){	
							threads.get(i).sendPlayerInfo(threads.get(j).getPlayer());
						}
					}
				}
				//System.out.println(threads.get(i).isConnected());
				//System.out.println(threads.get(i).getInfo());
				if(threads.get(i).getInfo() == null) {
					threads.remove(i);
				}
			}
			
			render();
		}
	}
	
	public void render(){
		BufferStrategy bs = getBufferStrategy();
		if(bs == null){
			createBufferStrategy(3);
			return;
		}
		
		Graphics g = bs.getDrawGraphics();
		
		// Draw stuff
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, (int)(WIDTH * .66), HEIGHT);
		
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect((int)(WIDTH * .66), 0, (int)(WIDTH * .4), HEIGHT);
		
		Font newFont = new Font("Monospaced", Font.BOLD, 30);
		g.setFont(newFont);
		
		g.setColor(Color.BLACK);
		g.drawString("Connected Players", 260, 65);	
		g.drawString("Leaderboard", 970, 65);
		g.drawLine(0, 73, WIDTH, 73);
		
		g.drawString("IP @: " + IP, 885, 700);
		
		newFont = new Font("Monospaced", Font.BOLD, 15);
		g.setFont(newFont);
		
		for(int i = 0; i < threads.size(); i++){
			if(threads.get(i).getPlayer() != null){
	
				g.drawString(threads.get(i).getPlayer().getName() + "   :   " + threads.get(i).getPlayer().getX() + ":" + threads.get(i).getPlayer().getY(), 50, 100 + i * 40);
			}
		}
		bs.show();
	}
}
