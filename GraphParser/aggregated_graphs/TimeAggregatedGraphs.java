package aggregated_graphs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
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
	
    public static void main(String[] args) throws Exception {
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
            //conf.set("dayOfMonthFilter", "5, 15, 25, 30");
       
        // 4. select only a month (10 or 11)
        //    conf.set("monthFilter", "11");
            conf.set("globalAggregators", "H:7-13;M:11;W:2;Y:2013;N:MondayMorning");
        // 5. select only a subset of day in the week, comma separated (sunday == 1)
            // c.set("dayOfWeekArray", "2");
       
        Job first =  Job.getInstance(conf, "filter of probability graphs");
        first.setJarByClass(TimeAggregatedGraphs.class);
        first.setMapperClass(FilterMapper.class);
        first.setReducerClass(AverageReducer.class);
        first.setOutputKeyClass(Text.class);
        first.setOutputKeyClass(Text.class);
        /**
         * mISSING FORMATS: CHECK
         */
        FileInputFormat.setInputPaths(first, new Path(args[0]));
        FileOutputFormat.setOutputPath(first, new Path("intermediate_filter"));
       
        Job second = Job.getInstance(conf, "create aggregated probability graphs");
        second.setJarByClass(TimeAggregatedGraphs.class);
        
        second.setMapperClass(IdentityMapper.class);
        
        second.setReducerClass(ProbabilityReducer.class);
        second.setOutputKeyClass(Text.class);
        second.setOutputValueClass(Text.class);
        FileInputFormat.setInputPaths(second,  new Path("intermediate_filter"));
        FileOutputFormat.setOutputPath(second, new Path(args[1]));
        
        first.submit();
        if (first.waitForCompletion(true)) {
            second.submit();
        } else System.exit(1);
       
        System.exit(second.waitForCompletion(true) ? 0 : 1);
       
    }

}
