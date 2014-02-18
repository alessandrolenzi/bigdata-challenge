import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Scanner;


public class AlmostNoMemoryVariance {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Error! Supply a valid number of arguments");
			System.exit(-1);
		}
		double minvalue = Double.MAX_VALUE; 
		FileInputStream day = null;
		BufferedReader bufferReader = null;
		Scanner scan = null;
		try {
			day = new FileInputStream(args[0]);
			bufferReader = new BufferedReader(new InputStreamReader(day));
			scan = new Scanner(bufferReader);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Couldn't find file named "+args[0]);
			System.exit(-1);
		}
		int d = 0;
		while (scan.hasNextLine()) {
			d++;
			String currentLine = scan.nextLine();
			String[] fields = currentLine.split("\t");
			double correlation_value = Double.parseDouble(fields[3]);
			minvalue = (correlation_value > 0) ? Math.min(minvalue, correlation_value) : minvalue;
			if(d % 1000000 == 0) System.err.println("[MIN] Processed "+d+" lines");
		}
		BigDecimal bd = new BigDecimal(minvalue).setScale(20, BigDecimal.ROUND_HALF_UP);
		int scale = 1;
		while (bd.doubleValue() < 1) {
			bd = bd.scaleByPowerOfTen(1);
			scale++;
		}
		scale/=2;
		System.err.println("Min found: "+minvalue+"; rescaling for 10^"+scale);
		bd = null;
		scan.close();
		try {
			day.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			scan = new Scanner(new BufferedReader(new InputStreamReader(new FileInputStream(args[0]))));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		d = 0;
		int startingGrid = -1;
		int endingGrid = -1;
		int n = 0;
		double mean = 0; 
		double m2 = 0;
		double MinVariance = Double.MAX_VALUE;
		double MaxVariance = Double.MIN_VALUE;
		while(scan.hasNextLine()) {
			String currentLine = scan.nextLine();
			String[] fields = currentLine.split("\t");
			if(fields.length < 3) System.err.println(currentLine);
			double scaledVal = Double.parseDouble(fields[3]);
			int sid = Integer.parseInt(fields[1]);
			int eid = Integer.parseInt(fields[2]);
			if (sid != startingGrid || eid != endingGrid) {
				//Calculate and send out covariance
				if (n > 1) {
					try {
						double variance = m2/(n-1);
						MinVariance = Math.min(MinVariance, variance);
						MaxVariance = Math.max(MaxVariance, variance);
						BigDecimal outputValue = new BigDecimal(variance).setScale(20, BigDecimal.ROUND_HALF_UP);
						System.out.println(outputValue.toEngineeringString());
					} catch(NumberFormatException exc) {
						System.err.println(m2+"\t"+n);
					}
				} else System.out.println(0);
				m2 = 0; n = 0; mean = 0;
				startingGrid = sid; endingGrid = eid;
			}
			n++;
			double delta = scaledVal - mean;
			mean += delta/n;
			m2 += delta*(scaledVal - mean);
			d++;
			if(d % 1000000 == 0) System.err.println("[VAR] Processed "+d+"lines");
			
		}
		System.out.println("#END#");
		System.out.println(MinVariance);
		System.out.println(MaxVariance);

	}

}
