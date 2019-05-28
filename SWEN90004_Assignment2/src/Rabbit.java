public class Rabbit {
	public final static int MAX_RABBIT_AGE = 10;
	public final static int MAX_HUNGRY = 5;
	public final static int MAX_MALE_RABBITS = 100;
	public final static int MAX_FEMALE_RABBITS = 100;
	public enum GENDER {MALE, FEMALE};
	protected GENDER gender;
	protected int age = 0;
	
	public Rabbit(GENDER gender) {
		this.gender = gender;
	}
	
	public void setRandomAge() {
		age = (int)(Math.random() * MAX_RABBIT_AGE);
	}
}