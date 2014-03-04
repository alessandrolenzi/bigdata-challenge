import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;


public class DayCallsWorker implements Callable<String> {

	private String filename;
	private long aggregationPeriod = 60 * 60 * 1000;
	private long dayTime = 24 * 60 * 60 * 1000;
	private List<Writer> outFiles;

	int records = 0;
	private String day;
	private String month;
	private String year;
	Date date = null;
	
	Statistics stat;
	
	TelcoRecord lastRec, currentRec;
	
	class TelcoRecord {
		public int SquareId = -1;
		public long timestamp = -1;
		public int Country = -1;
		public double SMS_In = -1, SMS_Out = -1, CallsIn = -1, CallsOut = -1, Internet = -1;
		
		// record format is SquareID Timestamp Country SMS-IN SMS-OUT CALL-IN CALL-OUT INTERNET
		
		public TelcoRecord(String s) {
			String[] l = s.split("\t", -1);
			if(l[0].length() != 0)
				this.SquareId = Integer.parseInt(l[0]);
			if(l[1].length() != 0)
				this.timestamp = Long.parseLong(l[1]);
			if(l[2].length() != 0)
				this.Country = Integer.parseInt(l[2]);
			if(l[3].length() != 0)
				this.SMS_In = Double.parseDouble(l[3]);
			if(l[4].length() != 0)
				this.SMS_Out = Double.parseDouble(l[4]);
			if(l[5].length() != 0)
				this.CallsIn = Double.parseDouble(l[5]);
			if(l[6].length() != 0)
				this.CallsOut = Double.parseDouble(l[6]);
			if(l[7].length() != 0)
				this.Internet = Double.parseDouble(l[7]);
		}
	}
	
	public DayCallsWorker(String filename) {
		this.filename = filename;
	}
	
	@Override
	public String call() throws Exception {
		Scanner s;
		double periodSum;
		
		// open file
		try{
			//s = new CustomReader(filename, 1024*1024*10L, 1024, new char[]{'\n'});
			s = new Scanner(new BufferedReader(new InputStreamReader(new FileInputStream(filename))));
		} catch (Exception e){
			// handle exception
			return "FileNotFound " + filename;
		}
					
		outFiles = new ArrayList<Writer>((int)(dayTime / aggregationPeriod));
		for(int i = 0; i < (dayTime / aggregationPeriod); i++){
			FileOutputStream f = null;
			try {
				f = new FileOutputStream(filename + "-" + String.valueOf(i));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			outFiles.add(new BufferedWriter(new OutputStreamWriter(f))); 
			
		}
		
		stat = new Statistics();
		
		String[] dateString = filename.split("-");
		// filename format is sms-call-internet-mi-2013-11-01.txt
		day = dateString[6].substring(0, 2);
		month = dateString[5];
		year = dateString[4];
		String testDate =  day + "-" + month  + "-" + year + ",00:00:00 AM";
		DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy,HH:mm:ss aaa");
		
		try {
			date = formatter.parse(testDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		periodSum = 0;
		
		boolean found = false;
		while(s.hasNextLine() && !found){
			currentRec = new TelcoRecord(s.nextLine());
			if(currentRec.CallsOut != -1){
				found = true;
				periodSum = currentRec.CallsOut;
			}
		}
		
		while(s.hasNextLine()){
			lastRec = currentRec;
			currentRec = new TelcoRecord(s.nextLine());
			records++;
			
			if(currentRec.CallsOut != -1){
				if(currentRec.SquareId == lastRec.SquareId && period(currentRec.timestamp)== period(lastRec.timestamp)) {
					periodSum += currentRec.CallsOut;
				} else {
					stat.update(periodSum);
					long periodId = period(lastRec.timestamp);
					// System.out.println(String.format("ts: %d \t dt: %d \t pi: %d", lastRec.timestamp, date.getTime(), periodId));
					outFiles.get((int) (periodId)).write(lastRec.SquareId + "\t" + periodSum + "\n");
					periodSum = currentRec.CallsOut;
				}
			}
		}

		for(Writer w : outFiles){
			w.flush();
			w.close();
		}
		
		s.close();
		return getResult();
	}

	private int period(long timestamp) {
		return (int) ((timestamp - this.date.getTime()) / aggregationPeriod);
	}

	private String getResult() {
		return String.format("%s-%s-%s\t", day,month,year) + stat.asRecord() + "\t" + records + "\n";
	}

}
