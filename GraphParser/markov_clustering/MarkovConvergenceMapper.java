package markov_clustering;

import java.io.IOException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class MarkovConvergenceMapper extends Mapper<LongWritable, Text, Text, DoubleWritable> {

	public void map(LongWritable key, Text value, Context cont) throws IOException, InterruptedException{
		String[] s = value.toString().split("\t");
		for(int i = 1; i < s.length; i++) {
			String[] cur = s[i].split(" ");
			String outputKey = key.toString().concat(","+cur[0]);
			// Writes all current values with key <row,column>.
			cont.write(new Text(outputKey), new DoubleWritable(Double.parseDouble(cur[1])));
		}
	}

}
