public class Patch {
	protected Daisy daisy = null;
	protected double temperature = 0;
	protected double receicedDiffuse = 0;
	protected int x;
	protected int y;
	
	public Patch(int x, int y) {
		this.x = x;
		this.y = y;
	}
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
	
	/**
	 * check the survivability of the daisy in this patch
	 * @return true if it request to generate a same type daisy
	 */
	public boolean checkSurvivability() {
		if (daisy == null) {
			return false;
		}
		
		double seedThreshold = 0;
		daisy.age++;
		if(daisy.age < DaisyWorld.maxDaisyAge) {
			seedThreshold = (0.1457 * temperature) - (0.0032 * temperature * temperature) - 0.6443;
			if (Math.random() < seedThreshold) {
				return true;
			}
			return false;
		}
		// die
		else {
			daisy = null;
			return false;
		}
	}
}