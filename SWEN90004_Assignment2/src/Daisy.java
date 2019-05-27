public class Daisy {
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
		// set age random max-age
		//age = (int)(Math.random() * DaisyWorld.maxDaisyAge);
	}
	
	public void setRandomAge() {
		age = (int)(Math.random() * DaisyWorld.maxDaisyAge);
	}
}