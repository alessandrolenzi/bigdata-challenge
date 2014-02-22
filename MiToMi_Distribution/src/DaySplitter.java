import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;


public class DaySplitter extends DayAnalyzer {

	List<Writer> outFiles;
	private String filename;
	
	public DaySplitter(Scanner scan, Date date, String filename) {
		super(scan, date);
		this.filename = filename; 
		long periodsNumber = this.millisecondsInADay / this.aggregationPeriod;
		outFiles = new ArrayList<Writer>((int) periodsNumber);
		for(int i = 1; i <= periodsNumber; i++){
			FileOutputStream f = null;
			try {
				f = new FileOutputStream(filename + String.valueOf(i));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			outFiles.add(new BufferedWriter(new OutputStreamWriter(f)));
		}
	}

	public void appendResultLine (String s){
		result = result + "\n" + s;
	};
	
	@Override
	public void updateWith(double v, int periodId, Arc curr) {
		try {
			outFiles.get(periodId).write(String.format("%d\t%d\t%s\n", curr.getSourceId(), curr.getDestId(), v));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void finish() {
		for(int i = 0; i < (this.millisecondsInADay) / (aggregationPeriod); i++){
			try {
				outFiles.get(i).flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		appendResultLine("\n Parsed day. Output files are named " + filename + "<Id>");
		
	}}
