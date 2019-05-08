import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.nio.charset.*;
import java.nio.file.*;

/*
 * All logic and related code for the antivirus program.
 * Does exactly as the lab asks.
 */
public class Antivirus {
	
	private final String MESSAGE = "Number scanned: ";
	private final String VIRUS_MESSAGE = ". Total Viruses: ";
	private final String QUARANTINE_LOCATION = "\\Quarantine";
	private final String VIRUS_FOUND = " is a virus. Taking appropriate action...\n";
	private final String INOCULATION = "The invalid bytes have been cleaned.\n";
	private final String QUARANTINED = "The file has been quarantined.\n";
	
	private JFrame frame;
	private JPanel scanPanel;
	private JPanel definitionPanel;
	private JButton scan;
	private JButton getDefinitions;
	private JTextField input;
	private JTextField definitionLocation;
	private JLabel numberScanned;
	private JTextArea textArea;
	private JScrollPane scrollPane;
	private List<String> definitions;
	private String quarantinePath; // Each scan quarantines to the root folder
	private int scanned;
	private int virusCount;

	/*
	 * Sets up the GUI, action listeners, and all related variables
	 */
	public Antivirus() {
		definitions = new ArrayList<String>();
		
		frame = new JFrame("CSI 4139 - Lab 3 Antivirus");
		frame.setVisible(true);
		// Grid layout code inspired from:
		// https://stackoverflow.com/questions/9674774/stack-swing-elements-from-top-to-bottom
		frame.getContentPane().setLayout(new GridBagLayout());
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridy = 0;
		constraints.gridx = 0;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.ipady = 10;
		
		scanPanel = new JPanel();
		definitionPanel = new JPanel();
		
		input = new JTextField(75);
		input.setToolTipText("Insert directory to scan");
		definitionLocation = new JTextField(75);
		definitionLocation.setToolTipText("Insert virus definition file");
		
		numberScanned = new JLabel(MESSAGE + "0" + VIRUS_MESSAGE + "0");
		
		scan = new JButton("Scan This Directory");
		scan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startSearch();
			}
		});
		
		getDefinitions = new JButton("Get the Definitions");
		getDefinitions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Get the definitions
				getVirusDefinitions();
			}
		});
		
		textArea = new JTextArea(20, 80);
		scrollPane = new JScrollPane(textArea);
		textArea.setEditable(false);
		scanPanel.add(input);
		scanPanel.add(scan);
		scanPanel.add(scrollPane);
		definitionPanel.add(definitionLocation);
		definitionPanel.add(getDefinitions);
	    frame.getContentPane().add(numberScanned, constraints);
	    constraints.gridy = 1;
	    frame.getContentPane().add(definitionPanel, constraints);
	    constraints.gridy = 2;
	    frame.getContentPane().add(scanPanel, constraints);
		frame.pack();
	}
	
	/*
	 * Read all of the virus definitions from a provided .txt file
	 * The definitions are expected to be formatted such that they are separated by a line
	 */
	public void getVirusDefinitions() {
		String definitionLocation = this.definitionLocation.getText();
		definitions = new ArrayList<String>();
		try {
			FileReader fileReader = new FileReader(definitionLocation);
			try (BufferedReader reader = new BufferedReader(fileReader)) {
			    String signature = "";
			    while ((signature = reader.readLine()) != null) {
			       definitions.add(signature);
			       System.out.println(signature); // For testing purposes
			    }
			}
			textArea.append("Virus definitions have been updated\n.");
		} catch (Exception e) { textArea.append("Unable to read signatures from file.\n"); }
	}
	
	/*
	 * Begins the scanning of the inputed directory.
	 * Assumes correct input from the user.
	 */
	public void startSearch() {
		String path = input.getText();
		File currentDirectory = new File(path);
		quarantinePath = path + QUARANTINE_LOCATION;
		createDirectory(quarantinePath);
		scanned = 0;
		virusCount = 0;
		textArea.setText(null);;
		textArea.append("Starting Scan of provided directory.\n");
		scanDirectory(currentDirectory);
		textArea.append("Scan complete.\n");
	}

	/*
	 * Create a directory if it doesn't already exist.
	 * If it already exists, all that will happen is that files in it will be scanned during
	 * the scan, which is fine since it will check if a new virus definition is found in a quarantined file.
	 * 
	 * Plus in a real antivirus, the quarantined data would be removed and the folder would be in a centralized
	 * location (only created once).
	 */
	public Path createDirectory(String directory) {
		Path path = Paths.get(directory);
		try {
			Files.createDirectory(path);
			textArea.append("Created a new quarantined directory... " + path);
		} catch (Exception e) { } // We don't need to care if it already exists

		return path;
	}

	/*
	 * Recursively go through each directory and handle the antivirus process
	 * for files.
	 * 
	 * Files are scanned for signature bits, cleansed if one is detected, and quarantined if a virus.
	 * Counts and UI updates occur along the way.
	 */
	public void scanDirectory(File dir) {
		// Recursive definition inspired from:
		// https://www.geeksforgeeks.org/java-program-list-files-directory-nested-sub-directories-recursive-approach/
		textArea.setCaretPosition(textArea.getDocument().getLength());
		try {
			File[] files = dir.listFiles();
			for (File file : files) {
				if (file.isDirectory()) { // if sub directory: enter and repeat
					textArea.append("Reached directory: " + file.getCanonicalPath() + "\n");
					scanDirectory(file);
				} else { // found file
					textArea.append("Checking file: " + file.getCanonicalPath() + "\n");
					
					// convert file to byte array
					byte[] fileBytes = Files.readAllBytes(file.toPath());
					
					// convert byte array to string
					String byteString = new String(fileBytes);
					textArea.append("File Contents: " + byteString + "\n");

					//Go through every signature in our definition file
					for (int i = 0; i < definitions.size(); i++) {
						String currentDefinition = definitions.get(i);
						if (byteString.contains(currentDefinition)) { // check if string of file contains virus signature
							textArea.append(file.getCanonicalPath() + VIRUS_FOUND);
							// replace with iteration through file of signature lists
							int location = byteString.indexOf(currentDefinition); // get location of found string
							// replace first 8 characters with 'x' at start of found string
							for (int j = 0; j < 8; j++) { 
								byteString = byteString.substring(0, location + j) + 'x'
										+ byteString.substring(location + j + 1);
							}
							//System.out.println(byteString);
							byte[] newBytesForFile = byteString.getBytes(Charset.forName("UTF-8")); // convert back to byte
																									// array
							java.nio.file.Files.write(file.toPath(), newBytesForFile); // write byte array back to file
							textArea.append(INOCULATION);
							textArea.append(QUARANTINED);
							// Move the file to the quarantined folder (quarantines the file
							Files.move(Paths.get(file.getCanonicalPath()),
									   Paths.get(quarantinePath + file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("\\"))),
									   StandardCopyOption.REPLACE_EXISTING);
							virusCount++;
							break;
						}
					}
					scanned++;
					numberScanned.setText(MESSAGE + Integer.toString(scanned) + VIRUS_MESSAGE + Integer.toString(virusCount));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
