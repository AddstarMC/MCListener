package au.com.addstar.MCListener.handlers;

import au.com.addstar.MCListener.MCListener;
import au.com.addstar.MCListener.Utils;
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
			
			String version = MCListener.pingMcVersion;
			if(MCListener.pingAppearOffline)
				version = "Offline";
			
			int size = buffer.readableBytes();
			String message;
			switch(size)
			{
			case 0:
				MCListener.logger.info(String.format("%s pinged the server using < MC 1.3", Utils.getAddressString(ctx.channel().remoteAddress())));
				message = String.format("%s§%d§%d", MCListener.legacyPingMOTD(false), MCListener.pingCurPlayers, MCListener.pingMaxPlayers);
				break;
			case 1:
				if (buffer.readUnsignedByte() != 1)
					return;

				MCListener.logger.info(String.format("%s pinged the server using MC 1.4-1.5", Utils.getAddressString(ctx.channel().remoteAddress())));
				message = String.format("\u00a71\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", 127, version, MCListener.legacyPingMOTD(true), MCListener.pingCurPlayers, MCListener.pingMaxPlayers);
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

                MCListener.logger.info(String.format("%s pinged the server using MC 1.6", Utils.getAddressString(ctx.channel().remoteAddress())));
                message = String.format("\u00a71\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", 127, version, MCListener.legacyPingMOTD(true), MCListener.pingCurPlayers, MCListener.pingMaxPlayers);
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
	
	@Override
	public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause ) throws Exception
	{
		cause.printStackTrace();
		ctx.close();
	}
	
	private void sendPing(ChannelHandlerContext ctx, String message)
    {
		ByteBuf buffer = toByteBuff(message);
		ctx.pipeline().firstContext().writeAndFlush(buffer).addListener(ChannelFutureListener.CLOSE);
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
