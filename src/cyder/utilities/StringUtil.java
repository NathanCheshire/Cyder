package cyder.utilities;

import cyder.consts.CyderStrings;
import cyder.genesis.GenesisShare;
import cyder.handlers.internal.ErrorHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple general String util methods along with some JTextPane appending methods
 * Note: these methods are not thread safe and you should take that into account when using these utils
 */
public class StringUtil {
    private JTextPane linkedJTextPane = null;

    public void setItemAlignment(int styleConstantsAlignment) {
        SimpleAttributeSet attribs = new SimpleAttributeSet();
        StyleConstants.setAlignment(attribs, styleConstantsAlignment);
        linkedJTextPane.setParagraphAttributes(attribs, true);
    }

    private StringUtil() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    } //no instantiation without jtextpane object

    //StringUtil can only be instantiated if a valid JTextPane is provided
    public StringUtil(JTextPane linkedJTextPane) {
        this.linkedJTextPane = linkedJTextPane;
    }

    /**
     * Standard getter for this object's possible JTextPane
     * @return The resultant output area if one is connected
     */
    public JTextPane getLinkedJTextPane() {
        return linkedJTextPane;
    }

    /**
     * Sets the output area for this instance of StringUtil.
     * @param jTextPane the JTextPane which we will append to when needed
     */
    public void setLinkedJTextPane(JTextPane jTextPane) {
        this.linkedJTextPane = jTextPane;
    }

    /**
     * Removes the first object from the linked pane, this could be anything from a Component to a String
     */
    public void removeFirst() {
        try {
            GenesisShare.getPrintingSem().acquire();
            Element root = linkedJTextPane.getDocument().getDefaultRootElement();
            Element first = root.getElement(0);
            linkedJTextPane.getDocument().remove(first.getStartOffset(), first.getEndOffset());
            linkedJTextPane.setCaretPosition(linkedJTextPane.getDocument().getLength());
            GenesisShare.getPrintingSem().release();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Removes the last "thing" addeed to the JTextPane whether it's a component,
     *  icon, or string of multi-llined text.
     *  In more detail, this method figures out what it'll be removing and then determines how many calls
     *   are needed to {@link StringUtil#removeLastLine()}
     */
    public void removeLast() {
        try {
            boolean removeTwoLines = false;

            LinkedList<Element> elements = new LinkedList<>();
            ElementIterator iterator = new ElementIterator(linkedJTextPane.getStyledDocument());
            Element element;
            while ((element = iterator.next()) != null) {
                elements.add(element);
            }

            int leafs = 0;

            for (Element value : elements)
                if (value.getElementCount() == 0)
                    leafs++;

            int passedLeafs = 0;

            for (Element value : elements) {
                if (value.getElementCount() == 0) {
                    if (passedLeafs + 3 != leafs) {
                        passedLeafs++;
                        continue;
                    }

                    if (value.toString().toLowerCase().contains("icon") || value.toString().toLowerCase().contains("component")) {
                        removeTwoLines = true;
                    }
                }
            }

            GenesisShare.getPrintingSem().acquire();

            if (removeTwoLines) {
                removeLastLine();
            }

            removeLastLine();

            GenesisShare.getPrintingSem().release();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Finds the last line of text from the linked output area
     * @return the last line of raw ASCII text
     */
    public String getLastTextLine() {
        String text = linkedJTextPane.getText();
        String[] lines = text.split("\n");
        return lines[lines.length - 1];
    }

    /**
     * Removes the last line added to the linked JTextPane. This could appear to remove nothing,
     *  but really be removing just a newline (line break) character.
     */
    public void removeLastLine() {
        try {
            LinkedList<Element> elements = new LinkedList<>();
            ElementIterator iterator = new ElementIterator(linkedJTextPane.getStyledDocument());
            Element element;
            while ((element = iterator.next()) != null) {
                elements.add(element);
            }

            int leafs = 0;

            for (Element value : elements)
                if (value.getElementCount() == 0)
                    leafs++;

            int passedLeafs = 0;

            for (Element value : elements) {
                if (value.getElementCount() == 0) {
                    if (passedLeafs + 2 != leafs) {
                        passedLeafs++;
                        continue;
                    }

                    linkedJTextPane.getStyledDocument().remove(value.getStartOffset(),
                            value.getEndOffset() - value.getStartOffset());
                }
            }
        } catch (BadLocationException ignored) {}
        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //start generic print methods for the linked JTextPane, these are not thread safe by default
    // See ConsoleFrame's outputArea and implementation there for thread safety

    /**
     * Adds a {@link Component} to the linked JTextPane. Make sure all listeners, bounds,
     *  modifiers, etc. have been set before printing the component.
     * @param c the component to append to the pane
     * @param nm the name identifier for the style
     * @param str the string identifier for the underlying insert string call
     */
    public void printComponent(Component c, String nm, String str) {
        try {
            Style cs = linkedJTextPane.getStyledDocument().addStyle(nm, null);
            StyleConstants.setComponent(cs, c);
            linkedJTextPane.getStyledDocument().insertString(linkedJTextPane.getStyledDocument().getLength(), str, cs);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Adds a {@link Component} to the linked JTextPane. Make sure all listeners, bounds,
     *  modifiers, etc. have been set before printing the component.
     * @param c the component to append to the pane
     */
    public void printComponent(Component c) {
        try {
            String componentUUID = SecurityUtil.generateUUID();
            Style cs = linkedJTextPane.getStyledDocument().addStyle(componentUUID, null);
            StyleConstants.setComponent(cs, c);
            linkedJTextPane.getStyledDocument().insertString(linkedJTextPane.getStyledDocument().getLength(), componentUUID, cs);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Adds a {@link Component} to the linked JTextPane. Make sure all listeners, bounds,
     *  modifiers, etc. have been set before printing the component. Following the component print,
     *  a new line is appended to the pane.
     * @param c the component to append to the pane
     */
    public void printlnComponent(Component c) {
        try {
            String componentUUID = SecurityUtil.generateUUID();
            printComponent(c, componentUUID, componentUUID);
            println("");
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Adds a {@link Component} to the linked JTextPane. Make sure all listeners, bounds,
     *  modifiers, etc. have been set before printing the component. Following the component print,
     *  a new line is appended to the pane.
     * @param c the component to append to the pane
     * @param nm the name identifier for the style
     * @param str the string identifier for the underlying insert string call
     */
    public void printlnComponent(Component c, String nm, String str) {
        try {
            printComponent(c, nm, str);
            println("");
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void print(String Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedJTextPane.getDocument();
            document.insertString(document.getLength(), Usage, null);
            linkedJTextPane.setCaretPosition(linkedJTextPane.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void print(int Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedJTextPane.getDocument();
            document.insertString(document.getLength(), Integer.toString(Usage), null);
            linkedJTextPane.setCaretPosition(linkedJTextPane.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void print(double Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedJTextPane.getDocument();
            document.insertString(document.getLength(), Double.toString(Usage), null);
            linkedJTextPane.setCaretPosition(linkedJTextPane.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void print(boolean Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedJTextPane.getDocument();
            document.insertString(document.getLength(), Boolean.toString(Usage), null);
            linkedJTextPane.setCaretPosition(linkedJTextPane.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void print(float Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedJTextPane.getDocument();
            document.insertString(document.getLength(), Float.toString(Usage), null);
            linkedJTextPane.setCaretPosition(linkedJTextPane.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void print(long Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedJTextPane.getDocument();
            document.insertString(document.getLength(), Long.toString(Usage), null);
            linkedJTextPane.setCaretPosition(linkedJTextPane.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void print(char Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedJTextPane.getDocument();
            document.insertString(document.getLength(), String.valueOf(Usage), null);
            linkedJTextPane.setCaretPosition(linkedJTextPane.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void print(Object Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedJTextPane.getDocument();
            document.insertString(document.getLength(), Usage.toString(), null);
            linkedJTextPane.setCaretPosition(linkedJTextPane.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void println(String Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedJTextPane.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            linkedJTextPane.setCaretPosition(linkedJTextPane.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void println(int Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedJTextPane.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            linkedJTextPane.setCaretPosition(linkedJTextPane.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void println(double Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedJTextPane.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            linkedJTextPane.setCaretPosition(linkedJTextPane.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void println(boolean Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedJTextPane.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            linkedJTextPane.setCaretPosition(linkedJTextPane.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void println(float Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedJTextPane.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            linkedJTextPane.setCaretPosition(linkedJTextPane.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void println(long Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedJTextPane.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            linkedJTextPane.setCaretPosition(linkedJTextPane.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void println(char Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedJTextPane.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            linkedJTextPane.setCaretPosition(linkedJTextPane.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void println(Object Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedJTextPane.getDocument();
            document.insertString(document.getLength(), Usage.toString() + "\n", null);
            linkedJTextPane.setCaretPosition(linkedJTextPane.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //end generic print methods

    /**
     * Reverses the given array
     * @param Array the array to reverse
     * @return the reversed array
     */
    public static char[] reverseArray(char[] Array) {
        String reverse = new StringBuilder(new String(Array)).reverse().toString();
        return reverse.toCharArray();
    }

    /**
     * Prints the object array to {@link this} object's connected output area
     * @param arr the array of objects to print
     */
    public void printArr(Object[] arr) {
        for (Object o : arr)
            println(o);
    }

    /**
     * Determines the proper english grammer when attempting to use possession on a string that typically
     * represents a noun.
     * @param name the proper name of the noun
     * @return the string to be appended to the proper noun ('s or simply ')
     */
    public static String getApostrophe(String name) {
        if (name.endsWith("s"))
            return "'";
        else
            return "'s";
    }

    /**
     * Returns the plural form of the word. A singular item doesn't need to be made plural
     * whilst any number of objects other than 1 should be converted to plural using English Language rules.
     * @param num the number of items associated with the word
     * @param word the word to be converted to plural
     * @return the plural form of the word
     */
    public static String getPlural(int num, String word) {
        if (num == 1) {
            return word;
        } else {
            return word.endsWith("s") ? word + "es" : word + "s";
        }
    }
    /**
     * Determines if the given string is empty
     * @param s the string to compare for emptiness (self.Soul() usually returns true)
     * @return the boolean result of the comparison
     */
    public static boolean empytStr(String s) {
        return (s == null ? null: (s == null) || (s.trim().length() == 0));
    }

    /**
     * Fills a string with the provided character to result in a string of the specified length
     * @param count the length of the resultant string
     * @param c the character to fill the string with
     * @return the resultant filled array
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
     * @param sentence the string to search for
     * @return the resultant first word found
     */
    public static String firstWord(String sentence) {
        String[] sentences = sentence.split(" ");
        return sentences[0];
    }

    /**
     * Determines if a word is palindrome (spelled the same forward and backwards like ogopogo and racecar).
     * @param word the word to check
     * @return the result of the comparison
     */
    public static boolean isPalindrome(String word) {
        return Arrays.equals(word.toLowerCase().toCharArray(), reverseArray(word.toLowerCase().toCharArray()));
    }

    /**
     * Uses a regex to find the first occurence of a digit and follows until no more digits.
     * @param search the string to search for digits in
     * @return the reusltant number found, if any
     */
    public static String firstNumber(String search) {
        Pattern Pat = Pattern.compile("\\d+");
        Matcher m = Pat.matcher(search);
        return m.find() ? m.group() : null;
    }

    /**
     * Matches a given string with the provided regex and returns the result.
     * @param search the string to use the regex on
     * @param regex the regex to compare to the given string
     * @return the resultant match of the string to the regex
     */
    public static String match(String search, String regex) {
        Pattern pat = Pattern.compile(regex);
        Matcher match = pat.matcher(search);
        return match.find() ? match.group() : null;
    }

    /**
     * Concatinates two arrays together.
     * @param a the first array
     * @param b the second array
     * @return the resultant array
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
     * @param word the word to capitalize the first letter of
     * @return the resultant wtring
     */
    public static String capsFirst(String word) {
        if (word.length() == 0)
            return word;

        StringBuilder SB = new StringBuilder(word.length());
        String[] words = word.split(" ");

        for (String wordy : words) {
            SB.append(Character.toUpperCase(wordy.charAt(0))).append(wordy.substring(1)).append(" ");
        }

        return SB.toString();
    }


    /**
     * Filters out simple leet speech from the provided string.
     * @param filter the word to filter leet out of
     * @return the resultant string after filtering
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
     * @param word the word to filter leet out of.
     * @return the word having leet removed to the best of our abilities
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
     * @param userInput the master string to search through
     * @param findWord the word to search the master string for
     * @return a boolean depicting whether or not the given string contains the test word
     */
    public static boolean hasWord(String userInput, String findWord) {
        return hasWord(userInput, findWord, false);
    }

    /**
     * Tests whether or not the provided string has the provided word inside of it.
     * @param userInput the master string to search through
     * @param findWord the word to search the master string for
     * @param removeComments whether or not to remove comment tags from the input
     * @return a boolean depicting whether or not the given string contains the test word
     */
    public static boolean hasWord(String userInput, String findWord, boolean removeComments) {
        if (userInput == null || findWord == null)
            throw new IllegalArgumentException("Provided input is null: userInput = " + userInput + ", word = " + findWord);

        if (removeComments) {
            userInput = userInput.replace("//","")
                    .replace("/*","")
                    .replace("*/","")
                    .replace("*","");
        }

        userInput = userInput.toLowerCase();
        findWord.toLowerCase();

        return userInput.startsWith(findWord + ' ') || //first word
               userInput.endsWith(' ' + findWord) || //last word
               userInput.contains(' ' + findWord + ' ') || //middle word
               userInput.equalsIgnoreCase(findWord); //literal
    }

    /**
     * Tests a given string to see if it contains any blocked words contained in the v.txt system file
     * @param input the provided string to test against
     * @param filterLeet whether or not to filter out possible leet from the string
     * @return a boolean describing whether or not the filter was triggered by the input
     */
    public static boolean filterLanguage(String input, boolean filterLeet) {
        boolean ret = false;

        if (filterLeet)
            input = filterLeet(input.toLowerCase());

        try (BufferedReader vReader = new BufferedReader(new FileReader("static/text/v.txt"))) {
            String blockedWord;

            while ((blockedWord = vReader.readLine()) != null) {
                if (hasWord(input, blockedWord, true)) {
                    ret = true;
                    break;
                }
            }
        } catch (Exception ex) {
            ErrorHandler.handle(ex);
        } finally {
            return ret;
        }
    }

    /**
     * Provides the exact string object but with the first character converted to lowercase.
     * @param str the string to convert the first character to lowercase
     * @return the resultant string
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
     * @param str the string ot count the words of
     * @return the word count of the requested string
     */
    public static int countWords(String str) {
        return (str == null || str.isEmpty()) ? 0 : str.split("\\s+").length;
    }

    /**
     * Uses a regex to get the file name of the provided file, does not return the period.
     * @param file the file of which to return the name of (this does not include the
     *             extension; use {@link File#getName()} )} to get the full filename + extension)
     * @return the file name requested
     */
    public static String getFilename(String file) {
        return file.replaceAll("\\.([^.]+)$", "");
    }

    /**
     * Uses a regex to get the file extension of the provided file, returns the period too.
     * @param file the name of the file of which to return the extension of
     * @return the file extension requested
     */
    public static String getExtension(String file) {
        return file.replace(getFilename(file), "");
    }

    /**
     * Uses a regex to get the file name of the provided file, does not return the period.
     * @param file the name of the file of which to return the name of (this does not include the
     *             extension; use {@link File#getName()})} to get the full filename + extension)
     * @return the file name requested
     */
    public static String getFilename(File file) {
        return file.getName().replaceAll("\\.([^.]+)$", "");
    }

    /**
     * Uses a regex to get the file extension of the provided file, returns the period too.
     * @param file the file of which to return the extension of
     * @return the file extension requested
     */
    public static String getExtension(File file) {
        return file.getName().replace(getFilename(file), "");
    }

    /**
     * Determines if a string is confirming a question or denying it
     * @param input the input string to check for verifcation key words
     * @return the boolean result of the confirmation
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

    /**
     * Ensures that there is a space after every comma within the input.
     * @param input the potentially wrongly formatted string
     * @return the corrected string
     */
    public static String formatCommas(String input) {
        if (!input.contains(","))
            throw new IllegalArgumentException("Input does not contain a comma");
        else {
            String[] parts = input.split(",");
            StringBuilder sb = new StringBuilder();

            for (String s : parts)
                sb.append(s).append(", ");

            return sb.substring(0,sb.toString().length() - 2);
        }
    }

    /**
     * Searches Dictionary.com for the provided word.
     * @param word the word to find a definition for
     * @return the definition of the requested word if found
     */
    public static String define(String word) {
        String ret = null;

        try {
            Document doc = Jsoup.connect("https://www.dictionary.com/browse/" + word).get();
            Elements els = doc.getElementsByClass("one-click-content css-nnyc96 e1q3nk1v1").not(".pad_10").not(".pad_20");
            org.jsoup.nodes.Element htmlDescription = els.get(0);

            Document docParsed = Jsoup.parse(String.valueOf(htmlDescription));
            ret = capsFirst(docParsed.text());
        } catch (Exception e) {
            ret = "Definition not found";
            ErrorHandler.silentHandle(e);
        } finally {
            return ret;
        }
    }

    /**
     * Web scrapes Wikipedia for the appropriate article and returns the body of the wiki article.
     * @param query the query to search wikipedia for
     * @return the wiki body result
     */
    public static String wikiSummary(String query) {
        String ret = null;

        try  {
            String urlString = "https://en.wikipedia.org/w/api.php?format=json&action=query" +
                    "&prop=extracts&exintro&explaintext&redirects=1&titles=" +
                    query.replace(" ","%20");
            String jsonString = NetworkUtil.readUrl(urlString);

            String[] serializedPageNumber = jsonString.split("\"extract\":\"");
            ret = serializedPageNumber[1].replace("}","");
            ret = ret.substring(0, ret.length() - 1);
        } catch (Exception e) {
            ErrorHandler.silentHandle(e);
            ret = "Wiki article not found";
        } finally {
            return ret;
        }
    }

    /**
     * Determines whether the given words are anagrams of each other
     * @param wordOne the first word
     * @param wordTwo the second word
     * @return a boolean describing whether or not these words are anagrams
     */
    public static boolean areAnagrams(String wordOne, String wordTwo) {
        char[] W1C = wordOne.toLowerCase().toCharArray();
        char[] W2C = wordTwo.toLowerCase().toCharArray();
        Arrays.sort(W1C);
        Arrays.sort(W2C);

        return Arrays.equals(W1C, W2C);
    }

    //tagged strings for HTML methods

    /**
     * Finds the rawtext and html tags of a string and returns a linked list representing the parts
     * @param htmlText the text containing html tags
     * @return a linked list where each object represents either a complete tag or raw text
     */
    public static LinkedList<TaggedString> getTaggedStrings(String htmlText) {
        //init list for strings by tag
        LinkedList<TaggedString> taggedStrings = new LinkedList<>();

        //figoure out tags
        String textCopy = htmlText;
        while ((textCopy.contains("<") && textCopy.contains(">"))) {
            int firstOpeningTag = textCopy.indexOf("<");
            int firstClosingTag = textCopy.indexOf(">");

            //failsafe
            if (firstClosingTag == -1 || firstOpeningTag == -1 || firstClosingTag < firstOpeningTag)
                break;

            String regularText = textCopy.substring(0, firstOpeningTag);
            String firstHtml = textCopy.substring(firstOpeningTag, firstClosingTag + 1);

            if (regularText.length() > 0)
                taggedStrings.add(new TaggedString(regularText, Tag.TEXT));
            if (firstHtml.length() > 0)
                taggedStrings.add(new TaggedString(firstHtml, Tag.HTML));

            textCopy = textCopy.substring(firstClosingTag + 1);
        }

        //if there's remaining text, it's just non-html
        if (textCopy.length() > 0)
            taggedStrings.add(new TaggedString(textCopy, Tag.TEXT));

        return taggedStrings;
    }

    public enum Tag {
        HTML,TEXT
    }

    public static class TaggedString {
        private String text;
        private Tag tag;

        public TaggedString(String text, Tag tag) {
            this.text = text;
            this.tag = tag;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Tag getTag() {
            return tag;
        }

        public void setTag(Tag tag) {
            this.tag = tag;
        }
    }
}