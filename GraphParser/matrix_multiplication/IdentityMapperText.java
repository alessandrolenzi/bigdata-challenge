package matrix_multiplication;
import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;

public class IdentityMapperText extends Mapper<Text, Text, Text, Text> {
   	public void map(Text key, Text value, Context context) throws IOException, InterruptedException {
   		context.write(key, value);
   	}
}