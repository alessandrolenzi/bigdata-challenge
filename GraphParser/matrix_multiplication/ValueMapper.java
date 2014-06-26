package matrix_multiplication;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class ValueMapper extends Mapper<Text, Text, Text, Text> {
	 public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
         String line = value.toString();
         /** TODO: fix*/
         /** format: MatrixName[0],row[1],column[2],value[3]*/
         String[] indicesAndValue = line.split(",");
         Text outputKey = new Text();
         Text outputValue = new Text();
         //first matrix
         if (indicesAndValue[0].equals("A")) {
        	 /** Key is column */
             outputKey.set(indicesAndValue[2]);
             //for first matrix, throw out the row
             outputValue.set("A," + indicesAndValue[1] + "," + indicesAndValue[3]);
             context.write(outputKey, outputValue);
         } else {
        	 //second matrix, for which we append to the value the column identifier
        	 /** Key is row */
             outputKey.set(indicesAndValue[1]);
             outputValue.set("B," + indicesAndValue[2] + "," + indicesAndValue[3]);
             context.write(outputKey, outputValue);
         }
     }
}
