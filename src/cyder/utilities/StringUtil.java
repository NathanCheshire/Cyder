package cyder.utilities;

import cyder.handler.ErrorHandler;
import cyder.ui.ConsoleFrame;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//a StringUtil method should always be connected to an inputhandler if instantiated
public class StringUtil {
    private static JTextPane outputArea = null;

    private StringUtil() {} //no instantiation without jtextpane object

    //StringUtil can only be instantiated if a valid JTextPane is provided
    public StringUtil(JTextPane outputArea) {
        this.outputArea = outputArea;
    }

    /**
     * Standard getter for this object's possible JTextPane
     * @return - The resultant output area if one is connected
     */
    public static JTextPane getOutputArea() {
        return outputArea;
    }

    private boolean userInputMode;
    private String userInputDesc;

    /**
     * Getter for this instance's input mode
     * @return - the value of user input mode
     */
    public boolean getUserInputMode() {
        return this.userInputMode;
    }

    /**
     * Set the value of secondary input mode
     * @param b - the value of input mode
     */
    public void setUserInputMode(boolean b) {
        this.userInputMode = b;
    }

    /**
     * Returns the expected secondary input description
     * @return - the input description
     */
    public String getUserInputDesc() {
        return this.userInputDesc;
    }

    /**
     * Sets this instance's secondary input description
     * @param s - the description of the input we expect to receive next
     */
    public void setUserInputDesc(String s) {
        this.userInputDesc = s;
    }

    /**
     * Sets the output area for this instance of StringUtil.
     * @param jTextPane - the JTextPane which we will append to when needed
     */
    public void setOutputArea(JTextPane jTextPane) {
        this.outputArea = jTextPane;
    }

    /**
     * Prints a suggestion as to what the user should do
     */
    public void help() {

        String[] Helps = {
                "Nathan forgot to finish this; tell him to fill out this list"
        };

        ArrayList<Integer> UniqueIndexes = new ArrayList<>();

        for (int i = 0; i < Helps.length; i++)
            UniqueIndexes.add(i);

        Collections.shuffle(UniqueIndexes);
        println("Try typing:");

        for (int i = 0; i < 10; i++)
            println(Helps[UniqueIndexes.get(i)]);
    }

    //todo test
    public void removeLastChars(int n) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.remove(document.getLength() - 1 - n,document.getLength() - 1);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //todo test
    public void removeFirstChars(int n) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.remove(0, n);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //todo test
    public void removeFirstLine() {
        try {
            Element root = outputArea.getDocument().getDefaultRootElement();
            Element first = root.getElement(0);
            outputArea.getDocument().remove(first.getStartOffset(), first.getEndOffset());
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        } catch (BadLocationException e) {
            ErrorHandler.handle(e);
        }
    }

    //todo test
    public void removeLastLine() {
        try {
            Element root = outputArea.getDocument().getDefaultRootElement();
            Element first = root.getElement(root.getElementCount() - 1);
            outputArea.getDocument().remove(first.getStartOffset(), first.getEndOffset());
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        } catch (BadLocationException e) {
            ErrorHandler.handle(e);
        }
    }

    public void print(String Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage, null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void print(int Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Integer.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void print(double Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Double.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void print(boolean Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Boolean.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void print(float Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Float.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void print(long Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Long.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void print(char Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), String.valueOf(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void print(Object Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage.toString(), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void println(String Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void println(int Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void println(double Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void println(boolean Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void println(float Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void println(long Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void println(char Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void println(Object Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage.toString() + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Reverses the given array
     * @param Array - the array to reverse
     * @return - the reversed array
     */
    public static char[] reverseArray(char[] Array) {
        String reverse = new StringBuilder(new String(Array)).reverse().toString();
        return reverse.toCharArray();
    }

    /**
     * Determines if the provided string starts with the provided prefix
     * @param string - the string to see if it starts with the given prefix
     * @param prefix - the prefix to search the array for
     * @return - the boolean result of the comparison
     */
    public static boolean startsWith(String string, String prefix) {
        char[] opA = string.toLowerCase().toCharArray();

        char[] compA = prefix.toLowerCase().toCharArray();

        for (int i = 0 ; i < prefix.length() ; i++) {
            if (Math.min(compA.length, opA.length) >= i) {
                return false;
            }

            if (Math.min(compA.length, opA.length) < i && compA[i] != opA[i]) {
                return false;
            }

            else if (Math.min(compA.length, opA.length) < i && compA[i] == opA[i] && i == compA.length - 1 && opA[i+1] == ' ') {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines if the provided string ends with the provided suffix
     * @param string - the string to to see if it ends with the given suffix
     * @param suffix - the suffix to search the array for
     * @return - the boolean result of the comparison
     */
    public static boolean endsWith(String string, String suffix) {
        char[] opA = reverseArray(string.toLowerCase().toCharArray());
        char[] compA = reverseArray(suffix.toLowerCase().toCharArray());

        for (int i = 0 ; i < suffix.length() ; i++) {
            if (Math.min(opA.length, compA.length) >= i) {
                return false;
            }

            if (i < Math.min(opA.length, compA.length) && compA[i] != opA[i]) {
                return false;
            }

            else if (compA[i] == opA[i] && i == compA.length - 1 && opA[i+1] == ' ') {
                return true;
            }
        }

        return false;
    }

    /**
     * Prints the object array to {@link this} object's connected output area
     * @param arr - the array of objects to print
     */
    public void printArr(Object[] arr) {
        for (Object o : arr)
            println(o);
    }

    //todo this will be written in logs so held in memory until proper closing
    public void logSuggestion(String input) {
        try {
            if (input != null && !input.equals("") && !filterLanguage(input) && input.length() > 10 && !filterLanguage(input)) {
                File suggestionsFile = new File("sys/text/suggestions.txt");

                if (!suggestionsFile.exists())
                    suggestionsFile.mkdir();

                BufferedWriter sugWriter = new BufferedWriter(new FileWriter(suggestionsFile, true));

                sugWriter.write("User " + ConsoleFrame.getUsername() + " at " + new TimeUtil().weatherTime() + " made the suggestion: ");
                sugWriter.write(System.getProperty("line.separator"));

                sugWriter.write(input);

                sugWriter.write(System.getProperty("line.separator"));
                sugWriter.write(System.getProperty("line.separator"));

                sugWriter.flush();
                sugWriter.close();

                println("Request registered.");
                sugWriter.close();
            }
        }

        catch (Exception ex) {
            ErrorHandler.handle(ex);
        }
    }

    /**
     * Determines the proper english grammer when attempting to use possession on a string that typically
     * represents a noun.
     * @param name - the proper name of the noun
     * @return - the string to be appended to the proper noun ('s or simply ')
     */
    public static String getApostrophe(String name) {
        if (name.endsWith("s"))
            return "'";
        else
            return "'s";
    }

    /**
     * Determines if the given string is empty
     * @param s - the string to compare for emptiness (self.Soul() usually returns true)
     * @return - the boolean result of the comparison
     */
    public static boolean empytStr(String s) {
        return (s == null ? null: (s == null) || (s.trim().equals("")) || (s.trim().length() == 0));
    }

    /**
     * Fills a string with the provided character to result in a string of the specified length
     * @param count - the length of the resultant string
     * @param c - the character to fill the string with
     * @return - the resultant filled array
     */
    public static String fillString(int count, String c) {
        StringBuilder sb = new StringBuilder(count);

        for (int i = 0; i < count; i++) {
            sb.append(c);
        }

        return sb.toString();
    }

    /**
     * Finds the first word in a given string.
     * @param sentence - the string to search for
     * @return - the resultant first word found
     */
    public static String firstWord(String sentence) {
        String[] sentences = sentence.split(" ");
        return sentences[0];
    }

    /**
     * Determines if a word is palindrome (spelled the same forward and backwards like ogopogo and racecar).
     * @param word - the word to check
     * @return - the result of the comparison
     */
    public static boolean isPalindrome(String word) {
        return Arrays.equals(word.toLowerCase().toCharArray(), reverseArray(word.toLowerCase().toCharArray()));
    }

    /**
     * Uses a regex to find the first occurence of a digit and follows until no more digits.
     * @param search - the string to search for digits in
     * @return - the reusltant number found, if any
     */
    public static String firstNumber(String search) {
        Pattern Pat = Pattern.compile("\\d+");
        Matcher m = Pat.matcher(search);
        return m.find() ? m.group() : null;
    }

    /**
     * Matches a given string with the provided regex and returns the result.
     * @param search - the string to use the regex on
     * @param regex - the regex to compare to the given string
     * @return - the resultant match of the string to the regex
     */
    public static String match(String search, String regex) {
        Pattern pat = Pattern.compile(regex);
        Matcher match = pat.matcher(search);
        return match.find() ? match.group() : null;
    }

    /**
     * Concatinates two arrays together.
     * @param a - the first array
     * @param b - the second array
     * @return - the resultant array
     */
    public static String[] combineArrays(String[] a, String[] b) {
        int length = a.length + b.length;
        String[] result = new String[length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    /**
     * Converts the first character in a string to the capital version of it if it is a standard latin letter
     * @param word - the word to capitalize the first letter of
     * @return - the resultant wtring
     */
    public static String capsFirst(String word) {
        StringBuilder SB = new StringBuilder(word.length());
        String[] Words = word.split(" ");

        for (String wordy : Words) {
            SB.append(Character.toUpperCase(wordy.charAt(0))).append(wordy.substring(1)).append(" ");
        }

        return SB.toString();
    }


    /**
     * Filters out simple leet speech from the provided string.
     * @param filter - the word to filter leet out of
     * @return - the resultant string after filtering
     */
    public static String filterLeet(String filter) {
        if (filter == null || filter.length() == 0)
            return null;

        //split at spaces and run leet in each of those
        String[] words = filter.split(" ");

        if (words.length == 0)
            return filter;

        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            sb.append(wordLeet(word)).append(" ");
        }

        return sb.toString().trim();
    }

    /**
     * Inner filtering of leet speach for words specifically, this is the main driver method that does the magic.
     * @param word - the word to filter leet out of.
     * @return - the word having leet removed to the best of our abilities
     */
    private static String wordLeet(String word) {
        char[] chars = word.toLowerCase().toCharArray();

        for (int i = 0 ; i < chars.length ; i++) {
            char c = chars[i];

            //someone pls make this better and PR a method for this, make it work for permutations
            //from this table: https://cleanspeak.com/images/blog/leet-wiki-table.png

            if (c == '4' || c == '@' || c == '^' || c == 'z') 
                chars[i] = 'a';
            else if (c == '8' || c == '6') 
                chars[i] = 'b';
            else if (c == '(' || c == '<' || c == '{')
                chars[i] = 'c';
            else if (c == '3' || c == '&')
                chars[i] = 'e';
            else if (c == '}')
                chars[i] = 'f';
            else if ( c == '9')
                chars[i] = 'g';
            else if (c == '#')
                chars[i] = 'h';
            else if (c == '1' || c == '!' || c == '|')
                chars[i] = 'i';
            else if (c == ']')
                chars[i] = 'j';
            else if (c == '7')
                chars[i] = 'l';
            else if (c == '~')
                chars[i] = 'n';
            else if (c == '0')
                chars[i] = 'o';
            else if (c == '?' || c == 'q')
                chars[i] = 'p';
            else if (c == '2')
                chars[i] = 'r';
            else if (c == '$' || c == '5')
                chars[i] = 's';
            else if (c == '+')
                chars[i] = 't';
            else if (c == '*')
                chars[i] = 'x';
            else if (c == '%')
                chars[i] = 'z';
            
        }

        return String.valueOf(chars);
    }

    /**
     * Tests whether or not the provided string has the provided word inside of it.
     * @param userInput - the master string to search through
     * @param word - the word to search the master string for
     * @return - a boolean depicting whether or not the given string contains the test word
     */
    public static boolean hasWord(String userInput, String word) {
        userInput.toLowerCase();
        word.toLowerCase();

        return  word.equals(userInput) ||
                word.contains(' ' + userInput + ' ') ||
                word.contains(' ' + userInput) ||
                word.contains(userInput + ' ') ||
                word.contains(userInput + ' ');
    }

    /**
     * Tests a given string to see if it contains any blocked words contained in the v.txt system file
     * @param userInput - the provided string to test against
     * @return - a boolean describing whether or not the filter was triggered by the input
     */
    public static boolean filterLanguage(String userInput) {
        try {
            BufferedReader vReader = new  BufferedReader(new FileReader("sys/text/v.txt"));
            String blockedWord = vReader.readLine();
            userInput = filterLeet(userInput.toLowerCase());

            while(blockedWord != null)  {
                if (hasWord(userInput, blockedWord)) {
                    vReader.close();
                    return true;
                }

                blockedWord = vReader.readLine();
            }

            vReader.close();
        }

        catch (Exception ex) {
            ErrorHandler.handle(ex);
        }

        return false;
    }

    /**
     * Provides the exact string object but with the first character converted to lowercase.
     * @param str - the string to convert the first character to lowercase
     * @return - the resultant string
     */
    public static String firstCharToLowerCase(String str) {
        if (str == null || str.length() == 0)
            return "";

        if(str.length() == 1)
            return str.toLowerCase();
        else return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /**
     * Count the number of words of the provided string.
     * @param str - the string ot count the words of
     * @return - the word count of the requested string
     */
    public static int countWords(String str) {
        return (str == null || str.isEmpty()) ? 0 : str.split("\\s+").length;
    }

    /**
     * Uses a regex to get the file name of the provided file, does not return the period.
     * @param file - the file of which to return the name of (this does not include the
     *             extension; use {@link File#getName()} )} to get the full filename + extension)
     * @return - the file name requested
     */
    public static String getFilename(String file) {
        return file.replaceAll("\\.([^.]+)$", "");
    }

    /**
     * Uses a regex to get the file extension of the provided file, returns the period too.
     * @param file - the name of the file of which to return the extension of
     * @return - the file extension requested
     */
    public static String getExtension(String file) {
        return file.replace(getFilename(file), "");
    }

    /**
     * Uses a regex to get the file name of the provided file, does not return the period.
     * @param file - the name of the file of which to return the name of (this does not include the
     *             extension; use {@link File#getName()})} to get the full filename + extension)
     * @return - the file name requested
     */
    public static String getFilename(File file) {
        return file.getName().replaceAll("\\.([^.]+)$", "");
    }

    /**
     * Uses a regex to get the file extension of the provided file, returns the period too.
     * @param file - the file of which to return the extension of
     * @return - the file extension requested
     */
    public static String getExtension(File file) {
        return file.getName().replace(getFilename(file), "");
    }

    /**
     * Determines if a string is confirming a question or denying it
     * @param input - the input string to check for verifcation key words
     * @return - the boolean result of the confirmation
     */
    public static boolean isConfirmation(String input) {
        return (input.equalsIgnoreCase("yes") ||
                input.equalsIgnoreCase("y") ||
                input.equalsIgnoreCase("sure") ||
                input.equalsIgnoreCase("i guess") ||
                input.equalsIgnoreCase("why not") ||
                input.equalsIgnoreCase("mhmm") ||
                input.equalsIgnoreCase("mhm") ||
                input.equalsIgnoreCase("k") ||
                input.equalsIgnoreCase("ok") ||
                input .equalsIgnoreCase("okay") ||
                input.contains("go"));
    }
}