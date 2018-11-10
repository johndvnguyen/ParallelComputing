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
			//recursiveCompute(i);//compute can't have any params so call recursive compute
			schwartzCompute(i);
		}
		
		protected void schwartzCompute(Integer i) {
			if(!isLeaf(i)) {
				//check to see if we should split thread if we are at the cap
				if(leafCount(i) > threshold) {
					
					ComputeReduction right = new ComputeReduction(right(i));
					right.fork();
					//invokeAll(new ComputeReduction(right(i)));
					schwartzCompute(left(i));
					right.join();
					interior.set(i, combine(value(left(i)), value(right(i))));	
					
				}else {
					//System.out.println("Inside the reduction else: " + i );
					//if we are not at the cap we should loop across leaves
					for(int j = firstLeaf(i); j <= lastLeaf(i); j++) {
						//System.out.println(i +": Inside the reduction else loop: " + j);
						//update the interior node or each value o its leaves
						interior.set(i, combine(value(i), value(j)));
						//printVal(value(j));
					}
				}
				
			}
		}
		
		
		protected void recursiveCompute(Integer i) {
			if(!isLeaf(i)) {
				if(leafCount(i) > threshold) {//make new thread
					ComputeReduction right = new ComputeReduction(right(i));
					right.fork();
					//invokeAll(new ComputeReduction(right(i)));
					recursiveCompute(left(i));
					right.join();
					//System.out.println("Reduction active thread count: " + threadPool.getActiveThreadCount());
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
			//recursiveCompute(i,tallyPrior,output);
			schwartzCompute(i,tallyPrior,output);
			
		}
		protected void schwartzCompute(Integer i,TallyType tallyPrior, ArrayList<TallyType> output) {
			//System.out.println("Node:" + i);
			if(!isLeaf(i)) {
				//check to see if we should split thread if we are at the cap
				if(leafCount(i) > threshold) {
					
					//System.out.println("Left:: i; " + i + ", tallyprior: " + tallyPrior);
					ComputeScan scanLeft = new ComputeScan(left(i), tallyPrior, output);
					scanLeft.fork();
					//recursiveCompute(left(i), tallyPrior, output);
					
					schwartzCompute(right(i), combine(tallyPrior, value(left(i))), output);
					scanLeft.join();

				}else {
					//System.out.println("Inside the scan else: " + i );
					//if we are not at the cap we should loop across leaves
					for(int j = firstLeaf(i); j <= lastLeaf(i); j++) {
						//System.out.println(i +": Inside the scan else loop: " + j);
						accum(tallyPrior,value(j));
						output.set(j-(n-1), cloneTally(tallyPrior) );

					}

				}
				
			}
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
//		if(1<<height != n) {
//			throw new IllegalArgumentException("n must be a power of 2");
//		}
		
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

	protected void accum(TallyType left, TallyType right) {
		throw new IllegalArgumentException("This function to be overwritten");
	}
	
	protected TallyType cloneTally(TallyType tally) {
		throw new IllegalArgumentException("This function to be overwritten");
	}
	
	protected void printVal(TallyType tally) {
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
	
	//determines first leaf for an interior node i
	protected int firstLeaf(int i) {
		
		while( i<=(2*n-2) && !isLeaf(i)) {
			i = left(i);
		}
		return i;
	}
	
	protected int lastLeaf(int i) {
		while(i<=(2*n-2) && !isLeaf(i)){
			i = right(i);
		}
		return i;
		
	}
	
	private boolean reduce(int i) {
		threadPool.invoke(new ComputeReduction(i));
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
