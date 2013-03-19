package pl.papaya.bot;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

abstract public class Log {

	protected String logName = "";

	protected void write(String file, String message) {

		try {
			
			FileWriter fstream;
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			
			fstream = new FileWriter(file, true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(dateFormat.format(cal.getTime()).toString()+" - "+message+"\r\n");
			out.close();
		
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
