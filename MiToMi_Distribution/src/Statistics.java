
public class Statistics {
	double mean = 0, m2 = 0, min = Double.MAX_VALUE, max = Double.MIN_VALUE;
	long valuesCounter = 0;
	
	void update(double value){
		if(value < min) min = value;
		if(value > max) max = value;
		valuesCounter++;
		double delta = value - mean;
		mean += delta/(valuesCounter);
		m2 += delta*(value - mean);
	}
	
	double variance (){
		return ((valuesCounter > 1) ? m2 /(valuesCounter-1) : 0);
	}
	
	String asRecord () {
		return 	 mean + "\t" +
				   variance() + "\t" +
				   min + "\t" +
				   max + "\t" +
				   valuesCounter;
	}
	
	String prettyPrinted () {
		return "\n\tmean:" + mean + "\n" +
		"\tvariance:" + variance() + "\n" +
		"\tmin:" + min + "\n" + 
		"\tmax:" + max + "\n" +
		"\tvaluesCounter:" + valuesCounter + "\n";
	}
	
}
