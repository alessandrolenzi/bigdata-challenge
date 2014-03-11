package markov_clustering;

import org.apache.hadoop.conf.Configuration;

/** Generates the clusters for the given aggregated graphs
 * 
 * @author alessandro
 *
 */
public class Markov_Clustering {

	/**
	 * @param args[0] input folder
	 * @param args[1] output folder
	 * @param arg[2] maximum number of iterations
	 * @param arg[3] number of workers (not mandatory; if not specified will be one for each row)
	 */
	public static void main(String[] args) {
		Configuration clusterConf = new Configuration();
		int iterations = 0;
		int maxIterations = Integer.parseInt(args[2].trim());
		boolean converged = false;
		do {
			iterations++;
			runMultiplication();
			
		} while (!(converged = convergence()) && maxIterations > iterations);
		if (converged) {
			System.out.println("Reached convergency");
		} else 
			System.out.println("Convergency not reached");
	}
	
	/** Runs the job for matrix multiplication*/
	private static void runMultiplication() {
		
	}
	
	private static boolean convergence() {
		// TODO Auto-generated method stub
		return false;
	}

	

}
