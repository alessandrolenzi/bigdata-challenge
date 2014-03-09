package aggregated_graphs;
import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;



public class MeanReducer extends Reducer<Text, Text, Text, Text> {
	 
	public void reduce(Text aggregation, Iterator<Text> values, Context context)
	            throws IOException, InterruptedException {
		/** params
		 * 0 -> aggregationName
		 * 1 -> aggregationSize
		 * 2 -> source
		 * 3 -> destination
		 */
		String[] params = aggregation.toString().split(":");
		double sum = 0; 
		while(values.hasNext()) {
			sum += Double.parseDouble(values.next().toString());
		}
		context.write(new Text(params[0]+":"+params[2]), 
				new Text(params[3]+":"+(sum/Integer.parseInt(params[1]))));
	}

}
