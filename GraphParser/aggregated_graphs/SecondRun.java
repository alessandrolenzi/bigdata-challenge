package aggregated_graphs;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class SecondRun {

	public static void main(String[] args) throws IllegalArgumentException, IOException, ClassNotFoundException, InterruptedException {
        Configuration conf = new Configuration();
        conf.set("globalAggregators", "H:7-13;M:11;W:2;Y:2013;N:MondayMorning");
       
        Job second = Job.getInstance(conf, "create aggregated probability graphs");
        second.setJarByClass(SecondRun.class);
        
        second.setMapperClass(IdentityMapper.class);
        second.setReducerClass(ProbabilityReducer.class);
        
        second.setOutputKeyClass(Text.class);
        second.setOutputValueClass(Text.class);
        
        FileInputFormat.setInputPaths(second,  new Path(args[0]));
        FileOutputFormat.setOutputPath(second, new Path(args[1]));
        
        second.submit();
        System.exit(second.waitForCompletion(true) ? 0 : 1);
	}

}
