package de.ipbhalle.metfrag.newScoring;

public class Gaussian {
	
	
	double mean; // \mu
	double variance; // \sigma^2
	

	public Gaussian(double mean,double variance)
	{
		this.mean=mean;
		this.variance=variance;
	}
	
	public double getVal(double x)
	{
		double res =  Math.exp( ( -1* Math.pow((x-this.mean), 2) / (2.0*this.variance)  ));
		return res;
	}
	
	public double getLogVal(double x)
	{
		double res = -1 * (Math.pow(x-this.mean,2)/(2.0*this.variance))  ;
		return res;
	}
	

	public static void main(String[] args) {
		
		Gaussian gauss = new Gaussian(0, 0.002);
		
		System.out.println(gauss.getVal(0.1));
		System.out.println(Math.exp(gauss.getLogVal(0.1)));
		//		
//		GaussianDistribution gauss2 = new GaussianDistribution(0, 0.002);
//		double norm = gauss2.getProb(0);
//		
//		System.out.println(gauss2.getProb(0.1)/norm);
		
	}
	
}
