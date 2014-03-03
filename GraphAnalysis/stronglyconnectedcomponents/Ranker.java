package stronglyconnectedcomponents;

import java.util.Collections;
import java.util.Random;
import java.util.Vector;

/**
 * Returns the threshold of the given percentile for all the values analized.
 * Randomized implementation using the reservoir sampling.
 * @author alessandro
 *
 */
public class Ranker {
	private double percentile;
	private int sampleNumber;
	private double curVal = Double.MIN_VALUE;
	private Vector<Double> samples = new Vector<Double>();
	private int seen = 0;
	private Random r = new Random();
	public Ranker(double percentl, int sampleN) throws IllegalArgumentException{
		if (percentile >= 1.0 || sampleN < 1) throw new IllegalArgumentException("Please provide a percentile < 1 and a number of samples >= 1 (percentile:"+percentl+")(samples:"+sampleN+")");
		percentile = percentl;
		sampleNumber = sampleN;
	}
	
	/** Reservoir sampling*/
	public void checkElement(double element) {
		if (seen < sampleNumber) {
			samples.add(element);
			seen++;
			return;
		}
		seen++;
		int replace = r.nextInt(seen);
		if (replace < sampleNumber) {
			samples.set(replace, element);
		}
		
	}
	
	public double returnPercentile() {
		Collections.sort(samples);
		int position =(int) Math.rint(Math.min(sampleNumber, seen) * percentile) - 1;
		return samples.get(position);
	}

}
