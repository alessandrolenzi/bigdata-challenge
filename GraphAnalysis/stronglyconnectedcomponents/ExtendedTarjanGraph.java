package stronglyconnectedcomponents;

import graph.UnvalidFileFormatException;

import java.io.IOException;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Set;

import common.Arc;
import common.exceptions.NodeNotFound;

public class ExtendedTarjanGraph extends TarjanGraph {
	private BitSet CFCindex = new BitSet(10000);
	public ExtendedTarjanGraph(String str, long max_memory)	throws NoSuchMethodException, SecurityException, IOException, UnvalidFileFormatException {
		super(str, max_memory);
	}
	
	public ExtendedTarjanGraph(String str, long max_memory, Comparator<Arc> comparer)throws NoSuchMethodException, SecurityException, IOException, UnvalidFileFormatException {
		super(str, max_memory, comparer);
	}
	
	protected final void resetVisitIndex() {
		super.indexedNodes = new BitSet(10000);
	}
	
	private final boolean CFCIndexed(int node_id) {
		return node_id >= 0 && CFCindex.size() >= node_id && CFCindex.get(node_id) == true;
	}
	
	@Override
	protected boolean isIndexed(int value) {
		return super.isIndexed(value) || this.CFCIndexed(value);
	}
	
	public Set<Integer> findStronglyConnectedComponentCaller(int node_id) throws NodeNotFound {
		Set<Integer> scc = super.findStronglyConnectedComponent(node_id);
		if (scc != null)
			for(Integer s: scc)	CFCindex.set(s);
		this.resetVisitIndex();
		return scc;
	}
	

}
