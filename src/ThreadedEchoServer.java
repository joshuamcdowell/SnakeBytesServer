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
import java.util.Random;

import javax.swing.JFrame;
//RUN THIS ONE
public class ThreadedEchoServer extends JFrame implements Runnable{

    static final int PORT = 3000;
    private Thread updater;
    private static ArrayList<EchoThread> threads = new ArrayList<EchoThread>();
    private ArrayList<Snack> snacks = new ArrayList<Snack>();
    
    private int WIDTH = 1280;
    private int HEIGHT = 720;
    
    private int maxSnackSize;
    private boolean newSnack;
    
    private String IP;
    private ArrayList<String> scoreNames;
    private ArrayList<Integer> scores;

    public ThreadedEchoServer() {
    	updater = new Thread(this);
    	
    	setSize(WIDTH, HEIGHT);
    	setResizable(false);
    	setTitle("Snake Bytes Server");
    	setLocationRelativeTo(null);
    	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	
    	setVisible(true);
    	
    	updater.start();
    	
    	maxSnackSize = 25;
    	
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
			
			if(snacks.size() < maxSnackSize){
				Random rand = new Random();
				snacks.add(new Snack(rand.nextInt(76) + 2, rand.nextInt(39) + 2));
				newSnack = true;
			}
			
			System.out.print("");
			for(int i = 0; i < threads.size(); i++) {
				if(newSnack){
					threads.get(i).sendSnacks(snacks);
				}
				// Check if need to remove snack
				if(threads.get(i).canRemoveSnack()){
					String snackToRemove = threads.get(i).getRemoveSnack();
					// Send signal to other players and remove snack from server memory
					for(int j = 0; j < threads.size(); j++){
						threads.get(j).removeSnack(snackToRemove);
					}
					for(int z = 0; z < snacks.size(); z++){
						if(snacks.get(z).getID().equals(snackToRemove)){
							snacks.remove(z);
						}
					}
				}
				for(int j = 0; j < threads.size(); j++){
					if(i != j){
						if(threads.get(i).getPlayer() != null && threads.get(j).getPlayer() != null && threads.get(i).getPlayer().hasMoved()){	
							threads.get(i).sendPlayerInfo(threads.get(j).getPlayer());
						}
					}
				}
				if(threads.get(i).getInfo() == null) {
					threads.remove(i);
				}
			}
			
			calcScores();
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
		
		g.drawString("IP: " + IP, 895, 700);
		
		newFont = new Font("Monospaced", Font.BOLD, 15);
		g.setFont(newFont);
		
		for(int i = 0; i < threads.size(); i++){
			if(threads.get(i).getPlayer() != null){
				g.drawString(threads.get(i).getPlayer().getName() + "   :   " + threads.get(i).getPlayer().getX() + ":" + threads.get(i).getPlayer().getY(), 50, 100 + i * 40);
				try {
					g.drawString(scoreNames.get(i) + "    Score: " + (scores.get(i) + 1), 900, 100 + i * 40);
				} catch (IndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
		}
		bs.show();
	}
	
	// Method for calculating Leaderboard scores
	// Currently organizes 2 arrays at once
	// Probably a better way to do that :/
	public void calcScores() {
		scoreNames = new ArrayList<String>();
		scores = new ArrayList<Integer>();
		int temp;
		String tempName;
		for(int i = 0; i < threads.size(); i++) {
			temp = 0;
			tempName = "";
			if(threads.get(i).getPlayer() != null) {
				if (i == 0) {
					scores.add(Integer.parseInt(threads.get(i).getScore().substring(threads.get(i).getScore().indexOf(":") + 1)));
					scoreNames.add(threads.get(i).getPlayer().getName());
				}
				else if(scores.get(i-1) >= Integer.parseInt(threads.get(i).getScore())) {
					scores.add(Integer.parseInt(threads.get(i).getScore()));
					scoreNames.add(threads.get(i).getPlayer().getName());
				}
				else{
					temp = scores.get(i-1);
					tempName = scoreNames.get(i-1);
					scores.remove(i-1);
					scoreNames.remove(i-1);
					scores.add(Integer.parseInt(threads.get(i).getScore()));
					scoreNames.add(threads.get(i).getPlayer().getName());
					scores.add(temp);
					scoreNames.add(tempName);
				}
			}
		}
	}
}
