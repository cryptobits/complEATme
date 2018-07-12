/*
 * Created by: Dane Hart
 * For CS493 Capstone Project
 * Regis University
 * 
 *  June 2017
 */

package complEATme.Objects;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to collect user input to inform
 * a REST request to the USDA NDB API by REST_Svc.class
 * It also provides a map of nutrients and nurtient IDs
 * for use in the UI, so user inputs are expected values.
 */
public class UserInput {
	
	private Integer max;
	private static Map<String,String> nutrientMap;
	private List<Nutrient> nutrients;
	
	public UserInput() {
		nutrients = new ArrayList<Nutrient>();
		nutrientMap = new HashMap<String,String>();
		nutrientMap.put("Calories", "208");
		nutrientMap.put("Fat(total)", "204");
		nutrientMap.put("Fat(monounsaturated)", "645");
		nutrientMap.put("Fat(polyunsaturated)", "646");
		nutrientMap.put("Fat(saturated)", "606");
		nutrientMap.put("Fat(unsaturated)", "605");
		nutrientMap.put("Protein", "203");
		nutrientMap.put("Carbohydrates", "205");
		nutrientMap.put("Sugars", "269");
		nutrientMap.put("Fiber", "291");
		nutrientMap.put("Vitamin A", "318");
		nutrientMap.put("Vitamin B12", "418");
		nutrientMap.put("Vitamin C", "401");
		nutrientMap.put("Vitamin D", "324");
		nutrientMap.put("Calcium", "301");
		nutrientMap.put("Iron", "303");
		nutrientMap.put("Magnesium", "304");
		nutrientMap.put("Manganese", "315");
		nutrientMap.put("Potassium", "306");
		nutrientMap.put("Vitamin E", "323");
		nutrientMap.put("Vitamin K", "430");
		nutrientMap.put("Folate", "417");
		nutrientMap.put("Folic Acid", "431");
		nutrientMap.put("Zinc", "309");
		nutrientMap.put("Vitamin B6", "415");
		nutrientMap.put("Thiamine", "404");
		nutrientMap.put("Riboflavin", "405");
		nutrientMap.put("Niacin", "406");
		nutrientMap.put("Sodium", "307");
		nutrientMap.put("Cholesterol", "601");
		nutrientMap.put("Caffeine", "262");
	}

	/**
	 * Used to get the maximum search depth desired by the user
	 * @return maximum search depth integer, null value means no limit
	 */
	public Integer getMax() {
		return max;
	}

	/**
	 * Used to set the maximum search depth desired by the user
	 * @param max maximum search depth intgerer value
	 */
	public void setMax(int max) {
		this.max = max;
	}

	/**
	 * Used to get the list of user input nutrients in priority order
	 * @return a list of user input Nutrient objects
	 */
	public List<Nutrient> getUserNutrients() {
		return nutrients;
	}

	/**
	 * Sets the user nutrients to specific list of Nutrient objects
	 * Unlikely to be used.
	 * @param nutrients a list of Nutrient objects
	 */
	public void setUserNutrients(List<Nutrient> nutrients) {
		this.nutrients = nutrients;
	}
	
	/**
	 * Adds a Nutrient object to the users list
	 * @param nutrient a Nutrient object to add to the list
	 */
	public void addUserNutrient(Nutrient nutrient) {
		nutrients.add(nutrient);
	}
	
	/**
	 * Gets a list of nutrient String for use in UI or other output
	 * @return an array of all nutrient names in the classes Map
	 */
	public String[] getAllNutrients() {
		String[] nutrientArray = new String[nutrientMap.size()];
		int i = 0;
		for (Map.Entry<String,String> m:nutrientMap.entrySet()) {
			nutrientArray[i] = m.getKey();
			i++;
		}
		return nutrientArray;
	}
	
	/**
	 * Gets the ID number for a nutrient based on its String name value
	 * @param nutrient the string name of a nutrient in the Map
	 * @return a nutrient ID string
	 */
	public String getNutrientID(String nutrient) {
		return nutrientMap.get(nutrient);
	}

	/**
	 * Auto-generated hashCode method
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((max == null) ? 0 : max.hashCode());
		result = prime * result + ((nutrientMap == null) ? 0 : nutrientMap.hashCode());
		result = prime * result + ((nutrients == null) ? 0 : nutrients.hashCode());
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
		UserInput other = (UserInput) obj;
		if (max == null) {
			if (other.max != null)
				return false;
		} else if (!max.equals(other.max))
			return false;
		if (nutrients == null) {
			if (other.nutrients != null)
				return false;
		} else if (!nutrients.equals(other.nutrients))
			return false;
		return true;
	}
}
