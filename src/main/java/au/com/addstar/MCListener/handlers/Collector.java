package au.com.addstar.MCListener.handlers;

import java.util.List;

import au.com.addstar.MCListener.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class Collector extends ByteToMessageDecoder
{
	@Override
	protected void decode( ChannelHandlerContext ctx, ByteBuf in, List<Object> out ) throws Exception
	{
		in.markReaderIndex();
		
		byte[] data = new byte[3];
		for (int i = 0; i < data.length; ++i)
		{
			if (!in.isReadable())
			{
				in.resetReaderIndex();
				return;
			}
			
			data[i] = in.readByte();
			if (data[i] >= 0)
			{
				int size = Utils.readVarInt(data);
				
				if (in.readableBytes() < size)
				{
					in.resetReaderIndex();
					return;
				}
				out.add(in.readBytes(size));
				return;
			}
		}
		
		throw new RuntimeException("Size is too large!");
	}

}
