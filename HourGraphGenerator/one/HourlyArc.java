package one;

public class HourlyArc implements Comparable<HourlyArc> {
	public int startID, endID, hourID;
	public double strength;
	public HourlyArc(int start, int end, int hour, double directionalStrength) {
		startID = start; endID = end; hourID = hour;
		strength = directionalStrength;
	}
	
	public void update(int start, int end, int hour, double s) throws NotSameArcException{
		if (startID != start || endID != end || hourID != hour) throw new NotSameArcException("Not the same");
		strength += s;
	}
	
	public boolean equals(Object o) {
		HourlyArc that = (HourlyArc) o;
		return this.startID == that.startID && this.endID == that.endID && this.hourID == that.endID && this.strength == that.strength;
	}
	
	/** *
	 * Sort by: hour, startNode, endNode.
	 */
	public int compareTo(HourlyArc that) {
		if (this.hourID < that.hourID) return -1;
		if (this.hourID > that.hourID) return 1;
		if (this.startID < that.startID) return -1;
		if (this.startID > that.startID) return 1;
		if (this.endID < that.endID) return -1;
		if (this.endID > that.endID) return 1;
		return 0;
	}

}
