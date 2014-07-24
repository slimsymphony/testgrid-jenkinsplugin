package frank.incubator.testgrid.ciplugin.logging;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
	private PrintStream consolePrinter = null;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("[yyyy-MM-dd hh:mm:ss:SSS]");

	public Logger(PrintStream logger) {
		this.consolePrinter = logger;
	}

	public PrintStream getLogger() {
		return this.consolePrinter;
	}

	public synchronized void log(String message) {
		consolePrinter.println(message);
		consolePrinter.flush();
	}
	public synchronized void log(String clazzName, String message) {
		consolePrinter.println(getTimeStamp() +" "+"["+clazzName+"]"+ " " + message);
		consolePrinter.flush();
	}

	private static synchronized final String getTimeStamp() {
		return sdf.format(new Date());
	}
}
