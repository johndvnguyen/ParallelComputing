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
	public class HeatScan extends GeneralScan<Double,Tally>{
		
	
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
		// TODO Auto-generated method stub

	}

}
