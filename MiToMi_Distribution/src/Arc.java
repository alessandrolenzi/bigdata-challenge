public class Arc {
		
		private int SourceId, DestId;
		private long Timestamp;
		private double Strength;
		
		public int getSourceId() {
			return SourceId;
		}

		public int getDestId() {
			return DestId;
		}

		public long getTimestamp() {
			return Timestamp;
		}

		public double getStrength() {
			return Strength;
		}

		private void setSourceId(int sourceId) {
			SourceId = sourceId;
		}

		private void setDestId(int destId) {
			DestId = destId;
		}

		private void setTimestamp(long timestamp) {
			Timestamp = timestamp;
		}

		private void setStrength(double strength) {
			Strength = strength;
		}

		public int getPeriodId(long initialTime, long aggregationPeriod){
			return (int) ((this.getTimestamp() - initialTime) / aggregationPeriod);
		}
		
		public Arc(String s) {
			String[] tokens = s.split("\t");
			this.setTimestamp(Long.parseLong(tokens[0]));
			this.setSourceId(Integer.parseInt(tokens[1]));
			this.setDestId(Integer.parseInt(tokens[2]));
			this.setStrength(Double.parseDouble(tokens[3]));
		}
		
		public String prettyHeadTail () {
			return "(" + this.SourceId + "," + this.DestId + ")";
		}
		
	}