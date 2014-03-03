package common.exceptions;

import common.Arc;
import common.Node;


public class UnvalidNode extends Exception {
	private static final long serialVersionUID = -885188935524677860L;
	public UnvalidNode(Node<? extends Arc> n, String str) {super(str);}
}
