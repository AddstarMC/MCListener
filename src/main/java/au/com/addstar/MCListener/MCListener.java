package au.com.addstar.MCListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MCListener
{
	public static boolean pingAppearOffline;
	public static String pingMOTD;
	public static int pingCurPlayers;
	public static int pingMaxPlayers;
	public static String pingMcVersion;
	public static String[] pingDescription;
	public static String kickMessage;
	public static ServerIcon pingIcon;
	
	public static int port;
	
	public static Logger logger;
	
	private static void loadConfig() throws IOException
	{
		Properties props = new Properties();
		File file = new File("config.cfg");
		InputStream input;
		if(file.exists())
			input = new FileInputStream(file);
		else
			input = MCListener.class.getResourceAsStream("/config.cfg");
		
		props.load(input);
		input.close();
		
		String portStr = props.getProperty("server.port", "25565");
		try
		{
			port = Integer.parseInt(portStr);
			if(port < 0 || port > 65535)
				throw new IOException("ERROR: server.port is out of range. Needs to be 0 <= port <= 65535");
		}
		catch(NumberFormatException e)
		{
			throw new IOException("ERROR: server.port is not number. Needs to be 0 <= port <= 65535");
		}
		
		pingMcVersion = props.getProperty("ping.mcversion", "1.8.0");
		pingAppearOffline = Boolean.valueOf(props.getProperty("ping.offline"));
		pingMOTD = translate(props.getProperty("ping.motd", "The server is offline"));
		
		String ratio = props.getProperty("ping.players", "0/0");
		Pattern ratioPattern = Pattern.compile("^([0-9]+)/([0-9]+)$");
		Matcher ratioMatcher = ratioPattern.matcher(ratio);
		if(!ratioMatcher.matches())
			throw new IOException("ERROR: ping.players is not in the format of number/number");
		
		pingCurPlayers = Integer.valueOf(ratioMatcher.group(1));
		pingMaxPlayers = Integer.valueOf(ratioMatcher.group(2));
		
		pingDescription = translate(props.getProperty("ping.description", "")).split("\n");
		
		try
		{
			String filePath = props.getProperty("ping.icon", "server-icon.png");
			File iconFile = new File(filePath);
			if(iconFile.exists())
				pingIcon = new ServerIcon(iconFile);
			else if(!filePath.equals("server-icon.png"))
				logger.warning("Cannot find icon " + props.getProperty("ping.icon", "server-icon.png"));
		}
		catch(FileNotFoundException e)
		{
			if(!props.getProperty("ping.icon", "server-icon.png").equals("server-icon.png"))
				logger.warning("Cannot find icon " + props.getProperty("ping.icon", "server-icon.png"));
		}
		catch(IllegalArgumentException e)
		{
			throw new IOException("ERROR: ping.icon " + e.getMessage());
		}
		
		kickMessage = translate(props.getProperty("kick.message", "This server is offline"));
	}
	
	private static Pattern mColorPattern = Pattern.compile("&([a-f0-9lmnork])|(\\n)");
	private static String translate(String message)
	{
		Matcher matcher = mColorPattern.matcher(message);
		StringBuffer buffer = new StringBuffer();
		
		while(matcher.find())
		{
			if (matcher.group(1) != null)
				matcher.appendReplacement(buffer, "§$1");
			else if (matcher.group(2) != null)
				matcher.appendReplacement(buffer, "\n");
		}
		
		matcher.appendTail(buffer);
		
		return buffer.toString();
	}
	
	private static Pattern mStripPattern = Pattern.compile("§[a-f0-9lmnork]");
	public static String legacyPingMOTD(boolean colours)
	{
		String message = pingMOTD;
		if(message.contains("\n"))
			message = message.substring(0, message.indexOf("\n"));
		
		if(!colours && message.contains("§"))
		{
			Matcher m = mStripPattern.matcher(message);
			message = m.replaceAll("");
			message = message.replace("§", "");
		}
		
		return message;
	}
	
	public static void main(String[] args) throws IOException
	{
		logger = Logger.getLogger("MCListener");
		logger.setUseParentHandlers(false);
		Handler handler = new ConsoleHandler();
		handler.setFormatter(new LogFormatter());
		logger.addHandler(handler);
		
		try
		{
			loadConfig();
		}
		catch(IOException e)
		{
			System.err.println(e.getMessage());
			return;
		}
		
		ServerListener listener = new ServerListener(port);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while(true)
		{
			String line = reader.readLine();
			if(line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("q") || line.equalsIgnoreCase("stop"))
				break;
		}
		
		MCListener.logger.info("Shutting down...");
		listener.close();
	}
}
