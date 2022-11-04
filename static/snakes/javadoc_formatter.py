"""
javadoc-formatter

A formatter to convert multi-line javadoc comments which could be expressed on a single line, to a single line.
"""

from dataclasses import replace
import enum
from fileinput import lineno
import os
import sys
import re

def find_files(starting_dir: str, extensions: list = [], recursive: bool = False) -> list:
    """ 
    Finds all files within the provided directory that end in one of the provided extensions.
    :param starting_dir: the directory to start recursion from
    :param extensions: a list of valid extensions such as [".java"]
    :param recursive: whether to recurse through found subdirectories
    :return: a list of discovered files
    """

    ret = []

    if len(extensions) == 0:
        raise Exception('Error: must provide valid extensions')

    if os.path.isdir(starting_dir):
        for sub_directory in os.listdir(starting_dir):
            if recursive:
                ret = ret + \
                    find_files(os.path.join(starting_dir, sub_directory),
                               extensions, recursive)
            else:
                ret.append(os.path.join(starting_dir, sub_directory))
    else:
        for extension in extensions:
            if starting_dir.endswith(extension):
                ret.append(starting_dir)

    return ret

OPENING_JAVADOC = "/**"
CLOSING_JAVADOC = "*/"

def check_javadoc(file, correct: bool) -> int:
    """
    Corrects the javadoc in the provided java file by ensuring that 
    single line javadocs are expressed on a single line.

    For example, the following:

    /*
    * A javadoc comment.
    */

    Would be translated to:

    /** A javadoc comment. */

    :param file: the java file to correct
    :param correct: whether to correct the file's javadoc or simply return the javadoc violation count
    """

    three_liners = []

    lines = open(file, 'r').readlines()

    in_doc = False

    current_doc_starting_line = -1
    current_doc_lines = 0

    current_line = 1

    for line in lines:
        line = line.strip()

        start_of_doc = line == OPENING_JAVADOC
        end_of_doc = line == CLOSING_JAVADOC

        if start_of_doc:
            current_doc_starting_line = current_line
            in_doc = True
        if end_of_doc:
            in_doc = False
            current_doc_lines += 1

            if current_doc_lines == 3:
                three_liners.append(current_doc_starting_line)

            current_doc_lines = 0
        if in_doc:
            current_doc_lines += 1

        current_line += 1

    if correct:
        replace_lines = []

        for line_num in three_liners:
            line = lines[line_num]
            spaces = line.rfind("*")
            starting_spacer = " " * (spaces - 1)
            replace_lines.append(starting_spacer + OPENING_JAVADOC + " " + line.strip()[2:] + " " + CLOSING_JAVADOC)

        write_lines = []

        for line_num, line in enumerate(lines):
            if line_num - 1 in three_liners:
                continue
            elif line_num + 1 in three_liners:
                continue
            elif line_num in three_liners:
                write_lines.append(replace_lines[three_liners.index(line_num)] + "\n")
            else:
                write_lines.append(line)

        with open(file, 'w+') as file:
            file.writelines(write_lines)

    return len(three_liners)

def main():
    args = sys.argv

    if len(args) != 3:
        print("Usage: python javadoc_formatter.py path/to/starting/directory/ [should_correct: bool]")
        sys.exit(1)

    starting_dir = args[1]
    should_correct = args[2].lower() == 'true'
    print(should_correct)

    three_liners = 0

    for file in find_files(starting_dir=starting_dir, extensions=[".java"], recursive=True):
        three_liners += check_javadoc(file, correct=should_correct)

    print("{} three line javadocs found, corrected: {}".format(three_liners, should_correct))
    sys.exit(three_liners)

if __name__ == '__main__':
    main()