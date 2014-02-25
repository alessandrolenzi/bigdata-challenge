package common;

import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

/**
 * A node in the graph
 * @author alessandro
 *
 */

@SuppressWarnings("rawtypes")
public class Node<A extends Arc> implements Comparable {
	private TreeSet<A> arcs = new TreeSet<A>();
	private int identifier;
	public Node(int nodeval) {identifier=nodeval;}
	public int getId(){return identifier;}
	public void putArc(A a){arcs.add(a);}
	
	@Override
	@SuppressWarnings("unchecked")
	public int compareTo(Object o) { //Comparator for ordering.
		Node<A> compared = (Node<A>) o;
		if (this.getId() < compared.getId()) return -1;
		if (this.getId() > compared.getId()) return 1;
		return 0;
	}
	
	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		if (o.getClass().isAssignableFrom(Node.class)) return ((Node<A>)o).getId() == this.getId();
		return false;
	}
	
	public Set<A> getArcs(Predicate<? super A> predicate) {
		return Sets.filter(arcs, predicate);
	}
}
