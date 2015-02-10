package au.com.addstar.MCListener.protocols;

import io.netty.handler.codec.ByteToMessageDecoder;

public class ProtocolManager
{
	public static ByteToMessageDecoder createProtocolHandler(int protocolVersion)
	{
		if (protocolVersion == 4 || protocolVersion == 5)
			return new MC1_7Handler();
		if (protocolVersion == 47)
			return new MC1_8Handler();
		
		return null;
	}
}
