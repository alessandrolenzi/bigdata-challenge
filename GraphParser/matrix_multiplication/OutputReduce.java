package matrix_multiplication;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class OutputReduce extends Reducer<Text, Text, Text, DoubleWritable> {
    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            double result = 0;
            Iterator<Text> it = values.iterator();
            while(it.hasNext()) {
            	String[] tokens = it.next().toString().split(",");
            	
            }
            context.write(key, new DoubleWritable(result));
    }
}
