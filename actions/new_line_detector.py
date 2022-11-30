"""
New_line_detector.py

A detection script to detect and output any blocks of newlines greater than 1 in length.
"""

import argparse
import sys
from stat_generator import find_files


def find_unnecessary_new_lines(start_directory: str):
    """
    Finds unnecessary new lines in all .java files recursively found starting from this directory.
    Unnecessary new lines are defined by a length of two or more for Cyder.

    :param start_directory: the starting directory to recursively find the files from
    """
    failed = False
    java_files = find_files(starting_dir=start_directory, extensions=['.java'], recursive=True)

    for file in java_files:
        print(f"Searching file {file}")

        current_running_new_lines = 0

        anchored = False
        originally_anchored = False
        last_anchor = None
        last_anchor_line_number = 0

        file_lines = open(file, 'r').readlines()

        for line_index, line in enumerate(file_lines):
            line = line.strip()
            empty = len(line) == 0

            if empty and not originally_anchored:
                continue
            elif not empty and not anchored:
                if current_running_new_lines > 1:
                    print_stats(file, current_running_new_lines, last_anchor_line_number,
                                last_anchor, line_index + 1, line)
                    failed = True

                originally_anchored = True
                anchored = True
                last_anchor = line
                current_running_new_lines = 0
            elif empty:
                if current_running_new_lines == 0:
                    last_anchor_line_number = line_index

                anchored = False
                current_running_new_lines = current_running_new_lines + 1

    if not failed:
        print(f"No newlines found in {len(java_files)} searched java files")

    sys.exit(1 if failed else 0)


def print_stats(file: str, num_unnecessary_new_lines: int, starting_line_num: int,
                starting_line: str, ending_list_num: int, ending_line: str):
    """
    Prints the statistics found for the current file.
    """

    print("File:", file)
    print("Found", num_unnecessary_new_lines, "new lines between:")
    print(starting_line_num, ":", starting_line)
    print("and")
    print(ending_list_num, ":", ending_line)
    print("-------------------------------")


def main():
    parser = argparse.ArgumentParser(prog='New line detector',
                                     description="Detects blocks of new lines greater than 1")
    parser.add_argument('-sd', '--starting_directory', required=True,
                        help='the starting directory')

    args = parser.parse_args()
    
    find_unnecessary_new_lines(start_directory=args.starting_directory)


if __name__ == '__main__':
    main()
