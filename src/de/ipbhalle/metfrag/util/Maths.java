package de.ipbhalle.metfrag.util;

import java.util.Arrays;

public class Maths {

	
	public static <T extends Comparable<T> >T min(T[] x)
	{
		T min = x[0];
		
		for (int i = 1; i < x.length; i++) {
			
			if(x[i].compareTo(x[i-1]) < 0)
			{
				min = x[i];
			}
			
		}
		
		return min;
	}
	
	public static <T extends Comparable<T> >T max(T[] x)
	{
		T max = x[0];
		
		for (int i = 1; i < x.length; i++) {
			
			System.out.println(x[i]);
			if(x[i].compareTo(x[i-1]) > 0)
			{
				max = x[i];
			}
			
		}
		
		return max;
	}
	
	
	public static <T extends Comparable<T>> boolean isEqual(T[] x,T[] y)
	{
		if(x.length == y.length)
		{
			Arrays.sort(x);
			Arrays.sort(y);
		
			for (int i = 0; i < y.length; i++) {
				if(x[i].compareTo(y[i])!=0)
				{
					return false;
				}
			}
		}
		else
		{
			return false;
		}
		
		return true;
	}
	
	
	public static void main(String[] args) {
		
//		Integer test [] = {1,2,3}; 
//		Integer test2[] = {1,2,3,3};
//		
//		//int result = Maths.min(test);
//		System.out.println(Maths.isEqual(test, test2));
		
		Double test[] = {(double) 6,(double) 4,(double) 8};
		double res = Maths.max(test);
		
		System.out.println(res);
		
	}
}
