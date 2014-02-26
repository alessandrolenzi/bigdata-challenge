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

// TODO usare seconds o milliseconds?
// TODO cosa fare di ore vuote?


public class DayAnalyzer {

	/* ************* CLASS FIELDS ******** */
	
	long initialTime, timestamp;
	
	List<Writer> outFiles;
	String filename;
	
	/* double minStrength = Double.MAX_VALUE, maxStrength = Double.MIN_VALUE;
	double dayMean = 0, dayM2 = 0;*/
	int records = 0; //, periods = 0, periodId = 0;
	Statistics stat = null;
	
	double periodSum = 0;
	
	// 0: print only final statistics
	// 1: print also discovered arcs, aggredated by periods
	int verbose = 0;

	Scanner s;		
	Arc currentArc = null, lastArc = null;
	Date d;
	
	protected String result = "";
	
	/* ***************** CONSTANTS  ****************** */
	
	int millisecondsInADay = 24 * 60 * 60 * 1000; 
	
	// 10 minutes in milliseconds 
	int timeInterval = 10 * 60 * 1000;

	// aggregate data in 1 hour
	int aggregationPeriod = 60 * 60 * 1000;
	
	int intervalsInPeriod = aggregationPeriod / timeInterval;

	// private long nextPeriod; 
	
	
	/* **** COSTRUCTOR AND UTILITY METHODS ****** */
	
	/** Constructor
	 * 	
	 * */
	public DayAnalyzer (Scanner scan, Date date, String filename){
		this.initialTime = date.getTime();
		d = date;
		s = scan;
		
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
	
	public void setVerbose(int verbose) {
		this.verbose = verbose;
	}
	
	public String getResult() {
		return result;
	}
	
	protected void appendResultLine (String s){
		result = result + "\n" + s;
	}
	
	/* ************** ANALYSING METHODS **************** */
	
	private void readArc () {
		String n = s.nextLine();
		this.lastArc = this.currentArc;
		this.currentArc = new Arc(n);
		records++;
		
		// System.out.println(n);
	}
	
	
	public void endPeriod(double value, int periodId){
		try {
			outFiles.get(periodId).write(String.format("\t%d:%s", currentArc.getDestId(),value));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stat.update(value);
	}
	// 
	private void endSource() {
		for(Writer f : outFiles){
			try {
				f.write(String.format("\n%d", currentArc.getSourceId()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	private void initSource() {
		for(Writer f : outFiles){
			try {
				f.write(String.format("%d", currentArc.getSourceId()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public void endArc(){
		
	}
	
	public void Analyse () {
		appendResultLine("== Analyze day " + d.toString());
		
		stat = new Statistics();
		
		// First arc is to be examined alone (no previous arc)
		if(! s.hasNextLine()) return; 
		readArc();
		this.periodSum = currentArc.getStrength();
		initSource();
		
		while(s.hasNextLine()){
			readArc();
			if(currentArc.getSourceId() == lastArc.getSourceId()){
				// same source node
			
				   if (currentArc.getDestId() == lastArc.getDestId()){
					   // same source and dest
					   if(currentArc.getTimestamp() / aggregationPeriod == lastArc.getTimestamp() / aggregationPeriod){
							// same source and dest and same period -> sum
							this.periodSum += currentArc.getStrength();
					   } else {
						   // same source and dest, different period
						   endPeriod(periodSum, lastArc.getPeriodId(initialTime, aggregationPeriod));
						   this.periodSum = currentArc.getStrength();
						   /* if(verbose > 0)
								appendResultLine(lastArc.prettyHeadTail() + "\t\t" + 
												 lastArc.getPeriodId(initialTime, aggregationPeriod) + 
												 "\t" + 
												 periodSum); */
					   }
				   } else {
					   // same source, different dest
					   endArc();
				}
			} else {
				// different source
				endSource();
				// appendResultLine("endSource");
			}
		}
		endPeriod(periodSum, currentArc.getPeriodId(initialTime, aggregationPeriod));
		
		finish();
		
		
	}	

	protected void finish(){
		
		appendResultLine("\n\trecords:" + this.records);
		appendResultLine(stat.prettyPrinted());
		
		for(int i = 0; i < (this.millisecondsInADay) / (aggregationPeriod); i++){
			try {
				outFiles.get(i).write("\n");
				outFiles.get(i).flush(); 
			} catch (IOException e) { e.printStackTrace(); }
		}
		appendResultLine("\n Parsed day. Output files are named " + filename + "<Id>");
		
	}
	
}
