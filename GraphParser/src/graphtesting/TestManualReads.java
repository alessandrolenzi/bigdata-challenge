package graphtesting;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.sourceforge.sizeof.SizeOf;

import common.Arc;
import common.Node;
import common.exceptions.NodeNotFound;

import actual.BufferedGraph;
import actual.UnvalidFileFormatException;

public class TestManualReads {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length <= 1) {
			try {
				SizeOf.skipStaticField(true);
				BufferedGraph<Node, Arc> buf = new BufferedGraph<Node, Arc>(args[0], 1024L*1024L*100, Node.class, Arc.class);
				buf.load();
				Node n = buf.getNode(5000);
				System.out.println(n.getId());
				Node n2 = buf.getNode(2);
				Node n3 = buf.getNode(9990);
				//System.out.println(SizeOf.humanReadable(SizeOf.deepSizeOf(buf)));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch(UnvalidFileFormatException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NodeNotFound e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return;
		}
		
	}

}
