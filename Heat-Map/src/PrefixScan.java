import java.util.ArrayList;
import java.util.List;



public class PrefixScan extends GeneralScan<Integer, TallyDoubAdd> {
	

	
	public PrefixScan(List<Integer> raw, int threshold) {
		super(raw, threshold);
	}
	
	protected TallyDoubAdd init() {
		return new TallyDoubAdd(0.0);
	}
	
	protected TallyDoubAdd prepare(Integer datum) {
		return new TallyDoubAdd(datum);
	}
	
	protected TallyDoubAdd combine(TallyDoubAdd left, TallyDoubAdd right) {
		//System.out.println(left.d + " " + right.d);
		return new TallyDoubAdd(left.d + right.d);
	}
	
	//accum should take two lists and append the records together within the left list
	protected void accum(TallyDoubAdd left, TallyDoubAdd right) {
		left.accum(right.d);
	}
	
	protected TallyDoubAdd cloneTally(TallyDoubAdd tally) {
		return tally.clone();
	}
	
	protected void printVal(TallyDoubAdd tally) {
		System.out.println("this tally has a value of: " + tally.d);
	}

	
	
	
	public static void main(String[] args) {
		// Create test array of data from -1 to 1
		int n = 1<<20;
		
		List<Integer> testData = new ArrayList<Integer>(n);
		for(int i = 1; i <= n; i++) {
			testData.add(i);
		}

		
		//create arraylist for storing scan result
		ArrayList<TallyDoubAdd> output = new ArrayList<TallyDoubAdd>(n);
		//initialize output array
		while(output.size()<n)
			output.add(new TallyDoubAdd(0.0));
		PrefixScan pScan = new PrefixScan(testData, 4);
		//print out prefix sum
		
		//test first and last
		int x= 2;
		System.out.println("node " + x +"'s first leaf is: " +pScan.firstLeaf(x) +" and last leaf is: " + pScan.lastLeaf(x));
		
		
		
		System.out.println("reduction: " + pScan.getReduction(0).d);


		pScan.getScan(output);
		//print out the scan arraylist
		for(int i=0; i< 10; i++)
			System.out.println("i: " + i +", value: " + output.get(i).d);
		System.out.println("i: " + (n-1) +", value: " + output.get(n-1).d);
		
	}

}
