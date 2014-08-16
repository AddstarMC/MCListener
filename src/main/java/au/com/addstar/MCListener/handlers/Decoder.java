package au.com.addstar.MCListener.handlers;

import java.util.List;

import au.com.addstar.MCListener.MCListener;
import au.com.addstar.MCListener.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class Decoder extends ByteToMessageDecoder
{
	@Override
	protected void decode( ChannelHandlerContext ctx, ByteBuf in, List<Object> out ) throws Exception
	{
		int byteSize = in.readableBytes();
		if(byteSize == 0)
		{
			System.out.println("exit");
			return;
		}
		
		int id = Utils.readVarInt(in);
		int conState = ctx.channel().attr(Utils.connectionState).get();

		System.out.println("id: " + id + " con: " + conState);
		switch(conState)
		{
		case 0: // Handshake (initial)
			ctx.channel().attr(Utils.connectionState).set(readHandshakePacket(in));
			break;
		case 1: // Status
			switch(id)
			{
			case 0: // Status
				send(ctx, writeStatusPacket());
				break;
			case 1: // Ping
				send(ctx, writePongPacket(in.readLong()));
				break;
			}
			break;
		case 2: // Login
			disconnect(ctx, "Test");
			break;
		}
		
	}
	
	private int readHandshakePacket(ByteBuf in)
	{
		int protocol = Utils.readVarInt(in);
		String address = Utils.readString(in);
		int port = in.readUnsignedShort();
		int next = Utils.readVarInt(in);
		
		System.out.println(String.format("Handshake: proto=%d address=%s:%d next=%d", protocol, address, port, next));
		return next;
	}
	
	private void disconnect(ChannelHandlerContext ctx, String message)
	{
		String text = String.format("{\"text\":\"%s\"}", message);
		
		ByteBuf buffer = Unpooled.buffer();
		Utils.writeVarInt(buffer, 0);
		Utils.writeString(buffer, text);
		
		ctx.channel().writeAndFlush(buffer).addListener(ChannelFutureListener.CLOSE);
	}
	
	private void send(ChannelHandlerContext ctx, ByteBuf buffer)
	{
		ctx.channel().writeAndFlush(buffer);
	}
	
	private ByteBuf writeStatusPacket()
	{
		String status = Utils.createServerStatus(MCListener.pingMessage, MCListener.mcVersion, MCListener.mcProtocol, MCListener.currentPlayers, MCListener.maxPlayers);
		System.out.println("Writing status: " + status);
		
		ByteBuf buffer = Unpooled.buffer();
		Utils.writeVarInt(buffer, 0);
		Utils.writeString(buffer, status);
		return buffer;
	}
	
	private ByteBuf writePongPacket(long time)
	{
		System.out.println("Writing ping: " + time);
		
		ByteBuf buffer = Unpooled.buffer();
		Utils.writeVarInt(buffer, 1);
		buffer.writeLong(time);
		return buffer;
	}
}
