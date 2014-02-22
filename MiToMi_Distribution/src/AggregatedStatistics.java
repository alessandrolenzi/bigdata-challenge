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
		
		ExecutorService pool =  Executors.newFixedThreadPool(Math.min(MAX_POOL_SIZE, args.length - 1));
		ArrayList<Future<String>> res = new ArrayList<Future<String>>();
		Date start = new Date();
		
		if(args.length < 2) {
			printUsage(args);
		}
		
		// for each file in command arguments
		for(int i = 1; i < args.length; i++){
			Callable<String> c;
			
			if(args[0].equals("s")){
				c  = new DayAnalyzeWorker(args[i],DayAnalyzeWorker.JobType.SPLIT);
			} else {
				c  = new DayAnalyzeWorker(args[i],DayAnalyzeWorker.JobType.ANALYSIS);
			}
				
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
		System.out.println("Usage: " + args[0] + " {-a|-s} file1 [file2 ..]\n\t-a : analyze mean and variance\n\t-s : split by period");
		
	}

}
