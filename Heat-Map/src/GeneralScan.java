/***
 * @file GeneralScan.java
 * @author John Nguyen
 * CPSC 5600 HW5A
 * 
 * Generic Reduce and Scan Class using Java's ForkJoinPool 
 * 
 */
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/***
 * GeneralScan Class
 * @author John
 *
 * @param <ElemType>
 * @param <TallyType>
 * 
 * Generic class to be extended with specific Element and  Tally Types.
 * The class is setup to be extended and apply a reduce and scan on data.
 * This is a multithreaded class and utilizes Java's ForkJoinPool class 
 * to manage task creation, start, and thread usage.
 */
public class GeneralScan<ElemType, TallyType> {
	
	/***
	 * ComputeReduction Class
	 * extends the RecursiveAction Class
	 * A nested class that provides tasks for The ForkJoinPool to start
	 * Uses schwartz's algorithm in a tight loop to compute subsets of the raw data
	 */
	public class ComputeReduction extends RecursiveAction{
		//public variables
		int i;
		
		//constructor
		public ComputeReduction(int i) {
			this.i = i;
		}
		
		/***
		 * compute()
		 * method to be picked up by forkjoinpool
		 * in this framework the compute() method cannot take inputs so these are defined in the constructor
		 */
		protected void compute() {
			schwartzCompute(i);
		}
		
		/***
		 * schwartzCompute(Integer i)
		 * @param i integer index o the current node
		 * A recursive method to compute the reduction of the data.
		 */
		protected void schwartzCompute(Integer i) {
			if(!isLeaf(i)) {
				//check to see if we should split thread if we are at the cap
				if(leafCount(i) > threshold) {
					
					ComputeReduction right = new ComputeReduction(right(i));
					right.fork();

					schwartzCompute(left(i));
					
					right.join();
					interior.set(i, combine(value(left(i)), value(right(i))));	
					
				}else {
					//if we are not at the tree cap we should loop across leaves
					for(int j = firstLeaf(i); j <= lastLeaf(i); j++) {
						//update the interior node or each value of its leaves
						interior.set(i, combine(value(i), value(j)));
					}
				}
				
			}
		}
	//end ComputeReduction Class	
	}
	
	/***
	 * ComputeScan Class 
	 * extends the RecursiveAction Class
	 * A nested class that provides tasks for The ForkJoinPool to start
	 * Uses schwartz's algorithm in a tight loop to compute subsets of the raw data
	 */
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

		/***
		 * compute()
		 * method to be picked up by forkjoinpool
		 * in this framework the compute() method cannot take inputs so these are defined in the constructor
		 */
		protected void compute() {
			schwartzCompute(i,tallyPrior,output);
			
		}
		
		/***
		 * schwartzCompute(Integer i)
		 * @param i integer index o the current node
		 * A recursive method to compute the scan of the data.
		 */
		protected void schwartzCompute(Integer i,TallyType tallyPrior, ArrayList<TallyType> output) {
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
					//if we are not at the cap we should loop across leaves
					for(int j = firstLeaf(i); j <= lastLeaf(i); j++) {
						accum(tallyPrior,value(j));
						output.set(j-(n-1), cloneTally(tallyPrior) );

					}
				}	
			}
		}
	//end ComputeScan Class	
	}
	
	//private GeneralScan class variables
	private List<ElemType> rawData; //static array as this does not change
	private List<TallyType> interior;  //interior nodes.
	private final int ROOT = 0; //root of the tree
	private boolean reduced; // is the data reduced.
	private int height; //tree height
	private int threshold; //threshold of leaves
	private int n; //total num leaves
	private ForkJoinPool threadPool;
	
	//constant public thread limit
	public final int N_THREADS=16;
	
	/***
	 * Constructor
	 * GeneralScan(List<ElemType> raw, int threshold
	 * @param raw List of ElemTypes
	 * @param threshold Threshold number of leaves for threads
	 */
	public GeneralScan(List<ElemType> raw, int threshold) {
		//Flag if data is reduced
		this.reduced = false;
		//set the rest of the private variables
		this.n = raw.size();
		this.rawData = raw;
		this.threshold = threshold;
		//calculate log base 2 of n, TODO can be removed once power of 2 restriction removed
		this.height = (int) Math.ceil(Math.log(n)/Math.log(2));
		
		//initialize the interior arraylist, each threshold of leaves requires a node in the tree cap
		//if the lowest layer of the tree cap has M nodes, the interior of the cap has M-1 nodes for a total of 2M-1 nodes
		interior = new ArrayList<TallyType>(n-1);
		
		while(interior.size()<(2*n/threshold)-1)
			interior.add(init());
		//if the user specified a larger threshold than there are leafs make sure we still have root node
		if(threshold>n)
			interior.add(init());
		
		// must be power of 2 for the scan currently 
		//TODO fix the power of 2 issue
		if(1<<height != n) {
			throw new IllegalArgumentException("n must be a power of 2");
		}
		
		//define thread pool
		this.threadPool = new ForkJoinPool(N_THREADS);
	}
	
	//protected methods to be overwritten
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
	
	//private methods
	private int size() {
		return (n-1) +n;
	}
	
	/***
	 * value(int i)
	 * @param i integer index into data's tree like structure
	 * @return TallyType 
	 * 
	 * Returns the appropriate TallyType data given an index assuming a Heap Representation of tree
	 * if index does not exist in interior 
	 */
	private TallyType value(int i) {
		if(i<n-1) {
			return interior.get(i);
		}
		else 
			return prepare(rawData.get(i-(n-1)));
	}
	
	//not currently used but was ported over from C++ example
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
	
	
	/***
	 * leafCount(int i)
	 * @param i integer index
	 * @return returns the total number of leaves that are under the current node
	 * takes any node index and returns number of leaves below using the recursive method leaf count. 
	 * TODO make this not recursive
	 */
	private int leafCount(int i) {
		int sum=0;
		if(!isLeaf(i)) {
			sum+=leafCount(left(i));
			sum+=leafCount(right(i));
		}else
			return 1;
		
		return sum;
	}
	
	//determines first leaf for an interior node i
	private int firstLeaf(int i) {
		
		while( i<=(2*n-2) && !isLeaf(i)) {
			i = left(i);
		}
		return i;
	}
	
	/***
	 * lastLeaf(int i)
	 * @param i integer index
	 * @return integer value of the last leaf index
	 * Calculates the last leaf that is under a specific node with index i
	 */
	private int lastLeaf(int i) {
		while(i<=(2*n-2) && !isLeaf(i)){
			i = right(i);
		}
		return i;
		
	}
	
	/***
	 * invokes the ComputeReduction Function
	 * @param i - integer index for a reduction
	 * @return boolean with the state of the reduction. True = Reduced
	 * 
	 * Uses ForkJoinPool to start a RecursiveAction of type ComputeReduction
	 */
	private boolean reduce(int i) {
		//Uses ForkJoinPool to start a RecursiveAction task
		threadPool.invoke(new ComputeReduction(i));
		return true;		
	}

	
	///public methods
	
	/***
	 * getReduction()
	 * @param i integer index, specified what node to get the reduction from
	 * @return TallyType node in the tree that has been reduced
	 */
	public TallyType getReduction(int i) {
		reduced = reduced || reduce(ROOT);
		return value(i);
	}
	
	/***
	 * getScan()
	 * @param output ArrayList of TallyType an output parameter to store results of the scan.
	 * 
	 * This method first checks to see if the data has been reduced, and performs a reduction if needed
	 * Uses ForkJoinPool to start a RecursiveAction of type ComputeScan
	 */
	public void getScan(ArrayList<TallyType> output) {
		reduced = reduced || reduce(ROOT);
		threadPool.invoke( new ComputeScan(ROOT, init(), output));

	}
	
}
