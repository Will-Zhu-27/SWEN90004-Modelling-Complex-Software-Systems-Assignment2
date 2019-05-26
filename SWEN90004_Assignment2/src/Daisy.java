public class Daisy {
	public enum TYPE {WHITE, BLACK};
	protected TYPE type;
	private int age;
	protected double albedo;
	
	public Daisy(TYPE type) {
		this.type = type;
		if (type == TYPE.WHITE) {
			albedo = DaisyWorld.albedoOfWhites;
		} else {
			albedo = DaisyWorld.albedoOfBlacks;
		}
		// set age random max-age
		age = (int)(Math.random() * DaisyWorld.maxDaisyAge);
	}
}