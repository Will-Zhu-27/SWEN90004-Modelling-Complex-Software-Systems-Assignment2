/**
 * The entrance of the simulator.
 * @author yuqiangz@student.unimelb.edu.au
 *
 */
public class Simulator {
	public static final String OUTPUT_FILE_PATH = "output.csv";
	
	/**
	 * @param args the incoming parameters
	 */
	public static void main(String[] args) {
		new DaisyWorld(args);
	}
}