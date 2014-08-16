package au.com.addstar.MCListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MCListener
{
	public static String pingMessage = "Ping test";
	public static int currentPlayers = 0;
	public static int maxPlayers = 0;
	public static String mcVersion = "Offline";
	public static int mcProtocol = 0;
	public static String dcMessage = "The server is offline";
	
	public static void main(String[] args) throws IOException
	{
		ServerListener listener = new ServerListener(25566);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while(true)
		{
			String line = reader.readLine();
			if(line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("q") || line.equalsIgnoreCase("stop"))
				break;
		}
		listener.close();
	}
}
