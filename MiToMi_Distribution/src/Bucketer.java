import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;


public class Bucketer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FileInputStream variancesAndMeans = null;
		BufferedReader bufferReader = null;
		double interval = 0;
		int nintervals = Integer.parseInt(args[0]);
		long countValues[] = new long[nintervals+1];
		double mean = 0;
		Arrays.fill(countValues, 0l);
		
		try {
			variancesAndMeans = new FileInputStream(args[1]);
			bufferReader = new BufferedReader(new InputStreamReader(variancesAndMeans));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		double[] container = new double[10];
		int size = 10;
		int cur = 0; 
		Scanner sc = new Scanner(bufferReader);
		
		double maxVariance = Double.MIN_VALUE;
		double minVariance = Double.MAX_VALUE;
		while(sc.hasNextLine()) {
			String str = sc.nextLine();
			try {
			double var = Double.parseDouble(str.trim());
			//System.err.println(var+"\t"+cur);
			container[cur++] = var;
			double delta = var - mean;
			mean += delta/cur;
			if (cur == size) {
				size*=2;
				double[] tmp = container;
				container = new double[size];
				for (int i = 0; i < tmp.length; i++) {
					container[i] = tmp[i];
				}
			}
			
			} catch(NumberFormatException exc) {
				if (str.trim().equals("#END#")) {
					minVariance = Double.parseDouble(sc.nextLine().trim());
					maxVariance = Double.parseDouble(sc.nextLine().trim());
				}
				else {System.err.println("Failure"); System.exit(1);}
			}
		}
		System.err.println("Start sorting");
		
		Arrays.sort(container, 0, cur -1);
		
		
		interval = ((maxVariance - minVariance)/nintervals);
		System.err.println("Start bucketing. Bucket size:"+interval);
		int oldbucket = 0;
		for (int i = 0; i < cur; i++) {
			double var = container[i];
			int bucket = (int) (Math.floor((var - minVariance)/interval));
			//System.out.println(var+"\t"+bucket);
			if (bucket > oldbucket) {
				//System.out.println(oldbucket*interval+"\t"+countValues[oldbucket]);
				oldbucket = bucket;	
				//System.err.println(oldbucket);
			}
			if (bucket <= nintervals && bucket >= 0) countValues[bucket]++;
			else {
				if (bucket < 0) { System.err.println("eh??"+bucket+";"+var+"/"+interval); System.exit(1);}
				//long[] tmp = countValues;
				//countValues = new long[max*2];				
			}
		}
		//System.out.println(oldbucket*interval+"\t"+countValues[oldbucket]);
		for(int i = 0; i <= nintervals; i++) {
			if (countValues[i] > 0) System.out.println(i*interval+"\t"+countValues[i]);
		}
		System.err.println("Total buckets interval = "+countValues.length);
		System.err.println("Aggregation interval = "+interval);
		System.err.println("Maximum value = "+maxVariance);
		System.err.println("Minimum value =" +minVariance);
		System.err.println("Average value ="+mean);
	}

}
