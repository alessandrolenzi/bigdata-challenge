package stronglyconnectedcomponents;

import java.io.IOException;
import java.util.BitSet;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import common.Arc;
import common.exceptions.NodeNotFound;

import actual.BufferedGraph;

public class TarjanEnabledGraph extends BufferedGraph<TarjanNode, Arc> {
	
	private BitSet indexedNodes = new BitSet();
	private int index = 0;
	private Stack<TarjanNode> stack = new Stack<TarjanNode>();
	
	/** Constructor with fixed nodes and arcs class */
	public TarjanEnabledGraph(String str, long max_memory) throws NoSuchMethodException, SecurityException, IOException {
		super(str, max_memory, TarjanNode.class, Arc.class);
	}
	
	private boolean isIndexed(int node_id) { return indexedNodes.cardinality() >= node_id && indexedNodes.get(node_id) == true;}
	
	/**
	 * Returns the node with the requested id if found and the node doesn't belong to a SCC
	 * @param node_id the id of the node to get
	 * @return the node if it is not indexed and it exists
	 * @throws NodeNotFound in case the node doesn't exist or is already indexed.
	 */
	public TarjanNode getIfNotAssigned(int node_id) throws NodeNotFound {
		if (isIndexed(node_id)) return this.getNode(node_id);
		throw new NodeNotFound("Node is indexed"); //Replace with indexed
	}
	
	/**
	 * find the strongly connected component of a certain node identifier.
	 * @param node_id
	 * @return
	 * @throws NodeNotFound
	 */
	public Set<Integer> findStronglyConnectedComponent(int node_id) throws NodeNotFound {
		if (isIndexed(node_id)) return null; 
		TarjanNode first = this.getNode(node_id);
		first.setIndex(index);
		first.setLowIndex(index);
		
		Set<Arc> outgoing_arcs =  this.getAllOutgoingArcs(node_id);
		for (Arc current: outgoing_arcs) {
			try {
			TarjanNode destination = getNode(current.getEndNode_id()); 
			if (!isIndexed(current.getEndNode_id())) {
				findStronglyConnectedComponent(destination.getIndex());
				first.setLowIndex(destination.getLowIndex());
			} else {
				first.setLowIndex(destination.getIndex());
			}
		
			} catch (NodeNotFound exc) {
				//do nothing.
			}
		}
		
		if (first.getLowIndex() == first.getIndex()) { //first is a root node
			Set<Integer> scc = new TreeSet<Integer>();
			TarjanNode popped = null;
			do {
				popped = stack.pop();
				scc.add(popped.getId());
			} while(popped != first);
		}
		
		return null;
	}
	
	
}
