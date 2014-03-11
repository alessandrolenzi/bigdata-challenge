package aggregated_graphs;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * Returns a graph for every aggregation period
 * @author alessandro
 *
 */
public class AverageGraph extends Mapper<Text, Text, Text, Text> {
	// gets ((day-month-year-periodId-Source-Sum),(Dest:Prob))
	private boolean initialized = false;
	private List<Aggregator> aggregatorsList = new LinkedList<Aggregator>();
	public void initializeAggregator(Context context) {
		initialized = true;
		Configuration conf = context.getConfiguration();
		String allAggregators = conf.get("globalAggregators");
		String[] aggregators = allAggregators.split("\n");
		/** Initialize all aggregators, adding them */
		for(String a: aggregators)
			aggregatorsList.add(new Aggregator(a /* , Integer.parseInt(conf.get("aggregationPeriod"))*10*/));		
	}
	
	public String getAggregatorKey(Text k) {
		String key = k.toString();
		String src = key.split("-")[3];
		for (Aggregator a: aggregatorsList)
			if (a.respects(key)) return a.getIdentifier()+":"+src+":"+(a.getTotalSeconds() / 600);
		return "DEFAULT";
	}
	
	public void map(Text key, Text arc, Context context) 
			throws IOException, InterruptedException {
		if (!initialized) initializeAggregator(context);
		String arcProb[] = arc.toString().split(":");
		String id = getAggregatorKey(key)+":"+arcProb[0];
		context.write(new Text(id), new Text(arcProb[1]));
		/** Writes down something like:
		 * aggregationName:aggregationSize(in slots of 10 minutes):source:destination, probability*/
	}
	

}
