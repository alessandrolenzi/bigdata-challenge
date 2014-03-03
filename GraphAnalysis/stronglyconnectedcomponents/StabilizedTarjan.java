package stronglyconnectedcomponents;

import graph.UnvalidFileFormatException;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;


import common.Arc;
import customreader.CustomReader;

public class StabilizedTarjan extends TarjanSCC {
	
	private int relativeNodeIndex = 0;
	
	public StabilizedTarjan(File trafficDir, File hourDir, File destDir) {
		super(trafficDir, hourDir, destDir);
	}
	
	
	public StabilizedTarjan(File trafficDir, File hourDir, File destDir,
			Comparator<NodeTraffic> vOrd, Comparator<Arc> aOrd) {
		super(trafficDir, hourDir, destDir, vOrd, aOrd);
	}

	@Override
	public boolean hasNext() {
		return relativeNodeIndex < allNodes.size()-1;
	}
	
	@Override
	public Integer next() {
		return allNodes.get(relativeNodeIndex++).identifier;
	}
	
	@Override
	/**
	 * In this case the order is the same for every hour, to check if SCC remain if we do it like this.
	 */
	protected void loadVisitOrder(String whatever, Comparator<NodeTraffic> comp) throws IOException{
		if (allNodes.size() > 0){
			relativeNodeIndex = 0;
			return;
		}
		if(!traffic.isDirectory()) { //Load from the traffic file all the information needed.
			super.loadVisitOrder(traffic.getAbsolutePath(), comp);
			relativeNodeIndex = 0;
			return;
		}
		super.loadVisitOrder(whatever, comp);
		relativeNodeIndex = 0;
	}
	
	private static double hourlyWeightedCut(int hr, File hourGraphDirectory, double percentile) throws IOException {
		String filename = hourGraphDirectory.getAbsolutePath()+"/"+hr+".hr";
		CustomReader r = new CustomReader(filename, 1024*1024*10L, 1024, new char[]{'\n', '\t', ':'});
		Ranker ranker = new Ranker(percentile, 1000000);
		while (!r.fileEnded()) {
			r.next();
			while(!r.lineEnded()) {
				r.next();
				ranker.checkElement(Double.parseDouble(r.next().trim()));
			}
		}
		return ranker.returnPercentile();
	}
	/**
	 * @param args[0] the traffic file for node visit order
	 * @param args[1] the directory obtaining the .hr files, with the hour graph representation
	 * @param args[2] the directory in which the cfc files and their logs have to be written
	 * @param args[3] the probability value for which arcs lte won't be considered.
	 */
	public static void main(String[] args) {
		try {
			File trafficDirectory = new File(args[0]);
			File hourGraphDirectory = new File(args[1]);
			File destinationDirectory = new File(args[2]);
			double percentile = Double.parseDouble(args[3]);
			StabilizedTarjan calculator = 
					new StabilizedTarjan(trafficDirectory, hourGraphDirectory, destinationDirectory,
					new Comparator<NodeTraffic>() {
						public int compare(NodeTraffic o1, NodeTraffic o2) {
							return Double.compare(o1.traffic, o2.traffic);
						}
					},
					new Comparator<Arc>() {
						@Override
						public int compare(Arc o1, Arc o2) {
							return -1 * Double.compare(o1.getWeight(), o2.getWeight());
						}
						
					});
			while (calculator.hasNextHour()) {
				//System.err.println("Check next hour...");
				double cutValue = hourlyWeightedCut(calculator.whichHour(), hourGraphDirectory, percentile);
				System.err.println("Cutting graph with for hour "+calculator.whichHour()+" with "+ cutValue);
				calculator.calculateNextCFC(cutValue);
			}
			calculator.close();
		} catch (SecurityException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnvalidFileFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

}
