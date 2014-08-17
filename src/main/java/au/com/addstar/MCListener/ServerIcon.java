package au.com.addstar.MCListener;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.util.CharsetUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ServerIcon
{
	private String mIconString;
	
	public ServerIcon(File file) throws IOException
	{
		BufferedImage image = ImageIO.read(file);
		if(image.getWidth() != 64 || image.getHeight() != 64)
			throw new IllegalArgumentException("Server icon must be 64 x 64");
		
		ByteBuf buffer = Unpooled.buffer();
		ImageIO.write(image, "PNG", new ByteBufOutputStream(buffer));
		buffer = Base64.encode(buffer);
		
		mIconString = "data:image/png;base64," + buffer.toString(CharsetUtil.UTF_8);
	}
	
	public String getIconString()
	{
		return mIconString;
	}
}
