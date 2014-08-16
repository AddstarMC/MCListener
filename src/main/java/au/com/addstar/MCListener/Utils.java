package au.com.addstar.MCListener;

import io.netty.buffer.ByteBuf;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

public class Utils
{
	public static final AttributeKey<Integer> connectionState = AttributeKey.valueOf("connection-state");
	public static int readVarInt(byte[] bytes)
	{
		int value = 0;
	    
	    for(int i = 0; i < bytes.length; ++i)
	    {
	    	value |= (bytes[i] & 0x7F) << i * 7;
	    	
	    	if((bytes[i] & 0x80) != 128)
	    		break;
	    }

	    return value; 
	}
	
	public static int readVarInt(ByteBuf buffer)
	{
		int value = 0;
	    int count = 0;
	    byte b = 0;
		do
		{
			b = buffer.readByte();
			value |= (b & 0x7F) << count++ * 7;
	    	
	    	if(count > 5)
	    		throw new IllegalArgumentException();
		}
		while(((b & 0x80) == 128));
		
		return value; 
	}
	
	public static void writeVarInt( ByteBuf buffer, int value )
	{
		while((value & 0xFFFFFF80) != 0)
		{
			buffer.writeByte(value & 0x7F | 0x80);
			value >>>= 7;
		}
		
		buffer.writeByte(value);
	}
	
	public static String readString(ByteBuf buffer)
	{
		int size = readVarInt(buffer);
		byte[] bytes = new byte[size];
		buffer.readBytes(bytes);
		return new String(bytes, CharsetUtil.UTF_8);
	}
	
	public static void writeString(ByteBuf buffer, String string)
	{
		byte[] data = string.getBytes(CharsetUtil.UTF_8);
		writeVarInt(buffer, data.length);
		buffer.writeBytes(data);
	}
	
	public static String createServerStatus(String motd, String mcVer, int mcProto, int currentPlayers, int maxPlayers)
	{
		return String.format("{\"version\": {\"name\": \"%s\",\"protocol\": %d},\"players\":{\"max\": %d,\"online\":%d},\"description\":{\"text\":\"%s\"}}", mcVer, mcProto, maxPlayers, currentPlayers, motd);
	}

	
}
