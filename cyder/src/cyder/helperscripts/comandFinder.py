#simple script (relatively) to extract lines 
# from InputHandler.java that match the below 
# pattern to extract commands from the file. 
# A csv will be outputed with valid strings the commandIs method accepts.

import re
import sys
import os

def main():
    args = sys.argv

    if (len(args) != 2):
        print('Script usage: commandFinder.py path/to/output/txt')
    else:
        commands = []

        inputHandler = open(os.path.dirname(os.getcwd()) + "\\handlers\\internal\\InputHandler.java",'r')
        validRegs = [r'.*commandIs\("(.*)"\).*', r'.*commandMatches\("(.*)"\).*']

        lines = inputHandler.readlines()

        for part in lines:
            for line in part.split('||'):
                for regex in validRegs:
                    comp = re.compile(regex)
                    for match in comp.findall(line):
                        commands.append(match)
  
        print('Found', len(commands), 'commands')

        save = open(args[1], 'w+')

        commands.sort()

        for line in commands:
            save.write(line + '\n')

        print('Saved as', args[1])

if __name__ == "__main__":
    main()