package de.ipbhalle.metfrag.newScoring;

public class Sigmoid {

	/*first idea for a "good fit": shift=1.25, steepness=6 */
	
	
	double shift;
	
	double steepness;
	
	public Sigmoid(double shift, double steepness)
	{
		this.shift=shift;
		this.steepness = steepness;
				
	}
	
	
	public double getVal(double x)
	{
		return 1/(1+Math.exp( this.steepness*(x-this.shift)  ));
	}
	
	public static void main(String[] args) {
		
		Sigmoid sig = new Sigmoid(1.25, 6);
		
		System.out.println(sig.getVal(1.25));
		
	}
	
}
