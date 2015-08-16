/**
 * CSCI E-10b - Final Project
 * Johanna Bodnyk | May 9, 2014
 *
 * This program translates knitting instructions written with standard stitch abbreviations
 * and syntax into corresponding charts of knitting stitch symbols.
 *
 * Users may create instructions from scratch within the GUI, or import text files to render.
 *
 */

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.*;

class KnittingEditor extends JFrame
{
    private JTextArea instructionsPane = new JTextArea(""); // Text area for written instructions
    private JPanel chart; // Panel for display of rendered knitting chart
    private File currentFile = null; // File object representing imported file, or last saved file, used by save method
    private JFrame help; // Secondary JFrame to display help text
    private final String HELP_TEXT = "<html>In order for your knitting instructions to be properly rendered into a chart, please observe the following guidelines:<br><br><ul><li>All rows must contain the same number of stitches<br><br></li><li>Each abbrevation should be followed by a comma and one space<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;E.g. k, p, ktog<br><br></li><li>Multiple knit or purl stitches may be denoted by K or P followed by a number<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;E.g. k6, p2<br><br></li><li>Repeated sequences may be enclosed by parentheses, brackets, or asterisks, followed by an indication of the number of times the sequence is to be repeated. Be sure the preceding abbrevation is followed by a comma.<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;E.g. k2, (yo, k2tog) 3 times, k2<br><br></li><li>The following abbrevations are currently recognized (case insensitive):<br><br><table><tr><th>STITCH NAME</th><th>ABBREVIATION</th><th>CHART SYMBOL</th></tr><tr><td>knit</td><td>k</td><td>[blank]</td></tr><tr><td>purl</td><td>p</td><td>*</td></tr><tr><td>yarn over</td><td>yo</td><td>O</td></tr><tr><td>knit 2 together</td><td>k2tog</td><td>/</td></tr><tr><td>slip, knit, pass</td><td>skp</td><td>\\</td></tr></table><br>(For quick reference you can mouse over a symbol in the rendered chart to view the name of the stitch it represents.)</li></ul></html>";

    public KnittingEditor()
    {
        setTitle("Knitting Pattern Editor");
        setSize(700,750);
        setLocation(500,150);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        //
        // Instruction display/editing area and chart display area
        //
        JPanel panes = new JPanel(new GridLayout(2,1,0,0));

        JPanel chartArea = new JPanel(new BorderLayout());
        JLabel chartLabel = new JLabel("Knitting Chart");
        chartLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 0));
        chartArea.add(chartLabel, BorderLayout.NORTH);
        chart = new JPanel();
        chart.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,20,20,20), BorderFactory.createLineBorder(Color.BLACK,1)));
        chartArea.add(chart, BorderLayout.CENTER);
        panes.add(chartArea);

        JPanel instructionsArea = new JPanel(new BorderLayout());
        JLabel instructionsLabel = new JLabel("Knitting Instructions");
        instructionsLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 0));
        instructionsLabel.setOpaque(true);
        instructionsLabel.setBackground(Color.WHITE);
        instructionsArea.add(instructionsLabel, BorderLayout.NORTH);
        instructionsPane = new JTextArea("");
        instructionsPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,20,20,20), BorderFactory.createLineBorder(Color.BLACK,1)));
        instructionsArea.add(instructionsPane, BorderLayout.CENTER);
        panes.add(instructionsArea);

        add(panes, BorderLayout.CENTER);

        //
        // Buttons and button event listeners 
        //
        JPanel buttons = new JPanel();
        buttons.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        // Import File Button
        JButton importFileButton = new JButton("Import");
        importFileButton.addActionListener(
            new ActionListener() 
            {
                public void actionPerformed(ActionEvent e) {

                    // Unless instructions pane is empty, ask if user wants 
                    // to save current work before importing
                    if (instructionsPane.getText().length() > 0)
                    {
                        int choice = JOptionPane.showConfirmDialog(null, "Have you saved your work?\nUnsaved changes to your current pattern will be lost if you import a new file.\nClick YES to continue import, or NO to stop and save your current pattern first.", "Have you saved your work?", JOptionPane.YES_NO_CANCEL_OPTION);
                        if (choice == JOptionPane.YES_OPTION)
                        {
                            importFile();
                        }
                        if (choice == JOptionPane.NO_OPTION)
                        {
                            saveAs();
                            importFile();
                        }
                    }
                    else importFile();
                }
            });
        
        // Save Button -- calls method to save text from instructions pane to currentFile
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e) {
                    if (currentFile == null)
                    {
                        saveAs();
                    }
                    else
                    {
                        save(currentFile);
                    }
                }
            });

        // Save as Button -- calls method to save text from instructions pane to a file
        // designated by the user
        JButton saveAsButton = new JButton("Save as");
        saveAsButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e) {
                    saveAs();
                }
            });

        // Render Button -- calls method to instantiate new Instructions object from
        // text in instructions pane and display resulting chart in chart pane
        JButton renderButton = new JButton("Render");
        renderButton.addActionListener(
            new ActionListener() 
            {
                public void actionPerformed(ActionEvent e) {
                    String[] newText = parseInstructionsPane();
                    try
                    {
                        renderChart(newText);
                    }
                    // Intended to catch various exceptions generated by the Intructions class
                    // in case of syntax errors in the input text. Refer to the NOTE comments
                    // and method headers in Instructions.java to see where parsing code 
                    // may result in exceptions.
                    catch (Exception ex)
                    {
                        JOptionPane.showMessageDialog(null, "Error: Unable to render knitting chart.\nPlease check your pattern syntax and abbrevations and retry.\n(Click the \"Help\" button to view pattern syntax rules\n and recognized stitch abbrevations.)");
                    }
                }
            });

        // Help Button - opens (makes visible) a JFrame showing help text
        JButton helpButton = new JButton("Help");
        helpButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e) {
                    help.setVisible(true);
                }
            });

        buttons.add(importFileButton);
        buttons.add(saveButton);
        buttons.add(saveAsButton);
        buttons.add(renderButton);
        buttons.add(helpButton);

        add(buttons, BorderLayout.SOUTH);

        setVisible(true);

        //
        // Help panel
        //
        help = new JFrame();
        help.setLocation(575, 185);
        help.setSize(470, 670);
        help.setTitle("Help");
        JLabel helpText = new JLabel(HELP_TEXT);
        helpText.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        helpText.setOpaque(true);
        helpText.setBackground(Color.WHITE);
        helpText.setFont(new Font("Helvetica", Font.PLAIN, 12));
        help.add(helpText);
        help.setDefaultCloseOperation(HIDE_ON_CLOSE);

        JOptionPane.showMessageDialog(this, "Welcome!\n\nTo begin, type your knitting pattern into the lower pane,\nthen click the \"Render\" button.\n\nOr click \"Import\" to open a pattern from a text file.");

    }

    /**
     * Opens a file selected by the user through a file chooser dialog,
     * extracts text from file into an array (one line per array element),
     * displays text in instructions pane, and calls renderChart() method
     * to show corresponding chart in chart pane
     */
    private void importFile()
    {
        String[] stringArray = null; // Array to hold contents of file

        // Open dialog so user can select file
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) 
        {
            // If user selects a file, create a Scanner out of it
            currentFile = chooser.getSelectedFile();
            Scanner s = null;
            try
            {
                s = new Scanner(currentFile);
            }
            catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(this, "Error: Unable to find the file " + "\"" + currentFile + "\".");
            }

            ArrayList<String> importedText = new ArrayList<String>();
            if (!s.hasNext()) // If file has no content, report error to user
            {
                JOptionPane.showMessageDialog(this, "Error: File is empty or is not a recognized file type.");
            }
            else  // Otherwise, build array of lines from file using ArrayList
            {
                while (s.hasNext())
                {
                    importedText.add(s.nextLine());
                }
                // Convert ArrayList to array
                stringArray = new String[importedText.size()];
                stringArray = importedText.toArray(stringArray);
                // Add text from file to instructions pane
                updateInstructionPane(stringArray);
                // Call method to render chart out of instructions
                try
                {
                    renderChart(stringArray);
                }
                // Intended to catch various exceptions generated by the Intructions class
                // in case of syntax errors in the input text. Refer to the NOTE comments 
                // in Instructions.java to see where parsing code may result in exceptions.
                catch (Exception ex)
                {
                    JOptionPane.showMessageDialog(null, "Error: Unable to render knitting chart.\nPlease check your pattern syntax and abbrevations and retry.\n(Click the \"Help\" button to view pattern syntax rules\n and recognized stitch abbrevations.)");
                }

            }
        }
    }

    /**
     * Displays text from a supplied String array in the instructions pane
     *
     * @param	s	User-supplied knitting pattern instruction text from an imported file,
     *				parsed into an array (one line of text per array item)
     */
    private void updateInstructionPane(String[] s)
    {
        StringBuilder instructionText = new StringBuilder();
        for (String line : s)
        {
            instructionText.append(line);
            instructionText.append("\n");
        }
        instructionsPane.setText(instructionText.toString());
    }

    /**
     * Retrieves text from instructions pane and constructs a String array
     * from it (one line per array element)
     *
     * @return	User-supplied knitting pattern instructions from Instructions Pane,
     *			parsed into an array (one line of text per array item
     */
    private String[] parseInstructionsPane()
    {
        Scanner s = new Scanner(instructionsPane.getText());
        ArrayList<String> al = new ArrayList<String>();
        while (s.hasNext())
        {
            al.add(s.nextLine());
        }        
        String[] a = new String[al.size()];
        a = al.toArray(a);
        return a;
    }

    /**
     * Renders knitting chart corresponding to the text of a supplied String array
     * Creates a new Instructions object, then gets a 2D array of the Enum class Stitches
     * from the object. Loops through array and prints the symbol field of each Stitch in the chart.
     *
     * @param	s						User-supplied knitting pattern instruction text,  
     *									from imported file or instructions pane
     *
     * @throws	NullPointerException 	Thrown if an item in the stitches array is null,
     *									as a result of an unrecognized stitch abbrevation or
     *									syntax error, or unequal row lengths. Indicates a user syntax error.
     *
     * (This method will also bubble up exceptions thrown by the processRepeats method in the 
     * Instructions class, which is called by its constructor.)
     */
    private void renderChart(String[] s)
    {
        // Create an Instructions object out of the supplied String array of text instructions
        Instructions inst = new Instructions(s);
        
        // Get 2D array of Stitches from Instructions object
        Stitch[][] stitches = inst.getStitches();

        // Determine number of rows and stitches per row based on array size
        int numberOfRows = stitches.length;
        int stitchesPerRow = stitches[0].length;

        // Clear current contents of chart pane, add new GridLayout
        // based on number of rows and stitches per row
        chart.removeAll();
        chart.setLayout(new GridLayout(numberOfRows, stitchesPerRow,0,0));

        // Loop through 2D array of stitches to create an item for each stitch
        // and add it to the layout
        JLabel jl;

        // Knitting charts are read from the bottom up, so outer loop counts
        // down from the last row to the first
        for (int i = numberOfRows-1; i >= 0; i--)
        {
            // Knitting charts are read right to left, then left to right, alternating rows
            if (i%2 == 0) // right to left
            {
                for (int j = stitchesPerRow-1; j >= 0; j--)
                {
                    // Set label text to stitch symbol
                    jl = new JLabel(stitches[i][j].getSymbol(), SwingConstants.CENTER);
                    jl.setBorder(BorderFactory.createLineBorder(Color.BLACK,1));
                    // Set tooltip to stitch name for mouseover reference
                    String stitchName = stitches[i][j].getStitchName();
                    jl.setToolTipText(stitchName);
                    chart.add(jl);
                }
            }
            else // left to right
            {
                for (int j = 0; j < stitchesPerRow; j++)
                {
                    jl = new JLabel(stitches[i][j].getSymbol(), SwingConstants.CENTER);            
                    jl.setBorder(BorderFactory.createLineBorder(Color.BLACK,1));
                    String stitchName = stitches[i][j].getStitchName();
                    jl.setToolTipText(stitchName);
                    chart.add(jl);
               }
            }
            // NOTE: One or more elements in stitches array may be null if the 
            // convertToStitches method in the Instructions class was unable 
            // to match an abbrevation to a Stitch enum type, or if a false result
            // from checkRowLengths() in Instructions class prevented the convertToStitches
            // method from being called. This will cause a NullPointerException to be thrown
            // to the method that called renderChart(), generating an error message to the user.
        }

        // Redraw JFrame with new UI
        SwingUtilities.updateComponentTreeUI(this);
    }

    /**
     * Asks the user to choose a filename and location to save as,
     * then calls save method with that file
     */
    private void saveAs()
    {
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showSaveDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) 
        {
            currentFile = chooser.getSelectedFile(); // Set currentFile to user selection for next save
            save(currentFile);
        }        
    }

    /**
     * Saves contents of instructions pane to a file
     *
     * @param	f	File object that current contents of instructions pane should be saved to
     */
    private void save(File f)
    {
        FileWriter fw = null;
        try
        {
            fw = new FileWriter(f);
            fw.write(instructionsPane.getText());
            fw.close();
        }
        catch (IOException e) 
        {
            JOptionPane.showMessageDialog(this, "Error: Unable to save file.");
        }
        
    }
    /**
     * Main method - creates a new object of the KnittingEditor class
     */
    public static void main (String [] args)
    {
        KnittingEditor k = new KnittingEditor();
    }

}