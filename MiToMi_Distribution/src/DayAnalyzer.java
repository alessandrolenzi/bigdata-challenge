import java.util.Date;
import java.util.Scanner;

// TODO usare seconds o milliseconds?
// TODO cosa fare di ore vuote?


public class DayAnalyzer {

	/* ************* CLASS FIELDS ******** */
	
	long initialTime, timestamp;
	
	double minStrength = Double.MAX_VALUE, maxStrength = Double.MIN_VALUE;
	double dayMean = 0, dayM2 = 0;
	int records = 0, periods = 0, periodId = 0;
	
	double periodSum = 0;
	
	Scanner s;		
	Arc currentArc = null, lastArc = null;
	Date d;
	
	
	
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
	public DayAnalyzer (Scanner scan, Date date){
		this.initialTime = date.getTime();
		d = date;
		s = scan;
	}
	
	
	/* ************** ANALYSING METHODS **************** */
	
	private void readArc () {
		String n = s.nextLine();
		this.lastArc = this.currentArc;
		this.currentArc = new Arc(n);
		records++;
		
		if(currentArc.getStrength() < minStrength)
			minStrength = currentArc.getStrength();
		if(currentArc.getStrength() > maxStrength)
			maxStrength = currentArc.getStrength();
		
		// System.out.println(n);
	}
	
	private void updateWith(double v){
		double delta = v - dayMean;
		dayMean += delta/records;
		dayM2 += delta*(v - dayMean);
	}
	
	public void Analyse () {
		System.out.println("\t == Analyze day " + d.toString());
		
		// First arc is to be examined alone (no previous arc)
		if(! s.hasNextLine()) return; 
		readArc();
		updateWith(currentArc.getStrength());
		
		// Load second arc so that we have a couple of them
		if(! s.hasNextLine()) return; 
		readArc();
		
		while(s.hasNextLine()){
			
			periodId = 0;
			
			// look for same source-destination in arcs
			while(currentArc.getSourceId() == lastArc.getSourceId() && currentArc.getDestId() == lastArc.getDestId()){
				// keep track of number of found periods, so to evaluate the "compression" rate
				// against the total number of records
				periods++;
				periodId = (int) ((currentArc.getTimestamp() - initialTime) / aggregationPeriod);			
				
				// periodSum is the accumulator over the period
				this.periodSum = currentArc.getStrength();
				
				if(s.hasNextLine()){
					// consume a new arc. If it has same (source,dest) and is within the same period
					// then aggregate to the previous. Else this arc is evaluated in a new iteration of this
					// loop (its strenght is counted in the assignment right above
					readArc();
					
					// should check if we switched (source,dest) couple
					if(currentArc.getSourceId() == lastArc.getSourceId() && currentArc.getDestId() == lastArc.getDestId())
						// consume all the arcs with timestamp in the same period 
						while(currentArc.getTimestamp() / aggregationPeriod == lastArc.getTimestamp() / aggregationPeriod){
							this.periodSum += currentArc.getStrength();
							if(s.hasNextLine()) readArc();
							else break;
						}
				} else {
					// file is finished. Update then close
					updateWith(periodSum);
					System.out.println("Arc " + currentArc.prettyHeadTail() + ", Period " + periodId + " Sum: " + periodSum);
					break;
				}
				
				// in both cases of having consumed or not more than 1 line, update values 
				updateWith(periodSum);
				System.out.println("Arc " + currentArc.prettyHeadTail() + ", Period " + periodId + " Sum: " + periodSum);
				
			}
			
			updateWith(currentArc.getStrength());
			if (s.hasNextLine()) readArc();
		}
		
		System.out.println("\n\nrecords:" + this.records);
		System.out.println("periods:" + periods);
		System.out.println("mean:" + this.dayMean);
		System.out.println("variance:" + ((records > 1) ? dayM2 /(records-1) : 0));
		System.out.println("min:" + minStrength);
		System.out.println("max:" + maxStrength);
		
		
	}
	
}
