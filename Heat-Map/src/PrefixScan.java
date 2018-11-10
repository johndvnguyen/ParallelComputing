import java.util.ArrayList;
import java.util.List;



public class PrefixScan extends GeneralScan<Integer, Tally> {
	

	
	public PrefixScan(List<Integer> raw, int threshold) {
		super(raw, threshold);
	}
	
	protected Tally init() {
		return new Tally(0.0);
	}
	
	protected Tally prepare(Integer datum) {
		return new Tally(datum);
	}
	
	protected Tally combine(Tally left, Tally right) {
		//System.out.println(left.d + " " + right.d);
		return new Tally(left.d + right.d);
	}
	
	protected Tally accum(List<Tally> output, Tally datum) {
		throw new IllegalArgumentException("This function to be overwritten");
	}
	
	protected void printVal(Tally tally) {
		System.out.println("this tally has a value of: " + tally.d);
	}

	public static void main(String[] args) {
		// Create test array of data from -1 to 1
		int n = 10;
		
		List<Integer> testData = new ArrayList<Integer>(n);
		for(int i = 1; i <= n; i++) {
			testData.add(i);
		}

		
		//create arraylist for storing scan result
		ArrayList<Tally> output = new ArrayList<Tally>(n);
		//initialize output array
		while(output.size()<n)
			output.add(new Tally(0.0));
		PrefixScan pScan = new PrefixScan(testData, 4);
		//print out prefix sum
		
		//test first and last
		int x= 2;
		System.out.println("node " + x +"'s first leaf is: " +pScan.firstLeaf(x) +" and last leaf is: " + pScan.lastLeaf(x));
		
		
		
		System.out.println("reduction: " + pScan.getReduction(0).d);


		//pScan.getScan(output);
		//print out the scan arraylist
		for(int i=0; i< 8; i++)
			System.out.println("i: " + i +", value: " + output.get(i).d);
		System.out.println("i: " + (n-1) +", value: " + output.get(n-1).d);
		
	}

}
