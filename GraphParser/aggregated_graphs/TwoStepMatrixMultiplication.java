package aggregated_graphs;

import java.io.IOException;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 
public class TwoStepMatrixMultiplication {
 
	public class IdentityMapperForDoubles extends Mapper<Text, DoubleWritable, Text, DoubleWritable> {
		public void map(Text k, DoubleWritable v, Context c) throws IOException, InterruptedException{
			c.write(k, v);
		}
	}
	
    public static class Map extends Mapper<LongWritable, Text, Text, Text> {
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            String[] indicesAndValue = line.split(",");
            Text outputKey = new Text();
            Text outputValue = new Text();
            if (indicesAndValue[0].equals("A")) {
                outputKey.set(indicesAndValue[2]);
                outputValue.set("A," + indicesAndValue[1] + "," + indicesAndValue[3]);
                context.write(outputKey, outputValue);
            } else {
                outputKey.set(indicesAndValue[1]);
                outputValue.set("B," + indicesAndValue[2] + "," + indicesAndValue[3]);
                context.write(outputKey, outputValue);
            }
        }
    }

    public class SecondStepReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {
    	public void reduce(Text key, Iterable<DoubleWritable> values, Context context) throws IOException, InterruptedException {
    		double result = 0;
    		Iterator<DoubleWritable> it = values.iterator();
    		while(it.hasNext()) {
    			result += it.next().get();
    		}
    		context.write(key, new DoubleWritable(result));
    	}
    }
 
    public static class FirstStepReduce extends Reducer<Text, Text, Text, DoubleWritable> {
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            String[] value;
            ArrayList<Entry<Integer, Float>> listA = new ArrayList<Entry<Integer, Float>>();
            ArrayList<Entry<Integer, Float>> listB = new ArrayList<Entry<Integer, Float>>();
            for (Text val : values) {
                value = val.toString().split(",");
                if (value[0].equals("A")) {
                    listA.add(new SimpleEntry<Integer, Float>(Integer.parseInt(value[1]), Float.parseFloat(value[2])));
                } else {
                    listB.add(new SimpleEntry<Integer, Float>(Integer.parseInt(value[1]), Float.parseFloat(value[2])));
                }
            }
            String i;
            float a_ij;
            String k;
            float b_jk;
            DoubleWritable outputValue;
            Text out_key;
            for (Entry<Integer, Float> a : listA) {
                i = Integer.toString(a.getKey());
                a_ij = a.getValue();
                if (a_ij != 0){
	                for (Entry<Integer, Float> b : listB) {
	                    k = Integer.toString(b.getKey());
	                    b_jk = b.getValue();
	                    if(b_jk != 0){
		                    out_key = new Text(i + "," + k);
		                    outputValue = new DoubleWritable(a_ij * b_jk);
		                    context.write(out_key, outputValue);
	                    }
	                }
                }
            }
        }
    }
 
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
 
        Job job = Job.getInstance(conf, "MatrixMatrixMultiplicationTwoSteps");
        job.setJarByClass(aggregated_graphs.TwoStepMatrixMultiplication.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);
 
        job.setMapperClass(Map.class);
        job.setReducerClass(FirstStepReduce.class);
 
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
 
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        
        Job job2 = Job.getInstance(conf, "MatrixMatrixMultiplicationTwoSteps 2");
        
        //job2.setJarByClass(TwoStepMatrixMultiplication.class);
        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(DoubleWritable.class);
 
        job2.setMapperClass(IdentityMapperForDoubles.class);
        job2.setReducerClass(SecondStepReducer.class);
 
        job2.setInputFormatClass(TextInputFormat.class);
        job2.setOutputFormatClass(TextOutputFormat.class);
 
        FileInputFormat.addInputPath(job, new Path(args[1]));
        FileOutputFormat.setOutputPath(job, new Path(args[2]));
 
        job.submit();
        if(job.waitForCompletion(true)){
        	job2.submit();
        }
        
        boolean success = job2.waitForCompletion(true);
        
    }
}
