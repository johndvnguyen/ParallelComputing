import java.util.ArrayList;

//Mutable class to hold a double as our Tally
public class TallyDoubAdd implements Tally<Double>{
	public double d;
	public double prefix;
	public ArrayList<Double> dList;
	
	public TallyDoubAdd(Double d) {
		this.d = d;
		this.prefix = 0;
		this.dList = new ArrayList<Double>(0);

	}
	
	public TallyDoubAdd(Integer d) {
		this.d = d;
		this.prefix=0;
		this.dList = new ArrayList<Double>();

	}
	
	//should take in a tally list, compute the running sum and add it to the list. 
	public void accum(Double elem) {
		this.d += elem;
		
	}
	
	@Override
	public TallyDoubAdd clone(){
		return new TallyDoubAdd(d);
		
	}

	@Override
	public Tally<Double> init() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void combine(Tally<Double> other) {
		// TODO Auto-generated method stub
		
	}
	
}