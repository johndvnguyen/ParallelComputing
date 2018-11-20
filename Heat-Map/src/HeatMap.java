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
		public double x;
		public double y;
		public List<Observation> history;
		
		public Tally() {
			x = 0.0;
			y = 0.0;
			history = new ArrayList<Observation>();
		}
		
		public Tally(Observation o) {
			this.x = o.x;
			this.y = o.y;
			this.time = o.time;
			history = new ArrayList<Observation>();
			history.add(o);
		}
		
		public static Tally combine(Tally a, Tally b) {
			Tally result = new Tally();
			for(int i = 0; i < a.history.size(); i++)
				result.history.add(a.history.get(i));
				
			for(int i = 0; i < b.history.size(); i++)
				result.history.add(b.history.get(i));
			
			result.x = a.x +b.x;
			result.y = a.y +b.y;
			System.out.println("result history size: " + result.history.size());
			return result;
			
			
		}
		
		public void accum(Observation datum) {
			this.x += datum.x;
			this.y += datum.y;
			this.history.add(datum);
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
				final double EPSILON = 1e-3;
		
				
				//Compute and print out sum		
				pScan.reduce(0);
				System.out.println("Reduction x: " + pScan.interior.get(0).x);
				System.out.println("Reduction y: " + pScan.interior.get(0).y);
				
				//create arraylist for storing scan result
				List<Tally> output = pScan.getScan();
				

				//call the scan function
				
				double check = 0.0;
				for (int i = 0; i < num_obs; i++) {
					if (i < 10)
						System.out.printf("+ %8.2f, %8.2f = %8.2f, %8.2f%n", observations.get(i).x,observations.get(i).y, output.get(i).x,output.get(i).y);
					
					else if (i == num_obs - 1 || i % 10000 == 0)
						System.out.printf("...(%d)%n+ %8.2f, %8.2f  = %8.2f, %8.2f%n", i, observations.get(i).x,observations.get(i).y, output.get(i).x,output.get(i).y);
				}
				
				
				
				
				//ColorGrid stuff
				grid = new Color[DIM][DIM];
				application = new JFrame();
				application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				fillGrid(grid,output.get(0));
				
				ColoredGrid gridPanel = new ColoredGrid(grid);
				application.add(gridPanel, BorderLayout.CENTER);
				
				button = new JButton(REPLAY);
				button.addActionListener(new BHandler());
				application.add(button, BorderLayout.PAGE_END);
				
				application.setSize(DIM * 4, (int)(DIM * 4.4));
				application.setVisible(true);
				application.repaint();
				//animate();
		
		
	}
	private static final int DIM = 200;
	private static final String REPLAY = "Replay";
	private static JFrame application;
	private static JButton button;
	private static Color[][] grid;
	
	private static void animate() throws InterruptedException {
		button.setEnabled(false);
 		for (int i = 0; i < DIM; i++) { 
			fillGrid(grid);
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
			            try {
								animate();
							} catch (InterruptedException e) {
								System.exit(0);
							}
			        }
			    }.start();
			}
		}
	};

	static private final Color COLD = new Color(0x0a, 0x37, 0x66), HOT = Color.YELLOW;
	static private int offset = 0;
	
//	private static void fillGrid(Color[][] grid, ArrayList<Tally> output) {
//		int pixels = grid.length * grid[0].length;
//			for (int c = 0; c < output.size(); c++) {
//				Tally tal = output.get(c);
//				System.out.println("grid " + ((tal.x*10)+100)+ ", " + ((tal.y*10)+100));
//				grid[(int) ((tal.y*10)+100)][(int) (tal.x*10) +100] = interpolateColor((offset)%pixels / (double)pixels, COLD, HOT);
//			}
//			offset += DIM;
//	}

	
	private static void fillGrid(Color[][] grid,Tally tal) {
		int pixels = grid.length * grid[0].length;
		//convert tally floats to grid space
		//5X+5
		tal.x = (5*tal.x)+5;
		tal.y +=(5*tal.y)+5;
		for (int r = 0; r < grid.length; r++)
			for (int c = 0; c < grid[r].length; c++) {
				if (Math.abs(r-tal.y) < 10 && Math.abs(c-tal.x) <10)
					grid[r][c]  = HOT;
				else
					grid[r][c] = COLD;
				//grid[r][c] = interpolateColor((r*c+offset)%pixels / (double)pixels, COLD, HOT);
			}
		offset += DIM;
	}
	
//	private static Color interpolateColor(double ratio, Color a, Color b) {
//		int ax = a.getRed();
//		int ay = a.getGreen();
//		int az = a.getBlue();
//		int cx = ax + (int) ((b.getRed() - ax) * ratio);
//		int cy = ay + (int) ((b.getGreen() - ay) * ratio);
//		int cz = az + (int) ((b.getBlue() - az) * ratio);
//		return new Color(cx, cy, cz);
//	}

}
