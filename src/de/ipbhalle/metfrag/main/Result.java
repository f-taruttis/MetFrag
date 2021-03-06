package de.ipbhalle.metfrag.main;

import java.util.List;

import org.openscience.cdk.interfaces.IAtomContainer;

// TODO: Auto-generated Javadoc
/**
 * The Class Result.
 */
public class Result {
	
	private String identifier;
	private double score;
	private int hits;
	private int peakCount;
	private List<IAtomContainer> fragments;
	private List<IAtomContainer> fragmentHits;
	

	/**
	 * Instantiates a new result.
	 * 
	 * @param score the score
	 * @param hits the hits
	 * @param peakCount the peak count
	 * @param fragments the fragments
	 * @param fragmentHits the fragment hits
	 */
	public Result(String identifier, double score, int hits, int peakCount, List<IAtomContainer> fragments, List<IAtomContainer> fragmentHits)
	{
		this.identifier = identifier;
		this.score = score;
		this.hits = hits;
		this.peakCount = peakCount;
		this.fragments = fragments;
		this.fragmentHits = fragmentHits;
	}


	/**
	 * Sets the fragment hits.
	 * 
	 * @param fragmentHits the new fragment hits
	 */
	public void setFragmentHits(List<IAtomContainer> fragmentHits) {
		this.fragmentHits = fragmentHits;
	}


	/**
	 * Gets the fragment hits.
	 * 
	 * @return the fragment hits
	 */
	public List<IAtomContainer> getFragmentHits() {
		return fragmentHits;
	}


	/**
	 * Sets the fragments.
	 * 
	 * @param fragments the new fragments
	 */
	public void setFragments(List<IAtomContainer> fragments) {
		this.fragments = fragments;
	}


	/**
	 * Gets the fragments.
	 * 
	 * @return the fragments
	 */
	public List<IAtomContainer> getFragments() {
		return fragments;
	}


	/**
	 * Sets the peak count.
	 * 
	 * @param peakCount the new peak count
	 */
	public void setPeakCount(int peakCount) {
		this.peakCount = peakCount;
	}


	/**
	 * Gets the peak count.
	 * 
	 * @return the peak count
	 */
	public int getPeakCount() {
		return peakCount;
	}


	/**
	 * Sets the hits.
	 * 
	 * @param hits the new hits
	 */
	public void setHits(int hits) {
		this.hits = hits;
	}


	/**
	 * Gets the hits.
	 * 
	 * @return the hits
	 */
	public int getHits() {
		return hits;
	}


	/**
	 * Sets the score.
	 * 
	 * @param score the new score
	 */
	public void setScore(double score) {
		this.score = score;
	}


	/**
	 * Gets the score.
	 * 
	 * @return the score
	 */
	public double getScore() {
		return score;
	}


	/**
	 * Sets the identifier.
	 * 
	 * @param identifier the new identifier
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}


	/**
	 * Gets the identifier.
	 * 
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

}
