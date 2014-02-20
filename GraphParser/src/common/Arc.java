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
		// TODO Auto-generated method stub
		return 0;
	}
}
