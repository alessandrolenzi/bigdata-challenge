package totalaggregator;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import customreader.CustomReader;

public class GlobalTraffic {
	
	public static Vector<Double> total = new Vector<Double>();
	/**
	 * @param args[0] the directory containing the .traffic files to be aggregated
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		File f = new File(args[0]);
		if(!f.isDirectory()) return;
		/** Calculate daily traffic outgoing from each cell*/
		for(File traffic: f.listFiles()) {
			if (traffic.isFile()) {
				CustomReader cr = new CustomReader(traffic.getAbsolutePath(), 1024*1024*10L, 1024, new char[]{'\n', '\t'});
				while (!cr.fileEnded()) {
					String s1 = cr.next();
					String s2 = cr.next();
					if(s1 != null && s2 != null) incrementTraffic(s1, s2);
				}
			}
		}
		for(int i = 0; i < total.size(); i++) {
			if (total.get(i) != null) {
				System.out.println(i+"\t"+total.get(i));
			} else {
				System.out.println(i+"\t0");
			}
		}
	}

	private static void incrementTraffic(String s1, String s2) {
		int gridId = Integer.parseInt(s1.trim());
		double value = Double.parseDouble(s2.trim());
		if(total.size() < gridId) {
			total.setSize(gridId+1);
			total.set(gridId, value);
			return;
		}
		if (total.get(gridId) != null)
			total.set(gridId, total.get(gridId)+value);
		else total.set(gridId, value);
	}

}
