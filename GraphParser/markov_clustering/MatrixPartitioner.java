package markov_clustering;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

public class MatrixPartitioner extends Partitioner<IntWritable, Text> {

	public int getPartition(IntWritable arg0, Text arg1, int arg2) {
		return arg0.get(); //for now. Then make it parametric.
	}

}
