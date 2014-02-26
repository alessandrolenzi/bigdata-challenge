package stronglyconnectedcomponents;

import java.io.IOException;
import java.util.Set;

import com.google.common.base.Predicate;

import actual.UnvalidFileFormatException;

import common.Arc;
import common.exceptions.NodeNotFound;

public class TarjanSCC {

	public static void main(String[] args) {
		String filename = args[0];
		final Double cutVal = Double.parseDouble(args[1]);
		try {
			TarjanEnabledGraph graph = new TarjanEnabledGraph(filename, 1024*1024*1024);
			int i = 1;
			Set<Integer> foundSCC = null; 
			graph.cutArcs(new Predicate<Arc>() {
				public boolean apply(Arc a) {
					return a.getWeight() > cutVal;
				}
			});
			do {
				try {
					foundSCC = graph.findStronglyConnectedComponent(i);
					if(foundSCC != null && foundSCC.size() > 1) {
						System.out.print("{");
						for (Integer nid: foundSCC) {
							System.out.print(nid+",");
						}
						System.out.println("}");
					}
				} catch (NodeNotFound e) {
					//System.err.println("Node with id "+i+"hasn't been found");
				}
			} while (i++ < 10000);
		
		} catch (NoSuchMethodException | SecurityException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnvalidFileFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
