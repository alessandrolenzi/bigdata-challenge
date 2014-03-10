package aggregated_graphs;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
/**
 * Gets average value and returns probability
 * @author alessandro
 *
 */
public class AvgProbReducer extends Reducer<Text, Text, Text, Text> {
	private LinkedList<String> list = new LinkedList<String>();
	public void reduce(Text aggregation, Iterator<Text> values, Context context)
            throws IOException, InterruptedException {
		double sum = 0;
		while(values.hasNext()) {
			String value = values.next().toString();
			list.add(value);
			String[] params = value.split("-");
			sum += Double.parseDouble(params[1]);
		}
		for(String s: list) {
			String[] fields = s.split("-");
			Text output = new Text(fields[0]+"-"+(Double.parseDouble(fields[1])/sum));
			context.write(aggregation, output);
		}
	}
}
