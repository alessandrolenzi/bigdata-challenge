import java.util.Date;
import java.util.Scanner;


public class DayStatistics extends DayAnalyzer {

	public DayStatistics(Scanner scan, Date date) {
		super(scan, date);
	}

	public void appendResultLine (String s) {
		result = result + "\n" + s;
	}
	
	public void updateWith(double v, int periodId, Arc curr){
		double delta = v - dayMean;
		dayMean += delta/(records);
		dayM2 += delta*(v - dayMean);
	}
	
	protected void finish () {
		appendResultLine("\n\trecords:" + this.records);
		appendResultLine("\tperiods:" + periods);
		appendResultLine("\tmean:" + this.dayMean);
		appendResultLine("\tvariance:" + ((records > 1) ? dayM2 /(records-1) : 0));
		appendResultLine("\tmin:" + minStrength);
		appendResultLine("\tmax:" + maxStrength);
	}

	
}
