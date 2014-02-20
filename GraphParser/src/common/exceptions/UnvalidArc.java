package common.exceptions;

import common.Arc;


public class UnvalidArc extends Exception {
	private static final long serialVersionUID = -1723160585984903098L;
	public UnvalidArc(Arc a, String str) {super (str);}
}
