package graphtesting;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

public class GenerateGraph {

	/**
	 * @param args[0] = number of nodes to generate
	 * @param args[1] = maximum number of outgoing arcs for each  node
	 * @param args[2] = minimum arc weight
	 * @param args[3] = maximum arc weight
	 * @param args[4] = output file (not mandatory)
	 */
	public static void main(String[] args) {
		int nodes = Integer.parseInt(args[0]);
		int max_arcs = Integer.parseInt(args[1]);
		double minweight = Double.parseDouble(args[2]);
		double maxweight = Double.parseDouble(args[3]);
		Writer out = new OutputStreamWriter(System.out);;
		if (args.length == 5) {
			try {
				out = new PrintWriter(args[4]);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (int i = 0; i < nodes; i++) {
			int arc_number = (int) (Math.random() * (max_arcs+1));
			int min_arcVal = 0;
			StringBuffer arcs = new StringBuffer(i+"\t");
			while (arc_number-- > 1 && min_arcVal < nodes) {
				int generatedVal = (int)(min_arcVal + Math.random()*(nodes - min_arcVal));
				min_arcVal = generatedVal;
				arcs.append(generatedVal);
				arcs.append(':'); 
				arcs.append(minweight + Math.random()*(maxweight - minweight));
				arcs.append('\t');
			}
			try {
				out.write(arcs.toString().trim()+"\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
