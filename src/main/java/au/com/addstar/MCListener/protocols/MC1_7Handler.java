package au.com.addstar.MCListener.protocols;

import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import au.com.addstar.MCListener.MCListener;
import au.com.addstar.MCListener.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class MC1_7Handler extends ByteToMessageDecoder
{
	@Override
	protected void decode( ChannelHandlerContext ctx, ByteBuf in, List<Object> out ) throws Exception
	{
		int byteSize = in.readableBytes();
		if(byteSize == 0)
			return;
		
		int id = Utils.readVarInt(in);
		int conState = ctx.channel().attr(Utils.connectionState).get();

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
			disconnect(ctx, MCListener.kickMessage);
			break;
		}
		
	}
	
	private int readHandshakePacket(ByteBuf in)
	{
		Utils.readVarInt(in);
		Utils.readString(in);
		in.readUnsignedShort();
		int next = Utils.readVarInt(in);
		
		return next;
	}
	
	@SuppressWarnings( "unchecked" )
	private void disconnect(ChannelHandlerContext ctx, String message)
	{
		JSONObject root = new JSONObject();
		root.put("text", message);
		
		ByteBuf buffer = Unpooled.buffer();
		Utils.writeVarInt(buffer, 0);
		Utils.writeString(buffer, root.toJSONString());
		
		ctx.channel().writeAndFlush(buffer).addListener(ChannelFutureListener.CLOSE);
	}
	
	private void send(ChannelHandlerContext ctx, ByteBuf buffer)
	{
		ctx.channel().writeAndFlush(buffer);
	}
	
	@SuppressWarnings( "unchecked" )
	private ByteBuf writeStatusPacket()
	{
		JSONObject root = new JSONObject();
		JSONObject version = new JSONObject();
		if(MCListener.pingAppearOffline)
		{
			version.put("name", "Offline");
			version.put("protocol", 0);
		}
		else
		{
			version.put("name", "1.7.10");
			version.put("protocol", 5);
		}
		
		JSONObject players = new JSONObject();
		players.put("max", MCListener.pingMaxPlayers);
		players.put("online", MCListener.pingCurPlayers);
		if(MCListener.pingDescription.length > 0)
		{
			JSONArray sample = new JSONArray();
			for(String line : MCListener.pingDescription)
			{
				UUID id = UUID.nameUUIDFromBytes(line.getBytes());
				JSONObject samplePart = new JSONObject();
				samplePart.put("name", line);
				samplePart.put("id", id.toString());
				sample.add(samplePart);
			}
			players.put("sample", sample);
		}
		
		root.put("version", version);
		root.put("players", players);
		root.put("description", MCListener.pingMOTD);
		
		ByteBuf buffer = Unpooled.buffer();
		Utils.writeVarInt(buffer, 0);
		Utils.writeString(buffer, root.toJSONString());
		return buffer;
	}
	
	private ByteBuf writePongPacket(long time)
	{
		ByteBuf buffer = Unpooled.buffer();
		Utils.writeVarInt(buffer, 1);
		buffer.writeLong(time);
		return buffer;
	}
}
