from typing import List
import sys


from new_line_detector import find_files


class BadWord:
    """ A record class for holding a bad word and information about its origin.
    """

    def __init__(self, clazz: str, line_number: int, line: str, word: str) -> None:
        self._clazz = clazz
        self._line_number = line_number
        self._line = line
        self._word = word

    def get_class(self) -> str:
        """ Returns the Java class this bad word was found from.
        """
        return self._class

    def get_line_number(self) -> int:
        """ Returns the line number this bad word was found at.
        """
        return self._line_number

    def get_line(self) -> str:
        """ Returns the full text line this bad word was found from.
        """
        return self._line

    def get_word(self) -> str:
        """ Returns the bad word which triggered a bad word match.
        """
        return self._word


def find_bad_words(starting_dir: str, filter_path: str, extensions: list) -> List:
    """ Finds bad words in files matching the extensions list starting from the
    starting directory and recursively discovering files.
    :param starting_dir: the path to the directory to start recursing from
    :type starting_dir: str
    :param filter_path: the path to the csv containing blocked words
    :type filter_path: str
    :param extensions: a list of extensions of files to search once found. Ex: [".java",".py"]
    :type extensions: list
    :return: a list of bad words
    :rtype: list
    """

    files = find_files(starting_dir, extensions, recursive=True)

    for file in files:
        if file is filter_path: # TODO make this work
            continue

        file_lines = open(file, 'r').readlines()

        for line in file_lines:
            line = line.strip()
            # TODO

    pass


def has_word(line: str, search_word: str) -> bool:
    """ Determines if the provided line contains the provided word to search for.
    :param line: the line to search through
    :type line: str
    :param search_word: the word to search line for
    :type line: str
    :return: whether the provided word was found in the line
    :rtype: bool
    """
    pass


def contains_blocked_word(input: str, blocked_words: list) -> bool:
    """ Returns whether the provided word is contained in the list of blocked words.
    :param input: the word to search for
    :type input: str
    :param blocked_words: the list of blocked words
    :type blocked_words: list
    :return: whether the provided word is contained in the list of blocked words.
    :rtype: bool
    """
    pass


def main():
    """ Spins off the bad_word_detector script and outputs the results.
    """

    linesep = '-------------------------------------------------------'

    starting_dir = '.'
    filter_path = './static/txt/blocked.txt'
    extensions = ['.java', '.kt', '.py', '.md', '.txt']

    bad_words = find_bad_words(starting_dir, filter_path, extensions)
    len_bad_words = len(bad_words)

    print(len_bad_words, " found:")
    for bad_word in bad_words:
        if isinstance(bad_word, BadWord):
            print("Found \"" + bad_word.get_word() + "\" from \"" + bad_word.get_class() + "\"" +
                  " on line " + bad_word.get_line_number() + ".\nFull line: \"" + bad_word.get_line() + "\"")
            print(linesep)

    sys.exit(0 if len_bad_words == 0 else 1)


if __name__ == '__main__':
    main()
