"""
python_fucntions.py

python functions utilized by Cyder. 
These exist purely because it is simpler to create an API to 
wrap this script in Java and parse the results than to implement 
the functionality in Java since I couldn't find a good library to do these operations.
"""

import argparse
import os
from PIL import Image, ImageFilter
from mutagen.mp3 import MP3


def gaussian_blur(image_path: str, radius: int) -> str:
    """ 
    Returns the path to the blurred and saved image.

    :param image_path: the path to the image to blur
    :type image_path: str
    :param radius: the radius of the gaussian blur to apply
    :type radius: int
    :return: the path to the blurred image
    """
    filename, extension = os.path.basename(image_path).rsplit(".", 1)

    save_filename = filename + "_blurred_" + str(radius) \
        + "." + extension

    save_as = os.path.join(os.path.split(
        os.path.abspath(image_path))[0], save_filename)

    gaussImage = Image.open(image_path).filter(
        ImageFilter.GaussianBlur(radius))

    gaussImage.save(save_as)

    return save_as


def get_audio_length(path: str) -> float:
    """ 
    Returns the length of the provided audio file in seconds.

    :param path: the path to the audio file
    :type path: str
    :return: how long the audio file is in seconds
    :rtype: float
    """
    return MP3(path).info.length


# The commands supported by python_functions
COMMANDS = ['blur', 'audio_length']


def main():
    parser = argparse.ArgumentParser(prog='Cyder Python Utility Functions',
                                     description="Cyder Python Utility Functions")
    parser.add_argument('-c', '--command', required=True,
                        help='the command to invoke')
    parser.add_argument('-i', '--input', required=True, help='the input file')
    parser.add_argument('-r', '--radius', help='the radius of the kernel for " \
        + "the Gaussian blur command (must be an odd number)')

    args = parser.parse_args()

    input_file = args.input

    if not os.path.exists(input_file):
        print("Provided input file does not exist: \"" + input_file + "\"")
        return

    if args.command == COMMANDS[0]:
        if not args.radius:
            print("Missing kernel radius for Gaussian blur command, use --radius")
            return
        
        radius = -1

        try:
            radius = int(args.radius)
        except Exception:
            print("Failed to parse radius as an odd integer, input: \"" + args.radius + "\"")
            return

        if not radius % 2:
            print("Gaussian blur radius must be an odd number")
            return

        save_as = gaussian_blur(input_file, radius)
        print("Blurred: ", save_as)
                
    elif args.command == COMMANDS[1]:
        length = str(get_audio_length(input_file))
        print("Audio length: " + length)

    else:
        print(f"Unsupported command: {args.command}")

if __name__ == '__main__':
    main()
