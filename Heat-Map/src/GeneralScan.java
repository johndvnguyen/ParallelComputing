import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class GeneralScan<ElemType, TallyType> {
	
	public class ComputeReduction extends RecursiveAction{
		//public variables
		int i;
		TallyType reduction;
		//constructor
		public ComputeReduction(int i) {
			this.i = i;
		}
		//function to be picked up by forkjoinpool
		protected void compute() {
			if(!isLeaf(i)) {
				if(i < N_THREADS-2) {//make new thread
					
					invokeAll(new ComputeReduction(right(i)));
					directCompute(left(i));
					
					//System.out.println("active thread count: " + threadPool.getActiveThreadCount());

				}
				else {//no thread creation
					directCompute(left(i));
					
					directCompute(right(i));
				}
				
				interior.set(i, combine(value(left(i)), value(right(i))));
				
			}
		}
		protected void directCompute(Integer i) {
			if(!isLeaf(i)) {
				if(i < N_THREADS-2) {//make new thread
					invokeAll(new ComputeReduction(right(i)));
					//invokeAll(new ComputeReduction(right(i)));
					directCompute(left(i));
					//System.out.println("active thread count: " + threadPool.getActiveThreadCount());
				}
				else {//no thread creation
					directCompute(left(i));
					directCompute(right(i));
				}
				
				interior.set(i, combine(value(left(i)), value(right(i))));
				
			}
			
		}
	}
	
	public class ComputeScan extends RecursiveAction{
		//public variables
		int i;
		TallyType tallyPrior;
		ArrayList<TallyType> output;
		//constructor
		public ComputeScan(int i, TallyType tallyPrior, ArrayList<TallyType> output) {
			this.i = i;
			this.tallyPrior= tallyPrior;
			this.output = output;
		}
		//function to be picked up by forkjoinpool
		protected void compute() {
			if (isLeaf(i)) {
				output.set(i - (n-1), combine(tallyPrior, value(i)));
			} else {
				if (i < N_THREADS-2) {
					//need to replace with threads
					invokeAll(new ComputeScan(left(i), tallyPrior, output));
					directCompute(right(i), combine(tallyPrior, value(left(i))), output);
				} else {
					directCompute(left(i), tallyPrior, output);
					directCompute(right(i), combine(tallyPrior, value(left(i))), output);
				}
			}
		}
		protected void directCompute(int i, TallyType tallyPior, ArrayList<TallyType> output) {
			if (isLeaf(i)) {
				output.set(i - (n-1), combine(tallyPrior, value(i)));
			} else {
				if (i < N_THREADS-2) {
					//need to replace with threads
					invokeAll(new ComputeScan(left(i), tallyPrior, output));
					directCompute(right(i), combine(tallyPrior, value(left(i))), output);
				} else {
					directCompute(left(i), tallyPrior, output);
					directCompute(right(i), combine(tallyPrior, value(left(i))), output);
				}
			}
			
		}
	}
	
	//private class variables
	private List<ElemType> rawData; //static array as this does not change
	private List<TallyType> interior; 
	private final int ROOT = 0;
	private boolean reduced;
	private int height;
	private int n;
	private ForkJoinPool threadPool;
	
	//constant public thread
	public final int N_THREADS=16;
	
	//constructor
	public GeneralScan(List<ElemType> raw) {
		this.reduced = false;
		this.n = raw.size();
		this.rawData = raw;
		//calculate log base 2 of n 
		this.height = (int) Math.ceil(Math.log(n)/Math.log(2));
		//n-1 interior nodes
		interior = new ArrayList<TallyType>(n-1);
		//initialize the arraylist
		while(interior.size()<n-1)
			interior.add(init());
		// must be power of 2
		if(1<<height != n) {
			throw new IllegalArgumentException("n must be a power of 2");
		}
		
		//define thread pool
		this.threadPool = new ForkJoinPool(N_THREADS);
	}
	
	//protected methods
	protected TallyType init() {
		throw new IllegalArgumentException("This function to be overwritten");
	}
	
	protected TallyType prepare(ElemType datum) {
		throw new IllegalArgumentException("This function to be overwritten");
	}
	
	protected TallyType combine(TallyType left, TallyType right) {
		throw new IllegalArgumentException("This function to be overwritten");
	}
	
	//private methods
	private int size() {
		return (n-1) +n;
	}
	
	private TallyType value(int i) {
		if(i<n-1)
			return interior.get(i);
		else 
			return prepare(rawData.get(i-(n-1)));
	}
	
	private int parent(int i) {
		return (i-1)/2;
	}
	
	private int left(int i) {
		return i*2+1;
	}
	
	private int right(int i) {
		return left(i)+1;
	}
	
	private boolean isLeaf(int i) {
		return right(i) >= size();
	}
	
//	private boolean reduce(int i) {
//				
//
//	}
	
	private void scan(int i, TallyType tallyPrior, ArrayList<TallyType> output) {
		if (isLeaf(i)) {
			output.set(i - (n-1), combine(tallyPrior, value(i)));
		} else {
			if (i < N_THREADS-2) {
				//need to replace with threads
				scan(left(i), tallyPrior, output);
				scan(right(i), combine(tallyPrior, value(left(i))), output);
			} else {
				scan(left(i), tallyPrior, output);
				scan(right(i), combine(tallyPrior, value(left(i))), output);
			}
		}
	}
	
	//public methods

	public TallyType getReduction(int i) {
		
		threadPool.invoke( new ComputeReduction(i));
		//return true;
		
		//reduced = reduced || reduce(ROOT);
		return value(i);
	}
	
	public void getScan(ArrayList<TallyType> output) {
		//reduced = reduced || reduce(ROOT);
		threadPool.invoke( new ComputeScan(ROOT, init(), output));

	}
	
}
