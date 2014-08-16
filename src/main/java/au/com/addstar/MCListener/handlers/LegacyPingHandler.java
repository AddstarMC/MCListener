package au.com.addstar.MCListener.handlers;

import java.net.InetSocketAddress;

import au.com.addstar.MCListener.MCListener;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class LegacyPingHandler extends ChannelInboundHandlerAdapter
{
	@Override
	public void channelRead( ChannelHandlerContext ctx, Object msg ) throws Exception
	{
		ByteBuf buffer = (ByteBuf)msg;
		
		buffer.markReaderIndex();
		
		boolean needReset = true;
		
		try
		{
			if(buffer.readUnsignedByte() != 254)
				return;
			
			InetSocketAddress address = (InetSocketAddress)ctx.channel().remoteAddress();
		      
			int size = buffer.readableBytes();
			String message;
			switch(size)
			{
			case 0:
				System.out.println(String.format("Ping: (<1.3.x) from %s:%d", address.getAddress(), address.getPort()));
				message = String.format("%s§%d§%d", MCListener.pingMessage, MCListener.currentPlayers, MCListener.maxPlayers);
				break;
			case 1:
				if (buffer.readUnsignedByte() != 1)
					return;
				
				System.out.println(String.format("Ping: (1.4-1.5.x) from %s:%d", address.getAddress(), address.getPort()));
				message = String.format("\u00a71\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", 127, MCListener.mcVersion, MCListener.pingMessage, MCListener.currentPlayers, MCListener.maxPlayers);
				break;
			default:
				boolean flag1 = buffer.readUnsignedByte() == 1;
                flag1 &= buffer.readUnsignedByte() == 250;
                flag1 &= "MC|PingHost".equals(new String(buffer.readBytes(buffer.readShort() * 2).array(), CharsetUtil.UTF_16BE));
                int j = buffer.readUnsignedShort();
                flag1 &= buffer.readUnsignedByte() >= 73;
                flag1 &= 3 + buffer.readBytes(buffer.readShort() * 2).array().length + 4 == j;
                flag1 &= buffer.readInt() <= 65535;
                flag1 &= buffer.readableBytes() == 0;

                if (!flag1)
                    return;

                System.out.println(String.format("Ping: (1.6.X) from %s:%d", address.getAddress(), address.getPort()));
                message = String.format("\u00a71\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", 127, MCListener.mcVersion, MCListener.pingMessage, MCListener.currentPlayers, MCListener.maxPlayers);
                break;
			}
			
			sendPing(ctx, message);
			
			needReset = false;
			
			buffer.release();
		}
		finally
		{
			if (needReset)
			{
				buffer.resetReaderIndex();
				ctx.channel().pipeline().remove("legacy_query");
				ctx.fireChannelRead(msg);
			}
		}
	}
	
	private void sendPing(ChannelHandlerContext ctx, String message)
    {
		ByteBuf buffer = toByteBuff(message);
        ctx.channel().writeAndFlush(buffer).addListener(ChannelFutureListener.CLOSE);
    }

    private ByteBuf toByteBuff(String message)
    {
        ByteBuf bytebuf = Unpooled.buffer();
        bytebuf.writeByte(255);
        char[] achar = message.toCharArray();
        bytebuf.writeShort(achar.length);
        char[] achar1 = achar;
        int i = achar.length;

        for (int j = 0; j < i; ++j)
        {
            char c0 = achar1[j];
            bytebuf.writeChar(c0);
        }

        return bytebuf;
    }
}
