/*
 * Created by: Dane Hart
 * For CS493 Capstone Project
 * Regis University
 * 
 *  June 2017
 */

package complEATme.Objects;


/**
 * This class pairs a nutrient with user input of
 * whether to minimize or maximize the nutrient content
 */
public class Nutrient {
	
	private String nutrient;
	private boolean maximize;
	
	/**
	 * Default constructor does not take any inputs
	 * allows assignment after instantiation
	 */
	public Nutrient() {
		
	}
	
	/**
	 * Constructor that takes nutrient name and boolean
	 * for minimize/maximize. True to maximize, false
	 * to minimize.
	 * @param name a nutrient name String
	 * @param max True to maximize, false to minimize
	 */
	public Nutrient(String name, boolean max) {
		this.nutrient = name;
		this.maximize = max;
	}

	/**
	 * Returns the nutrient name
	 * @return String name of a nutrient
	 */
	public String getNutrient() {
		return nutrient;
	}

	/**
	 * Sets the nutrient name
	 * @param nutrient String name of a nutrient
	 */
	public void setNutrient(String nutrient) {
		this.nutrient = nutrient;
	}

	/**
	 * Returns whether the nutrient should be maximized
	 * @return true for maximize, false for minimize
	 */
	public boolean isMaximize() {
		return maximize;
	}

	/**
	 * Sets the maximize variable
	 * @param maximize true to maximize, false to minimize
	 */
	public void setMaximize(boolean maximize) {
		this.maximize = maximize;
	}
	
	/**
	 * Auto-genterated hashCode() method
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (maximize ? 1231 : 1237);
		result = prime * result + ((nutrient == null) ? 0 : nutrient.hashCode());
		return result;
	}

	/**
	 * Auto-generated equals() method
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Nutrient other = (Nutrient) obj;
		if (maximize != other.maximize)
			return false;
		if (nutrient == null) {
			if (other.nutrient != null)
				return false;
		} else if (!nutrient.equals(other.nutrient))
			return false;
		return true;
	}

}
