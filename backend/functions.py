
from datetime import datetime
import calendar
from typing import Tuple
from PIL import Image
from PIL import ImageFilter
from mutagen.mp3 import MP3
import os
import libusb_package


def gaussian_blur(image_path: str, radius: int, save_directory: str = None) -> str:
    """ 
    Returns the path to the blurred and saved image.

    :param image_path: the path to the image to blur
    :type image_path: str
    :param radius: the radius of the gaussian blur to apply
    :type radius: int
    :return: the path to the blurred image
    :rtype: str
    """

    save_filename = __get_filename(image_path) + "_blurred_" + str(radius) \
        + "." + __get_extension(image_path)

    save_as = os.path.join(save_directory if save_directory is not None
                           else os.path.split(os.path.abspath(image_path))[0], save_filename)

    print("save name:", save_as)

    gaussImage = Image.open(image_path).filter(
        ImageFilter.GaussianBlur(radius))

    gaussImage.save(save_as)

    return save_as


def __get_filename(full_path: str) -> str:
    """ 
    Returns the filename (without the extension) of the provided file path.
    For example, providing "c:\\users\\nathan\\downloads\\something.png" will return "something".

    :param full_path: the full, absolute path of the file
    :type full_path: str
    :return: the filename
    :rtype: str
    """
    name_and_extension = os.path.basename(full_path)

    name, extension = __separate_name_from_extension(name_and_extension)

    return name


def __get_extension(full_path: str) -> str:
    """ 
    Returns the extension of the provided file path.
    For example, providing "c:\\users\\nathan\\downloads\\something.png" will return "png".

    :param full_path: the full, absolute path of the file
    :type full_path: str
    :return: the extension
    :rtype: str
    """
    name_and_extension = os.path.basename(full_path)

    name, extension = __separate_name_from_extension(name_and_extension)

    return extension


def __separate_name_from_extension(file_name: str) -> Tuple:
    """ 
    Returns the filename separated from the extension.

    :param file_name: the filename and extension. For example, "something.png"
    :type file_name: str
    :return: a tuple consisting of the name and extension
    :rtype: tuple
    """

    return file_name.rsplit('.', 1)


def get_unix_gmt_time() -> int:
    """ 
    Returns the current unix gmt time.

    :return: the current unix gmt time
    :rtype: int
    """
    return calendar.timegm(datetime.utcnow().utctimetuple())


def get_audio_length(path: str) -> float:
    """ 
    Returns the length of the provided audio file in seconds.

    :param path: the path to the audio file
    :type path: str
    :return: how long the audio file is in seconds
    :rtype: float
    """
    return MP3(path).info.length


def get_usb_devices() -> list:
    """ 
    Returns a list of the devices connected to this comuter via USB.
    
    :return: a list of the devices connected to this computer via USB.
    :rtype: list
    """
    ret = []

    devices = libusb_package.find(find_all=True)

    for dev in devices:
        lines = str(dev).split('\n')

        for line in lines:
            ret.append(line.strip())

    return ret
