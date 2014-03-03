package common;

import java.util.Set;

import com.google.common.base.Predicate;

import common.exceptions.ArcNotFound;
import common.exceptions.NodeNotFound;
import common.exceptions.UnvalidArc;

/**
 * This class is an abstract class defining the common functionalities of a graph
 * @author alessandro
 *
 */
public interface Graph<N extends Node<A>, A extends Arc> {

	/**
	 * @param node_id, the node identifier
	 * @return the node with the selected id
	 * @throws NodeNotFound exception in case the node is not found
	 */
	public abstract N getNode(int node_id) throws NodeNotFound;
	
	/**
	 * @throws UnvalidNodeException
	 * @param node the node to be inserted in the graph
	 */
	public abstract void putNode(N node);
	
	
	/**
	 * @param node_id the id of the node 
	 * @return the set of arcs outgoing from the node with the specified id
	 * @throws NodeNotFound exception in case the node is not found
	 */
	public abstract Set<A> getAllOutgoingArcs(int node_id) throws NodeNotFound;
	
	/**
	 * 
	 * @param n the node  
	 * @return the set of arcs outgoing from the node specified
	 * @throws NodeNotFound exception in case the node is not found
	 */
	public abstract Set<A> getAllOutgoingArcs(N n) throws NodeNotFound;
	
	/**
	 * @param start_node the id of starting node
	 * @param dest_node the id of destination node
	 * @return the retrieved arc
	 * @throws ArcNotFound in case the arc specified doesn't exist
	 */
	public abstract A getArc(int start_node, int dest_node) throws ArcNotFound;
	
	/**
	 * @param start_node the starting node
	 * @param dest_node the ending node
	 * @return the retrieved arc
	 * @throws ArcNotFound in case the arc specified doesn't exist
	 */
	public abstract A getArc(N start_node, N dest_node) throws ArcNotFound;
	
	/**
	 * @param arc the arc to be added
	 * @throws UnvalidArc in case the arc is not valid
	 */
	public abstract void addArc(A arc) throws UnvalidArc; 
	
	/**
	 * Removes all the arcs for which p doesn't hold
	 * @param p the predicate 
	 * @return a new graph.
	 */
	public abstract void cutArcs(Predicate<A> p);
	/**
	 * Save changes
	 */
	//public abstract void commit(); 
	
	/**
	 * Destructor
	 */
	public abstract void close();
	
	
	public abstract void finalize();

	
}
