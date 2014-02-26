package common;

public class Arc implements Comparable {
	private int starting_node;
	private int destination_node;
	private double weight;
	public Arc(int start, int destval, double w){starting_node=start;destination_node=destval;weight=w;}
	
	
	public int getStartNode_id(){return starting_node;}
	public int getEndNode_id(){return destination_node;}
	public double getWeight(){return weight;}
	
	public void setWeight(double d){weight = d;}

	@Override
	public int compareTo(Object o) {
		Arc a = (Arc) o;
		if (this.getStartNode_id() < a.getStartNode_id()) return -1;
		if (this.getStartNode_id() > a.getStartNode_id()) return 1;
		if (this.getEndNode_id() < a.getEndNode_id()) return -1;
		if (this.getEndNode_id() > a.getEndNode_id()) return 1;
		return 0;
	}
}
