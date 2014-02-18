import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Scanner;


public class Day {
	private Scanner scan = null;
	private double maxvalue = Double.MIN_VALUE;
	private double minvalue = Double.MAX_VALUE;
	private int records = 0;
	private double mean = 0; 
	private double m2 = 0;
	private String day = null;
	public Day(String s) throws FileNotFoundException {
		scan = new Scanner(new BufferedReader(new InputStreamReader(new FileInputStream(s))));
		day = s;
	}
	
	public boolean hasNext() {
		if(!scan.hasNextLine()) { scan.close(); scan = null; return false;}
		return true;
	}
	
	public double nextValue() {
		double retrieved = 0d;
		String[] fields = scan.nextLine().split("\t");
		retrieved = Double.parseDouble(fields[3]);
		//Max&Min calculation
		maxvalue = Math.max(retrieved, maxvalue);
		minvalue = Math.min(retrieved, minvalue);
		//variance calculation
		records++;
		double delta = retrieved - mean;
		mean += delta/records;
		m2 += delta*(retrieved - mean);
		return retrieved;
	}
	
	
	public double getVariance() { return (records > 1) ? m2/(records -1):0;}
	public double getMax(){return maxvalue;}
	public double getMin(){return minvalue;}
	public double getMean(){return mean;}
	public void printOut() {
		System.out.println("------"+day+"------");
		System.out.println("\t Max:"+maxvalue);
		System.out.println("\t Min:"+minvalue);
		System.out.println("\t Var:"+getVariance());
		System.out.println("\t Avg:"+getMean());
		System.out.println();
	}
}
