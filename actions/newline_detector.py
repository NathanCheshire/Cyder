
import os


def find_files(starting_dir: str, extensions: list = [], recursive: bool = False) -> list:
    """ Finds all files within the provided directory that
        end in one of the provided extensions.

        Parameters:
            starting_dir: the directory to start recursing from
            extensions: a list of valid extensions such as [".java"]
            recursive: whether to recurse through found subdirectories

        Returns:
            a list of discovered files
    """

    ret = []

    if len(extensions) == 0:
        raise Exception('Error: must provide valid extensions')

    if os.path.isdir(starting_dir):
        for subDir in os.listdir(starting_dir):
            if recursive:
                ret = ret + \
                    find_files(os.path.join(starting_dir, subDir),
                               extensions, recursive)
            else:
                ret.append(os.path.join(starting_dir, subDir))
    else:
        for extension in extensions:
            if starting_dir.endswith(extension):
                ret.append(starting_dir)

    return ret


def main():
    files = find_files(starting_dir=".",
                       extensions=['.java'], recursive=True)

    for file in files:

        num_newlines = 0
        anchored = False
        last_anchor = None
        last_anchor_line_number = 0
        originally_anchored = False
        line_number = -1

        file_lines = open(file, 'r').readlines()

        for line in file_lines:
            empty = len(line.strip()) == 0

            line_number = line_number + 1

            if empty and not originally_anchored:
                continue
            elif not empty and not anchored:
                if num_newlines > 1:
                    print("File:", file)
                    print("Found", num_newlines, "new lines between:")
                    print(last_anchor_line_number, ":", last_anchor.strip())
                    print("and")
                    print(line_number, ":", line.strip())
                    print("-------------------------------")

                originally_anchored = True
                anchored = True
                last_anchor = line
                last_anchor_line_number = line_number
                num_newlines = 0
            elif empty:
                anchored = False
                num_newlines = num_newlines + 1


if __name__ == '__main__':
    main()
