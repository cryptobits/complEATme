/*
 * Created by: Dane Hart
 * For CS493 Capstone Project
 * Regis University
 * 
 *  June 2017
 */

package complEATme.UI;

import java.awt.Dialog.ModalityType;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import complEATme.Objects.Nutrient;
import complEATme.Objects.UserInput;
import complEATme.Svc.REST_Svc;

import javax.swing.JLabel;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.JComboBox;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import java.awt.Font;

/**
 * This is the main class for the complEATme application.
 * This class creates the UI and calls all other methods
 * and services to perform a nutrient priority search of 
 * foods using the USDA NDB REST API. Based on user input
 * from a list of nutrients, the application will determine
 * recommended foods to meet the users nutrition goals.
 * The recommended foods will be displayed by name in a list
 * and can be selected to view the detailed nutrient content.
 */

public class MainWindow extends JFrame {
	
	// auto-generated serialization number
	private static final long serialVersionUID = -8289044589095233781L;
	
	// models used for displaying various lists in the UI
	// declared globally for easy access in listeners
	private DefaultComboBoxModel<String> model0;
	private DefaultComboBoxModel<String> model1;
	private DefaultComboBoxModel<String> model2;
	private DefaultComboBoxModel<String> model3;
	private DefaultComboBoxModel<String> model4;
	private DefaultComboBoxModel<String> model5;
	private DefaultComboBoxModel<String> depthModel;
	private DefaultListModel<String> nameModel;
	private DefaultListModel<String> detailModel;
	
	// other variables declared globally for access in listeners
	// and SwingWorkers
	private LoadingDialog loadingDialog;
	private JList<String> nameList;
	private ListSelectionListener nameListListener;
	private JPanel contentPane;
	private String[] nutrientList;
	private UserInput userInput;
	private REST_Svc service;
	private List<JsonObject> foodList;
	private String[] foodNames;
	
	// app name for UI heading and message string for dialog box
	private final String APP_NAME_STR = "complEATme";
	private final String MESSAGE_STR = "A query is currently being performed. Please wait...";
	
	// list size should be kept below 100 to allow multiple searches
	// depending on the depth and number of nutrients selected, each
	// press of the search button will perform (2 + LIST_SIZE) to
	// (36 + LIST_SIZE) requests. The server has a limit of 1000
	// requests per IP per hour.
	private final int LIST_SIZE = 50;
	
	// depth of search to be performed, lower numbers go much faster
	// searches for minimized nutrients will always be performed at 
	// max depth, due to REST server limitations.
	private final String[] DEPTH_ARRAY = {"500", "1000", "1500", "2000", "3000", "4500", "6000", "MAX"};
	
	// determines if the first nutrient in the list is allowed to be minimized
	// recommended to be set to false or to use only higher depth searches
	// as results can be very limited with lower depth
	private final boolean FIRST_MIN_RADIO_BTN = false;
	

	/**
	 * Main method simply starts the UI
	 * @param args this class does nothing with the args parameter 
	 * 
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Throwable e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow frame = new MainWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * The constructor for this class contains all of the UI elements and locations
	 * as well as the listeners for the various interactive components. See listeners
	 * within for details on how the UI functions.
	 */
	public MainWindow() {
		
		// initializing the window
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1122, 742);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);	
		
		// generates UserInput class for holding various user selections
		userInput = new UserInput();
		
		// UserInput class contains a list of the valid nutrients to use for comboBox lists
		nutrientList = userInput.getAllNutrients();
		Arrays.sort(nutrientList);
		
		// model for first comboBox
		model0 = new DefaultComboBoxModel<String>(nutrientList);
		
		JLabel nutrientLabel = new JLabel("Select Nutrient");
		nutrientLabel.setToolTipText("Must select at least one nutrient.");
		
		JButton btnNewButton = new JButton("Search");
		
		// models for populating JList boxes
		nameModel = new DefaultListModel<String>();
		detailModel = new DefaultListModel<String>();
		
		// scroll functionality and list styles for food name list
		JScrollPane scrollPane = new JScrollPane();
		nameList = new JList<String>();
		scrollPane.setViewportView(nameList);
		nameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		nameList.setLayoutOrientation(JList.VERTICAL);
		nameList.setModel(nameModel);
		
		// scroll functionality and list styles for food detail list
		// this list allows multiple selection so the user can easily
		// copy the information if desired
		// the selection has no functionality in this program
		JScrollPane scrollPane_1 = new JScrollPane();
		JList<String> detailList = new JList<String>();
		scrollPane_1.setViewportView(detailList);
		detailList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		detailList.setLayoutOrientation(JList.VERTICAL);
		detailList.setModel(detailModel);
		 
		// first comboBox initialized to no selection because it looks nicer when started
		JComboBox<String> comboBox_0 = new JComboBox<String>(model0);
		comboBox_0.setToolTipText("Select a nutrient to maximize. This list should be populated by importance/priority.");
		comboBox_0.setSelectedIndex(-1);
		JComboBox<String> comboBox_1 = new JComboBox<String>();
		comboBox_1.setToolTipText("Further nutrients are optional.");
		JComboBox<String> comboBox_2 = new JComboBox<String>();
		comboBox_2.setToolTipText("Further nutrients are optional");
		JComboBox<String> comboBox_3 = new JComboBox<String>();
		comboBox_3.setToolTipText("Further nutrients are optional.");
		JComboBox<String> comboBox_4 = new JComboBox<String>();
		comboBox_4.setToolTipText("Further nutrients are optional");
		JComboBox<String> comboBox_5 = new JComboBox<String>();
		comboBox_5.setToolTipText("Further nutrients are optional");
		
		// minimize radio buttons
		JRadioButton radioButtonHidden = new JRadioButton("");
		radioButtonHidden.setToolTipText("First nutrient is set to Max only.");
		JRadioButton radioBtnMin_2 = new JRadioButton("");
		radioBtnMin_2.setToolTipText("Minimizing nutrients can take longer, as search depth is performed at MAX.");
		JRadioButton radioBtnMin_3 = new JRadioButton("");
		radioBtnMin_3.setToolTipText("Minimizing nutrients can take longer, as search depth is performed at MAX.");
		JRadioButton radioBtnMin_4 = new JRadioButton("");
		radioBtnMin_4.setToolTipText("Minimizing nutrients can take longer, as search depth is performed at MAX.");
		JRadioButton radioBtnMin_5 = new JRadioButton("");
		radioBtnMin_5.setToolTipText("Minimizing nutrients can take longer, as search depth is performed at MAX.");
		JRadioButton radioBtnMin_6 = new JRadioButton("");
		radioBtnMin_6.setToolTipText("Minimizing nutrients can take longer, as search depth is performed at MAX.");
		
		// maximize radio buttons set to max by default
		JRadioButton radioMaxOnly = new JRadioButton("");
		radioMaxOnly.setSelected(true);
		JRadioButton radioBtnMax_2 = new JRadioButton("");
		radioBtnMax_2.setSelected(true);
		JRadioButton radioBtnMax_3 = new JRadioButton("");
		radioBtnMax_3.setSelected(true);
		JRadioButton radioBtnMax_4 = new JRadioButton("");
		radioBtnMax_4.setSelected(true);
		JRadioButton radioBtnMax_5 = new JRadioButton("");
		radioBtnMax_5.setSelected(true);
		JRadioButton radioBtnMax_6 = new JRadioButton("");
		radioBtnMax_6.setSelected(true);
		
		// ButtonGroups ensure only one radio button from a pair is selected
		ButtonGroup btnGroup_0 = new ButtonGroup();
		btnGroup_0.add(radioMaxOnly);
		btnGroup_0.add(radioButtonHidden);
		ButtonGroup btnGroup_1 = new ButtonGroup();
		btnGroup_1.add(radioBtnMax_2);
		btnGroup_1.add(radioBtnMin_2);
		ButtonGroup btnGroup_2 = new ButtonGroup();
		btnGroup_2.add(radioBtnMax_3);
		btnGroup_2.add(radioBtnMin_3);
		ButtonGroup btnGroup_3 = new ButtonGroup();
		btnGroup_3.add(radioBtnMax_4);
		btnGroup_3.add(radioBtnMin_4);
		ButtonGroup btnGroup_4 = new ButtonGroup();
		btnGroup_4.add(radioBtnMax_5);
		btnGroup_4.add(radioBtnMin_5);
		ButtonGroup btnGroup_5 = new ButtonGroup();
		btnGroup_5.add(radioBtnMax_6);
		btnGroup_5.add(radioBtnMin_6);
		
		// radio button labels
		JLabel lblMin = new JLabel("Min");
		JLabel lblMax = new JLabel("Max");
		
		// disables the minimize radio button for the first drop down box
		// can be enabled, but due to the number of products with zero or
		// near zero nutrients it is not as informative to have the first
		// nutrient be minimized.
		radioButtonHidden.setEnabled(FIRST_MIN_RADIO_BTN);
		
		JLabel lblSelectFoodTo = new JLabel("Select a food to see nutrient content");
		lblSelectFoodTo.setToolTipText("Perform a search to populate this list.");
		
		// comboBox for depth of search to be performed. 
		JComboBox<String> depthComboBox = new JComboBox<String>();
		depthComboBox.setToolTipText("The higher the search depth, the more possible results, but the longer it will take.");
		depthModel = new DefaultComboBoxModel<String>(DEPTH_ARRAY);
		depthComboBox.setModel(depthModel);
		depthComboBox.setSelectedIndex(2);
		
		JLabel lblDepth = new JLabel("Depth");
		
		JLabel lblFoodDetails = new JLabel("Nutrient content per 100g (3.5oz)");
		lblFoodDetails.setToolTipText("Select a food item after searching to populate this box.");
		
		JLabel lblCompleteme = new JLabel(APP_NAME_STR);
		lblCompleteme.setFont(new Font("SansSerif", Font.PLAIN, 42));
		
		JLabel lblNewLabel = new JLabel("<html><b>Search Tips:</b> A lower depth will offer fewer results that better fit the selected criteria. If no results or too few results come up, a larger depth should be attempted. The larger the depth number, the more compromise there is on the search criteria. Depth can be thought of as assessing the top X amount for each nutrient (e.g., a depth of 500, for a search looking to maximize Protein and Vitamin A, will use the top 500 Protein rich foods and the top 500 Vitamin A rich foods to create the result). The result displays up to 50 foods that meet the search criteria. Higher depth numbers will also take longer. Depths up to 1500 should offer no appreciable difference in speed performance.</html>");
		
		
		// code generated by WindowBuilder for locating the UI components
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGap(6)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_contentPane.createSequentialGroup()
									.addPreferredGap(ComponentPlacement.RELATED)
									.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
										.addComponent(comboBox_2, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)
										.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING, false)
											.addComponent(comboBox_0, Alignment.LEADING, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
											.addComponent(comboBox_1, Alignment.LEADING, 0, 170, Short.MAX_VALUE))
										.addComponent(comboBox_3, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)
										.addComponent(comboBox_4, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)
										.addComponent(comboBox_5, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)))
								.addGroup(gl_contentPane.createSequentialGroup()
									.addGap(39)
									.addComponent(nutrientLabel)))
							.addGap(18)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_contentPane.createSequentialGroup()
									.addComponent(radioBtnMin_3, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
									.addGap(18)
									.addComponent(radioBtnMax_3, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_contentPane.createSequentialGroup()
									.addComponent(radioBtnMin_4, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
									.addGap(18)
									.addComponent(radioBtnMax_4, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_contentPane.createSequentialGroup()
									.addComponent(radioBtnMin_5, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
									.addGap(18)
									.addComponent(radioBtnMax_5, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_contentPane.createSequentialGroup()
									.addComponent(radioBtnMin_6, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
									.addGap(18)
									.addComponent(radioBtnMax_6, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_contentPane.createSequentialGroup()
									.addComponent(radioBtnMin_2, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
									.addGap(18)
									.addComponent(radioBtnMax_2, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_contentPane.createSequentialGroup()
									.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
										.addComponent(lblMin)
										.addComponent(radioButtonHidden, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
									.addGap(18)
									.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
										.addComponent(radioMaxOnly, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
										.addComponent(lblMax))))
							.addGap(28))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(lblDepth)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(depthComboBox, GroupLayout.PREFERRED_SIZE, 77, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addComponent(btnNewButton)
							.addGap(40))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(lblNewLabel, GroupLayout.PREFERRED_SIZE, 254, GroupLayout.PREFERRED_SIZE)
							.addGap(15)))
					.addGap(28)
					.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 360, GroupLayout.PREFERRED_SIZE)
					.addGap(36)
					.addComponent(scrollPane_1, GroupLayout.PREFERRED_SIZE, 335, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(57, Short.MAX_VALUE))
				.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
					.addContainerGap(378, Short.MAX_VALUE)
					.addComponent(lblSelectFoodTo)
					.addGap(200)
					.addComponent(lblFoodDetails)
					.addGap(142))
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGap(25)
					.addComponent(lblCompleteme)
					.addContainerGap(817, Short.MAX_VALUE))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblCompleteme)
					.addGap(29)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblFoodDetails)
						.addComponent(lblSelectFoodTo))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addComponent(scrollPane_1, GroupLayout.PREFERRED_SIZE, 565, GroupLayout.PREFERRED_SIZE)
						.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 569, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblMin)
								.addComponent(lblMax)
								.addComponent(nutrientLabel))
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
								.addComponent(radioMaxOnly)
								.addComponent(comboBox_0, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(radioButtonHidden))
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
								.addComponent(comboBox_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(radioBtnMin_2)
								.addComponent(radioBtnMax_2))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
								.addComponent(comboBox_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(radioBtnMin_3)
								.addComponent(radioBtnMax_3))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
								.addComponent(comboBox_3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(radioBtnMin_4)
								.addComponent(radioBtnMax_4))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
								.addComponent(comboBox_4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(radioBtnMin_5)
								.addComponent(radioBtnMax_5))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
								.addComponent(comboBox_5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(radioBtnMin_6)
								.addComponent(radioBtnMax_6))
							.addGap(18)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
								.addComponent(btnNewButton)
								.addComponent(depthComboBox, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblDepth))
							.addGap(32)
							.addComponent(lblNewLabel, GroupLayout.PREFERRED_SIZE, 254, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(13, Short.MAX_VALUE))
		);
		
		// applying the above code to the window
		contentPane.setLayout(gl_contentPane);
		
		// listener for the list of food names available after a search is performed
		// Gets a JsonObject from the list matching the index of the selection to 
		// populate a model with a list of strings that represent the
		// nutrient, quantity, and unit of measure.
		// listeners are assigned to variables instead of generated directly on
		// the object so they can be removed and re-applied for optimal functionality 
		nameListListener = (new ListSelectionListener() {
		      public void valueChanged(ListSelectionEvent listSelectionEvent) {
		    	  // this block matches the selected index to a JsonObject in the list
		    	  int index = nameList.getSelectedIndex();
		    	  JsonObject food = foodList.get(index);
		    	  
		    	  // this clears the current elements from the detail list model
		    	  // and populates the list model with the nutrients for the 
		    	  // selected food
		    	  JsonArray nutrientArray = food.getJsonArray("nutrients");
		    	  detailModel.clear();
		    	  List<String> highlightStrings = new ArrayList<String>();
		    	  for(JsonValue jVal:nutrientArray) {
		    		  JsonObject jObj = (JsonObject) jVal;
		    		  String str1 = jObj.getString("name");
		    		  JsonNumber jNum = (JsonNumber)jObj.get("value");
		    		  double num = jNum.doubleValue();
		    		  String str2 = jObj.getString("unit");
		    		  
		    		  // this block adds bold font to nutrients that were part of
		    		  // the users query and save them to a list
		    		  // the if statements compare the nutrient_id value because
		    		  // the comboBox nutrient strings are simplified compared
		    		  // to the nutrient string provided by the food detail query
		    		  if (String.valueOf(jObj.getInt("nutrient_id")).equals(userInput.getNutrientID(((String)comboBox_0.getSelectedItem())))) {
		    			  highlightStrings.add("<html><b>" + str1 + ": " + num + " " + str2 + "</b><html>");
		    		  } else if (String.valueOf(jObj.getInt("nutrient_id")).equals(userInput.getNutrientID(((String)comboBox_1.getSelectedItem())))) {
		    			  highlightStrings.add("<html><b>" + str1 + ": " + num + " " + str2 + "</b><html>");
		    		  } else if (String.valueOf(jObj.getInt("nutrient_id")).equals(userInput.getNutrientID(((String)comboBox_2.getSelectedItem())))) {
		    			  highlightStrings.add("<html><b>" + str1 + ": " + num + " " + str2 + "</b><html>");
		    		  } else if (String.valueOf(jObj.getInt("nutrient_id")).equals(userInput.getNutrientID(((String)comboBox_3.getSelectedItem())))) {
		    			  highlightStrings.add("<html><b>" + str1 + ": " + num + " " + str2 + "</b><html>");
		    		  } else if (String.valueOf(jObj.getInt("nutrient_id")).equals(userInput.getNutrientID(((String)comboBox_4.getSelectedItem())))) {
		    			  highlightStrings.add("<html><b>" + str1 + ": " + num + " " + str2 + "</b><html>");
		    		  } else if (String.valueOf(jObj.getInt("nutrient_id")).equals(userInput.getNutrientID(((String)comboBox_5.getSelectedItem())))) {
		    			  highlightStrings.add("<html><b>" + str1 + ": " + num + " " + str2 + "</b><html>");
		    		  } else {
		    			  detailModel.addElement(str1 + ": " + num + " " + str2);
		    		  }  
		    	  }
		    	  
		    	  // this block adds the user selected nutrients to the top of
	    		  // the list for display
	    		  int i = 0;
	    		  for(String s:highlightStrings) {
	    			  detailModel.add(i, s);
	    			  i++;
		    	  }
		      }
		    });
		
		// adds the listener to the name list
		nameList.addListSelectionListener(nameListListener);
		
		// search button listener checks to see which drop down lists have been
		// populated and whether minimum or maximum has been set. It applies these
		// selections in order to a UserInput object. Once the user input has
		// been colleted, it calls a dialog box and performs the queries
		// inside of a SwingWorker
		ActionListener searchButton_Listener = (new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UserInput tempInput = new UserInput();
				
				String selected0 = (String)comboBox_0.getSelectedItem();
				if (selected0 != null) {
					Nutrient n = new Nutrient(selected0, true);
					tempInput.addUserNutrient(n);
				}
				
				String selected1 = (String)comboBox_1.getSelectedItem();
				boolean maximize1 = true;
				if (radioBtnMin_2.isSelected()) {maximize1 = false;}
				if (selected1 != null) {
					Nutrient n = new Nutrient(selected1, maximize1);
					tempInput.addUserNutrient(n);
				}
				
				String selected2 = (String)comboBox_2.getSelectedItem();
				boolean maximize2 = true;
				if (radioBtnMin_3.isSelected()) {maximize2 = false;}
				if (selected2 != null) {
					Nutrient n = new Nutrient(selected2, maximize2);
					tempInput.addUserNutrient(n);
				}
				
				String selected3 = (String)comboBox_3.getSelectedItem();
				boolean maximize3 = true;
				if (radioBtnMin_4.isSelected()) {maximize3 = false;}
				if (selected3 != null) {
					Nutrient n = new Nutrient(selected3, maximize3);
					tempInput.addUserNutrient(n);
				}
				
				String selected4 = (String)comboBox_4.getSelectedItem();
				boolean maximize4 = true;
				if (radioBtnMin_5.isSelected()) {maximize4 = false;}
				if (selected4 != null) {
					Nutrient n = new Nutrient(selected4, maximize4);
					tempInput.addUserNutrient(n);
				}
				
				String selected5 = (String)comboBox_5.getSelectedItem();
				boolean maximize5 = true;
				if (radioBtnMin_6.isSelected()) {maximize5 = false;}
				if (selected5 != null) {
					Nutrient n = new Nutrient(selected5, maximize5);
					tempInput.addUserNutrient(n);
				}
				
				// verifies validity of user input before passing it on to search
				if (tempInput.getUserNutrients() != null) {
					
					String maxDepth = (String)depthComboBox.getSelectedItem();
					if (maxDepth.equals("MAX")) {
						// do not set a maximum
					} else {
						tempInput.setMax(Integer.valueOf(maxDepth));
					}
					userInput = tempInput;
					
					// create dialog box and perform query in SwingWorkers
					new FoodQueryWorker().execute();
					new DialogWorker().execute();
				}
			}
	    });
		
		// adds listener to search button
		btnNewButton.addActionListener(searchButton_Listener);
		
		
		// all remaining listeners are for the ComboBox drop down menus
		// each listener populates the next ComboBox below it with all
		// nutrients not selected within or above it
		// if one higher level box is changed, all boxes below it are
		// cleared of their entries, this ensures no boxes have the 
		// same nutrient selected
		// the last ComboBox does not have a listener because it does
		// not effect the contents of any other boxes
		// listeners for the next ComboBox being populated are removed
		// while being populated to prevent the activation of the listener
		ActionListener cB4_Listener = (new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object selected0 = comboBox_0.getSelectedItem();
				Object selected1 = comboBox_1.getSelectedItem();
				Object selected2 = comboBox_2.getSelectedItem();
				Object selected3 = comboBox_3.getSelectedItem();
				Object selected4 = comboBox_4.getSelectedItem();
				model5 = new DefaultComboBoxModel<String>(nutrientList);
				model5.removeElement(selected0);
				model5.removeElement(selected1);
				model5.removeElement(selected2);
				model5.removeElement(selected3);
				model5.removeElement(selected4);
				comboBox_5.setModel(model5);
				comboBox_5.setSelectedIndex(-1);
				
			}
	    });
		
		ActionListener cB3_Listener = (new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object selected0 = comboBox_0.getSelectedItem();
				Object selected1 = comboBox_1.getSelectedItem();
				Object selected2 = comboBox_2.getSelectedItem();
				Object selected3 = comboBox_3.getSelectedItem();
				model4 = new DefaultComboBoxModel<String>(nutrientList);
				model4.removeElement(selected0);
				model4.removeElement(selected1);
				model4.removeElement(selected2);
				model4.removeElement(selected3);
				comboBox_4.removeActionListener(cB4_Listener);
				comboBox_4.setModel(model4);
				comboBox_4.setSelectedIndex(-1);
				comboBox_4.addActionListener(cB4_Listener);
				comboBox_5.removeAllItems();
				
			}
	    });
		
		ActionListener cB2_Listener = (new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object selected0 = comboBox_0.getSelectedItem();
				Object selected1 = comboBox_1.getSelectedItem();
				Object selected2 = comboBox_2.getSelectedItem();
				model3 = new DefaultComboBoxModel<String>(nutrientList);
				model3.removeElement(selected0);
				model3.removeElement(selected1);
				model3.removeElement(selected2);
				comboBox_3.removeActionListener(cB3_Listener);
				comboBox_3.setModel(model3);
				comboBox_3.setSelectedIndex(-1);
				comboBox_3.addActionListener(cB3_Listener);
				comboBox_4.removeAllItems();
				comboBox_5.removeAllItems();				
			}
	    });
		
		ActionListener cB1_Listener = (new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object selected0 = comboBox_0.getSelectedItem();
				Object selected1 = comboBox_1.getSelectedItem();
				model2 = new DefaultComboBoxModel<String>(nutrientList);
				model2.removeElement(selected0);
				model2.removeElement(selected1);
				comboBox_2.removeActionListener(cB2_Listener);
				comboBox_2.setModel(model2);
				comboBox_2.setSelectedIndex(-1);
				comboBox_2.addActionListener(cB2_Listener);
				comboBox_3.removeAllItems();
				comboBox_4.removeAllItems();
				comboBox_5.removeAllItems();
			}
	    });
		
		ActionListener cB0_Listener = (new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object selected0 = comboBox_0.getSelectedItem();
				model1 = new DefaultComboBoxModel<String>(nutrientList);
				model1.removeElement(selected0);
				comboBox_1.removeActionListener(cB1_Listener);
				comboBox_1.setModel(model1);
				comboBox_1.setSelectedIndex(-1);
				comboBox_1.addActionListener(cB1_Listener);
				comboBox_2.removeAllItems();
				comboBox_3.removeAllItems();
				comboBox_4.removeAllItems();
				comboBox_5.removeAllItems();
			}
	    });
		
		// add listeners to ComboBoxes
		comboBox_0.addActionListener(cB0_Listener);
		comboBox_1.addActionListener(cB1_Listener);
		comboBox_2.addActionListener(cB2_Listener);
		comboBox_3.addActionListener(cB3_Listener);
		comboBox_4.addActionListener(cB4_Listener);
		
		// create initial query and loading dialog box within SwingWorkers	
		new InitialQueryWorker().execute();
		new DialogWorker().execute();
		
	}
	
	
	/**
	 * 
	 * @author Dane Hart
	 * This SwingWorker initializes the REST_Svc, which performs the initial
	 * query for all foods and their calorie content. This SwingWorker must be
	 * used in combination with the DialogWorker to prevent interruption or
	 * other unexpected functionality. 
	 * The calorie content is used for all other queries to create a nutrient 
	 * to calorie ratio. When the query is finished it dismisses the DialogBox
	 * running inside of the other SwingWorker
	 * 
	 */
	class InitialQueryWorker extends SwingWorker<Object,Void> {

		@Override
		protected Object doInBackground() {
			service = new REST_Svc();
			return null;
		}
		
		@Override
		protected void done() {
			loadingDialog.dispose();
		}
	}
	
	/**
	 * 
	 * @author Dane Hart
	 * This SwingWorker creates a LoadingDiaglog which disables the main UI
	 * while a search is being performed, preventing interruption and unexpected
	 * behavior that could happen. It serves to let the user know that
	 * something is happening, otherwise the program can appear to be frozen.
	 * It is opened inside of a SwingWorker because otherwise the dialog box
	 * takes over the main thread and is not easily programmatically dismissed
	 * when the query is finished.
	 *
	 */
	class DialogWorker extends SwingWorker<Object,Void> {

		@Override
		protected Object doInBackground() {
			loadingDialog = new LoadingDialog(APP_NAME_STR, MESSAGE_STR);
			
			// disables the windows close button
			loadingDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			
			// prevents activation of the underlying UI
			loadingDialog.setModalityType(ModalityType.APPLICATION_MODAL);
			
			// sets to center of the activating window and displays the DialogBox
			loadingDialog.setLocationRelativeTo(contentPane);
			loadingDialog.setVisible(true);	
			return null;
		}
		
	}
	
	/**
	 * 
	 * @author Dane Hart
	 * This SwingWorker performs the query for the search button and dismisses
	 * the LoadingDialog when finished. This worker should only be called in 
	 * combination with a DialogWorker, because the DialogWorker prevents further
	 * interaction with the UI on the main thread, preventing interruption. The
	 * DialogWorker also informs the user that a search is being performed.
	 *
	 */
	class FoodQueryWorker extends SwingWorker<Object,Void> {

		@Override
		protected Object doInBackground() {
			foodQuery();
			
			return null;
		}
		
		@Override
		protected void done() {
			loadingDialog.dispose();
			// this block creates a list of food names to display in the food list box
			// and removes the listener during population to prevent unexpected events
			// removes elements from detail list to prevent null pointer exception
			// detail list will be re-populated by food name list listener
			detailModel.clear();
			nameList.removeListSelectionListener(nameListListener);
			nameModel.clear();
			loadingDialog.setVisible(false);
			for (int i = 0; i < foodNames.length; i++) {
				nameModel.addElement(foodNames[i]);
			}
			nameList.addListSelectionListener(nameListListener);
		}
		
	}
	
	/*
	 * This method performs a query with the information gathered by the 
	 * search button. Should only be called from the search button listener.
	 * Best if called from within the FoodQueryWorker to prevent the 
	 * program from appearing frozen. 
	 */
	private void foodQuery () {
		// retrieves the list of JsonObjects that match the search criteria
		foodList = service.getRecommendations(userInput, LIST_SIZE);
		foodNames = service.getFoodNames(foodList);
		
	}
}
