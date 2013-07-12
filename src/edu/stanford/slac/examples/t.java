package edu.stanford.slac.examples;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
public class t {

	
	public static void main(String[] args) throws Exception {
		String s =     "   \"-disabled from \"ZW\" by jerrodw 12/14/06 not pingable.   \" ";
		
		s = s.replaceAll("\"(.*)\"(.*)\"(.*)\"", "\"$1'$2'$3\"");
		//s = s.replaceAll("\"(.*)\"(.*)\"", "\"$1'$2\"");
		System.out.println(s);
	
		
	}

}
