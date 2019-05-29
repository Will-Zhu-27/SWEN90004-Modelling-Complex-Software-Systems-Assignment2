/**
 * Represent Daisy
 * @author yuqiangz@student.unimelb.edu.au
 *
 */
public class Daisy {
	public final static int MAX_START_DAISY = 50;
	public final static int MIN_START_DAISY = 0;
	public final static double MAX_DAISY_ALBEDO = 0.99D;
	public final static double MIN_DAISY_ALBEDO =  0;
	public final static int MAX_DAISY_AGE = 25;
	public enum TYPE {WHITE, BLACK};
	
	protected TYPE type;
	protected int age;
	protected double albedo;
	
	public Daisy(TYPE type) {
		this.type = type;
		if (type == TYPE.WHITE) {
			albedo = DaisyWorld.albedoOfWhites;
		} else {
			albedo = DaisyWorld.albedoOfBlacks;
		}
		age = 0;
	}
	
	public void setRandomAge() {
		age = (int)(Math.random() * MAX_DAISY_AGE);
	}
}