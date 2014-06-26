package aggregated_graphs;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;



public class ProbabilityReducer extends Reducer<Text, Text, Text, Text> {
	 
	private MultipleOutputs<Text, Text> out;
	
	private class ArcTail {
		public ArcTail(String destination, double weight) {
			this.destination = destination;
			this.weight = weight;
		}
		String destination; double weight;
	}
	
	public void setup(Context context){
		out = new MultipleOutputs<Text,Text>(context);
	}
	
	public void reduce(Text aggregation, Iterable<Text> v, Context context)
	            throws IOException, InterruptedException {
		/** params
		 * 0 -> aggregationName
		 * 1 -> source
		 */
		/** value
		 *  0 -> destination
		 *  1 -> sum
		 */
		String[] params = aggregation.toString().split("-"), vSplit;
		double sum = 0; 
		
		List<ArcTail> al = new LinkedList<ArcTail>();
		Iterator<Text> values = v.iterator();
		while(values.hasNext()) {
			vSplit = values.next().toString().split(":");
			double w = Double.parseDouble(vSplit[1]);
			al.add(new ArcTail(vSplit[0], w));
			sum += w;
		}
		
		Text newKey = new Text(params[1]);
		
		for(ArcTail a : al){
			Text val = new Text(a.destination +" "+ (a.weight / sum));
			out.write(newKey, val, generateFileName(aggregation));
		}		
		al.clear();
		al = null;
		
		
	}
	
	protected String generateFileName(Text key) {
		String[] val = key.toString().split("-");
		return val[0]+"-"+val[1];
	}
	
	protected void cleanup(Context context) throws IOException, InterruptedException {
		   out.close();
	}

}
