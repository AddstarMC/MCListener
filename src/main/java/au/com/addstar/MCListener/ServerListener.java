package au.com.addstar.MCListener;

import au.com.addstar.MCListener.handlers.Collector;
import au.com.addstar.MCListener.handlers.Handshaker;
import au.com.addstar.MCListener.handlers.LegacyPingHandler;
import au.com.addstar.MCListener.handlers.Prepender;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ServerListener
{
	private EventLoopGroup mBoss;
	private EventLoopGroup mWorker;
	
	public ServerListener(int port)
	{
		mBoss = new NioEventLoopGroup();
		mWorker = new NioEventLoopGroup();
		
		ServerBootstrap builder = new ServerBootstrap();
		builder.group(mBoss, mWorker)
			.channel(NioServerSocketChannel.class)
			.childHandler(new ChannelInitializer<SocketChannel>()
			{
				@Override
				protected void initChannel( SocketChannel ch ) throws Exception
				{
					ch.pipeline().addLast("legacy_query", new LegacyPingHandler()).addLast("splitter", new Collector()).addLast("decoder", new Handshaker()).addLast("prepender", new Prepender());
					ch.attr(Utils.connectionState).set(0);
				}
			})
			.option(ChannelOption.SO_BACKLOG, 128)
			.childOption(ChannelOption.SO_KEEPALIVE, true);
		
		builder.bind(port).syncUninterruptibly();
	}
	
	public void close()
	{
		mWorker.shutdownGracefully().syncUninterruptibly();
		mBoss.shutdownGracefully().syncUninterruptibly();
	}
}
