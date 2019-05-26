public class Patch {
	protected Daisy daisy = null;
	protected double temperature = 0;
	protected double receicedDiffuse = 0;
	
	public void calTemperature() {
		double absorbedLuminosity = 0;
		double localHeating = 0;
		if (daisy == null) {
			absorbedLuminosity = (1 - DaisyWorld.albedoOfSurface) * DaisyWorld.solarLuminosity;
		} else {
			absorbedLuminosity = (1 - daisy.albedo) * DaisyWorld.solarLuminosity;
		}
		if( absorbedLuminosity > 0) {
			localHeating = 72 * Math.log(absorbedLuminosity) + 80;
		} else {
			localHeating = 80;
		}
		temperature = (temperature + localHeating) / 2;
	}
}