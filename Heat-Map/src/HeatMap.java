/***
 * @file HeatMap.java
 * @author John Nguyen
 * CPSC 5600 HW6
 * 
 * Using the GenericScan3 class, this HeatMap takes a file of observations 
 * and draws a hypothetical two dimensional colored histogram
 * Based on the ColoredGrid Example, as well as the GeneralScan3 solution.
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;



public class HeatMap{
	//Constant for dimension size
	private static final int DIM = 11;
	//Constant for tally threshold size
	private static final int THRESHOLD = 20;

	/***
	 * Static Class Tally to hold Observation Data
	 * @author John
	 * Contains a Queue of observation history, as well as a Map of timestamps and observation grids
	 */
	public static class Tally{
		public int[][] map;
		public HashMap<Long,int[][]> history;
		public int threshold;
		public Queue<Observation> obs_history;

		///Constructors
		/***
		 * Default constructor
		 */
		public Tally() {
			//default size is 25 sections.
			map = new int[DIM][DIM];
			history = new HashMap<Long,int[][]>();
			threshold =THRESHOLD;
			obs_history = new LinkedList<Observation>();
		}
		
		/***
		 * Constructor with an initial observation
		 * @param o Observation to add to the initial tally
		 */
		public Tally(Observation o) {

			map = new int[DIM][DIM];
			history = new HashMap<Long,int[][]>();
			threshold = THRESHOLD;
			obs_history = new LinkedList<Observation>();
			accum(o);
		}

		
		
		/***
		 * combine()
		 * Takes two Tallys and combines their maps.  
		 * @param a Tally - the left or older tally
		 * @param b Tally - the right or newer tally
		 * @return
		 * 
		 */
		@SuppressWarnings("unchecked")
		public static Tally combine(Tally a, Tally b) {
			Tally result = new Tally();
			//copy the history
			result.history = (HashMap<Long, int[][]>) a.history.clone();
			
			//sum the contents of each cell from both tallies and add to result's current map
			for(int j = 0; j<DIM; j++) {
				for(int k = 0; k<DIM; k++) {
					
					result.map[j][k] = a.map[j][k] + b.map[j][k];
				}
			}
			//update each of this hashmap's maps
			for(HashMap.Entry<Long, int[][]> entry : b.history.entrySet()) {
			    Long key = entry.getKey();
			    int[][] value = entry.getValue();

			    //combine b maps into a
			    for(int j = 0; j<11; j++) {
					for(int k = 0; k<11; k++) {
						a.history.getOrDefault(key, new int[DIM][DIM])[j][k] += value[j][k];
					}
				}
			}
			
			//update the observation history queue with both sets
			result.obs_history.addAll(a.obs_history);
			result.obs_history.addAll(b.obs_history);
			//trim to the current threshold
			result.trim();
			
			return result;			
			
		}
		
		/***
		 * accum()
		 * @param datum Observation to be accumulated into tally
		 * add the observation to the current HeatMap
		 */
		public void accum(Observation datum) {

			//change the observation coordinates (-1.0 to 1.0) into matrix coordinates (0 to 10) 
			int x = (int)((5*datum.x)+5);
			int y = (int)((5*datum.y)+5);
			//
			map[x][y]++;
			//add the new map to the history for scan purposes. 
			int[][] tempMap = history.getOrDefault(datum.time, new int[DIM][DIM]);
			tempMap[x][y]++;
			history.put( datum.time, tempMap);
			obs_history.add(datum);
			this.trim();
		}
		
		/***
		 * trim()
		 * Removes any observations that are outside the thresholded range
		 */
		private void trim() {
			while(obs_history.size()>threshold) {
				//grab the oldest from the head of the queue
				Observation old_obs = obs_history.remove();
				//convert observation coordinates
				int x = (int)((5*old_obs.x)+5);
				int y = (int)((5*old_obs.y)+5);
				//remove the observation from the current map
				map[x][y]--;
				//Update the history to remove the observeration
				int[][] tempMap = history.getOrDefault(old_obs.time, new int[DIM][DIM]);
				tempMap[x][y]--;
				history.put( old_obs.time, tempMap);
			}
		}
		
		
		
	}
	
	
	/***
	 * Nested Class that extends the GeneralScan3 from the HW5 Solution
	 * @author John
	 * Encountered several issues with my own general scan that was turned in for HW5, used the provided solution to move past the road block
	 * 
	 */
	public static class HeatScan extends GeneralScan3<Observation,Tally>{
		
	
		public HeatScan(List<Observation> raw, int threshold) {
			super(raw, threshold);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		protected Tally init() {
			return new Tally();
		}
	
		
		/***
		 * prepare()
		 * @param datum Observation
		 * @return Tally
		 * Uses an observation to create a new Tally Object
		 */
		@Override
		protected Tally prepare(Observation datum) {
			return new Tally(datum);
		}
		
		/***
		 * combine()
		 * @param left Tally
		 * @param right Tally
		 * @return new Tally
		 * Call's the tally class's combine function
		 */
		@Override
		protected Tally combine(Tally left, Tally right) {
			return Tally.combine(left, right);
		}
	
		/***
		 * accum()
		 * @param tally Tally tally to be modified
		 * @param datum Observation to be added to the tally
		 * Call's the Tally class's accum function
		 */
		@Override
		protected void accum(Tally tally, Observation datum) {
			tally.accum(datum);
		}
	
	//End of HeatScan Class
	}
	
	//private variables for color grid
	
	private static final String REPLAY = "Replay";
	private static JFrame application;
	private static JButton button;
	private static Color[][] grid;
	
	public static void main(String[] args) throws InterruptedException {
		//Read in File data (observation_test.dat)
		final String FILENAME = "observation_test.dat";
		ArrayList<Observation> observations = new ArrayList<Observation>(400);
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILENAME));

			Observation obs = (Observation) in.readObject();
			while (!obs.isEOF()) {
				//System.out.println(++count + ": " + obs);
				// add the observation to an ArrayList
				observations.add(obs);
				obs = (Observation) in.readObject();
			}
			in.close();
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("reading from " + FILENAME + "failed: " + e);
         e.printStackTrace();
         System.exit(1);
		}
		
		//Using the list of observations and count, we can make a 2Dimensional Array

		
		//Create the prefix scan object with a threshold
		HeatScan pScan = new HeatMap.HeatScan(observations, 50);
		
		//Compute reduction	
		pScan.reduce(0);

		//create arraylist for storing scan result and produce a scan
		List<Tally> output = pScan.getScan();

		
		//ColorGrid stuff modified from example
		grid = new Color[DIM][DIM];
		application = new JFrame();
		application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fillGrid(grid,new Tally());
		
		ColoredGrid gridPanel = new ColoredGrid(grid);
		application.add(gridPanel, BorderLayout.CENTER);
		
		button = new JButton(REPLAY);
		button.addActionListener(new BHandler());
		application.add(button, BorderLayout.PAGE_END);
		
		application.setSize(DIM * 80, (int)(DIM * 80));
		application.setVisible(true);
		application.repaint();
		//draw the output
		animate(output);

		
	}

	
	/***
	 * animate()
	 * @param tallies
	 * @throws InterruptedException
	 * Takes a list of Tallies and draws them one at a time. 
	 */
	private static void animate(List<Tally> tallies) throws InterruptedException {
		button.setEnabled(false);
 		for (int i = 0; i < tallies.size(); i++) { 
			fillGrid(grid,tallies.get(i));
			application.repaint();
			Thread.sleep(50);
		}
		button.setEnabled(true);
		application.repaint();
	}
	
	/***
	 * Button handler is incomplete, the button should restart the animation
	 * 
	 *
	 */
	static class BHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (REPLAY.equals(e.getActionCommand())) {
				new Thread() {
			        public void run() {
			            //try {
								System.out.println("press the button");
								//animate();
							//} catch (InterruptedException e) {
							//	System.exit(0);
							//}
			        }
			    }.start();
			}
		}
	};

	//static colors
	static private final Color COLD = new Color(0x0a, 0x37, 0x66), HOT = Color.RED;

	/***
	 * fillGrid()
	 * @param grid a 2D Color Array
	 * @param tal a Tally to be drawn
	 * Converts a Tally's map of integers to a 2D array of Colors based on the integer values
	 */
	private static void fillGrid(Color[][] grid,Tally tal) {
		//convert the observations in the map to colors
		for (int r = 0; r < grid.length; r++)
			for (int c = 0; c < grid[r].length; c++) {
				//greater than or equal for possible thresholding later
				if (tal.map[r][c]>=1)
					grid[r][c]  = HOT;
				else
					grid[r][c] = COLD;

			}
	}
	


}
