package matrix_multiplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class IntermediateMultiplicationReducer extends
		Reducer<Text, Text, Text, Text> {
	/** 
	 *  We will find here elements from rows j and columns i such that i and j are the same. 
	 *  This means that they will be useful in order to find out 1 elel 
	 * */
	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException{
		/** Values coming from the two different matrices */
		  ArrayList<Entry<Integer, Double>> listA = new ArrayList<Entry<Integer, Double>>();
          ArrayList<Entry<Integer, Double>> listB = new ArrayList<Entry<Integer, Double>>();
          String[] value;
          for (Text val : values) {
              value = val.toString().split(",");
              if (value[0].equals("A")) {
                  listA.add(new SimpleEntry<Integer, Double>(Integer.parseInt(value[1]), Double.parseDouble(value[2])));
              } else {
                  listB.add(new SimpleEntry<Integer, Double>(Integer.parseInt(value[1]), Double.parseDouble(value[2])));
              }
          }
          String i;
          double a_ij;
          String k;
          double b_jk;
          Text outputValue = new Text();
          for (Entry<Integer, Double> a : listA) {
              i = Integer.toString(a.getKey());
              a_ij = a.getValue();
              if (a_ij == 0) continue;
              for (Entry<Integer, Double> b : listB) {
            	  int val = context.getConfiguration().getInt("bCounter", 0);
              	  context.getConfiguration().setInt("bCounter", val+1);
                  k = Integer.toString(b.getKey());
                  b_jk = b.getValue();
                  if (b_jk == 0) continue;
                  outputValue.set(i + "," + k + "," + Double.toString(a_ij*b_jk));
                  /** eh??*/
                  context.write(null, outputValue);
              }
          }
      }
}
