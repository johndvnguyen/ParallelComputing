import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;



public class HeatMap{
	

	//Static Class Tally to hold Data
	public static class Tally{
		public double d;
		public Tally() {
			d = 0.0;
		}
		
		public Tally(double d) {
			this.d = d;
		}
		
		public static Tally combine(Tally a, Tally b) {
			return new Tally(a.d + b.d);
		}
		
		public void accum(double datum) {
			this.d += datum;
		}
	}
	//Scan and Reduce Class
	public static class HeatScan extends GeneralScan3<Double,Tally>{
		
	
		public HeatScan(List<Double> raw, int threshold) {
			super(raw, threshold);
			// TODO Auto-generated constructor stub
		}
		@Override
		protected Tally init() {
			return new Tally();
		}
	
		@Override
		protected Tally prepare(Double datum) {
			return new Tally(datum);
		}
	
		@Override
		protected Tally combine(Tally left, Tally right) {
			return Tally.combine(left, right);
		}
	
		@Override
		protected void accum(Tally tally, Double datum) {
			tally.accum(datum);
		}
	

	}
	
	public static void main(String[] args) {
		//Read in File data (observation_test.dat)
		final String FILENAME = "observation_test.dat";
		ArrayList<Observation> observations = new ArrayList<Observation>(400);
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILENAME));
			int count = 0;

			Observation obs = (Observation) in.readObject();
			while (!obs.isEOF()) {
				//System.out.println(++count + ": " + obs);
				// add the observation to an ArrayList
				observations.add(obs);
				obs = (Observation) in.readObject();
			}
			in.close();
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("reading from " + FILENAME + "failed: " + e);
         e.printStackTrace();
         System.exit(1);
		}
		int num_obs = observations.size();
		ArrayList<Double> testData = new ArrayList<Double>(num_obs);
		
		//Using the list of observations and count, we can make a 2Dimensional Array
		for(int i=0; i< num_obs; i++) {
			testData.add(observations.get(i).y);
			//System.out.println(testData.get(i));
		}
		
		//Create the prefix scan object with a threshold
				HeatScan pScan = new HeatMap.HeatScan(testData, 32);
				final double EPSILON = 1e-3;
				int n = testData.size();
				
				//Compute and print out sum		
				pScan.reduce(0);
				System.out.println("Reduction: " + pScan.interior.get(0).d);
				
				//create arraylist for storing scan result
				List<Tally> output = pScan.getScan();
				

				//call the scan function
				
				double check = 0.0;
				for (int i = 0; i < n; i++) {
					if (i < 10)
						System.out.printf("+ %8.2f = %8.2f%n", testData.get(i), output.get(i).d);
					else if (i == n - 1 || i % 10000 == 0)
						System.out.printf("...(%d)%n+ %8.2f = %8.2f%n", i, testData.get(i), output.get(i).d);
				}
		
		
	}

}
