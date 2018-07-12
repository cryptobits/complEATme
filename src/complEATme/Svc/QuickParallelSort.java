/*
 * Created by: Dane Hart
 * For CS493 Capstone Project
 * Regis University
 * 
 *  June 2017
 */

package complEATme.Svc;


/**
 * A standard quick sort algorithm, modified to accept two parallel
 * arrays. One array contains float values that the algorithm can
 * sort by, the other contains strings that will be moved around
 * with their matching float values.
 * @author Dane Hart
 *
 */

public class QuickParallelSort {
	private float array[];
    private int length;
    private String pArray[];
    private int pLength;
    
    /**
     * This method allows the sort to be called for standard low to high sorting.
     * Calls: sort()
     * @param inputArr float array to sort
     * @param parallelArr parallel string array to maintain parallelism
     */
    public void sort(float[] inputArr, String[] parallelArr) {
    	boolean backwards = false;
    	sort(inputArr, parallelArr, backwards);
    }
 
    /**
     * This method is used to determine whether the sort should be done in
     * ascending or descending manner. 
     * Calls: quickSortDesc, quickSortAsc
     * @param inputArr float array to sort
     * @param parallelArr parallel string array to maintain parallelism
     * @param backwards false to sort ascending, true for descending
     */
    public void sort(float[] inputArr, String[] parallelArr, boolean backwards) {
         
        if (inputArr == null || inputArr.length == 0) {
            return;
        }
        this.array = inputArr;
        this.pArray = parallelArr;
        length = inputArr.length;
        pLength = parallelArr.length;
        
        if (length != pLength) {
        	return;
        }
        
        if (backwards) {
        	quickSortDesc(0, length - 1);
        } else {
        	quickSortAsc(0, length - 1);
        }
    }
 
    /*
     * This method performs a descending parallel quick sort.
     * Calls: quickSortDesc, switchValues
     * @param lowerIndex the lower index
     * @param higherIndex the upper index
     */
    private void quickSortDesc(int lowerIndex, int higherIndex) {
         
        int i = lowerIndex;
        int j = higherIndex;
        // calculate pivot number, I am taking pivot as middle index number
        float pivot = array[lowerIndex+(higherIndex-lowerIndex)/2];
        // Divide into two arrays
        while (i <= j) {
            
            while (array[i] > pivot) {
                i++;
            }
            while (array[j] < pivot) {
                j--;
            }
            if (i <= j) {
                switchValues(i, j);
                // increment/decrement position
                i++;
                j--;
            }
        }
        
        // calls method recursively
        if (lowerIndex < j)
            quickSortDesc(lowerIndex, j);
        if (i < higherIndex)
            quickSortDesc(i, higherIndex);
    }
    
    /*
     * This method performs an ascending parallel quick sort.
     * Calls: quickSortAsc, switchValues
     * @param lowerIndex the lower index
     * @param higherIndex the upper index
     */
    private void quickSortAsc(int lowerIndex, int higherIndex) {
        
        int i = lowerIndex;
        int j = higherIndex;
        
        // calculate pivot number
        float pivot = array[lowerIndex+(higherIndex-lowerIndex)/2];
        
        // divide into two arrays
        while (i <= j) {
            
            while (array[i] < pivot) {
                i++;
            }
            while (array[j] > pivot) {
                j--;
            }
            if (i <= j) {
                switchValues(i, j);
                //increment/decrement position
                i++;
                j--;
            }
        }
        // calls method recursively
        if (lowerIndex < j)
            quickSortAsc(lowerIndex, j);
        if (i < higherIndex)
            quickSortAsc(i, higherIndex);
    }
 
    /*
     * Switches two parallel indexes in two parallel arrays
     * @param i one index to switch
     * @param j the other index to switch
     */
    
    private void switchValues(int i, int j) {
        float temp = array[i];
        String temp2 = pArray[i];
        array[i] = array[j];
        pArray[i] = pArray[j];
        array[j] = temp;
        pArray[j] = temp2;
    }
     
}
