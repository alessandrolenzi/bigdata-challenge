package aggregated_graphs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
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
            conf.set("globalAggregators", "H:7-13;M:11;W:2;D:2;Y:2013;N:0212MondayMorning");
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
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        Calendar cal = Calendar.getInstance();
        Path intermediate =  new Path("/tmp/intermediate_filter"+dateFormat.format(cal.getTime()));
        FileOutputFormat.setOutputPath(first, intermediate);
       
        Job second = Job.getInstance(conf, "create aggregated probability graphs");
        second.setJarByClass(TimeAggregatedGraphs.class);
        
        second.setMapperClass(IdentityMapper.class);
        
        second.setReducerClass(ProbabilityReducer.class);
        second.setOutputKeyClass(Text.class);
        second.setOutputValueClass(Text.class);
        FileInputFormat.setInputPaths(second,  intermediate);
        Path output = new Path(args[1]);
        FileSystem.get(conf).delete(output, true);
        FileOutputFormat.setOutputPath(second, output);
        
        first.submit();
        if (first.waitForCompletion(true)) {
            second.submit();
        } else System.exit(1);
        
        boolean success = second.waitForCompletion(true);
        FileSystem fs = FileSystem.get(conf);
        fs.delete(intermediate, true);
        System.exit((success) ? 0 : 1);
    }

}
