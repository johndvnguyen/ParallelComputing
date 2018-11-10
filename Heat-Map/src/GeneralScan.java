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
			recursiveCompute(i);//compute can't have any params so call recursive compute
		}
		
		
		
		protected void recursiveCompute(Integer i) {
			if(!isLeaf(i)) {
				if(leafCount(i) > threshold) {//make new thread
					ComputeReduction right = new ComputeReduction(right(i));
					right.fork();
					//invokeAll(new ComputeReduction(right(i)));
					recursiveCompute(left(i));
					right.join();
					System.out.println("Reduction active thread count: " + threadPool.getActiveThreadCount());
				}
				else {//no thread creation
					recursiveCompute(left(i));
					recursiveCompute(right(i));
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
			recursiveCompute(i,tallyPrior,output);
		}
		
		protected void recursiveCompute(int i, TallyType tallyPrior, ArrayList<TallyType> output) {
			if (isLeaf(i)) {
				output.set(i - (n-1), combine(tallyPrior, value(i)));
			} else {
				if (leafCount(i) > threshold) {
					//need to replace with threads
					//System.out.println("Left:: i; " + i + ", tallyprior: " + tallyPrior);
					ComputeScan scanLeft = new ComputeScan(left(i), tallyPrior, output);
					scanLeft.fork();
					//recursiveCompute(left(i), tallyPrior, output);
					recursiveCompute(right(i), combine(tallyPrior, value(left(i))), output);
					scanLeft.join();
				} else {
					recursiveCompute(left(i), tallyPrior, output);
					recursiveCompute(right(i), combine(tallyPrior, value(left(i))), output);
				}
			}
			
		}
	}
	
	//private class variables
	private List<ElemType> rawData; //static array as this does not change
	private List<TallyType> interior;  //interior nodes.
	private final int ROOT = 0; //root of the tree
	private boolean reduced; // is the data reduced.
	private int height; //tree height
	private int threshold; //threshold of leaves
	private int n; //total num leaves
	private ForkJoinPool threadPool;
	
	//constant public thread
	public final int N_THREADS=16;
	
	//constructor
	public GeneralScan(List<ElemType> raw, int threshold) {
		this.reduced = false;
		this.n = raw.size();
		this.rawData = raw;
		this.threshold = threshold;
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

	protected TallyType accum(List<TallyType> output, ElemType datum) {
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
	
	//takes any node index and returns number of leaves below using the recursive method leaf count. 
	private int leafCount(int index) {
		int sum=0;
		if(!isLeaf(index)) {
			sum+=leafCount(left(index));
			sum+=leafCount(right(index));
		}else
			return 1;
		
		return sum;
	}
	
	
	private boolean reduce(int i) {
		threadPool.invoke( new ComputeReduction(i));
		return true;		

	}
	// this is no longer used
	private void scan(int i, TallyType tallyPrior, ArrayList<TallyType> output) {
		if (isLeaf(i)) {
			output.set(i - (n-1), combine(tallyPrior, value(i)));
		} else {
			if (leafCount(i) > n/4) {
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
		
		reduced = reduced || reduce(ROOT);
		return value(i);
	}
	
	public void getScan(ArrayList<TallyType> output) {
		reduced = reduced || reduce(ROOT);
		threadPool.invoke( new ComputeScan(ROOT, init(), output));
		//scan(ROOT,init(),output);
	}
	
}
