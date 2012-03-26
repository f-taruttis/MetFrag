package de.ipbhalle.metfrag.newScoring;

import java.util.Random;

public class GaussianDistribution {
	
	
	double mean; // \mu
	double variance; // \sigma^2
	

	public GaussianDistribution(double mean,double variance)
	{
		this.mean=mean;
		this.variance=variance;
	}
	
	
	public double getProb(double x)
	{
		double res = 1.0/(Math.sqrt(2 * Math.PI * this.variance )) * Math.exp( ( -1* Math.pow((x-this.mean), 2) / (2.0*this.variance)  ));
		return res;
	}
	
	public double getLogProb(double x)
	{
		double res = -0.5 * (  Math.log(2*Math.PI*this.variance) + (Math.pow(x-this.mean,2)/this.variance)  );
		return res;
	}
	
	
	public double getVariance()
	{
		return this.variance;
	}
	

	
	public static void main(String[] args) {
		
		Random rand = new Random();
		GaussianDistribution gauss = new GaussianDistribution(0, 2);

		
//		int counter=100;
//		 
//		for (int i = 0; i < 100; i++) {
//			
//			
//			double randD = (rand.nextDouble())*10; 
//			
//			double px = gauss.getProb(randD);
//			double logpx = gauss.getLogProb(randD);
//			
//			System.out.println((px-Math.exp(logpx)));
//			
//		}
		

		
		
	}

}
