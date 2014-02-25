package stronglyconnectedcomponents;

import java.io.IOException;
import java.util.Set;

import common.exceptions.NodeNotFound;

public class TarjanSCC {

	public static void main(String[] args) {
		String filename = args[0];
		try {
			TarjanEnabledGraph graph = new TarjanEnabledGraph(filename, 1024*1024*1024);
			int i = 0;
			Set<Integer> foundSCC = null; 
			do {
				try {
					foundSCC = graph.findStronglyConnectedComponent(i);
				} catch (NodeNotFound e) {
					//do nothing;
				}
			} while (foundSCC != null);
		} catch (NoSuchMethodException | SecurityException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
