package stronglyconnectedcomponents;

public class NodeTraffic implements Comparable<NodeTraffic> {
	public int identifier; public double traffic;
	
	public NodeTraffic(String token1, String token2) {
		identifier = Integer.parseInt(token1.trim()); 
		traffic = Double.parseDouble(token2.trim());
	}

	@Override
	public int compareTo(NodeTraffic that) {
		if (this.traffic < that.traffic) return -1;
		if (this.traffic > that.traffic) return 1;
		return 0;
	}
	
}
