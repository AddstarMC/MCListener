package au.com.addstar.MCListener.handlers;

import java.util.List;

import au.com.addstar.MCListener.MCListener;
import au.com.addstar.MCListener.Utils;
import au.com.addstar.MCListener.protocols.ProtocolManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class Handshaker extends ByteToMessageDecoder
{
	@Override
	protected void decode( ChannelHandlerContext ctx, ByteBuf in, List<Object> out ) throws Exception
	{
		int byteSize = in.readableBytes();
		if(byteSize == 0 || !ctx.channel().isOpen())
			return;
		
		int id = Utils.readVarInt(in);
		
		
		if (id != 0)
		{
			in.skipBytes(in.readableBytes());
			throw new IllegalStateException();
		}
		else
		{
			int protocol = Utils.readVarInt(in);
			Utils.readString(in);
			in.readUnsignedShort();
			int next = Utils.readVarInt(in);
			
			ByteToMessageDecoder handler = ProtocolManager.createProtocolHandler(protocol);
			if(handler == null)
			{
				MCListener.logger.warning(Utils.getAddressString(ctx.channel().remoteAddress()) + " tried to use unknown protocol " + protocol);
				in.skipBytes(in.readableBytes());
				throw new IllegalStateException();
			}
			else
			{
				ctx.channel().attr(Utils.connectionState).set(next);
				ctx.channel().pipeline().replace("decoder", "decoder", handler);
			}
		}
	}
	
	@Override
	public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause ) throws Exception
	{
		if(!(cause.getCause() instanceof IllegalStateException))
			cause.printStackTrace();
		
		ctx.close();
	}
}
