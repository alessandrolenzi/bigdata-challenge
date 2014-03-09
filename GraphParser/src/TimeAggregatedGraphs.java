import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;


/** Generate aggregated sums of strengths over the nodes
 *  for each hour interval
 *  
 * @author miche
 *
 */

public class TimeAggregatedGraphs {
	
	
	/** Creates sum over probabilities for one arc in one hour interval
	 * 
	 * @author Michele Carignani
	 *
	 */
	public static class ScaleArcs extends Reducer<Text, Text, Text, DoubleWritable> {
        public void reduce(Text key, Iterator<Text> values, Context context)
            throws IOException, InterruptedException {
        	
        	double sum = 0;
        	while(values.hasNext()){
        		sum += Double.parseDouble((values.next().toString().split(":"))[1]);
        	}
        	
        	context.write(key, new DoubleWritable(sum));
            
        }
    }

	
	public static void main(String[] args) {
		Configuration c = new Configuration();
		
		// 1. set aggregation period as multiples of 10 minutes

			// e.g. to set aggregation per hour
			// c.set("aggregationPeriod", "6");
			
			// e.g. to set aggregation per day
			// c.set("aggregationPeriod", "144");
		
		// 2. select only a subset of periods in a day, comma separated
		
			// e.g. to get only the 10th hour
			// c.set("periodFilter", "10");

		// 3. select only a subset of day in the month, comma separated
			// c.set("dayOfMonthFilter", "5, 15, 25, 30");
		
		// 4. select only a month (10 or 11)
			// c.set("monthFilter", "11");
		
		// 5. select only a subset of day in the week, comma separated (sunday == 1)
			// c.set("dayOfWeekArray", "2");

		JobConf j = new JobConf(c);
		j.setMapperClass(TimeAggregatedArcsMapper.class);
		j.setReducerClass(ScaleArcs.class);
	}

}
