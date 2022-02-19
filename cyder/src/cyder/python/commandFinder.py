import re
import sys
import os
from difflib import SequenceMatcher as SM
from tkinter import COMMAND

def main():
    args = sys.argv

    if (len(args) != 3):
        print('Script usage: commandFinder.py COMMAND_TO_FIND_A_SIMILAR_ONE JAR_MODE')
    else:
        similarCommand = ''
        correspondingRatio = 0.0

        COMMANDS_PATH = 'cyder\\src\\cyder\\python\\commands.csv';

        jarMode = False if str(args[2]) == 'false' else True

        # valid regexes to use, may need to add to this in the future
        validRegs = [r'.*commandIs\("(.*)"\).*']

        # recompile the csv now from InputHandler
        if not jarMode:
            # path to the file from project root
            inputHandler = open("cyder\\src\\cyder\\handlers\\internal\\InputHandler.java",'r')
            
            # get all lines of input handler
            lines = inputHandler.readlines()
            inputHandler.close()

            # the ops to write to the csv we are generating
            ops = []

            # get all lines in file
            for part in lines:

                # sep at the ors if any
                for line in part.split('||'):

                    # might be multiple regex
                    for regex in validRegs:
                        # compile regex to see if match
                        comp = re.compile(regex)

                        for match in comp.findall(line):
                            ops.append(match)

            file = open(COMMANDS_PATH, 'w+')

            # write ops to file
            for op in ops:
                file.write(op + '\n')
            
            file.close()
        
        if os.path.exists(COMMANDS_PATH):
            file = open(COMMANDS_PATH, 'r')

            lines = file.readlines()
            file.close()

            for op in lines:
                ratio = SM(None, op, args[1]).ratio()

                if (ratio > correspondingRatio):
                    correspondingRatio = ratio
                    similarCommand = op.strip()
                            

            print(similarCommand, ',', correspondingRatio, sep ='')
        else:
            raise Exception('Commands.csv was not found, did you create it before building the Jar?')             

if __name__ == "__main__":
    main()
