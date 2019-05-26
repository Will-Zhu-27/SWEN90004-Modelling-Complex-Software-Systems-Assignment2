public class Daisy {
	public enum TYPE {WHITE, BLACK};
	protected TYPE type;
	private int age;
	private float albedo;
	
	public Daisy(TYPE type) {
		this.type = type;
		if (type == TYPE.WHITE) {
			albedo = DaisyWorld.albedoOfWhites;
		} else {
			albedo = DaisyWorld.albedoOfBlacks;
		}
		age = (int)(Math.random() * DaisyWorld.maxDaisyAge);
	}
}