package au.com.addstar.MCListener;

import java.text.DateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter
{
	private static DateFormat mFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
	
	@Override
	public String format( LogRecord record )
	{
		return String.format("[%s %s] %s\n", mFormat.format(record.getMillis()), record.getLevel().getLocalizedName(), String.format(record.getMessage(), record.getParameters()));
	}

}
