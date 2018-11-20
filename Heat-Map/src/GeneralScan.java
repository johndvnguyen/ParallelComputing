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
		//private variables
		private int i;
		
		//constructor
		public ComputeReduction(int i) {
			this.i = i;
		}
		
		/***
		 * compute()
		 * method to be picked up by forkjoinpool
		 * in this framework the compute() method cannot take inputs so these are defined in the constructor
		 * From HW5 Solution
		 */
		protected void compute() {
			if (leafCount(i) < threshold) {
				reduce(i);
				return;
			}
			invokeAll(
					new ComputeReduction(left(i)), 
					new ComputeReduction(right(i)));
			interior.set(i, combine(value(left(i)), value(right(i))));
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
		 * From HW5 Solution
		 */
		protected void compute() {
			if (leafCount(i) < threshold) {
				scan(i, tallyPrior, output);
				return;
			}
			invokeAll(
					new ComputeScan(left(i), tallyPrior, output),
					new ComputeScan(right(i), combine(tallyPrior, value(left(i))), output));
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
	private int first_data;
	
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
		this.height = 0;
		while((1<<this.height) < n)
			height++;
		
		first_data = (1<<height) - 1;
		int m = 4 * (1 + first_data/threshold);
		
		//initialize the interior arraylist, each threshold of leaves requires a node in the tree cap
		//if the lowest layer of the tree cap has M nodes, the interior of the cap has M-1 nodes for a total of 2M-1 nodes
		interior = new ArrayList<TallyType>(m);
		
		for (int i = 0; i < m; i++)
			interior.add(init());
		
//		// must be power of 2 for the scan currently 
//		//TODO fix the power of 2 issue
//		if(1<<height != n) {
//			throw new IllegalArgumentException("n must be a power of 2");
//		}
		
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

	protected void accum(TallyType tally, ElemType datum) {
		throw new IllegalArgumentException("This function to be overwritten");
	}
	
	protected TallyType cloneTally(TallyType tally) {
		throw new IllegalArgumentException("This function to be overwritten");
	}
	
	
	protected int size() {
		return first_data + n;
	}
	
	protected ElemType leafValue(int i) {
		if (i < first_data || i >= size())
			throw new IllegalArgumentException("bad i " + i);
		return rawData.get(i - first_data);
	}
	
	protected int firstData(int i) {
		if (isLeaf(i))
			return i < first_data ? -1 : i;
		return firstData(left(i));
	}
	
	protected int lastData(int i) {
		if (isLeaf(i))
			return i < first_data ? -1 : i;
		if (hasRight(i)) {
			int r = lastData(right(i));
			if (r != -1)
				return r;
		}
		return lastData(left(i));
	}
	
	//private methods
	
	
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
	
	private boolean hasRight(int i) {
		return right(i) < size();
	}
	
	private int left(int i) {
		return i*2+1;
	}
	
	private int right(int i) {
		return left(i)+1;
	}
	
	private boolean isLeaf(int i) {
		return left(i) >= size();
	}
	
	
	/***
	 * leafCount(int i)
	 * @param i integer index
	 * @return returns the total number of leaves with data that are under the current node
	 * takes any node index and returns number of leaves below using the recursive method leaf count. 
	 * From HW5 Solution
	 */
	private int leafCount(int i) {
		return lastData(i)-firstData(i);
	}
	

	
	
	/***
	 * invokes the ComputeReduction Function
	 * @param i - integer index for a reduction
	 * @return boolean with the state of the reduction. True = Reduced
	 * 
	 * Uses ForkJoinPool to start a RecursiveAction of type ComputeReduction
	 */
	protected void reduce(int i) {
		int first = firstData(i), last = lastData(i);
		//System.out.println("reduce(" + i + ") from " + first + " to " + last);
		TallyType tally = init();
		if (first != -1)
			for (int j = first; j <= last; j++)
				accum(tally, leafValue(j));
		interior.set(i, tally);
	}
	
	/***
	 * From professor HW5 Solution
	 * @param i
	 * @param tallyPrior
	 * @param output
	 */
	protected void scan(int i, TallyType tallyPrior, List<TallyType> output) {
		int first = firstData(i), last = lastData(i);
		if (first != -1)
			for (int j = first; j <= last; j++) {
				tallyPrior = combine(tallyPrior, value(j));
				output.set(j - first_data, tallyPrior);
			}
	}

	
	///public methods
	
	/***
	 * getReduction()
	 * @param i integer index, specified what node to get the reduction from
	 * @return TallyType node in the tree that has been reduced
	 */
	public TallyType getReduction(int i) {
		if(!reduced)
			threadPool.invoke(new ComputeReduction(ROOT));

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
		if(!reduced)
			threadPool.invoke(new ComputeReduction(ROOT));
		threadPool.invoke( new ComputeScan(ROOT, init(), output));

	}
	
}
