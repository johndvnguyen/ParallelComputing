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

public class PrefixScan{
	

public static class TallyDoubAdd {
	//class variable is public similar to the example in lecture
	public double d;
	
	///Constructors
	public TallyDoubAdd() {
	}
	
	public TallyDoubAdd(Double d) {
		this.d = d;
	}
	
	public TallyDoubAdd(Integer d) {
		this.d = d;

	}
	
	//should take in a tally list, compute the running sum and add it to the list. 
	public void accum(Double elem) {
		this.d += elem;
		
	}
	
	public TallyDoubAdd clone(){
		return new TallyDoubAdd(d);
		
	}

	public TallyDoubAdd init() {
		return new TallyDoubAdd();
	}

	public void combine(TallyDoubAdd other) {
		this.d += other.d;
		
	}
	
}

/***
 * PrefixScan Class
 * An extension of the GenericScan Class
 * This class can be used to reduce a set of Integers into a sum with format double.
 * It also has a method getScan for prefix sums of the Integers as a double. 
 * The output or tally values are doubles and are in the form of TallyDoubAdd type objects
 *
 */
public static class PrefixScanner extends GeneralScan<Integer, TallyDoubAdd> {
	
	//constructors
	public PrefixScanner(List<Integer> raw) {
		//default threshold of 100
		super(raw, 100);
	}
	
	
	public PrefixScanner(List<Integer> raw, int threshold) {
		super(raw, threshold);
	}
	
	/***
	 * init() 
	 * @return a new TallyDoubAdd object
	 * constructs a new Tally with the inital value of 0 for a sum 
	 */
	@Override
	protected TallyDoubAdd init() {
		return new TallyDoubAdd(0.0);
	}
	
	/***
	 * prepare() 
	 * @param datum Integer input type element data
	 * @return a new TallyDoubAdd object
	 * constructs a new Tally and converts the integer datum into a double and stores it in the object
	 */
	@Override
	protected TallyDoubAdd prepare(Integer datum) {
		return new TallyDoubAdd(datum);
	}
	
	@Override
	protected TallyDoubAdd combine(TallyDoubAdd left, TallyDoubAdd right) {
		//System.out.println(left.d + " " + right.d);
		return new TallyDoubAdd(left.d + right.d);
	}
	
	/***
	 * accum(TallyDoubAdd, TallyDoubAdd)
	 * @param left
	 * @param right
	 * Takes the double value from the right tally and updates the left tally's value
	 * Does not create a new tally object
	 */
	@Override
	protected void accum(TallyDoubAdd tally, Integer data) {
		tally.accum((double) data);
	}
	
	/***
	 * cloneTally(TallyDoubAdd)
	 * @param TallyDoubAdd, an input tally to clone
	 * Calls the TallyDoubAdd's clone method to return a new TallyDoubAdd.
	 */
	@Override
	protected TallyDoubAdd cloneTally(TallyDoubAdd tally) {
		return tally.clone();
	}
	
	
}
/***
 * Main Method 
 * @param args none needed at this time
 * Creates sample data, and computes a prefix sum scan, and a reduction to get the total sum.
 */
public static void main(String[] args) {
	// Create test array of data from -1 to 1
	int n = 1<<22;
	
	//generate test data
	List<Integer> testData = new ArrayList<Integer>(n);
	for(int i = 1; i <= n; i++) {
		testData.add(i);
	}
	
	//Create the prefix scan object with a threshold
	PrefixScanner pScan = new PrefixScanner(testData, 1024);
	
	//Compute and print out sum		
	System.out.println("Reduction: " + pScan.getReduction(0).d);
	
	//create arraylist for storing scan result
	ArrayList<TallyDoubAdd> output = new ArrayList<TallyDoubAdd>(n);
	
	//initialize output array
	while(output.size()<n)
		output.add(new TallyDoubAdd(0.0));


	//call the scan function
	pScan.getScan(output);
	
	//print out the scan of the prefix sums
	for(int i=0; i< 10; i++)
		System.out.println("i: " + i +", value: " + output.get(i).d);
	System.out.println("i: " + (n-1) +", value: " + output.get(n-1).d);
	
}
}
