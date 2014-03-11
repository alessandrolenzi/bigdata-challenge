package aggregated_graphs;
import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/** Creates averages for one arc (source dest couple) in one aggregation period
	 * 
	 * @author Michele Carignani
	 *
	 */
	public class AverageReducer extends Reducer<Text, Text, Text, Text> {
		
        public void reduce(Text key, Iterable<Text> v, Context context)
            throws IOException, InterruptedException {
        	// takes ((ID-Num-Source-Dest),(Val))
        	// puts ((ID-Source),(Dest-AvgVal))
        	
        	double w, sum = 0;
        	String  s;
        	int count = 0;
        	Iterator<Text> values = v.iterator();
        	while(values.hasNext()){
        		s = values.next().toString();
        		w = Double.parseDouble(s);
        		sum += w;
        		count++;
        	}
        	
        	String[] splitKey = key.toString().split("-");
        	double num = Double.parseDouble(splitKey[1]);
        	
        	double avg = sum / num;
        	
        	Text newKey = new Text(splitKey[0] + "-" + splitKey[2]);
        	Text val = new Text(splitKey[3] + ":" + avg);
        	context.write(newKey,val);
        	
        }
        
    }