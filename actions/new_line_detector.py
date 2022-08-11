

import sys
from stat_generator import find_files


def find_new_lines():
    """
    Finds unnecessary new lines in all .java files recursively found starting from this directory.
    Unnecessary new lines are defined by a length of two or more for Cyder.
    """

    total_unnecessary_new_lines = 0

    for file in find_files(starting_dir=".", extensions=['.java'], recursive=True):
        def print_stats(current_file: str, num_lines: int, starting_line: str, ending_line: str,
                        starting_line_num: int, ending_line_num: int):
            """
            Prints the statistics found for the current file.
            """

            print("File:", current_file)
            print("Found", num_lines, "new lines between:")
            print(starting_line_num, ":", starting_line)
            print("and")
            print(ending_line_num + 1, ":", ending_line)
            print("-------------------------------")

        local_unnecessary_new_lines = 0
        anchored = False
        last_anchor_line = None
        last_anchor_line_number = 0
        originally_anchored = False

        file_lines = open(file, 'r').readlines()

        for line_index, line in enumerate(file_lines):
            line = line.strip()
            empty = len(line) == 0

            if empty and not originally_anchored:
                continue
            elif not empty and not anchored:
                if local_unnecessary_new_lines > 1:
                    print_stats(file, local_unnecessary_new_lines, last_anchor_line, line,
                                last_anchor_line_number, line_index + 1)

                originally_anchored = True
                anchored = True
                last_anchor_line = line
                local_unnecessary_new_lines = 0
            elif empty:
                if local_unnecessary_new_lines == 0:
                    last_anchor_line_number = line_index

                anchored = False
                local_unnecessary_new_lines = local_unnecessary_new_lines + 1

        total_unnecessary_new_lines += local_unnecessary_new_lines
                
    sys.exit(total_unnecessary_new_lines)


if __name__ == '__main__':
    find_new_lines()
