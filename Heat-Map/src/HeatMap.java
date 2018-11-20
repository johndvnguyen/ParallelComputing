import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;



public class HeatMap{
	

	//Static Class Tally to hold Observation Data
	public static class Tally{
		public double time;
		public double x;
		public double y;
		public List<Observation> history;
		
		public Tally() {
			x = 0.0;
			y = 0.0;
			history = new ArrayList<Observation>();
		}
		
		public Tally(Observation o) {
			this.x = o.x;
			this.y = o.y;
			this.time = o.time;
			history = new ArrayList<Observation>();
			history.add(o);
		}
		
		public static Tally combine(Tally a, Tally b) {
			Tally result = new Tally();
			for(int i = 0; i < a.history.size(); i++)
				result.history.add(a.history.get(i));
				
			for(int i = 0; i < b.history.size(); i++)
				result.history.add(b.history.get(i));
			
			result.x = a.x +b.x;
			result.y = a.y +b.y;
			System.out.println("result history size: " + result.history.size());
			return result;
			
			
		}
		
		public void accum(Observation datum) {
			this.x += datum.x;
			this.y += datum.y;
			this.history.add(datum);
		}
	}
	//Scan and Reduce Class
	public static class HeatScan extends GeneralScan3<Observation,Tally>{
		
	
		public HeatScan(List<Observation> raw, int threshold) {
			super(raw, threshold);
			// TODO Auto-generated constructor stub
		}
		@Override
		protected Tally init() {
			return new Tally();
		}
	
		@Override
		protected Tally prepare(Observation datum) {
			return new Tally(datum);
		}
	
		@Override
		protected Tally combine(Tally left, Tally right) {
			return Tally.combine(left, right);
		}
	
		@Override
		protected void accum(Tally tally, Observation datum) {
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
		
		//Using the list of observations and count, we can make a 2Dimensional Array

		
		//Create the prefix scan object with a threshold
				HeatScan pScan = new HeatMap.HeatScan(observations, 50);
				final double EPSILON = 1e-3;
		
				
				//Compute and print out sum		
				pScan.reduce(0);
				System.out.println("Reduction x: " + pScan.interior.get(0).x);
				System.out.println("Reduction y: " + pScan.interior.get(0).y);
				
				//create arraylist for storing scan result
				List<Tally> output = pScan.getScan();
				

				//call the scan function
				
				double check = 0.0;
				for (int i = 0; i < num_obs; i++) {
					if (i < 10)
						System.out.printf("+ %8.2f, %8.2f = %8.2f, %8.2f%n", observations.get(i).x,observations.get(i).y, output.get(i).x,output.get(i).y);
					
					else if (i == num_obs - 1 || i % 10000 == 0)
						System.out.printf("...(%d)%n+ %8.2f, %8.2f  = %8.2f, %8.2f%n", i, observations.get(i).x,observations.get(i).y, output.get(i).x,output.get(i).y);
				}
		
		
	}

}
