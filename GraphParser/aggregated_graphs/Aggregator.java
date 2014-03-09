package aggregated_graphs;


import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * Aggregator: can be used also for complex filters definition.
 * @author alessandro
 *
 */
public class Aggregator {
	private List<int[]> supportedWDays;
	private List<int[]> supportedDays;
	private List<int[]> supportedMonths;
	private List<int[]> supportedHours;
	String identifier = null;
	int aggregationPeriod;
	/**
	 * 
	 * @param s represents a complex criteria expression. Format:
	 * [[H|W|D|M]:(d+)[-(d+)|(,d+)*]?;]+
	 * [N:identifier of the criteria]+
	 * Where:
	 * - H,W,D,M identify, respectively, an hour, week day, day, month criteria
	 * - x-y represents the interval [x,y]
	 * - x represents the hour x
	 * - x,y,z represents the list of criteria x,y,z which could be either single hours or intervals 
	 * - N is a "special type" for giving the criteria a name. Notice that this is mandatory
	 * Conditions expressed on the same "normal type" (H,W,D,M) are in OR; those expressed on different "normal types" are in AND.
	 * A canonical form is in the format:
	 * H:[list of allowed hours]
	 * W:[list of allowed week days, issued using Calendar.$DAYOFTHEWEEK]
	 * D:[list of the allowed days]
	 * M:[list of the allowed months]
	 * N:String identifying the criteria
	 * @param aggregPeriod the aggregation period for translating hours back.
	 * @throws IllegalArgumentException in case the name for the criteria is not supplied or a criteria has an invalid format
	 */
	public Aggregator(String s, int aggregPeriod) throws IllegalArgumentException{
		supportedWDays = new LinkedList<int[]>();
		supportedDays = new LinkedList<int[]>();
		supportedMonths = new LinkedList<int[]>();
		supportedHours = new LinkedList<int[]>();
		String[] criteria = s.split(";");
		aggregationPeriod = aggregPeriod;
		for (String c: criteria) parseCriteria(c);
		if (identifier == null) throw new IllegalArgumentException("Please provide a name for this aggregator");
			
	}
	
	private void critIntervals(String string, List<int[]> l) {
		if (string.contains(",")) { /**	List of accepted values */
			String[] singletons = string.split(",");
			for(String s: singletons) critIntervals(s, l);
			return;
		}
		if (string.contains("-")) { /** Interval */
			String[] interval = string.split("-");
			l.add(new int[] {Integer.parseInt(interval[0], Integer.parseInt(interval[1]))});
			return;
		}
		/** Singleton */
		l.add(new int[]{Integer.parseInt(string)});
	}
	
	private void parseCriteria(String criteria) {
		String[] record = criteria.split(":");
		switch(record[0]) {
		case "H": critIntervals(record[1], supportedHours);  return;
		case "W": critIntervals(record[1], supportedWDays); return;
		case "D": critIntervals(record[1], supportedDays); return;
		case "M": critIntervals(record[1], supportedMonths); return;
		case "N": identifier = record[0]; return;
		default: throw new IllegalArgumentException("The string \""+criteria+"\" is not a valid criteria");
		}
	}	
	
	public static boolean checkCriteria(int[] criteria, int value) {
		return (criteria.length == 1) ? criteria[0] == value : criteria[0] <= value && criteria[1] >= value;
	}
	
	public boolean respects(String key) {
		// gets ((day-month-year-periodId-Source-Sum)
		String[] fields = key.split("-");
		int day = Integer.parseInt(fields[0]);
		int month = Integer.parseInt(fields[1]);
		int year = Integer.parseInt(fields[2]);
		int hour = getHourFromPeriod(Integer.parseInt(fields[3]));
		boolean respected = false;
		/** Check criteria from most restrictive to least restrictive */
		for (int[] allowedMonths: supportedMonths) {
			respected = respected || checkCriteria(allowedMonths, month); 
		}
		if (!respected && supportedMonths.size() > 0) return false;
		respected = false;
		for (int[] allowedWeekDays: supportedWDays) {
			Calendar c = Calendar.getInstance();
			c.set(year, month, day);
			int day_of_week = c.get(Calendar.DAY_OF_WEEK);
			respected = respected || checkCriteria(allowedWeekDays, day_of_week);
		}
		if (!respected && supportedWDays.size() > 0) return false;
		respected = false;
		for(int[] allowedDays : supportedDays) {
			respected = respected || checkCriteria(allowedDays, day);
		}
		if (!respected && supportedDays.size() > 0) return false;
		respected = false;
		for (int[] allowedHours : supportedHours) {
			respected = respected || checkCriteria(allowedHours, hour);
		}
		return true;
		
	}

	private int getHourFromPeriod(int periodId) {
		int time = periodId * aggregationPeriod;
		return (int) Math.floor(time/3600);
	}
	
	public String getIdentifier() {
		return identifier;
	}
}
