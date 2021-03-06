import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class AggregatedStatistics {
	
	static int MAX_POOL_SIZE = 8;
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		
		if(args.length < 1) {
			printUsage(args);
		}
		
		ExecutorService pool =  Executors.newFixedThreadPool(Math.min(MAX_POOL_SIZE, args.length));
		ArrayList<Future<String>> res = new ArrayList<Future<String>>();
		Date start = new Date();
		
		// for each file in command arguments
		for(int i = 0; i < args.length; i++){
			Callable<String> c;
			
			c  = new DayAnalyzeWorker(args[i]);
				
			Future<String> f = pool.submit(c); 
			res.add(f);
		}
		
		pool.shutdown();
		try {
			pool.awaitTermination(20, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Iterator<Future<String>> it = res.iterator();
		while(it.hasNext()){
			System.out.println(it.next().get() + "\n");
		}
		
		Date end = new Date();
		System.out.println("== Finished in " + ((end.getTime() - start.getTime())/1000) + " seconds ==");
		
	}

	private static void printUsage(String[] args) {
		System.out.println("Usage: " + args[0] + " file1 [file2 ..]\nAggregates arcs by hours, creates" + 
				"one file for each our and computes mean and variance over the obtained values." );
		
	}

}
