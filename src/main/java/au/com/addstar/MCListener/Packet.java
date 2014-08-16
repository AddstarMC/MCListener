package au.com.addstar.MCListener;

import io.netty.buffer.ByteBuf;

public abstract class Packet
{
	public abstract void write(ByteBuf buffer);
}
