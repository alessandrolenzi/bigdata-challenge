import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.Callable;

public class DayAnalyzeWorker implements Callable<String> {

		String filename;
		String result;
		
		public DayAnalyzeWorker (String filename) {
			this.filename = filename;
		}
		

		public String call() throws Exception {
			Scanner s;
			DayAnalyzer an;
			
			// open file
			try{
				s = new Scanner(new BufferedReader(new InputStreamReader(new FileInputStream(filename))));
			} catch (Exception e){
				// handle exception
				return "FileNotFound " + filename;
			}
						
			
			String[] dateString = filename.split("-");
			String testDate = dateString[3].substring(0, 2) + "-" + dateString[2] + "-" + dateString[1] + ",00:00:00 AM";
			DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy,HH:mm:ss aaa");
			Date date = null;
			try {
				date = formatter.parse(testDate);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
						
			an = new DayAnalyzer(s, date);
			an.Analyse();
			
			return "== Using file: " + filename + " " + an.getResult();
		}
		
	}