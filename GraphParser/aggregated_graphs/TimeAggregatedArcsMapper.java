package aggregated_graphs;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;


/** Read arcs, map each of them onto hours intervals
	 * 
	 * @author Michele Carignani
	 *
	 */
	public class TimeAggregatedArcsMapper extends Mapper<LongWritable, Text, Text, Text>{
				
		public void map(LongWritable key, Text line, Context context) 
				throws IOException, InterruptedException {
			
			Configuration conf = context.getConfiguration();
			
			// Parse the line, format is Timestamp \t SourceID \t DestID \t Strength 
			String[] record = line.toString().split("\t");
			
			Long timestamp = Long.parseLong(record[0]);
			
			// Find the time at the start of the day
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(timestamp);
			
			int d = cal.get(Calendar.DAY_OF_MONTH);
			int h = cal.get(Calendar.HOUR_OF_DAY);
			int m = cal.get(Calendar.MONTH);
			int y = cal.get(Calendar.YEAR);
			
			String[] aggregators = conf.get("globalAggregators").split("\n");
			List<Aggregator> aggList = new LinkedList<Aggregator>();
			for(String s :aggregators){
				aggList.add(new Aggregator(s, 0));
			}
			
			String day = d + "-" + m + "-" + y + "-" + h;
			
			for(Aggregator a : aggList){
				if(a.respects(day)){
					Text newKey = new Text(a.getIdentifier() + "-" + 
							record[1].toString() + "-" +
							record[2].toString());
					Text val = new Text(record[3]);
					context.write(newKey,val);
					return;
				}
			}
			
		}
		
	}