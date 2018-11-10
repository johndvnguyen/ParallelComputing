/***
 * @file GeneralScan.java
 * @author John Nguyen
 * CPSC 5600 HW5A
 * 
 * TallyDoubAdd Class Implementation of the Tally Interface by Professor Lundeen
 * 
 */



/***
 * Mutable class to hold a double as our Tally
 * Doubles are not mutable within Java, so this class stores the doubles in a mutable fashion.
 * Also has methods to clone and combine similar objects of the same class
 *
 */
public class TallyDoubAdd implements Tally<Double>{
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
	@Override
	public void accum(Double elem) {
		this.d += elem;
		
	}
	
	@Override
	public TallyDoubAdd clone(){
		return new TallyDoubAdd(d);
		
	}

	@Override
	public TallyDoubAdd init() {
		return new TallyDoubAdd();
	}

	@Override
	public void combine(Tally<Double> other) {
		this.d += ((TallyDoubAdd)other).d;
		
	}
	
}