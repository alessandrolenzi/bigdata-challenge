package aggregated_graphs;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


/** Generate aggregated sums of strengths over the nodes
 *  for each hour interval
 *  
 * @author miche
 *
 */

public class TimeAggregatedGraphs {
	
	public static void main(String[] args) throws IllegalArgumentException, IOException, ClassNotFoundException, InterruptedException {
		Configuration conf = new Configuration();
		// 1. set aggregation period as multiples of 10 minutes

			// e.g. to set aggregation per hour
			// c.set("aggregationPeriod", "6");
			
			// e.g. to set aggregation per day
			// c.set("aggregationPeriod", "144");
		
		// 2. select only a subset of periods in a day, comma separated
		
			// e.g. to get only the 10th hour
			// c.set("periodFilter", "10");

		// 3. select only a subset of day in the month, comma separated
			conf.set("dayOfMonthFilter", "5, 15, 25, 30");
		
		// 4. select only a month (10 or 11)
			conf.set("monthFilter", "11");
		
		// 5. select only a subset of day in the week, comma separated (sunday == 1)
			// c.set("dayOfWeekArray", "2");

		
		Job j =  Job.getInstance(conf);

		j.setJobName("create aggregated probability graphs");
		
		FileInputFormat.setInputPaths(j, new Path(args[0]));
		FileOutputFormat.setOutputPath(j, new Path(args[1]));
		
		j.setMapperClass(TimeAggregatedArcsMapper.class);
		j.setReducerClass(ProbabilitiesReducer.class);
		
		j.setJarByClass(TimeAggregatedGraphs.class);
		
		System.exit(j.waitForCompletion(true) ? 0 : 1);
		
	}

}
