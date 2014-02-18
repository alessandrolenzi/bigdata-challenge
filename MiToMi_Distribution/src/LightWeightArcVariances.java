import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Scanner;
import java.util.Vector;


public class LightWeightArcVariances {

	/**
	 * @param args
	 * @return 
	 */
	
	private static double calculateVariance(String corvalues, int scale) {
		String[] detected_values = corvalues.split("\t");
		if (detected_values.length > 1) { //return m2/(number -1);
			 double xisq = 0;
			 double exisq = 0; 
			 for (String value: detected_values) {
				 double scaledVal = Double.parseDouble(value)*Math.pow(10, scale);
				 xisq += Math.pow(scaledVal, 2);
				 exisq += scaledVal;
			 }
			 return (xisq - Math.pow(exisq, 2)/detected_values.length)/(detected_values.length - 1);
		}
		return 0;
	}
	
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
		int startingGrid = -1;
		int endingGrid = -1;
		StringBuffer buffer = new StringBuffer();
		Vector<String> dayValues = new Vector<String>();
		int d = 0;
		while (scan.hasNextLine()) {
			d++;
			String currentLine = scan.nextLine();
			String[] fields = currentLine.split("\t");
			int cursgrid = Integer.parseInt(fields[1]);
			int curegrid = Integer.parseInt(fields[2]);
			if (cursgrid == startingGrid && curegrid == endingGrid) {
				buffer.append(fields[3]);
				buffer.append("\t");
			} else {
				dayValues.add(buffer.toString());
				buffer = new StringBuffer(fields[3]);
				buffer.append("\t");
				startingGrid = cursgrid;
				endingGrid = curegrid;
			}
			double correlation_value = Double.parseDouble(fields[3]);
			minvalue = (correlation_value > 0) ? Math.min(minvalue, correlation_value) : minvalue;
			if(d % 1000000 == 0) System.err.println("Processed "+d+" lines, occupied space is "+dayValues.size()+"/"+dayValues.capacity());
		}
		BigDecimal bd = new BigDecimal(minvalue).setScale(20, BigDecimal.ROUND_HALF_UP);
		int scale = 1;
		while (bd.doubleValue() < 1) {
			bd = bd.scaleByPowerOfTen(1);
			scale++;
		}
		bd = null;
		System.err.println("Start correlation calculation");
		double MinVariance = Double.MAX_VALUE;
		double MaxVariance = Double.MIN_VALUE;
		for(String dayCorrelations: dayValues) {
			double variance = calculateVariance(dayCorrelations, scale);
			BigDecimal show = new BigDecimal(minvalue).setScale(20, BigDecimal.ROUND_HALF_UP);
			System.out.println(show.toEngineeringString());
			MinVariance = Math.min(MinVariance, variance);
			MaxVariance = Math.max(MaxVariance, variance);
		}
		System.out.println("#END#");
		System.out.println(MinVariance);
		System.out.println(MaxVariance);
		
		

	}

}
