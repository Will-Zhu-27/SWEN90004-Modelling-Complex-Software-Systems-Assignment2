import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Output {
	public static final String HEADER = 
		"tick,num-white-daisy,num-black-daisy,luminosity,global-temperature\r\n";
	public static final String HEADER_EXTENDED = 
		"tick,num-white-daisy,num-black-daisy,luminosity,global-temperature,num-male-rabbits" +
		",num-female-rabbits\r\n";
	private String fileName;
	private FileWriter fileWritter;
	
	public Output(String fileName, String ParametersInfo, String header) {
		this.fileName = fileName;
		File file = new File(fileName);
		try {
			file = new File(fileName);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			// replace the origin content
			fileWritter = new FileWriter(file.getName(), false);
			write(ParametersInfo);
			write(header);
			
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error in outputing the result.");
			System.exit(1);
		}
	}
	
	/**
	 * write data into given file.
	 * @param data It'd better end with "\n"
	 */
	public void write(String data) {
		try {
			fileWritter.write(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("Error in writing data.\n");
			System.exit(1);
		}
	}
	
	public void endOutput() {
		try {
			fileWritter.close();
			System.out.println("Successfully get a new " + fileName + ".");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("Error in closing file.\n");
			System.exit(1);
		}
	}
}