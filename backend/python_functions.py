
def gaussian_blur(image_path: str, radius: int) -> str:
    """ 
    Returns the path to the blurred and saved image.

    :param image_path: the path to the image to blur
    :type image_path: str
    :param radius: the radius of the gaussian blur to apply
    :type radius: int
    :return: the path to the blurred image
    """
    import os
    filename, extension = os.path.basename(image_path).rsplit(".", 1)

    save_filename = filename + "_blurred_" + str(radius) \
        + "." + extension

    save_as = os.path.join(os.path.split(os.path.abspath(image_path))[0], save_filename)

    from PIL import Image
    from PIL import ImageFilter

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
    from mutagen.mp3 import MP3
    return MP3(path).info.length

if __name__ == '__main__':
    import sys

    args = sys.argv
    command = args[1]

    if command == "blur":
        image_path = args[2]
        radius = args[3]
        print(image_path + "," + radius)
        gaussian_blur(image_path, int(radius))
        print("Blurred " + image_path)
    elif command == "audio_length":
        path = args[2]
        length = str(get_audio_length(path))
        print("Audio length: " + length)
    else:
        raise Exception("Invalid command")