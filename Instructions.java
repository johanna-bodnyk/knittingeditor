/**
 * CSCI E-10b - Final Project
 * Johanna Bodnyk | May 9, 2014
 *
 * This class is used by the KnittingEditor program to parse a set of written knitting instructions 
 * into a 2D array of stitch symbols to be rendered in a knitting chart.
 *
 */

import java.util.*;
import java.io.*;

class Instructions 
{
    private Stitch[][] stitches; // 2D array of enum type Stitch, representing the stitches in the instructions
    private int numberOfRows;
    private int stitchesPerRow;

    /**
     * Constructor
     * 
     * @param	originalInstructions	Array representation of user-supplied text version
     *									of knitting pattern instructions
     * 									(one line of instructions text per array element)
     */
    public Instructions (String[] originalInstructions)
    {
        // Each line of instructions equals one row in the knitting pattern
        numberOfRows = originalInstructions.length; 

        // Call method to parse the text instructions into a 2D ArrayList (one stitch abbreviation per array element)
        ArrayList<ArrayList<String>> parsedInstructions = parseTextInstructions(originalInstructions); 

        // Make sure each row has the same number of stitches
        boolean rowLengthsEqual = checkRowLengths(parsedInstructions);

        // If row lengths are all the same, pass 2D ArrayList to method 
        // that converts stitch abbrevations into Stitch enum types.
        // NOTE: If not, stitches array will be left empty so that attempting to
        // render it into a chart by the calling method will generate an exception.
        if (rowLengthsEqual)
        {
            convertToStitches(parsedInstructions);
        }
    }

    /**
     * Parses lines of instructions into individual stitch abbreviations
     *
     * @param	originalInstructions 	User-supplied text version of knitting pattern instructions
     *
     * @return	2D ArrayList of individual stitch abbrevations, 
     *			with repeats and multiples parsed into individual items
     */
    private ArrayList<ArrayList<String>> parseTextInstructions(String[] originalInstructions)
    {
        // Process repeat shorthand (repeated sequences of stitches in parentheses, brackets, or asterisks) 
        originalInstructions = processRepeats(originalInstructions);

        // Loop through original instructions array to split row strings on commas 
        // into arrays of stitch abbreviations
        ArrayList<ArrayList<String>> parsedInstructions = new ArrayList<ArrayList<String>>(); // 2D ArrayList to hold parsed instructions
        for (int i = 0; i < numberOfRows; i++) 
        {
            // Split each row into array individual abbrevations
            String[] tempArray = originalInstructions[i].split(", "); 
            // Convert array to ArrayList
            ArrayList<String> tempArrayList = new ArrayList<String>(Arrays.asList(tempArray)); 
            // Add ArrayList holding one row of abbrevations to 2D ArrayList
            parsedInstructions.add(tempArrayList); 
        }

        // Process abbreviations for multiple stitches (Kn, Pn,) into separate abbrevations (K3 ==> K, K, K)
        parsedInstructions = processMultiples(parsedInstructions);

        stitchesPerRow = parsedInstructions.get(0).size();

        return parsedInstructions;
    }

    /**
     * Processes syntax representing repeated stitch sequences into fully "written out" equivalent.
     * Repeated sequences are enclosed by parentheses, brackets, or asterisks, followed by a note 
     * specifying the number of times to repeat the sequence.
     *      Eg. "(k, yo) 3 times, " will be converted to "k, yo, k, yo, k, yo, "
     * 
     * @param 	rows	Unparsed user-supplied text version of knitting pattern instructions
     *
     * @return	Knitting pattern instruction text with syntax denoting repeats
     *			parsed into individual abbreviations
     *
     * @throws	IndexOutOfBoundsException	Thrown if an instance of the endDelimiter character
     *										is not found following a startDelimiter (so that
     *										endDelimiter is set to -1). Indicates a user syntax error.
     *
     * @throws	InputMismatchException		Thrown if the Scanner containing the text that is supposed
     *										to indicate the times to repeat the stitch sequence
     *										does not contain a parseable integer. Indicates user sytax error.
     */    
    private String[] processRepeats(String[] rows)
    {
        // Loop through array of rows to check each row for repeats
        for (int i = 0; i < numberOfRows; i++)
        {
            boolean hasRepeats = true;
            // Row may contain multiple repeat sections. Repeat until all are processed.
            while (hasRepeats) 
            {
                // If the row contains any of the characters indicating the beginning 
                // of a repeat section, set the start and end delimiter variables accordingly
                String startDelimiter = "";
                String endDelimiter = "";             
                if (rows[i].contains("("))
                {
                    startDelimiter = "(";
                    endDelimiter = ")";
                }
                else if (rows[i].contains("*"))
                {
                    startDelimiter = "*";
                    endDelimiter = "*";                    
                }
                else if (rows[i].contains("["))
                {
                    startDelimiter = "[";
                    endDelimiter = "]";                    
                }
                else
                {
                    hasRepeats = false; // If no repeat start characters are found, we're done checking this row
                }

                if (hasRepeats)
                {
                    // Create StringBuilder out of this row so it can be manipulated
                    StringBuilder row = new StringBuilder(rows[i]);

                    // Find the indexes of the first start delimiter and next end delimiter
                    // (these indexes enclose the sequence to be repeated)
                    int startDelimiterIndex = row.indexOf(startDelimiter);
                    int endDelimiterIndex = row.indexOf(endDelimiter, startDelimiterIndex+1); 
                    // NOTE: If no end delimiter is found, subsequent attempts to use it will 
                    // generate an IndexOutOfBoundsException to be caught by the main program,
                    // which will display an error message to the user.

                    // Get the sequence to be repeated (plus a comma and space), and its length
                    String repeatText = row.substring(startDelimiterIndex+1, endDelimiterIndex)+", ";
                    int repeatTextLength = repeatText.length();

                    // Find the index of the first comma following the end delimiter
                    // (the end delimiter and next comma enclose the instructions on how many times
                    // to repeat the text). If the repeat sequence is the last instruction in the row, 
                    // the comma index will be -1
                    int commaIndex = row.indexOf(",", endDelimiterIndex);

                    // Get the text that says how many times to repeat the sequence
                    String timesText = "";
                    if (commaIndex == -1) // If comma follows repeat sequence, get all text to end of row
                    {
                        timesText = row.substring(endDelimiterIndex+1);
                    }
                    else // Otherwise get the text between the end delimiter and the next comma
                    {
                        timesText = row.substring(endDelimiterIndex+1, commaIndex);
                    }
                    // From the resulting text, extract the first integer -- this is the times to repeat the sequence
                    int times = new Scanner(timesText).useDelimiter("\\D+").nextInt(); // Solution from: http://stackoverflow.com/questions/9742680/given-a-string-find-the-first-embedded-occurrence-of-an-integer
                    // NOTE: If no int is found, an exception will be generated 
                    // to be caught by the main program, which will display an 
                    // error message to the user.

                    // Replace the full repeat instructions -- from the start delimiter to the comma
                    // (or if there is no comma, to the end of the row) -- with the sequence to be repeated.
                    // This represents the first repeat.
                    if (commaIndex == -1)
                    {
                        row.replace(startDelimiterIndex,row.length()+1,repeatText);
                    }
                    else
                    {
                        row.replace(startDelimiterIndex,commaIndex+2,repeatText);
                    }

                    // For the second through nth repeats, 
                    // insert the repeat sequence after the last one that was added
                    for (int j = 1; j < times; j++)
                    {
                        // Insert the sequence at the index of the start delimiter plus
                        // the sequence length times how many times it's already been added
                        int insertIndex = startDelimiterIndex+(repeatTextLength*j);
                        row.insert(insertIndex, repeatText);
                    }

                    // Convert StringBuilder to String add replace current row in array with it
                    rows[i] = row.toString();
                }
            }
        }
        return rows;
    }

    /**
     * Proccess abbrevations representing multiple stitches into fully "written out" equivalent.
     * Syntax for multiple stitches is Kn or Pn where n is the number of times to repeat K or N.
     *      eg. "P5" will be converted to "P, P, P, P, P" (this is why an ArrayList is needed)
     *
     * @param 	parsedInstructions	User supplied text, repeats removed and comma seperated abbrevations
     *								parsed into individual array items
     *
     * @return	Further parsed text with syntax denoting multiple stitches
     *			parsed into individual abbreviations
     */  
    private ArrayList<ArrayList<String>> processMultiples(ArrayList<ArrayList<String>> parsedInstructions)
    {
        for (int i = 0; i < numberOfRows; i++)
        {
            // Check each abbrevation in each row to see if it matches syntax for multiples
            for (int j = 0; j < parsedInstructions.get(i).size(); j++)
            {
                // Get current abbrevation string
                String instruction = parsedInstructions.get(i).get(j);
                // See if it matches multiple syntax: upper or lowercase K or P followed by 1 or more digits
                if (instruction.matches("[kKpP][1-9]+"))
                {
                    // Extract stitch abbrevation from front
                    String type = instruction.substring(0,1); 
                    // Extract integer representing times to repeat
                    int repeat = Integer.parseInt(instruction.substring(1));
                    // Replace abbrevation representing multiple with one instance of stitch abbrevation
                    parsedInstructions.get(i).set(j, type);
                    // Insert second through nth instances following first instance
                    for (int k = 1; k < repeat; k++)
                    {
                        parsedInstructions.get(i).add(j+k-1, type);
                    }
                }
            }
        }
        return parsedInstructions;    
    }

    /**
     * Makes sure all rows are the same size. Returns true if so, false if not.
     *
     * @param 	parsedInstructions	Knitting pattern instructions separated into individual
     *								abbrevations with repeats and multiples parsed out
     *
     * @return	True if all rows are the same size, false if not
     */
    private boolean checkRowLengths(ArrayList<ArrayList<String>> parsedInstructions)
    {
        int previousRowLength = parsedInstructions.get(0).size(); // Length of first row
        int currentRowLength = 0;
        // Check second through nth rows to make sure they are the same length as the previous row
        for (int i = 1; i < numberOfRows; i++)
        {
            currentRowLength = parsedInstructions.get(i).size();
            if (currentRowLength != previousRowLength)
            {
                return false;
            }
            previousRowLength = currentRowLength;
        }
        return true;
    }

    /**
     * Sets up the stitches array, a 2D array of the Stitch enum type representing
     * the stitch symbols that correspond to the abbrevations in the parsedInstructions ArrayList
     *
     * @param 	parsedInstructions	Knitting pattern instructions separated into individual
     *								abbrevations with repeats and multiples parsed out
     */    
    private void convertToStitches(ArrayList<ArrayList<String>> parsedInstructions)
    {
        // Initialize stitches array with correct number of rows and stitches per row
        stitches = new Stitch[numberOfRows][stitchesPerRow];

        // Loop through rows, and stitches in each row
        for (int i = 0; i < numberOfRows; i++)
        {
            for (int j = 0; j < stitchesPerRow; j++)
            {
                // For each stitch, loop through all the possible values of the Stitch enum type
                for (Stitch stitch : Stitch.values())
                {
                    // If the name of a value in the Stitch enum type matches the abbrevation
                    // of the current stitch in parsedInstructions, add that Stitch enum type
                    // to the corresponding spot in the stitch array
                    if (stitch.name().equalsIgnoreCase(parsedInstructions.get(i).get(j)))
                    {
                        stitches[i][j] = stitch;
                    }
                }
                // NOTE: If a match is not found, this spot on the stitch array will be left empty
                // generating an exception when the calling method attempts to render the chart.
            }
            
        }
    }

    /**
     * Getter for stitch array
     *
     * @return	2D array with elements of enum type Stitch representing the rows
     *			and individual stitches of the user-supplied knitting pattern instructions
     */   
    public Stitch[][] getStitches ()
    {
        return stitches;
    }

}