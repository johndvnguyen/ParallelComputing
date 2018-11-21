import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;



public class HeatMap{
	

	//Static Class Tally to hold Observation Data
	public static class Tally{
		public double time;
		public int[][] map;
		public List<int[][]> history;

		
		public Tally() {
			//default size is 25 sections.
			map = new int[11][11];
			history = new ArrayList<int[][]>();
		}
		
		public Tally(Observation o) {

			map = new int[11][11];
			history = new ArrayList<int[][]>();
			accum(o);
		}
		
		public static Tally combine(Tally a, Tally b) {
			Tally result = new Tally();
			//System.out.println("combined A into the result, now combining B");
		
			for(int j = 0; j<11; j++) {
				for(int k = 0; k<11; k++) {
					//only add to current records do not take away
					//System.out.println("i " + i +" j " + j + " k " + k);
					
					result.map[j][k] = a.map[j][k] + b.map[j][k];
				}
			}

			//add the set of maps
			//System.out.println("result history size: " + result.history.size());
			return result;
			
			
		}
		
		//add the observation to the current HeatMap
		public void accum(Observation datum) {

			//change the observation coordinates (-1.0 to 1.0) into matrix coordinates (0 to 10) 
			int x = (int)((5*datum.x)+5);
			int y = (int)((5*datum.y)+5);
			//
			map[x][y] += 1;
			//add the new map to the history for scan purposes. 
			//history.add(map.clone());
		}
		
		
	}
	
	
	//Scan and Reduce Class
	public static class HeatScan extends GeneralScan3<Observation,Tally>{
		
	
		public HeatScan(List<Observation> raw, int threshold) {
			super(raw, threshold);
			// TODO Auto-generated constructor stub
		}
		@Override
		protected Tally init() {
			return new Tally();
		}
	
		@Override
		protected Tally prepare(Observation datum) {
			return new Tally(datum);
		}
	
		@Override
		protected Tally combine(Tally left, Tally right) {
			return Tally.combine(left, right);
		}
	
		@Override
		protected void accum(Tally tally, Observation datum) {
			tally.accum(datum);
		}
	

	}
	
	public static void main(String[] args) throws InterruptedException {
		//Read in File data (observation_test.dat)
		final String FILENAME = "observation_test.dat";
		ArrayList<Observation> observations = new ArrayList<Observation>(400);
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILENAME));
			int count = 0;

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
		int num_obs = observations.size();
		
		//Using the list of observations and count, we can make a 2Dimensional Array

		
		//Create the prefix scan object with a threshold
				HeatScan pScan = new HeatMap.HeatScan(observations, 50);
				
				//Compute and print out sum		
				pScan.reduce(0);

				
				//create arraylist for storing scan result
				List<Tally> output = pScan.getScan();
				

				//call the scan function
				
				
				
				
				//ColorGrid stuff
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
				animate(output);
		
		
	}
	private static final int DIM = 11;
	private static final String REPLAY = "Replay";
	private static JFrame application;
	private static JButton button;
	private static Color[][] grid;
	
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

	static private final Color COLD = new Color(0x0a, 0x37, 0x66), HOT = Color.RED;
	static private int offset = 0;

	
	private static void fillGrid(Color[][] grid,Tally tal) {
		int pixels = grid.length * grid[0].length;

		//convert the 1's to HOT
		for (int r = 0; r < grid.length; r++)
			for (int c = 0; c < grid[r].length; c++) {
				//greater than or equal for possible thresholding
				if (tal.map[r][c]>=1)
					grid[r][c]  = HOT;
				else
					grid[r][c] = COLD;

			}
	}
	


}
