from actions.stat_generator import find_files


def main():
    files = find_files(starting_dir="cyder",
                       extensions=['.java'], recursive=True)

    num_newlines = 0
    anchored = False
    last_anchor = None

    for file in files:
        file_lines = open(file, 'r').readlines()

        for line in file_lines:
            # if line has stuff set anchored to True and let this be the last anchor
            if len(line) > 0:
                # this means that we've found a gap
                if not anchored and num_newlines > 1:
                    print("Found",num_newlines,"new lines between:")
                    print(last_anchor)
                    print("and")
                    print(line)
                    print("-------------------------------")

                    num_newlines = 0
                    


                anchored = True
                last_anchor = line
            # if we haven't encountered a true line yet, continue
            elif not anchored:
                continue
            # otherwise count it as a blank line
            else:
                num_newlines = num_newlines + 1



if __name__ == '__main__':
    main()
