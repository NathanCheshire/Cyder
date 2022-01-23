#simple script (relatively) to extract lines 
# from InputHandler.java that match the below 
# pattern to extract commands from the file. 
# A csv will be outputed with valid strings the commandIs method accepts.

import re
import sys
import os
from difflib import SequenceMatcher as SM

def main():
    args = sys.argv

    if (len(args) != 2):
        print('Script usage: commandFinder.py COMMAND_TO_FIND_A_SIMILAR_ONE')
    else:
        similarCommand = ''
        correspondingRatio = 0.0

        #path to the file
        inputHandler = open(os.path.dirname(os.getcwd()) + "\\handlers\\internal\\InputHandler.java",'r')

        #valid regexes to use, may need to add to this in the
        validRegs = [r'.*commandIs\("(.*)"\).*', r'.*commandMatches\("(.*)"\).*']

        #get all lines of input handler
        lines = inputHandler.readlines()

        for part in lines:
            for line in part.split('||'):
                for regex in validRegs:
                    comp = re.compile(regex)
                    for match in comp.findall(line):
                        ratio = SM(None, match, args[1]).ratio()

                        if (ratio > correspondingRatio):
                            correspondingRatio = ratio
                            similarCommand = match
        
        print(similarCommand, ', ratio: ',correspondingRatio, sep ='')                

if __name__ == "__main__":
    main()