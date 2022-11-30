"""
Javadoc_formatter.py

A formatter to convert multi-line javadoc comments which could be expressed on a single line, to a single line.
"""

import argparse
import os
import sys
from stat_generator import find_files

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
            replace_lines.append(starting_spacer + OPENING_JAVADOC +
                                 " " + line.strip()[2:] + " " + CLOSING_JAVADOC)

        write_lines = []

        for line_num, line in enumerate(lines):
            if line_num - 1 in three_liners:
                continue
            elif line_num + 1 in three_liners:
                continue
            elif line_num in three_liners:
                write_lines.append(
                    replace_lines[three_liners.index(line_num)] + "\n")
            else:
                write_lines.append(line)

        with open(file, 'w+') as file:
            file.writelines(write_lines)

    return len(three_liners)


def main():
    parser = argparse.ArgumentParser(prog='Javadoc formatter',
                                     description="Formats three line javadocs to a singular line")
    parser.add_argument("-sd", "--starting_directory", required=True,
                        help="The directory to recursively find java files starting from")
    parser.add_argument('-c', '--correct', required=True,
                        help='Whether the three line javadocs should be corrected to a singular line')
    parser.add_argument('-f', '--fail', required=True,
                        help='whether the program should exit with a failure exit code " + \
                            "if three line javadocs are found and not corrected')

    args = parser.parse_args()

    should_correct = args.correct.lower() == "true"
    should_fail_if_found = args.fail.lower() == "true"

    three_liners = 0

    files = find_files(starting_dir=args.starting_directory, extensions=[".java"], recursive=True)

    for file in files:
        current_three_liners = check_javadoc(file, correct=should_correct)
        print("{} found to have {} three line javadocs".format(
            file, current_three_liners))
        three_liners += current_three_liners
``
    print("{} three line javadocs found across {} java files, were corrected: {}".format(
        three_liners, len(files), should_correct))

    if should_fail_if_found and not should_correct:
        if len(three_liners):
            print ("Failed")
        sys.exit(three_liners)
    else:
        print("Exiting normally")
        sys.exit(0)


if __name__ == '__main__':
    main()
