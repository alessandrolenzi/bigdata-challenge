package common.exceptions;


public class ArcNotFound extends Exception {
	private static final long serialVersionUID = 4057720427946307747L;
	public ArcNotFound(int start_node, int dest_node, String str) {super(str);}
}
