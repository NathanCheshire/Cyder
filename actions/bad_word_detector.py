"""
Bad_word_detector.py

A script to detect blocked words from a provided filter file.
"""

import argparse
import sys
import os


from new_line_detector import find_files


class BadWord:
    """ 
    A record type for holding a bad word and information about its origin.
    """

    def __init__(self, clazz: str, line_number: int, line: str, words: list) -> None:
        """
        Constructs a new BadWord.

        :param clazz: the class the bad word was found in.
        :param line_number: the line number of the file the bad word was found in
        :param line: the full line the bad word was found in
        :param words: the list of bad words found on the file line
        """

        self._clazz = clazz
        self._line_number = line_number
        self._line = line
        self._words = words

    def get_class(self) -> str:
        """ 
        Returns the Java class this bad word was found from.
        """
        return self._clazz

    def get_line_number(self) -> int:
        """ 
        Returns the line number this bad word was found at.
        """
        return self._line_number

    def get_line(self) -> str:
        """ 
        Returns the full text line this bad word was found from.
        """
        return self._line

    def get_words(self) -> list:
        """
        Returns the list of bad words which triggered a bad word match.
        """
        return self._words


def find_bad_words(starting_dir: str, filter_path: str, extensions: list) -> list:
    """ 
    Finds bad words in files matching the extensions list starting from the
    starting directory and recursively discovering files.

    :param starting_dir: the path to the directory to start recursion from
    :type starting_dir: str
    :param filter_path: the path to the txt containing blocked words
    :type filter_path: str
    :param extensions: a list of extensions of files to search once found. Ex: [".java",".py"]
    :type extensions: list
    :return: a list of bad words
    """

    bad_words = get_stripped_lines(filter_path)
    files = find_files(starting_dir, extensions, recursive=True)

    ret = []

    for file in files:
        if os.path.samefile(file, filter_path):
            continue

        print(f"Searching file {file}")

        file_lines = get_stripped_lines(file)

        for line_number, line in enumerate(file_lines):
            words = contains_blocked_word(line, bad_words)
            if words is not None:
                ret.append(
                    BadWord(clazz=file, line_number=line_number, line=line, words=words))

    return ret


def get_stripped_lines(path: str) -> list:
    """ 
    Returns a list of lines contained in the found file pointed to by the provided path.
    All lines are stripped before being added to the returned list.

    :param path: the path to the file
    :return: a list of lines found, after removing trailing and leading whitespace
    """

    return [line.strip() for line in open(path, 'r').readlines()]


def contains_blocked_word(line: str, blocked_words: list) -> list:
    """ 
    Returns whether the provided line contains words from the list of blocked words.
    A list of the bad word(s) found is/are returned if found, otherwise None.

    :param input: the line to search through
    :param blocked_words: the list of blocked words
    :return: a list of found blocked words, None otherwise
    """

    lower_blocked_words = [word.lower() for word in blocked_words]
    line_words = line.strip().lower().split()

    intersection = [word for word in lower_blocked_words if word in line_words]

    return intersection if len(intersection) > 0 else None


def main():
    """ 
    Spins off the bad_word_detector script and outputs the results.
    """

    parser = argparse.ArgumentParser(prog='Bad word detector',
                                     description="Detects blocked words from the provided filter")
    parser.add_argument('-sd', '--starting_directory', required=True,
                        help='the starting directory')
    parser.add_argument('-f', '--filter', required=True,
                        help='the filter txt file containing each blocked word on a line of its own')

    args = parser.parse_args()

    line_sep = '-------------------------------------------------------'

    extensions = ['.java', '.kt', '.py', '.md', '.txt']

    bad_words = find_bad_words(args.starting_directory, args.filter, extensions)
    len_bad_words = len(bad_words)

    if len_bad_words > 0:
        print("Found", len_bad_words, "bad words")
    else:
        print("No bad words found")

    for bad_word in bad_words:
        if isinstance(bad_word, BadWord):
            print("Found \"" + str(bad_word.get_words()) + "\" from \"" + bad_word.get_class() + "\"" +
                  " on line " + str(bad_word.get_line_number()) + ".\nFull line: \"" + bad_word.get_line() + "\"")
            print(line_sep)

    sys.exit(len_bad_words)


if __name__ == '__main__':
    main()
