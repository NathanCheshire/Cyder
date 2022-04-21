import re
import sys
import os
from difflib import SequenceMatcher as SM

def main():
    args = sys.argv

    if (len(args) != 3):
        print('Script usage: python command_finder.py COMMAND_TO_FIND_A_SIMILAR_ONE JAR_MODE')
    else:
        similarCommand = ''
        correspondingRatio = 0.0

        # where to output the resultilng csv
        COMMANDS_PATH = 'static\\csv\\commands.csv';

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

            # get all lines in inputHandler
            for part in lines:

                # sep at the ors if any
                for line in part.split('||'):

                    # might be multiple regex
                    for regex in validRegs:
                        # compile regex to see if match
                        comp = re.compile(regex)

                        for match in comp.findall(line):
                            if match not in ops:
                                ops.append(match)

            # now check for widgets recursively
            # now check for widgets recursively
            for file in findFiles('cyder', extensions = '.java', recursive = True):
                # current java file
                javaFile = open(file, "r")

                # current file lines
                lines = javaFile.readlines()
                javaFile.close()

                for line in lines:
                    search = re.search('@Widget\((.*)', line, re.DOTALL)
                    if search:
                        search = re.search('triggers = {(.*)},', search.group(1), re.DOTALL)
                        if search:
                            triggers = search.group(1)

                            for trigger in triggers.split(','):
                                trigger = trigger.replace('"','').strip()
                                
                                if trigger not in ops:
                                    ops.append(trigger)

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

def findFiles(startingDirectory, extensions = [], recursive = False):
    '''
    Finds all files within the provided directory that 
    end in one of the provided extensions.
    '''

    ret = []

    if len(extensions) == 0:
        print('Error: must provide valid extensions')
        return

    if os.path.isdir(startingDirectory):
        for subDir in os.listdir(startingDirectory):
            if recursive:
                ret = ret + findFiles(os.path.join(startingDirectory, subDir), extensions, recursive)
            else:
                ret.append(os.path.join(startingDirectory, subDir))
    else:
        for extension in extensions:
            if startingDirectory.endswith(extension):
                ret.append(startingDirectory)
                
    return ret

if __name__ == "__main__":
    main()
