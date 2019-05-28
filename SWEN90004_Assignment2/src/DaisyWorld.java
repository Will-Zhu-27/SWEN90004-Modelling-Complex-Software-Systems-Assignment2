import java.util.ArrayList;
import java.util.HashMap;



public class DaisyWorld {
	public final static double DIFFUSE_PERCENT = 50;
	public final static int X_COR = 0;
	public final static int Y_COR = 1;
	public final static int MIN_PXCOR = -14;
	public final static int MAX_PXCOR = 14;
	public final static int MIN_PYCOR = -14;
	public final static int MAX_PYCOR = 14;
	public final static int MAX_START_DAISY = 50;
	public final static int MIN_START_DAISY = 0;
	public final static double MAX_DAISY_ALBEDO = 0;
	public final static double MIN_DAISY_ALBEDO =  0.99;
	public final static double MAX_SOLAR_LUMINOSITY = 3D;
	public final static double MIN_SOLAR_LUMINOSITY = 0D;
	public final static double MAX_ALBEDO_OF_SURFACE = 1D;
	public final static double MIN_ALBEDO_OF_SURFACE = 0D;
	public final static String[] SCENARIO = {"maintain-current-luminosity", "ramp-up-ramp-down", 
		"low-solar-luminosity", "our-solar-luminosity", "high-solar-luminosity"}; 
	private String scenario = null;
	private long ticks = 0;
	private long currentTick = 0;
	private int startWhites = 20;
	public static double albedoOfWhites = 0.75D;
	private int startBlacks = 20;
	public static double albedoOfBlacks = 0.25D;
	protected static double solarLuminosity = 0.8D;
	public static double albedoOfSurface = 0.4D;
	private int numBlacks = 0;
	private int numWhites = 0;
	private double globalTemperature = 0;
	public static int maxDaisyAge = 25;
	private HashMap <String, Patch> patchGraph = new HashMap<>();
	private Output output;
	private boolean isExtended = false;
	private int numMaleRabbits = 0;
	private int numFemaleRabbits = 0;
	public DaisyWorld(String[] commands) {
		getInput(commands);
		String paramterString = getExperimentParameter();
		System.out.println(paramterString);
		output = new Output(Simulator.OUTPUT_FILE_PATH, paramterString);	
		initialize();
		runInTick();
		output.endOutput();
	}
	
	private void runInTick() {
		while(currentTick != ticks) {
			
			numBlacks = getDaisyNum(Daisy.TYPE.BLACK);
			numWhites = getDaisyNum(Daisy.TYPE.WHITE);
			String data = String.format("%12d,%22d,%22d,%17.3f,%25.1f\n", currentTick, numWhites, numBlacks, solarLuminosity, globalTemperature);
			//System.out.println(data);
			output.write(data);
			
			// ask patches [calc-temperature]
			for (int x = MIN_PXCOR, y; x <= MAX_PXCOR; x++) {
				for (y = MIN_PYCOR; y <= MAX_PYCOR; y++) {
					String coordinate = String.valueOf(x) + "," + y;
					patchGraph.get(coordinate).calTemperature();
				}
			}
			
			
			
			
			// diffuse temperature .5
			diffuseHandler();
			/* print the patch temperature distribution graph
			System.out.println("Ticks:" + currentTick + " after diffues:");
			System.out.print(getDistributionTemperatureGraph());
			*/
			
			// ask daisies [check-survivability]
			checkSurvivabilityHandler();
			// set global-temperature (mean [temperature] of patches)
			setGlobalTemperature();
			currentTick++;
			if (scenario.equals("ramp-up-ramp-down")) {
				if (currentTick > 200 && currentTick <= 400) {		
					// need to up to 4 digits after the decimal point
					String temp = String.valueOf(Math.round((solarLuminosity + 0.005) * 10000));
					solarLuminosity = Double.parseDouble(temp)/10000;
				}
				if (currentTick > 600 && currentTick <= 850) {
					// need to up to 4 digits after the decimal point
					String temp = String.valueOf(Math.round((solarLuminosity - 0.0025 ) * 10000));
					solarLuminosity = Double.parseDouble(temp)/10000;
				}
			}
			if (scenario.equals("low-solar-luminosity")) {
				solarLuminosity = 0.6;
			}
			if (scenario.equals("our-solar-luminosity")) {
				solarLuminosity = 1.0;
			}
			if (scenario.equals("high-solar-luminosity")) {
				solarLuminosity = 1.4;
			}
			
		}
	}
	
	private int getDaisyNum(Daisy.TYPE type) {
		int num = 0;
		for (int x = MIN_PXCOR, y; x <= MAX_PXCOR; x++) {
			for (y = MIN_PYCOR; y <= MAX_PYCOR; y++) {
				String coordinate = String.valueOf(x) + "," + y;
				Patch patch = patchGraph.get(coordinate);
				if (patch.daisy != null && patch.daisy.type == type) {
					num++;
				}
			}
		}
		return num;
	}
	
	private void setGlobalTemperature() {
		double totalTemperature = 0;
		for (int x = MIN_PXCOR, y; x <= MAX_PXCOR; x++) {
			for (y = MIN_PYCOR; y <= MAX_PYCOR; y++) {
				String coordinate = String.valueOf(x) + "," + y;
				totalTemperature += patchGraph.get(coordinate).temperature;
			}
		}
		int totalPatches = (MAX_PXCOR - MIN_PXCOR + 1) * (MAX_PYCOR - MIN_PYCOR + 1);
		globalTemperature = totalTemperature / totalPatches;
	}
	
	private void checkSurvivabilityHandler() {
		ArrayList<Patch> patchList = getPatchList();
		int num = patchList.size();
		for (int i = 0; i < num; i++) {
			Patch randomPatch = getRandomPatch(patchList);
			if (randomPatch == null) {
				return;
			}
			// breed a same type daisy
			if(randomPatch.checkSurvivability() == true) {
				//boolean flag = false;
				ArrayList<String> neighbourCoordinateList = new ArrayList<String>();
				String[] neighbourCoordinateSets = getNeighbours(randomPatch.x, randomPatch.y);
				for (int j = 0; j < neighbourCoordinateSets.length; j++) {
					neighbourCoordinateList.add(neighbourCoordinateSets[j]);
				}
				while(true) {
					if (neighbourCoordinateList.size() == 0) {
						break;
					}
					int index = (int) (Math.random() * neighbourCoordinateList.size());
					String neighbourCoordinate = neighbourCoordinateList.get(index);
					neighbourCoordinateList.remove(index);
					neighbourCoordinateList.trimToSize();
					Patch neighbour = patchGraph.get(neighbourCoordinate);
					if (neighbour.daisy == null) {
						neighbour.daisy = new Daisy(randomPatch.daisy.type);
						//flag = true;
						break;
					}
				}
			}	
		}
	}
	
	private void diffuseHandler() {
		for (int x = MIN_PXCOR, y; x <= MAX_PXCOR; x++) {
			for (y = MIN_PYCOR; y <= MAX_PYCOR; y++) {
				diffuseBegin(x, y);
			}
		}
		for (int x = MIN_PXCOR, y; x <= MAX_PXCOR; x++) {
			for (y = MIN_PYCOR; y <= MAX_PYCOR; y++) {
				diffuseEnd(x, y);
			}
		}
	}
	
	private void diffuseBegin(int x, int y) {
		Patch diffusingPatch = patchGraph.get(String.valueOf(x) + "," + y);
		double diffuseUnit = diffusingPatch.temperature * DIFFUSE_PERCENT / 100 / 8;
		diffusingPatch.temperature *= (100 - DIFFUSE_PERCENT) / 100;
		for(String coordinate : getNeighbours(x, y)) {
			Patch diffusedPatch = patchGraph.get(coordinate);
			diffusedPatch.receicedDiffuse += diffuseUnit;
		}
	}
	
	private void diffuseEnd(int x, int y) {
		Patch patch = patchGraph.get(String.valueOf(x) + "," + y);
		patch.temperature += patch.receicedDiffuse;
		patch.receicedDiffuse = 0;
	}
	
	private String[] getNeighbours (int x, int y) {
		String[] neighbours = new String[8];
		String upNeighbour = String.valueOf(x) + "," + (y + 1 > MAX_PYCOR ? MIN_PYCOR : y + 1);
		String downNeighbour = String.valueOf(x) + "," + (y - 1 < MIN_PYCOR ? MAX_PYCOR : y - 1);
		String leftNeighbour = String.valueOf((x - 1 < MIN_PXCOR ? MAX_PYCOR : x - 1)) + "," + y;
		String rightNeighbour = String.valueOf((x + 1 > MAX_PXCOR ? MIN_PYCOR : x + 1)) + "," + y;
		String leftUpNeighbour = String.valueOf((x - 1 < MIN_PXCOR ? MAX_PYCOR : x - 1)) + "," + (y + 1 > MAX_PYCOR ? MIN_PYCOR : y + 1);
		String leftDownNeighbour = String.valueOf((x - 1 < MIN_PXCOR ? MAX_PYCOR : x - 1)) + "," + (y - 1 < MIN_PYCOR ? MAX_PYCOR : y - 1);
		String rightUpNeighbour = String.valueOf((x + 1 > MAX_PXCOR ? MIN_PYCOR : x + 1)) + "," + (y + 1 > MAX_PYCOR ? MIN_PYCOR : y + 1);
		String rightDownNeighbour = String.valueOf((x + 1 > MAX_PXCOR ? MIN_PYCOR : x + 1)) + "," + (y - 1 < MIN_PYCOR ? MAX_PYCOR : y - 1);
		neighbours[0] = upNeighbour;
		neighbours[1] = downNeighbour;
		neighbours[2] = leftNeighbour;
		neighbours[3] = rightNeighbour;
		neighbours[4] = leftUpNeighbour;
		neighbours[5] = leftDownNeighbour;
		neighbours[6] = rightUpNeighbour;
		neighbours[7] = rightDownNeighbour;
		return neighbours;
	}
	
	private void initialize() {
		// initialize patchGraph
		for (int x = MIN_PXCOR, y; x <= MAX_PXCOR; x++) {
			for (y = MIN_PYCOR; y <= MAX_PYCOR; y++) {
				String coordinate = String.valueOf(x) + "," + y;
				Patch patch = new Patch(x, y);
				patchGraph.put(coordinate, patch);
			}
		}
		
		if (isExtended == true) {
			setRabbitRandomly(Rabbit.GENDER.MALE);
			setRabbitRandomly(Rabbit.GENDER.FEMALE);
			System.out.println(getDistributionRabbitsGraph());
		}
		
		// seed-blacks-randomly
		setDaisyRandomly(Daisy.TYPE.BLACK);
		// seed-whites-randomly
		setDaisyRandomly(Daisy.TYPE.WHITE);
		
		// ask daisies [set age random max-age]
		for (int x = MIN_PXCOR, y; x <= MAX_PXCOR; x++) {
			for (y = MIN_PYCOR; y <= MAX_PYCOR; y++) {
				String coordinate = String.valueOf(x) + "," + y;
				Patch patch = patchGraph.get(coordinate);
				if (patch.daisy != null) {
					patch.daisy.setRandomAge();
				}
			}
		}
		// print the daisy distribution graph
		//System.out.print(getDistributionGraph());
		
		
		// ask patches [calc-temperature]
		for (int x = MIN_PXCOR, y; x <= MAX_PXCOR; x++) {
			for (y = MIN_PYCOR; y <= MAX_PYCOR; y++) {
				String coordinate = String.valueOf(x) + "," + y;
				patchGraph.get(coordinate).calTemperature();
			}
		}
		
		// print the patch temperature distribution graph
		// System.out.print(getDistributionTemperatureGraph());
	}
	
	private void setRabbitRandomly(Rabbit.GENDER gender) {
		ArrayList<Patch> emptyPatchList = getEmptyRabbitPatchList();
		int totalPatch = (MAX_PXCOR - MIN_PXCOR + 1) * (MAX_PYCOR - MIN_PYCOR + 1);
		int num;
		if (gender == Rabbit.GENDER.MALE) {
			num = numMaleRabbits;
		} else {
			num = numFemaleRabbits;
		}
		for (int i = 0; i < num; i++) {
			Patch emptyPatch = getRandomPatch(emptyPatchList);
			if (emptyPatch != null) {
				emptyPatch.rabbit = new Rabbit(gender);
			} else {
				break;
			}	
		}
	}
	
	private String getDistributionGraph() {
		String ret = "";
		for (int y = MAX_PYCOR, x; y >= MIN_PYCOR; y--) {
			for (x = MIN_PXCOR; x <= MAX_PXCOR; x++) {
				String coordinate = String.valueOf(x) + "," + y;
				Patch patch = patchGraph.get(coordinate);
				if (patch.daisy == null) {
					ret = ret + " ";
				} else if (patch.daisy.type == Daisy.TYPE.BLACK) {
					ret = ret + "B";
				} else {
					ret = ret + "W";
				}
				if (x == MAX_PXCOR) {
					ret = ret + "\n";
				}
			}
		}
		return ret;
	}
	
	private String getDistributionRabbitsGraph() {
		String ret = "";
		for (int y = MAX_PYCOR, x; y >= MIN_PYCOR; y--) {
			for (x = MIN_PXCOR; x <= MAX_PXCOR; x++) {
				String coordinate = String.valueOf(x) + "," + y;
				Patch patch = patchGraph.get(coordinate);
				if (patch.rabbit == null) {
					ret = ret + " ";
				} else if (patch.rabbit.gender == Rabbit.GENDER.MALE) {
					ret = ret + "M";
				} else {
					ret = ret + "F";
				}
				if (x == MAX_PXCOR) {
					ret = ret + "\n";
				}
			}
		}
		return ret;
	}
	
	private String getDistributionTemperatureGraph() {
		String ret = "";
		for (int y = MAX_PYCOR, x; y >= MIN_PYCOR; y--) {
			for (x = MIN_PXCOR; x <= MAX_PXCOR; x++) {
				String coordinate = String.valueOf(x) + "," + y;
				Patch patch = patchGraph.get(coordinate);
				ret = ret + patch.temperature;
				if (x == MAX_PXCOR) {
					ret = ret + "\n";
				}
			}
		}
		return ret;	
	}
	
	private void setDaisyRandomly(Daisy.TYPE type) {
		ArrayList<Patch> emptyPatchList = getEmptyPatchList();
		int totalPatch = (MAX_PXCOR - MIN_PXCOR + 1) * (MAX_PYCOR - MIN_PYCOR + 1);
		int num;
		if (type == Daisy.TYPE.BLACK) {
			num = Math.round((float)totalPatch * startBlacks / 100);
			System.out.println("Black num is " + num);
		} else {
			num = Math.round((float)totalPatch * startWhites / 100);
			System.out.println("White num is " + num);
		}
		for (int i = 0; i < num; i++) {
			Patch emptyPatch = getRandomPatch(emptyPatchList);
			if (emptyPatch != null) {
				emptyPatch.daisy = new Daisy(type);
			} else {
				break;
			}	
		}
	}
	
	/**
	 * get patch in random and remove it from patchList 
	 */
	private Patch getRandomPatch(ArrayList<Patch> patchList) {
		int listLength = patchList.size();
		if (listLength == 0) {
			return null;
		}
		int index = (int) (Math.random() * listLength);
		//System.out.println("the length: " + listLength + " get index: " + index);
		Patch ret = patchList.get(index);
		patchList.remove(index);
		patchList.trimToSize();
		return ret;
	}
	
	private ArrayList<Patch> getEmptyPatchList() {
		ArrayList<Patch> emptyPatchList = new ArrayList<Patch>();
		for (int x = MIN_PXCOR, y; x <= MAX_PXCOR; x++) {
			for (y = MIN_PYCOR; y <= MAX_PYCOR; y++) {
				String coordinate = String.valueOf(x) + "," + y;
				Patch patch = patchGraph.get(coordinate);
				if (patch.daisy == null) {
					emptyPatchList.add(patch);
				}
			}
		}
		return emptyPatchList;
	}
	
	private ArrayList<Patch> getEmptyRabbitPatchList() {
		ArrayList<Patch> emptyPatchList = new ArrayList<Patch>();
		for (int x = MIN_PXCOR, y; x <= MAX_PXCOR; x++) {
			for (y = MIN_PYCOR; y <= MAX_PYCOR; y++) {
				String coordinate = String.valueOf(x) + "," + y;
				Patch patch = patchGraph.get(coordinate);
				if (patch.rabbit == null) {
					emptyPatchList.add(patch);
				}
			}
		}
		return emptyPatchList;
	}
	
	private ArrayList<Patch> getPatchList() {
		ArrayList<Patch> patchList = new ArrayList<Patch>();
		for (int x = MIN_PXCOR, y; x <= MAX_PXCOR; x++) {
			for (y = MIN_PYCOR; y <= MAX_PYCOR; y++) {
				String coordinate = String.valueOf(x) + "," + y;
				Patch patch = patchGraph.get(coordinate);
				patchList.add(patch);
			}
		}
		return patchList;
	}
	
	private void getInput(String[] commands) {
		for(int i = 0; i < commands.length; i++) {
			switch (commands[i]) {
				case "-ticks": {
					try {
						ticks = Long.parseLong(commands[++i]);
						if (ticks < 0) {
							Exception e = new Exception();
							throw e;
						}
					} catch (Exception e) {
						System.err.println("Wrong format! \"-ticks\" should be followed" + 
							" an integer more than 0.");
						System.exit(1);
					}
					break;
				}
				case "-start-%-whites": {
					try {
						startWhites = Integer.parseInt(commands[++i]);
						if (!(startWhites >= MIN_START_DAISY && startWhites <= MAX_START_DAISY)) {
							Exception e = new Exception();
							throw e;
						}
					} catch (Exception e) {
						System.err.println("Wrong format! \"-start-%-whites\"" +
							" should be followed an integer from 0 to 50.");
						System.exit(1);
					}
					break;
				}
				case "-albedo-of-whites": {
					try {
						int temp = (int) (Float.parseFloat(commands[++i]) * 100);
						albedoOfWhites = (double)temp/100;
						if (!(albedoOfWhites >= 0 && albedoOfWhites <= 0.99)) {
							Exception e = new Exception();
							throw e;
						}
					} catch (Exception e) {
						System.err.println("Wrong format! \"-albedo-of-whites\"" +
								" should be followed a decimal from 0 to 0.99.");
						System.exit(1);
					}
					break;
				}
				case "-start-%-blacks": {
					try {
						startBlacks = Integer.parseInt(commands[++i]);
						if (!(startBlacks >= MIN_START_DAISY && startBlacks <= MAX_START_DAISY)) {
							Exception e = new Exception();
							throw e;
						}
					} catch (Exception e) {
						System.err.println("Wrong format! \"-start-%-blacks\"" + 
							" should be followed an integer from 0 to 50.");
						System.exit(1);
					}
					break;
				}
				case "-albedo-of-blacks": {
					try {
						int temp = (int) (Float.parseFloat(commands[++i]) * 100);
						albedoOfBlacks = (double)temp/100;
						if (!(albedoOfBlacks >= 0 && albedoOfBlacks <= 0.99)) {
							Exception e = new Exception();
							throw e;
						}
					} catch (Exception e) {
						System.err.println("Wrong format! \"-albedo-of-blacks\"" +
								" should be followed by a decimal from 0 to 0.99.");
						System.exit(1);
					}
					break;
				}
				case "-scenario": {
					try {
						boolean flag = false;
						for (String scenario : SCENARIO) {
							if (scenario.equals(commands[i + 1])) {
								flag = true;
								this.scenario = commands[++i];
								break;
							}
						}
						if (flag == false) {
							Exception e = new Exception();
							throw e;
						}
					} catch (Exception e) {
						System.err.print("Wrong format! \"-scenario\"" +
								" should be followed by one of these command:");
						for(int j = 0; j < SCENARIO.length; j++) {
							System.err.print("\"" + SCENARIO[j] + "\"");
							if (j != SCENARIO.length - 1) {
								System.err.print(", ");
							} else {
								System.err.println(".");
							}
						}
						System.exit(1);
					}
					break;
				}
				case "-solar-luminosity": {
					try {
						int temp = (int) (Float.parseFloat(commands[++i]) * 1000);
						solarLuminosity = (double)temp/1000;
						if (!(solarLuminosity >= MIN_SOLAR_LUMINOSITY && 
							solarLuminosity <= MAX_SOLAR_LUMINOSITY)) {
							Exception e = new Exception();
							throw e;
						}
					} catch (Exception e) {
						System.err.println("Wrong format! \"-solar-luminosity\"" +
								" should be followed by a decimal from 0 to 3.");
						System.exit(1);
					}
					break;
				}
				case "-albedo-of-surface": {
					try {
						int temp = (int) (Float.parseFloat(commands[++i]) * 100);
						albedoOfSurface = (double)temp/100;
						if (!(albedoOfSurface >= MIN_ALBEDO_OF_SURFACE && 
							albedoOfSurface <= MAX_ALBEDO_OF_SURFACE)) {
							Exception e = new Exception();
							throw e;
						}
					} catch (Exception e) {
						System.err.println("Wrong format! \"-albedo-of-surface\"" +
								" should be followed by a decimal from 0 to 1.");
						System.exit(1);
					}
					break;
				}
				case "-start-male-rabbits": {
					isExtended = true;
					try {
						numMaleRabbits = Integer.parseInt(commands[++i]);
						if (numMaleRabbits >  Rabbit.MAX_MALE_RABBITS || numMaleRabbits < 0) {
							Exception e = new Exception();
							throw e;
						}
					} catch (Exception e) {
						System.err.println("Wrong format! \"-start-male-rabbits\"" + 
							" cannot more than " + Rabbit.MAX_MALE_RABBITS);
						System.exit(1);
					}
					break;
				}
				case "-start-female-rabbits": {
					isExtended = true;
					try {
						numFemaleRabbits = Integer.parseInt(commands[++i]);
						if (numFemaleRabbits >  Rabbit.MAX_MALE_RABBITS || numFemaleRabbits < 0) {
							Exception e = new Exception();
							throw e;
						}
					} catch (Exception e) {
						System.err.println("Wrong format! \"-start-female-rabbits\"" + 
							" cannot more than " + Rabbit.MAX_FEMALE_RABBITS);
						System.exit(1);
					}
					break;
				}
				default: {
					break;
				}
			}
		}
		
		if (scenario != null) {
			if (scenario.equals(SCENARIO[1])) {
				solarLuminosity =0.8D;
			} else if (scenario.equals(SCENARIO[2])) {
				solarLuminosity =0.6D;
			} else if (scenario.equals(SCENARIO[3])) {
				solarLuminosity = 1D;
			} else if (scenario.equals(SCENARIO[4])) {
				solarLuminosity = 1.4D;
			}
		} else {
			scenario = SCENARIO[0];
		}
		
		// at least need to spicify ticks
		if (ticks == 0) {
			System.err.println("Need \"-ticks\" to spicify ticks.");
				System.exit(1);
		}
	}
	
	public String getExperimentParameter() {
		String ret = null;
		ret = "Experiment Parameter is: ";
		ret = ret + "scenario:" + scenario;
		ret = ret + ", solar-luminosity = " + solarLuminosity;
		ret = ret + ", albedo-of-surface = " + albedoOfSurface;
		ret = ret + ", start-%-whites = " + startWhites;
		ret = ret + ", albedo-of-whites = " + albedoOfWhites;
		ret = ret + ", start-%-blacks = " + startBlacks;
		ret = ret + ", albedo-of-blacks = " + albedoOfBlacks;
		ret = ret + ", total ticks = " + ticks;
		if (isExtended == true) {
			ret = ret + ", start-male-rabbits = " + numMaleRabbits;
			ret = ret + ", start-female-rabbits = " + numFemaleRabbits;
		}
		ret = ret + ".\n";
		return ret;
	}
		
}