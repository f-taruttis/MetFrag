package de.ipbhalle.metfrag.scoring;

public class OptimizationMatrixEntry {
	
	private String candidateID;
	private Double peakMass;
	private Double peakInt;
	private String bondEnergyString;
	private int hydrogenPenalty;
	private String chargesDiffString;
	
	/**
	 * Instantiates a new optimization matrix entry.
	 * 
	 * @param candidateID the candidate id
	 * @param peakMass the peak mass
	 * @param peakInt the peak int
	 * @param weightedPeak the weighted peak
	 * @param bondEnergyString the bond energy string
	 * @param hydrogenPenalty the hydrogen penalty
	 * @param chargesDiffString the charges diff string
	 */
	public OptimizationMatrixEntry(String candidateID, Double peakMass, Double peakInt, String bondEnergyString, int hydrogenPenalty, String chargesDiffString)
	{
		setBondEnergyString(bondEnergyString);
		setCandidateID(candidateID);
		setChargesDiffString(chargesDiffString);
		setHydrogenPenalty(hydrogenPenalty);
		setPeakInt(peakInt);
		setPeakMass(peakMass);
	}

	/**
	 * Sets the candidate id.
	 * 
	 * @param candidateID the new candidate id
	 */
	public void setCandidateID(String candidateID) {
		this.candidateID = candidateID;
	}

	/**
	 * Gets the candidate id.
	 * 
	 * @return the candidate id
	 */
	public String getCandidateID() {
		return candidateID;
	}

	/**
	 * Sets the peak mass.
	 * 
	 * @param peakMass the new peak mass
	 */
	public void setPeakMass(Double peakMass) {
		this.peakMass = peakMass;
	}

	/**
	 * Gets the peak mass.
	 * 
	 * @return the peak mass
	 */
	public Double getPeakMass() {
		return peakMass;
	}

	/**
	 * Sets the peak int.
	 * 
	 * @param peakInt the new peak int
	 */
	public void setPeakInt(Double peakInt) {
		this.peakInt = peakInt;
	}

	/**
	 * Gets the peak int.
	 * 
	 * @return the peak int
	 */
	public Double getPeakInt() {
		return peakInt;
	}

	/**
	 * Sets the bond energy string.
	 * 
	 * @param bondEnergyString the new bond energy string
	 */
	public void setBondEnergyString(String bondEnergyString) {
		this.bondEnergyString = bondEnergyString;
	}

	/**
	 * Gets the bond energy string.
	 * 
	 * @return the bond energy string
	 */
	public String getBondEnergyString() {
		return bondEnergyString;
	}

	/**
	 * Sets the hydrogen penalty.
	 * 
	 * @param hydrogenPenalty the new hydrogen penalty
	 */
	public void setHydrogenPenalty(int hydrogenPenalty) {
		this.hydrogenPenalty = hydrogenPenalty;
	}

	/**
	 * Gets the hydrogen penalty.
	 * 
	 * @return the hydrogen penalty
	 */
	public int getHydrogenPenalty() {
		return hydrogenPenalty;
	}

	/**
	 * Sets the charges diff string.
	 * 
	 * @param chargesDiffString the new charges diff string
	 */
	public void setChargesDiffString(String chargesDiffString) {
		this.chargesDiffString = chargesDiffString;
	}

	/**
	 * Gets the charges diff string.
	 * 
	 * @return the charges diff string
	 */
	public String getChargesDiffString() {
		return chargesDiffString;
	}

}
