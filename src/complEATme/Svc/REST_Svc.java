/*
 * Created by: Dane Hart
 * For CS493 Capstone Project
 * Regis University
 * 
 *  June 2017
 */

package complEATme.Svc;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import complEATme.Objects.Nutrient;
import complEATme.Objects.UserInput;
/**
 * This class is used to query the USDA NDB using their REST API.
 * It is also used to process the data and return JsonObjects
 * for use by other parts of the program. Processing performed
 * includes calculating nutrient/energy density ratios, sorting,
 * and finding intersecting values.
 * 
 * @author Dane Hart
 *
 */
public class REST_Svc {
	
	private final String REST_NUTRIENT_URL = 
			"http://api.nal.usda.gov/ndb/nutrients/?format=json&api_key=API_KEY_GOES_HERE&nutrients=";
	private final String REST_FOOD_URL = 
			"https://api.nal.usda.gov/ndb/reports/?type=f&format=json&api_key=API_KEY_GOES_HERE&ndbno=";
	private final String CALORIE_ID = "208";
	private static Client client;
	private static Map<String,String> calorieMap;
	private final int MINIMIZE_LIST_SIZE = 3000; // artificially limits the size of a list when looking minimize a nutrient content
	
	
	/**
	 * The constructor initializes the web client and creates an initial
	 * calorie map to use for comparison to other nutrient maps
	 */
	public REST_Svc() {
		client = ClientBuilder.newClient();
		calorieMap = buildMap(CALORIE_ID);
	}
	
	/**
	 * This method takes an array of food IDs and calls a method to
	 * retrieve the detailed reports for a supplied maximum number
	 * Calls: getFoodDetail
	 * @param arr an array of food ID strings
	 * @param max the quantity of foods to retrieve details for
	 * @return a list of JsonObjects containing nutrient details for foods
	 */
	public List<JsonObject> getFoodList(String[] arr, int max) {
		List<JsonObject> foods = new ArrayList<JsonObject>();
		if (max > arr.length) {
			max = arr.length;
		}
		for (int i = 0; i < max; i++) {
			foods.add(getFoodDetail(arr[i]));
		}		
		
		return foods;
	}
	
	/*
	 * This method performs a detail query for one food item
	 * represented by the food id string. Details are returned
	 * as a JsonObject.
	 * 
	 * @param id the string ID for the food being queried  
	 * @return a JsonObject representing the nutrient details of a food
	 */
	private JsonObject getFoodDetail(String id) {
		// performs REST request and processes through the InputStream
		// and JsonReader
		WebTarget target = client.target(REST_FOOD_URL + id);
		InputStream inStream = target.request().get(InputStream.class);
		JsonReader jReader = Json.createReader(inStream);
		JsonObject jsonObject = jReader.readObject();
		
		// This block pulls the details that are specific to the query
		// from the JsonObject that is returned from the server
		JsonObject jReport = jsonObject.getJsonObject("report");
		JsonObject foodDetail = jReport.getJsonObject("food");
		
		return foodDetail;
	}
	
	/**
	 * This method provides a simple way of stripping just food
	 * names from a list of JsonObjects for display or other output.
	 * 
	 * @param foodList a list of foods represented as JsonObjects
	 * @return an array of only the food names as Strings
	 */
	public String[] getFoodNames(List<JsonObject> foodList) {
		String[] names = new String[foodList.size()];
		for (int i = 0; i < names.length; i++) {
			names[i] = foodList.get(i).getString("name");
		}
		return names;
	}
	
	/*
	 * This method takes a nutrient ID, a maximum search result, 
	 * and a boolean for maximizing or minimizing the nutrient.
	 * If the user chooses to maximize, results with zero nutrient
	 * content are ignored. Results with zero calories are ignored to 
	 * prevent dividing by zero. Few foods in the database contain
	 * zero calories and they often provide little to no nutrition
	 * per serving, so results are minimally affected. 
	 * 
	 * Calls: buildMap
	 * @param nutrientID the string ID number of a specific nutrient
	 * @param max the maximum number of results desired from the query
	 * @param maximize boolean representing whether the user wants to maximize or minimize the nutrient
	 * @return a map of food ids and nutrient/calorie ratio
	 */
	private Map<String,Float> getRatioMap(String nutrientID, int max, boolean maximize) {
		Map<String,String> nutrientMap = buildMap(nutrientID, max);
		Map<String,Float> ratioMap = new HashMap<String,Float>();
		for (Map.Entry<String,String> m:nutrientMap.entrySet()) {
			float nutrient;
			float calories;
			if (calorieMap.get(m.getKey()) != null && m.getValue() != null) {
				nutrient = Float.valueOf(m.getValue());
				
				// retrieve calorie data for same food ID
				// data gathered on class instantiation
				calories = Float.valueOf(calorieMap.get(m.getKey()));
				
				// ignore results with zero calories and zero nutrient content
				// with maximize preference
				if (calories > 0 && nutrient > 0 && maximize) {
					float ratio = (nutrient / calories);
					ratioMap.put(m.getKey(), ratio);
				} else if (calories > 0 && !maximize) {
					float ratio = (nutrient / calories);
					ratioMap.put(m.getKey(), ratio);
				}
			}
		}
		
		return ratioMap;
	}
	
	/*
	 * Overloaded method of the above. This method does not use a maximum
	 * search result quantity, but is otherwise the same as above. Without
	 * the maximum result quantity, this method retrieves all foods containing
	 * relevant nutrient data. This is the preferred method for minimized nutrients
	 * because the server only sorts results high->low, meaning results with 
	 * minimal amounts of a specified nutrient are the very last results
	 * returned.
	 * 
	 * Calls: buildMap
	 * @param nutrientID
	 * @param maximize
	 * @return
	 */
	private Map<String,Float> getRatioMap(String nutrientID, boolean maximize) {
		Map<String,String> nutrientMap = buildMap(nutrientID);
		Map<String,Float> ratioMap = new HashMap<String,Float>();
		for (Map.Entry<String,String> m:nutrientMap.entrySet()) {
			float nutrient;
			float calories;
			if (calorieMap.get(m.getKey()) != null && m.getValue() != null) {
				nutrient = Float.valueOf(m.getValue());
				calories = Float.valueOf(calorieMap.get(m.getKey()));
				if (calories > 0 && nutrient > 0 && maximize) {
					float ratio = (nutrient / calories);
					ratioMap.put(m.getKey(), ratio);
				} else if (calories > 0 && !maximize) {
					float ratio = (nutrient / calories);
					ratioMap.put(m.getKey(), ratio);
				}
			}
		}
		
		return ratioMap;
	}
	
	/*
	 * This method takes a list of food ID arrays and returns 
	 * the intersection of all the arrays as a single array.
	 * 
	 * @param idArrays a list of String arrays contain food IDs
	 * @return an array of food ID Strings
	 */
	private String[] intersectArrays(List<String[]> idArrays) {
		List<String> ids = new ArrayList<String>();
		if (idArrays == null) {
			
		} else {
			Iterator<String[]> itr = idArrays.iterator();
			String[] tempArr = itr.next();
			
			//populate a list from an array
			for (int i = 0; i < tempArr.length; i++) {
				ids.add(tempArr[i]);
			}
			while (itr.hasNext()) {
				String[] tempArray = itr.next();
				List<String> temp = new ArrayList<String>();
				for (int i = 0; i < tempArray.length; i++) {
					temp.add(tempArray[i]);
				}
				
				// get intersection of two lists, retaining results
				ids.retainAll(temp);
			}
		}
		
		// convert List to Array
		String[] idArr = new String[ids.size()];
		idArr = ids.toArray(idArr);
		
		return idArr;
	}
	
	/*
	 * This method queries the USDA NDB using their REST API for a 
	 * list of foods and nutrient quantity for a single nutrient.
	 * The results arrive from the server sorted from high to low
	 * by the nutrient quantity. The food IDs and nutrient content
	 * are then returned as a Map. This method performs a search
	 * at maximum depth, meaning all foods with data for the requested
	 * nutrient value are returned. This is the preferred search
	 * for nutrient that are intended to be minimized by the user.
	 * 
	 * @param nutrientID a nutrient ID String to use in a REST query
	 * @return a map of food IDs and quantity of the queried nutrient
	 */
	private Map<String,String> buildMap(String nutrientID) {
		int offset = 0;
		int end = 0;
		int total = 1;
		Map<String,String> nutrientMap = new HashMap<String,String>();
		while (end < total) {
			WebTarget target = client.target("http://api.nal.usda.gov/ndb/nutrients/?format=json&api_key=iS2VhHkPCiBZMlQx6EaAIl3swHtIcblh6dyS1Mvh&nutrients=" + nutrientID + "&max=1500&sort=c&offset=" + offset);
			InputStream inStream = target.request().get(InputStream.class);
			JsonReader jReader = Json.createReader(inStream);
			JsonObject jsonObject = jReader.readObject();
			JsonObject jReport = jsonObject.getJsonObject("report");
			JsonArray jArray = jReport.getJsonArray("foods");
			for (JsonValue jObj : jArray) {
				JsonObject element = (JsonObject) jObj;
				String foodID = element.getString("ndbno");
				String calorieValue = element.getJsonArray("nutrients").getJsonObject(0).getString("value");
				if (calorieValue == null) {System.out.println(foodID);}
				nutrientMap.put(foodID, calorieValue);
			}
			total = jReport.getInt("total");
			end = jReport.getInt("end");
			offset = end;
		}
		
		return nutrientMap;
	}
	
	/*
	 * This method is an overloaded version of the above method and
	 * performs the same except to a specific search depth.
	 * This method queries the USDA NDB using their REST API for a 
	 * list of foods and nutrient quantity for a single nutrient.
	 * The results arrive from the server sorted from high to low
	 * by the nutrient quantity. The food IDs and nutrient content
	 * are then returned as a Map. This method performs a search
	 * at to a depth of the input integer max.
	 * 
	 * @param nutrientID a nutrient ID String to use in a REST query
	 * @param max the maximum depth of a search
	 * @return a map of food IDs and quantity of the queried nutrient
	 */
	private Map<String,String> buildMap(String nutrientID, int max) {
		int offset = 0;
		int end = 0;
		if (max <= 0) {max = 1;}
		int total = max;
		if (max > 1500) {
			max = 1500;
		}
		Map<String,String> nutrientMap = new HashMap<String,String>();
		while (end < total) {
			WebTarget target = client.target(REST_NUTRIENT_URL + nutrientID + "&max=" + max + "&sort=c&offset=" + offset);
			InputStream inStream = target.request().get(InputStream.class);
			JsonReader jReader = Json.createReader(inStream);
			JsonObject jsonObject = jReader.readObject();
			JsonObject jReport = jsonObject.getJsonObject("report");
			JsonArray jArray = jReport.getJsonArray("foods");
			
			// for each JsonObject in the array, add food ID and nutrient value to the Map
			for (JsonValue jObj : jArray) {
				JsonObject element = (JsonObject) jObj;
				String foodID = element.getString("ndbno");
				String calorieValue = element.getJsonArray("nutrients").getJsonObject(0).getString("value");
				nutrientMap.put(foodID, calorieValue);
			}
			end = jReport.getInt("end");
			offset = end;
			
			// this block ensures the total results are not more than the requested max
			// possible for some nutrients
			// repeats for each search in the while loop, not really an issue
			int checkTotal = jReport.getInt("total");
			if (checkTotal < total) {
				total = checkTotal;
			}
			
			// this block sets the requested quantity max to the remaining amount at the end of the
			// search if necessary
			if ((end + max) > total) {
				max = total - end;
			}
		}
		
		return nutrientMap;
	}
	
	/**
	 * This method organizes the manipulation of all the data based on the
	 * users input. Calling the other methods in this class to perform
	 * the necessary work.
	 * 
	 * Calls: getRatioMap, getSortedFoodIds, intersectArrays, getFoodDetail
	 * @param userIn a UserInput representing the users search parameters
	 * @param listMax the maximum number of detailed food objects to return
	 * @return list of food objects with all nutrient details
	 */
	public List<JsonObject> getRecommendations(UserInput userIn, int listMax) {
		List<Nutrient> nutrients = userIn.getUserNutrients();
		Integer depth = userIn.getMax();    // the maximum desired search results, lower depth creates very restrictive results
		
		// creates a list of maps containing food IDs with nutrient to calorie ratios
		// includes a parallel array of booleans to inform the sort to perform ascending or descending sort
		List<Map<String,Float>> mapList = new ArrayList<Map<String,Float>>();
		Boolean[] maximize = new Boolean[nutrients.size()];
		int maxIndex = 0;
		if (depth != null) {
			for (Nutrient n:nutrients) {
				boolean max = n.isMaximize();
				
				// this if statement allows a maximum depth map to be created for minimized nutrients
				if (!max) {
					mapList.add(getRatioMap(userIn.getNutrientID(n.getNutrient()), max));
				} else {
					mapList.add(getRatioMap(userIn.getNutrientID(n.getNutrient()), depth, max));
				}
				maximize[maxIndex] = max;
				maxIndex++;
			}
		} else {
			for (Nutrient n:nutrients) {
				boolean max = n.isMaximize();
				mapList.add(getRatioMap(userIn.getNutrientID(n.getNutrient()), max));
				maximize[maxIndex] = max;
				maxIndex++;
			}
		}
		
		// use ratio maps to create a list of food ID arrays sorted by ratio value
		List<String[]> idArrays = new ArrayList<String[]>();
		maxIndex = 0;
		for (Map<String,Float> m:mapList) {
			
			// This if block limits the size of an array for nutrients that the user has requested to minimize.
			// Searches are returned from the server sorted high to low only, so the list must be truncated
			// to the appropriate size after sorting.
			// An artificial limit is created with MINIMUM_LIST_SIZE to ensure that results are somewhat
			// relevant, when using the maximum/unlimited search depth.
			if (!maximize[maxIndex]) {
				String [] tempArr = getSortedFoodIds(m,maximize[maxIndex]);
				String[] smallerArr;
				if (depth != null && tempArr.length > depth) {
					smallerArr = new String[depth];
					for (int i = 0; i < smallerArr.length; i++) {
						smallerArr[i] = tempArr[i];
					} 
				} else if (tempArr.length > MINIMIZE_LIST_SIZE) {
					smallerArr = new String[MINIMIZE_LIST_SIZE];
					for (int i = 0; i < smallerArr.length; i++) {
						smallerArr[i] = tempArr[i];
					} 
				} else {
					smallerArr = tempArr;
				}
				idArrays.add(smallerArr);
			} else {
				idArrays.add(getSortedFoodIds(m, maximize[maxIndex]));
			}
			maxIndex++;
		}
		
		// gets the common food IDs from each array
		String[] ids = intersectArrays(idArrays);
		
		// gets the detail for each food that is left in the single list
		List<JsonObject> jsonFoods = new ArrayList<JsonObject>();
		if (listMax > ids.length) {
			for (int i = 0; i < ids.length; i++) {
				jsonFoods.add(getFoodDetail(ids[i]));
			}
		} else {
			for (int i = 0; i < listMax; i++) {
				jsonFoods.add(getFoodDetail(ids[i]));
			}
		}
		return jsonFoods;
	}
	
	/*
	 * This method takes a Map of food ID strings and nutrient/calorie
	 * ratio value and creates a sorted array of food IDs based
	 * on the nutrient/calorie ratio.
	 * 
	 * Calls: QuickParallelSort.sort()
	 * @param map a map of food IDs and nutrient/calorie ratios
	 * @param maximize true sorts high->low, false sorts low->high
	 * @return an array of food ids sorted by their nutrient/calorie ratio
	 */
	private String[] getSortedFoodIds(Map<String,Float> map, boolean maximize) {
		String[] ids = new String[map.size()];
		float[] ratios = new float[map.size()];
		int i = 0;
		for (Map.Entry<String, Float> m:map.entrySet()) {
			ids[i] = m.getKey();
			ratios[i] = m.getValue();
			i++;
		}
		
		QuickParallelSort quickSort = new QuickParallelSort();
		quickSort.sort(ratios, ids, maximize);
		
		return ids;
	}

}
