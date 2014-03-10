import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;


public class MitomiRecord implements Writable {

	int source, destination;
	long timestamp;
	double weight;
	
	public static MitomiRecord read (DataInput in) throws IOException {
		MitomiRecord m = new MitomiRecord();
		m.readFields(in);
		return m;
	}
	
	@Override
	public void readFields(DataInput arg0) throws IOException {
		long t = arg0.readLong();
		arg0.readChar();
		int s = arg0.readInt();
		arg0.readChar();
		int d = arg0.readInt();
		arg0.readChar();
		double w = arg0.readDouble();
		arg0.readChar();
		timestamp = t;
		source = s;
		destination = d;
		weight = w;
	}

	@Override
	public void write(DataOutput arg0) throws IOException {
		arg0.writeBytes(timestamp + "\t" + source + "\t" + destination + "\t" + weight);

	}

}
