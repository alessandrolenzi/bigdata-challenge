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
	/** Gets all aggregators from the context. Modifies aggregatorsList
	 * @param context the context of the computation
	 * 
	 * */
	public void initializeAggregator(Context context) {
		initialized = true;
		Configuration conf = context.getConfiguration();
		String allAggregators = conf.get("globalAggregators");
		String[] aggregators = allAggregators.split("\n");
		/** Initialize all aggregators, adding them to the list */
		for(String a: aggregators)
			aggregatorsList.add(new Aggregator(a /* , Integer.parseInt(conf.get("aggregationPeriod"))*10*/));		
	}
	/** 
	 * Get the current aggregator unique identifier (tagging it)
	 * @param k the current key (timestamp)
	 * @return the value or default.
	 * */
	public String getAggregatorKey(Text k) {
		String key = k.toString();
		String src = key.split("-")[3];
		for (Aggregator a: aggregatorsList)
			if (a.respects(key)) return a.getIdentifier()+":"+src+":"+(a.getTotalSeconds() / 600);
		return "DEFAULT";
	}
	
	/**
	 * Hadoop mapper, changes the key to be UniqueIdentifierOfPeriod:Destinati
	 * 
	 */
	public void map(Text key, Text arc, Context context) throws IOException, InterruptedException {
		if (!initialized) initializeAggregator(context);
		String arcProb[] = arc.toString().split(":");
		String id = getAggregatorKey(key)+":"+arcProb[0];
		context.write(new Text(id), new Text(arcProb[1]));
	}
	

}
