package cyder.utils;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.constants.CyderColors;
import cyder.constants.CyderUrls;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.math.NumberUtil;
import cyder.ui.pane.CyderOutputPane;
import org.atteo.evo.inflector.English;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String utility methods along with some JTextPane utility methods
 * <p>
 * Note: these methods are not thread safe and thus this class should be externally synchronized
 * to achieve thread safety. Typically, in Cyder, this is performed via using a {@link CyderOutputPane}
 * which bundles a JTextPane, StringUtil, and Semaphore.
 */
public class StringUtil {
    /**
     * The output pane to print to in the case an object is created.
     */
    private final CyderOutputPane linkedCyderPane;

    /**
     * The error message if the private constructor is invoked via reflection.
     */
    private static final String INSTANTIATION_MESSAGE
            = "Instantiation of StringUtil is not permitted without a CyderOutputPane";

    /**
     * Suppress default constructor.
     */
    private StringUtil() {
        throw new IllegalMethodException(INSTANTIATION_MESSAGE);
    }

    /**
     * Constructs a StringUtil object with a linked CyderOutputPane.
     *
     * @param cyderOutputPane the CyderOutputPane to link
     */
    public StringUtil(CyderOutputPane cyderOutputPane) {
        Preconditions.checkNotNull(cyderOutputPane);

        linkedCyderPane = cyderOutputPane;
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

            ArrayList<Element> elements = new ArrayList<>();
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

                    if (value.toString().toLowerCase().contains("icon") ||
                            value.toString().toLowerCase().contains("component")) {
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

        if (lines.length < 1) {
            throw new FatalException("Linked pane contains no text lines");
        }

        return lines[lines.length - 1];
    }

    /**
     * Removes the last line added to the linked JTextPane. This could appear to remove nothing,
     * but really be removing just a newline (line break) character.
     */
    public synchronized void removeLastLine() {
        try {
            ArrayList<Element> elements = new ArrayList<>();
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
        } catch (BadLocationException e) {
            ExceptionHandler.silentHandle(e);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /*
    Start generic print methods for the linked JTextPane, these are not thread safe by default.
    See Console's outputArea and implementation there for thread safety.
     */

    /**
     * Adds a {@link Component} to the linked JTextPane. Make sure all listeners, bounds,
     * modifiers, etc. have been set before printing the component.
     *
     * @param component the component to append to the pane
     * @param name      the name identifier for the style
     * @param stringId  the string identifier for the underlying insert string call
     */
    public synchronized void printComponent(Component component, String name, String stringId) {
        Preconditions.checkNotNull(component);
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(stringId);

        try {
            Style style = linkedCyderPane.getJTextPane().getStyledDocument().addStyle(name, null);
            StyleConstants.setComponent(style, component);
            linkedCyderPane.getJTextPane().getStyledDocument()
                    .insertString(linkedCyderPane.getJTextPane().getStyledDocument().getLength(), stringId, style);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Adds a {@link Component} to the linked JTextPane. Make sure all listeners, bounds,
     * modifiers, etc. have been set before printing the component.
     *
     * @param component the component to append to the pane
     */
    public synchronized void printComponent(Component component) {
        Preconditions.checkNotNull(component);

        try {
            String componentUuid = SecurityUtil.generateUuid();
            Style cs = linkedCyderPane.getJTextPane().getStyledDocument().addStyle(componentUuid, null);
            StyleConstants.setComponent(cs, component);
            linkedCyderPane.getJTextPane().getStyledDocument()
                    .insertString(linkedCyderPane.getJTextPane().getStyledDocument().getLength(), componentUuid, cs);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Adds a {@link Component} to the linked JTextPane. Make sure all listeners, bounds,
     * modifiers, etc. have been set before printing the component. Following the component print,
     * a new line is appended to the pane.
     *
     * @param component the component to append to the pane
     */
    public synchronized void printlnComponent(Component component) {
        Preconditions.checkNotNull(component);

        try {
            String componentUuid = SecurityUtil.generateUuid();
            printComponent(component, componentUuid, componentUuid);
            println("");
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Adds a {@link Component} to the linked JTextPane. Make sure all listeners, bounds,
     * modifiers, etc. have been set before printing the component. Following the component print,
     * a new line is appended to the pane.
     *
     * @param component the component to append to the pane
     * @param name      the name identifier for the style
     * @param stringId  the string identifier for the underlying insert string call
     */
    public synchronized void printlnComponent(Component component, String name, String stringId) {
        Preconditions.checkNotNull(component);
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(stringId);

        try {
            printComponent(component, name, stringId);
            println("");
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Prints the provided generic to the linked JTextPane.
     *
     * @param print the provided generic to print to the linked JTextPane
     * @param <T>   the type of the generic
     */
    public synchronized <T> void print(T print) {
        try {
            StyledDocument document = (StyledDocument) linkedCyderPane.getJTextPane().getDocument();
            document.insertString(document.getLength(), print.toString(), null);
            linkedCyderPane.getJTextPane().setCaretPosition(linkedCyderPane.getJTextPane().getDocument().getLength());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Prints the provided generic to the linked JTextPane followed by a newline.
     *
     * @param print the provided generic to print to the linked JTextPane
     * @param <T>   the type of the generic
     */
    public synchronized <T> void println(T print) {
        try {
            StyledDocument document = (StyledDocument) linkedCyderPane.getJTextPane().getDocument();
            document.insertString(document.getLength(), print.toString() + "\n", null);
            linkedCyderPane.getJTextPane().setCaretPosition(linkedCyderPane.getJTextPane().getDocument().getLength());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Prints the object array to {@link this} object's JTextPane.
     *
     * @param arr the array of objects to print
     */
    public synchronized void printLines(Object[] arr) {
        for (Object o : arr) {
            println(o);
        }
    }

    /**
     * Prints a newline to the linked JTextPane.
     */
    public synchronized void newline() {
        println("");
    }

    /**
     * Prints a newline to the linked JTextPane if the condition is true.
     *
     * @param condition the condition which needs to evaluate to true before printing a newline
     */
    public synchronized void newline(boolean condition) {
        if (condition) {
            println("");
        }
    }

    /**
     * Prints a separator surrounded by newlines to the linked JTextPane.
     */
    public synchronized void printSeparator() {
        printlnComponent(getMenuSeparator());
        newline();
    }

    // -----------------------------------------------------------
    // end methods which require instantiation and synchronization
    // -----------------------------------------------------------

    /**
     * The text used to generate a menu separation label.
     */
    private static final String magicMenuSepText = "NateCheshire";

    private static final int menuSepX = 0;
    private static final int menuSepY = 7;
    private static final int menuSepWidth = 175;
    private static final int menuSepHeight = 5;

    /**
     * The bounds for a menu separation label.
     */
    private static final Rectangle menuSepBounds = new Rectangle(menuSepX, menuSepY, menuSepWidth, menuSepHeight);

    /**
     * The default color of menu separator components.
     */
    private static final Color DEFAULT_MENU_SEP_COLOR = CyderColors.vanilla;

    /**
     * Returns a menu separator label.
     *
     * @return a menu separator label
     */
    private JLabel getMenuSeparator() {
        return getMenuSeparator(DEFAULT_MENU_SEP_COLOR);
    }

    /**
     * Returns a menu separator label.
     *
     * @return a menu separator label
     */
    @SuppressWarnings("SameParameterValue")
    private JLabel getMenuSeparator(Color color) {
        Preconditions.checkNotNull(color);

        JLabel sepLabel = new JLabel(magicMenuSepText) {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(getForeground());
                g.fillRect(
                        (int) menuSepBounds.getX(),
                        (int) menuSepBounds.getY(),
                        (int) menuSepBounds.getWidth(),
                        (int) menuSepBounds.getHeight());
                g.dispose();
            }
        };
        sepLabel.setForeground(color);
        return sepLabel;
    }

    /**
     * Reverses the given array
     *
     * @param Array the array to reverse
     * @return the reversed array
     */
    public static char[] reverseArray(char[] Array) {
        String reverse = new StringBuilder(new String(Array)).reverse().toString();
        return reverse.toCharArray();
    }

    /**
     * Determines the proper english grammar when attempting to use possession
     * on a string that represents a noun.
     *
     * @param name the proper singular form of the noun
     * @return the string to be appended to the proper noun ('s or simply ')
     */
    public static String getApostrophe(String name) {
        if (name.endsWith("s")) {
            return "'";
        } else {
            return "'s";
        }
    }

    /**
     * Returns the form of the word.
     *
     * @return the form of the word
     */
    public static String getPlural(String word) {
        Preconditions.checkNotNull(word);
        Preconditions.checkArgument(!word.isEmpty());

        return English.plural(word);
    }

    /**
     * Returns the form of the word depending on the number of items.
     * A singular item doesn't need to be made plural whilst any number of objects other
     * than one should be converted to plural using standard English Language rules.
     *
     * @param num  the number of items associated with the word
     * @param word the word to be converted to plural
     * @return the plural form of the word
     */
    public static String getPlural(int num, String word) {
        Preconditions.checkNotNull(word);

        if (num == 1) {
            return word;
        } else if (word.endsWith("s")) {
            return word + "es";
        } else {
            return word + "s";
        }
    }

    /**
     * Fills a string with the provided character to result in a string of the specified length.
     *
     * @param count     the length of the resultant string
     * @param character the character to fill the string with
     * @return the resultant filled array
     */
    public static String fillString(int count, String character) {
        Preconditions.checkNotNull(character);

        return character.repeat(Math.max(0, count));
    }

    /**
     * Determines if a word is palindrome (spelled the same forward and backwards like ogopogo and racecar).
     *
     * @param word the word to check
     * @return the result of the comparison
     */
    public static boolean isPalindrome(String word) {
        Preconditions.checkNotNull(word);

        return Arrays.equals(word.toLowerCase().toCharArray(), reverseArray(word.toLowerCase().toCharArray()));
    }

    /**
     * Converts the first character of all words in the string ot the
     * capital version if the character is a standard latin character.
     *
     * @param word the word to capitalize the first letter of
     * @return the resultant string
     */
    public static String capsFirstWords(String word) {
        Preconditions.checkNotNull(word);
        Preconditions.checkArgument(!word.isEmpty());

        StringBuilder sb = new StringBuilder(word.length());
        String[] words = word.split("\\s+");

        Arrays.stream(words).forEach(wordy -> {
            sb.append(Character.toUpperCase(wordy.charAt(0)));
            sb.append(wordy.substring(1).toLowerCase()).append(" ");
        });

        return sb.toString().trim();
    }

    /**
     * Converts the first character to upper case provided it is a standard latin character.
     *
     * @param word the word to capitalize the first letter of
     * @return the capitalized word
     */
    public static String capsFirst(String word) {
        Preconditions.checkNotNull(word);

        return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
    }

    /**
     * Filters out simple leet speech from the provided string.
     *
     * @param filter the word to filter leet out of
     * @return the resultant string after filtering
     */
    public static String filterLeet(String filter) {
        Preconditions.checkNotNull(filter);

        if (filter.isEmpty()) {
            return filter;
        }

        //split at spaces and run leet in each of those
        String[] words = filter.split("\\s+");

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
     * @param word the word to filter leet out of
     * @return the word having leet removed to the best of our abilities
     */
    private static String replaceLeet(String word) {
        char[] chars = word.toLowerCase().toCharArray();

        for (int i = 0 ; i < chars.length ; i++) {
            char c = chars[i];

            /*
            Ideally this should be redone or use a library.
            I'd like to implement the rules from this table at some point:
            https://cleanspeak.com/images/blog/leet-wiki-table.png
             */

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
            else if (c == '9')
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
     * @param findWord  the word to search the master string for
     * @return a boolean depicting whether the given string contains the test word
     */
    public static boolean hasWord(String userInput, String findWord) {
        return hasWord(userInput, findWord, false);
    }

    /**
     * Tests whether the provided string has the provided word inside it.
     *
     * @param userInput      the master string to search through
     * @param findWord       the word to search the master string for
     * @param removeComments whether to remove comment tags from the input
     * @return a boolean depicting whether the given string contains the test word
     */
    public static boolean hasWord(String userInput, String findWord, boolean removeComments) {
        Preconditions.checkNotNull(userInput);
        Preconditions.checkArgument(!userInput.isEmpty());
        Preconditions.checkNotNull(findWord);
        Preconditions.checkArgument(!findWord.isEmpty());

        if (removeComments) {
            userInput = userInput.replace("//", "")
                    .replace("/*", "")
                    .replace("*/", "")
                    .replace("*", "");
        }

        userInput = userInput.toLowerCase();
        findWord = findWord.toLowerCase();

        String[] words = userInput.split("\\s+");

        for (String word : words) {
            if (word.equalsIgnoreCase(findWord)) {
                return true;
            }
        }

        return false;
    }

    /**
     * The list of blocked words as found from the static file "blocked.txt".
     */
    private static final ImmutableList<String> BLOCKED_WORDS;

    static {
        LinkedList<String> blockedWords = new LinkedList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(
                StaticUtil.getStaticResource("blocked.txt")))) {
            String blockedWord;

            while ((blockedWord = reader.readLine()) != null) {
                blockedWords.add(blockedWord);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        BLOCKED_WORDS = ImmutableList.copyOf(blockedWords);
    }

    /**
     * A record for holding information regarding a located bad word.
     */
    public record BlockedWordResult(boolean failed, String triggerWord) {}

    /**
     * Tests a given string to see if it contains any blocked words contained in the blocked.txt system file.
     *
     * @param input      the provided string to test against
     * @param filterLeet whether to filter out possible leet from the string
     * @return a result object describing whether a bad word was found and if so, which
     */
    public static BlockedWordResult containsBlockedWords(String input, boolean filterLeet) {
        Preconditions.checkNotNull(input);
        Preconditions.checkArgument(!input.isEmpty());

        if (filterLeet) {
            input = filterLeet(input.toLowerCase());
        }

        for (String blockedWord : BLOCKED_WORDS) {
            if (hasWord(input, blockedWord, true)) {
                return new BlockedWordResult(true, blockedWord);
            }
        }

        return new BlockedWordResult(false, "");
    }

    /**
     * Provides the exact string object but with the first character converted to lowercase.
     *
     * @param str the string to convert the first character to lowercase
     * @return the resultant string
     */
    public static String firstCharToLowerCase(String str) {
        if (isNullOrEmpty(str)) {
            return "";
        }

        if (str.length() == 1) {
            return str.toLowerCase();
        } else {
            return str.substring(0, 1).toLowerCase() + str.substring(1);
        }
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
        Preconditions.checkNotNull(input);
        Preconditions.checkArgument(!input.isEmpty());

        String[] parts = input.split(",");
        StringBuilder sb = new StringBuilder();

        for (int i = 0 ; i < parts.length ; i++) {
            sb.append(parts[i]);

            if (i != parts.length - 1) {
                sb.append(", ");
            }
        }

        return sb.toString();

    }

    /**
     * The name for the element to grab from the DOM returned when getting a Dictionary.com html document.
     */
    @SuppressWarnings("SpellCheckingInspection")
    private static final String DEFINITION_ELEMENT_NAME = "one-click-content css-nnyc96 e1q3nk1v1";

    /**
     * The dictionary base url.
     */
    private static final String DICTIONARY_BASE = "https://www.dictionary.com/browse/";

    /**
     * Searches Dictionary.com for the provided word.
     *
     * @param word the word to find a definition for
     * @return the definition of the requested word if found
     */
    public static String getDefinition(String word) {
        String ret;

        try {
            Document doc = Jsoup.connect(DICTIONARY_BASE + word).get();
            Elements els = doc.getElementsByClass(DEFINITION_ELEMENT_NAME)
                    .not(".pad_10").not(".pad_20");
            org.jsoup.nodes.Element htmlDescription = els.get(0);

            Document docParsed = Jsoup.parse(String.valueOf(htmlDescription));
            ret = capsFirstWords(docParsed.text());
        } catch (HttpStatusException e) {
            ret = "Word not found; check your spelling";
        } catch (Exception e) {
            ret = "Definition not found";
            ExceptionHandler.silentHandle(e);
        }

        return ret;
    }

    /**
     * The additional part for a wikipedia summary scrape.
     */
    @SuppressWarnings("SpellCheckingInspection")
    private static final String WIKI_SUM_PROP = "&prop=extracts&exintro&explaintext&redirects=1&titles=";

    /**
     * Web scrapes Wikipedia for the appropriate article and returns the body of the wiki article.
     *
     * @param query the query to search wikipedia for
     * @return the wiki body result
     */
    public static String getWikipediaSummary(String query) {
        String ret;

        try {
            String urlString = CyderUrls.WIKIPEDIA_SUMMARY_BASE
                    + WIKI_SUM_PROP + query.replace(" ", NetworkUtil.URL_SPACE);
            String jsonString = NetworkUtil.readUrl(urlString);

            String[] serializedPageNumber = jsonString.split("\"extract\":\"");
            ret = serializedPageNumber[1].replace("}", "");
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
        Preconditions.checkNotNull(wordOne);
        Preconditions.checkNotNull(wordTwo);

        char[] wordOneArr = wordOne.toLowerCase().toCharArray();
        char[] wordTwoArr = wordTwo.toLowerCase().toCharArray();

        Arrays.sort(wordOneArr);
        Arrays.sort(wordTwoArr);

        return Arrays.equals(wordOneArr, wordTwoArr);
    }

    /**
     * Finds the rawtext and html tags of a string and returns a linked list representing the parts.
     *
     * @param htmlText the text containing html tags
     * @return a linked list where each object represents either a complete tag or raw text
     */
    public static ImmutableList<BoundsUtil.TaggedString> getTaggedStrings(String htmlText) {
        Preconditions.checkNotNull(htmlText);
        Preconditions.checkArgument(!htmlText.isEmpty());

        LinkedList<BoundsUtil.TaggedString> taggedStrings = new LinkedList<>();

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

            if (!regularText.isEmpty())
                taggedStrings.add(new BoundsUtil.TaggedString(regularText, BoundsUtil.TaggedString.Type.TEXT));
            if (!firstHtml.isEmpty())
                taggedStrings.add(new BoundsUtil.TaggedString(firstHtml, BoundsUtil.TaggedString.Type.HTML));

            textCopy = textCopy.substring(firstClosingTag + 1);
        }

        //if there's remaining text, it's just non-html
        if (!textCopy.isEmpty())
            taggedStrings.add(new BoundsUtil.TaggedString(textCopy, BoundsUtil.TaggedString.Type.TEXT));

        return ImmutableList.copyOf(taggedStrings);
    }

    /**
     * The list of strings which are counted as null for comparisons by {@link #isNullOrEmpty(String)}.
     */
    public static final ImmutableList<String> NULL_STRINGS = ImmutableList.of("NULL", "NUL");

    /**
     * Determines if the provided String is null meaning literally null,
     * empty (length 0), or contained in {@link #NULL_STRINGS}.
     *
     * @param string the String to test for
     * @return whether the provided String was null
     */
    public static boolean isNullOrEmpty(String string) {
        if (string == null) {
            return true;
        }

        string = string.replaceAll("\\s+", "");
        return string.isEmpty() || in(string, true, NULL_STRINGS);
    }

    /**
     * Returns the length of the non-html text of the provided String.
     *
     * @param htmlText the text containing html tags and styling
     * @return the length of the non-html text
     */
    public static int getRawTextLength(String htmlText) {
        Preconditions.checkNotNull(htmlText);
        Preconditions.checkArgument(!htmlText.isEmpty());

        int length = 0;

        ImmutableList<BoundsUtil.TaggedString> taggedStrings = getTaggedStrings(htmlText);

        if (taggedStrings.isEmpty()) {
            length = htmlText.length();
        } else {
            for (BoundsUtil.TaggedString ts : taggedStrings) {
                if (ts.type() == BoundsUtil.TaggedString.Type.TEXT) {
                    length += ts.text().length();
                }
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
        Preconditions.checkNotNull(text);

        return text.trim().replaceAll("\\s+", " ");
    }

    /**
     * Returns whether the provided string is in the listed strings.
     *
     * @param lookFor the string to search the list for
     * @param strings the list of strings
     * @return whether the provided string is in the list of strings
     */
    public static boolean in(String lookFor, String... strings) {
        Preconditions.checkNotNull(lookFor);
        Preconditions.checkArgument(!lookFor.isEmpty());

        return in(lookFor, false, strings);
    }

    /**
     * Returns whether the provided string is in the listed strings.
     *
     * @param lookFor    the string to look for
     * @param strings    the list of strings
     * @param ignoreCase whether to ignore the case of the words
     * @return whether the provided string is in the list of strings
     */
    public static boolean in(String lookFor, boolean ignoreCase, String... strings) {
        Preconditions.checkNotNull(lookFor);

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
     * @param lookFor    the string to look for
     * @param strings    the list of strings
     * @param ignoreCase whether to ignore the case of the words
     * @return whether the provided string is in the list of strings
     */
    public static boolean in(String lookFor, boolean ignoreCase, Collection<String> strings) {
        Preconditions.checkNotNull(lookFor);

        for (String look : strings) {
            if ((ignoreCase && lookFor.equalsIgnoreCase(look)) || lookFor.equals(look)) {
                return true;
            }
        }

        return false;
    }

    /**
     * The amount added to {@link #getMinWidth(String, Font)} and
     * {@link #getMinHeight(String, Font)} to account for possible weird bugs.
     */
    public static final int SIZE_ADDITIVE = 10;

    /**
     * Returns the minimum width required for the given String using the given font.
     *
     * @param title the text you want to determine the width of
     * @param font  the font for the text
     * @return an integer value determining the minimum width of
     * a string of text (10 is added to avoid ... bug)
     */
    public static int getMinWidth(String title, Font font) {
        Preconditions.checkNotNull(title);
        Preconditions.checkNotNull(font);

        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) font.getStringBounds(title, frc).getWidth() + SIZE_ADDITIVE;
    }

    /**
     * Returns the minimum width required for the given String using the given font.
     *
     * @param title the text you want to determine the width of
     * @param font  the font for the text
     * @return an integer value determining the minimum width of a string of text
     */
    public static int getAbsoluteMinWidth(String title, Font font) {
        Preconditions.checkNotNull(title);
        Preconditions.checkNotNull(font);

        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) font.getStringBounds(title, frc).getWidth();
    }

    /**
     * Returns the minimum height required for the given String using the given font.
     *
     * @param title the text you want to determine the height of
     * @param font  the font to use to determine the min height
     * @return an integer value determining the minimum height
     * of a string of text (10 is added to avoid ... bug)
     */
    public static int getMinHeight(String title, Font font) {
        Preconditions.checkNotNull(title);
        Preconditions.checkNotNull(font);

        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) font.getStringBounds(title, frc).getHeight() + SIZE_ADDITIVE;
    }

    /**
     * Returns the minimum height required for the given String
     * using the given font without adding 10.
     *
     * @param title the text you want to determine the height of
     * @param font  the font to use to determine the min height
     * @return an integer value determining the minimum height of a string of text
     */
    public static int getAbsoluteMinHeight(String title, Font font) {
        Preconditions.checkNotNull(title);
        Preconditions.checkNotNull(font);

        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) font.getStringBounds(title, frc).getHeight();
    }

    /**
     * Parses out all non-ascii characters from the provided string.
     *
     * @param nonAsciiContaining the string containing non-ascii characters
     * @return the string with the non-ascii characters removed
     */
    public static String parseNonAscii(String nonAsciiContaining) {
        Preconditions.checkNotNull(nonAsciiContaining);

        return getTrimmedText(nonAsciiContaining.replaceAll("[^\\x00-\\x7F]", " "));
    }

    /**
     * Returns whether the provided string contains non-ascii characters.
     *
     * @param nonAsciiContaining the string which may contain non-ascii characters
     * @return whether the provided string contains non-ascii characters
     */
    public static boolean containsNonAscii(String nonAsciiContaining) {
        Preconditions.checkNotNull(nonAsciiContaining);
        Preconditions.checkArgument(!nonAsciiContaining.isEmpty());

        return CharMatcher.ascii().matchesAllOf(nonAsciiContaining);
    }

    /**
     * A space char.
     */
    private static final String SPACE = " ";

    /**
     * Returns a whitespace string of n spaces.
     *
     * @param n the number of spaces for the returned string to contain
     * @return the whitespace string with n spaces
     */
    public static String generateNSpaces(int n) {
        Preconditions.checkArgument(n > 0);
        return SPACE.repeat(n);
    }

    /**
     * Splits the provided string using the provided pattern and returns a
     * linked list as opposed to an array containing the parts of the split.
     *
     * @param string  the string to split on using the pattern
     * @param pattern the pattern to split the string on
     * @return a list of the split parts
     */
    public static LinkedList<String> split(String string, Pattern pattern) {
        Preconditions.checkNotNull(string);
        Preconditions.checkArgument(!string.isEmpty());
        Preconditions.checkNotNull(pattern);

        Matcher m = pattern.matcher(string);
        LinkedList<String> ret = new LinkedList<>();

        while (m.find()) {
            ret.add(m.group(0));
        }

        return ret;
    }

    /**
     * Strips all new lines and replaces them with a space.
     * Returns the resulting String trimmed.
     *
     * @param line the line to strip of new line characters and trim
     * @return the line stripped of new line characters and trimmed
     */
    public static String stripNewLinesAndTrim(String line) {
        Preconditions.checkNotNull(line);
        Preconditions.checkArgument(!line.isEmpty());

        return line.replace("\n", " ").replace("\r", " ").trim();
    }

    /**
     * Returns a string of the individual strings trimmed and separated via a single space.
     *
     * @param strings the strings to build into a single string
     * @return the combined string
     */
    public static String separate(String... strings) {
        Preconditions.checkNotNull(strings);
        Preconditions.checkArgument(strings.length > 0);

        StringBuilder ret = new StringBuilder();

        for (String string : strings) {
            Preconditions.checkNotNull(string);

            ret.append(string.trim());
            ret.append(" ");
        }

        return ret.toString().trim();
    }

    // -------------------
    // Levenshtein methods
    // -------------------

    /**
     * Returns the levenshtein distance between string alpha and string beta.
     * From <a href="http://rosettacode.org/wiki/Levenshtein_distance#Iterative_space_optimized_.28even_bounded.29</a>
     *
     * @param alpha the first string
     * @param beta  the second string
     * @return the levenshtein distance between alpha and beta
     */
    public static int levenshteinDistance(String alpha, String beta) {
        Preconditions.checkNotNull(alpha);
        Preconditions.checkNotNull(beta);

        return distance(alpha, beta);
    }

    /**
     * Computes the distance between the two strings meaning
     * the number of operations needed to morph string alpha
     * into string beta.
     *
     * @param alpha the first string
     * @param beta  the second string
     * @return the number of operations required to transform
     * string alpha into string beta
     */
    private static int distance(String alpha, String beta) {
        if (Objects.equals(alpha, beta)) {
            return 0;
        }

        int la = alpha.length();
        int lb = beta.length();

        if (la == 0) {
            return lb;
        }

        if (lb == 0) {
            return la;
        }

        if (la < lb) {
            int tl = la;
            la = lb;
            lb = tl;
            String ts = alpha;
            alpha = beta;
            beta = ts;
        }

        int[] cost = new int[lb + 1];

        for (int i = 0 ; i <= lb ; i += 1) {
            cost[i] = i;
        }

        for (int i = 1 ; i <= la ; i += 1) {
            cost[0] = i;
            int prv = i - 1;
            int min = prv;

            for (int j = 1 ; j <= lb ; j += 1) {
                int act = prv + (alpha.charAt(i - 1) == beta.charAt(j - 1) ? 0 : 1);
                cost[j] = NumberUtil.min(1 + (prv = cost[j]), 1 + cost[j - 1], act);

                if (prv < min) {
                    min = prv;
                }
            }
        }

        return cost[lb];
    }

    /**
     * Generates the text to use for a custom component that extends JLabel to
     * for the component to paint with the necessary size for the component
     * to be visible. This is a Cyder specific method.
     *
     * @param numLines the number of lines of text to return
     * @return the text to use for the JLabel's text
     */
    public static String generateTextForCustomComponent(int numLines) {
        Preconditions.checkArgument(numLines > 0);

        StringBuilder ret = new StringBuilder();
        ret.append(BoundsUtil.OPENING_HTML_TAG);

        for (int i = 0 ; i < numLines ; i++) {
            ret.append(" ").append(BoundsUtil.BREAK_TAG);
        }

        return ret + " " + BoundsUtil.CLOSING_HTML_TAG;
    }

    /**
     * Returns whether the provided array contains at least one letter.
     *
     * @param chars the list of characters
     * @return whether the provided array contains at least one letter
     */
    public static boolean containsLetter(char[] chars) {
        Preconditions.checkNotNull(chars);

        for (char c : chars) {
            if (Character.isAlphabetic(c)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns whether the provided array contains at least one number.
     *
     * @param chars the list of characters
     * @return whether the provided array contains at least one number
     */
    public static boolean containsNumber(char[] chars) {
        Preconditions.checkNotNull(chars);

        for (char c : chars) {
            if (Character.isDigit(c)) {
                return true;
            }
        }

        return false;
    }
}