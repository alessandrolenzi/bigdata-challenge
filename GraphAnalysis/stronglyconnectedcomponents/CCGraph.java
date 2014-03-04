package stronglyconnectedcomponents;

import java.util.Set;

import common.Arc;
import common.Graph;
import common.Node;
import common.exceptions.NodeNotFound;

public interface CCGraph<N extends Node<A>, A extends Arc> extends Graph<N, A>{
	public abstract Set<Integer> findStronglyConnectedComponent(int node_id) throws NodeNotFound; 
}
