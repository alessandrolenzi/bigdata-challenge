package markov_clustering;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;

public class MarkovConvergenceReducer extends Reducer<Text,DoubleWritable,Text,BooleanWritable> {
	
	public void reduce(Text coordinates, Iterable<DoubleWritable> values, Context context)
            throws IOException, InterruptedException {
		Configuration conf = context.getConfiguration();
		double threeshold = conf.getDouble("epsilon", 0.0);
		Iterator<DoubleWritable> val = values.iterator();
		double prev, next;
		prev = (val.hasNext()) ? 0.0 : val.next().get();
		next = (val.hasNext()) ? 0.0 : val.next().get();
		if(Math.abs(prev - next) > threeshold) {
			conf.setBoolean("converged", false);
		}
		conf.setBooleanIfUnset("converged", true);
		
	}
}
