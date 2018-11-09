import java.util.ArrayList;
import java.util.List;



public class PrefixScan extends GeneralScan<Integer, Tally> {
	

	public PrefixScan(List<Integer> raw) {
		super(raw);
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

	public static void main(String[] args) {
		// Create test array of data from -1 to 1
		int n = 1<<10
				;
		
		List<Integer> testData = new ArrayList<Integer>(n);
		for(int i = 0; i < n; i++) {
			testData.add(i);
		}
		
		//create arraylist for storing scan result
		ArrayList<Tally> output = new ArrayList<Tally>(n);
		//initialize output array
		while(output.size()<n)
			output.add(new Tally(0.0));
		PrefixScan pScan = new PrefixScan(testData);
		//print out prefix sum
		
		System.out.println("reduction: " + pScan.getReduction(0).d);
		

		pScan.getScan(output);
		//print out the scan arraylist
		for(int i=0; i< 10; i++)
			System.out.println("i: " + i +", value: " + output.get(i).d);
		System.out.println("i: " + (n-1) +", value: " + output.get(n-1).d);
		
	}

}
