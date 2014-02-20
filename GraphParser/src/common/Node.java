package common;

import java.util.TreeSet;

/**
 * A node in the graph
 * @author alessandro
 *
 */
public class Node implements Comparable {
	private TreeSet<Arc> arcs = new TreeSet<Arc>();
	private int identifier;
	public Node(int nodeval) {identifier=nodeval;}
	public int getId(){return identifier;}
	public void putArc(Arc a){arcs.add(a);}
	@Override
	public int compareTo(Object o) { //Comparator for ordering.
		Node compared = (Node) o;
		if (this.getId() < compared.getId()) return -1;
		if (this.getId() > compared.getId()) return 1;
		return 0;
	}
	
	public boolean equals(Object o) {
		if (o.getClass().isAssignableFrom(Node.class)) return ((Node)o).getId() == this.getId();
		return false;
	} 
}
