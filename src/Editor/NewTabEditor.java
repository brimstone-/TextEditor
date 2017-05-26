package Editor;

// Import all the libraries
// awt and swing are for gui, io for reading files
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;

// These libraries are for the versions of JTextArea and JScrollPane that have a bit more functionality
import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;

// This library is for the theme I use
import com.jtattoo.plaf.hifi.*;

class NewTabEditor implements Runnable {
	// Declare all variables to be used

	// Set up various containers, tabs goes in frame, the panels go in the tabs
	private JFrame frame = new JFrame("MattPad++");
	private JTabbedPane tabs = new JTabbedPane();
	private JPanel panel = new JPanel();
	private JPanel plusPanel = new JPanel();

	// Set up RSyntaxTextArea and RTextScrollPane null, as it works out better to instantiate them in the run() method
	RSyntaxTextArea text = null;
	RTextScrollPane scroll = null;
	
	// Set up menus and their items
	// Menu bar
	private JMenuBar menuBar = new JMenuBar();
	private JMenu file = new JMenu("File");
	private JMenu edit = new JMenu("Edit");
	private JMenuItem openButton = new JMenuItem("Open");
	private JMenuItem newButton = new JMenuItem("New Tab");
	private JMenuItem closeButton = new JMenuItem("Close Tab");
	private JMenuItem save = new JMenuItem("Save");
	private JMenuItem quit = new JMenuItem("Quit");
	private JMenuItem toggle = new JMenuItem("Toggle Syntax Highlighting");
	
	// Set up tool bar with a size selector
	private JToolBar tool = new JToolBar("Font Size Chooser");
	private JLabel size = new JLabel("Font Size");
	private JTextField field = new JTextField(5);
	
	// Listener for my add tab button
	private ChangeListener listener;

	// File Chooser, used to open the file choosing dialogue
	// System.getProperty("user.dir") gets the current directory the program was opened in
	private JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));

	// Set up Monospaced font because those are best
	private Font monospaced = new Font("Courier New", Font.PLAIN, 14);

	// I need to keep track of the number of tabs, mostly because of the add tab button
	private int numTabs = 0;

	// For my toggle highlighting menu item
	private boolean highlight = true;
	
	// Color gray for the background
	private Color gray = new Color(200,200,200);

	public static void main(String[] args) {
		try {
			System.setProperty("awt.useSystemAAFontSettings", "lcd"); // Anti-Alias fonts
			System.setProperty("swing.aatext", "true");
			Properties props = new Properties(); // Set the text labeling the drop down menus
			props.put("logoString", ""); // In this case, I made them empty
			HiFiLookAndFeel.setCurrentTheme(props); // Using the HiFiLookAndFeel from JTatto
			UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel"); // Found some nice themes online
			SwingUtilities.invokeLater(new NewTabEditor());  // invokeLater(new Runnable()) is nice because it lets me update the GUI
		}
		catch (Exception e) {
			e.printStackTrace();
			// If something goes wrong, tell me what and where
		}
	}

	public void run() {
		listener = new ChangeListener() {
			// ChangeListener for the adding tabs button
			public void stateChanged(ChangeEvent e) {
				addNewTab();
			}
		};

		// Time to make all my declarations actual objects
		
		// TextArea and ScrollPane that contains it
		// These are from the RSyntaxTextArea library
		text = myRSyntaxTextArea();		
		scroll = new RTextScrollPane(text); // Add text area to the scroll pane and make the scroll pane in one go
		
		// These three lines are so that changing the window size dynamically resizes everything inside too
		panel.setLayout(new BorderLayout()); // Using layouts
		panel.add(scroll, BorderLayout.CENTER); // Add the scroll pane to the panel
		panel.setBorder(BorderFactory.createEmptyBorder()); // Factories are about as annoying as Michael said they would be

		// This method is for the syntax highlighting that comes with RSyntaxTextAreas
		text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		tabs.add(panel, "Untitled", numTabs++); // Add default tab

		tabs.add(plusPanel, "+", numTabs++); // Here I add the add tab button
		tabs.addChangeListener(listener); // Add my listener
		tabs.addMouseListener(new MiddleClickListener());   // This mouse listener is for closing tabs with middle mouse click
		
		// Set mnemonics/accelerators for keyboard shortcuts, activated with CTRL+KEY
		file.setMnemonic(KeyEvent.VK_F);
		openButton.setMnemonic(KeyEvent.VK_O);
		openButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		newButton.setMnemonic(KeyEvent.VK_N);
		newButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		closeButton.setMnemonic(KeyEvent.VK_W);
		closeButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		save.setMnemonic(KeyEvent.VK_S);
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		quit.setMnemonic(KeyEvent.VK_Q);
		quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));

		edit.setMnemonic(KeyEvent.VK_E);
		toggle.setMnemonic(KeyEvent.VK_T);
		toggle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));

		// I made this method because having a billion listeners being added here took up
		// a lot of space and I got tired of scrolling past them
		addListeners();

		// Add all the file menu items to the file menu
		file.add(openButton);
		file.addSeparator();
		file.add(newButton);
		file.add(closeButton);
		file.addSeparator();
		file.add(save);
		file.addSeparator();
		file.add(quit);

		// Add all the edit menu items to the edit menu
		edit.add(toggle);

		// Icons are fun
		openButton.setIcon(new ImageIcon(getClass().getResource("images/open.gif")));
		newButton.setIcon(new ImageIcon(getClass().getResource("images/new.gif")));
		closeButton.setIcon(new ImageIcon(getClass().getResource("images/close.gif")));
		save.setIcon(new ImageIcon(getClass().getResource("images/save.gif")));
		quit.setIcon(new ImageIcon(getClass().getResource("images/quit.gif")));
		toggle.setIcon(new ImageIcon(getClass().getResource("images/toggle.gif")));

		// Add my menu(s) to the menu bar
		menuBar.add(file);
		menuBar.add(edit);

		frame.setJMenuBar(menuBar);
		
		// Add the label and text field to the tool bar		
		tool.add(size);
		tool.add(field);
		
		// Add the tool bar to the frame
		Container contentPane = frame.getContentPane();
	    	contentPane.add(tool, BorderLayout.SOUTH);

		// Good ol' GUI staples, setting default behaviors, etc.
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(tabs, BorderLayout.CENTER);
		frame.pack();
		frame.setSize(new Dimension(800, 600));
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	// This method simply creates and adds an blank tab to the editor
	private void addNewTab() {
		int index = numTabs - 1;
		if (tabs.getSelectedIndex() == index) {

			JPanel panel = newPanel();

			tabs.add(panel, "Untitled", index);
			tabs.setMnemonicAt(index, index);

			// This section is so that I can update the GUI of the already running program
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					tabs.setSelectedIndex(numTabs - 2);
				}
			});
			numTabs++;
		}
	}

	// This method reads in a file and then adds a new tab with the file in it
	private void readFile(String filePath, String fileName) {
		try {
			int index = tabs.getSelectedIndex();

			FileReader r = new FileReader(filePath); // Create a FileReader

			RSyntaxTextArea text = myRSyntaxTextArea() ; // New TextArea

			text.read(r, null); // Read the fileName into the reader object
			r.close(); // Close the FileReader

			RTextScrollPane scroll = new RTextScrollPane(text); // Add TextArea to ScrollPane

			JPanel panel = new JPanel(); // Set up the Panel to be added to the TabbedPane
			panel.setLayout(new BorderLayout());

			panel.add(scroll, BorderLayout.CENTER); // This allows the ScrollPanel/TextArea to size with the Panel
			panel.setBorder(BorderFactory.createEmptyBorder());
			
			// Weird bug where if user tries to open file when only the "+" exists, things break
			// This avoids those shenanigans, and gives me an excuse to use beep() again.
			// Better behavior is to probably just prevent the user from closing the last real tab.
			if (numTabs < 2) {
				Toolkit.getDefaultToolkit().beep(); // If no text tabs exist, yell at the user
				JOptionPane.showMessageDialog(tabs, "You need at least one tab open to open a file");
			}
			else {
				tabs.add(panel, fileName, index+1); // Add the Panel to the TabbedPanel
				numTabs++;
			
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						tabs.setSelectedIndex(index+1);
					}
				});
			}
		} catch (IOException e) {
			Toolkit.getDefaultToolkit().beep(); // If the file didn't exist, yell at the user... beep!
			JOptionPane.showMessageDialog(tabs, "Editor can't find the file called " + fileName);
		}
	}

	private void saveFile(String filePath, String fileName) { // Save is really just a save as, overwriting each time
		FileWriter w = null;
		try {
			w = new FileWriter(filePath);
			RSyntaxTextArea text = getTextArea(tabs.getSelectedIndex());
			text.write(w);
			
			int index = tabs.getSelectedIndex();

			tabs.setTitleAt(index, fileName);
		} catch (IOException e) {
			Toolkit.getDefaultToolkit().beep(); // If the file didn't exist/user has no privilege, yell at the user
			JOptionPane.showMessageDialog(tabs, "Editor can't write to the file called " + fileName + " either you don't have permission, or something else went wrong, like a velociraptor chewing on your motherboard.");
			// Tempted to use, "Check your privilege."
		} finally { // Better form to put the close() statement in a finally statement
			try {
				w.close();
			} catch (Exception ex) {
				Toolkit.getDefaultToolkit().beep(); // beep()
				JOptionPane.showMessageDialog(tabs, "FileWriter failed to close");
			}
		}
	}

	private void saveOld() { // Prompt user to save on quit
		if (JOptionPane.showConfirmDialog(tabs, "Would you like to save your work?", "Save", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				saveFile(fileChooser.getSelectedFile().getAbsolutePath(), fileChooser.getSelectedFile().getName()); // If the user quits, make sure they have saved all work
			}
		}
	}

	private RSyntaxTextArea getTextArea(int index) { // Get the text area at the index'th tab
		// This requires me to delve into each container type that each container holds, panel into scroll into port, etc
		// First time dealing with these, I had a mess of a time casting things incorrectly
		// over and over trying to figure out what the object actually was.
		// Hooray for casting though
		Container panel = (Container)tabs.getComponentAt(index);
		Container scroll = (Container)panel.getComponents()[0];
		Container port = (Container)(scroll.getComponents()[0]);

		JViewport realPort = (JViewport)port;

		RSyntaxTextArea text = (RSyntaxTextArea)realPort.getView();

		return text;
	}
	
	// Method to set up a generic JPanel with myRSyntaxTextArea(), reducing amount of code
	// I need a new JPanel with all the frills of the RSyntaxTextArea and RTextScrollPane quite often, hence this method
	private JPanel newPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		RSyntaxTextArea text = myRSyntaxTextArea();
		RTextScrollPane scroll = new RTextScrollPane(text);

		panel.add(scroll, BorderLayout.CENTER);
		panel.setBorder(BorderFactory.createEmptyBorder());
		
		return panel;
	}
	
	// Method to set up a RSyntaxTextArea with anti-aliasing, reducing amount of code
	// Both a helper method for the JPanel method, and useful in its own right
	public RSyntaxTextArea myRSyntaxTextArea() {
		RSyntaxTextArea text = new RSyntaxTextArea() {
			private static final long serialVersionUID = -1630391918477711897L; // Compiler warned me to make this
			@Override
        		public void paintComponent(Graphics g) {
                		Graphics2D graphics2d = (Graphics2D) g;
                		graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                		super.paintComponent(g);
            		}
		};
		text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA); // Set Font/Syntax
		text.setFont(monospaced);
		text.setBackground(gray);
		return text;
	}
	
	// I made this huge method just so I could put all the listeners out of the way where I didn't have to see them
	// Also made code folding nicer in my IDE
	public void addListeners() {
		// Listeners for the menu items
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					readFile(fileChooser.getSelectedFile().getAbsolutePath(), fileChooser.getSelectedFile().getName());
					// Use the file reader to get the selected file,
					// and then find it's full directory path.
					// This is then given to the readFile method.
				}
			}
		});

		// The if (numTabs > 1) else structure is to fix the bug that occurs
		// when the user makes a tab and the "+" is selected, or no tab at all
		// or worse, ruining the functionality of the "+" button entirely
		// I also had trouble with double tab creation
		// This method used to be obscenely long, but making the newPanel() method cleaned things up a bit
		newButton.addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				if (numTabs > 1) {
					int index = tabs.getSelectedIndex();
					tabs.add(newPanel(), "Untitled", index + 1);
					numTabs++;
					SwingUtilities.invokeLater(new Runnable() { // Here I update the GUI
						@Override
						public void run() {
							tabs.setSelectedIndex(index + 1);
						}
					});
				} else {
					tabs.add(newPanel(), "Untitled", 0);
					numTabs++;
					SwingUtilities.invokeLater(new Runnable() { // Here I update the GUI
						@Override
						public void run() {
							tabs.setSelectedIndex(numTabs - 2);
						}
					});
				}
			}
		});

		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = tabs.getSelectedIndex();
				tabs.remove(index);
				numTabs--;
				SwingUtilities.invokeLater(new Runnable() { // Here I update the GUI
					@Override
					public void run() { // Avoid an issue with the selected tab being the add new tab button, which is technically a tab.
						if (numTabs == 2 && index == 0) {
							tabs.setSelectedIndex(0);
						} else {
							tabs.setSelectedIndex(index - 1);
						}
					}
				});
			}
		});

		save.addActionListener(new ActionListener() {
			// My save button really just acts as a save as button,
			// and overwrites the old file every time
			public void actionPerformed(ActionEvent e) {
				if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
					saveFile(fileChooser.getSelectedFile().getAbsolutePath(), fileChooser.getSelectedFile().getName());
			}
		});

		quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveOld(); // Just in case the user forgot to save
				System.exit(0); // Close program
			}
		});

		toggle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {// Here I update the GUI, thanks invokeLater!
					@Override // I underestimated the use of @Override until now
					public void run() {
						RSyntaxTextArea text = getTextArea(tabs.getSelectedIndex());
						if (highlight) { // If highlighting is on, turn it off
							text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
							highlight = !highlight;
						} else { // Otherwise turn it on
							text.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
							highlight = !highlight;
						}
					}
				});
			}
		});
		
		field.addActionListener(new ActionListener() {
		// Get the number from the text field and change the font size
		// There needs to be done with checks with this because once you allow the user to input arbitrary values
		// Things tend to go easily wrong
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							int size = Integer.parseInt(field.getText());
								if (size > 0 && size < 1639) { // 1638 is the max font Microsoft Word 2010 allows
									RSyntaxTextArea text = getTextArea(tabs.getSelectedIndex());
									Font newSize = new Font("Courier New", Font.PLAIN, size);
									text.setFont(newSize);
									field.setText(null);
								}
								else {
									Toolkit.getDefaultToolkit().beep();
									JOptionPane.showMessageDialog(tabs, "Please enter a integer value between 1 and 1638");
									field.setText(null);
								}
						} catch (Exception e) {
							field.setText(null);
							Toolkit.getDefaultToolkit().beep();
							JOptionPane.showMessageDialog(tabs, "Please enter a integer value between 1 and 1638");
						}
					}
				});
			}
		});
	}

	public class MiddleClickListener extends MouseAdapter { // Listener for closing a tab with middle mouse click
		public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isMiddleMouseButton(e)) {
				
				int index = tabs.getSelectedIndex();
				tabs.remove(tabs.indexAtLocation(e.getX(), e.getY())); // Get the tab index at the location of the click
				numTabs--;

				SwingUtilities.invokeLater(new Runnable() { // Here I update the GUI
					@Override
					// This mess of if statements fixes the problem where having two tabs
					//open and closing the first one would make no tab be selected
					public void run() {
						if (index > 0)
							tabs.setSelectedIndex(index - 1);
						else if (numTabs > 1)
							tabs.setSelectedIndex(index);
						else
							tabs.setSelectedIndex(index - 1);
					}
				});
			}
		}
	}
}
