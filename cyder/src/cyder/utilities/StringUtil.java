package cyder.utilities;

import cyder.handlers.internal.ExceptionHandler;
import cyder.ui.CyderOutputPane;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.swing.text.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Simple general String utility methods along with some JTextPane utility methods
 * Note: these methods are not thread safe and thus this class should be externally synchronized
 * to achieve thread safety. Typically, in Cyder, this is performed via using a {@link CyderOutputPane}
 * which bundles a JTextPane, StringUtil, and Semaphore together.
 */
public class StringUtil {
    /**
     * The output pane to print to in the case an object is created.
     */
    private CyderOutputPane linkedCyderPane = null;

    /**
     * StringUtil instantiation not allowed unless a valid CyderOutputPane is provided.
     */
    private StringUtil() {
        throw new IllegalStateException("Instantiation of StringUtil is not permitted without a CyderOutputPane");
    }

    /**
     * Constructs a StringUtil object with a linked CyderOutputPane.
     *
     * @param cyderOutputPane the CyderOutputPane to link
     */
    public StringUtil(CyderOutputPane cyderOutputPane) {
        if (cyderOutputPane == null)
            throw new IllegalArgumentException("Provided output pane is null");

        this.linkedCyderPane = cyderOutputPane;
    }

    /**
     * Returns the Linked CyderOutputPane.
     *
     * @return the Linked CyderOutputPane
     */
    public CyderOutputPane getLinkedJTextPane() {
        return linkedCyderPane;
    }

    //begin util methods for an instance which require synchronization --------------------------------------

    /**
     * Removes the first object from the linked pane, this could be anything from a Component to a String.
     */
    public synchronized void removeFirst() {
        try {
            linkedCyderPane.getSemaphore().acquire();

            Element root = linkedCyderPane.getJTextPane().getDocument().getDefaultRootElement();
            Element first = root.getElement(0);
            linkedCyderPane.getJTextPane().getDocument().remove(first.getStartOffset(), first.getEndOffset());
            linkedCyderPane.getJTextPane().setCaretPosition(linkedCyderPane.getJTextPane().getDocument().getLength());
            linkedCyderPane.getSemaphore().release();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Removes the last "thing" added to the JTextPane whether it's a component,
     * icon, or string of multi-lined text.
     * In more detail, this method figures out what it'll be removing and then determines how many calls
     * are needed to {@link StringUtil#removeLastLine()}
     */
    public synchronized void removeLast() {
        try {
            boolean removeTwoLines = false;

            LinkedList<Element> elements = new LinkedList<>();
            ElementIterator iterator = new ElementIterator(linkedCyderPane.getJTextPane().getStyledDocument());
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

            linkedCyderPane.getSemaphore().acquire();

            if (removeTwoLines) {
                removeLastLine();
            }

            removeLastLine();

            linkedCyderPane.getSemaphore().release();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Finds the last line of text from the linked output area.
     *
     * @return the last line of raw ASCII text
     */
    public synchronized String getLastTextLine() {
        String text = linkedCyderPane.getJTextPane().getText();
        String[] lines = text.split("\n");
        return lines[lines.length - 1];
    }

    /**
     * Removes the last line added to the linked JTextPane. This could appear to remove nothing,
     *  but really be removing just a newline (line break) character.
     */
    public synchronized void removeLastLine() {
        try {
            LinkedList<Element> elements = new LinkedList<>();
            ElementIterator iterator = new ElementIterator(linkedCyderPane.getJTextPane().getStyledDocument());
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

                    linkedCyderPane.getJTextPane().getStyledDocument().remove(value.getStartOffset(),
                            value.getEndOffset() - value.getStartOffset());
                }
            }
        } catch (BadLocationException ignored) {}
        catch (Exception e) {
            ExceptionHandler.handle(e);
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
    public synchronized void printComponent(Component c, String nm, String str) {
        try {
            Style cs = linkedCyderPane.getJTextPane().getStyledDocument().addStyle(nm, null);
            StyleConstants.setComponent(cs, c);
            linkedCyderPane.getJTextPane().getStyledDocument()
                    .insertString(linkedCyderPane.getJTextPane().getStyledDocument().getLength(), str, cs);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Adds a {@link Component} to the linked JTextPane. Make sure all listeners, bounds,
     *  modifiers, etc. have been set before printing the component.
     * @param c the component to append to the pane
     */
    public synchronized void printComponent(Component c) {
        try {
            String componentUUID = SecurityUtil.generateUUID();
            Style cs = linkedCyderPane.getJTextPane().getStyledDocument().addStyle(componentUUID, null);
            StyleConstants.setComponent(cs, c);
            linkedCyderPane.getJTextPane().getStyledDocument()
                    .insertString(linkedCyderPane.getJTextPane().getStyledDocument().getLength(), componentUUID, cs);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Adds a {@link Component} to the linked JTextPane. Make sure all listeners, bounds,
     *  modifiers, etc. have been set before printing the component. Following the component print,
     *  a new line is appended to the pane.
     * @param c the component to append to the pane
     */
    public synchronized void printlnComponent(Component c) {
        try {
            String componentUUID = SecurityUtil.generateUUID();
            printComponent(c, componentUUID, componentUUID);
            println("");
        } catch (Exception e) {
            ExceptionHandler.handle(e);
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
    public synchronized void printlnComponent(Component c, String nm, String str) {
        try {
            printComponent(c, nm, str);
            println("");
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    public synchronized void print(String Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedCyderPane.getJTextPane().getDocument();
            document.insertString(document.getLength(), Usage, null);
            linkedCyderPane.getJTextPane().setCaretPosition(linkedCyderPane.getJTextPane().getDocument().getLength());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    public synchronized void print(int Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedCyderPane.getJTextPane().getDocument();
            document.insertString(document.getLength(), Integer.toString(Usage), null);
            linkedCyderPane.getJTextPane().setCaretPosition(linkedCyderPane.getJTextPane().getDocument().getLength());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    public synchronized void print(double Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedCyderPane.getJTextPane().getDocument();
            document.insertString(document.getLength(), Double.toString(Usage), null);
            linkedCyderPane.getJTextPane().setCaretPosition(linkedCyderPane.getJTextPane().getDocument().getLength());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    public synchronized void print(boolean Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedCyderPane.getJTextPane().getDocument();
            document.insertString(document.getLength(), Boolean.toString(Usage), null);
            linkedCyderPane.getJTextPane().setCaretPosition(linkedCyderPane.getJTextPane().getDocument().getLength());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    public synchronized void print(float Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedCyderPane.getJTextPane().getDocument();
            document.insertString(document.getLength(), Float.toString(Usage), null);
            linkedCyderPane.getJTextPane().setCaretPosition(linkedCyderPane.getJTextPane().getDocument().getLength());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    public synchronized void print(long Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedCyderPane.getJTextPane().getDocument();
            document.insertString(document.getLength(), Long.toString(Usage), null);
            linkedCyderPane.getJTextPane().setCaretPosition(linkedCyderPane.getJTextPane().getDocument().getLength());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    public synchronized void print(char Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedCyderPane.getJTextPane().getDocument();
            document.insertString(document.getLength(), String.valueOf(Usage), null);
            linkedCyderPane.getJTextPane().setCaretPosition(linkedCyderPane.getJTextPane().getDocument().getLength());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    public synchronized void print(Object Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedCyderPane.getJTextPane().getDocument();
            document.insertString(document.getLength(), Usage.toString(), null);
            linkedCyderPane.getJTextPane().setCaretPosition(linkedCyderPane.getJTextPane().getDocument().getLength());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    public synchronized  void println(String Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedCyderPane.getJTextPane().getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            linkedCyderPane.getJTextPane().setCaretPosition(linkedCyderPane.getJTextPane().getDocument().getLength());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    public synchronized void println(int Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedCyderPane.getJTextPane().getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            linkedCyderPane.getJTextPane().setCaretPosition(linkedCyderPane.getJTextPane().getDocument().getLength());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    public synchronized void println(double Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedCyderPane.getJTextPane().getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            linkedCyderPane.getJTextPane().setCaretPosition(linkedCyderPane.getJTextPane().getDocument().getLength());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    public synchronized void println(boolean Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedCyderPane.getJTextPane().getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            linkedCyderPane.getJTextPane().setCaretPosition(linkedCyderPane.getJTextPane().getDocument().getLength());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    public synchronized void println(float Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedCyderPane.getJTextPane().getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            linkedCyderPane.getJTextPane().setCaretPosition(linkedCyderPane.getJTextPane().getDocument().getLength());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    public synchronized void println(long Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedCyderPane.getJTextPane().getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            linkedCyderPane.getJTextPane().setCaretPosition(linkedCyderPane.getJTextPane().getDocument().getLength());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    public synchronized void println(char Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedCyderPane.getJTextPane().getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            linkedCyderPane.getJTextPane().setCaretPosition(linkedCyderPane.getJTextPane().getDocument().getLength());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    public synchronized void println(Object Usage) {
        try {
            StyledDocument document = (StyledDocument) linkedCyderPane.getJTextPane().getDocument();
            document.insertString(document.getLength(), Usage.toString() + "\n", null);
            linkedCyderPane.getJTextPane().setCaretPosition(linkedCyderPane.getJTextPane().getDocument().getLength());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Prints the object array to {@link this} object's connected output area
     * @param arr the array of objects to print
     */
    public synchronized void printArr(Object[] arr) {
        try {
            for (Object o : arr)
                println(o);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    //end methods which require instantiation/synchronization

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
     * Determines the proper english grammar when attempting to use possession on a string that typically
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
     * Fills a string with the provided character to result in a string of the specified length.
     *
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
     * Determines if a word is palindrome (spelled the same forward and backwards like ogopogo and racecar).
     *
     * @param word the word to check
     * @return the result of the comparison
     */
    public static boolean isPalindrome(String word) {
        return Arrays.equals(word.toLowerCase().toCharArray(), reverseArray(word.toLowerCase().toCharArray()));
    }

    /**
     * Concatenates two arrays together.
     *
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
     * Converts the first character in a string to the
     * capital version if the character is a standard latin character.
     *
     * @param word the word to capitalize the first letter of
     * @return the resultant string
     */
    public static String capsFirst(String word) {
        if (word.length() == 0)
            return word;

        StringBuilder sb = new StringBuilder(word.length());
        String[] words = word.split("\\s+");

        for (String wordy : words) {
            sb.append(Character.toUpperCase(wordy.charAt(0)));
            sb.append(wordy.substring(1).toLowerCase()).append(" ");
        }

        return sb.toString().trim();
    }


    /**
     * Filters out simple leet speech from the provided string.
     *
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
            sb.append(replaceLeet(word)).append(" ");
        }

        return sb.toString().trim();
    }

    /**
     * Inner filtering of leet speech for words specifically, this is the main driver method that does the magic.
     *
     * @param word the word to filter leet out of.
     * @return the word having leet removed to the best of our abilities
     */
    private static String replaceLeet(String word) {
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
     * Tests whether the provided string has the provided word inside it.
     *
     * @param userInput the master string to search through
     * @param findWord the word to search the master string for
     * @return a boolean depicting whether the given string contains the test word
     */
    public static boolean hasWord(String userInput, String findWord) {
        return hasWord(userInput, findWord, false);
    }

    /**
     * Tests whether the provided string has the provided word inside it.
     *
     * @param userInput the master string to search through
     * @param findWord the word to search the master string for
     * @param removeComments whether to remove comment tags from the input
     * @return a boolean depicting whether the given string contains the test word
     */
    public static boolean hasWord(String userInput, String findWord, boolean removeComments) {
        if (userInput == null || findWord == null)
            throw new IllegalArgumentException("Provided input is null: userInput = "
                    + userInput + ", word = " + findWord);

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
     * Tests a given string to see if it contains any blocked words contained in the v.txt system file.
     *
     * @param input the provided string to test against
     * @param filterLeet whether to filter out possible leet from the string
     * @return a boolean describing whether the filter was triggered by the input
     */
    public static boolean containsBlockedWords(String input, boolean filterLeet) {
        boolean ret = false;

        if (filterLeet)
            input = filterLeet(input.toLowerCase());

        try (BufferedReader vReader = new BufferedReader(new FileReader("static/txt/v.txt"))) {
            String blockedWord;

            while ((blockedWord = vReader.readLine()) != null) {
                if (hasWord(input, blockedWord, true)) {
                    ret = true;
                    break;
                }
            }
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }

        return ret;
    }

    /**
     * Provides the exact string object but with the first character converted to lowercase.
     *
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
     *
     * @param str the string ot count the words of
     * @return the word count of the requested string
     */
    public static int countWords(String str) {
        return (str == null || str.isEmpty()) ? 0 : str.split("\\s+").length;
    }

    /**
     * Ensures that there is a space after every comma within the input.
     *
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

            return sb.substring(0, sb.toString().length() - 2);
        }
    }

    /**
     * Searches Dictionary.com for the provided word.
     *
     * @param word the word to find a definition for
     * @return the definition of the requested word if found
     */
    public static String getDefinition(String word) {
        String ret = null;

        try {
            Document doc = Jsoup.connect("https://www.dictionary.com/browse/" + word).get();
            Elements els = doc.getElementsByClass("one-click-content css-nnyc96 e1q3nk1v1")
                    .not(".pad_10").not(".pad_20");
            org.jsoup.nodes.Element htmlDescription = els.get(0);

            Document docParsed = Jsoup.parse(String.valueOf(htmlDescription));
            ret = capsFirst(docParsed.text());
        } catch (Exception e) {
            ret = "Definition not found";
            ExceptionHandler.silentHandle(e);
        }

        return ret;
    }

    /**
     * Web scrapes Wikipedia for the appropriate article and returns the body of the wiki article.
     *
     * @param query the query to search wikipedia for
     * @return the wiki body result
     */
    public static String getWikipediaSummary(String query) {
        String ret;

        try  {
            String urlString = "https://en.wikipedia.org/w/api.php?format=json&action=query" +
                    "&prop=extracts&exintro&explaintext&redirects=1&titles=" +
                    query.replace(" ","%20");
            String jsonString = NetworkUtil.readUrl(urlString);

            String[] serializedPageNumber = jsonString.split("\"extract\":\"");
            ret = serializedPageNumber[1].replace("}","");
            ret = ret.substring(0, ret.length() - 1);
        } catch (Exception e) {
            ExceptionHandler.silentHandle(e);
            ret = "Wiki article not found";
        }

        return ret;
    }

    /**
     * Determines whether the given words are anagrams of each other.
     *
     * @param wordOne the first word
     * @param wordTwo the second word
     * @return a boolean describing whether these words are anagrams
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
     * Finds the rawtext and html tags of a string and returns a linked list representing the parts.
     *
     * @param htmlText the text containing html tags
     * @return a linked list where each object represents either a complete tag or raw text
     */
    public static LinkedList<TaggedString> getTaggedStrings(String htmlText) {
        //init list for strings by tag
        LinkedList<TaggedString> taggedStrings = new LinkedList<>();

        //figure out tags
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
                taggedStrings.add(new TaggedString(regularText, TaggedStringType.TEXT));
            if (firstHtml.length() > 0)
                taggedStrings.add(new TaggedString(firstHtml, TaggedStringType.HTML));

            textCopy = textCopy.substring(firstClosingTag + 1);
        }

        //if there's remaining text, it's just non-html
        if (textCopy.length() > 0)
            taggedStrings.add(new TaggedString(textCopy, TaggedStringType.TEXT));

        return taggedStrings;
    }

    /**
     * The type a given String is: HTML or TEXT
     */
    public enum TaggedStringType {
        HTML,TEXT
    }

    /**
     * Class representing a segment of text as either being raw text or an html tag
     */
    public static class TaggedString {
        private String text;
        private TaggedStringType type;

        public TaggedString(String text, TaggedStringType type) {
            this.text = text;
            this.type = type;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public TaggedStringType getType() {
            return type;
        }

        public void setType(TaggedStringType type) {
            this.type = type;
        }
    }

    /**
     * Determines if the provided String is null meaning literally null,
     * empty (length 0), equal to NULL, or equal to NUL.
     *
     * @param nullCheck the String to test for
     * @return whether the provided String was null
     */
    public static boolean isNull(String nullCheck) {
        if (nullCheck == null)
            return true;

        nullCheck = nullCheck.trim();

        return  nullCheck.length() == 0 ||
                nullCheck.equalsIgnoreCase("NUL") ||
                nullCheck.equalsIgnoreCase("NULL");
    }

    /**
     * Determines how closely string alpha is to string beta.
     *
     * @param alpha the base string
     * @param beta the string to test for similarity against alpha
     * @return how close beta is to alpha by a percentage score
     */
    public static int levenshteinDistance(String alpha, String beta) {
        if (alpha.isEmpty()) {
            return beta.length();
        }

        if (beta.isEmpty()) {
            return alpha.length();
        }

        int substitution = levenshteinDistance(alpha.substring(1), beta.substring(1))
                + alpha.charAt(0) == beta.charAt(0) ? 0 : 1;

        int insertion = levenshteinDistance(alpha, beta.substring(1)) + 1;
        int deletion = levenshteinDistance(alpha.substring(1), beta) + 1;

        return Arrays.stream(
                new int[] {substitution, insertion, deletion}).min().orElse(Integer.MAX_VALUE);
    }

    /**
     * Returns the length of the non-html text of the provided String.
     *
     * @param htmlText the text containing html tags and styling
     * @return the length of the non-html text
     */
    public static int getRawTextLength(String htmlText) {
        int length = 0;

        LinkedList<TaggedString> taggedStrings = getTaggedStrings(htmlText);

        if (taggedStrings.isEmpty()) {
            length = htmlText.length();
        } else {
            for (TaggedString ts : taggedStrings) {
                if (ts.getType() == TaggedStringType.TEXT)
                    length += ts.getText().length();
            }
        }

        return length;
    }

    /**
     * Returns the provided text trimmed and with all occurrences
     * of whitespace replaced with one whitespace char.
     *
     * @param text the text to trim
     * @return the text after trimming operations have been performed
     */
    public static String getTrimmedText(String text) {
        return text.trim().replaceAll("\\s+", " ");
    }

    /**
     * Returns the provided string with each word converted to
     * lowercase except for the first char of each word.
     *
     * @param text the text to convert to standard caps form
     * @return the converted text
     */
    public static String capsCheck(String text) {
        String[] words = text.trim().split("\\s+");

        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            sb.append(capsFirst(word)).append(" ");
        }

        return sb.toString().trim();
    }

    /**
     * Returns whether the provided string is in the listed strings.
     *
     * @param lookFor the string to search the list for
     * @param strings the list of strings
     * @return whether the provided string is in the list of strings
     */
    public static boolean in(String lookFor, String... strings) {
        return in(lookFor, false, strings);
    }

    /**
     * Returns whether the provided string is in the listed strings.
     *
     * @param lookFor the string to look for
     * @param strings the list of strings
     * @param ignoreCase whether to ignore the case of the words
     * @return whether the provided string is in the list of strings
     */
    public static boolean in(String lookFor, boolean ignoreCase, String... strings) {
        for (String look : strings) {
            if ((ignoreCase && lookFor.equalsIgnoreCase(look)) || lookFor.equals(look)) {
               return true;
            }
        }

        return false;
    }

    /**
     * Returns whether the provided string is in the listed strings.
     *
     * @param lookFor the string to look for
     * @param strings the list of strings
     * @param ignoreCase whether to ignore the case of the words
     * @return whether the provided string is in the list of strings
     */
    public static boolean in(String lookFor, boolean ignoreCase, ArrayList<String> strings) {
        for (String look : strings) {
            if ((ignoreCase && lookFor.equalsIgnoreCase(look)) || lookFor.equals(look)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the minimum width required for the given String using the given font.
     *
     * @param title the text you want to determine the width of
     * @param f the font for the text
     * @return an integer value determining the minimum width of
     * a string of text (10 is added to avoid ... bug)
     */
    public static int getMinWidth(String title, Font f) {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) f.getStringBounds(title, frc).getWidth() + 10;
    }

    /**
     * Returns the minimum width required for the given String using the given font.
     *
     * @param title the text you want to determine the width of
     * @param f the font for the text
     * @return an integer value determining the minimum width of a string of text
     */
    public static int getAbsoluteMinWidth(String title, Font f) {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) f.getStringBounds(title, frc).getWidth();
    }

    /**
     * Returns the minimum height required for the given String using the given font.
     *
     * @param title the text you want to determine the height of
     * @return an integer value determining the minimum height
     * of a string of text (10 is added to avoid ... bug)
     */
    public static int getMinHeight(String title, Font f) {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) f.getStringBounds(title, frc).getHeight() + 10;
    }

    /**
     * Returns the minimum height required for the given String
     * using the given font without adding 10.
     *
     * @param title the text you want to determine the height of
     * @return an integer value determining the minimum height of a string of text
     */
    public static int getAbsoluteMinHeight(String title, Font f) {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) f.getStringBounds(title, frc).getHeight();
    }
}