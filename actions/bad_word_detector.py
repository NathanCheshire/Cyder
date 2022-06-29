from typing import List
import sys


from new_line_detector import find_files


class BadWord:
    """ A record class for holding a bad word and information about its origin.
    """

    def __init__(self, clazz: str, line_number: int, line: str, words: list) -> None:
        self._clazz = clazz
        self._line_number = line_number
        self._line = line
        self._words = words

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

    def get_words(self) -> list:
        """ Returns the list of bad words which triggered a bad word match.
        """
        return self._words


def find_bad_words(starting_dir: str, filter_path: str, extensions: list) -> List:
    """ Finds bad words in files matching the extensions list starting from the
    starting directory and recursively discovering files.
    :param starting_dir: the path to the directory to start recursing from
    :type starting_dir: str
    :param filter_path: the path to the txt containing blocked words
    :type filter_path: str
    :param extensions: a list of extensions of files to search once found. Ex: [".java",".py"]
    :type extensions: list
    :return: a list of bad words
    :rtype: list
    """

    bad_words = get_stripped_lines(filter_path)
    files = find_files(starting_dir, extensions, recursive=True)

    ret = []

    for file in files:
        if file is filter_path:
            continue

        print("On file:", file)

        file_lines = get_stripped_lines(file)

        for line_number, line in enumerate(file_lines):
            words = contains_blocked_word(line, bad_words)
            if words is not None:
                ret.append(
                    BadWord(clazz=file, line_number=line_number, line=line, words=words))

    return ret


def get_stripped_lines(path: str) -> list:
    """ Returns a list of lines contained in the found file pointed to by the provided path.
    :param path: the path to the file
    :type path: str
    :return: a list of lines found, after removing trailing and leading whitespace
    :rtype: list
    """

    return [line.strip() for line in open(path, 'r').readlines()]


def contains_blocked_word(line: str, blocked_words: list) -> list:
    """ Returns whether the provided line containeds words from the list of blocked words.
    A list of the bad word(s) found is/are returned if found, otherwise None
    :param input: the line to search through
    :type input: str
    :param blocked_words: the list of blocked words
    :type blocked_words: list
    :return: a list of found blocked words, None otherwise
    :rtype: list
    """

    corrected_words = [word.lower() for word in blocked_words]
    line_words = line.strip().lower().split()

    intersection = [word for word in corrected_words if word in line_words]

    return intersection if len(intersection) > 0 else None


def main():
    """ Spins off the bad_word_detector script and outputs the results.
    """

    linesep = '-------------------------------------------------------'

    starting_dir = '.'
    filter_path = './static/txt/blocked.txt'
    extensions = ['.java', '.kt', '.py', '.md', '.txt']

    bad_words = find_bad_words(starting_dir, filter_path, extensions)
    len_bad_words = len(bad_words)

    if len_bad_words > 0:
        print("Found", len_bad_words, "matches")
    else:
        print("No bad words found, good job I guess")

    for bad_word in bad_words:
        if isinstance(bad_word, BadWord):
            print("Found \"" + str(bad_word.get_words()) + "\" from \"" + bad_word.get_class() + "\"" +
                  " on line " + bad_word.get_line_number() + ".\nFull line: \"" + bad_word.get_line() + "\"")
            print(linesep)

    sys.exit(0 if len_bad_words == 0 else 1)


if __name__ == '__main__':
    main()
