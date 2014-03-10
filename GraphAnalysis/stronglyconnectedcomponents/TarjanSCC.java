package stronglyconnectedcomponents;

import graph.UnvalidFileFormatException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import com.google.common.base.Predicate;


import common.Arc;
import common.exceptions.NodeNotFound;
import customreader.CustomReader;

public class TarjanSCC implements Iterator<Integer>{
	
	protected File traffic;
	protected File hour;
	protected File destination;
	protected Stack<NodeTraffic> allNodes = new Stack<NodeTraffic>();
	private Comparator<NodeTraffic> visitOrder = null;
	private Comparator<Arc> arcOrder = null;
	private Writer currentCFCWriter = null;
	private Writer currentLogWriter = null;
	private Writer currentErrorWriter = null;
	/**
	 * Constructor
	 * @param trafficDir directory/file from which the traffic is loaded
	 * @param hourDir directory/file from which the ours are loaded
	 * @param destDir directory where to write the results
	 * @param vOrd comparator determining the visit order of the nodes.
	 */
	public TarjanSCC(File trafficDir, File hourDir, File destDir, Comparator<NodeTraffic> vOrd, Comparator<Arc> aOrd) {
		traffic = trafficDir; hour = hourDir; destination = destDir;
		if (vOrd == null) { /** Default order: increasing outgoing traffic.*/
			visitOrder = new Comparator<NodeTraffic>() {
				@Override
				public int compare(NodeTraffic o1, NodeTraffic o2) {
					return Double.compare(o1.traffic, o2.traffic);
				}
			};
		} else  visitOrder = vOrd;
		arcOrder = aOrd;
	}
	
	public TarjanSCC(File trafficDir, File hourDir, File destDir) {
		this(trafficDir, hourDir, destDir, null, null);
	}
	/*** Iterator<Integer> implementation: for scanning through the nodes. Can be called again once
	 * loadVisitOrder has been executed.*/
	@Override
	public boolean hasNext() {
		return allNodes.size() > 0;
	}
	@Override
	public Integer next() {
		return allNodes.pop().identifier;
	}
	@Override
	public void remove() {
		allNodes.pop();
	}
	/***** Hour iterator.*/
	int currentHour = 0;
	public int nextHour() {
		return currentHour++;
	}
	public int whichHour() {
		return currentHour;
	}
	public boolean hasNextHour() {
		return currentHour < 24;
	}

	/**** Loading all information for a certain hour***/
	private ExtendedTarjanGraph loadHour(int hour) throws IOException, UnvalidFileFormatException {
		this.loadVisitOrder(traffic.getAbsolutePath()+"/"+hour+".traffic", visitOrder);
		this.loadOutputFiles(hour);
		try {
			/** Load the graph*/
			ExtendedTarjanGraph graph = new ExtendedTarjanGraph(this.hour.getAbsolutePath()+"/"+hour+".hr", 1024*1024*1024);
			if(arcOrder != null) graph.setArcOrder(arcOrder);
			return graph;
		} catch (NoSuchMethodException e) {
			return null;
		} catch (SecurityException e) {
			return null;
		}
	}
	
	protected void loadVisitOrder(String filename, Comparator<NodeTraffic> comp) throws IOException {
		allNodes.removeAllElements();
		CustomReader reader;
		try {
			reader = new CustomReader(filename, 1024*1024*10L, 1024, new char[]{'\n', '\t'});
		} catch (IOException io) {
			io.printStackTrace();
			throw io;
		}
		/** Load all nodes*/
		while(!reader.fileEnded()) {
			String s1, s2;
			s1 = reader.next(); s2 = reader.next();
			if (s1 != null && s2 != null)
				allNodes.add(new NodeTraffic(s1, s2));
		}
		reader.close();
		reader = null;
		Collections.sort(allNodes, comp);
		return;
	}
	
	private void loadOutputFiles(int hour) throws IOException {
		if (currentCFCWriter != null) {
			currentCFCWriter.write("\n");
			currentCFCWriter.flush();
			currentCFCWriter.close();
			currentCFCWriter =  new FileWriter(destination.getAbsolutePath()+"/"+hour+".cfc");
			currentLogWriter.write("\n"+hour+":\t");
			return;
		}
		currentCFCWriter =  new FileWriter(destination.getAbsolutePath()+"/"+hour+".cfc");
		currentLogWriter = new FileWriter(destination.getAbsolutePath()+"/total.log");
		currentLogWriter.write("\n"+hour+":\t");
		currentErrorWriter = new FileWriter(destination.getAbsolutePath()+"/errors.log");
	}
	
	public void calculateNextCFC(final double cutvalue) throws IOException, UnvalidFileFormatException {
		//System.err.println("Calculating next scc");
		/** Do something to get the next analyzed hour**/
		if (!this.hasNextHour()) return;
		int currentHour = this.nextHour();
		//System.err.println("Hour is next scc");
		if (currentHour == -1) return;
		ExtendedTarjanGraph graph = this.loadHour(currentHour);
		//System.err.println("Graph Loaded");
		if (graph == null) return;
		
		/** Cut arcs under a certain value*/
		graph.cutArcs(new Predicate<Arc>() {
			public boolean apply(Arc a) {
				return a.getWeight() > cutvalue;
			}
		});
		//System.err.println("Arcs cutted");
		while (this.hasNext()) {
			int curNodeId = this.next();
			//System.err.println("Node:"+currentHour);
			if (curNodeId == -1) { return;}
			try {
				Set<Integer> foundSCC = graph.findStronglyConnectedComponentCaller(curNodeId);
				this.writeFoundInformation(foundSCC, currentHour, curNodeId);
			} catch (NodeNotFound e) {
				this.writeErrorLog("Node not found:"+curNodeId);
			}
		}
	
		graph.close();
		graph = null;
		
	}
	private void writeErrorLog(String string) throws IOException {
		currentErrorWriter.write(string+"\n");		
	}
	
	private void writeFoundInformation(Set<Integer> foundSCC, int node, int curHour) throws IOException {
		if(foundSCC == null || foundSCC.size() == 0) {
			writeErrorLog(curHour+": Node with id"+node+" doesn't have any associated SCC");
			return;
		}
		if (foundSCC.size() == 1) return;
		currentCFCWriter.write("{");
		Iterator<Integer> el= foundSCC.iterator();
		while (el.hasNext()) {
			currentCFCWriter.write(""+el.next());
			
			if(el.hasNext()) {
				currentCFCWriter.write(',');
			}
		}
		currentCFCWriter.write("}\n");
		currentLogWriter.write(foundSCC.size()+",");
	}
	
	public void close() {
		try {
			currentErrorWriter.flush();
			currentLogWriter.flush();
			currentCFCWriter.flush();
			currentErrorWriter.close();
			currentLogWriter.close();
			currentCFCWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void finalize() {
		close();
	}
	
	/**
	 * @param args[0] the directory containing the .traffic files, with the total traffic for every grid and hour 
	 * @param args[1] the directory ontaining the .hr files, with the hour graph representation
	 * @param args[2] the directory in which the cfc files and their logs have to be written
	 * @param args[3] the probability value for which arcs lte won't be considered.
	 */
	public static void main(String[] args) {
		try {
			File trafficDirectory = new File(args[0]);
			File hourGraphDirectory = new File(args[1]);
			File destinationDirectory = new File(args[2]);
			double cutValue = Double.parseDouble(args[3]);
			TarjanSCC calculator = new TarjanSCC(trafficDirectory, hourGraphDirectory, destinationDirectory);
			while (calculator.hasNextHour()) {
				//System.err.println("Check next hour...");
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
