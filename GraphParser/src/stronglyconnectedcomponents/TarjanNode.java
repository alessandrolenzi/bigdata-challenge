package stronglyconnectedcomponents;

import common.Arc;
import common.Node;

public class TarjanNode extends Node<Arc>{
	private int index = -1; /** Index of the node*/
	public int lowIndex = Integer.MAX_VALUE;
	public TarjanNode(int nodeval) {super(nodeval);}
	/** Index management*/
	public boolean isIndexed() { return index != -1;}
	public int getIndex() { return index;}
	public void setIndex(int index_value) { index = index_value;}
	public void setLowIndex(int index2) {
		lowIndex = Math.min(lowIndex, index2);		
	}
	public int getLowIndex() {
		return lowIndex;
	}
	
	public boolean equals(Object o) {
		TarjanNode conf = (TarjanNode) o;
		return (conf.getId() == this.getId());
	}
	
}
