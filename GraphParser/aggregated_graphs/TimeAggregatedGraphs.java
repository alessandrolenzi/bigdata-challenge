package aggregated_graphs;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.LazyOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;


/** Generate aggregated sums of strengths over the nodes
 *  for each hour interval
 */

public class TimeAggregatedGraphs {
	/**
	 * @param args[0] aggregation file //not used :(
	 * @param args[1] input directory
	 * @param args[2] output directory
	 * @param args[3] number of reducers
	 * @throws Exception
	 */
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
        	System.out.println("Usage: call with aggregationFile (local, not hdfs) inputDirectory outputDirectory");
        	System.exit(-1);
        }
        /** Read the list of aggregators from the file passed as first arg.
         * Notice that the file is local to the executor. */
    	
    	String aggregators = "H:0-6;M:11,12;Y:2013;N:Night";/* +
    						"H:7-23;W:0;M:11,12;Y:2013;N:Sunday\n"+
    						"H:7-13;W:2;M:11,12;Y:2013;N:MondayMorning\n"+
    						"H:20-23;W:6;M:11,12;Y:2013;N:SaturdayEvening\n";*/
    	int countAggregators = 4;
    	if(countAggregators == 0) {
    		System.out.println("Please provide a valid aggregation file");
    		System.exit(-1);
    	}
    	
    	int numReducers = (args.length > 3) ? Integer.parseInt(args[3]) : countAggregators;
    	Configuration conf = new Configuration();
        /** Monday 2nd of November 2013 from 7 to 13 */
        conf.set("globalAggregators", aggregators);       
        Job job = Job.getInstance(conf, "Time Aggregated Graphs");
		LazyOutputFormat.setOutputFormatClass(job, TextOutputFormat.class);
	    job.setJarByClass(TimeAggregatedGraphs.class);
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(DoubleWritable.class);
	    
	    job.setReducerClass(AverageReducer.class);
	    job.setMapperClass(FilterMapper.class);
	    job.setOutputFormatClass(TextOutputFormat.class);
	    FileInputFormat.addInputPath(job, new Path(args[1]));
	    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        Calendar cal = Calendar.getInstance();
        Path intermediate =  new Path("/tmp/intermediate_filter"+dateFormat.format(cal.getTime()));
		FileOutputFormat.setOutputPath(job, intermediate);
		job.setNumReduceTasks(numReducers);
	    job.submit();
	    if(!job.waitForCompletion(true)) System.exit(-1);	
        
        Job second = Job.getInstance(conf, "create aggregated probability graphs");
        second.setJarByClass(TimeAggregatedGraphs.class);
        
        second.setMapperClass(IdentityMapper.class);
        LazyOutputFormat.setOutputFormatClass(second, TextOutputFormat.class);
        second.setReducerClass(ProbabilityReducer.class);
        FileInputFormat.setInputPaths(second,  intermediate);
        second.setOutputKeyClass(Text.class);
        second.setOutputValueClass(Text.class);
        Path output = new Path(args[2]);
        FileSystem.get(conf).delete(output, true);
        FileOutputFormat.setOutputPath(second, output);
        second.setInputFormatClass(TextInputFormat.class);
        job.setNumReduceTasks(numReducers);
        second.submit();
        
        
        boolean success = second.waitForCompletion(true);
        FileSystem fs = FileSystem.get(conf);
        fs.delete(intermediate, true);
        System.exit((success) ? 0 : 1);
    }

}
