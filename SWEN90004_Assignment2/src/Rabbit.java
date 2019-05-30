/**
 * Represent Rabbit.
 * @author yuqiangz@student.unimelb.edu.au
 *
 */
public class Rabbit {
	public final static int MAX_RABBIT_AGE = 12;
	public final static int MAX_HUNGRY = 20;
	public final static int MAX_MALE_RABBITS = 400;
	public final static int MAX_FEMALE_RABBITS = 400;
	public final static int MAX_BREED_TEMPERATURE = 35;
	public final static int MIN_BREED_TEMPERATURE = 15;
	public enum GENDER {MALE, FEMALE};
	
	protected GENDER gender;
	protected int age = 0;
	protected int hungry = 0;
	
	public Rabbit(GENDER gender) {
		this.gender = gender;
	}
	
	public void setRandomAge() {
		age = (int)(Math.random() * MAX_RABBIT_AGE);
	}
	
	public void setRandomHungry() {
		hungry = (int)(Math.random() * MAX_HUNGRY);
	}
}