package DiskIndex;
/**
 * Class used to index the file, to speed up search. 
 * Implementation of a Red-Black tree
 * @author alessandro
 *
 */
public class DiskIndex extends RedBlackBST<Integer, Long> {
	
	public class Interval<T> {
		public T beginning;
		public T end;
		public Interval(T int_begin, T int_end) {
			this.beginning = int_begin;
			this.end = int_end;
		}
		
	}
	
	public Interval<Long> getDiskInterval(int value) {
		return new Interval<Long>(searchFirstPreceeding(value), searchFirstFollowing(value));
	}

	private Long searchFirstFollowing(int value) {
		return super.get(super.ceiling(value));
	}

	private long searchFirstPreceeding(int value) {
		return super.get(super.floor(value));
	}
}
