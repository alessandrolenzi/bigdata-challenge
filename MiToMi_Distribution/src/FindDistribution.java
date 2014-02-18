import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Stack;
import java.util.Vector;

public class FindDistribution {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//Step1: find minimum, find maximum, find variance.
		double mincorrelation = Double.MAX_VALUE;
		double maxcorrelation = Double.MIN_VALUE;
		long records = 0;
		double mean = 0; 
		double m2 = 0;
		Vector<Day> all_days = new Vector<Day>();
		for(String file: args) {
			Day current=null;
			try {
				 current = new Day(file);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				System.err.println("File Not Found");
			}
			while(current.hasNext()) {
				records++;
				double value = current.nextValue();
				double delta = value - mean;
				mean += delta/records;
				m2 += delta*(value - mean);
			}
			System.err.println("Processed "+records+" records so far ("+file+")");
			all_days.add(current);
			maxcorrelation = Math.max(maxcorrelation, current.getMax());
			mincorrelation = Math.min(mincorrelation, current.getMin());
		}
		System.out.println("Variance:"+m2/(records-1));
		System.out.println("Mean:"+mean);
		System.out.println("Maximum:"+maxcorrelation);
		System.out.println("Minimum:"+mincorrelation);
		int bucketnumber = 10000;
		long[] buckets = new long[bucketnumber+1];
		double interval = (maxcorrelation - mincorrelation)/bucketnumber;
		
		for(String file: args) {
			Stack<Double> dayValues = new Stack<Double>();
			Day day = null;
			try {
				day = new Day(file);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			while(day.hasNext()) dayValues.add(day.nextValue());
			Collections.sort(dayValues);
			while(!dayValues.isEmpty()) {
				double val = dayValues.pop();
				int bucket = (int) (Math.floor((val - mincorrelation)/interval));
				buckets[bucket]++;
			}
		}
		for(int i = 0; i < bucketnumber+1; i++) {
			System.out.println(i*interval+"\t"+buckets[i]);
		}
	}
	
	

}
