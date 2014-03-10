package aggregated_graphs;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;


/** Read arcs, map each of them onto hours intervals
	 * 
	 * @author Michele Carignani
	 *
	 */
	public class TimeAggregatedArcsMapper extends Mapper<LongWritable, Text, Text, Text>{
		
		/** Aggregation period */
		private static long period = 60 * 60 * 1000;
		/** Array of day to be kept, e.g. {Calendar.MONDAY}*/
		private static Integer[] dayOfWeekFilter;
		/** Array of day to be kept, e.g. {21, 22} */
		private static Integer[] dayOfMonthFilter;
		/**  */
		private static Integer[] periodFilter;
		
		private int monthFilter = 0;

		
		public static Integer[] parseIntArray(String s)	{
			if (s == null){
				return new Integer[0];
			}
			List<Integer> res = new LinkedList<Integer>();
			Integer k = 0;
			String[] items = s.split(",", -1);
			for(String i : items){
				k = Integer.parseInt(i.replaceAll("\\s", ""));
				res.add(k);
			}
			return res.toArray(new Integer[0]);
		}
		
		/**
		 * 
		 * @return true if element is to be removed
		 */
		public static boolean filterWith(Integer[] a, int v) {
			if(a.length == 0){
				return false;
			}
			boolean f = true;
			for(Integer i : a){
				f = i.intValue() != v;
			}
			return f;
		}

		public static boolean checkMonth(int m, int c){
			return (m != 0) && (m != c);
		}
		
		public void map(LongWritable key, Text line, Context context) 
				throws IOException, InterruptedException {
			
			Configuration conf = context.getConfiguration();
			
			String p = conf.get("aggregationPeriod"),
				   mf = conf.get("monthFilter");
			if(p != null){
				period = (Long.parseLong(p.replaceAll("\\s", ""))) * 10 * 1000;
			}
			
			if(mf != null){
				monthFilter = (Integer.parseInt(mf.replaceAll("\\s", "")));
			}
			
			periodFilter = parseIntArray(conf.get("periodFilter"));
			dayOfMonthFilter = parseIntArray(conf.get("dayOfMonthFilter"));
			dayOfWeekFilter = parseIntArray(conf.get("dayOfWeekFilter"));
			
			// Parse the line, format is Timestamp \t SourceID \t DestID \t Strength 
			String[] record = line.toString().split("\t");
			
			Long timestamp = Long.parseLong(record[0]);
			
			// Find the time at the start of the day
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(timestamp);
			
			int d = cal.get(Calendar.DAY_OF_MONTH);
			int dw = cal.get(Calendar.DAY_OF_WEEK);
			int m = cal.get(Calendar.MONTH);
			int y = cal.get(Calendar.YEAR);
			
			DateFormat form = new SimpleDateFormat("dd-MM-yyyy,HH:mm:ss aaa");
			String s = ((d < 10) ? "0" : "" ) + d + "-" +
					   ((m < 10) ? "0" : "" ) + m + "-" +
					   y + "-" +
					   ",00:00:00 AM";
			
			Date date = null;
			try {
				date = form.parse(s);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Long dayStart = date.getTime();
			
			// find Period ID
			int pId = (int)((timestamp - dayStart ) / period);
			
			// Check if we are interested in this period ID
			if(filterWith(periodFilter, pId)filterWith(dayOfMonthFilter, d) 
				&& filterWith(dayOfWeekFilter, dw)
				&& checkMonth(monthFilter, m - 1)){
				return;
			}
			
			String day = d + "-" + m + "-" + y;
			context.write(new Text(day + "-" + pId + "-" + record[1].toString()), new Text(record[2] + ":" + record[3]));
		}
		
	}