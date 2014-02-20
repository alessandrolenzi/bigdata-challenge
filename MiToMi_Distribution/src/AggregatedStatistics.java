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
	
	static int POOL_SIZE = 1;
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		
		ExecutorService pool =  Executors.newFixedThreadPool(POOL_SIZE);
		ArrayList<Future<String>> res = new ArrayList<Future<String>>();
		Date start = new Date();
		
		// for each file in command arguments
		for(String filename : args){
			Callable<String> c  = new DayAnalyzeWorker(filename);
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

}
