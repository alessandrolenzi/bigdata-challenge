package matrix_multiplication;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper.Context;


/**
 * Matrix Multiplication chain
 * @author alessandro
 *
 */
public class MatrixMultiplication {

	public static void main(String[] args) {
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

	}

}
