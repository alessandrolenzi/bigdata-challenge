package common;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

/**
 * A node in the graph
 * @author alessandro
 *
 */

public class Node<A extends Arc> implements Comparable<Node<A>> {
	private TreeSet<A> arcs = new TreeSet<A>();
	private int identifier;
	public Node(int nodeval) {identifier=nodeval;}
	public int getId(){return identifier;}
	public void putArc(A a){arcs.add(a);}
	
	@Override
	public int compareTo(Node<A> compared) { //Comparator for ordering.
		return Integer.compare(this.getId(), compared.getId());
	}
	
	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		if (o.getClass().isAssignableFrom(Node.class)) return ((Node<A>)o).getId() == this.getId();
		return false;
	}
	
	public Set<A> getArcs(Predicate<A> predicate) {
		if (arcs != null && predicate != null) return Sets.filter(arcs, predicate);
		if (predicate == null) return arcs; 
		return null;
	}
	public Set<A> getArcs(Predicate<A> predicate, Comparator<A> comparator) {
		@SuppressWarnings("unchecked")
		List<A> arcs_unordered = (List<A>) Arrays.asList(this.getArcs(predicate).toArray(new Arc[]{}));
		Collections.sort(arcs_unordered, comparator);
		return new HashSet<A>(arcs_unordered);
	}
}
