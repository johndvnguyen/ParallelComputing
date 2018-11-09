import java.util.ArrayList;
import java.util.List;

public class PrefixScan extends GeneralScan<Integer, Double> {
	
	public class Tally {
		
	}

	public PrefixScan(List<Integer> raw) {
		super(raw);
	}
	
	protected Double init() {
		return 0.0;
	}
	
	protected Double prepare(Integer datum) {
		return (double) datum;
	}
	
	protected Double combine(Double left, Double right) {
		System.out.println(left + " " + right);
		return left + right;
	}

	public static void main(String[] args) {
		// Create test array of data from -1 to 1
		int n = 32;
		
		List<Integer> testData = new ArrayList<Integer>(n);
		for(int i = 0; i < n; i++) {
			testData.add(i);
		}
		
		//create arraylist for storing scan result
		ArrayList<Double> output = new ArrayList<Double>(n);
		//initialize output array
		while(output.size()<n)
			output.add(0.0);
		PrefixScan pScan = new PrefixScan(testData);
		//pScan.getScan(output);
		//print out the scan arraylist
		//for(int i=0; i< output.size(); i++)
//			System.out.println("i: " + i +", value: " + output.get(i));
		//print out prefix sum
		//pScan.getReduction(0);
		System.out.println("reduction: " + pScan.getReduction(0));

	}

}
