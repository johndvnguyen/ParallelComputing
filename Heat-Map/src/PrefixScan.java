import java.util.ArrayList;
import java.util.List;

public class PrefixScan extends GeneralScan<Integer, Double> {
	
	public class Tally {
		public double d;
		
		public Tally(double d) {
			this.d=d;
		}
		
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
		return left + right;
	}

	public static void main(String[] args) {
		// Create test array of data from -1 to 1
		int n = 16;
		List<Integer> testData = new ArrayList<Integer>(n);
		for(int i = 0; i < n; i++) {
			testData.add(i);
		}
		ArrayList<Double> output = new ArrayList<Double>(n);
		while(output.size()<n)
			output.add(0.0);
		PrefixScan pScan = new PrefixScan(testData);
		pScan.getScan(output);
		for(int i=0; i< output.size(); i++)
			System.out.println("i: " + i +", value: " + output.get(i));
		System.out.println("reduction: " + pScan.getReduction(0));

	}

}
