package au.com.addstar.MCListener.handlers;

import au.com.addstar.MCListener.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class Prepender extends MessageToByteEncoder<ByteBuf>
{
	@Override
	protected void encode( ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out ) throws Exception
	{
		int size = msg.readableBytes();
		
		Utils.writeVarInt(out, size);
		out.writeBytes(msg, msg.readerIndex(), size);
	}

}
