package aggregated_graphs;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;

public class ProbabilityAggregatorMapper extends Mapper<Text, Text, Text, Text> {
	/**
	 * @param key is in format D-P-Source-Sum
	 * @param arc is in format Destination:probability
	 * @writesKey D-P-Source-Sum-Destination
	 * @writesValue P
	 */
	public void map(Text key, Text arc, Context context) 
			throws IOException, InterruptedException {
		
	}

}
