package cyder.strings;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.bounds.HtmlString;
import cyder.bounds.PlainString;
import cyder.bounds.StringContainer;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderUrls;
import cyder.constants.HtmlTags;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.network.NetworkUtil;
import cyder.ui.pane.CyderOutputPane;
import cyder.utils.ArrayUtil;
import cyder.utils.SecurityUtil;
import cyder.utils.StaticUtil;
import org.atteo.evo.inflector.English;
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
import java.util.List;
import java.util.*;

/**
 * String utility methods along with JTextPane utility methods
 * <p>
 * Note: these methods are not thread safe and thus this class should be externally synchronized
 * to achieve thread safety. Typically, in Cyder, this is performed via using a {@link CyderOutputPane}
 * which bundles a JTextPane, StringUtil, and Semaphore.
 */
@SuppressWarnings("SpellCheckingInspection") /* urls */
public final class StringUtil {
    /**
     * The default number of elements of a {@link JTextPane}s {@link StyledDocument}.
     */
    private static final int defaultDocumentElements = 3;

    /**
     * The output pane to print to in the case an object is created.
     */
    private final CyderOutputPane linkedCyderPane;

    /**
     * Suppress default constructor.
     */
    private StringUtil() {
        throw new IllegalMethodException(CyderStrings.ILLEGAL_CONSTRUCTOR);
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
    public CyderOutputPane getLinkedCyderPane() {
        return linkedCyderPane;
    }

    // ---------------------------------
    // Methods which require an instance
    // ---------------------------------

    /**
     * Removes the first object from the linked pane, this could be anything from a Component to a String.
     */
    public synchronized void removeFirst() {
        try {
            if (!linkedCyderPane.acquireLock()) {
                throw new FatalException("Failed to acquire output pane lock");
            }

            Element root = linkedCyderPane.getJTextPane().getDocument().getDefaultRootElement();
            Element first = root.getElement(0);
            linkedCyderPane.getJTextPane().getDocument().remove(first.getStartOffset(), first.getEndOffset());
            linkedCyderPane.getJTextPane().setCaretPosition(linkedCyderPane.getJTextPane().getDocument().getLength());

            linkedCyderPane.releaseLock();
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
        String[] lines = text.split(CyderStrings.newline);

        if (lines.length < 1) {
            throw new FatalException("Linked pane contains no text lines");
        }

        return lines[lines.length - 1];
    }

    /**
     * Removes the last element added to the linked JTextPane such as an
     * {@link ImageIcon}, {@link JComponent}, string, or newline.
     */
    public synchronized void removeLastElement() {
        try {
            Element rootElement = linkedCyderPane.getJTextPane().getDocument().getDefaultRootElement();
            Element removeElement = rootElement.getElement(rootElement.getElementCount() - 1);
            int start = removeElement.getStartOffset();
            int end = removeElement.getEndOffset();

            int offset = start - 1;
            int length = end - start;

            if (offset < 0) {
                offset = 0;
                length -= 1;
            }

            linkedCyderPane.getJTextPane().getDocument().remove(offset, length);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Returns the number of {@link Element}s contained in by the inner {@link JTextPane}s {@link StyledDocument}.
     *
     * @return the number of elements found
     */
    public synchronized int countDocumentElements() {
        ElementIterator iterator = new ElementIterator(linkedCyderPane.getJTextPane().getStyledDocument());
        int count = 0;
        while (iterator.next() != null) count++;
        return count;
    }

    /**
     * Returns whether the inner {@link JTextPane}s {@link StyledDocument} contains more than the default
     * number of elements, that of three.
     *
     * @return whether there are more than the default number of elements in the styled document
     */
    public synchronized boolean documentContainsMoreThanDefaultElements() {
        return countDocumentElements() > defaultDocumentElements;
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
            document.insertString(document.getLength(), print.toString() + CyderStrings.newline, null);
            linkedCyderPane.getJTextPane().setCaretPosition(linkedCyderPane.getJTextPane().getDocument().getLength());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Prints the list to {@link this} object's JTextPane.
     *
     * @param list the list of objects to print
     */
    public synchronized <T> void printLines(List<T> list) {
        Preconditions.checkNotNull(list);

        for (T tee : list) {
            println(tee);
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

    // ------------------------------------
    // End methods which require an instance
    // ------------------------------------

    /**
     * Determines the proper suffix when attempting to use possession
     * on a string that represents a noun.
     *
     * @param name the proper singular form of the noun
     * @return the string to be appended to the proper noun ('s or simply ')
     */
    public static String getApostropheSuffix(String name) {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(!name.isEmpty());

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
    public static String getWordFormBasedOnNumber(int num, String word) {
        Preconditions.checkArgument(num >= 0);
        Preconditions.checkNotNull(word);
        Preconditions.checkArgument(!word.isEmpty());

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
        Preconditions.checkArgument(count >= 0);
        Preconditions.checkNotNull(character);
        Preconditions.checkArgument(!character.isEmpty());

        return character.repeat(count);
    }

    /**
     * Determines if a word is palindrome (spelled the same forward and backwards like ogopogo and racecar).
     *
     * @param word the word to check
     * @return the result of the comparison
     */
    public static boolean isPalindrome(String word) {
        Preconditions.checkNotNull(word);

        ImmutableList<Character> chars = ArrayUtil.toList(word.toLowerCase().toCharArray());
        ImmutableList<Character> reversed = chars.reverse();

        return chars.equals(reversed);
    }

    /**
     * Converts the first character of all words in the string ot the
     * capital version if the character is a standard latin character.
     *
     * @param sentence the sentence to capitalize the first letter of each word
     * @return the resultant string
     */
    public static String capsFirstWords(String sentence) {
        Preconditions.checkNotNull(sentence);

        if (sentence.isEmpty()) return sentence;

        StringBuilder sb = new StringBuilder(sentence.length());

        Arrays.stream(sentence.split(CyderRegexPatterns.whiteSpaceRegex)).forEach(wordy -> {
            sb.append(Character.toUpperCase(wordy.charAt(0)));
            sb.append(wordy.substring(1).toLowerCase()).append(CyderStrings.space);
        });

        return sb.toString().trim();
    }

    /**
     * Converts the first character to upper case provided it is a standard latin character.
     *
     * @param word the word to capitalize the first letter of
     * @return the capitalized word
     */
    public static String capsFirstWord(String word) {
        Preconditions.checkNotNull(word);

        if (word.isEmpty()) return word;
        return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
    }

    /**
     * Filters out simple leet speech from the provided string.
     *
     * @param string the string to filter leet out of
     * @return the resultant string after filtering
     */
    public static String filterLeet(String string) {
        Preconditions.checkNotNull(string);

        if (string.isEmpty()) return string;
        String[] words = string.split(CyderRegexPatterns.whiteSpaceRegex);
        if (words.length == 0) return string;

        StringBuilder sb = new StringBuilder();
        Arrays.stream(words).forEach(word ->
                sb.append(replaceLeetChars(word)).append(CyderStrings.space));
        return sb.toString().trim();
    }

    /**
     * Inner filtering of leet speech for words specifically, this is the main driver method that does the magic.
     *
     * @param word the word to filter leet out of
     * @return the word having leet removed to the best of our abilities
     */
    private static String replaceLeetChars(String word) {
        char[] chars = word.toCharArray();

        for (int i = 0 ; i < chars.length ; i++) {
            char c = chars[i];

            /*
            Ideally this should be redone or use a library.
            I'd like to implement the rules from this table at some point:
            https://cleanspeak.com/images/blog/leet-wiki-table.png
             */

            if (c == '4' || c == '@' || c == '^' || c == 'z' || c == 'Z') {
                chars[i] = 'a';
            } else if (c == '8' || c == '6') {
                chars[i] = 'b';
            } else if (c == '(' || c == '<' || c == '{') {
                chars[i] = 'c';
            } else if (c == '3' || c == '&') {
                chars[i] = 'e';
            } else if (c == '}') {
                chars[i] = 'f';
            } else if (c == '9') {
                chars[i] = 'g';
            } else if (c == '#') {
                chars[i] = 'h';
            } else if (c == '1' || c == '!' || c == '|') {
                chars[i] = 'i';
            } else if (c == ']') {
                chars[i] = 'j';
            } else if (c == '7') {
                chars[i] = 'l';
            } else if (c == '~') {
                chars[i] = 'n';
            } else if (c == '0') {
                chars[i] = 'o';
            } else if (c == '?' || c == 'q' || c == 'Q') {
                chars[i] = 'p';
            } else if (c == '2') {
                chars[i] = 'r';
            } else if (c == '$' || c == '5') {
                chars[i] = 's';
            } else if (c == '+') {
                chars[i] = 't';
            } else if (c == '*') {
                chars[i] = 'x';
            } else if (c == '%') {
                chars[i] = 'z';
            }
        }

        return String.valueOf(chars);
    }

    /**
     * Tests whether the provided string has the provided word inside it.
     *
     * @param userInput the master string to search through
     * @param findWord  the word to search the master string for
     * @return whether the given string contains the test word
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
     * @return whether the given string contains the test word
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

        String[] words = userInput.split(CyderRegexPatterns.whiteSpaceRegex);

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
    private static ImmutableList<String> blockedWords;

    private static void loadBlockedWords() {
        ArrayList<String> blockedWords = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(
                StaticUtil.getStaticResource("blocked.txt")))) {
            String blockedWord;

            while ((blockedWord = reader.readLine()) != null) {
                blockedWords.add(blockedWord);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        StringUtil.blockedWords = ImmutableList.copyOf(blockedWords);
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

        if (blockedWords == null) loadBlockedWords();
        if (filterLeet) input = filterLeet(input.toLowerCase());

        for (String blockedWord : blockedWords) {
            if (hasWord(input, blockedWord, true)) {
                return new BlockedWordResult(true, blockedWord);
            }
        }

        return new BlockedWordResult(false, "");
    }

    /**
     * Provides the exact string object but with the first character converted to lowercase.
     *
     * @param string the string to convert the first character to lowercase
     * @return the resultant string
     */
    public static String firstCharToLowerCase(String string) {
        Preconditions.checkNotNull(string);

        if (isNullOrEmpty(string)) {
            return "";
        }

        if (string.length() == 1) {
            return string.toLowerCase();
        } else {
            return string.substring(0, 1).toLowerCase() + string.substring(1);
        }
    }

    /**
     * Count the number of words of the provided string.
     * A word is defined as a sequence of non-whitespace chars surrounded on both sides by whitespace.
     * The starting or ending word(s) in a provided string, need not be surrounded on both sides by whitespace.
     *
     * @param string the string ot count the words of
     * @return the word count of the requested string
     */
    public static int countWords(String string) {
        if (isNullOrEmpty(string)) return 0;

        return string.split(CyderRegexPatterns.whiteSpaceRegex).length;
    }

    /**
     * Ensures that there is a singular space after every comma within the input.
     *
     * @param input the potentially wrongly formatted string
     * @return the corrected string
     */
    public static String formatCommas(String input) {
        Preconditions.checkNotNull(input);

        if (input.isEmpty()) return input;

        return input.replaceAll(",\\s*", ", ");
    }

    /**
     * The name for the element to grab from the DOM returned when getting a Dictionary.com html document.
     */
    private static final String DEFINITION_ELEMENT_NAME = "one-click-content css-nnyc96 e1q3nk1v1";

    /**
     * The dictionary base url.
     */
    private static final String DICTIONARY_BASE = "https://www.dictionary.com/browse/";

    /**
     * The pad 10 class name of elements to remove from the JSoup document.
     */
    private static final String PAD_10 = "pad_10";

    /**
     * The pad 20 class name of elements to remove from the JSoup document.
     */
    private static final String PAD_20 = "pad_20";

    /**
     * Searches Dictionary.com for the provided word.
     *
     * @param word the word to find a definition for
     * @return the definition of the requested word if found
     */
    public static Optional<String> getDefinition(String word) {
        Preconditions.checkNotNull(word);
        Preconditions.checkArgument(!word.isEmpty());

        try {
            Document doc = Jsoup.connect(DICTIONARY_BASE + word).get();
            Elements els = doc.getElementsByClass(DEFINITION_ELEMENT_NAME).not(PAD_10).not(PAD_20);
            org.jsoup.nodes.Element htmlDescription = els.get(0);

            Document docParsed = Jsoup.parse(String.valueOf(htmlDescription));
            String definition = docParsed.text();
            return Optional.of(definition);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return Optional.empty();
    }

    /**
     * The suffix for a wikipedia summary scrape.
     */
    private static final String WIKI_SUM_URL_SUFFIX = "&prop=extracts&exintro&explaintext&redirects=1&titles=";

    /**
     * The string to split the wikisum results on.
     */
    private static final String wikiSumSplitOn = CyderStrings.quote + "extract"
            + CyderStrings.quote + CyderStrings.colon + CyderStrings.quote;

    /**
     * Web scrapes Wikipedia for the appropriate article and returns the body of the wiki article.
     *
     * @param query the query to search wikipedia for
     * @return the wiki body result
     */
    public static Optional<String> getWikipediaSummary(String query) {
        Preconditions.checkNotNull(query);
        Preconditions.checkArgument(!query.isEmpty());

        try {
            String queryUrl = CyderUrls.WIKIPEDIA_SUMMARY_BASE + WIKI_SUM_URL_SUFFIX
                    + query.replace(CyderRegexPatterns.whiteSpaceRegex, NetworkUtil.URL_SPACE);
            String urlContents = NetworkUtil.readUrl(queryUrl);

            String[] serializedPageNumber = urlContents.split(wikiSumSplitOn);
            String result = serializedPageNumber[1].replace("}", "");
            result = result.substring(0, result.length() - 1);
            return Optional.of(result);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return Optional.empty();
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
     * Finds the non-html text and html tags of a string and returns a linked list representing the parts.
     *
     * @param htmlText the text containing html tags
     * @return a linked list where each object represents either a complete tag or raw text
     */
    public static ImmutableList<StringContainer> splitToHtmlTagsAndContent(String htmlText) {
        Preconditions.checkNotNull(htmlText);
        Preconditions.checkArgument(!htmlText.isEmpty());

        ArrayList<StringContainer> taggedStrings = new ArrayList<>();
        String textCopy = htmlText;

        while ((textCopy.contains(HtmlTags.opening) && textCopy.contains(HtmlTags.closing))) {
            int firstOpeningTagIndex = textCopy.indexOf(HtmlTags.opening);
            int firstClosingTagIndex = textCopy.indexOf(HtmlTags.closing);
            if (firstClosingTagIndex < firstOpeningTagIndex) break;

            String nextText = textCopy.substring(0, firstOpeningTagIndex);
            if (!nextText.isEmpty()) {
                taggedStrings.add(new PlainString(nextText));
            }

            String nextHtml = textCopy.substring(firstOpeningTagIndex, firstClosingTagIndex + 1);
            if (!nextHtml.isEmpty()) {
                taggedStrings.add(new HtmlString(nextHtml));
            }

            textCopy = textCopy.substring(firstClosingTagIndex + 1);
        }

        // Remaining text is non-html since the textCopy contain neither opening or closing tags
        if (!textCopy.isEmpty()) {
            taggedStrings.add(new PlainString(textCopy));
        }

        return ImmutableList.copyOf(taggedStrings);
    }

    /**
     * Determines if the provided String is null meaning literally null,
     * empty (length 0), or contained in {@link CyderStrings#NULL_STRINGS}.
     *
     * @param string the String to test for
     * @return whether the provided String was null
     */
    public static boolean isNullOrEmpty(String string) {
        if (string == null) return true;

        string = string.replaceAll(CyderRegexPatterns.whiteSpaceRegex, "");
        return string.isEmpty() || in(string, true, CyderStrings.NULL_STRINGS);
    }

    /**
     * Returns the length of the non-html text of the provided String.
     *
     * @param htmlText the text containing html tags and styling
     * @return the length of the non-html text
     */
    public static int getTextLengthIgnoringHtmlTags(String htmlText) {
        Preconditions.checkNotNull(htmlText);
        Preconditions.checkArgument(!htmlText.isEmpty());

        int length = 0;

        ImmutableList<StringContainer> taggedStrings = splitToHtmlTagsAndContent(htmlText);

        if (taggedStrings.isEmpty()) {
            length = htmlText.length();
        } else {
            for (StringContainer stringContainer : taggedStrings) {
                if (stringContainer instanceof PlainString plainString) {
                    length += plainString.getString().length();
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

        return text.replaceAll(CyderRegexPatterns.whiteSpaceRegex, CyderStrings.space).trim();
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
        for (String string : strings) {
            Preconditions.checkNotNull(string);
            Preconditions.checkArgument(!string.isEmpty());
        }

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
        Preconditions.checkNotNull(strings);

        return in(lookFor, ignoreCase, ArrayUtil.toList(strings));
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
        Preconditions.checkNotNull(strings);

        return strings.stream().anyMatch(look ->
                (ignoreCase && lookFor.equalsIgnoreCase(look)) || lookFor.equals(look));
    }

    /**
     * The amount added to {@link #getMinWidth(String, Font)} and
     * {@link #getMinHeight(String, Font)} to account for possible weird bugs.
     */
    public static final int SIZE_ADDITIVE = 10;

    /**
     * The font render context to use for string bounds calculations.
     */
    private static final FontRenderContext fontRenderContext =
            new FontRenderContext(new AffineTransform(), true, true);

    /**
     * Returns the minimum width required for the given String using the given font.
     * {@link #SIZE_ADDITIVE} is added to the returned value to avoid a graphical bug.
     *
     * @param text the text to determine the width of
     * @param font the font for the text
     * @return the minimum width
     */
    public static int getMinWidth(String text, Font font) {
        Preconditions.checkNotNull(text);
        Preconditions.checkNotNull(font);

        return (int) font.getStringBounds(text, fontRenderContext).getWidth() + SIZE_ADDITIVE;
    }

    /**
     * Returns the minimum width required for the given String using the given font.
     *
     * @param text the text to determine the width of
     * @param font the font for the text
     * @return the absolute minimum width
     */
    public static int getAbsoluteMinWidth(String text, Font font) {
        Preconditions.checkNotNull(text);
        Preconditions.checkNotNull(font);

        return (int) font.getStringBounds(text, fontRenderContext).getWidth();
    }

    /**
     * Returns the minimum height required for the given String using the given font.
     * {@link #SIZE_ADDITIVE} is added to the returned value to avoid a graphical bug.
     *
     * @param text the text to determine the width of
     * @param font the font
     * @return the minimum height
     */
    public static int getMinHeight(String text, Font font) {
        Preconditions.checkNotNull(text);
        Preconditions.checkNotNull(font);

        return (int) font.getStringBounds(text, fontRenderContext).getHeight() + SIZE_ADDITIVE;
    }

    /**
     * Returns the minimum height required for the given String.
     *
     * @param text the text to determine the width of
     * @param font the font to use to determine the min height
     * @return the absolute minimum height
     */
    public static int getAbsoluteMinHeight(String text, Font font) {
        Preconditions.checkNotNull(text);
        Preconditions.checkNotNull(font);

        return (int) font.getStringBounds(text, fontRenderContext).getHeight();
    }

    /**
     * The regex to target non Ascii characters.
     */
    private static final String nonAsciiRegex = "[^\\x00-\\x7F]";

    /**
     * Removes all non-ascii characters from the provided string.
     *
     * @param string the string containing non-ascii characters
     * @return the string with the non-ascii characters removed
     */
    public static String removeNonAscii(String string) {
        Preconditions.checkNotNull(string);

        return getTrimmedText(string.replaceAll(nonAsciiRegex, CyderStrings.space));
    }

    /**
     * Returns whether the provided string contains non-ascii characters.
     *
     * @param string the string which may contain non-ascii characters
     * @return whether the provided string contains non-ascii characters
     */
    public static boolean containsNonAscii(String string) {
        Preconditions.checkNotNull(string);

        return removeNonAscii(string).length() != string.length();
    }

    /**
     * Returns a whitespace string of n spaces.
     *
     * @param n the number of spaces for the returned string to contain
     * @return the whitespace string with n spaces
     */
    public static String generateSpaces(int n) {
        Preconditions.checkArgument(n >= 0);

        return CyderStrings.space.repeat(n);
    }

    /**
     * Strips all new lines and replaces them with a space.
     * Returns the resulting string trimmed.
     *
     * @param line the line to strip of new line characters and trim
     * @return the line stripped of new line characters and trimmed
     */
    public static String stripNewLinesAndTrim(String line) {
        Preconditions.checkNotNull(line);

        return line.replace(CyderStrings.newline, CyderStrings.space)
                .replace(CyderStrings.carriageReturnChar, CyderStrings.space).trim();
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

        // Note to maintainers: do not try to enhance this, we are using a char[] for security.

        for (char c : chars) {
            if (Character.isDigit(c)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Escapes all the quotes in the provided string.
     * All instances of {\"} are replaced with {\\\"}.
     *
     * @param string the string
     * @return the string with quotes escaped
     */
    public static String escapeQuotes(String string) {
        Preconditions.checkNotNull(string);

        return string.replace("\"", "\\\"");
    }

    /**
     * Returns a string of the parts combined with the "between" string inserted between the parts.
     *
     * @param parts   the parts
     * @param between the string to insert between the parts
     * @return a string of the parts combined with the "between" string inserted between the parts
     */
    public static String joinParts(List<String> parts, String between) {
        Preconditions.checkNotNull(parts);
        Preconditions.checkNotNull(between);

        StringBuilder ret = new StringBuilder();

        for (int i = 0 ; i < parts.size() ; i++) {
            ret.append(parts.get(i));
            if (i != parts.size() - 1) ret.append(between);
        }

        return ret.toString();
    }

    /**
     * Removes the last character from the provided string.
     *
     * @param string the string to remove the last char from
     * @return the string with the last character removed
     */
    public static String removeLastChar(String string) {
        Preconditions.checkNotNull(string);
        Preconditions.checkArgument(!string.isEmpty());

        return string.substring(0, string.length() - 1);
    }

    /**
     * Returns whether the provided alpha string contains the provided beta string ignoring case.
     *
     * @param alpha the first string
     * @param beta  the second string
     * @return whether the provided alpha string contains the provided beta string ignoring case
     */
    public static boolean containsIgnoreCase(String alpha, String beta) {
        Preconditions.checkNotNull(alpha);
        Preconditions.checkNotNull(beta);

        return alpha.toLowerCase().contains(beta.toLowerCase());
    }
}